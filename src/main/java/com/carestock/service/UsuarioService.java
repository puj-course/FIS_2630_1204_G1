package com.carestock.service;

import com.carestock.dao.UsuarioDAO;
import com.carestock.exception.AccesoDenegadoException;
import com.carestock.security.AccessControl;
import com.carestock.session.SessionContext;
import com.carestock.session.UserSession;

import java.sql.SQLException;

public class UsuarioService {

    private static final int LONGITUD_MINIMA_PASSWORD = 8;

    private final UsuarioDAO usuarioDAO;
    private final SessionContext sessionContext;

    public UsuarioService() {
        this(
                new UsuarioDAO(),
                new SessionContext()
        );
    }

    UsuarioService(
            UsuarioDAO usuarioDAO,
            SessionContext sessionContext
    ) {
        this.usuarioDAO = usuarioDAO;
        this.sessionContext = sessionContext;
    }

    public int crearUsuario(
            String nombre,
            String email,
            String password,
            Integer idFarmaciaSeleccionada
    ) throws SQLException {

        AccessControl.requireRole(
                "SUPER_ADMIN",
                "ADMINISTRADOR"
        );

        validarDatos(
                nombre,
                email,
                password
        );

        UserSession.CurrentUser actor =
                sessionContext
                        .requireCurrentUser();

        Integer farmaciaDestino;

        if (actor.esSuperAdmin()) {

            if (
                    idFarmaciaSeleccionada == null
                    || idFarmaciaSeleccionada <= 0
            ) {

                throw new IllegalArgumentException(
                        "Debe seleccionar la farmacia del nuevo administrador."
                );
            }

            farmaciaDestino =
                    idFarmaciaSeleccionada;

        } else if (actor.esAdministrador()) {

            if (!actor.tieneFarmaciaAsignada()) {

                throw new IllegalStateException(
                        SessionContext.ERROR_FARMACIA_REQUERIDA
                );
            }

            if (
                    idFarmaciaSeleccionada != null
                    && !idFarmaciaSeleccionada.equals(
                            actor.getIdFarmacia()
                    )
            ) {

                throw new AccesoDenegadoException(
                        "Un administrador no puede asignar "
                        + "usuarios a otra farmacia."
                );
            }

            /*
             * No enviamos una farmacia seleccionable.
             * PostgreSQL hereda la farmacia del actor.
             */
            farmaciaDestino = null;

        } else {

            throw new AccesoDenegadoException(
                    "Su rol no puede crear usuarios."
            );
        }

        return usuarioDAO.crearJerarquico(
                actor.getId(),
                nombre,
                email,
                password,
                farmaciaDestino
        );
    }

    private void validarDatos(
            String nombre,
            String email,
            String password
    ) {

        if (
                nombre == null
                || nombre.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El nombre es obligatorio."
            );
        }

        if (
                email == null
                || email.isBlank()
                || !email.contains("@")
        ) {

            throw new IllegalArgumentException(
                    "Debe ingresar un correo electrónico válido."
            );
        }

        if (
                password == null
                || password.length()
                < LONGITUD_MINIMA_PASSWORD
        ) {

            throw new IllegalArgumentException(
                    "La contraseña debe tener al menos "
                    + LONGITUD_MINIMA_PASSWORD
                    + " caracteres."
            );
        }
    }
}
