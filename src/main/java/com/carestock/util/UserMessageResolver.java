package com.carestock.util;

import com.carestock.exception.AccesoDenegadoException;
import com.carestock.session.SessionContext;

import java.sql.SQLException;

public final class UserMessageResolver {

    public static final String ERROR_BASE_DATOS =
            "No fue posible consultar la información. "
            + "Intente nuevamente.";

    private UserMessageResolver() {
    }

    public static String resolve(
            Throwable error
    ) {

        if (error instanceof SQLException sqlException) {

            String sqlState =
                    sqlException.getSQLState();

            if ("23505".equals(sqlState)) {

                return "Ya existe un registro "
                        + "con esos datos.";
            }

            if ("23503".equals(sqlState)) {

                return "La operación contiene "
                        + "una referencia inválida.";
            }

            return ERROR_BASE_DATOS;
        }


        if (error instanceof IllegalStateException) {

            String mensaje =
                    error.getMessage();

            if (
                    SessionContext.ERROR_SESION_REQUERIDA
                            .equals(mensaje)
            ) {

                return "La sesión no está disponible. "
                        + "Inicie sesión nuevamente.";
            }

            if (
                    SessionContext.ERROR_FARMACIA_REQUERIDA
                            .equals(mensaje)
            ) {

                return "El usuario activo no tiene "
                        + "una farmacia asociada.";
            }

            return "No fue posible completar la operación "
                    + "con el contexto actual.";
        }


        if (error instanceof AccesoDenegadoException) {

            return "No tiene permisos para realizar "
                    + "esta operación.";
        }


        return "Ocurrió un error inesperado. "
                + "Intente nuevamente.";
    }
}
