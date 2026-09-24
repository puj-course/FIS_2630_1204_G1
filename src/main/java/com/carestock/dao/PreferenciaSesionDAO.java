package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.PreferenciaSesion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Persistencia de las preferencias individuales
 * de seguridad de sesión.
 */
public class PreferenciaSesionDAO {

    /**
     * Obtiene la configuración del usuario.
     *
     * Si todavía no existe, crea automáticamente
     * una configuración predeterminada segura.
     */
    public PreferenciaSesion obtenerOCrearPorUsuario(
            int idUsuario
    ) throws SQLException {

        try (
                Connection conn =
                        DatabaseConfig.getConnection()
        ) {

            PreferenciaSesion existente =
                    buscar(
                            conn,
                            idUsuario
                    );

            if (existente != null) {
                return existente;
            }

            insertarPredeterminada(
                    conn,
                    idUsuario
            );

            PreferenciaSesion creada =
                    buscar(
                            conn,
                            idUsuario
                    );

            if (creada == null) {

                throw new SQLException(
                        "No fue posible crear las preferencias "
                        + "de sesión del usuario."
                );
            }

            return creada;
        }
    }

    /**
     * Crea o actualiza la configuración del usuario.
     */
    public void guardar(
            PreferenciaSesion preferencia
    ) throws SQLException {

        String sql =
                "INSERT INTO PREFERENCIAS_USUARIO (" +
                "id_usuario, " +
                "timeout_inactividad_activo, " +
                "timeout_inactividad_minutos, " +
                "fecha_actualizacion" +
                ") VALUES (?, ?, ?, CURRENT_TIMESTAMP) " +
                "ON CONFLICT (id_usuario) " +
                "DO UPDATE SET " +
                "timeout_inactividad_activo = " +
                "EXCLUDED.timeout_inactividad_activo, " +
                "timeout_inactividad_minutos = " +
                "EXCLUDED.timeout_inactividad_minutos, " +
                "fecha_actualizacion = CURRENT_TIMESTAMP";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    preferencia.getIdUsuario()
            );

            stmt.setBoolean(
                    2,
                    preferencia.isTimeoutActivo()
            );

            stmt.setInt(
                    3,
                    preferencia.getTimeoutMinutos()
            );

            stmt.executeUpdate();
        }
    }

    private PreferenciaSesion buscar(
            Connection conn,
            int idUsuario
    ) throws SQLException {

        String sql =
                "SELECT " +
                "timeout_inactividad_activo, " +
                "timeout_inactividad_minutos " +
                "FROM PREFERENCIAS_USUARIO " +
                "WHERE id_usuario = ?";

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuario
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (rs.next()) {

                    return new PreferenciaSesion(
                            idUsuario,
                            rs.getBoolean(
                                    "timeout_inactividad_activo"
                            ),
                            rs.getInt(
                                    "timeout_inactividad_minutos"
                            )
                    );
                }
            }
        }

        return null;
    }

    private void insertarPredeterminada(
            Connection conn,
            int idUsuario
    ) throws SQLException {

        String sql =
                "INSERT INTO PREFERENCIAS_USUARIO (" +
                "id_usuario, " +
                "timeout_inactividad_activo, " +
                "timeout_inactividad_minutos" +
                ") VALUES (?, ?, ?) " +
                "ON CONFLICT (id_usuario) DO NOTHING";

        try (
                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    idUsuario
            );

            stmt.setBoolean(
                    2,
                    PreferenciaSesion.DEFAULT_TIMEOUT_ACTIVO
            );

            stmt.setInt(
                    3,
                    PreferenciaSesion.DEFAULT_TIMEOUT_MINUTOS
            );

            stmt.executeUpdate();
        }
    }
}
