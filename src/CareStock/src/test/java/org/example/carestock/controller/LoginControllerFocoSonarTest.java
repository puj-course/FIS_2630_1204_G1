package org.example.carestock.controller;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.example.carestock.service.AuthenticationService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.prefs.Preferences;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginControllerFocoSonarTest {

    @BeforeAll
    static void iniciarJavaFx() throws InterruptedException {
        CountDownLatch iniciado = new CountDownLatch(1);

        try {
            Platform.startup(iniciado::countDown);
        } catch (IllegalStateException yaIniciado) {
            iniciado.countDown();
        }

        assertTrue(iniciado.await(20, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    @Test
    void validaCorreoYContrasenasAlPerderElFoco()
            throws Exception {

        FutureTask<Void> tarea = new FutureTask<>(() -> {
            Preferences preferencias = mock(Preferences.class);

            when(preferencias.get("correo_recordado", ""))
                    .thenReturn("");

            FXMLLoader cargador = new FXMLLoader(
                    getClass().getResource(
                            "/org/example/carestock/login-view.fxml"));

            cargador.setControllerFactory(tipo ->
                    new LoginController(
                            mock(AuthenticationService.class),
                            () -> preferencias,
                            usuario -> { },
                            () -> { }));

            Parent raiz = cargador.load();

            Stage ventana = new Stage();
            ventana.setScene(new Scene(raiz, 1100, 650));

            try {
                ventana.show();
                ventana.requestFocus();

                TextField correo =
                        (TextField) raiz.lookup("#txtEmail");

                PasswordField clave =
                        (PasswordField) raiz.lookup("#txtPassword");

                TextField claveVisible =
                        (TextField) raiz.lookup("#txtPasswordVisible");

                Button alternar =
                        (Button) raiz.lookup("#btnMostrarPassword");

                Label errorCorreo =
                        (Label) raiz.lookup("#lblEmailError");

                Label errorClave =
                        (Label) raiz.lookup("#lblPasswordError");

                assertAll(
                        () -> assertNotNull(correo),
                        () -> assertNotNull(clave),
                        () -> assertNotNull(claveVisible),
                        () -> assertNotNull(alternar),
                        () -> assertNotNull(errorCorreo),
                        () -> assertNotNull(errorClave)
                );

                // Rama: el correo gana el foco.
                correo.requestFocus();

                assertTrue(correo.isFocused());
                assertFalse(errorCorreo.isVisible());

                // Rama: el correo pierde el foco.
                clave.requestFocus();

                assertTrue(clave.isFocused());
                assertTrue(errorCorreo.isVisible());
                assertFalse(errorClave.isVisible());

                // Rama: PasswordField pierde el foco.
                alternar.fire();

                assertTrue(claveVisible.isVisible());
                assertTrue(claveVisible.isFocused());
                assertTrue(errorClave.isVisible());

                // Rama: TextField visible pierde el foco.
                correo.requestFocus();

                assertTrue(correo.isFocused());
                assertTrue(errorClave.isVisible());

            } finally {
                ventana.hide();
            }

            return null;
        });

        Platform.runLater(tarea);
        tarea.get(20, TimeUnit.SECONDS);
    }
}
