package org.example.carestock.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/** HU.137 / #991: exportacion segura de los registros visibles. */
public final class ExportadorReposicionCsv {

    public static final String ENCABEZADO =
            "Código;Producto;Categoría;Existencias actuales;"
            + "Stock mínimo;Cantidad faltante;Severidad";

    private ExportadorReposicionCsv() { }

    public static String generar(List<RegistroReposicion> visibles) {
        Objects.requireNonNull(visibles,
                "Los registros son obligatorios");

        if (visibles.isEmpty()) {
            throw new IllegalArgumentException(
                    "No hay productos para exportar");
        }

        StringBuilder csv = new StringBuilder(ENCABEZADO)
                .append('\n');

        for (RegistroReposicion registro : visibles) {
            Objects.requireNonNull(registro,
                    "No se permiten registros nulos");

            csv.append(campo(registro.codigo())).append(';')
                    .append(campo(registro.nombre())).append(';')
                    .append(campo(registro.categoria().etiqueta())).append(';')
                    .append(registro.existenciasActuales()).append(';')
                    .append(registro.stockMinimo()).append(';')
                    .append(registro.cantidadFaltante()).append(';')
                    .append(campo(registro.severidad().etiqueta()))
                    .append('\n');
        }

        return csv.toString();
    }

    /** UTF-8 con BOM para facilitar su lectura en Excel. */
    public static void guardar(
            Path destino,
            List<RegistroReposicion> visibles) throws IOException {

        Objects.requireNonNull(destino,
                "El destino es obligatorio");

        // Validar todo antes de escribir el archivo.
        String contenido = generar(visibles);

        Files.writeString(
                destino,
                "\uFEFF" + contenido,
                StandardCharsets.UTF_8);
    }

    /** Escapa CSV y neutraliza posibles formulas de hojas de calculo. */
    static String campo(String valor) {
        if (valor == null) {
            return "";
        }

        int indice = 0;

        while (indice < valor.length()) {
            int punto = valor.codePointAt(indice);

            if (!Character.isWhitespace(punto)
                    && !Character.isISOControl(punto)
                    && punto != 0xFEFF) {
                break;
            }

            indice += Character.charCount(punto);
        }

        boolean formula = indice < valor.length()
                && "=+-@".indexOf(valor.charAt(indice)) >= 0;

        boolean controlInicial = !valor.isEmpty()
                && (Character.isISOControl(valor.charAt(0))
                    || valor.charAt(0) == '\uFEFF');

        String seguro = formula || controlInicial
                ? "'" + valor : valor;

        if (seguro.indexOf(';') >= 0
                || seguro.indexOf('"') >= 0
                || seguro.indexOf('\r') >= 0
                || seguro.indexOf('\n') >= 0) {

            return '"' + seguro.replace("\"", "\"\"") + '"';
        }

        return seguro;
    }
}
