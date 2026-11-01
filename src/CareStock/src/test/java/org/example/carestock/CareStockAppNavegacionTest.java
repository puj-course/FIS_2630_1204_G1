package org.example.carestock;

import javafx.scene.control.Alert;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CareStockAppNavegacionTest {

    private CareStockApp aplicacion() {
        CareStockApp app = spy(new CareStockApp());

        doNothing().when(app).transicionarEscena(
                anyString(), anyDouble(), anyDouble());

        return app;
    }

    @Test
    void sinSesionRedirigeAlLogin() {
        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class)) {

            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

            CareStockApp app = aplicacion();
            doNothing().when(app).mostrarLogin();

            app.mostrarAlertas();

            verify(app).mostrarLogin();
            verify(app, never()).transicionarEscena(
                    anyString(), anyDouble(), anyDouble());
        }
    }

    @Test
    void permiteConsultarAlertasConFarmaciaAsignada() {
        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class)) {

            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);

            sesion.when(SesionUsuario::actual)
                    .thenReturn(new SesionUsuario.DatosUsuario(
                            10, "Ana", "ana@test.local",
                            "ADMINISTRADOR", 7, "Farmacia Central"
                    ));

            CareStockApp app = aplicacion();
            app.mostrarAlertas();

            verify(app).transicionarEscena(
                    "/org/example/carestock/alertas-view.fxml",
                    1240.0, 760.0);
        }
    }

    @Test
    void rechazaAccesoSinFarmacia() {
        comprobarFarmaciaInvalida(null);
    }

    @Test
    void rechazaFarmaciaConIdCero() {
        comprobarFarmaciaInvalida(0);
    }

    private void comprobarFarmaciaInvalida(Integer farmacia) {
        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class);
             MockedConstruction<Alert> alertas =
                     mockConstruction(Alert.class)) {

            sesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);

            sesion.when(SesionUsuario::actual)
                    .thenReturn(new SesionUsuario.DatosUsuario(
                            10, "Ana", "ana@test.local",
                            "ADMINISTRADOR", farmacia, null
                    ));

            CareStockApp app = aplicacion();
            app.mostrarAlertas();

            assertEquals(1, alertas.constructed().size());
            verify(alertas.constructed().get(0)).showAndWait();

            verify(app, never()).transicionarEscena(
                    anyString(), anyDouble(), anyDouble());
        }
    }
}
