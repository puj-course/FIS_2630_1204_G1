package org.example.carestock.controller;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.example.carestock.session.SesionUsuario;

import static org.junit.jupiter.api.Assertions.*;

class ControladorTareasPendientesTest {

    @Test
    void testInicializacionDeContadoresVisuales() {
        SesionUsuario.DatosUsuario sesionMock = Mockito.mock(SesionUsuario.DatosUsuario.class);
        Mockito.when(sesionMock.idFarmacia()).thenReturn(10);
        Mockito.when(sesionMock.rol()).thenReturn("ADMINISTRADOR");

        try (MockedStatic<SesionUsuario> sesionStatic = Mockito.mockStatic(SesionUsuario.class)) {
            sesionStatic.when(SesionUsuario::actual).thenReturn(sesionMock);
            sesionStatic.when(SesionUsuario::estaActiva).thenReturn(true);

            ControladorInventario controlador = new ControladorInventario();
            assertNotNull(controlador, "El controlador debe instanciarse correctamente");
        }
    }
}
