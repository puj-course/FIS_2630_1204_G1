package org.example.carestock.controller;

import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class LoginViewStructureTest {

    @Test
    void fxmlIncluyeControlesAccionesYEstilos() throws Exception {
        try (var recurso = getClass().getResourceAsStream(
                "/org/example/carestock/login-view.fxml")) {

            assertNotNull(recurso);

            String contenido = new String(
                    recurso.readAllBytes(), StandardCharsets.UTF_8);

            for (String id : new String[]{
                    "txtEmail",
                    "txtPassword",
                    "txtPasswordVisible",
                    "btnMostrarPassword",
                    "chkRecordarme",
                    "lblEmailError",
                    "lblPasswordError",
                    "btnIngresar",
                    "loaderCarga",
                    "lblNotificacion"}) {

                assertTrue(
                        contenido.contains("fx:id=\"" + id + "\""),
                        "Falta el control " + id);
            }

            assertTrue(contenido.contains(
                    "onAction=\"#onTogglePassword\""));

            assertTrue(contenido.contains(
                    "stylesheets=\"@login.css\""));
        }

        assertNotNull(getClass().getResource(
                "/org/example/carestock/login.css"));
    }
}
