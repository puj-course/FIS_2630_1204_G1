package org.example.carestock.controller;

import org.example.carestock.model.Aseo;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.Medicamento;
import org.example.carestock.model.Lote;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class ControladoresLogicaCoberturaTest {

    private static Object ejecutar(
            Class<?> clase,
            Object instancia,
            String metodo,
            Class<?> parametro,
            Object valor) throws Exception {

        Method m = clase.getDeclaredMethod(metodo, parametro);
        m.setAccessible(true);
        return m.invoke(instancia, valor);
    }

    private static Object catalogo(
            CatalogoController controlador,
            String metodo,
            Object producto) throws Exception {

        return ejecutar(CatalogoController.class,
                controlador, metodo, Object.class, producto);
    }

    @Test
    void muestraCorrectamenteProductosDeAseo() throws Exception {
        CatalogoController controlador = new CatalogoController();

        Aseo producto = new Aseo();
        producto.setCodigo("ASE-001");
        producto.setNombre("Jabon");
        producto.setDescripcion("Jabon liquido");
        producto.setPrecio(12500);
        producto.setStock(8);

        assertAll(
                () -> assertEquals("ASE-001",
                        catalogo(controlador, "codigo", producto)),
                () -> assertEquals("Jabon",
                        catalogo(controlador, "nombre", producto)),
                () -> assertEquals("Jabon liquido",
                        catalogo(controlador, "descripcion", producto)),
                () -> assertEquals("12500.0",
                        catalogo(controlador, "precioTexto", producto)),
                () -> assertEquals(8,
                        catalogo(controlador, "stock", producto))
        );
    }

    @Test
    void muestraCorrectamenteProductosDeMaternidad() throws Exception {
        CatalogoController controlador = new CatalogoController();

        Maternidad producto = new Maternidad();
        producto.setCodigo("MAT-001");
        producto.setNombre("Kit maternal");
        producto.setDescripcion("Cuidado prenatal");
        producto.setPrecio(45000);
        producto.setStock(3);

        assertAll(
                () -> assertEquals("MAT-001",
                        catalogo(controlador, "codigo", producto)),
                () -> assertEquals("Kit maternal",
                        catalogo(controlador, "nombre", producto)),
                () -> assertEquals("Cuidado prenatal",
                        catalogo(controlador, "descripcion", producto)),
                () -> assertEquals("45000.0",
                        catalogo(controlador, "precioTexto", producto)),
                () -> assertEquals(3,
                        catalogo(controlador, "stock", producto))
        );
    }

    @Test
    void muestraCorrectamenteMedicamentos() throws Exception {
        CatalogoController controlador = new CatalogoController();

        Medicamento medicamento = new Medicamento();
        medicamento.setCodigoInvima("INV-001");
        medicamento.setNombreComercial("Acetaminofen");
        medicamento.setPrincipioActivo("Paracetamol");
        medicamento.setPrecio(new BigDecimal("2500.50"));
        medicamento.setStockTotal(12);

        assertAll(
                () -> assertEquals("INV-001",
                        catalogo(controlador, "codigo", medicamento)),
                () -> assertEquals("Acetaminofen",
                        catalogo(controlador, "nombre", medicamento)),
                () -> assertEquals("Principio activo: Paracetamol",
                        catalogo(controlador, "descripcion", medicamento)),
                () -> assertEquals("2500.50",
                        catalogo(controlador, "precioTexto", medicamento)),
                () -> assertEquals(12,
                        catalogo(controlador, "stock", medicamento))
        );
    }

    @Test
    void medicamentoHistoricoSinPrecioNiPrincipioActivo()
            throws Exception {

        CatalogoController controlador = new CatalogoController();
        Medicamento medicamento = new Medicamento();
        medicamento.setNombreComercial("Historico");

        assertEquals("Sin precio",
                catalogo(controlador, "precioTexto", medicamento));

        assertEquals("",
                catalogo(controlador, "descripcion", medicamento));
    }

    @Test
    void categoriasFarmacologicasMantienenSuRepresentacion() {
        var categoria = new CatalogoController
                .CategoriaMedicamento(5, "Analgesicos");

        assertEquals(5, categoria.id());
        assertEquals("Analgesicos", categoria.nombre());
        assertEquals("Analgesicos (#5)", categoria.toString());
    }

    @Test
    void controlaEstadosSanitariosDeLotes() throws Exception {
        InventarioController controlador = new InventarioController();

        Lote lote = new Lote();
        lote.setEstadoLote("DISPONIBLE");
        lote.setFechaVencimiento(LocalDate.now().plusDays(10));

        assertEquals("DISPONIBLE",
                ejecutar(InventarioController.class, controlador,
                        "estadoMostrado", Lote.class, lote));

        lote.setEstadoLote("RETENIDO");
        assertEquals("RETENIDO",
                ejecutar(InventarioController.class, controlador,
                        "estadoMostrado", Lote.class, lote));

        lote.setEstadoLote("DISPONIBLE");
        lote.setFechaVencimiento(LocalDate.now().minusDays(1));

        assertEquals("DESHABILITADO",
                ejecutar(InventarioController.class, controlador,
                        "estadoMostrado", Lote.class, lote));

        lote.setFechaVencimiento(null);
        lote.setEstadoLote(null);

        assertEquals("SIN ESTADO",
                ejecutar(InventarioController.class, controlador,
                        "estadoMostrado", Lote.class, lote));
    }

    @Test
    void validaRolesAutorizadosParaAjustes() throws Exception {
        assertEquals(true, ejecutar(
                ControladorAjustes.class, null,
                "administrador", String.class, "ADMINISTRADOR"));

        assertEquals(true, ejecutar(
                ControladorAjustes.class, null,
                "administrador", String.class, "super_admin"));

        assertEquals(false, ejecutar(
                ControladorAjustes.class, null,
                "administrador", String.class, "AUXILIAR"));

        assertEquals(false, ejecutar(
                ControladorAjustes.class, null,
                "administrador", String.class, null));
    }

    @Test
    void representaHorariosSmsCorrectamente() throws Exception {
        assertEquals("08:05", ejecutar(
                ControladorPreferenciasSms.class, null,
                "hora", LocalTime.class, LocalTime.of(8, 5)));

        assertEquals("23:59", ejecutar(
                ControladorPreferenciasSms.class, null,
                "hora", LocalTime.class, LocalTime.of(23, 59)));

        assertEquals("", ejecutar(
                ControladorPreferenciasSms.class, null,
                "hora", LocalTime.class, null));
    }

    @Test
    void normalizaTextosNulosDelCatalogo() throws Exception {
        assertEquals("", ejecutar(
                CatalogoController.class, null,
                "nulo", String.class, null));

        assertEquals("Producto", ejecutar(
                CatalogoController.class, null,
                "nulo", String.class, "Producto"));
    }
}
