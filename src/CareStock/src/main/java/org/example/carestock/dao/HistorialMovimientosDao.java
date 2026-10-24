package org.example.carestock.dao;

import org.example.carestock.config.ConexionBaseDatos;
import org.example.carestock.dto.MovimientoHistorial;
import org.example.carestock.session.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * HU-795: Consulta unificada, paginada y segregada
 * por la farmacia activa.
 */
public final class HistorialMovimientosDao {

    // Fuentes canonicas:
    // - log_movimientos: medicamentos
    // - movimientos_productos: aseo y maternidad
    // - auditoria_ajustes: ajustes justificados
    //
    // No unir automaticamente movimientos_stock ni
    // movimientos_catalogo: son fuentes alternativas
    // sin clave de correlacion compartida.

    static final String SQL = """
        SELECT h.origen, h.id_origen, h.categoria,
               h.tipo_movimiento, h.id_referencia,
               h.producto, h.referencia, h.cantidad,
               h.motivo, h.stock_anterior, h.stock_posterior,
               h.diferencia, h.id_farmacia, h.id_usuario,
               h.usuario, h.fecha_movimiento
        FROM (
            SELECT 'LOG_MOVIMIENTOS'::text AS origen,
                   lm.id_log::bigint AS id_origen,
                   'MEDICAMENTO'::text AS categoria,
                   lm.tipo_movimiento::text AS tipo_movimiento,
                   l.id_lote AS id_referencia,
                   m.nombre_comercial::text AS producto,
                   l.numero_lote::text AS referencia,
                   lm.cantidad_afectada::bigint AS cantidad,
                   lm.detalle_cambio::text AS motivo,
                   NULL::integer AS stock_anterior,
                   NULL::integer AS stock_posterior,
                   NULL::bigint AS diferencia,
                   m.id_farmacia,
                   lm.id_usuario,
                   u.nombre_completo::text AS usuario,
                   lm.fecha_hora::timestamptz AS fecha_movimiento
            FROM log_movimientos lm
            JOIN lotes l
                ON l.id_lote = lm.id_lote
            JOIN medicamentos m
                ON m.id_medicamento = l.id_medicamento
            JOIN usuarios u
                ON u.id_usuario = lm.id_usuario

            UNION ALL

            SELECT 'MOVIMIENTOS_PRODUCTOS'::text AS origen,
                   mp.id_movimiento::bigint AS id_origen,
                   CASE WHEN mp.id_aseo IS NOT NULL
                        THEN 'ASEO' ELSE 'MATERNIDAD'
                   END AS categoria,
                   mp.tipo_movimiento::text AS tipo_movimiento,
                   COALESCE(mp.id_aseo, mp.id_maternidad)
                       AS id_referencia,
                   COALESCE(a.nombre, ma.nombre)::text
                       AS producto,
                   COALESCE(a.codigo, ma.codigo)::text
                       AS referencia,
                   mp.cantidad::bigint AS cantidad,
                   mp.motivo::text AS motivo,
                   mp.stock_anterior,
                   mp.stock_posterior,
                   (mp.stock_posterior::bigint
                        - mp.stock_anterior::bigint)
                       AS diferencia,
                   mp.id_farmacia,
                   mp.id_usuario,
                   u.nombre_completo::text AS usuario,
                   mp.fecha_movimiento::timestamptz
                       AS fecha_movimiento
            FROM movimientos_productos mp
            LEFT JOIN aseo a
                ON a.id_aseo = mp.id_aseo
            LEFT JOIN maternidad ma
                ON ma.id_maternidad = mp.id_maternidad
            JOIN usuarios u
                ON u.id_usuario = mp.id_usuario
            WHERE (mp.id_aseo IS NOT NULL
                   AND a.id_farmacia = mp.id_farmacia)
               OR (mp.id_maternidad IS NOT NULL
                   AND ma.id_farmacia = mp.id_farmacia)

            UNION ALL

            SELECT 'AUDITORIA_AJUSTES'::text AS origen,
                   aj.id_ajuste::bigint AS id_origen,
                   CASE
                       WHEN aj.id_lote IS NOT NULL
                           THEN 'MEDICAMENTO'
                       WHEN aj.id_aseo IS NOT NULL
                           THEN 'ASEO'
                       ELSE 'MATERNIDAD'
                   END AS categoria,
                   'AJUSTE'::text AS tipo_movimiento,
                   COALESCE(aj.id_lote, aj.id_aseo,
                            aj.id_maternidad)
                       AS id_referencia,
                   COALESCE(m.nombre_comercial,
                            a.nombre, ma.nombre)::text
                       AS producto,
                   COALESCE(l.numero_lote,
                            a.codigo, ma.codigo)::text
                       AS referencia,
                   ABS(aj.diferencia::bigint) AS cantidad,
                   aj.motivo::text AS motivo,
                   aj.cantidad_anterior AS stock_anterior,
                   aj.cantidad_nueva AS stock_posterior,
                   aj.diferencia::bigint AS diferencia,
                   aj.id_farmacia,
                   aj.id_usuario,
                   u.nombre_completo::text AS usuario,
                   aj.fecha_ajuste::timestamptz
                       AS fecha_movimiento
            FROM auditoria_ajustes aj
            LEFT JOIN lotes l
                ON l.id_lote = aj.id_lote
            LEFT JOIN medicamentos m
                ON m.id_medicamento = l.id_medicamento
            LEFT JOIN aseo a
                ON a.id_aseo = aj.id_aseo
            LEFT JOIN maternidad ma
                ON ma.id_maternidad = aj.id_maternidad
            JOIN usuarios u
                ON u.id_usuario = aj.id_usuario
            WHERE (aj.id_lote IS NOT NULL
                   AND m.id_farmacia = aj.id_farmacia)
               OR (aj.id_aseo IS NOT NULL
                   AND a.id_farmacia = aj.id_farmacia)
               OR (aj.id_maternidad IS NOT NULL
                   AND ma.id_farmacia = aj.id_farmacia)
        ) h
        WHERE h.id_farmacia = ?
        ORDER BY h.fecha_movimiento DESC,
                 h.origen, h.id_origen DESC
        LIMIT ? OFFSET ?
        """;

