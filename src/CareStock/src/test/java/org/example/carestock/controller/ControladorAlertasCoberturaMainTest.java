package org.example.carestock.controller;

import javafx.beans.value.ObservableValue;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ControladorAlertasCoberturaMainTest {

    private AlertaInventario alerta(
            LocalDateTime fecha,
            String tipo,
            String usuario) {

        return new AlertaInventario(
                TipoAlerta.STOCK_MINIMO,
                Categoria.ASEO,
                1, "ASE-001", "Jabon", null,
                0, 5, null, null,
                Severidad.CRITICA,
                fecha, tipo, usuario
        );
    }

    private String movimiento(AlertaInventario alerta)
            throws Exception {

        Method metodo = ControladorAlertas.class
                .getDeclaredMethod(
                        "textoUltimoMovimiento",
                        AlertaInventario.class);

        metodo.setAccessible(true);
        return (String) metodo.invoke(null, alerta);
    }

    @Test
    void rechazaInicializacionSinSesion() {
        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class)) {

            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> new ControladorAlertas().initialize()
            );

            assertTrue(error.getMessage().contains("sesion"));
        }
    }

    @Test
    void rechazaUsuarioSinFarmacia() {
        comprobarFarmaciaInvalida(null);
    }

    @Test
    void rechazaFarmaciaConIdCero() {
        comprobarFarmaciaInvalida(0);
    }

    private void comprobarFarmaciaInvalida(Integer idFarmacia) {
        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class)) {

            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);

            sesion.when(SesionUsuario::actual)
                    .thenReturn(new SesionUsuario.DatosUsuario(
                            10, "Administrador",
                            "admin@test.local",
                            "ADMINISTRADOR",
                            idFarmacia, null
                    ));

            IllegalStateException error = assertThrows(
                    IllegalStateException.class,
                    () -> new ControladorAlertas().initialize()
            );

            assertTrue(error.getMessage()
                    .contains("farmacia asignada"));
        }
    }

    @Test
    void movimientoSinFechaMuestraMensaje() throws Exception {
        assertEquals(
                "Sin movimientos registrados",
                movimiento(alerta(null, null, null))
        );
    }

    @Test
    void movimientoConFechaIncluyeAuditoria() throws Exception {
        var fecha = LocalDateTime.of(2026, 10, 31, 14, 30);

        String texto = movimiento(
                alerta(fecha, "AJUSTE", "Administrador")
        );

        assertAll(
                () -> assertTrue(texto.contains("AJUSTE")),
                () -> assertTrue(texto.contains("Administrador")),
                () -> assertTrue(
                        texto.contains("31/10/2026 14:30"))
        );
    }

    @Test
    void movimientoSinUsuarioNiTipoNoFalla() throws Exception {
        var fecha = LocalDateTime.of(2026, 10, 31, 8, 5);

        String texto = movimiento(alerta(fecha, null, null));

        assertNotNull(texto);
        assertTrue(texto.contains("31/10/2026 08:05"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void construyeOpcionesDeFiltro() throws Exception {
        Method metodo = ControladorAlertas.class
                .getDeclaredMethod("opciones", String[].class);

        metodo.setAccessible(true);

        List<String> opciones = (List<String>) metodo.invoke(
                null,
                (Object) new String[]{
                        "Todos", "Stock minimo", "Vencimiento"
                }
        );

        assertEquals(3, opciones.size());
        assertEquals("Todos", opciones.get(0));
        assertEquals("Vencimiento", opciones.get(2));
    }

    @Test
    @SuppressWarnings("unchecked")
    void normalizaCeldasNulas() throws Exception {
        Method metodo = ControladorAlertas.class
                .getDeclaredMethod("celda", String.class);

        metodo.setAccessible(true);

        ObservableValue<String> vacia =
                (ObservableValue<String>) metodo.invoke(
                        null, (Object) null);

        ObservableValue<String> producto =
                (ObservableValue<String>) metodo.invoke(
                        null, "Acetaminofen");

        assertEquals("", vacia.getValue());
        assertEquals("Acetaminofen", producto.getValue());
    }

    @Test
    void noIniciaOtraCargaSiYaEstaCargando()
            throws Exception {

        ControladorAlertas controlador =
                new ControladorAlertas();

        Field campo = ControladorAlertas.class
                .getDeclaredField("cargando");

        campo.setAccessible(true);
        campo.setBoolean(controlador, true);

        Method metodo = ControladorAlertas.class
                .getDeclaredMethod("cargar");

        metodo.setAccessible(true);

        assertDoesNotThrow(() -> metodo.invoke(controlador));
        assertTrue(campo.getBoolean(controlador));
    }
}
