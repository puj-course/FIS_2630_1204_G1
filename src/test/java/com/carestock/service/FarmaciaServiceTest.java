package com.carestock.service;

import com.carestock.exception.AccesoDenegadoException;
import com.carestock.model.Usuario;
import com.carestock.session.UserSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FarmaciaServiceTest {

    private final UserSession userSession =
            UserSession.getInstance();


    @AfterEach
    void limpiarSesion() {

        userSession.clearSession();
    }


    @Test
    void administradorNoPuedeCrearFarmacia() {

        Usuario administrador =
                new Usuario(
                        50,
                        "Administrador Prueba",
                        "admin@test.com",
                        "hash",
                        2,
                        "ADMINISTRADOR",
                        "ACTIVO",
                        1
                );


        userSession.setCurrentUser(
                administrador
        );


        assertThrows(
                AccesoDenegadoException.class,
                () ->
                        new FarmaciaService()
                                .crearFarmacia(
                                        "SEDE-X",
                                        "Farmacia X"
                                )
        );
    }


    @Test
    void farmaceuticoNoPuedeCrearFarmacia() {

        Usuario farmaceutico =
                new Usuario(
                        51,
                        "Farmacéutico Prueba",
                        "farm@test.com",
                        "hash",
                        3,
                        "FARMACEUTICO",
                        "ACTIVO",
                        1
                );


        userSession.setCurrentUser(
                farmaceutico
        );


        assertThrows(
                AccesoDenegadoException.class,
                () ->
                        new FarmaciaService()
                                .crearFarmacia(
                                        "SEDE-Y",
                                        "Farmacia Y"
                                )
        );
    }
}
