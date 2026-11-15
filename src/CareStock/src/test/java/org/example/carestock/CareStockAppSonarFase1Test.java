package org.example.carestock;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CareStockAppSonarFase1Test {
    @Test
    void rutaFXMLNoExistenteSeNotificaSinCambiarPantalla() {
        CareStockApp app = new CareStockApp();
        assertDoesNotThrow(() -> app.transicionarEscena(
                "/org/example/carestock/NO_EXISTE_QA.fxml", 400, 300));
    }
}
