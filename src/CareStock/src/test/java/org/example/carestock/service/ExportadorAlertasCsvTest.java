package org.example.carestock.service;

import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportadorAlertasCsvTest {

    private static AlertaInventario stock(String producto, int existencias, int minimo) {
        return new AlertaInventario(
                TipoAlerta.STOCK_MINIMO, Categoria.ASEO,
                1, "ASE-001", producto, null,
                existencias, minimo, null, null,
                existencias == 0 ? Severidad.CRITICA : Severidad.ADVERTENCIA,
                null, null, null);
    }

    private static AlertaInventario vencimiento() {
        return new AlertaInventario(
                TipoAlerta.VENCIMIENTO, Categoria.MEDICAMENTO,
                7, "MED-007", "Acetaminofen", "L-77",
                12, null, LocalDate.of(2026, 11, 10), 8,
                Severidad.CRITICA, null, null, null);
    }

    @Test
    void conListaVaciaDevuelveSoloElEncabezado() {
        assertEquals(ExportadorAlertasCsv.ENCABEZADO + "\n",
                ExportadorAlertasCsv.generar(List.of()));
    }

    @Test
    void encabezadoTieneLasSieteColumnas() {
        assertEquals(
                "Severidad;Tipo;Categoría;Producto;Referencia;Existencias;Detalle",
                ExportadorAlertasCsv.ENCABEZADO);
    }

    @Test
    void generaUnaLineaPorAlertaEnElOrdenDeLasColumnas() {
        String[] lineas = ExportadorAlertasCsv
                .generar(List.of(stock("Jabon", 0, 5), vencimiento()))
                .split("\n");

        assertEquals(3, lineas.length);
        assertEquals(
                "Crítica;Stock mínimo;Aseo;Jabon;ASE-001;0;Sin existencias (mínimo 5)",
                lineas[1]);
        assertEquals(
                "Crítica;Vencimiento;Medicamento;Acetaminofen;Lote L-77;12;"
                        + "Vence en 8 días (10/11/2026)",
                lineas[2]);
    }

    @Test
    void escapaSeparadorYComillas() {
        assertEquals("\"Crema; 50 ml\"", ExportadorAlertasCsv.campo("Crema; 50 ml"));
        assertEquals("\"Jabon \"\"extra\"\"\"", ExportadorAlertasCsv.campo("Jabon \"extra\""));
        assertEquals("\"linea1\nlinea2\"", ExportadorAlertasCsv.campo("linea1\nlinea2"));
    }

    @Test
    void unProductoConSeparadorNoRompeLasColumnas() {
        String linea = ExportadorAlertasCsv
                .generar(List.of(stock("Crema; 50 ml", 2, 5)))
                .split("\n")[1];

        assertTrue(linea.contains(";\"Crema; 50 ml\";"));
    }

    @Test
    void valorNuloQuedaVacio() {
        assertEquals("", ExportadorAlertasCsv.campo(null));
        assertEquals("", ExportadorAlertasCsv.campo(""));
    }

    @Test
    void neutralizaTextosQueExcelTomaComoFormula() {
        assertEquals("'=SUMA(A1)", ExportadorAlertasCsv.campo("=SUMA(A1)"));
        assertEquals("'@cmd", ExportadorAlertasCsv.campo("@cmd"));
        assertEquals("'-5", ExportadorAlertasCsv.campo("-5"));
        assertEquals("'+1", ExportadorAlertasCsv.campo("+1"));
    }

    @Test
    void textoNormalNoSeModifica() {
        assertEquals("Acetaminofen", ExportadorAlertasCsv.campo("Acetaminofen"));
    }

    @Test
    void listaNulaSeRechaza() {
        assertThrows(NullPointerException.class, () -> ExportadorAlertasCsv.generar(null));
    }

    @Test
    void guardaElArchivoEnUtf8ConMarcaParaExcel(@TempDir Path carpeta) throws IOException {
        Path destino = carpeta.resolve("alertas.csv");

        ExportadorAlertasCsv.guardar(destino, List.of(stock("Jabón", 0, 5)));

        String contenido = Files.readString(destino, StandardCharsets.UTF_8);
        assertTrue(contenido.startsWith("﻿Severidad;Tipo;Categoría"));
        assertTrue(contenido.contains("Jabón"));
        assertEquals(2, contenido.split("\n").length);
    }

    @Test
    void guardarSinDestinoSeRechaza() {
        assertThrows(NullPointerException.class,
                () -> ExportadorAlertasCsv.guardar(null, List.of()));
    }
}
