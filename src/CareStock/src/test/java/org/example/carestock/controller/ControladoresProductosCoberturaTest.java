package org.example.carestock.controller;

import org.example.carestock.model.Aseo;
import org.example.carestock.model.Maternidad;
import org.example.carestock.model.Medicamento;
import org.example.carestock.model.Lote;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladoresProductosCoberturaTest {

    private final ControladorCatalogo catalogo =
            new ControladorCatalogo();

    private Object consultar(String metodo, Object producto)
            throws Exception {
        Method m = ControladorCatalogo.class
                .getDeclaredMethod(metodo, Object.class);
        m.setAccessible(true);
        return m.invoke(catalogo, producto);
    }

    private Object consultarEstatico(String metodo, Object producto)
            throws Exception {
        Method m = ControladorCatalogo.class
                .getDeclaredMethod(metodo, Object.class);
        m.setAccessible(true);
        return m.invoke(null, producto);
    }

    @Test
    void presentaProductosDeAseo() throws Exception {
        Aseo aseo = mock(Aseo.class);

        when(aseo.getCodigo()).thenReturn("ASE-001");
        when(aseo.getNombre()).thenReturn("Jabon");
        when(aseo.getDescripcion()).thenReturn("Liquido");
        when(aseo.getPrecio()).thenReturn(12500.0);
        when(aseo.getStock()).thenReturn(8);
        when(aseo.getStockMinimo()).thenReturn(5);

        assertAll(
                () -> assertEquals("ASE-001",
                        consultar("codigo", aseo)),
                () -> assertEquals("Jabon",
                        consultar("nombre", aseo)),
                () -> assertEquals("Liquido",
                        consultar("descripcion", aseo)),
                () -> assertEquals("12500.0",
                        consultar("precioTexto", aseo)),
                () -> assertEquals(8,
                        consultar("stock", aseo)),
                () -> assertEquals(5,
                        consultarEstatico("stockMinimoDe", aseo))
        );
    }

    @Test
    void presentaProductosDeMaternidad() throws Exception {
        Maternidad maternidad = mock(Maternidad.class);

        when(maternidad.getCodigo()).thenReturn("MAT-001");
        when(maternidad.getNombre()).thenReturn("Kit maternal");
        when(maternidad.getDescripcion()).thenReturn("Prenatal");
        when(maternidad.getPrecio()).thenReturn(45000.0);
        when(maternidad.getStock()).thenReturn(3);
        when(maternidad.getStockMinimo()).thenReturn(2);

        assertAll(
                () -> assertEquals("MAT-001",
                        consultar("codigo", maternidad)),
                () -> assertEquals("Kit maternal",
                        consultar("nombre", maternidad)),
                () -> assertEquals("Prenatal",
                        consultar("descripcion", maternidad)),
                () -> assertEquals("45000.0",
                        consultar("precioTexto", maternidad)),
                () -> assertEquals(3,
                        consultar("stock", maternidad)),
                () -> assertEquals(2,
                        consultarEstatico("stockMinimoDe", maternidad))
        );
    }

    @Test
    void presentaMedicamentos() throws Exception {
        Medicamento medicamento = mock(Medicamento.class);

        when(medicamento.getCodigoInvima()).thenReturn("INV-001");
        when(medicamento.getNombreComercial())
                .thenReturn("Acetaminofen");
        when(medicamento.getPrincipioActivo())
                .thenReturn("Paracetamol");
        when(medicamento.getPrecio())
                .thenReturn(new BigDecimal("2500.50"));
        when(medicamento.getStockTotal()).thenReturn(12);
        when(medicamento.getStockMinimo()).thenReturn(4);

        assertAll(
                () -> assertEquals("INV-001",
                        consultar("codigo", medicamento)),
                () -> assertEquals("Acetaminofen",
                        consultar("nombre", medicamento)),
                () -> assertEquals("Principio activo: Paracetamol",
                        consultar("descripcion", medicamento)),
                () -> assertEquals("2500.50",
                        consultar("precioTexto", medicamento)),
                () -> assertEquals(12,
                        consultar("stock", medicamento)),
                () -> assertEquals(4,
                        consultarEstatico("stockMinimoDe", medicamento))
        );
    }

    @Test
    void medicamentoHistoricoSinPrecioNiPrincipioActivo()
            throws Exception {
        Medicamento medicamento = mock(Medicamento.class);

        assertEquals("Sin precio",
                consultar("precioTexto", medicamento));

        assertEquals("",
                consultar("descripcion", medicamento));
    }

    @Test
    void productoDesconocidoNoTieneStockMinimo()
            throws Exception {
        assertNull(consultarEstatico(
                "stockMinimoDe", "Otro producto"));
    }

    @Test
    void validaFXMLIncompleto() {
        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                catalogo::initialize);

        assertTrue(error.getMessage().contains("FXML"));
    }

    @Test
    void inventarioRechazaSesionInactiva() {
        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

            ControladorInventario controlador =
                    new ControladorInventario();

            assertThrows(IllegalStateException.class,
                    controlador::initialize);
        }
    }

    private String estado(Lote lote) throws Exception {
        ControladorInventario controlador =
                new ControladorInventario();

        Method metodo = ControladorInventario.class
                .getDeclaredMethod("estadoMostrado", Lote.class);
        metodo.setAccessible(true);

        return (String) metodo.invoke(controlador, lote);
    }

    @Test
    void loteRetenidoConservaSuEstado() throws Exception {
        Lote lote = mock(Lote.class);

        when(lote.getEstadoLote()).thenReturn("RETENIDO");

        assertEquals("RETENIDO", estado(lote));
    }

    @Test
    void loteVencidoSeDeshabilita() throws Exception {
        Lote lote = mock(Lote.class);

        when(lote.getEstadoLote()).thenReturn("DISPONIBLE");
        when(lote.getFechaVencimiento())
                .thenReturn(LocalDate.now().minusDays(1));

        assertEquals("DESHABILITADO", estado(lote));
    }

    @Test
    void loteDisponibleConservaSuEstado() throws Exception {
        Lote lote = mock(Lote.class);

        when(lote.getEstadoLote()).thenReturn("DISPONIBLE");
        when(lote.getFechaVencimiento())
                .thenReturn(LocalDate.now().plusDays(30));

        assertEquals("DISPONIBLE", estado(lote));
    }

    @Test
    void loteSinEstadoTieneValorPorDefecto()
            throws Exception {
        Lote lote = mock(Lote.class);

        assertEquals("SIN ESTADO", estado(lote));
    }
}
