package org.example.carestock.service;

import org.example.carestock.utils.FechaNegocio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ServicioCrudLotesValidacionesTest {

    @Test
    void aceptaRolesAutorizados() {
        assertTrue(ServicioCrudLotes.rolGestor("ADMINISTRADOR"));
        assertTrue(ServicioCrudLotes.rolGestor("SUPER_ADMIN"));
        assertTrue(ServicioCrudLotes.rolGestor("FARMACEUTICO"));
        assertTrue(ServicioCrudLotes.rolGestor(" administrador "));
    }

    @Test
    void rechazaRolesNoAutorizados() {
        assertFalse(ServicioCrudLotes.rolGestor(null));
        assertFalse(ServicioCrudLotes.rolGestor(""));
        assertFalse(ServicioCrudLotes.rolGestor("AUXILIAR"));
        assertFalse(ServicioCrudLotes.rolGestor("INVITADO"));
    }

    @Test
    void permiteCrearLoteConDatosValidos() {
        assertDoesNotThrow(() -> ServicioCrudLotes.validarNuevo(
                "LOT-001", 10, FechaNegocio.hoy().plusDays(60), 1));
    }

    @Test
    void rechazaNumeroDeLoteInvalido() {
        for (String numero : new String[]{
                "", " ", "x".repeat(51)
        }) {
            final var sonarPrecondicion1 = FechaNegocio.hoy().plusDays(30);
            assertThrows(IllegalArgumentException.class,
                    () -> ServicioCrudLotes.validarNuevo(
                            numero, 10, sonarPrecondicion1, 1));
        }

        final var sonarPrecondicion2 = FechaNegocio.hoy().plusDays(30);
        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        null, 10, sonarPrecondicion2, 1));
    }

    @Test
    void rechazaCantidadInicialNoPositiva() {
        final var sonarPrecondicion3 = FechaNegocio.hoy().plusDays(30);
        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        "LOT-001", 0, sonarPrecondicion3, 1));

        final var sonarPrecondicion4 = FechaNegocio.hoy().plusDays(30);
        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        "LOT-001", -10, sonarPrecondicion4, 1));
    }

    @Test
    void rechazaVencimientoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo("LOT-001", 10, null, 1));

        final var sonarPrecondicion5 = FechaNegocio.hoy().minusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        "LOT-001", 10, sonarPrecondicion5, 1));

        final var sonarPrecondicion6 = FechaNegocio.hoy();
        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        "LOT-001", 10, sonarPrecondicion6, 1));
    }

    @Test
    void exigeUbicacionValida() {
        final var sonarPrecondicion7 = FechaNegocio.hoy().plusDays(30);
        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        "LOT-001", 10, sonarPrecondicion7, 0));
    }

    @Test
    void permiteRetirarLoteSinExistencias() {
        assertDoesNotThrow(
                () -> ServicioCrudLotes.validarRetiro(0, "DISPONIBLE"));
    }

    @Test
    void bloqueaRetiroConStockPositivoDisponible() {
        assertThrows(IllegalStateException.class,
                () -> ServicioCrudLotes.validarRetiro(10, "DISPONIBLE"));
    }

    @Test
    void permiteRetirarLoteDeshabilitado() {
        assertDoesNotThrow(
                () -> ServicioCrudLotes.validarRetiro(10, "DESHABILITADO"));
    }

    @Test
    void permiteRetirarLoteVencido() {
        assertDoesNotThrow(
                () -> ServicioCrudLotes.validarRetiro(
                        10, "DISPONIBLE", FechaNegocio.hoy().minusDays(1)));
    }

    @Test
    void impideRetirarLoteYaRetenido() {
        assertThrows(IllegalStateException.class,
                () -> ServicioCrudLotes.validarRetiro(0, "RETENIDO"));
    }

    @Test
    void opcionesMuestranIdentificadores() {
        var medicamento = new ServicioCrudLotes.MedicamentoOpcion(3, "Acetaminofen");
        var ubicacion = new ServicioCrudLotes.UbicacionOpcion(4, "A", "2");

        assertEquals("Acetaminofen (#3)", medicamento.toString());
        assertEquals("A / 2 (#4)", ubicacion.toString());
    }

    @Test
    void creacionInvalidaSeRechazaAntesDeAccederABaseDatos() {
        ServicioCrudLotes servicio = new ServicioCrudLotes();

        final var sonarPrecondicion8 = FechaNegocio.hoy().plusDays(30);
        assertThrows(IllegalArgumentException.class,
                () -> servicio.crear(1, "LOT", 0,
                        sonarPrecondicion8, 1));

        final var sonarPrecondicion9 = FechaNegocio.hoy().plusDays(30);
        assertThrows(IllegalArgumentException.class,
                () -> servicio.crear(0, "LOT", 10,
                        sonarPrecondicion9, 1));
    }

    @Test
    void rechazaEdicionYRetiroSinLote() {
        ServicioCrudLotes servicio = new ServicioCrudLotes();

        final var sonarPrecondicion10 = FechaNegocio.hoy().plusDays(30);
        assertThrows(NullPointerException.class,
                () -> servicio.editar(null, "LOT",
                        sonarPrecondicion10, 1));

        assertThrows(NullPointerException.class,
                () -> servicio.retirar(null));
    }
}
