package org.example.carestock;

import javafx.application.Application;

public final class Launcher {

    private Launcher() {
        // Clase de punto de entrada.
    }

    public static void main(String[] args) {
        Application.launch(CareStockApp.class, args);
    }
}
