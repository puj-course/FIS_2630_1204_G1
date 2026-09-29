package com.carestock.util;

import com.carestock.session.SessionContext;

import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class UserMessageResolverTest {

    @Test
    void errorJdbcNoExponeDetallesInternos() {

        SQLException error =
                new SQLException(
                        "password authentication failed "
                        + "for user neondb_owner "
                        + "jdbc:postgresql://servidor/"
                        + "?password=secreto",
                        "08001"
                );


        String mensaje =
                UserMessageResolver
                        .resolve(error);


        assertEquals(
                UserMessageResolver.ERROR_BASE_DATOS,
                mensaje
        );


        assertFalse(
                mensaje.contains(
                        "secreto"
                )
        );

        assertFalse(
                mensaje.contains(
                        "jdbc:"
                )
        );

        assertFalse(
                mensaje.contains(
                        "neondb_owner"
                )
        );
    }


    @Test
    void sesionAusenteGeneraMensajeControlado() {

        IllegalStateException error =
                new IllegalStateException(
                        SessionContext
                                .ERROR_SESION_REQUERIDA
                );


        assertEquals(
                "La sesión no está disponible. "
                + "Inicie sesión nuevamente.",
                UserMessageResolver
                        .resolve(error)
        );
    }


    @Test
    void farmaciaAusenteGeneraMensajeControlado() {

        IllegalStateException error =
                new IllegalStateException(
                        SessionContext
                                .ERROR_FARMACIA_REQUERIDA
                );


        assertEquals(
                "El usuario activo no tiene "
                + "una farmacia asociada.",
                UserMessageResolver
                        .resolve(error)
        );
    }


    @Test
    void restriccionUnicaProduceMensajeFuncional() {

        SQLException error =
                new SQLException(
                        "detalle interno",
                        "23505"
                );


        assertEquals(
                "Ya existe un registro con esos datos.",
                UserMessageResolver
                        .resolve(error)
        );
    }
}
