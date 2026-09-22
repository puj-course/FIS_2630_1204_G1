package com.carestock.service;

import com.carestock.dao.UsuarioDAO;
import com.carestock.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthenticationService {

    public static final String ERROR_CREDENCIALES =
            "Usuario o contraseña incorrectos";

    private final UsuarioDAO usuarioDAO;

    public AuthenticationService() {
        this.usuarioDAO = new UsuarioDAO();
    }

    public AuthenticationService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    public Usuario autenticar(String email, String password)
            throws SQLException {

        // Validación básica
        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalArgumentException(ERROR_CREDENCIALES);
        }

        Usuario usuario = usuarioDAO.buscarPorEmail(email.trim());

        // No revelar si el usuario existe o no.
        if (usuario == null) {
            throw new IllegalArgumentException(ERROR_CREDENCIALES);
        }

        // INACTIVO y BLOQUEADO reciben exactamente el mismo mensaje.
        if (!usuario.estaActivo()) {
            throw new IllegalArgumentException(ERROR_CREDENCIALES);
        }

        try {

            boolean passwordCorrecta = BCrypt.checkpw(
                    password,
                    usuario.getPasswordHash()
            );

            if (!passwordCorrecta) {
                throw new IllegalArgumentException(ERROR_CREDENCIALES);
            }

        } catch (IllegalArgumentException e) {

            /*
             * BCrypt también lanza IllegalArgumentException si el hash
             * almacenado no tiene un formato válido.
             *
             * No revelamos esa información hacia la interfaz.
             */
            throw new IllegalArgumentException(ERROR_CREDENCIALES);
        }

        return usuario;
    }
}
