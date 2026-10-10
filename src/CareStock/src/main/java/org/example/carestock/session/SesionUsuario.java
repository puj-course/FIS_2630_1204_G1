
package org.example.carestock.session;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Gestiona los datos de identidad del usuario autenticado.
 *
 * Almacena:
 * - Identificador del usuario
 * - Nombre completo
 * - Correo electronico
 * - Rol
 * - Farmacia asignada
 */
public final class SesionUsuario {

    private static volatile DatosUsuario actual;

    private SesionUsuario() {
    }

    /**
     * Datos del usuario autenticado.
     */
    public record DatosUsuario(
            int id,
            String nombre,
            String email,
            String rol,
            Integer idFarmacia,
            String farmacia
    ) {
    }

    /**
     * Inicializa la sesion despues de una
     * autenticacion exitosa.
     */
    public static void iniciar(
            Usuario usuarioAutenticado
    ) throws Exception {

        if (usuarioAutenticado == null ||
                usuarioAutenticado.getIdUsuario() <= 0) {

            throw new IllegalArgumentException(
                    "Se requiere un usuario autenticado valido"
            );
        }

        String sql = """
                SELECT
                    u.id_usuario,
                    u.nombre_completo,
                    u.email,
                    u.id_rol,
                    u.id_farmacia,
                    f.nombre AS farmacia_nombre
                FROM usuarios u
                LEFT JOIN farmacias f
                    ON f.id_farmacia = u.id_farmacia
                WHERE u.id_usuario = ?
                  AND UPPER(u.estado) = 'ACTIVO'
                """;

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    usuarioAutenticado.getIdUsuario()
            );

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new IllegalStateException(
                            "Usuario no activo o inexistente"
                    );
                }

                int farmaciaId =
                        rs.getInt("id_farmacia");

                Integer idFarmacia =
                        rs.wasNull() ? null : farmaciaId;

                actual = new DatosUsuario(
                        rs.getInt("id_usuario"),
                        rs.getString("nombre_completo"),
                        rs.getString("email"),
                        "Rol #" + rs.getInt("id_rol"),
                        idFarmacia,
                        rs.getString("farmacia_nombre")
                );
            }
        }
    }

    /**
     * Verifica si existe una sesion activa.
     */
    public static boolean estaActiva() {
        return actual != null;
    }

    /**
     * Retorna los datos de la sesion actual.
     */
    public static DatosUsuario actual() {

        DatosUsuario sesion = actual;

        if (sesion == null) {
            throw new IllegalStateException(
                    "No existe una sesion activa"
            );
        }

        return sesion;
    }

    /**
     * Obtiene el usuario necesario para
     * trabajar con la fachada Inventario.
     */
    public static Usuario usuarioParaFachada() {

        DatosUsuario sesion = actual();

        Usuario usuario = new Usuario();

        usuario.setIdUsuario(sesion.id());

        return usuario;
    }

    /**
     * Finaliza y limpia la sesion.
     */
    public static void cerrar() {
        actual = null;
    }
}
