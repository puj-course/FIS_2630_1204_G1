package org.example.carestock;

import javafx.application.Application;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LauncherCoberturaAdicionalTest {

    @Test
    void iniciaCareStockSinAbrirVentanaReal() {
        String[] argumentos = {"--qa"};

        try (MockedStatic<Application> javafx =
                     mockStatic(Application.class)) {

            assertDoesNotThrow(
                    () -> Launcher.main(argumentos));

            javafx.verify(() -> Application.launch(
                    CareStockApp.class, argumentos));
        }
    }
}
