package org.example.carestock.factory;

import org.example.carestock.model.Aseo;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.Medicamento;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FabricaProductoTest {

    @Test
    void creaAseo() {
        Aseo a = new FabricaAseo().crear(p -> {
            p.setCodigo("A001");
            p.setNombre("Jabon");
            p.setDescripcion("Jabon liquido");
            p.setPrecio(1200);
            p.setStock(10);
        });
        assertEquals("A001", a.getCodigo());
    }

    @Test
    void creaMaternidad() {
        Maternidad m = new FabricaMaternidad().crear(p -> {
            p.setCodigo("M001");
            p.setNombre("Crema");
            p.setDescripcion("Uso materno");
            p.setPrecio(2000);
            p.setStock(3);
        });
        assertEquals("M001", m.getCodigo());
    }

    @Test
    void creaMedicamento() {
        Medicamento m = new FabricaMedicamento().crear(p -> {
            p.setCodigoInvima("INV-01");
            p.setNombreComercial("Demo");
            p.setEstado("ACTIVO");
        });
        assertEquals("INV-01", m.getCodigoInvima());
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThrows(IllegalStateException.class,
            () -> new FabricaAseo().crear(
                p -> p.setNombre("Sin codigo")));
    }

    @Test
    void generaInstanciasDistintas() {
        FabricaMedicamento f = new FabricaMedicamento();
        Medicamento a = f.crear(p -> {
            p.setCodigoInvima("1");
            p.setNombreComercial("A");
        });
        Medicamento b = f.crear(p -> {
            p.setCodigoInvima("2");
            p.setNombreComercial("B");
        });
        assertNotSame(a, b);
    }
}
