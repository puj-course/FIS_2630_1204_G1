package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.session.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * Servicio transaccional de entradas y salidas.
 * Aplica a productos de aseo y maternidad.
 */
public final class ServicioMovimientosProducto {

    public enum Categoria {
        ASEO("aseo", "id_aseo"),
        MATERNIDAD("maternidad", "id_maternidad");

        private final String tabla;
        private final String columnaId;

        Categoria(String tabla, String columnaId) {
            this.tabla = tabla;
            this.columnaId = columnaId;
        }
    }

    public enum TipoMovimiento {
        ENTRADA,
        SALIDA
    }

    public record ResultadoMovimiento(
            long idMovimiento,
            Categoria categoria,
            int idProducto,
            TipoMovimiento tipo,
            int cantidad,
            int stockAnterior,
            int stockPosterior,
            int idFarmacia,
            int idUsuario
    ) {}

    public ResultadoMovimiento registrar(
            Categoria categoria,
            int idProducto,
            TipoMovimiento tipo,
            int cantidad,
            String motivo,
            int idUsuario,
            int idFarmacia
    ) throws Exception {

        String motivoLimpio = validarSolicitud(
                categoria,
                idProducto,
                tipo,
                cantidad,
                motivo,
                idUsuario,
                idFarmacia
        );

        var sesion = SesionUsuario.actual();

        if (sesion.id() != idUsuario
                || !Objects.equals(
                        sesion.idFarmacia(),
                        idFarmacia
                )) {

            throw new ReglaNegocioException(
                    "La sesión ha cambiado o la farmacia no corresponde."
            );
        }

        try (Connection conexion = ConexionBD.getConexion()) {

            conexion.setAutoCommit(false);

            try {
                verificarOperador(
                        conexion, idUsuario, idFarmacia
                );

                int anterior = bloquearYLeerStock(
                        conexion,
                        categoria,
                        idProducto,
                        idFarmacia
                );

                int posterior = calcularStock(
                        anterior, tipo, cantidad
                );

                actualizarStock(
                        conexion,
                        categoria,
                        idProducto,
                        idFarmacia,
                        idUsuario,
                        posterior
                );

                long idMovimiento = insertarMovimiento(
                        conexion,
                        categoria,
                        idProducto,
                        tipo,
                        cantidad,
                        motivoLimpio,
                        idUsuario,
                        idFarmacia,
                        anterior,
                        posterior
                );

                conexion.commit();

                return new ResultadoMovimiento(
                        idMovimiento,
                        categoria,
                        idProducto,
                        tipo,
                        cantidad,
                        anterior,
                        posterior,
                        idFarmacia,
                        idUsuario
                );

            } catch (Exception fallo) {

                try {
                    conexion.rollback();

                } catch (SQLException errorRollback) {
                    fallo.addSuppressed(errorRollback);
                }

                throw fallo;
            }

            // HikariCP restablece autoCommit al
            // devolver la conexión al pool.
        }
    }

    static String validarSolicitud(
            Categoria categoria,
            int idProducto,
            TipoMovimiento tipo,
            int cantidad,
            String motivo,
            int idUsuario,
            int idFarmacia
    ) throws ReglaNegocioException {

        if (categoria == null || tipo == null) {
            throw new ReglaNegocioException(
                    "La categoría y el tipo de movimiento son obligatorios."
            );
        }

        if (idProducto <= 0
                || idUsuario <= 0
                || idFarmacia <= 0) {

            throw new ReglaNegocioException(
                    "Producto, usuario y farmacia deben ser válidos."
            );
        }

        if (cantidad <= 0) {
            throw new ReglaNegocioException(
                    "La cantidad debe ser un entero mayor que cero."
            );
        }

        if (motivo == null
                || motivo.isBlank()
                || motivo.trim().length() > 250) {

            throw new ReglaNegocioException(
                    "El motivo es obligatorio y no debe superar 250 caracteres."
            );
        }

        return motivo.trim();
    }

