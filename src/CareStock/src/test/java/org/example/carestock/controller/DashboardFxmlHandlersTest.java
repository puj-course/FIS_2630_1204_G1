package org.example.carestock.controller;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class DashboardFxmlHandlersTest {

    @Test
    void todosLosBotonesTienenControlador() throws Exception {

        String ruta = "/org/example/carestock/inventario-view.fxml";

        try (InputStream entrada =
                     getClass().getResourceAsStream(ruta)) {

            assertNotNull(entrada, "No existe el FXML");

            String xml = new String(
                    entrada.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            Set<String> metodos = Arrays.stream(
                    InventarioController.class.getDeclaredMethods()
            ).map(Method::getName).collect(Collectors.toSet());

            var patron = Pattern.compile(
                    "onAction\\s*=\\s*\"#([A-Za-z0-9_]+)\""
            );

            var coincidencias = patron.matcher(xml);

            int comprobados = 0;

            while (coincidencias.find()) {
                String metodo = coincidencias.group(1);

                assertTrue(
                        metodos.contains(metodo),
                        "Falta el metodo del controlador: " + metodo
                );

                comprobados++;
            }

            assertTrue(comprobados > 0);
        }
    }
}
