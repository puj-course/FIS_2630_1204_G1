package com.carestock.service;

import com.carestock.dao.AccesoDAO;
import com.carestock.dao.UsuarioDAO;
import com.carestock.model.Usuario;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthenticationService {

    public static final String ERROR_CREDENCIALES =
            "Usuario o contraseña incorrectos";

    private static final String RESULTADO_EXITOSO = "EXITOSO";
    private static final String RESULTADO_FALLIDO = "FALLIDO";

    private final UsuarioDAO usuarioDAO;
    private final AccesoDAO accesoDAO;

    public AuthenticationService() {
        this.usuarioDAO = new UsuarioDAO();
        this.accesoDAO = new AccesoDAO();
    }

    public AuthenticationService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
        this.accesoDAO = new AccesoDAO();
    }

    public AuthenticationService(UsuarioDAO usuarioDAO, AccesoDAO accesoDAO) {
        this.usuarioDAO = usuarioDAO;
        this.accesoDAO = accesoDAO;
    }

    public Usuario autenticar(String email, String password)
            throws SQLException {

        // Validación básica
        if (email == null || email.isBlank()
                || password == null || password.isBlank()) {

            throw new IllegalArgumentException(ERROR_CREDENCIALES);
        }

        Usuario usuario = usuarioDAO.buscarPorEmail(email.trim());

     
        if (usuario == null) {
            throw new IllegalArgumentException(ERROR_CREDENCIALES);
        }

        // INACTIVO y BLOQUEADO reciben exactamente el mismo mensaje.
        if (!usuario.estaActivo()) {
            accesoDAO.registrarAcceso(
                    usuario.getIdUsuario(),
                    RESULTADO_FALLIDO
            );
            throw new IllegalArgumentException(ERROR_CREDENCIALES);
        }

        boolean passwordCorrecta;

        try {

            passwordCorrecta = BCrypt.checkpw(
                    password,
                    usuario.getPasswordHash()
            );

        } catch (IllegalArgumentException e) {

           
            accesoDAO.registrarAcceso(
                    usuario.getIdUsuario(),
                    RESULTADO_FALLIDO
            );
            throw new IllegalArgumentException(ERROR_CREDENCIALES);
        }

        if (!passwordCorrecta) {
            accesoDAO.registrarAcceso(
                    usuario.getIdUsuario(),
                    RESULTADO_FALLIDO
            );
            throw new IllegalArgumentException(ERROR_CREDENCIALES);
        }

        accesoDAO.registrarAcceso(
                usuario.getIdUsuario(),
                RESULTADO_EXITOSO
        );

        return usuario;
    }
}
