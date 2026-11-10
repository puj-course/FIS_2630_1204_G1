package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.example.carestock.DataTransferObject.FiltroDisponibilidad;
import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.service.ResolucionNavegacionAlertas.Destino;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

/** Pruebas del filtro de disponibilidad de la consulta de inventario (HU-151). */
class ConsultaInventarioDisponibilidadTest {

    private static final SesionUsuario.DatosUsuario ADMIN =
            new SesionUsuario.DatosUsuario(
                    7, "QA", "qa@test.local",
                    "ADMINISTRADOR", 8, "Farmacia QA");

    @BeforeAll
    static void iniciarFx() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);
        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException yaIniciado) {
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

    private static void poner(Object objeto, String campo, Object valor) throws Exception {
        Field f = objeto.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        f.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object objeto, String campo) throws Exception {
        Field f = objeto.getClass().getDeclaredField(campo);
        f.setAccessible(true);
        return (T) f.get(objeto);
    }

    private static void invocar(Object objeto, String nombre) throws Exception {
        Method m = objeto.getClass().getDeclaredMethod(nombre);
        m.setAccessible(true);
        m.invoke(objeto);
    }

    private static ProductoInventario producto(int id, String categoria, int stock) {
        return new ProductoInventario(
                id, categoria, "COD-" + id, "Producto " + id, "Unidad", stock, "ACTIVO");
    }

    private static ConsultaInventarioController vista(boolean conDisponibilidad) throws Exception {
        ConsultaInventarioController c = new ConsultaInventarioController();

        poner(c, "cmbCategoria", new ComboBox<String>());
        if (conDisponibilidad) {
            poner(c, "cmbDisponibilidad", new ComboBox<String>());
        }
        poner(c, "txtBuscar", new TextField());
        poner(c, "btnActualizar", new Button());
        poner(c, "btnRegistrar", new Button());
        poner(c, "btnEditar", new Button());
        poner(c, "lblFarmacia", new Label());
        poner(c, "lblEstado", new Label());
        poner(c, "lblTotal", new Label());
        poner(c, "sesionVista", ADMIN);

        TableView<ProductoInventario> tabla = new TableView<>();
        poner(c, "tablaInventario", tabla);

        for (String campo : new String[]{
                "colCodigo", "colNombre", "colCategoria",
                "colPresentacion", "colExistencia", "colEstado"}) {
            TableColumn<ProductoInventario, String> columna = new TableColumn<>(campo);
            poner(c, campo, columna);
            tabla.getColumns().add(columna);
        }

        invocar(c, "configurarColumnas");
        invocar(c, "configurarTabla");
        invocar(c, "configurarFiltros");
        return c;
    }

    private static void consultar(ConsultaInventarioController c, ProductoInventario... productos)
            throws Exception {
        ObservableList<ProductoInventario> lista = leer(c, "productos");
        lista.setAll(productos);
        poner(c, "consultaExitosa", true);
        invocar(c, "aplicarFiltros");
    }

    private static TableView<ProductoInventario> tabla(ConsultaInventarioController c)
            throws Exception {
        return leer(c, "tablaInventario");
    }

    private static ComboBox<String> disponibilidad(ConsultaInventarioController c)
            throws Exception {
        return leer(c, "cmbDisponibilidad");
    }

    @Test
    void elComboIniciaEnTodosConLasTresOpciones() throws Exception {
        fx(() -> {
            ConsultaInventarioController c = vista(true);

            assertEquals(FiltroDisponibilidad.opciones(), disponibilidad(c).getItems());
            assertEquals(FiltroDisponibilidad.TODOS, disponibilidad(c).getValue());
            return null;
        });
    }

    @Test
    void todosMuestraTodosLosProductos() throws Exception {
        fx(() -> {
            ConsultaInventarioController c = vista(true);
            consultar(c, producto(1, "Aseo", 5), producto(2, "Aseo", 0), producto(3, "Aseo", 2));

            assertEquals(3, tabla(c).getItems().size());
            return null;
        });
    }

    @Test
    void conExistenciasEscondeLosAgotados() throws Exception {
        fx(() -> {
            ConsultaInventarioController c = vista(true);
            consultar(c, producto(1, "Aseo", 5), producto(2, "Aseo", 0), producto(3, "Aseo", 2));

            disponibilidad(c).setValue(FiltroDisponibilidad.CON_EXISTENCIAS);

            assertEquals(2, tabla(c).getItems().size());
            assertTrue(tabla(c).getItems().stream().allMatch(p -> p.existencia() > 0));
            return null;
        });
    }

    @Test
    void agotadosMuestraSoloLosQueNoTienenUnidades() throws Exception {
        fx(() -> {
            ConsultaInventarioController c = vista(true);
            consultar(c, producto(1, "Aseo", 5), producto(2, "Aseo", 0), producto(3, "Aseo", 2));

            disponibilidad(c).setValue(FiltroDisponibilidad.AGOTADOS);

            assertEquals(1, tabla(c).getItems().size());
            assertEquals(2, tabla(c).getItems().get(0).id());
            Label total = leer(c, "lblTotal");
            assertEquals("Productos encontrados: 1", total.getText());
            return null;
        });
    }

    @Test
    void seCombinaConLaCategoria() throws Exception {
        fx(() -> {
            ConsultaInventarioController c = vista(true);
            consultar(c,
                    producto(1, "Aseo", 0),
                    producto(2, "Maternidad", 0),
                    producto(3, "Aseo", 4));

            ComboBox<String> categoria = leer(c, "cmbCategoria");
            categoria.setValue("Aseo");
            disponibilidad(c).setValue(FiltroDisponibilidad.AGOTADOS);

            assertEquals(1, tabla(c).getItems().size());
            assertEquals(1, tabla(c).getItems().get(0).id());
            return null;
        });
    }

    @Test
    void sinElComboElFiltroSeTrataComoTodos() throws Exception {
        fx(() -> {
            ConsultaInventarioController c = vista(false);
            consultar(c, producto(1, "Aseo", 0), producto(2, "Aseo", 3));

            assertEquals(2, tabla(c).getItems().size());
            return null;
        });
    }

    @Test
    void abrirUnProductoDesdeUnaAlertaRestableceElFiltroATodos() throws Exception {
        fx(() -> {
            ConsultaInventarioController c = vista(true);
            consultar(c, producto(1, "Aseo", 0), producto(2, "Aseo", 7));
            disponibilidad(c).setValue(FiltroDisponibilidad.AGOTADOS);

            Destino destino = new Destino(
                    TipoAlerta.STOCK_MINIMO, Categoria.ASEO,
                    2, "COD-2", "Producto 2", null, 8);

            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::actual).thenReturn(ADMIN);

                c.abrirDesdeAlerta(destino);

                assertEquals(FiltroDisponibilidad.TODOS, disponibilidad(c).getValue());
                assertEquals(2, tabla(c).getSelectionModel().getSelectedItem().id());
            }
            return null;
        });
    }

    @Test
    void sinCoincidenciasLaListaQuedaVaciaYSeAvisa() throws Exception {
        fx(() -> {
            ConsultaInventarioController c = vista(true);
            consultar(c, producto(1, "Aseo", 5));

            disponibilidad(c).setValue(FiltroDisponibilidad.AGOTADOS);

            assertTrue(tabla(c).getItems().isEmpty());
            Label estado = leer(c, "lblEstado");
            assertTrue(estado.getText().contains("No hay coincidencias"));
            return null;
        });
    }
}
