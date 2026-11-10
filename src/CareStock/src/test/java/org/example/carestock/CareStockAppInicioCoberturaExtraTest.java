package org.example.carestock;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.carestock.config.PrecalentadorConexionBD;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CareStockAppInicioCoberturaExtraTest {

    @Test
    @SuppressWarnings("unchecked")
    void iniciarRegistraInstanciaYConfiguraVentanaSinConexionReal()
            throws Exception {

        Field campo = CareStockApp.class
                .getDeclaredField("INSTANCE");
        campo.setAccessible(true);

        AtomicReference<CareStockApp> referencia =
                (AtomicReference<CareStockApp>) campo.get(null);

        CareStockApp anterior = referencia.get();

        CareStockApp app = spy(new CareStockApp());
        Stage ventana = mock(Stage.class);
        Scene escena = mock(Scene.class);

        when(ventana.getScene()).thenReturn(escena);
        doNothing().when(app).mostrarLogin();

        try (MockedStatic<PrecalentadorConexionBD> precalentador =
                mockStatic(PrecalentadorConexionBD.class)) {

            app.start(ventana);

            assertSame(app, CareStockApp.getInstance());
            verify(ventana).setResizable(true);
            verify(ventana).show();
            verify(ventana).setMaximized(true);

            precalentador.verify(
                    PrecalentadorConexionBD::iniciarAsync);

        } finally {
            referencia.set(anterior);
        }
    }
}
