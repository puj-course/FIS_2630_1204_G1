package org.example.carestock.config;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;
import static org.mockito.Mockito.*;

class ConexionBDConfiguracionAisladaTest {

    private static final Path RUTA = Path.of(
            "config", "conexion-local.properties");

    private static void sinCredencialesExternas() {
        assumeTrue(
                System.getenv("DB_URL") == null
                && System.getenv("DB_USER") == null
                && System.getenv("DB_PASSWORD") == null,
                "El entorno tiene credenciales externas");
    }

    private static Object ejecutar(String nombre)
            throws ReflectiveOperationException {
        Method metodo = ConexionBD.class
                .getDeclaredMethod(nombre);

        metodo.setAccessible(true);
        return metodo.invoke(null);
    }

    private static MockedStatic<Files> simularArchivo(
            String contenido) throws IOException {

        MockedStatic<Files> archivos =
                mockStatic(Files.class);

        archivos.when(
                () -> Files.isRegularFile(RUTA))
                .thenReturn(true);

        archivos.when(
                () -> Files.newInputStream(RUTA))
                .thenAnswer(invocacion ->
                        new ByteArrayInputStream(
                                contenido.getBytes(
                                        StandardCharsets.UTF_8)));

        return archivos;
    }

    @Test
    void cargaConfiguracionLocalSimulada() throws Exception {
        sinCredencialesExternas();

        String propiedades = """
                db.url=jdbc:postgresql://127.0.0.1:5432/qa
                db.user=usuario_qa
                db.password=clave_qa
                """;

        try (MockedStatic<Files> archivos =
                     simularArchivo(propiedades)) {

            Properties p = (Properties)
                    ejecutar("cargarConfiguracion");

            assertEquals(
                    "usuario_qa", p.getProperty("db.user"));

            assertEquals(
                    "clave_qa", p.getProperty("db.password"));
        }
    }

    @Test
    void fallaSiNoExisteArchivoDeConfiguracion() {
        sinCredencialesExternas();

        try (MockedStatic<Files> archivos =
                     mockStatic(Files.class)) {

            InvocationTargetException error =
                    assertThrows(
                            InvocationTargetException.class,
                            () -> ejecutar("cargarConfiguracion"));

            assertInstanceOf(
                    SQLException.class, error.getCause());
        }
    }

    @Test
    void convierteErrorDeLecturaEnSQLException()
            throws Exception {
        sinCredencialesExternas();

        try (MockedStatic<Files> archivos =
                     mockStatic(Files.class)) {

            archivos.when(
                    () -> Files.isRegularFile(RUTA))
                    .thenReturn(true);

            archivos.when(
                    () -> Files.newInputStream(RUTA))
                    .thenThrow(
                            new IOException("Archivo de prueba corrupto"));

            InvocationTargetException error =
                    assertThrows(
                            InvocationTargetException.class,
                            () -> ejecutar("cargarConfiguracion"));

            assertInstanceOf(
                    SQLException.class, error.getCause());

            assertInstanceOf(
                    IOException.class,
                    error.getCause().getCause());
        }
    }

    @Test
    void rechazaConfiguracionDePoolInvalida()
            throws Exception {
        sinCredencialesExternas();

        String propiedades = """
                db.url=jdbc:postgresql://127.0.0.1:5432/qa
                db.user=usuario_qa
                db.password=clave_qa
                db.pool.max=4
                db.pool.min=5
                """;

        try (MockedStatic<Files> archivos =
                     simularArchivo(propiedades)) {

            InvocationTargetException error =
                    assertThrows(
                            InvocationTargetException.class,
                            () -> ejecutar("crearPool"));

            assertInstanceOf(
                    SQLException.class, error.getCause());
        }
    }
}
