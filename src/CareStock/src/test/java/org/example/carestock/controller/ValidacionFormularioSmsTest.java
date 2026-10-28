package org.example.carestock.controller;

import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidacionFormularioSmsTest {

    @Test
    void ui01AdministradorPuedeAcceder() {
        assertTrue(ValidacionFormularioSms.puedeAcceder(
                sesion("ADMINISTRADOR", 10)));
    }

    @Test
    void ui02AuxiliarNoPuedeAcceder() {
        assertFalse(ValidacionFormularioSms.puedeAcceder(
                sesion("AUXILIAR", 10)));
    }

    @Test
    void ui03UsuarioSinFarmaciaNoPuedeAcceder() {
        assertFalse(ValidacionFormularioSms.puedeAcceder(
                sesion("ADMINISTRADOR", null)));
    }

    @Test
    void ui04AceptaTelefonoE164() {
        var solicitud = crear("+573001234567",
                true, true, true, "08:00", "20:00");

        assertEquals("+573001234567", solicitud.telefono());
    }

    @Test
    void ui05RechazaTelefonoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> crear("3001234567", true,
                        true, true, "", ""));
    }

    @Test
    void ui06PreferenciasIndependientes() {
        var solicitud = crear("+573001234567",
                true, true, false, "", "");

        assertTrue(solicitud.notificaStockCritico());
        assertFalse(solicitud.notificaVencimientoCritico());
    }

    @Test
    void ui07AceptaHorarioNocturno() {
        var solicitud = crear("+573001234567",
                true, true, true, "21:00", "06:00");

        assertEquals(21, solicitud.horarioInicio().getHour());
        assertEquals(6, solicitud.horarioFin().getHour());
    }

    @Test
    void ui09RechazaHorarioIncompleto() {
        assertThrows(IllegalArgumentException.class,
                () -> crear("+573001234567",
                        true, true, true, "08:00", ""));
    }

    @Test
    void ui11NoActivaSinConfirmacionExplicita() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidacionFormularioSms.crear(
                        "+573001234567", true, true, true,
                        "", "", "America/Bogota", false));
    }

    @Test
    void rechazaZonaHorariaInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> ValidacionFormularioSms.crear(
                        "+573001234567", false, false, false,
                        "", "", "Zona/Falsa", false));
    }

    private static org.example.carestock.sms
            .ServicioPreferenciasSms.Solicitud crear(
                    String telefono, boolean autorizado,
                    boolean stock, boolean vence,
                    String inicio, String fin) {

        return ValidacionFormularioSms.crear(
                telefono, autorizado, stock, vence,
                inicio, fin, "America/Bogota", autorizado);
    }

    private static SesionUsuario.DatosUsuario sesion(
            String rol, Integer farmacia) {

        return new SesionUsuario.DatosUsuario(
                1, "Usuario QA", "qa@example.invalid",
                rol, farmacia, "Farmacia QA");
    }
}
