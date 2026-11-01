package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;

import java.sql.*;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

final class EnvioProtegidoSms {

    private static final String LIMPIAR = """
        UPDATE sms_alertas a
        SET activa = FALSE,
            estado = 'FALLIDA',
            error_detalle =
                'Destinatario sin consentimiento o preferencia',
            procesado_en = NOW()
        WHERE a.activa
          AND a.estado = 'PENDIENTE'
          AND NOT EXISTS (
            SELECT 1
            FROM sms_destinatarios d
            JOIN usuarios u
              ON u.id_usuario = d.id_usuario
             AND u.id_farmacia = d.id_farmacia
            JOIN farmacias f
              ON f.id_farmacia = d.id_farmacia
            WHERE d.id_destinatario = a.id_destinatario
              AND d.id_farmacia = a.id_farmacia
              AND d.activo
              AND d.autorizado
              AND d.autorizado_en IS NOT NULL
              AND u.estado = 'ACTIVO'
              AND f.estado = 'ACTIVA'
              AND (
                (
                    (a.clave LIKE 'STOCK:%'
                     OR a.clave LIKE 'AVISO:STOCK:%')
                    AND d.notifica_stock_critico
                )
                OR
                (
                    (a.clave LIKE 'LOTE:%'
                     OR a.clave LIKE 'AVISO:VENCIMIENTO:%')
                    AND d.notifica_vencimiento_critico
                )
              )
          )
        """;

    private static final String BLOQUEAR = """
        SELECT a.estado, a.activa, a.clave, a.mensaje,
               d.telefono,
               d.activo AS destinatario_activo,
               d.autorizado,
               d.autorizado_en,
               d.notifica_stock_critico,
               d.notifica_vencimiento_critico,
               d.horario_inicio,
               d.horario_fin,
               d.zona_horaria,
               u.estado AS usuario_estado,
               f.estado AS farmacia_estado
        FROM sms_alertas a
        JOIN sms_destinatarios d
          ON d.id_destinatario = a.id_destinatario
         AND d.id_farmacia = a.id_farmacia
        JOIN usuarios u
          ON u.id_usuario = d.id_usuario
         AND u.id_farmacia = d.id_farmacia
        JOIN farmacias f
          ON f.id_farmacia = d.id_farmacia
        WHERE a.id_alerta = ?
        FOR UPDATE OF a, d, u, f
        """;

    private final DespachadorProtegidoSms despachador;
    private final Map<String, String> config;

    EnvioProtegidoSms(
            ServicioSms proveedor,
            Map<String, String> configuracion) {

        this.despachador =
                new DespachadorProtegidoSms(proveedor);

        this.config = Map.copyOf(
                Objects.requireNonNull(configuracion)
        );
    }

    void limpiarPendientes() throws SQLException {
        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps =
                     con.prepareStatement(LIMPIAR)) {

            ps.executeUpdate();
        }
    }

    void procesar(long alertaId) throws SQLException {
        try (Connection con = ConexionBD.getConexion()) {
            con.setAutoCommit(false);

            try (PreparedStatement ps =
                         con.prepareStatement(BLOQUEAR)) {

                ps.setLong(1, alertaId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()
                            || !rs.getBoolean("activa")
                            || !"EN_PROCESO".equals(
                                    rs.getString("estado"))) {

                        con.commit();
                        return;
                    }

                    String clave = rs.getString("clave");

                    boolean elegible =
                            ReglasNotificacionSms.puedeGenerar(
                                    rs.getBoolean("destinatario_activo"),
                                    rs.getBoolean("autorizado"),
                                    rs.getTimestamp("autorizado_en") != null,
                                    "ACTIVO".equalsIgnoreCase(
                                            rs.getString("usuario_estado")),
                                    "ACTIVA".equalsIgnoreCase(
                                            rs.getString("farmacia_estado")),
                                    ReglasNotificacionSms.tipo(clave),
                                    rs.getBoolean("notifica_stock_critico"),
                                    rs.getBoolean("notifica_vencimiento_critico")
                            );

                    if (!elegible) {
                        actualizar(
                                con, alertaId, "FALLIDA",
                                null,
                                "Autorizacion o preferencia revocada",
                                false
                        );

                        con.commit();
                        return;
                    }

                    Time inicio = rs.getTime("horario_inicio");
                    Time fin = rs.getTime("horario_fin");

                    var desde = inicio == null
                            ? null : inicio.toLocalTime();

                    var hasta = fin == null
                            ? null : fin.toLocalTime();

                    String zona = rs.getString("zona_horaria");

                    boolean critica =
                            ReglasNotificacionSms.esCritica(clave);

                    if (!ReglasNotificacionSms.puedeEnviar(
                            elegible, critica, desde, hasta,
                            zona, Instant.now())) {

                        actualizar(
                                con, alertaId, "PENDIENTE",
                                null, null, true
                        );

                        con.commit();
                        return;
                    }

                    String mensaje = "SI".equalsIgnoreCase(
                            config.getOrDefault(
                                    "SMS_MODO_TRIAL", "NO"))
                            ? "sms_internal_alerts"
                            : rs.getString("mensaje");

                    try {
                        ResultadoSms resultado =
                                despachador.enviarSiPermitido(
                                        elegible,
                                        critica,
                                        desde,
                                        hasta,
                                        zona,
                                        Instant.now(),
                                        rs.getString("telefono"),
                                        mensaje
                                ).orElseThrow(() ->
                                        new IllegalStateException(
                                                "Envio sin autorizacion horaria"
                                        ));

                        String estado = resultado.simulado()
                                ? "SIMULADA"
                                : resultado.aceptado()
                                        ? "ACEPTADA"
                                        : "FALLIDA";

                        actualizar(
                                con, alertaId, estado,
                                resultado.referencia(),
                                null, true
                        );

                    } catch (RuntimeException e) {
                        actualizar(
                                con, alertaId, "FALLIDA",
                                null,
                                e.getClass().getSimpleName()
                                        + ": " + e.getMessage(),
                                true
                        );
                    }

                    con.commit();
                }

            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        }
    }

    private static void actualizar(
            Connection con,
            long id,
            String estado,
            String referencia,
            String error,
            boolean activa) throws SQLException {

        String sql = """
            UPDATE sms_alertas
            SET estado = ?,
                proveedor_referencia = ?,
                error_detalle = ?,
                activa = ?,
                procesado_en = NOW()
            WHERE id_alerta = ?
              AND estado = 'EN_PROCESO'
            """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setString(2, referencia);

            ps.setString(
                    3,
                    error == null ? null : error.substring(
                            0, Math.min(250, error.length()))
            );

            ps.setBoolean(4, activa);
            ps.setLong(5, id);

            ps.executeUpdate();
        }
    }
}