    static int calcularStock(
            int stockAnterior,
            TipoMovimiento tipo,
            int cantidad
    ) throws ReglaNegocioException {

        if (stockAnterior < 0) {
            throw new ReglaNegocioException(
                    "Las existencias actuales son inválidas."
            );
        }

        if (cantidad <= 0 || tipo == null) {
            throw new ReglaNegocioException(
                    "Movimiento o cantidad inválidos."
            );
        }

        if (tipo == TipoMovimiento.SALIDA) {

            if (stockAnterior < cantidad) {
                throw new ReglaNegocioException(
                        "Stock insuficiente para realizar la salida."
                );
            }

            return stockAnterior - cantidad;
        }

        try {
            return Math.addExact(stockAnterior, cantidad);

        } catch (ArithmeticException excepcion) {
            throw new ReglaNegocioException(
                    "La entrada excede el máximo de existencias admitido."
            );
        }
    }

    private static void verificarOperador(
            Connection conexion,
            int idUsuario,
            int idFarmacia
    ) throws Exception {

        String sql = """
                SELECT 1
                FROM usuarios u
                INNER JOIN farmacias f
                    ON f.id_farmacia = u.id_farmacia
                WHERE u.id_usuario = ?
                  AND u.id_farmacia = ?
                  AND UPPER(TRIM(u.estado)) = 'ACTIVO'
                  AND UPPER(TRIM(f.estado)) = 'ACTIVA'
                FOR SHARE OF u, f
                """;

        try (PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setInt(2, idFarmacia);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "Usuario inactivo o farmacia no autorizada."
                    );
                }
            }
        }
    }

    private static int bloquearYLeerStock(
            Connection conexion,
            Categoria categoria,
            int idProducto,
            int idFarmacia
    ) throws Exception {

        String sql =
                "SELECT stock FROM " + categoria.tabla
                + " WHERE " + categoria.columnaId
                + " = ? AND id_farmacia = ?"
                + " AND UPPER(TRIM(estado)) = 'ACTIVO'"
                + " FOR UPDATE";

        try (PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, idProducto);
            ps.setInt(2, idFarmacia);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new ReglaNegocioException(
                            "Producto inexistente, inactivo o de otra farmacia."
                    );
                }

                int stock = rs.getInt("stock");

                if (rs.wasNull()) {
                    throw new ReglaNegocioException(
                            "El producto no tiene un stock válido."
                    );
                }

                return stock;
            }
        }
    }

    private static void actualizarStock(
            Connection conexion,
            Categoria categoria,
            int idProducto,
            int idFarmacia,
            int idUsuario,
            int stockNuevo
    ) throws SQLException {

        String sql =
                "UPDATE " + categoria.tabla
                + " SET stock = ?,"
                + " id_usuario_modificacion = ?,"
                + " fecha_modificacion = NOW()"
                + " WHERE " + categoria.columnaId
                + " = ? AND id_farmacia = ?"
                + " AND UPPER(TRIM(estado)) = 'ACTIVO'";

        try (PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            ps.setInt(1, stockNuevo);
            ps.setInt(2, idUsuario);
            ps.setInt(3, idProducto);
            ps.setInt(4, idFarmacia);

            if (ps.executeUpdate() != 1) {
                throw new SQLException(
                        "No fue posible actualizar las existencias."
                );
            }
        }
    }

    private static long insertarMovimiento(
            Connection conexion,
            Categoria categoria,
            int idProducto,
            TipoMovimiento tipo,
            int cantidad,
            String motivo,
            int idUsuario,
            int idFarmacia,
            int anterior,
            int posterior
    ) throws SQLException {

        String sql = """
                INSERT INTO movimientos_productos
                (id_aseo, id_maternidad, id_farmacia, id_usuario,
                 tipo_movimiento, cantidad, motivo,
                 stock_anterior, stock_posterior)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id_movimiento
                """;

        try (PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            if (categoria == Categoria.ASEO) {
                ps.setInt(1, idProducto);
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setNull(1, Types.INTEGER);
                ps.setInt(2, idProducto);
            }

            ps.setInt(3, idFarmacia);
            ps.setInt(4, idUsuario);
            ps.setString(5, tipo.name());
            ps.setInt(6, cantidad);
            ps.setString(7, motivo);
            ps.setInt(8, anterior);
            ps.setInt(9, posterior);

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se pudo registrar el historial del movimiento."
                    );
                }

                return rs.getLong("id_movimiento");
            }
        }
    }
}
