package org.example.carestock.controller;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Cobertura de las tres salidas del traductor de errores del inicio de sesion. */
class LoginControllerErroresSonarTest {

    private static String descripcion(Throwable causa) throws Exception {
        Method metodo = LoginController.class.getDeclaredMethod(
                "describirErrorInicioSesion", Throwable.class);
        metodo.setAccessible(true);
        return (String) metodo.invoke(null, causa);
    }

    @Test
    void conservaElMensajeDeReglaDeNegocio() throws Exception {
        assertEquals("Acceso denegado",
                descripcion(new ReglaNegocioException("Acceso denegado")));
    }

    @Test
    void presentaMensajeControladoSiFallaElInicioDeSesion() throws Exception {
        Class<?> tipo = Class.forName(
                "org.example.carestock.controller.LoginController$ErrorDeInicioDeSesion");
        Constructor<?> constructor = tipo.getDeclaredConstructor(Exception.class);
        constructor.setAccessible(true);
        Throwable causa = (Throwable) constructor.newInstance(new IllegalStateException("interno"));

        assertEquals("No fue posible abrir la sesión. Inténtalo nuevamente.",
                descripcion(causa));
    }

    @Test
    void ocultaLosDetallesInternosDeErroresInesperados() throws Exception {
        assertEquals("No fue posible verificar las credenciales. Inténtalo nuevamente.",
                descripcion(new IllegalArgumentException("secreto")));
    }
}
