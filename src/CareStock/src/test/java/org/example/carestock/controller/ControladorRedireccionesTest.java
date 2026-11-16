package org.example.carestock.controller;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.example.carestock.session.SesionUsuario;

import static org.junit.jupiter.api.Assertions.*;

class ControladorRedireccionesTest {

    @Test
    void testRedireccionesNoFallan() {
        SesionUsuario.DatosUsuario sesionMock = Mockito.mock(SesionUsuario.DatosUsuario.class);
        Mockito.when(sesionMock.idFarmacia()).thenReturn(10);
        Mockito.when(sesionMock.rol()).thenReturn("ADMINISTRADOR");

        try (MockedStatic<SesionUsuario> sesionStatic = Mockito.mockStatic(SesionUsuario.class)) {
            sesionStatic.when(SesionUsuario::actual).thenReturn(sesionMock);
            sesionStatic.when(SesionUsuario::estaActiva).thenReturn(true);

            try (MockedStatic<org.example.carestock.CareStockApp> appStatic = Mockito.mockStatic(org.example.carestock.CareStockApp.class)) {
                org.example.carestock.CareStockApp appMock = Mockito.mock(org.example.carestock.CareStockApp.class);
                appStatic.when(org.example.carestock.CareStockApp::getInstance).thenReturn(appMock);

                ControladorInventario controlador = new ControladorInventario();
                
                assertDoesNotThrow(() -> controlador.onIrAAlertas());
                assertDoesNotThrow(() -> controlador.onIrALotesPorVencer());
                assertDoesNotThrow(() -> controlador.onIrAHistorial());
                
                Mockito.verify(appMock, Mockito.times(1)).mostrarAlertas();
                Mockito.verify(appMock, Mockito.times(1)).mostrarHistorialMovimientos();
            }
        }
    }
}
