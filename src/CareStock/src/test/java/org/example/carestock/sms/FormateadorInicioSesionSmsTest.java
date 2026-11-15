package org.example.carestock.sms;

import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class FormateadorInicioSesionSmsTest {
    private final FormateadorInicioSesionSms formateador =
            new FormateadorInicioSesionSms();

    private static SesionUsuario.DatosUsuario sesion() {
        return new SesionUsuario.DatosUsuario(
                13, "Ana García", "ana@prueba.local", "FARMACEUTICO",
                2, "Farmacia Central");
    }

    @Test
    void incluyeIdentidadFarmaciaYHoraDeBogota() {
        String texto = formateador.generar(
                sesion(), Instant.parse("2026-11-11T19:50:00Z"));
        assertAll(
                () -> assertTrue(texto.contains("Ana García")),
                () -> assertTrue(texto.contains("FARMACEUTICO")),
                () -> assertTrue(texto.contains("Farmacia Central")),
                () -> assertTrue(texto.contains("11/11/2026 14:50")),
                () -> assertFalse(texto.contains("ana@prueba.local")),
                () -> assertFalse(texto.contains("2026-11-11T19:50:00Z")));
    }

    @Test
    void limpiaSeparadoresYSaltosDeLinea() {
        var sesion = new SesionUsuario.DatosUsuario(
                2, "A|B\nC", "x@test", "ADMIN|TEST",
                3, "Norte\rSur");
        String mensaje = formateador.generar(
                sesion, Instant.parse("2026-11-11T19:50:00Z"));
        assertTrue(mensaje.contains("A B C"));
        assertTrue(mensaje.contains("ADMIN TEST"));
        assertTrue(mensaje.contains("Norte Sur"));
        assertFalse(mensaje.contains("\n"));
        assertFalse(mensaje.contains("\r"));
    }

    @Test
    void validaDatosObligatorios() {
        assertThrows(NullPointerException.class,
                () -> formateador.generar(null, Instant.now()));
        assertThrows(NullPointerException.class,
                () -> formateador.generar(sesion(), null));
        assertThrows(IllegalArgumentException.class,
                () -> formateador.generar(new SesionUsuario.DatosUsuario(
                        1, "Ana", "a@b", "ADMINISTRADOR", null, null),
                        Instant.now()));
    }
}
