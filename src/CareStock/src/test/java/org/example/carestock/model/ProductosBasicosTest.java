package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductosBasicosTest {
    private Aseo aseo() {
        Aseo a = new Aseo();
        a.setCodigo("ASE-01");
        a.setNombre("Jabon");
        a.setDescripcion("Liquido");
        a.setPrecio(5000);
        a.setStock(4);
        return a;
    }

    private Maternidad maternidad() {
        Maternidad m = new Maternidad();
        m.setCodigo("MAT-01");
        m.setNombre("Crema");
        m.setDescripcion("  Cuidado  ");
        m.setPrecio(5000);
        m.setStock(3);
        return m;
    }

    @Test
    void aseoValidoDispensableYConMetadatos() throws Exception {
        Aseo a = aseo();
        a.setIdAseo(3);
        a.setIdFarmacia(1);
        a.setIdUsuarioCreacion(8);
        a.setTipoAseo("Corporal");
        a.setBiodegradable(true);
        a.setComponentesActivos("Glicerina");
        a.validar();
        assertTrue(a.esAptoParaDispensar());
        assertAll(() -> assertEquals(3, a.getIdAseo()),
                  () -> assertEquals(Integer.valueOf(1), a.getIdFarmacia()),
                  () -> assertEquals(Integer.valueOf(8), a.getIdUsuarioCreacion()),
                  () -> assertEquals("Corporal", a.getTipoAseo()),
                  () -> assertTrue(a.isBiodegradable()),
                  () -> assertEquals("Glicerina", a.getComponentesActivos()));
    }

    @Test
    void aseoRechazaCamposInvalidosYEstadoInactivo() {
        Aseo a = aseo();
        a.setCodigo(" ");
        assertThrows(ReglaNegocioException.class, a::validar);
        a = aseo(); a.setNombre("");
        assertThrows(ReglaNegocioException.class, a::validar);
        a = aseo(); a.setDescripcion(null);
        assertThrows(ReglaNegocioException.class, a::validar);
        a = aseo(); a.setPrecio(Double.NaN);
        assertThrows(ReglaNegocioException.class, a::validar);
        a = aseo(); a.setPrecio(-1);
        assertThrows(ReglaNegocioException.class, a::validar);
        a = aseo(); a.setStock(-1);
        assertThrows(ReglaNegocioException.class, a::validar);
        a = aseo(); a.setEstado("INVALIDO");
        assertThrows(ReglaNegocioException.class, a::validar);
        a = aseo(); a.setEstado("INACTIVO");
        assertFalse(a.esAptoParaDispensar());
        a = aseo(); a.setStock(0);
        assertFalse(a.esAptoParaDispensar());
    }

    @Test
    void maternidadValidaYDispensa() throws Exception {
        Maternidad m = maternidad();
        m.setEtapaRecomendada("Prenatal");
        m.setHipoalergenico(true);
        m.setEdadGestacionalSugerida(20);
        m.validar();
        assertTrue(m.esAptoParaDispensar());
        assertEquals("Cuidado", m.getDescripcion());
        assertEquals("Prenatal", m.getEtapaRecomendada());
        assertTrue(m.isHipoalergenico());
        assertEquals(20, m.getEdadGestacionalSugerida());
    }

    @Test
    void maternidadRechazaDatosInvalidos() {
        Maternidad m = maternidad(); m.setCodigo(null);
        assertThrows(ReglaNegocioException.class, m::validar);
        m = maternidad(); m.setNombre("");
        assertThrows(ReglaNegocioException.class, m::validar);
        m = maternidad(); m.setPrecio(Double.POSITIVE_INFINITY);
        assertThrows(ReglaNegocioException.class, m::validar);
        m = maternidad(); m.setStock(-1);
        assertThrows(ReglaNegocioException.class, m::validar);
        m = maternidad(); m.setEdadGestacionalSugerida(-1);
        assertThrows(ReglaNegocioException.class, m::validar);
        m = maternidad(); m.setStock(0);
        assertFalse(m.esAptoParaDispensar());
        assertThrows(IllegalArgumentException.class, () -> new Maternidad().setDescripcion(" "));
    }

    @Test
    void cosmeticoValidaYControlaDispensacion() throws Exception {
        Cosmetico c = new Cosmetico();
        c.setIdCosmetico(5);
        c.setCodigo("COS-1");
        c.setNombre("Crema");
        c.setRegistroSanitario("REG-1");
        c.setPrecio(15);
        c.setStock(2);
        c.setTipoPiel("Seca");
        c.validar();
        assertTrue(c.esAptoParaDispensar());
        assertEquals(5, c.getIdCosmetico());
        assertEquals("Seca", c.getTipoPiel());
        c.setStock(0);
        assertFalse(c.esAptoParaDispensar());
        c.setStock(1); c.setRegistroSanitario(" ");
        assertThrows(ReglaNegocioException.class, c::validar);
        assertFalse(c.esAptoParaDispensar());
        c.setRegistroSanitario("REG-1"); c.setPrecio(-1);
        assertThrows(ReglaNegocioException.class, c::validar);
        c.setPrecio(1); c.setNombre(" ");
        assertThrows(ReglaNegocioException.class, c::validar);
    }

    @Test
    void dispositivoValidaYControlaDispensacion() throws Exception {
        DispositivoMedico d = new DispositivoMedico();
        d.setIdDispositivo(9);
        d.setCodigo("DIS-1");
        d.setNombre("Termometro");
        d.setDescripcion("Digital");
        d.setClaseRiesgo("I");
        d.setRegistroSanitario("RS-1");
        d.setPrecio(100);
        d.setStock(2);
        d.validar();
        assertTrue(d.esAptoParaDispensar());
        assertEquals(9, d.getIdDispositivo());
        assertEquals("RS-1", d.getRegistroSanitario());
        d.setStock(0);
        assertFalse(d.esAptoParaDispensar());
        d.setStock(-1);
        assertThrows(ReglaNegocioException.class, d::validar);
        d.setStock(2); d.setPrecio(Double.NaN);
        assertThrows(ReglaNegocioException.class, d::validar);
        assertFalse(d.esAptoParaDispensar());
        d.setPrecio(10); d.setClaseRiesgo(" ");
        assertThrows(ReglaNegocioException.class, d::validar);
        d.setClaseRiesgo("I"); d.setNombre(null);
        assertThrows(ReglaNegocioException.class, d::validar);
    }

    @Test
    void usuarioConservaIdentidadYRol() {
        Usuario u = new Usuario(1, "Ana", "ana@test.local", "hash", "ADMINISTRADOR");
        assertEquals(1, u.getIdUsuario());
        assertEquals("Ana", u.getNombre());
        assertEquals("ana@test.local", u.getEmail());
        assertEquals("hash", u.getPassword());
        assertEquals("ADMINISTRADOR", u.getRol());
        Usuario vacio = new Usuario();
        vacio.setIdUsuario(2);
        vacio.setNombre("Luis");
        vacio.setEmail("luis@test.local");
        vacio.setPassword("otro-hash");
        vacio.setRol("FARMACEUTICO");
        assertEquals(2, vacio.getIdUsuario());
        assertEquals("Luis", vacio.getNombre());
        assertEquals("luis@test.local", vacio.getEmail());
        assertEquals("otro-hash", vacio.getPassword());
        assertEquals("FARMACEUTICO", vacio.getRol());
    }
}
