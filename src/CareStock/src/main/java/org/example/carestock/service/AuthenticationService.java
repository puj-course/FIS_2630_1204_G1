package org.example.carestock.service;

import org.example.carestock.dao.UsuarioDAO;
import org.example.carestock.dao.UsuarioDAOImpl;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

/** Solo admite hashes BCrypt verificables; nunca acepta el prefijo como credencial. */
public class AuthenticationService {
    private final UsuarioDAO usuarioDAO;

    public AuthenticationService() {
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    public Usuario autenticar(String email, String password) throws Exception {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new ReglaNegocioException("Debe ingresar correo y contrasena.");
        }

        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        if (usuario == null) {
            throw new ReglaNegocioException("Credenciales incorrectas o usuario inactivo.");
        }

        String hash = usuario.getPassword();
        boolean valido = false;
        if (hash != null && hash.matches("\\$2[aby]\\$\\d{2}\\$.+")) {
            try {
                valido = BCrypt.checkpw(password, hash);
            } catch (IllegalArgumentException ex) {
                valido = false;
            }
        }
        if (!valido) {
            throw new ReglaNegocioException("Credenciales incorrectas o usuario inactivo.");
        }
        usuario.setPassword(null); // Nunca conservar el hash en la sesion.
        return usuario;
    }
}
