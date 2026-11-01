package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DispositivoMedicoQualityGateTest {

    private DispositivoMedico dispositivoValido() {
        DispositivoMedico dispositivo = new DispositivoMedico();
        dispositivo.setNombre("Termometro");
        dispositivo.setClaseRiesgo("I");
        dispositivo.setPrecio(100);
        dispositivo.setStock(5);
        return dispositivo;
    }

    @Test
    void rechazaNombreNuloYNombreVacio() {
        DispositivoMedico dispositivo = dispositivoValido();

        dispositivo.setNombre(null);
        assertThrows(
                ReglaNegocioException.class,
                dispositivo::validar);
        assertFalse(dispositivo.esAptoParaDispensar());

        dispositivo.setNombre(" ");
        assertThrows(
                ReglaNegocioException.class,
                dispositivo::validar);
        assertFalse(dispositivo.esAptoParaDispensar());
    }

    @Test
    void rechazaClaseDeRiesgoNulaYVacia() {
        DispositivoMedico dispositivo = dispositivoValido();

        dispositivo.setClaseRiesgo(null);
        assertThrows(
                ReglaNegocioException.class,
                dispositivo::validar);
        assertFalse(dispositivo.esAptoParaDispensar());

        dispositivo.setClaseRiesgo(" ");
        assertThrows(
                ReglaNegocioException.class,
                dispositivo::validar);
        assertFalse(dispositivo.esAptoParaDispensar());
    }

    @Test
    void rechazaPreciosInvalidos() {
        DispositivoMedico dispositivo = dispositivoValido();

        for (double precio : new double[]{
                -1, Double.NaN, Double.POSITIVE_INFINITY}) {

            dispositivo.setPrecio(precio);

            assertThrows(
                    ReglaNegocioException.class,
                    dispositivo::validar);

            assertFalse(dispositivo.esAptoParaDispensar());
        }

        dispositivo.setPrecio(0);
        assertDoesNotThrow(dispositivo::validar);
        assertTrue(dispositivo.esAptoParaDispensar());
    }

    @Test
    void rechazaStockNegativoYAgotado() {
        DispositivoMedico dispositivo = dispositivoValido();

        dispositivo.setStock(-1);
        assertThrows(
                ReglaNegocioException.class,
                dispositivo::validar);
        assertFalse(dispositivo.esAptoParaDispensar());

        dispositivo.setStock(0);
        assertDoesNotThrow(dispositivo::validar);
        assertFalse(dispositivo.esAptoParaDispensar());

        dispositivo.setStock(5);
        assertTrue(dispositivo.esAptoParaDispensar());
    }
}
