package org.example.carestock.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class GeneradorQR {

    /**
     * Genera una imagen de código QR a partir de un texto.
     * 
     * @param texto El texto a codificar (ID del lote, nombre, etc).
     * @param tamaño El tamaño de la imagen en píxeles (ej. 200x200).
     * @return Un objeto Image de JavaFX listo para ser mostrado.
     */
    public static Image generar(String texto, int tamaño) throws Exception {
        if (texto == null || texto.isBlank()) {
            throw new IllegalArgumentException("El texto para el QR no puede estar vacío.");
        }

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(texto, BarcodeFormat.QR_CODE, tamaño, tamaño, hints);
        BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
        return convertirAJavaFXImage(bufferedImage);
    }

    private static Image convertirAJavaFXImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();
        
        WritableImage writableImage = new WritableImage(width, height);
        PixelWriter pixelWriter = writableImage.getPixelWriter();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                java.awt.Color colorAwt = new java.awt.Color(bufferedImage.getRGB(x, y), true);
                javafx.scene.paint.Color colorFx = javafx.scene.paint.Color.rgb(
                        colorAwt.getRed(),
                        colorAwt.getGreen(),
                        colorAwt.getBlue(),
                        colorAwt.getAlpha() / 255.0
                );
                pixelWriter.setColor(x, y, colorFx);
            }
        }

        return writableImage;
    }
}
