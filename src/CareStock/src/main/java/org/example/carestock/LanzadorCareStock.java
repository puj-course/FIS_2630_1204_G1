package org.example.carestock;

/**
 * Punto de entrada para la distribucion
 * ejecutable de CareStock.
 *
 * Permite iniciar JavaFX desde jpackage
 * sin heredar de Application.
 */
public final class LanzadorCareStock {

    private LanzadorCareStock() {
    }

    public static void main(String[] args) {
        CareStockApp.main(args);
    }
}
