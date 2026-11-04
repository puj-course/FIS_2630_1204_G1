package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.scene.control.*;
import javafx.scene.shape.Circle;
import org.example.carestock.facade.Inventario;
import org.example.carestock.model.Lote;
import org.example.carestock.service.ServicioAlertasInventario;
import org.example.carestock.service.ResolucionNavegacionAlertas.Destino;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DashboardAsincrono984Test {
    private Object sesionAnterior;

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        CountDownLatch iniciado = new CountDownLatch(1);
        try { Platform.startup(iniciado::countDown); }
        catch (IllegalStateException yaIniciado) { iniciado.countDown(); }
        assertTrue(iniciado.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T enFx(Callable<T> operacion) throws Exception {
        if (Platform.isFxApplicationThread()) return operacion.call();
        FutureTask<T> tarea = new FutureTask<>(operacion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void poner(Object objeto, String nombre, Object dato) throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, dato);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object objeto, String nombre) throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    private static void ejecutar(ControladorInventario controlador) throws Exception {
        Method metodo = ControladorInventario.class.getDeclaredMethod("cargarDatosInicialesAsync");
        metodo.setAccessible(true);
        metodo.invoke(controlador);
    }

    private static SesionUsuario.DatosUsuario usuario(int id) {
        return new SesionUsuario.DatosUsuario(id, "Pruebas QA", "qa@local", "FARMACEUTICO", 4, "Farmacia QA");
    }

    @BeforeEach
    void activarSesion() throws Exception {
        Field campo = SesionUsuario.class.getDeclaredField("actual");
        campo.setAccessible(true);
        sesionAnterior = campo.get(null);
        campo.set(null, usuario(10));
    }

    @AfterEach
    void restaurarSesion() throws Exception {
        Field campo = SesionUsuario.class.getDeclaredField("actual");
        campo.setAccessible(true);
        campo.set(null, sesionAnterior);
    }

    private static ControladorInventario preparar(Inventario fachada) throws Exception {
        return enFx(() -> {
            var controlador = new ControladorInventario();
            poner(controlador, "inventario", fachada);
            poner(controlador, "tablaLotes", new TableView<Lote>());
            poner(controlador, "txtBuscar", new TextField());
            poner(controlador, "cmbCategoria", new ComboBox<String>());
            poner(controlador, "dateInicio", new DatePicker());
            poner(controlador, "dateFin", new DatePicker());
            for (String nombre : List.of("lblTotalLotes", "lblLotesVencidos",
                    "lblLoteSeleccionadoInfo", "lblTotalMedicamentos",
                    "lblTotalAseo", "lblTotalMaternidad", "lblAlertaStock",
                    "lblAlertaVencimiento", "lblContadorNotificaciones")) {
                poner(controlador, nombre, new Label());
            }
            poner(controlador, "badgeNotificaciones", new Circle());
            poner(controlador, "btnAlertas", new Button());
            ComboBox<String> categorias = leer(controlador, "cmbCategoria");
            categorias.getItems().add("Todas");
            categorias.getSelectionModel().selectFirst();
            Method configurar = controlador.getClass().getDeclaredMethod("configurarFiltroBusqueda");
            configurar.setAccessible(true);
            configurar.invoke(controlador);
            return controlador;
        });
    }

    private static void prepararDatos(Inventario fachada, List<Lote> lotes) throws Exception {
        when(fachada.listarCategoriasMedicamento()).thenReturn(Map.of(1, "Analgesicos"));
        when(fachada.listarLotes()).thenReturn(lotes);
        when(fachada.categoriaPorMedicamento()).thenReturn(Map.of(44, "Analgesicos"));
        when(fachada.contarPorCategoria()).thenReturn(Map.of(
                "Medicamento", 8, "Aseo", 4, "Maternidad", 2));
    }

    private static Lote lote(int id, String codigo, boolean vencido) {
        Lote l = new Lote();
        l.setIdLote(id);
        l.setNumeroLote(codigo);
        l.setIdMedicamento(44);
        l.setCantidadActual(5);
        l.setEstadoLote("DISPONIBLE");
        l.setFechaIngreso(LocalDateTime.now().minusDays(3));
        l.setFechaVencimiento(LocalDate.now().plusDays(vencido ? -1 : 30));
        return l;
    }

    private static void esperar(BooleanSupplier verificacion) throws Exception {
        for (int i = 0; i < 400; i++) {
            if (enFx(verificacion::getAsBoolean)) return;
            Thread.sleep(25);
        }
        fail("La tarea de carga no concluyo en el tiempo de prueba");
    }

    @Test
    void cargaDatosAlertasYContadoresSinBloquearHiloFx() throws Exception {
        Inventario fachada = mock(Inventario.class);
        prepararDatos(fachada, List.of(lote(41, "LOT-41", true), lote(42, "LOT-42", false)));
        when(fachada.resumenAlertas()).thenReturn(
                new ServicioAlertasInventario.Resumen(1, 2, 2, 1));
        ControladorInventario c = preparar(fachada);
        enFx(() -> { ejecutar(c); assertEquals("Cargando...", ((Label) leer(c, "lblTotalLotes")).getText()); return null; });
        esperar(() -> !((Label) campoSinExcepcion(c, "lblTotalLotes")).getText().equals("Cargando..."));
        enFx(() -> {
            assertEquals("2", ((Label) leer(c, "lblTotalLotes")).getText());
            assertEquals("1", ((Label) leer(c, "lblLotesVencidos")).getText());
            assertEquals("8", ((Label) leer(c, "lblTotalMedicamentos")).getText());
            assertEquals("4", ((Label) leer(c, "lblTotalAseo")).getText());
            assertEquals("2", ((Label) leer(c, "lblTotalMaternidad")).getText());
            assertEquals("2 productos", ((Label) leer(c, "lblAlertaStock")).getText());
            assertEquals("1 lotes", ((Label) leer(c, "lblAlertaVencimiento")).getText());
            assertEquals("3", ((Label) leer(c, "lblContadorNotificaciones")).getText());
            assertTrue(((Circle) leer(c, "badgeNotificaciones")).isVisible());
            assertTrue(((ComboBox<?>) leer(c, "cmbCategoria")).getItems().contains("Analgesicos"));
            return null;
        });
        verify(fachada).listarLotes();
        verify(fachada).resumenAlertas();
    }

    private static Object campoSinExcepcion(Object obj, String nombre) {
        try { return leer(obj, nombre); }
        catch (Exception e) { throw new AssertionError(e); }
    }

    @Test
    void falloSoloDeAlertasMantieneInventarioDisponible() throws Exception {
        Inventario fachada = mock(Inventario.class);
        prepararDatos(fachada, List.of());
        when(fachada.resumenAlertas()).thenThrow(new IllegalStateException("Vista SQL no disponible"));
        ControladorInventario c = preparar(fachada);
        enFx(() -> { ejecutar(c); return null; });
        esperar(() -> !((Label) campoSinExcepcion(c, "lblTotalLotes")).getText().equals("Cargando..."));
        enFx(() -> {
            assertEquals("0", ((Label) leer(c, "lblTotalLotes")).getText());
            assertEquals("No disponible", ((Label) leer(c, "lblAlertaStock")).getText());
            assertEquals("No disponible", ((Label) leer(c, "lblAlertaVencimiento")).getText());
            assertFalse(((Circle) leer(c, "badgeNotificaciones")).isVisible());
            return null;
        });
    }

    @Test
    void errorDeInventarioSeMuestraSinBloquearInterfaz() throws Exception {
        Inventario fachada = mock(Inventario.class);
        when(fachada.listarCategoriasMedicamento()).thenReturn(Map.of());
        when(fachada.listarLotes()).thenThrow(new IllegalStateException("Base de datos sin conexion"));
        ControladorInventario c = preparar(fachada);
        enFx(() -> { ejecutar(c); return null; });
        esperar(() -> !((Boolean) campoSinExcepcion(c, "cargaInicialEnCurso")));
        enFx(() -> {
            assertEquals("No disponible", ((Label) leer(c, "lblTotalLotes")).getText());
            assertTrue(((Label) leer(c, "lblLoteSeleccionadoInfo")).getText().contains("Error al cargar"));
            return null;
        });
    }

    @Test
    void lotePendienteSeSeleccionaSoloTrasLaCarga() throws Exception {
        Inventario fachada = mock(Inventario.class);
        CountDownLatch inicio = new CountDownLatch(1);
        CountDownLatch continuar = new CountDownLatch(1);
        when(fachada.listarCategoriasMedicamento()).thenAnswer(inv -> {
            inicio.countDown();
            if (!continuar.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Timeout en prueba");
            return Map.of(1, "Analgesicos");
        });
        when(fachada.listarLotes()).thenReturn(List.of(lote(41, "LOT-41", false)));
        when(fachada.categoriaPorMedicamento()).thenReturn(Map.of(44, "Analgesicos"));
        when(fachada.contarPorCategoria()).thenReturn(Map.of());
        when(fachada.resumenAlertas()).thenReturn(null);
        ControladorInventario c = preparar(fachada);
        try {
            enFx(() -> { ejecutar(c); return null; });
            assertTrue(inicio.await(5, TimeUnit.SECONDS));
            enFx(() -> {
                c.abrirDesdeAlerta(new Destino(TipoAlerta.VENCIMIENTO, Categoria.MEDICAMENTO,
                        41, "M-44", "Med", "LOT-41", 4));
                assertEquals("Cargando lote solicitado...", ((Label) leer(c, "lblLoteSeleccionadoInfo")).getText());
                return null;
            });
        } finally {
            continuar.countDown();
        }
        esperar(() -> !((Boolean) campoSinExcepcion(c, "cargaInicialEnCurso")));
        enFx(() -> {
            TableView<?> tabla = leer(c, "tablaLotes");
            assertNotNull(tabla.getSelectionModel().getSelectedItem());
            assertNull(leer(c, "destinoLotePendiente"));
            return null;
        });
    }

    @Test
    void cambioDeSesionDescartaResultadoDeConsultaEnCurso() throws Exception {
        Inventario fachada = mock(Inventario.class);
        CountDownLatch inicio = new CountDownLatch(1);
        CountDownLatch continuar = new CountDownLatch(1);
        when(fachada.listarCategoriasMedicamento()).thenAnswer(inv -> {
            inicio.countDown();
            if (!continuar.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Timeout en prueba");
            return Map.of(1, "Analgesicos");
        });
        when(fachada.listarLotes()).thenReturn(List.of());
        when(fachada.categoriaPorMedicamento()).thenReturn(Map.of());
        when(fachada.contarPorCategoria()).thenReturn(Map.of());
        ControladorInventario c = preparar(fachada);
        try {
            enFx(() -> { ejecutar(c); return null; });
            assertTrue(inicio.await(5, TimeUnit.SECONDS));
            Field campo = SesionUsuario.class.getDeclaredField("actual");
            campo.setAccessible(true);
            campo.set(null, usuario(11));
        } finally {
            continuar.countDown();
        }
        esperar(() -> !((Boolean) campoSinExcepcion(c, "cargaInicialEnCurso")));
        enFx(() -> {
            assertEquals("Cargando...", ((Label) leer(c, "lblTotalLotes")).getText());
            return null;
        });
    }
}
