package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;

import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Usuario;
import org.example.carestock.service.AuthenticationService;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoginControllerFxTest {

    private AuthenticationService autenticacion;
    private Preferences preferencias;
    private LoginController.InicioDeSesion iniciarSesion;
    private Runnable dashboard;
    private LoginController controlador;

    @BeforeAll
    static void iniciarJavaFx() throws Exception {
        CountDownLatch listo = new CountDownLatch(1);

        try {
            Platform.startup(listo::countDown);
        } catch (IllegalStateException yaIniciado) {
            listo.countDown();
        }

        assertTrue(listo.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    private static <T> T enFx(Callable<T> accion)
            throws Exception {

        if (Platform.isFxApplicationThread()) {
            return accion.call();
        }

        FutureTask<T> tarea = new FutureTask<>(accion);
        Platform.runLater(tarea);

        return tarea.get(15, TimeUnit.SECONDS);
    }

    private void esperar(BooleanSupplier condicion)
            throws Exception {

        for (int intento = 0; intento < 150; intento++) {
            if (enFx(condicion::getAsBoolean)) {
                return;
            }
            Thread.sleep(25);
        }

        fail("La operacion JavaFX no finalizo.");
    }

    @SuppressWarnings("unchecked")
    private <T> T campo(String nombre) throws Exception {
        Field atributo = LoginController.class
                .getDeclaredField(nombre);

        atributo.setAccessible(true);

        return (T) atributo.get(controlador);
    }

    private void cargarFormulario() throws Exception {
        enFx(() -> {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/carestock/login-view.fxml"));

            cargador.setControllerFactory(tipo ->
                    new LoginController(
                            autenticacion,
                            () -> preferencias,
                            iniciarSesion,
                            dashboard));

            cargador.load();
            controlador = cargador.getController();

            return null;
        });
    }

    @BeforeEach
    void preparar() throws Exception {
        autenticacion = mock(AuthenticationService.class);
        preferencias = mock(Preferences.class);
        iniciarSesion = mock(
                LoginController.InicioDeSesion.class);
        dashboard = mock(Runnable.class);

        when(preferencias.get("correo_recordado", ""))
                .thenReturn("");

        cargarFormulario();
    }

    private void completarCredenciales() throws Exception {
        TextField email = campo("txtEmail");
        PasswordField password = campo("txtPassword");

        email.setText(" ana@test.local ");
        password.setText("Clave#2026");
    }

    private Usuario usuario() {
        return new Usuario(
                7, "Ana", "ana@test.local",
                null, "FARMACEUTICO");
    }

    @Test
    void formularioIniciaConControlesHabilitados()
            throws Exception {

        enFx(() -> {
            Button ingresar = campo("btnIngresar");
            ProgressIndicator carga = campo("loaderCarga");
            TextField visible = campo("txtPasswordVisible");
            PasswordField password = campo("txtPassword");

            assertFalse(ingresar.isDisable());
            assertFalse(carga.isVisible());
            assertFalse(visible.isVisible());
            assertTrue(password.isVisible());
            assertEquals("Iniciar sesión", ingresar.getText());
            return null;
        });
    }

    @Test
    void rechazaFormularioVacioSinConsultarRepositorio()
            throws Exception {

        enFx(() -> {
            Button ingresar = campo("btnIngresar");
            Label correoError = campo("lblEmailError");
            Label claveError = campo("lblPasswordError");

            ingresar.fire();

            assertTrue(correoError.isVisible());
            assertTrue(claveError.isVisible());
            assertFalse(ingresar.isDisable());

            return null;
        });

        verifyNoInteractions(autenticacion);
    }

    @Test
    void validaCamposCuandoUsuarioCorrigeDatos()
            throws Exception {

        enFx(() -> {
            Button ingresar = campo("btnIngresar");
            TextField correo = campo("txtEmail");
            PasswordField clave = campo("txtPassword");
            Label correoError = campo("lblEmailError");
            Label claveError = campo("lblPasswordError");

            correo.setText("correo-invalido");
            ingresar.fire();

            assertTrue(correoError.isVisible());
            assertTrue(claveError.isVisible());

            correo.setText("ana@test.local");
            clave.setText("Clave#2026");

            assertFalse(correoError.isVisible());
            assertFalse(claveError.isVisible());

            return null;
        });

        verifyNoInteractions(autenticacion);
    }

    @Test
    void muestraYOcultaContrasenaSinPerderContenido()
            throws Exception {

        enFx(() -> {
            PasswordField clave = campo("txtPassword");
            TextField visible = campo("txtPasswordVisible");
            Button mostrar = campo("btnMostrarPassword");

            clave.setText("Clave#2026");
            mostrar.fire();

            assertTrue(visible.isVisible());
            assertFalse(clave.isVisible());
            assertEquals("Clave#2026", visible.getText());
            assertEquals("Ocultar contraseña",
                    mostrar.getAccessibleText());

            visible.setText("NuevaClave#2026");
            mostrar.fire();

            assertFalse(visible.isVisible());
            assertTrue(clave.isVisible());
            assertEquals("NuevaClave#2026", clave.getText());
            assertEquals("Mostrar contraseña",
                    mostrar.getAccessibleText());

            return null;
        });
    }

    @Test
    void recuperaCorreoRecordado()
            throws Exception {

        when(preferencias.get("correo_recordado", ""))
                .thenReturn("recordado@test.local");

        cargarFormulario();

        enFx(() -> {
            TextField email = campo("txtEmail");
            CheckBox recordar = campo("chkRecordarme");

            assertEquals("recordado@test.local",
                    email.getText());

            assertTrue(recordar.isSelected());
            return null;
        });
    }

    @Test
    void errorDePreferenciasNoImpideMostrarFormulario()
            throws Exception {

        when(preferencias.get("correo_recordado", ""))
                .thenThrow(new SecurityException("Sin permisos"));

        cargarFormulario();

        enFx(() -> {
            TextField email = campo("txtEmail");
            Button ingresar = campo("btnIngresar");

            assertTrue(email.getText().isEmpty());
            assertFalse(ingresar.isDisable());

            return null;
        });
    }

    @Test
    void autenticacionCorrectaMuestraCargaYRecuerdaCorreo()
            throws Exception {

        CountDownLatch permitir = new CountDownLatch(1);

        when(autenticacion.autenticar(
                "ana@test.local", "Clave#2026"))
                .thenAnswer(invocacion -> {
                    if (!permitir.await(5, TimeUnit.SECONDS)) {
                        throw new IllegalStateException(
                                "Timeout simulado");
                    }
                    return usuario();
                });

        try {
            enFx(() -> {
                completarCredenciales();

                CheckBox recordar = campo("chkRecordarme");
                Button ingresar = campo("btnIngresar");
                ProgressIndicator carga = campo("loaderCarga");

                recordar.setSelected(true);
                ingresar.fire();

                assertTrue(ingresar.isDisable());
                assertEquals("Validando...", ingresar.getText());
                assertTrue(carga.isVisible());

                return null;
            });
        } finally {
            permitir.countDown();
        }

        esperar(() -> {
            try {
                Button ingresar = campo("btnIngresar");
                return !ingresar.isDisable();
            } catch (Exception e) {
                throw new AssertionError(e);
            }
        });

        verify(iniciarSesion, timeout(5000))
                .iniciar(any(Usuario.class));

        verify(dashboard, timeout(5000)).run();

        verify(preferencias).put(
                "correo_recordado", "ana@test.local");
    }

    @Test
    void autenticacionCorrectaSinRecordarEliminaPreferencia()
            throws Exception {

        when(autenticacion.autenticar(
                "ana@test.local", "Clave#2026"))
                .thenReturn(usuario());

        enFx(() -> {
            completarCredenciales();

            CheckBox recordar = campo("chkRecordarme");
            Button ingresar = campo("btnIngresar");

            recordar.setSelected(false);
            ingresar.fire();
            return null;
        });

        verify(dashboard, timeout(5000)).run();

        verify(preferencias).remove("correo_recordado");
    }

    @Test
    void accesoExitosoRegistraUnaAuditoriaSms() throws Exception {
        var auditoria = mock(LoginController.AuditoriaAcceso.class);
        when(autenticacion.autenticar(
                "ana@test.local", "Clave#2026")).thenReturn(usuario());

        enFx(() -> {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/carestock/login-view.fxml"));
            cargador.setControllerFactory(tipo -> new LoginController(
                    autenticacion, () -> preferencias,
                    iniciarSesion, dashboard, auditoria));
            cargador.load();
            controlador = cargador.getController();
            completarCredenciales();
            ((Button) campo("btnIngresar")).fire();
            return null;
        });

        verify(auditoria, timeout(5000).times(1))
                .registrar(any(Usuario.class));
        verify(dashboard, timeout(5000)).run();
    }

    @Test
    void credencialesIncorrectasNoGeneranAuditoria() throws Exception {
        var auditoria = mock(LoginController.AuditoriaAcceso.class);
        when(autenticacion.autenticar(
                "ana@test.local", "Clave#2026"))
                .thenThrow(new ReglaNegocioException("Acceso denegado"));

        enFx(() -> {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/carestock/login-view.fxml"));
            cargador.setControllerFactory(tipo -> new LoginController(
                    autenticacion, () -> preferencias,
                    iniciarSesion, dashboard, auditoria));
            cargador.load();
            controlador = cargador.getController();
            completarCredenciales();
            ((Button) campo("btnIngresar")).fire();
            return null;
        });
        Label mensaje = campo("lblNotificacion");
        esperar(mensaje::isVisible);
        verifyNoInteractions(auditoria, dashboard);
    }

    @Test
    void errorAlAuditarNoBloqueaDashboard() throws Exception {
        var auditoria = mock(LoginController.AuditoriaAcceso.class);
        when(autenticacion.autenticar(
                "ana@test.local", "Clave#2026")).thenReturn(usuario());
        doThrow(new SQLException("SMS fuera de linea"))
                .when(auditoria).registrar(any(Usuario.class));

        enFx(() -> {
            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/carestock/login-view.fxml"));
            cargador.setControllerFactory(tipo -> new LoginController(
                    autenticacion, () -> preferencias,
                    iniciarSesion, dashboard, auditoria));
            cargador.load();
            controlador = cargador.getController();
            completarCredenciales();
            ((Button) campo("btnIngresar")).fire();
            return null;
        });
        verify(dashboard, timeout(5000)).run();
    }

    @Test
    void credencialesIncorrectasMuestranErrorYRestablecenFormulario()
            throws Exception {

        when(autenticacion.autenticar(
                "ana@test.local", "Clave#2026"))
                .thenThrow(new ReglaNegocioException(
                        "Credenciales incorrectas o usuario inactivo."));

        enFx(() -> {
            completarCredenciales();

            Button ingresar = campo("btnIngresar");
            ingresar.fire();

            return null;
        });

        Label notificacion = campo("lblNotificacion");

        esperar(notificacion::isVisible);

        enFx(() -> {
            PasswordField clave = campo("txtPassword");
            Button ingresar = campo("btnIngresar");
            ProgressIndicator carga = campo("loaderCarga");

            assertTrue(clave.getText().isEmpty());
            assertFalse(ingresar.isDisable());
            assertFalse(carga.isVisible());
            assertTrue(notificacion.getText()
                    .contains("Credenciales incorrectas"));

            return null;
        });

        verifyNoInteractions(iniciarSesion, dashboard);
    }

    @Test
    void errorInesperadoNoMuestraDetallesInternos()
            throws Exception {

        when(autenticacion.autenticar(
                "ana@test.local", "Clave#2026"))
                .thenThrow(new IllegalStateException(
                        "Detalle interno de conexion"));

        enFx(() -> {
            completarCredenciales();

            Button ingresar = campo("btnIngresar");
            ingresar.fire();

            return null;
        });

        Label notificacion = campo("lblNotificacion");

        esperar(notificacion::isVisible);

        enFx(() -> {
            assertFalse(notificacion.getText()
                    .contains("Detalle interno"));
            assertTrue(notificacion.getText()
                    .contains("No fue posible verificar"));
            return null;
        });

        verifyNoInteractions(iniciarSesion, dashboard);
    }

    @Test
    void errorAlIniciarSesionNoAbreDashboard()
            throws Exception {

        when(autenticacion.autenticar(
                "ana@test.local", "Clave#2026"))
                .thenReturn(usuario());

        doThrow(new Exception("Error JDBC simulado"))
                .when(iniciarSesion)
                .iniciar(any(Usuario.class));

        enFx(() -> {
            completarCredenciales();

            Button ingresar = campo("btnIngresar");
            ingresar.fire();

            return null;
        });

        Label notificacion = campo("lblNotificacion");

        esperar(notificacion::isVisible);

        enFx(() -> {
            assertTrue(notificacion.getText()
                    .contains("No fue posible abrir la sesión"));
            return null;
        });

        verifyNoInteractions(dashboard);
    }

    @Test
    void errorGuardandoPreferenciaNoBloqueaIngreso()
            throws Exception {

        when(autenticacion.autenticar(
                "ana@test.local", "Clave#2026"))
                .thenReturn(usuario());

        doThrow(new SecurityException("Sin permisos"))
                .when(preferencias)
                .put(anyString(), anyString());

        enFx(() -> {
            completarCredenciales();

            CheckBox recordar = campo("chkRecordarme");
            Button ingresar = campo("btnIngresar");

            recordar.setSelected(true);
            ingresar.fire();
            return null;
        });

        verify(dashboard, timeout(5000)).run();
    }
}
