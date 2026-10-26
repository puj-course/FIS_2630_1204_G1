package org.example.carestock.sms;

import org.example.carestock.config.ConexionBaseDatos;
import java.sql.*;
import java.util.*;

/**
 * Proceso de alertas independiente de JavaFX.
 * No modifica el stock ni las transacciones de inventario.
 */
public final class ServicioAlertasSms {

    private static final String EVENTOS = """
        SELECT m.id_farmacia AS farmacia,
               'STOCK:MED:' || m.id_medicamento AS clave,
               'STOCK' AS tipo,
               'Medicamento' AS categoria,
               m.nombre_comercial AS producto,
               '' AS lote,
               m.stock_total AS unidades,
               0 AS dias
        FROM medicamentos m
        WHERE m.estado = 'ACTIVO'
          AND m.stock_total = 0

        UNION ALL

        SELECT a.id_farmacia,
               'STOCK:ASEO:' || a.id_aseo,
               'STOCK', 'Aseo', a.nombre,
               '', a.stock, 0
        FROM aseo a
        WHERE a.estado = 'ACTIVO'
          AND a.stock = 0

        UNION ALL

        SELECT mt.id_farmacia,
               'STOCK:MAT:' || mt.id_maternidad,
               'STOCK', 'Maternidad', mt.nombre,
               '', mt.stock, 0
        FROM maternidad mt
        WHERE mt.estado = 'ACTIVO'
          AND mt.stock = 0

        UNION ALL

        SELECT m.id_farmacia,
               'LOTE:' || l.id_lote,
               'VENCIMIENTO',
               'Medicamento',
               m.nombre_comercial,
               l.numero_lote,
               l.cantidad_actual,
               (l.fecha_vencimiento - CURRENT_DATE)::integer
        FROM lotes l
        JOIN medicamentos m
          ON m.id_medicamento = l.id_medicamento
        WHERE m.estado = 'ACTIVO'
          AND l.estado_lote IN ('DISPONIBLE', 'VENCIDO')
          AND l.cantidad_actual > 0
          AND l.fecha_vencimiento <= CURRENT_DATE + 15
        """;

    private static final String DESTINATARIOS = """
        SELECT d.id_destinatario,
               d.id_farmacia,
               f.nombre AS farmacia
        FROM sms_destinatarios d
        JOIN usuarios u
          ON u.id_usuario = d.id_usuario
         AND u.id_farmacia = d.id_farmacia
         AND u.estado = 'ACTIVO'
        JOIN farmacias f
          ON f.id_farmacia = d.id_farmacia
         AND f.estado = 'ACTIVA'
        WHERE d.activo = TRUE
          AND d.autorizado = TRUE
          AND d.autorizado_en IS NOT NULL
        """;

    private static final String INSERTAR = """
        INSERT INTO sms_alertas
            (id_destinatario, id_farmacia, clave, mensaje)
        VALUES (?, ?, ?, ?)
        ON CONFLICT (id_destinatario, clave)
        WHERE activa DO NOTHING
        """;

    private static final String RESOLVER = """
        UPDATE sms_alertas
        SET activa = FALSE
        WHERE id_destinatario = ?
          AND activa = TRUE
          AND NOT (clave = ANY (?))
        """;

    private static final String RECLAMAR = """
        WITH elegido AS (
            SELECT a.id_alerta
            FROM sms_alertas a
            JOIN sms_destinatarios d
              ON d.id_destinatario = a.id_destinatario
             AND d.id_farmacia = a.id_farmacia
            JOIN usuarios u
              ON u.id_usuario = d.id_usuario
             AND u.id_farmacia = d.id_farmacia
             AND u.estado = 'ACTIVO'
            JOIN farmacias f
              ON f.id_farmacia = d.id_farmacia
             AND f.estado = 'ACTIVA'
            WHERE a.activa
              AND a.estado = 'PENDIENTE'
              AND d.activo
              AND d.autorizado
              AND d.autorizado_en IS NOT NULL
            ORDER BY a.id_alerta
            FOR UPDATE OF a SKIP LOCKED
            LIMIT 1
        )
        UPDATE sms_alertas a
        SET estado = 'EN_PROCESO',
            procesado_en = NOW()
        FROM elegido e
        WHERE a.id_alerta = e.id_alerta
        RETURNING a.id_alerta,
                  a.mensaje,
                  (
                      SELECT d.telefono
                      FROM sms_destinatarios d
                      WHERE d.id_destinatario = a.id_destinatario
                  ) AS telefono
        """;

    private final ServicioSms sms;
    private final Map<String, String> config;
    private final FormateadorAlertasSms formateador =
            new FormateadorAlertasSms();

    public ServicioAlertasSms(
            ServicioSms sms,
            Map<String, String> config
    ) {
        this.sms = Objects.requireNonNull(sms);
        this.config = Map.copyOf(config);
    }

