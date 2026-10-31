package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RefactorizacionProductosTest {

    @Test
    void conservaPropiedadesHeredadasEnDispositivoMedico() {
        ProductoBase[] productos = {
                new DispositivoMedico()
        };

        for (ProductoBase producto : productos) {
            producto.setCodigo("COD-01");
            producto.setNombre("Producto");
            producto.setDescripcion("Descripcion");
            producto.setPrecio(12.5);
            producto.setStock(3);

            assertAll(
                    () -> assertEquals("COD-01", producto.getCodigo()),
                    () -> assertEquals("Producto", producto.getNombre()),
                    () -> assertEquals("Descripcion", producto.getDescripcion()),
                    () -> assertEquals(12.5, producto.getPrecio()),
                    () -> assertEquals(3, producto.getStock())
            );
        }
    }

    private Aseo aseoValido() {
        Aseo aseo = new Aseo();
        aseo.setCodigo("ASE-01");
        aseo.setNombre("Jabon");
        aseo.setDescripcion("Liquido");
        aseo.setPrecio(5000);
        aseo.setStock(4);
        return aseo;
    }

    private Maternidad maternidadValida() {
        Maternidad maternidad = new Maternidad();
        maternidad.setCodigo("MAT-01");
        maternidad.setNombre("Crema");
        maternidad.setDescripcion("  Cuidado  ");
        maternidad.setPrecio(5000);
        maternidad.setStock(3);
        return maternidad;
    }

    @Test
    void aseoConservaReglaDeEstadoYDisponibilidad()
            throws Exception {

        Aseo aseo = aseoValido();

        assertEquals("ACTIVO", aseo.getEstado());
        aseo.validar();
        assertTrue(aseo.esAptoParaDispensar());

        aseo.setEstado("INACTIVO");
        aseo.validar();
        assertFalse(aseo.esAptoParaDispensar());

        aseo.setEstado("DESCONOCIDO");
        assertEquals(
                "El estado debe ser ACTIVO o INACTIVO.",
                assertThrows(
                        ReglaNegocioException.class,
                        aseo::validar
                ).getMessage()
        );
        assertFalse(aseo.esAptoParaDispensar());
    }

    @Test
    void validacionesComunesConservanMensajesYRechazanInvalidos() {
        Aseo aseo = aseoValido();
        aseo.setCodigo(" ");

        assertEquals(
                "El codigo del producto de aseo es obligatorio.",
                assertThrows(
                        ReglaNegocioException.class,
                        aseo::validar
                ).getMessage()
        );

        aseo = aseoValido();
        aseo.setPrecio(Double.NaN);

        assertEquals(
                "El precio del producto de aseo debe ser valido y no negativo.",
                assertThrows(
                        ReglaNegocioException.class,
                        aseo::validar
                ).getMessage()
        );

        Maternidad maternidad = maternidadValida();
        maternidad.setNombre("");

        assertEquals(
                "El nombre del producto de maternidad es obligatorio.",
                assertThrows(
                        ReglaNegocioException.class,
                        maternidad::validar
                ).getMessage()
        );

        maternidad = maternidadValida();
        maternidad.setStock(-1);

        assertEquals(
                "El stock del producto de maternidad no puede ser negativo.",
                assertThrows(
                        ReglaNegocioException.class,
                        maternidad::validar
                ).getMessage()
        );
    }

    @Test
    void maternidadMantieneNormalizacionYReglasPropias()
            throws Exception {

        Maternidad maternidad = maternidadValida();

        assertEquals("Cuidado", maternidad.getDescripcion());
        maternidad.validar();
        assertTrue(maternidad.esAptoParaDispensar());

        assertThrows(
                IllegalArgumentException.class,
                () -> maternidad.setDescripcion(" ")
        );

        maternidad.setEdadGestacionalSugerida(-1);

        assertEquals(
                "La edad gestacional sugerida no puede ser negativa.",
                assertThrows(
                        ReglaNegocioException.class,
                        maternidad::validar
                ).getMessage()
        );

        assertFalse(maternidad.esAptoParaDispensar());
    }

    @Test
    void dispositivoMantieneReglasPropiasYConstructorSinArgumentos()
            throws Exception {

        DispositivoMedico dispositivo = new DispositivoMedico();
        dispositivo.setNombre("Termometro");
        dispositivo.setClaseRiesgo("I");
        dispositivo.setPrecio(100);
        dispositivo.setStock(2);

        dispositivo.validar();
        assertTrue(dispositivo.esAptoParaDispensar());

        dispositivo.setClaseRiesgo(" ");

        assertEquals(
                "La clase de riesgo del dispositivo médico es obligatoria.",
                assertThrows(
                        ReglaNegocioException.class,
                        dispositivo::validar
                ).getMessage()
        );

        assertFalse(dispositivo.esAptoParaDispensar());

        dispositivo.setClaseRiesgo("I");
        dispositivo.setStock(0);
        assertFalse(dispositivo.esAptoParaDispensar());
    }

    @Test
    void aseoConservaAtributosComunes() {
        Aseo aseo = aseoValido();

        assertAll(
                () -> assertEquals("ASE-01", aseo.getCodigo()),
                () -> assertEquals("Jabon", aseo.getNombre()),
                () -> assertEquals("Liquido", aseo.getDescripcion()),
                () -> assertEquals(5000.0, aseo.getPrecio()),
                () -> assertEquals(4, aseo.getStock())
        );
    }

    @Test
    void maternidadConservaAtributosComunes() {
        Maternidad maternidad = maternidadValida();

        assertAll(
                () -> assertEquals("MAT-01", maternidad.getCodigo()),
                () -> assertEquals("Crema", maternidad.getNombre()),
                () -> assertEquals("Cuidado", maternidad.getDescripcion()),
                () -> assertEquals(5000.0, maternidad.getPrecio()),
                () -> assertEquals(3, maternidad.getStock())
        );
    }
}
