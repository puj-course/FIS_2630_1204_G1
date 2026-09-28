package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.LoteGestion;
import com.carestock.model.MedicamentoGestion;
import com.carestock.model.Ubicacion;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.List;

public class InventarioCrudDAO {

    public List<MedicamentoGestion> listarMedicamentos(
            int idFarmacia,
            boolean incluirInactivos
    ) throws SQLException {

        String sql =
                "SELECT "
                + "m.id_medicamento, "
                + "m.id_farmacia, "
                + "m.codigo_invima, "
                + "m.nombre_comercial, "
                + "m.principio_activo, "
                + "m.concentracion, "
                + "m.forma_farmaceutica, "
                + "COALESCE(m.presentacion, '') AS presentacion, "
                + "c.nombre_categoria AS categoria, "
                + "m.stock_total, "
                + "m.stock_minimo, "
                + "m.estado "
                + "FROM MEDICAMENTOS m "
                + "INNER JOIN CATEGORIAS c "
                + "ON c.id_categoria = m.id_categoria "
                + "WHERE m.id_farmacia = ? "
                + (
                    incluirInactivos
                            ? ""
                            : "AND m.estado = 'ACTIVO' "
                )
                + "ORDER BY m.nombre_comercial ASC";


        List<MedicamentoGestion> resultado =
                new ArrayList<>();


        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idFarmacia
            );


            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                while (rs.next()) {

                    resultado.add(
                            mapearMedicamento(
                                    rs
                            )
                    );
                }
            }
        }


        return resultado;
    }


    public List<LoteGestion> listarLotes(
            int idFarmacia
    ) throws SQLException {

        String sql =
                "SELECT "
                + "l.id_lote, "
                + "m.id_farmacia, "
                + "l.id_medicamento, "
                + "l.numero_lote, "
                + "m.nombre_comercial AS medicamento, "
                + "l.cantidad_actual, "
                + "l.fecha_vencimiento, "
                + "l.id_ubicacion, "
                + "CONCAT(u.estante, ' - ', u.nivel) AS ubicacion, "
                + "l.estado_lote, "
                + "l.fecha_ingreso "
                + "FROM LOTES l "
                + "INNER JOIN MEDICAMENTOS m "
                + "ON m.id_medicamento = l.id_medicamento "
                + "INNER JOIN UBICACIONES u "
                + "ON u.id_ubicacion = l.id_ubicacion "
                + "WHERE m.id_farmacia = ? "
                + "ORDER BY l.fecha_ingreso DESC, "
                + "l.id_lote DESC";


        List<LoteGestion> resultado =
                new ArrayList<>();


        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idFarmacia
            );


            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                while (rs.next()) {

                    resultado.add(
                            new LoteGestion(
                                    rs.getInt("id_lote"),
                                    rs.getInt("id_farmacia"),
                                    rs.getInt("id_medicamento"),
                                    rs.getString("numero_lote"),
                                    rs.getString("medicamento"),
                                    rs.getInt("cantidad_actual"),
                                    rs.getDate(
                                            "fecha_vencimiento"
                                    ).toLocalDate(),
                                    rs.getInt("id_ubicacion"),
                                    rs.getString("ubicacion"),
                                    rs.getString("estado_lote"),
                                    rs.getTimestamp(
                                            "fecha_ingreso"
                                    ).toLocalDateTime()
                            )
                    );
                }
            }
        }


        return resultado;
    }


    public List<String> listarCategorias()
            throws SQLException {

        List<String> categorias =
                new ArrayList<>();

        String sql =
                "SELECT nombre_categoria "
                + "FROM CATEGORIAS "
                + "ORDER BY nombre_categoria";


        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        stmt.executeQuery()
        ) {

            while (rs.next()) {

                categorias.add(
                        rs.getString(1)
                );
            }
        }


        return categorias;
    }


    public List<Ubicacion> listarUbicaciones()
            throws SQLException {

        List<Ubicacion> ubicaciones =
                new ArrayList<>();

        String sql =
                "SELECT "
                + "id_ubicacion, "
                + "estante, "
                + "nivel, "
                + "descripcion "
                + "FROM UBICACIONES "
                + "ORDER BY estante, nivel";


        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        stmt.executeQuery()
        ) {

            while (rs.next()) {

                ubicaciones.add(
                        new Ubicacion(
                                rs.getInt(
                                        "id_ubicacion"
                                ),
                                rs.getString(
                                        "estante"
                                ),
                                rs.getString(
                                        "nivel"
                                ),
                                rs.getString(
                                        "descripcion"
                                )
                        )
                );
            }
        }


        return ubicaciones;
    }


    public int crearMedicamento(
            int idUsuarioActor,
            int idFarmacia,
            MedicamentoGestion medicamento
    ) throws SQLException {

        String sql =
                "SELECT fn_crear_medicamento_seguro("
                + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuarioActor
            );

            stmt.setInt(
                    2,
                    idFarmacia
            );

            stmt.setString(
                    3,
                    medicamento.getCodigoInvima()
            );

            stmt.setString(
                    4,
                    medicamento.getNombreComercial()
            );

            stmt.setString(
                    5,
                    medicamento.getPrincipioActivo()
            );

            stmt.setString(
                    6,
                    medicamento.getConcentracion()
            );

            stmt.setString(
                    7,
                    medicamento.getFormaFarmaceutica()
            );

            stmt.setString(
                    8,
                    medicamento.getPresentacion()
            );

            stmt.setString(
                    9,
                    medicamento.getCategoria()
            );

            stmt.setInt(
                    10,
                    medicamento.getStockMinimo()
            );


            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }


        throw new SQLException(
                "No fue posible crear el medicamento."
        );
    }


    public void actualizarMedicamento(
            int idUsuarioActor,
            int idFarmacia,
            MedicamentoGestion medicamento
    ) throws SQLException {

        String sql =
                "SELECT fn_actualizar_medicamento_seguro("
                + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";


        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuarioActor
            );

            stmt.setInt(
                    2,
                    idFarmacia
            );

            stmt.setLong(
                    3,
                    medicamento.getIdMedicamento()
            );

            stmt.setString(
                    4,
                    medicamento.getCodigoInvima()
            );

            stmt.setString(
                    5,
                    medicamento.getNombreComercial()
            );

            stmt.setString(
                    6,
                    medicamento.getPrincipioActivo()
            );

            stmt.setString(
                    7,
                    medicamento.getConcentracion()
            );

            stmt.setString(
                    8,
                    medicamento.getFormaFarmaceutica()
            );

            stmt.setString(
                    9,
                    medicamento.getPresentacion()
            );

            stmt.setString(
                    10,
                    medicamento.getCategoria()
            );

            stmt.setInt(
                    11,
                    medicamento.getStockMinimo()
            );

            stmt.executeQuery();
        }
    }


    public void cambiarEstadoMedicamento(
            int idUsuarioActor,
            int idFarmacia,
            long idMedicamento,
            String nuevoEstado
    ) throws SQLException {

        String sql =
                "SELECT fn_cambiar_estado_medicamento_seguro("
                + "?, ?, ?, ?)";


        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuarioActor
            );

            stmt.setInt(
                    2,
                    idFarmacia
            );

            stmt.setLong(
                    3,
                    idMedicamento
            );

            stmt.setString(
                    4,
                    nuevoEstado
            );

            stmt.executeQuery();
        }
    }


    public int crearLote(
            int idUsuarioActor,
            int idFarmacia,
            int idMedicamento,
            String numeroLote,
            int cantidad,
            java.time.LocalDate fechaVencimiento,
            int idUbicacion
    ) throws SQLException {

        String sql =
                "SELECT fn_crear_lote_seguro("
                + "?, ?, ?, ?, ?, ?, ?)";


        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuarioActor
            );

            stmt.setInt(
                    2,
                    idFarmacia
            );

            stmt.setInt(
                    3,
                    idMedicamento
            );

            stmt.setString(
                    4,
                    numeroLote
            );

            stmt.setInt(
                    5,
                    cantidad
            );

            stmt.setDate(
                    6,
                    Date.valueOf(
                            fechaVencimiento
                    )
            );

            stmt.setInt(
                    7,
                    idUbicacion
            );


            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }


        throw new SQLException(
                "No fue posible crear el lote."
        );
    }


    public void actualizarLote(
            int idUsuarioActor,
            int idFarmacia,
            int idLote,
            String numeroLote,
            int cantidad,
            java.time.LocalDate fechaVencimiento,
            int idUbicacion
    ) throws SQLException {

        String sql =
                "SELECT fn_actualizar_lote_seguro("
                + "?, ?, ?, ?, ?, ?, ?)";


        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuarioActor
            );

            stmt.setInt(
                    2,
                    idFarmacia
            );

            stmt.setInt(
                    3,
                    idLote
            );

            stmt.setString(
                    4,
                    numeroLote
            );

            stmt.setInt(
                    5,
                    cantidad
            );

            stmt.setDate(
                    6,
                    Date.valueOf(
                            fechaVencimiento
                    )
            );

            stmt.setInt(
                    7,
                    idUbicacion
            );

            stmt.executeQuery();
        }
    }


    public void cambiarEstadoLote(
            int idUsuarioActor,
            int idFarmacia,
            int idLote,
            String nuevoEstado
    ) throws SQLException {

        String sql =
                "SELECT fn_cambiar_estado_lote_seguro("
                + "?, ?, ?, ?)";


        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuarioActor
            );

            stmt.setInt(
                    2,
                    idFarmacia
            );

            stmt.setInt(
                    3,
                    idLote
            );

            stmt.setString(
                    4,
                    nuevoEstado
            );

            stmt.executeQuery();
        }
    }


    private MedicamentoGestion mapearMedicamento(
            ResultSet rs
    ) throws SQLException {

        return new MedicamentoGestion(
                rs.getLong(
                        "id_medicamento"
                ),
                rs.getInt(
                        "id_farmacia"
                ),
                rs.getString(
                        "codigo_invima"
                ),
                rs.getString(
                        "nombre_comercial"
                ),
                rs.getString(
                        "principio_activo"
                ),
                rs.getString(
                        "concentracion"
                ),
                rs.getString(
                        "forma_farmaceutica"
                ),
                rs.getString(
                        "presentacion"
                ),
                rs.getString(
                        "categoria"
                ),
                rs.getInt(
                        "stock_total"
                ),
                rs.getInt(
                        "stock_minimo"
                ),
                rs.getString(
                        "estado"
                )
        );
    }
}
