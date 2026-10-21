package org.example.carestock.session;

import org.example.carestock.config.ConexionBaseDatos;
import org.example.carestock.model.Usuario;
import org.example.carestock.proxy.InventarioConsultas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Datos de identidad y farmacia cargados tras autenticacion correcta.
 */
public final class SesionUsuario {

    private static final String ROL_ADMINISTRADOR = "ADMINISTRADOR";

    private static volatile DatosUsuario actual;

    private SesionUsuario() {}

    public record DatosUsuario(
            int id,
            String nombre,
            String email,
            String rol,
            Integer idFarmacia,
            String farmacia
    ) {
        public boolean esAdministrador() {
            return rol != null
                    && ROL_ADMINISTRADOR.equalsIgnoreCase(rol.trim());
        }
    }

    public static void iniciar(Usuario usuarioAutenticado) throws Exception {
        if (usuarioAutenticado == null
                || usuarioAutenticado.getIdUsuario() <= 0) {

            throw new IllegalArgumentException(
                    "Se requiere un usuario autenticado valido"
            );
        }

        // Evita conservar una sesion anterior si la consulta falla.
        actual = null;
        InventarioConsultas.invalidar();

        // La farmacia y el rol se obtienen desde PostgreSQL.
        String sql = """
            SELECT u.id_usuario,
                   u.nombre_completo,
                   u.email,
                   u.id_rol,
                   u.id_farmacia,
                   f.nombre AS farmacia_nombre,
                   COALESCE(
                       NULLIF(BTRIM(r.nombre_rol), ''),
                       'Rol #' || u.id_rol::text
                   ) AS nombre_rol
            FROM usuarios u
            LEFT JOIN farmacias f ON f.id_farmacia = u.id_farmacia
            LEFT JOIN roles r ON r.id_rol = u.id_rol
            WHERE u.id_usuario = ?
              AND UPPER(u.estado) = 'ACTIVO'
            """;

        try (Connection con = ConexionBaseDatos.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioAutenticado.getIdUsuario());

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalStateException(
                            "Usuario no activo o inexistente"
                    );
                }

                int farmaciaId = rs.getInt("id_farmacia");
                Integer idFarmacia = rs.wasNull() ? null : farmaciaId;

                actual = new DatosUsuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre_completo"),
                        rs.getString("email"),
                        rs.getString("nombre_rol"),
                        idFarmacia,
                        rs.getString("farmacia_nombre")
                );
            }
        }
    }

    public static boolean estaActiva() {
        return actual != null;
    }

    public static boolean esAdministrador() {
        DatosUsuario sesion = actual;
        return sesion != null && sesion.esAdministrador();
    }

    public static DatosUsuario actual() {
        DatosUsuario sesion = actual;

        if (sesion == null) {
            throw new IllegalStateException("No existe una sesion activa");
        }

        return sesion;
    }

    public static Usuario usuarioParaFachada() {
        DatosUsuario sesion = actual();

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(sesion.id());

        return usuario;
    }

    public static void cerrar() {
        actual = null;
        InventarioConsultas.invalidar();
    }
}