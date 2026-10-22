package org.example.carestock.service;

import org.example.carestock.config.ConexionBaseDatos;
import org.example.carestock.dao.AuditoriaAjustesDao;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.AjusteExistencias;
import org.example.carestock.model.AjusteExistencias.Categoria;
import org.example.carestock.proxy.InventarioConsultas;
import org.example.carestock.session.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * HU-793: Servicio de ajustes de inventario.
 *
 * Garantiza transaccion JDBC, autorizacion,
 * control de concurrencia y auditoria.
 */
public final class ServicioAjustesInventario {

    private final AuditoriaAjustesDao auditoria =
            new AuditoriaAjustesDao();

    public record ResultadoAjuste(
            long idAuditoria,
            Categoria categoria,
            int idReferencia,
            int cantidadAnterior,
            int cantidadNueva,
            int diferencia,
            int idUsuario,
            int idFarmacia
    ) {}

    /**
     * Obtiene usuario y farmacia de la sesion activa.
     */
    public ResultadoAjuste ajustar(
            Categoria categoria,
            int idReferencia,
            int cantidadEsperada,
            int cantidadNueva,
            String motivo
    ) throws Exception {

        if (!SesionUsuario.estaActiva()) {
            throw new ReglaNegocioException(
                    "Debe iniciar sesion."
            );
        }

        var sesion = SesionUsuario.actual();

        if (sesion.idFarmacia() == null) {
            throw new ReglaNegocioException(
                    "El usuario no tiene farmacia asignada."
            );
        }

        return ajustar(new AjusteExistencias(
                categoria,
                idReferencia,
                sesion.id(),
                sesion.idFarmacia(),
                motivo,
                cantidadEsperada,
                cantidadNueva
        ));
    }

    /**
     * Ejecuta actualizacion y auditoria atomicamente.
     *
     * cantidadAnterior representa el saldo esperado
     * por el usuario y se compara contra el saldo
     * real obtenido bajo bloqueo.
     */
    public ResultadoAjuste ajustar(
            AjusteExistencias solicitud
    ) throws Exception {

        Objects.requireNonNull(
                solicitud,
                "El ajuste es obligatorio."
        );

        if (!SesionUsuario.estaActiva()) {
            throw new ReglaNegocioException(
                    "Debe iniciar sesion."
            );
        }

        var sesion = SesionUsuario.actual();

        if (sesion.id() != solicitud.idUsuario()
                || !Objects.equals(
                        sesion.idFarmacia(),
                        solicitud.idFarmacia()
                )) {

            throw new ReglaNegocioException(
                    "La identidad o farmacia no corresponden a la sesion."
            );
        }

        if (!rolAdministrador(sesion.rol())) {
            throw new ReglaNegocioException(
                    "Solo un administrador puede ajustar existencias."
            );
        }

        ResultadoAjuste resultado;

        try (Connection con =
                     ConexionBaseDatos.getConexion()) {

            con.setAutoCommit(false);

            try {

                verificarAutorizacion(
                        con,
                        solicitud.idUsuario(),
                        solicitud.idFarmacia()
                );

                final int stockReal;

                if (solicitud.categoria()
                        == Categoria.MEDICAMENTO) {

                    // Primero se bloquea el medicamento
                    // y despues su lote.
                    bloquearMedicamento(
                            con,
                            solicitud.idReferencia(),
                            solicitud.idFarmacia()
                    );

                    stockReal = bloquearLote(
                            con,
                            solicitud.idReferencia(),
                            solicitud.idFarmacia()
                    );

                } else {

                    stockReal = bloquearProducto(
                            con,
                            solicitud.categoria(),
                            solicitud.idReferencia(),
                            solicitud.idFarmacia()
                    );
                }

                comprobarSaldo(
                        stockReal,
                        solicitud.cantidadAnterior(),
                        solicitud.cantidadNueva()
                );

                if (solicitud.categoria()
                        == Categoria.MEDICAMENTO) {

                    actualizarLote(con, solicitud);

                    // El trigger de PostgreSQL
                    // recalcula medicamentos.stock_total.

                } else {

                    actualizarProducto(con, solicitud);
                }

                // Usa la misma conexion y transaccion.
                long idAuditoria =
                        auditoria.registrar(con, solicitud);

                con.commit();

                resultado = new ResultadoAjuste(
                        idAuditoria,
                        solicitud.categoria(),
                        solicitud.idReferencia(),
                        stockReal,
                        solicitud.cantidadNueva(),
                        solicitud.diferencia(),
                        solicitud.idUsuario(),
                        solicitud.idFarmacia()
                );

            } catch (Exception fallo) {

                try {
                    con.rollback();

                } catch (SQLException errorRollback) {
                    fallo.addSuppressed(errorRollback);
                }

                throw fallo;
            }
        }

        // Solo despues de confirmar la transaccion.
        InventarioConsultas.invalidarFarmacia(
                solicitud.idFarmacia()
        );

        return resultado;
    }

