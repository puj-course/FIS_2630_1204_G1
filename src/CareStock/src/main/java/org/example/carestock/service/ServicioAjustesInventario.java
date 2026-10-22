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

/** HU-793: ajustes de stock con bloqueo, auditoria y rollback. */
public final class ServicioAjustesInventario {

    private final AuditoriaAjustesDao auditoria =
            new AuditoriaAjustesDao();

    public record ResultadoAjuste(
            long idAuditoria,
            Categoria categoria,
            int idReferencia,
            int anterior,
            int nuevo,
            int diferencia,
            int idUsuario,
            int idFarmacia
    ) {}

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
                    "No hay farmacia asignada."
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

    public ResultadoAjuste ajustar(
            AjusteExistencias a
    ) throws Exception {

        Objects.requireNonNull(
                a, "El ajuste es obligatorio."
        );

        if (!SesionUsuario.estaActiva()) {
            throw new ReglaNegocioException(
                    "Debe iniciar sesion."
            );
        }

        var sesion = SesionUsuario.actual();

        if (sesion.id() != a.idUsuario()
                || !Objects.equals(
                        sesion.idFarmacia(),
                        a.idFarmacia()
                )
                || !rolAdministrador(sesion.rol())) {

            throw new ReglaNegocioException(
                    "Usuario, rol o farmacia no autorizados."
            );
        }

        ResultadoAjuste resultado;

        try (Connection con =
                     ConexionBaseDatos.getConexion()) {

            con.setAutoCommit(false);

            try {

                verificarAutorizacion(
                        con,
                        a.idUsuario(),
                        a.idFarmacia()
                );

                int stockReal =
                        bloquearYLeerStock(con, a);

                comprobarSaldo(
                        stockReal,
                        a.cantidadAnterior(),
                        a.cantidadNueva()
                );

                actualizarStock(con, a);

                long idAuditoria =
                        auditoria.registrar(con, a);

                con.commit();

                resultado = new ResultadoAjuste(
                        idAuditoria,
                        a.categoria(),
                        a.idReferencia(),
                        stockReal,
                        a.cantidadNueva(),
                        a.diferencia(),
                        a.idUsuario(),
                        a.idFarmacia()
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

        // Invalidacion solamente despues del COMMIT.
        InventarioConsultas.invalidarFarmacia(
                a.idFarmacia()
        );

        return resultado;
    }

    static boolean rolAdministrador(String rol) {

        return rol != null && (
                "ADMINISTRADOR".equalsIgnoreCase(rol.trim())
                || "SUPER_ADMIN".equalsIgnoreCase(rol.trim())
        );
    }

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
                    "El stock cambio durante otra operacion. "
                    + "Actualice la consulta."
            );
        }

        if (real == nuevo) {
            throw new ReglaNegocioException(
                    "El ajuste debe modificar las existencias."
            );
        }
    }

    private static void verificarAutorizacion(
            Connection con,
            int idUsuario,
            int idFarmacia
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

            ps.setInt(1, idUsuario);
            ps.setInt(2, idFarmacia);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "El usuario no tiene permiso "
                            + "para ajustar inventario."
                    );
                }
            }
        }
    }

    private static int bloquearYLeerStock(
            Connection con,
            AjusteExistencias a
    ) throws Exception {

        if (a.categoria() == Categoria.MEDICAMENTO) {

            // Bloquear medicamento antes del lote.
            String medicamento = """
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
                         con.prepareStatement(medicamento)) {

                ps.setInt(1, a.idReferencia());
                ps.setInt(2, a.idFarmacia());

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new ReglaNegocioException(
                                "Lote inexistente o medicamento "
                                + "no autorizado."
                        );
                    }
                }
            }

            String lote = """
                    SELECT l.cantidad_actual
                    FROM lotes l
                    JOIN medicamentos m
                        ON m.id_medicamento = l.id_medicamento
                    WHERE l.id_lote = ?
                      AND m.id_farmacia = ?
                      AND l.estado_lote
                          IN ('DISPONIBLE', 'AGOTADO')
                      AND l.fecha_vencimiento > CURRENT_DATE
                    FOR UPDATE OF l
                    """;

            try (PreparedStatement ps =
                         con.prepareStatement(lote)) {

                ps.setInt(1, a.idReferencia());
                ps.setInt(2, a.idFarmacia());

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {
                        throw new ReglaNegocioException(
                                "Lote vencido, retenido o inexistente."
                        );
                    }

                    return rs.getInt(1);
                }
            }
        }

        String tabla = a.categoria() == Categoria.ASEO
                ? "aseo" : "maternidad";

        String id = a.categoria() == Categoria.ASEO
                ? "id_aseo" : "id_maternidad";

        String sql =
                "SELECT stock FROM " + tabla
                + " WHERE " + id + " = ?"
                + " AND id_farmacia = ?"
                + " AND UPPER(BTRIM(estado)) = 'ACTIVO'"
                + " FOR UPDATE";

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, a.idReferencia());
            ps.setInt(2, a.idFarmacia());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "Producto inexistente, inactivo "
                            + "o de otra farmacia."
                    );
                }

                return rs.getInt(1);
            }
        }
    }

    private static void actualizarStock(
            Connection con,
            AjusteExistencias a
    ) throws SQLException {

        String sql;

        if (a.categoria() == Categoria.MEDICAMENTO) {

            sql = """
                    UPDATE lotes
                    SET cantidad_actual = ?,
                        estado_lote =
                            CASE
                                WHEN ? = 0 THEN 'AGOTADO'
                                ELSE 'DISPONIBLE'
                            END,
                        id_usuario_modificacion = ?,
                        fecha_modificacion = NOW()
                    WHERE id_lote = ?
                      AND cantidad_actual = ?
                      AND estado_lote
                          IN ('DISPONIBLE', 'AGOTADO')
                      AND fecha_vencimiento > CURRENT_DATE
                      AND EXISTS (
                          SELECT 1
                          FROM medicamentos m
                          WHERE m.id_medicamento =
                              lotes.id_medicamento
                            AND m.id_farmacia = ?
                            AND UPPER(BTRIM(m.estado)) = 'ACTIVO'
                      )
                    """;

        } else {

            String tabla = a.categoria() == Categoria.ASEO
                    ? "aseo" : "maternidad";

            String id = a.categoria() == Categoria.ASEO
                    ? "id_aseo" : "id_maternidad";

            sql = "UPDATE " + tabla
                    + " SET stock = ?,"
                    + " id_usuario_modificacion = ?,"
                    + " fecha_modificacion = NOW()"
                    + " WHERE " + id + " = ?"
                    + " AND id_farmacia = ?"
                    + " AND stock = ?"
                    + " AND UPPER(BTRIM(estado)) = 'ACTIVO'";
        }

        try (PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(1, a.cantidadNueva());

            if (a.categoria() == Categoria.MEDICAMENTO) {

                ps.setInt(2, a.cantidadNueva());
                ps.setInt(3, a.idUsuario());
                ps.setInt(4, a.idReferencia());
                ps.setInt(5, a.cantidadAnterior());
                ps.setInt(6, a.idFarmacia());

            } else {

                ps.setInt(2, a.idUsuario());
                ps.setInt(3, a.idReferencia());
                ps.setInt(4, a.idFarmacia());
                ps.setInt(5, a.cantidadAnterior());
            }

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No fue posible actualizar el stock; "
                        + "reintente con saldo actualizado."
                );
            }
        }
    }
}
