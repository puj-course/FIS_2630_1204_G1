package org.example.carestock.sms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.*;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReglasSmsCoverage87Test {

    private static final String ZONA = "America/Bogota";
    private static final String TELEFONO = "+573001234567";

    private static Instant instante(String hora) {
        return LocalDate.of(2026, 11, 8)
            .atTime(LocalTime.parse(hora))
            .atZone(ZoneId.of(ZONA))
            .toInstant();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    void cadaCondicionDeAutorizacionEsObligatoria(int indice) {
        boolean[] condiciones = {
            true, true, true, true, true
        };
        condiciones[indice] = false;

        assertFalse(ReglasNotificacionSms.puedeGenerar(
            condiciones[0], condiciones[1],
            condiciones[2], condiciones[3],
            condiciones[4], "STOCK", true, true));
    }

    @Test
    void generaAlertasHabilitadasPorTipo() {
        assertTrue(ReglasNotificacionSms.puedeGenerar(
            true, true, true, true, true,
            "STOCK", true, false));

        assertTrue(ReglasNotificacionSms.puedeGenerar(
            true, true, true, true, true,
            "VENCIMIENTO", false, true));
    }

    @Test
    void noGeneraAlertasDeshabilitadas() {
        assertFalse(ReglasNotificacionSms.puedeGenerar(
            true, true, true, true, true,
            "STOCK", false, true));

        assertFalse(ReglasNotificacionSms.puedeGenerar(
            true, true, true, true, true,
            "VENCIMIENTO", true, false));
    }

    @Test
    void rechazaTipoDesconocidoONulo() {
        assertFalse(ReglasNotificacionSms.puedeGenerar(
            true, true, true, true, true,
            null, true, true));

        assertFalse(ReglasNotificacionSms.puedeGenerar(
            true, true, true, true, true,
            "OTRO", true, true));
    }

    @ParameterizedTest
    @CsvSource({
        "07:59,false",
        "08:00,true",
        "12:30,true",
        "17:59,true",
        "18:00,false"
    })
    void compruebaLimitesDeHorarioDiurno(
            String hora, boolean permitido) {

        assertEquals(permitido,
            ReglasNotificacionSms.puedeEnviar(
                true, false,
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                ZONA, instante(hora)));
    }

    @ParameterizedTest
    @CsvSource({
        "20:59,false",
        "21:00,true",
        "23:59,true",
        "00:00,true",
        "05:59,true",
        "06:00,false",
        "12:00,false"
    })
    void compruebaHorarioNocturnoQueCruzaMedianoche(
            String hora, boolean permitido) {

        assertEquals(permitido,
            ReglasNotificacionSms.puedeEnviar(
                true, false,
                LocalTime.of(21, 0),
                LocalTime.of(6, 0),
                ZONA, instante(hora)));
    }

    @Test
    void alertaCriticaOmiteSoloRestriccionHoraria() {
        assertTrue(ReglasNotificacionSms.puedeEnviar(
            true, true, LocalTime.of(8, 0),
            LocalTime.of(18, 0), ZONA, instante("23:30")));

        assertFalse(ReglasNotificacionSms.puedeEnviar(
            false, true, LocalTime.of(8, 0),
            LocalTime.of(18, 0), ZONA, instante("12:00")));
    }

    @Test
    void sinRestriccionesHorariasPermiteEnvio() {
        assertTrue(ReglasNotificacionSms.puedeEnviar(
            true, false, null, null,
            ZONA, instante("03:00")));
    }

    @Test
    void horarioIncompletoNoAutoriza() {
        assertFalse(ReglasNotificacionSms.puedeEnviar(
            true, false, LocalTime.of(8, 0),
            null, ZONA, instante("12:00")));

        assertFalse(ReglasNotificacionSms.puedeEnviar(
            true, false, null,
            LocalTime.of(18, 0), ZONA, instante("12:00")));
    }

    @Test
    void horarioConInicioYFinIgualesNoAutoriza() {
        assertFalse(ReglasNotificacionSms.puedeEnviar(
            true, false, LocalTime.NOON,
            LocalTime.NOON, ZONA, instante("12:00")));
    }

    @Test
    void reconoceTiposDeClavesDeAlerta() {
        assertEquals("STOCK",
            ReglasNotificacionSms.tipo("STOCK:15"));

        assertEquals("STOCK",
            ReglasNotificacionSms.tipo("AVISO:STOCK:15"));

        assertEquals("VENCIMIENTO",
            ReglasNotificacionSms.tipo("LOTE:25"));

        assertEquals("VENCIMIENTO",
            ReglasNotificacionSms.tipo(
                "AVISO:VENCIMIENTO:25"));

        assertEquals("",
            ReglasNotificacionSms.tipo("OTRO:25"));

        assertEquals("",
            ReglasNotificacionSms.tipo(null));
    }

    @Test
    void eventoAccesoRequierePreferenciaEspecifica() {
        assertEquals("ACCESO", ReglasNotificacionSms.tipo("ACCESO:uuid"));
        assertFalse(ReglasNotificacionSms.esCritica("ACCESO:uuid"));
        assertTrue(ReglasNotificacionSms.puedeGenerar(
                true, true, true, true, true,
                "ACCESO", false, false, true));
        assertFalse(ReglasNotificacionSms.puedeGenerar(
                true, true, true, true, true,
                "ACCESO", true, true, false));
        assertFalse(ReglasNotificacionSms.puedeGenerar(
                true, false, true, true, true,
                "ACCESO", false, false, true));
    }

    @Test
    void distingueAlertasCriticasDeAvisos() {
        assertTrue(ReglasNotificacionSms.esCritica("STOCK:15"));
        assertTrue(ReglasNotificacionSms.esCritica("LOTE:25"));

        assertFalse(ReglasNotificacionSms.esCritica(
            "AVISO:STOCK:15"));

        assertFalse(ReglasNotificacionSms.esCritica(
            "AVISO:VENCIMIENTO:25"));

        assertFalse(ReglasNotificacionSms.esCritica(null));
    }

    @Test
    void destinatarioAutorizadoPuedeValidarse() {
        var lista = new DestinatariosPermitidosSms(
            Set.of(TELEFONO));

        assertDoesNotThrow(
            () -> lista.exigirAutorizado(TELEFONO));
    }

    @Test
    void destinatarioNoAutorizadoEsBloqueado() {
        var lista = new DestinatariosPermitidosSms(
            Set.of(TELEFONO));

        assertThrows(SecurityException.class,
            () -> lista.exigirAutorizado("+573009999999"));
    }

    @Test
    void numeroMalFormadoSeRechazaAntesDeConsultarPermisos() {
        var lista = new DestinatariosPermitidosSms(
            Set.of(TELEFONO));

        assertThrows(IllegalArgumentException.class,
            () -> lista.exigirAutorizado("3001234567"));
    }

    @Test
    void csvPermiteMultiplesNumerosConEspacios() {
        var lista = DestinatariosPermitidosSms.desdeCsv(
            " +573001234567 , +573002345678 ");

        assertDoesNotThrow(
            () -> lista.exigirAutorizado("+573001234567"));

        assertDoesNotThrow(
            () -> lista.exigirAutorizado("+573002345678"));
    }

    @Test
    void csvNuloOVacioSeRechaza() {
        assertThrows(IllegalArgumentException.class,
            () -> DestinatariosPermitidosSms.desdeCsv(null));

        assertThrows(IllegalArgumentException.class,
            () -> DestinatariosPermitidosSms.desdeCsv("   "));
    }

    @Test
    void conjuntoVacioDeDestinatariosSeRechaza() {
        assertThrows(IllegalArgumentException.class,
            () -> new DestinatariosPermitidosSms(Set.of()));
    }

    @Test
    void conjuntoConNumeroInvalidoSeRechaza() {
        assertThrows(IllegalArgumentException.class,
            () -> new DestinatariosPermitidosSms(
                Set.of("NO-ES-TELEFONO")));
    }
}