    public void ejecutarCiclo() throws SQLException {

        sincronizar();

        try (Connection con =
                     ConexionBaseDatos.getConexion();
             PreparedStatement ps = con.prepareStatement("""
                 UPDATE sms_alertas
                 SET estado = 'INDETERMINADO',
                     error_detalle =
                         'Interrupcion durante solicitud al proveedor'
                 WHERE estado = 'EN_PROCESO'
                   AND procesado_en <
                       NOW() - INTERVAL '10 minutes'
                 """)) {

            ps.executeUpdate();
        }

        int maxPorCiclo = Integer.parseInt(
                config.getOrDefault("SMS_MAX_POR_CICLO", "1")
        );

        if (maxPorCiclo < 1 || maxPorCiclo > 100) {
            throw new IllegalArgumentException(
                    "SMS_MAX_POR_CICLO debe estar entre 1 y 100."
            );
        }

        for (int i = 0; i < maxPorCiclo; i++) {

            Envio envio = reclamar();

            if (envio == null) {
                break;
            }

            try {
                String cuerpo = "SI".equalsIgnoreCase(
                        config.getOrDefault(
                                "SMS_MODO_TRIAL", "NO"
                        )
                )
                        ? "sms_internal_alerts"
                        : envio.mensaje();

                ResultadoSms resultado =
                        sms.enviar(envio.telefono(), cuerpo);

                String estado = resultado.simulado()
                        ? "SIMULADA"
                        : resultado.aceptado()
                                ? "ACEPTADA"
                                : "FALLIDA";

                actualizar(
                        envio.id(),
                        estado,
                        resultado.referencia(),
                        null
                );

                System.out.println(
                        "SMS " + envio.id()
                        + " estado=" + estado
                );

            } catch (RuntimeException e) {

                actualizar(
                        envio.id(),
                        "FALLIDA",
                        null,
                        e.getClass().getSimpleName()
                                + ": " + e.getMessage()
                );

                System.err.println(
                        "SMS " + envio.id() + " fallido."
                );
            }
        }
    }

    private void sincronizar() throws SQLException {

        try (Connection con =
                     ConexionBaseDatos.getConexion()) {

            con.setAutoCommit(false);

            try {
                Map<Integer, List<EventoSms>> porFarmacia =
                        new HashMap<>();

                try (PreparedStatement ps =
                             con.prepareStatement(EVENTOS);
                     ResultSet rs = ps.executeQuery()) {

                    while (rs.next()) {

                        EventoSms evento = new EventoSms(
                                rs.getInt("farmacia"),
                                rs.getString("clave"),
                                rs.getString("tipo"),
                                rs.getString("categoria"),
                                rs.getString("producto"),
                                rs.getString("lote"),
                                rs.getInt("unidades"),
                                rs.getInt("dias")
                        );

                        porFarmacia.computeIfAbsent(
                                evento.farmaciaId(),
                                k -> new ArrayList<>()
                        ).add(evento);
                    }
                }

                try (PreparedStatement destinatarios =
                             con.prepareStatement(DESTINATARIOS);
                     ResultSet rs = destinatarios.executeQuery();
                     PreparedStatement insertar =
                             con.prepareStatement(INSERTAR);
                     PreparedStatement resolver =
                             con.prepareStatement(RESOLVER)) {

                    while (rs.next()) {

                        long id =
                                rs.getLong("id_destinatario");

                        int idFarmacia =
                                rs.getInt("id_farmacia");

                        String nombre =
                                rs.getString("farmacia");

                        List<EventoSms> eventos =
                                porFarmacia.getOrDefault(
                                        idFarmacia,
                                        List.of()
                                );

                        String[] claves = eventos.stream()
                                .map(EventoSms::clave)
                                .toArray(String[]::new);

                        resolver.setLong(1, id);

                        resolver.setArray(
                                2,
                                con.createArrayOf("text", claves)
                        );

                        resolver.executeUpdate();

                        for (EventoSms evento : eventos) {

                            insertar.setLong(1, id);
                            insertar.setInt(2, idFarmacia);

                            insertar.setString(
                                    3, evento.clave()
                            );

                            insertar.setString(
                                    4,
                                    formateador.generar(
                                            evento,
                                            nombre,
                                            config
                                    )
                            );

                            insertar.executeUpdate();
                        }
                    }
                }

                con.commit();

            } catch (Exception e) {

                con.rollback();

                if (e instanceof SQLException se) {
                    throw se;
                }

                throw new SQLException(
                        "No se pudo sincronizar alertas SMS", e
                );
            }
        }
    }

    private record Envio(
            long id,
            String telefono,
            String mensaje
    ) {
    }

    private Envio reclamar() throws SQLException {

        try (Connection con =
                     ConexionBaseDatos.getConexion();
             PreparedStatement ps =
                     con.prepareStatement(RECLAMAR);
             ResultSet rs = ps.executeQuery()) {

            return rs.next()
                    ? new Envio(
                            rs.getLong("id_alerta"),
                            rs.getString("telefono"),
                            rs.getString("mensaje")
                    )
                    : null;
        }
    }

    private void actualizar(
            long id,
            String estado,
            String referencia,
            String error
    ) throws SQLException {

        try (Connection con =
                     ConexionBaseDatos.getConexion();
             PreparedStatement ps = con.prepareStatement("""
                 UPDATE sms_alertas
                 SET estado = ?,
                     proveedor_referencia = ?,
                     error_detalle = ?,
                     procesado_en = NOW()
                 WHERE id_alerta = ?
                 """)) {

            ps.setString(1, estado);
            ps.setString(2, referencia);

            ps.setString(
                    3,
                    error == null
                            ? null
                            : error.substring(
                                    0,
                                    Math.min(250, error.length())
                            )
            );

            ps.setLong(4, id);
            ps.executeUpdate();
        }
    }
}
