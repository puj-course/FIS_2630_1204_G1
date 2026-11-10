package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.*;
import org.example.carestock.DataTransferObject.RegistroCatalogo;
import org.example.carestock.facade.Inventario;
import org.example.carestock.model.*;
import org.example.carestock.session.SesionUsuario;
import org.example.carestock.utils.FechaNegocio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorAjustesSonarFase1Test {
    private static final SesionUsuario.DatosUsuario ADMIN =
            new SesionUsuario.DatosUsuario(7, "QA", "qa@test.local", "ADMINISTRADOR", 8, "Farmacia");

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        CountDownLatch preparado = new CountDownLatch(1);
        try {
            Platform.startup(preparado::countDown);
        } catch (IllegalStateException yaIniciado) {
            preparado.countDown();
        }
        assertTrue(preparado.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T fx(Callable<T> accion) throws Exception {
        if (Platform.isFxApplicationThread()) return accion.call();
        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);
        return tarea.get(20, TimeUnit.SECONDS);
    }

    private static void asignar(Object objeto, String nombre, Object valor) throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        campo.set(objeto, valor);
    }

    @SuppressWarnings("unchecked")
    private static <T> T leer(Object objeto, String nombre) throws Exception {
        Field campo = objeto.getClass().getDeclaredField(nombre);
        campo.setAccessible(true);
        return (T) campo.get(objeto);
    }

    private static void ejecutar(Object objeto, String nombre) throws Exception {
        Method metodo = objeto.getClass().getDeclaredMethod(nombre);
        metodo.setAccessible(true);
        metodo.invoke(objeto);
    }

    private static ControladorAjustes formulario() throws Exception {
        ControladorAjustes c = new ControladorAjustes();
        asignar(c, "idUsuario", 7);
        asignar(c, "idFarmacia", 8);
        asignar(c, "lblEstado", new Label());
        asignar(c, "lblExistencias", new Label());
        asignar(c, "cmbCategoria", new ComboBox<AjusteExistencias.Categoria>());
        asignar(c, "cmbReferencia", new ComboBox<>());
        asignar(c, "txtNuevaCantidad", new TextField());
        asignar(c, "txtMotivo", new TextArea());
        asignar(c, "btnActualizar", new Button());
        asignar(c, "btnAplicar", new Button());
        return c;
    }

    @Test
    void noAccedeAjusteSinSesion() throws Exception {
        fx(() -> {
            ControladorAjustes c = formulario();
            try (var sesion = mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> alertas = mockConstruction(Alert.class)) {
                sesion.when(SesionUsuario::estaActiva).thenReturn(false);
                ejecutar(c, "onAplicar");
                assertEquals("La sesion cambio. Vuelve a iniciar sesion.",
                        ((Label) leer(c, "lblEstado")).getText());
                assertEquals(1, alertas.constructed().size());
            }
            return null;
        });
    }

    @Test
    void solicitaProductoAntesDeAplicarAjuste() throws Exception {
        fx(() -> {
            ControladorAjustes c = formulario();
            try (var sesion = mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> alertas = mockConstruction(Alert.class)) {
                sesion.when(SesionUsuario::estaActiva).thenReturn(true);
                sesion.when(SesionUsuario::actual).thenReturn(ADMIN);
                ejecutar(c, "onAplicar");
                assertEquals("Selecciona un producto o lote.",
                        ((Label) leer(c, "lblEstado")).getText());
                assertEquals(1, alertas.constructed().size());
            }
            return null;
        });
    }

    @Test
    void consultaFiltraEstadosYVencimientosSinBD() throws Exception {
        fx(() -> {
            ControladorAjustes c = formulario();
            AtomicReference<Task<?>> capturada = new AtomicReference<>();
            asignar(c, "ejecutorConsulta", (Consumer<Task<?>>) capturada::set);
            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva).thenReturn(true);
                sesion.when(SesionUsuario::actual).thenReturn(ADMIN);
                Usuario usuario = new Usuario();
                usuario.setIdUsuario(7);
                sesion.when(SesionUsuario::usuarioParaFachada).thenReturn(usuario);
                ejecutar(c, "cargarReferencias");
                assertNotNull(capturada.get());
                assertTrue(((Button) leer(c, "btnAplicar")).isDisabled());

                Aseo aseo = new Aseo();
                aseo.setNombre("Jabon"); aseo.setCodigo("ASE-1"); aseo.setStock(5);
                Maternidad maternidad = new Maternidad();
                maternidad.setNombre("Pañales"); maternidad.setCodigo("MAT-1");
                maternidad.setStock(9);
                Medicamento med = new Medicamento();
                med.setNombreComercial("Analgesico");

                Lote vigente = new Lote("L-V", 31, 4, FechaNegocio.hoy().plusDays(4),
                        1, "DISPONIBLE", 7);
                vigente.setIdLote(91);
                Lote agotado = new Lote("L-A", 31, 0, FechaNegocio.hoy().plusDays(8),
                        1, "AGOTADO", 7);
                agotado.setIdLote(92);
                Lote vencido = new Lote("L-X", 31, 4, FechaNegocio.hoy().minusDays(1),
                        1, "DISPONIBLE", 7);
                Lote retenido = new Lote("L-R", 31, 4, FechaNegocio.hoy().plusDays(1),
                        1, "RETENIDO", 7);
                Lote sinFecha = new Lote("L-S", 31, 4, null, 1, "DISPONIBLE", 7);
                Lote ajeno = new Lote("L-J", 99, 4, FechaNegocio.hoy().plusDays(1),
                        1, "DISPONIBLE", 7);

                try (MockedConstruction<Inventario> construidos = mockConstruction(
                        Inventario.class, (inventario, contexto) -> {
                            when(inventario.listarAseo()).thenReturn(List.of(
                                    new RegistroCatalogo<>(11, aseo, "ACTIVO"),
                                    new RegistroCatalogo<>(12, aseo, "INACTIVO")));
                            when(inventario.listarMaternidad()).thenReturn(List.of(
                                    new RegistroCatalogo<>(21, maternidad, "ACTIVO"),
                                    new RegistroCatalogo<>(22, maternidad, "INACTIVO")));
                            when(inventario.listarMedicamentos()).thenReturn(List.of(
                                    new RegistroCatalogo<>(31, med, "ACTIVO"),
                                    new RegistroCatalogo<>(32, med, "INACTIVO")));
                            when(inventario.listarLotes()).thenReturn(List.of(
                                    vigente, agotado, vencido, retenido, sinFecha, ajeno));
                        })) {
                    Method call = capturada.get().getClass().getDeclaredMethod("call");
                    call.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<Object> opciones = (List<Object>) call.invoke(capturada.get());
                    assertEquals(4, opciones.size());
                    List<String> textos = opciones.stream().map(Object::toString).toList();
                    assertTrue(textos.stream().anyMatch(t -> t.contains("Jabon")));
                    assertTrue(textos.stream().anyMatch(t -> t.contains("Pañales")));
                    assertTrue(textos.stream().anyMatch(t -> t.contains("L-V")));
                    assertTrue(textos.stream().anyMatch(t -> t.contains("L-A")));
                    assertTrue(textos.stream().noneMatch(t -> t.contains("L-X")));
                    assertEquals(1, construidos.constructed().size());
                }
            }
            return null;
        });
    }
}
