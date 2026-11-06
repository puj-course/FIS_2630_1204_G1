package org.example.carestock;

import org.example.carestock.controller.ControladorInventario;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CareStockAppCatalogoIntegradoTest {

    private static final SesionUsuario.DatosUsuario ADMIN =
            new SesionUsuario.DatosUsuario(
                    1, "Ana", "ana@test.local",
                    "ADMINISTRADOR", 7, "Farmacia QA");

    private CareStockApp app(
            ControladorInventario controlador) throws Exception {

        CareStockApp aplicacion = new CareStockApp();

        Field campo = CareStockApp.class.getDeclaredField(
                "controladorDashboard");

        campo.setAccessible(true);
        campo.set(aplicacion, controlador);

        return aplicacion;
    }

    @Test
    void desdeDashboardNoTransicionaAEscenaNueva()
            throws Exception {

        var dashboard = mock(ControladorInventario.class);
        var aplicacion = app(dashboard);

        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);

            assertDoesNotThrow(aplicacion::mostrarCatalogo);

            verify(dashboard).abrirCatalogoIntegrado(
                    null, null);
        }
    }

    @Test
    void edicionDesdeOtroModuloPreservaDestinoYNoLoReutiliza()
            throws Exception {

        var dashboard = mock(ControladorInventario.class);
        var aplicacion = app(dashboard);

        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);
            sesion.when(SesionUsuario::esAdministrador)
                    .thenReturn(true);

            aplicacion.mostrarCatalogoParaEditar("Aseo", 17);

            verify(dashboard).abrirCatalogoIntegrado(
                    "Aseo", 17);

            aplicacion.mostrarCatalogo();

            verify(dashboard).abrirCatalogoIntegrado(
                    null, null);
        }
    }

    @Test
    void registroDesdeOtroModuloPreservaCategoria()
            throws Exception {

        var dashboard = mock(ControladorInventario.class);
        var aplicacion = app(dashboard);

        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            sesion.when(SesionUsuario::actual)
                    .thenReturn(ADMIN);
            sesion.when(SesionUsuario::esAdministrador)
                    .thenReturn(true);

            aplicacion.mostrarCatalogoParaRegistro(
                    "Maternidad");

            verify(dashboard).abrirCatalogoIntegrado(
                    "Maternidad", null);
        }
    }

    @Test
    void sinSesionNoAbreCatalogo() throws Exception {
        var dashboard = mock(ControladorInventario.class);
        var aplicacion = spy(app(dashboard));

        doNothing().when(aplicacion).mostrarLogin();

        try (var sesion = mockStatic(SesionUsuario.class)) {
            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

            aplicacion.mostrarCatalogo();
        }

        verify(aplicacion).mostrarLogin();
        verifyNoInteractions(dashboard);
    }
}
