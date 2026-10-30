package org.example.carestock.DataTransferObject;

import org.example.carestock.model.Lote;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class DatosTransferenciaTest {
    @Test
    void datosLotesCopiaColecciones() {
        List<Lote> lotes = new ArrayList<>();
        lotes.add(new Lote());
        Map<Integer, String> categorias = new HashMap<>();
        categorias.put(1, "Analgesicos");
        Map<String, Integer> conteos = new HashMap<>();
        conteos.put("DISPONIBLE", 1);
        DatosLotes datos = new DatosLotes(lotes, categorias, categorias, conteos);
        lotes.clear();
        categorias.clear();
        conteos.clear();
        assertEquals(1, datos.lotes().size());
        assertEquals("Analgesicos", datos.categorias().get(1));
        assertEquals("Analgesicos", datos.categoriaPorMedicamento().get(1));
        assertEquals(Integer.valueOf(1), datos.conteoTipos().get("DISPONIBLE"));
        assertThrows(UnsupportedOperationException.class, () -> datos.lotes().clear());
        assertThrows(UnsupportedOperationException.class, () -> datos.categorias().clear());
    }

    @Test
    void datosCatalogoConservaOrdenYCopiaDatos() {
        List<RegistroCatalogo<?>> productos = new ArrayList<>();
        productos.add(new RegistroCatalogo<>(1, "Jabon", "ACTIVO"));
        Map<Integer, String> categorias = new LinkedHashMap<>();
        categorias.put(2, "Higiene");
        categorias.put(1, "Medicamentos");
        DatosCatalogo datos = new DatosCatalogo(productos, categorias, 123L);
        productos.clear();
        categorias.clear();
        assertEquals(1, datos.productos().size());
        assertEquals(123L, datos.creadoNanos());
        assertEquals(List.of(2, 1), new ArrayList<>(datos.categoriasMedicamento().keySet()));
        assertThrows(UnsupportedOperationException.class, () -> datos.productos().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> datos.categoriasMedicamento().clear());
    }
}
