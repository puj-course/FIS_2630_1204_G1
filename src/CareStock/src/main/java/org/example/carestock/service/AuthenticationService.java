package org.example.carestock.service;

import org.example.carestock.dao.UsuarioDAO;
import org.example.carestock.dao.UsuarioDAOImpl;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Usuario;

public class AuthenticationService {

    private final UsuarioDAO usuarioDAO;

    public AuthenticationService() {
        this.usuarioDAO = new UsuarioDAOImpl();
    }

    public Usuario autenticar(String email, String password) throws Exception {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new ReglaNegocioException("Debe ingresar tanto el correo como la contraseña.");
        }

        Usuario usuario = usuarioDAO.buscarPorEmail(email);

        if (usuario == null) {
            throw new ReglaNegocioException("Credenciales incorrectas o usuario no registrado.");
        }

        // Validación de contraseña (comprobando si coincide directamente o si es un hash)
        boolean passwordValida = usuario.getPassword().equals(password) || usuario.getPassword().startsWith("$2a$");

        if (!passwordValida) {
            throw new ReglaNegocioException("Contraseña incorrecta.");
        }

        return usuario;
    }
}