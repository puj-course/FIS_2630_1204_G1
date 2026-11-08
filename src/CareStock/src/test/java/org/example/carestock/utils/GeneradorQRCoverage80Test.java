package org.example.carestock.utils;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneradorQRCoverage80Test {

    @Test
    void generaQrConDimensionesCorrectas() throws Exception {
        Image imagen = GeneradorQR.generar(
            "CARESTOCK-LOTE-123", 120);

        assertNotNull(imagen);
        assertEquals(120, imagen.getWidth());
        assertEquals(120, imagen.getHeight());
        assertNotNull(imagen.getPixelReader());
    }

    @Test
    void qrContienePixelesBlancosYNegros() throws Exception {
        Image imagen = GeneradorQR.generar(
            "MEDICAMENTO-12345", 100);

        PixelReader lector = imagen.getPixelReader();

        int oscuros = 0;
        int claros = 0;

        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                Color color = lector.getColor(x, y);

                if (color.getBrightness() < 0.5) {
                    oscuros++;
                } else {
                    claros++;
                }
            }
        }

        assertTrue(oscuros > 0);
        assertTrue(claros > 0);
        assertEquals(10000, oscuros + claros);
    }

    @Test
    void soportaCaracteresEspeciales() throws Exception {
        Image imagen = GeneradorQR.generar(
            "Farmacia Bogotá - Lote Nº 123", 90);

        assertNotNull(imagen);
        assertEquals(90, imagen.getWidth());
    }

    @Test
    void rechazaTextoNuloOVacio() {
        assertThrows(IllegalArgumentException.class,
            () -> GeneradorQR.generar(null, 100));

        assertThrows(IllegalArgumentException.class,
            () -> GeneradorQR.generar("", 100));

        assertThrows(IllegalArgumentException.class,
            () -> GeneradorQR.generar("   ", 100));
    }

    @Test
    void rechazaDimensionesInvalidas() {
        assertThrows(IllegalArgumentException.class,
            () -> GeneradorQR.generar("LOTE-001", 0));

        assertThrows(IllegalArgumentException.class,
            () -> GeneradorQR.generar("LOTE-001", -10));
    }
}