    static boolean rolAdministrador(String rol) {

        return rol != null && (
                "ADMINISTRADOR".equalsIgnoreCase(rol.trim())
                || "SUPER_ADMIN".equalsIgnoreCase(rol.trim())
        );
    }

    /**
     * Previene ajustes basados en existencias obsoletas.
     */
    static void comprobarSaldo(
            int real,
            int esperado,
            int nuevo
    ) throws ReglaNegocioException {

        if (real < 0 || esperado < 0 || nuevo < 0) {
            throw new ReglaNegocioException(
                    "Las existencias no pueden ser negativas."
            );
        }

        if (real != esperado) {
            throw new ReglaNegocioException(
                    "Las existencias cambiaron por otra operacion. "
                    + "Actualice la consulta e intente nuevamente."
            );
        }

        if (real == nuevo) {
            throw new ReglaNegocioException(
                    "El ajuste debe cambiar las existencias."
            );
        }
    }

    private static void verificarAutorizacion(
            Connection con,
            int usuario,
            int farmacia
    ) throws Exception {

        String sql = """
                SELECT 1
                FROM usuarios u
                JOIN farmacias f
                    ON f.id_farmacia = u.id_farmacia
                JOIN roles r
                    ON r.id_rol = u.id_rol
                WHERE u.id_usuario = ?
                  AND u.id_farmacia = ?
                  AND UPPER(BTRIM(u.estado)) = 'ACTIVO'
                  AND UPPER(BTRIM(f.estado)) = 'ACTIVA'
                  AND UPPER(BTRIM(r.nombre_rol))
                      IN ('ADMINISTRADOR', 'SUPER_ADMIN')
                FOR SHARE OF u, f
                """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, usuario);
            ps.setInt(2, farmacia);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "Usuario, rol o farmacia no autorizados."
                    );
                }
            }
        }
    }

    /**
     * Bloquea el medicamento antes de bloquear el lote.
     */
    private static void bloquearMedicamento(
            Connection con,
            int lote,
            int farmacia
    ) throws Exception {

        String sql = """
                SELECT m.id_medicamento
                FROM medicamentos m
                JOIN lotes l
                    ON l.id_medicamento = m.id_medicamento
                WHERE l.id_lote = ?
                  AND m.id_farmacia = ?
                  AND UPPER(BTRIM(m.estado)) = 'ACTIVO'
                FOR UPDATE OF m
                """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, lote);
            ps.setInt(2, farmacia);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "Lote inexistente o medicamento inactivo "
                            + "o perteneciente a otra farmacia."
                    );
                }
            }
        }
    }

    private static int bloquearLote(
            Connection con,
            int lote,
            int farmacia
    ) throws Exception {

        String sql = """
                SELECT l.cantidad_actual,
                       l.estado_lote,
                       (l.fecha_vencimiento > CURRENT_DATE)
                           AS vigente
                FROM lotes l
                JOIN medicamentos m
                    ON m.id_medicamento = l.id_medicamento
                WHERE l.id_lote = ?
                  AND m.id_farmacia = ?
                FOR UPDATE OF l
                """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, lote);
            ps.setInt(2, farmacia);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "No se encontro el lote en la farmacia."
                    );
                }

                String estado =
                        rs.getString("estado_lote");

                if (!"DISPONIBLE".equalsIgnoreCase(estado)
                        && !"AGOTADO".equalsIgnoreCase(estado)) {

                    throw new ReglaNegocioException(
                            "No se ajustan lotes vencidos o retenidos."
                    );
                }

                if (!rs.getBoolean("vigente")) {
                    throw new ReglaNegocioException(
                            "El lote esta vencido."
                    );
                }

                int cantidad =
                        rs.getInt("cantidad_actual");

                if (rs.wasNull()) {
                    throw new ReglaNegocioException(
                            "El lote no tiene una cantidad valida."
                    );
                }

                return cantidad;
            }
        }
    }

    private static int bloquearProducto(
            Connection con,
            Categoria categoria,
            int id,
            int farmacia
    ) throws Exception {

        final String tabla;
        final String columnaId;

        if (categoria == Categoria.ASEO) {

            tabla = "aseo";
            columnaId = "id_aseo";

        } else if (categoria == Categoria.MATERNIDAD) {

            tabla = "maternidad";
            columnaId = "id_maternidad";

        } else {

            throw new ReglaNegocioException(
                    "Categoria no valida para producto."
            );
        }

        String sql =
                "SELECT stock FROM " + tabla
                + " WHERE " + columnaId + " = ?"
                + " AND id_farmacia = ?"
                + " AND UPPER(BTRIM(estado)) = 'ACTIVO'"
                + " FOR UPDATE";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, farmacia);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "Producto inexistente, inactivo "
                            + "o perteneciente a otra farmacia."
                    );
                }

                int stock = rs.getInt("stock");

                if (rs.wasNull()) {
                    throw new ReglaNegocioException(
                            "El stock del producto es invalido."
                    );
                }

                return stock;
            }
        }
    }

    private static void actualizarProducto(
            Connection con,
            AjusteExistencias a
    ) throws SQLException {

        String tabla =
                a.categoria() == Categoria.ASEO
                        ? "aseo" : "maternidad";

        String id =
                a.categoria() == Categoria.ASEO
                        ? "id_aseo" : "id_maternidad";

        String sql =
                "UPDATE " + tabla
                + " SET stock = ?,"
                + " id_usuario_modificacion = ?,"
                + " fecha_modificacion = NOW()"
                + " WHERE " + id + " = ?"
                + " AND id_farmacia = ?"
                + " AND stock = ?"
                + " AND UPPER(BTRIM(estado)) = 'ACTIVO'";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, a.cantidadNueva());
            ps.setInt(2, a.idUsuario());
            ps.setInt(3, a.idReferencia());
            ps.setInt(4, a.idFarmacia());
            ps.setInt(5, a.cantidadAnterior());

            exigirUnaFila(ps);
        }
    }

    private static void actualizarLote(
            Connection con,
            AjusteExistencias a
    ) throws SQLException {

        String sql = """
                UPDATE lotes
                SET cantidad_actual = ?,
                    estado_lote =
                        CASE WHEN ? = 0
                            THEN 'AGOTADO'
                            ELSE 'DISPONIBLE'
                        END,
                    id_usuario_modificacion = ?,
                    fecha_modificacion = NOW()
                WHERE id_lote = ?
                  AND cantidad_actual = ?
                  AND estado_lote IN ('DISPONIBLE', 'AGOTADO')
                  AND fecha_vencimiento > CURRENT_DATE
                  AND EXISTS (
                      SELECT 1
                      FROM medicamentos m
                      WHERE m.id_medicamento = lotes.id_medicamento
                        AND m.id_farmacia = ?
                        AND UPPER(BTRIM(m.estado)) = 'ACTIVO'
                  )
                """;

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, a.cantidadNueva());
            ps.setInt(2, a.cantidadNueva());
            ps.setInt(3, a.idUsuario());
            ps.setInt(4, a.idReferencia());
            ps.setInt(5, a.cantidadAnterior());
            ps.setInt(6, a.idFarmacia());

            exigirUnaFila(ps);
        }
    }

    private static void exigirUnaFila(
            PreparedStatement ps
    ) throws SQLException {

        if (ps.executeUpdate() != 1) {
            throw new SQLException(
                    "El registro cambio durante el ajuste; "
                    + "no se actualizo el inventario."
            );
        }
    }
}
