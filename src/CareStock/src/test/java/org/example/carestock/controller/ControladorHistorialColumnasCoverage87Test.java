package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import org.example.carestock.DataTransferObject.MovimientoHistorial;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

class ControladorHistorialColumnasCoverage87Test {

    @BeforeAll
    static void iniciarFx() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);
        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException iniciado) {
            listo.countDown();
        }
        assertTrue(listo.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T fx(Callable<T> accion) throws Exception {
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void poner(Object obj, String nombre, Object valor)
            throws Exception {
        Field f = obj.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        f.set(obj, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object obj, String nombre)
            throws Exception {
        Field f = obj.getClass().getDeclaredField(nombre);
        f.setAccessible(true);
        return (T) f.get(obj);
    }

    private static void llamar(Object obj, String nombre)
            throws Exception {
        Method m = obj.getClass().getDeclaredMethod(nombre);
        m.setAccessible(true);
        m.invoke(obj);
    }

    private static OffsetDateTime fecha() {
        return LocalDateTime.of(2026, 11, 8, 14, 30)
            .atZone(ZoneId.systemDefault())
            .toOffsetDateTime();
    }

    private static MovimientoHistorial movimiento(
            String producto, String referencia,
            String categoria, String tipo,
            String usuario, String motivo,
            OffsetDateTime fecha) {

        return new MovimientoHistorial(
            "LOG_MOVIMIENTOS", 15L,
            categoria, tipo, 12,
            producto, referencia, 5L,
            motivo, 10, 5, -5L,
            8, 7, usuario, fecha);
    }

    private static ControladorHistorialMovimientos columnas()
            throws Exception {

        var controlador = new ControladorHistorialMovimientos();

        poner(controlador, "colFecha",
            new TableColumn<MovimientoHistorial, OffsetDateTime>());
        poner(controlador, "colProducto",
            new TableColumn<MovimientoHistorial, String>());
        poner(controlador, "colCategoria",
            new TableColumn<MovimientoHistorial, String>());
        poner(controlador, "colTipo",
            new TableColumn<MovimientoHistorial, String>());
        poner(controlador, "colCantidad",
            new TableColumn<MovimientoHistorial, Long>());
        poner(controlador, "colUsuario",
            new TableColumn<MovimientoHistorial, String>());
        poner(controlador, "colMotivo",
            new TableColumn<MovimientoHistorial, String>());

        TableView<MovimientoHistorial> tabla = new TableView<>();
        for (String nombre : new String[]{
                "colFecha", "colProducto", "colCategoria",
                "colTipo", "colCantidad", "colUsuario", "colMotivo"}) {
            TableColumn<MovimientoHistorial, ?> columna = leer(controlador, nombre);
            tabla.getColumns().add(columna);
        }
        poner(controlador, "tablaHistorial", tabla);

        llamar(controlador, "configurarColumnas");
        return controlador;
    }

    @Test
    void columnasMuestranTodosLosDatosDelMovimiento()
            throws Exception {

        fx(() -> {
            var c = columnas();
            var m = movimiento(
                "Jabon", "ASE-12", "ASEO",
                "SALIDA", "Ana", "Dispensacion", fecha());

            TableColumn<MovimientoHistorial, String> producto =
                leer(c, "colProducto");
            TableColumn<MovimientoHistorial, String> categoria =
                leer(c, "colCategoria");
            TableColumn<MovimientoHistorial, String> tipo =
                leer(c, "colTipo");
            TableColumn<MovimientoHistorial, Long> cantidad =
                leer(c, "colCantidad");
            TableColumn<MovimientoHistorial, String> usuario =
                leer(c, "colUsuario");
            TableColumn<MovimientoHistorial, String> motivo =
                leer(c, "colMotivo");
            TableColumn<MovimientoHistorial, OffsetDateTime> f =
                leer(c, "colFecha");

            assertAll(
                () -> assertEquals("Jabon (ASE-12)",
                    producto.getCellObservableValue(m).getValue()),
                () -> assertEquals("ASEO",
                    categoria.getCellObservableValue(m).getValue()),
                () -> assertEquals("SALIDA",
                    tipo.getCellObservableValue(m).getValue()),
                () -> assertEquals(5L,
                    cantidad.getCellObservableValue(m).getValue()),
                () -> assertEquals("Ana",
                    usuario.getCellObservableValue(m).getValue()),
                () -> assertEquals("Dispensacion",
                    motivo.getCellObservableValue(m).getValue()),
                () -> assertEquals(fecha(),
                    f.getCellObservableValue(m).getValue())
            );

            return null;
        });
    }

    @Test
    void productoSinReferenciaNoMuestraParentesis()
            throws Exception {

        fx(() -> {
            var c = columnas();
            TableColumn<MovimientoHistorial, String> columna =
                leer(c, "colProducto");

            for (String referencia : new String[]{null, "", "  "}) {
                var m = movimiento("Jabon", referencia,
                    "ASEO", "ENTRADA", "QA", "Conteo", fecha());

                assertEquals("Jabon",
                    columna.getCellObservableValue(m).getValue());
            }

            return null;
        });
    }

    @Test
    void camposNulosSePresentanComoGuion()
            throws Exception {

        fx(() -> {
            var c = columnas();
            var m = movimiento(
                null, null, null, null, null, null, fecha());

            for (String campo : new String[]{
                "colProducto", "colCategoria", "colTipo",
                "colUsuario", "colMotivo"
            }) {
                TableColumn<MovimientoHistorial, String> col =
                    leer(c, campo);

                assertEquals("-",
                    col.getCellObservableValue(m).getValue(),
                    campo);
            }
            return null;
        });
    }

    @Test
    void celdasDeTextoRespetanCadenasNoVacias()
            throws Exception {

        fx(() -> {
            var c = columnas();
            var m = movimiento(
                "Producto", null, "MATERNIDAD",
                "AJUSTE", "Farmaceutico", "Inventario", fecha());

            TableColumn<MovimientoHistorial, String> col =
                leer(c, "colMotivo");

            assertEquals("Inventario",
                col.getCellObservableValue(m).getValue());
            return null;
        });
    }

    @Test
    void celdaDeFechaFormateaHoraLocal()
            throws Exception {

        fx(() -> {
            var c = columnas();
            TableColumn<MovimientoHistorial, OffsetDateTime> columna =
                leer(c, "colFecha");

            TableCell<MovimientoHistorial, OffsetDateTime> celda =
                columna.getCellFactory().call(columna);

            Method actualizar = celda.getClass()
                .getDeclaredMethod("updateItem",
                    OffsetDateTime.class, boolean.class);
            actualizar.setAccessible(true);

            actualizar.invoke(celda, fecha(), false);

            assertEquals("08/11/2026 14:30", celda.getText());

            actualizar.invoke(celda, null, true);
            assertNull(celda.getText());

            return null;
        });
    }

    @Test
    void celdaDeMotivoMuestraTooltipYLoLimpia()
            throws Exception {

        fx(() -> {
            var c = columnas();
            TableColumn<MovimientoHistorial, String> columna =
                leer(c, "colMotivo");

            TableCell<MovimientoHistorial, String> celda =
                columna.getCellFactory().call(columna);

            Method actualizar = celda.getClass()
                .getDeclaredMethod("updateItem",
                    String.class, boolean.class);
            actualizar.setAccessible(true);

            actualizar.invoke(celda, "Ajuste por conteo", false);

            assertEquals("Ajuste por conteo", celda.getText());
            assertEquals("Ajuste por conteo",
                celda.getTooltip().getText());

            actualizar.invoke(celda, null, true);

            assertNull(celda.getText());
            assertNull(celda.getTooltip());

            return null;
        });
    }

    @Test
    void filtroRechazaMovimientoNulo() {
        assertFalse(ControladorHistorialMovimientos.coincideFiltros(
            null, "Todos", "Todos", null, null));
    }

    @Test
    void filtroAdmiteCategoriaYTipoSinDistinguirMayusculas() {
        var m = movimiento(
            "Jabon", null, "ASEO", "SALIDA",
            "QA", "Conteo", fecha());

        assertTrue(ControladorHistorialMovimientos.coincideFiltros(
            m, "aseo", "salida", null, null));
    }

    @Test
    void filtroSinFechaAdmiteRegistroSinFecha() {
        var m = movimiento(
            "Jabon", null, "ASEO", "ENTRADA",
            "QA", "Conteo", null);

        assertTrue(ControladorHistorialMovimientos.coincideFiltros(
            m, null, null, null, null));
    }

    @Test
    void filtroConFechaRechazaRegistroSinFecha() {
        var m = movimiento(
            "Jabon", null, "ASEO", "ENTRADA",
            "QA", "Conteo", null);

        assertFalse(ControladorHistorialMovimientos.coincideFiltros(
            m, "Todos", "Todos",
            LocalDate.of(2026, 11, 1), null));
    }

    @Test
    void fechaInicialYFinalSonInclusivas() {
        var m = movimiento(
            "Jabon", null, "ASEO", "SALIDA",
            "QA", "Conteo", fecha());

        LocalDate dia = LocalDate.of(2026, 11, 8);

        assertTrue(ControladorHistorialMovimientos.coincideFiltros(
            m, "Todos", "Todos", dia, dia));
    }

    @Test
    void rechazaFechaAnteriorYPosteriorAlRango() {
        var m = movimiento(
            "Jabon", null, "ASEO", "SALIDA",
            "QA", "Conteo", fecha());

        assertFalse(ControladorHistorialMovimientos.coincideFiltros(
            m, "Todos", "Todos",
            LocalDate.of(2026, 11, 9), null));

        assertFalse(ControladorHistorialMovimientos.coincideFiltros(
            m, "Todos", "Todos",
            null, LocalDate.of(2026, 11, 7)));
    }

    @Test
    void rolesConEspaciosSonAceptados() {
        assertTrue(ControladorHistorialMovimientos
            .rolPermitido(" administrador "));

        assertTrue(ControladorHistorialMovimientos
            .rolPermitido(" farmaceutico "));
    }
}
