package org.example.carestock.controller;

import org.example.carestock.CareStockApp;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NavegacionRefactorizadaTest {

    private void ejecutar(Object controlador, String nombre)
            throws Exception {
        Method metodo = controlador.getClass()
                .getDeclaredMethod(nombre);
        metodo.setAccessible(true);
        metodo.invoke(controlador);
    }

    @Test
    void catalogoNavegaCorrectamente() throws Exception {
        CareStockApp app = mock(CareStockApp.class);

        try (MockedStatic<CareStockApp> estatico =
                     mockStatic(CareStockApp.class)) {

            estatico.when(CareStockApp::getInstance)
                    .thenReturn(app);

            ControladorCatalogo controlador =
                    new ControladorCatalogo();

            ejecutar(controlador, "onConsultaUnificada");
            verify(app).mostrarConsultaUnificada();

            ejecutar(controlador, "onVolver");
            verify(app).mostrarDashboard();

            ejecutar(controlador, "onCerrarSesion");
            verify(app).cerrarSesion();
        }
    }

    @Test
    void inventarioNavegaCorrectamente() throws Exception {
        CareStockApp app = mock(CareStockApp.class);

        try (MockedStatic<CareStockApp> estatico =
                     mockStatic(CareStockApp.class)) {

            estatico.when(CareStockApp::getInstance)
                    .thenReturn(app);

            ControladorInventario controlador =
                    new ControladorInventario();

            ejecutar(controlador, "onAbrirMovimientos");
            verify(app).mostrarFormularioMovimientos();

            ejecutar(controlador, "onAbrirAjustes");
            verify(app).mostrarFormularioAjustes();

            ejecutar(controlador, "onAbrirHistorial");
            verify(app).mostrarHistorialMovimientos();

            ejecutar(controlador, "onAbrirCatalogo");
            verify(app).mostrarCatalogo();

            ejecutar(controlador, "onAbrirAlertas");
            verify(app).mostrarAlertas();

            ejecutar(controlador, "onCerrarSesion");
            verify(app).cerrarSesion();
        }
    }

    @Test
    void alertasPermiteVolverYCerrarSesion() throws Exception {
        CareStockApp app = mock(CareStockApp.class);

        try (MockedStatic<CareStockApp> estatico =
                     mockStatic(CareStockApp.class)) {

            estatico.when(CareStockApp::getInstance)
                    .thenReturn(app);

            ControladorAlertas controlador =
                    new ControladorAlertas();

            ejecutar(controlador, "onVolver");
            verify(app).mostrarDashboard();

            ejecutar(controlador, "onCerrarSesion");
            verify(app).cerrarSesion();
        }
    }
}
