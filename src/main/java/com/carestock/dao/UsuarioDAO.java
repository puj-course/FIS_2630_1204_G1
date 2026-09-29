package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.exception.AccesoDenegadoException;
import com.carestock.model.Usuario;
import com.carestock.security.AccessControl;
import com.carestock.session.UserSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public int obtenerIdActivoPorEmail(
            String email
    ) throws SQLException {

        String sql =
                "SELECT id_usuario "
                + "FROM USUARIOS "
                + "WHERE LOWER(email) = LOWER(?) "
                + "AND estado = 'ACTIVO'";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    email
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (rs.next()) {

                    return rs.getInt(
                            "id_usuario"
                    );
                }
            }
        }

        throw new SQLException(
                "No existe un usuario ACTIVO con el correo configurado: "
                + email
        );
    }

    public Usuario buscarPorEmail(
            String email
    ) throws SQLException {

        String sql =
                "SELECT "
                + "u.id_usuario, "
                + "u.nombre_completo, "
                + "u.email, "
                + "u.password_hash, "
                + "u.id_rol, "
                + "r.nombre_rol, "
                + "u.estado, "
                + "u.id_farmacia "
                + "FROM USUARIOS u "
                + "INNER JOIN ROLES r "
                + "ON r.id_rol = u.id_rol "
                + "WHERE LOWER(u.email) = LOWER(?) "
                + "LIMIT 1";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    email.trim()
            );

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                if (rs.next()) {

                    return mapearUsuario(
                            rs
                    );
                }
            }
        }

        return null;
    }

    public String obtenerPasswordHashActivoPorId(
            int idUsuario
    ) throws SQLException {

        String sql =
                "SELECT password_hash "
                + "FROM USUARIOS "
                + "WHERE id_usuario = ? "
                + "AND estado = 'ACTIVO'";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

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

                    return rs.getString(
                            "password_hash"
                    );
                }
            }
        }

        return null;
    }

    public boolean actualizarPasswordHash(
            int idUsuario,
            String hashActualEsperado,
            String nuevoHash
    ) throws SQLException {

        String sql =
                "UPDATE USUARIOS "
                + "SET password_hash = ? "
                + "WHERE id_usuario = ? "
                + "AND password_hash = ? "
                + "AND estado = 'ACTIVO'";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setString(
                    1,
                    nuevoHash
            );

            stmt.setInt(
                    2,
                    idUsuario
            );

            stmt.setString(
                    3,
                    hashActualEsperado
            );

            return stmt.executeUpdate() == 1;
        }
    }

    public List<Usuario> listarTodos()
            throws SQLException {

        String sql =
                "SELECT "
                + "u.id_usuario, "
                + "u.nombre_completo, "
                + "u.email, "
                + "u.password_hash, "
                + "u.id_rol, "
                + "r.nombre_rol, "
                + "u.estado, "
                + "u.id_farmacia "
                + "FROM USUARIOS u "
                + "INNER JOIN ROLES r "
                + "ON r.id_rol = u.id_rol "
                + "ORDER BY u.nombre_completo ASC";

        List<Usuario> usuarios =
                new ArrayList<>();

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql);

                ResultSet rs =
                        stmt.executeQuery()
        ) {

            while (rs.next()) {

                usuarios.add(
                        mapearUsuario(rs)
                );
            }
        }

        return usuarios;
    }

    /**
     * Creación jerárquica.
     *
     * El rol destino NO se recibe desde la interfaz.
     * PostgreSQL lo determina usando el rol del actor.
     */
    public int crearJerarquico(
            int idUsuarioActor,
            String nombreCompleto,
            String email,
            String password,
            Integer idFarmaciaSeleccionada
    ) throws SQLException {

        AccessControl.requireRole(
                "SUPER_ADMIN",
                "ADMINISTRADOR"
        );

        UserSession.CurrentUser actor =
                UserSession
                        .getInstance()
                        .getCurrentUser();

        if (
                actor == null
                || actor.getId() != idUsuarioActor
        ) {

            throw new AccesoDenegadoException(
                    "El usuario creador no coincide con la sesión activa."
            );
        }

        String sql =
                "SELECT fn_crear_usuario_jerarquico("
                + "?, ?, ?, ?, ?)";

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

            stmt.setString(
                    2,
                    nombreCompleto.trim()
            );

            stmt.setString(
                    3,
                    email.trim()
            );

            stmt.setString(
                    4,
                    password
            );

            if (idFarmaciaSeleccionada == null) {

                stmt.setNull(
                        5,
                        Types.INTEGER
                );

            } else {

                stmt.setInt(
                        5,
                        idFarmaciaSeleccionada
                );
            }

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
                "No fue posible crear el usuario."
        );
    }

    private Usuario mapearUsuario(
            ResultSet rs
    ) throws SQLException {

        Integer idFarmacia =
                (Integer) rs.getObject(
                        "id_farmacia"
                );

        return new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("nombre_completo"),
                rs.getString("email"),
                rs.getString("password_hash"),
                rs.getInt("id_rol"),
                rs.getString("nombre_rol"),
                rs.getString("estado"),
                idFarmacia
        );
    }
}
