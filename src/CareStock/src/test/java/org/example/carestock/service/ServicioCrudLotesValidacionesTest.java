package org.example.carestock.service;

import org.junit.jupiter.api.Test;
import org.example.carestock.model.Lote;
import java.time.LocalDate;

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
                "LOT-001", 10, LocalDate.now().plusDays(60), 1));
    }

    @Test
    void rechazaNumeroDeLoteInvalido() {
        for (String numero : new String[]{
                "", " ", "x".repeat(51)
        }) {
            assertThrows(IllegalArgumentException.class,
                    () -> ServicioCrudLotes.validarNuevo(
                            numero, 10, LocalDate.now().plusDays(30), 1));
        }

        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        null, 10, LocalDate.now().plusDays(30), 1));
    }

    @Test
    void rechazaCantidadInicialNoPositiva() {
        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        "LOT-001", 0, LocalDate.now().plusDays(30), 1));

        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        "LOT-001", -10, LocalDate.now().plusDays(30), 1));
    }

    @Test
    void rechazaVencimientoInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo("LOT-001", 10, null, 1));

        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        "LOT-001", 10, LocalDate.now().minusDays(1), 1));

        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        "LOT-001", 10, LocalDate.now(), 1));
    }

    @Test
    void exigeUbicacionValida() {
        assertThrows(IllegalArgumentException.class,
                () -> ServicioCrudLotes.validarNuevo(
                        "LOT-001", 10, LocalDate.now().plusDays(30), 0));
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
                        10, "DISPONIBLE", LocalDate.now().minusDays(1)));
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

        assertThrows(IllegalArgumentException.class,
                () -> servicio.crear(1, "LOT", 0,
                        LocalDate.now().plusDays(30), 1));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.crear(0, "LOT", 10,
                        LocalDate.now().plusDays(30), 1));
    }

    @Test
    void rechazaEdicionYRetiroSinLote() {
        ServicioCrudLotes servicio = new ServicioCrudLotes();

        assertThrows(NullPointerException.class,
                () -> servicio.editar(null, "LOT",
                        LocalDate.now().plusDays(30), 1));

        assertThrows(NullPointerException.class,
                () -> servicio.retirar(null));
    }
}
