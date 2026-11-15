package org.example.carestock;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedConstruction;

import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CareStockAppAccesosCoverage87Test {

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
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

    private static CareStockApp aplicacion() {
        CareStockApp app = spy(new CareStockApp());
        doNothing().when(app).mostrarLogin();
        doNothing().when(app).transicionarEscena(
            anyString(), anyDouble(), anyDouble());
        return app;
    }

    private static <T> T conSesion(
            String rol, Integer farmacia,
            Callable<T> accion) throws Exception {

        try (var sesion = mockStatic(SesionUsuario.class)) {
            var datos = new SesionUsuario.DatosUsuario(
                7, "Usuario QA", "qa@test.local",
                rol, farmacia, "Farmacia QA");

            sesion.when(SesionUsuario::estaActiva)
                .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                .thenReturn(datos);
            sesion.when(SesionUsuario::esAdministrador)
                .thenReturn(datos.esAdministrador());

            return accion.call();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "dashboard", "consulta", "catalogo",
        "movimientos", "ajustes", "historial",
        "reposicion", "alertas", "detalle"
    })
    void sinSesionRedirigeAlLogin(String ruta)
            throws Exception {

        fx(() -> {
            try (var sesion = mockStatic(SesionUsuario.class)) {
                sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

                CareStockApp app = aplicacion();

                switch (ruta) {
                    case "dashboard" -> app.mostrarDashboard();
                    case "consulta" -> app.mostrarConsultaUnificada();
                    case "catalogo" -> app.mostrarCatalogo();
                    case "movimientos" -> app.mostrarFormularioMovimientos();
                    case "ajustes" -> app.mostrarFormularioAjustes();
                    case "historial" -> app.mostrarHistorialMovimientos();
                    case "reposicion" -> app.mostrarReposicion();
                    case "alertas" -> app.mostrarAlertas();
                    case "detalle" -> app.mostrarDetalleAlerta(null, null);
                    default -> fail("Ruta desconocida");
                }

                verify(app).mostrarLogin();
                verify(app, never()).transicionarEscena(
                    anyString(), anyDouble(), anyDouble());
            }
            return null;
        });
    }

    @ParameterizedTest
    @CsvSource(value = {
        "AUXILIAR,8",
        "ADMINISTRADOR,0",
        "FARMACEUTICO,-1",
        "FARMACEUTICO,NULL"
    }, nullValues = "NULL")
    void movimientosRechazaRolesOFarmaciasInvalidas(
            String rol, Integer farmacia) throws Exception {

        fx(() -> conSesion(rol, farmacia, () -> {
            CareStockApp app = aplicacion();

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {

                app.mostrarFormularioMovimientos();

                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0)).showAndWait();
                verify(app, never()).transicionarEscena(
                    anyString(), anyDouble(), anyDouble());
            }
            return null;
        }));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ADMINISTRADOR", "FARMACEUTICO", "SUPER_ADMIN"
    })
    void movimientosPermiteRolesGestores(String rol)
            throws Exception {

        fx(() -> conSesion(rol, 8, () -> {
            CareStockApp app = aplicacion();
            app.mostrarFormularioMovimientos();

            verify(app).transicionarEscena(
                "/org/example/carestock/movimientos-view.fxml",
                1100, 750);
            return null;
        }));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "AUXILIAR,8",
        "FARMACEUTICO,8",
        "ADMINISTRADOR,NULL"
    }, nullValues = "NULL")
    void ajustesRechazaRolesOFarmaciasInvalidas(
            String rol, Integer farmacia) throws Exception {

        fx(() -> conSesion(rol, farmacia, () -> {
            CareStockApp app = aplicacion();

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {

                app.mostrarFormularioAjustes();

                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0)).showAndWait();
                verify(app, never()).transicionarEscena(
                    anyString(), anyDouble(), anyDouble());
            }
            return null;
        }));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMINISTRADOR", "SUPER_ADMIN"})
    void ajustesPermiteAdministradores(String rol)
            throws Exception {

        fx(() -> conSesion(rol, 8, () -> {
            CareStockApp app = aplicacion();
            app.mostrarFormularioAjustes();

            verify(app).transicionarEscena(
                "/org/example/carestock/ajustes-view.fxml",
                1100, 750);
            return null;
        }));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "AUXILIAR,8",
        "ADMINISTRADOR,NULL",
        "FARMACEUTICO,0"
    }, nullValues = "NULL")
    void historialRechazaAccesoNoAutorizado(
            String rol, Integer farmacia) throws Exception {

        fx(() -> conSesion(rol, farmacia, () -> {
            CareStockApp app = aplicacion();

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {

                app.mostrarHistorialMovimientos();

                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0)).showAndWait();
                verify(app, never()).transicionarEscena(
                    anyString(), anyDouble(), anyDouble());
            }
            return null;
        }));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMINISTRADOR", "FARMACEUTICO"})
    void historialPermiteRolesAutorizados(String rol)
            throws Exception {

        fx(() -> conSesion(rol, 8, () -> {
            CareStockApp app = aplicacion();
            app.mostrarHistorialMovimientos();

            verify(app).transicionarEscena(
                "/org/example/carestock/historial-movimientos-view.fxml",
                1200, 760);
            return null;
        }));
    }

    @Test
    void smsRechazaSesionInactiva() throws Exception {
        fx(() -> {
            CareStockApp app = aplicacion();

            try (var sesion = mockStatic(SesionUsuario.class);
                 MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {

                sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

                app.mostrarConfiguracionSms();

                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0)).showAndWait();
            }
            return null;
        });
    }

    @ParameterizedTest
    @CsvSource(value = {
        "AUXILIAR,8",
        "FARMACEUTICO,8",
        "ADMINISTRADOR,NULL"
    }, nullValues = "NULL")
    void smsRechazaUsuarioSinPermisos(
            String rol, Integer farmacia) throws Exception {

        fx(() -> conSesion(rol, farmacia, () -> {
            CareStockApp app = aplicacion();

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {

                app.mostrarConfiguracionSms();

                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0)).showAndWait();
                verify(app, never()).transicionarEscena(
                    anyString(), anyDouble(), anyDouble());
            }
            return null;
        }));
    }

    @Test
    void smsPermiteAlAdministradorDeFarmacia()
            throws Exception {

        fx(() -> conSesion("ADMINISTRADOR", 8, () -> {
            CareStockApp app = aplicacion();
            app.mostrarConfiguracionSms();

            verify(app).transicionarEscena(
                "/org/example/carestock/preferencias-sms-view.fxml",
                1100, 750);
            return null;
        }));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "AUXILIAR,8",
        "FARMACEUTICO,8",
        "ADMINISTRADOR,NULL",
        "ADMINISTRADOR,0"
    }, nullValues = "NULL")
    void reposicionRechazaAccesoNoAutorizado(
            String rol, Integer farmacia) throws Exception {

        fx(() -> conSesion(rol, farmacia, () -> {
            CareStockApp app = aplicacion();

            try (MockedConstruction<Alert> alertas =
                    mockConstruction(Alert.class)) {

                app.mostrarReposicion();

                assertEquals(1, alertas.constructed().size());
                verify(alertas.constructed().get(0)).showAndWait();
                verify(app, never()).transicionarEscena(
                    anyString(), anyDouble(), anyDouble());
            }
            return null;
        }));
    }

    @Test
    void reposicionPermiteAlAdministrador()
            throws Exception {

        fx(() -> conSesion("ADMINISTRADOR", 8, () -> {
            CareStockApp app = aplicacion();
            app.mostrarReposicion();

            verify(app).transicionarEscena(
                "/org/example/carestock/reposicion-view.fxml",
                1180, 750);
            return null;
        }));
    }

    @Test
    void consultaUnificadaAbreRutaCorrecta()
            throws Exception {

        fx(() -> conSesion("AUXILIAR", 8, () -> {
            CareStockApp app = aplicacion();
            app.mostrarConsultaUnificada();

            verify(app).transicionarEscena(
                "/org/example/carestock/consulta-inventario-view.fxml",
                1120, 720);
            return null;
        }));
    }

    @Test
    void dashboardAbreVistaConSesion()
            throws Exception {

        fx(() -> conSesion("ADMINISTRADOR", 8, () -> {
            CareStockApp app = aplicacion();
            app.mostrarDashboard();

            verify(app).transicionarEscena(
                "/org/example/carestock/inventario-view.fxml",
                1100, 750);
            return null;
        }));
    }

    @Test
    void edicionRechazaIdentificadorInvalido() {
        CareStockApp app = aplicacion();

        assertThrows(IllegalArgumentException.class,
            () -> app.mostrarCatalogoParaEditar("Aseo", 0));

        assertThrows(IllegalArgumentException.class,
            () -> app.mostrarCatalogoParaEditar("Aseo", -1));
    }

    @Test
    void gestionAlertaExigeSesionActiva() {
        CareStockApp app = aplicacion();

        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva)
                .thenReturn(false);

            assertThrows(SecurityException.class,
                () -> app.mostrarGestionAlerta(null, 8));
        }
    }

    @Test
    void cerrarSesionInvocaLimpiezaYLogin()
            throws Exception {

        fx(() -> {
            CareStockApp app = aplicacion();

            try (var sesion = mockStatic(SesionUsuario.class)) {
                app.cerrarSesion();

                sesion.verify(SesionUsuario::cerrar);
                verify(app).mostrarLogin();
            }
            return null;
        });
    }
}
