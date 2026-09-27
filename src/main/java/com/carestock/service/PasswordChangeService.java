package com.carestock.service;

import com.carestock.dao.UsuarioDAO;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Gestiona las reglas de negocio relacionadas con
 * el cambio seguro de contraseña.
 *
 * HU.58 - #308
 */
public final class PasswordChangeService {

    public static final int LONGITUD_MINIMA_PASSWORD = 8;

    private final UsuarioDAO usuarioDAO;

    public PasswordChangeService() {

        this(
                new UsuarioDAO()
        );
    }

    public PasswordChangeService(
            UsuarioDAO usuarioDAO
    ) {

        this.usuarioDAO =
                Objects.requireNonNull(
                        usuarioDAO
                );
    }

    public ResultadoCambio cambiarPassword(
            int idUsuario,
            String passwordActual,
            String passwordNueva,
            String confirmacionPassword
    ) throws SQLException {

        if (idUsuario <= 0) {

            throw new IllegalArgumentException(
                    "El identificador del usuario no es válido."
            );
        }

        if (
                esVacio(passwordActual)
                || esVacio(passwordNueva)
                || esVacio(confirmacionPassword)
        ) {

            return ResultadoCambio.CAMPOS_INCOMPLETOS;
        }

        /*
         * #340
         * Las dos contraseñas nuevas deben coincidir.
         */
        if (!passwordNueva.equals(confirmacionPassword)) {

            return ResultadoCambio.NUEVAS_NO_COINCIDEN;
        }

        /*
         * Mantener la misma política utilizada
         * actualmente para creación de usuarios.
         */
        if (
                passwordNueva.length()
                < LONGITUD_MINIMA_PASSWORD
        ) {

            return ResultadoCambio.NUEVA_DEMASIADO_CORTA;
        }

        /*
         * #339
         * Recuperar el hash actual desde PostgreSQL.
         */
        String hashActual =
                usuarioDAO
                        .obtenerPasswordHashActivoPorId(
                                idUsuario
                        );

        if (
                hashActual == null
                || !passwordValida(
                        passwordActual,
                        hashActual
                )
        ) {

            return ResultadoCambio.ACTUAL_INCORRECTA;
        }

        /*
         * #341
         * Nunca almacenar la nueva contraseña
         * en texto plano.
         */
        String nuevoHash =
                BCrypt.hashpw(
                        passwordNueva,
                        BCrypt.gensalt()
                );

        /*
         * Se utiliza también el hash anterior como
         * condición para evitar sobreescribir un cambio
         * concurrente de contraseña.
         */
        boolean actualizado =
                usuarioDAO
                        .actualizarPasswordHash(
                                idUsuario,
                                hashActual,
                                nuevoHash
                        );

        return actualizado
                ? ResultadoCambio.EXITOSO
                : ResultadoCambio.NO_ACTUALIZADO;
    }

    private boolean esVacio(
            String valor
    ) {

        return valor == null
                || valor.isBlank();
    }

    private boolean passwordValida(
            String password,
            String hash
    ) {

        try {

            return BCrypt.checkpw(
                    password,
                    hash
            );

        } catch (IllegalArgumentException e) {

            /*
             * Un hash inválido nunca debe producir
             * autenticación satisfactoria.
             */
            return false;
        }
    }

    public enum ResultadoCambio {

        EXITOSO,

        CAMPOS_INCOMPLETOS,

        NUEVAS_NO_COINCIDEN,

        NUEVA_DEMASIADO_CORTA,

        ACTUAL_INCORRECTA,

        NO_ACTUALIZADO
    }
}