    /**
     * Consulta el historial de la farmacia activa.
     * Maximo 500 registros por pagina.
     */
    public List<MovimientoHistorial> listar(
            int limite,
            int desplazamiento
    ) throws SQLException {

        validarPaginacion(limite, desplazamiento);

        if (!SesionUsuario.estaActiva()) {
            throw new IllegalStateException(
                    "Debe iniciar sesion.");
        }

        var sesion = SesionUsuario.actual();

        Integer farmacia = sesion.idFarmacia();

        if (farmacia == null || farmacia <= 0) {
            throw new IllegalStateException(
                    "No hay farmacia asignada.");
        }

        List<MovimientoHistorial> resultado;

        try (Connection con = ConexionBaseDatos.getConexion()) {
            resultado = consultar(
                    con, farmacia, limite, desplazamiento);
        }

        // No entregar resultados si cambio la sesion.
        if (!SesionUsuario.estaActiva()
                || SesionUsuario.actual().id() != sesion.id()
                || !Objects.equals(
                        SesionUsuario.actual().idFarmacia(),
                        farmacia)) {

            throw new IllegalStateException(
                    "La sesion cambio durante la consulta.");
        }

        return resultado;
    }

    static void validarPaginacion(
            int limite,
            int desplazamiento
    ) {
        if (limite < 1
                || limite > 500
                || desplazamiento < 0) {

            throw new IllegalArgumentException(
                    "Limite entre 1 y 500, "
                    + "desplazamiento no negativo.");
        }
    }

    /**
     * Consulta interna con conexion proporcionada.
     * Permite pruebas de integracion JDBC.
     */
    static List<MovimientoHistorial> consultar(
            Connection con,
            int farmacia,
            int limite,
            int desplazamiento
    ) throws SQLException {

        Objects.requireNonNull(
                con, "La conexion es obligatoria.");

        if (farmacia <= 0) {
            throw new IllegalArgumentException(
                    "Farmacia invalida.");
        }

        validarPaginacion(limite, desplazamiento);

        List<MovimientoHistorial> registros =
                new ArrayList<>();

        try (PreparedStatement ps =
                     con.prepareStatement(SQL)) {

            ps.setInt(1, farmacia);
            ps.setInt(2, limite);
            ps.setInt(3, desplazamiento);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    registros.add(new MovimientoHistorial(
                            rs.getString("origen"),
                            rs.getLong("id_origen"),
                            rs.getString("categoria"),
                            rs.getString("tipo_movimiento"),
                            rs.getInt("id_referencia"),
                            rs.getString("producto"),
                            rs.getString("referencia"),
                            rs.getLong("cantidad"),
                            rs.getString("motivo"),
                            (Integer) rs.getObject(
                                    "stock_anterior"),
                            (Integer) rs.getObject(
                                    "stock_posterior"),
                            (Long) rs.getObject(
                                    "diferencia"),
                            rs.getInt("id_farmacia"),
                            rs.getInt("id_usuario"),
                            rs.getString("usuario"),
                            rs.getObject(
                                    "fecha_movimiento",
                                    OffsetDateTime.class)
                    ));
                }
            }
        }

        return List.copyOf(registros);
    }
}
