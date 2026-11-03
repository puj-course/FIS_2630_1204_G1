package org.example.carestock.service;

import org.example.carestock.dto.AlertaInventario;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Arma y guarda el CSV de las alertas de inventario (HU-142).
 *
 * <p>No usa JavaFX, por eso se puede probar sin abrir ventanas. El formato es el
 * mismo del CSV de lotes: separador {@code ;} y codificacion UTF-8.</p>
 */
public final class ExportadorAlertasCsv {

    public static final String ENCABEZADO =
            "Severidad;Tipo;Categoría;Producto;Referencia;Existencias;Detalle";

    private static final char SEPARADOR = ';';
    private static final String SALTO = "\n";
    private static final char MARCA_UTF8 = '﻿';

    private ExportadorAlertasCsv() {
    }

    /** Devuelve el texto del CSV: encabezado y una linea por alerta. */
    public static String generar(List<AlertaInventario> alertas) {
        Objects.requireNonNull(alertas, "La lista de alertas es obligatoria");

        StringBuilder csv = new StringBuilder(ENCABEZADO).append(SALTO);
        for (AlertaInventario alerta : alertas) {
            csv.append(campo(alerta.severidad().etiqueta())).append(SEPARADOR)
                    .append(campo(alerta.tipoAlerta().etiqueta())).append(SEPARADOR)
                    .append(campo(alerta.categoria().etiqueta())).append(SEPARADOR)
                    .append(campo(alerta.producto())).append(SEPARADOR)
                    .append(campo(alerta.referencia())).append(SEPARADOR)
                    .append(alerta.existencias()).append(SEPARADOR)
                    .append(campo(alerta.mensaje())).append(SALTO);
        }
        return csv.toString();
    }

    /**
     * Guarda el CSV en UTF-8. Se antepone la marca de orden de bytes para que Excel
     * muestre bien las tildes.
     */
    public static void guardar(Path destino, List<AlertaInventario> alertas)
            throws IOException {
        Objects.requireNonNull(destino, "El archivo de destino es obligatorio");
        Files.writeString(destino, MARCA_UTF8 + generar(alertas), StandardCharsets.UTF_8);
    }

    /**
     * Escapa un valor: si trae separador, comillas o saltos de linea se encierra entre
     * comillas, y si empieza con un caracter que Excel toma como formula se le antepone
     * una comilla simple.
     */
    static String campo(String valor) {
        if (valor == null) {
            return "";
        }
        String texto = valor;
        if (!texto.isEmpty() && "=+-@".indexOf(texto.charAt(0)) >= 0) {
            texto = "'" + texto;
        }
        boolean requiereComillas = texto.indexOf(SEPARADOR) >= 0
                || texto.indexOf('"') >= 0
                || texto.indexOf('\n') >= 0
                || texto.indexOf('\r') >= 0;
        if (!requiereComillas) {
            return texto;
        }
        return '"' + texto.replace("\"", "\"\"") + '"';
    }
}
