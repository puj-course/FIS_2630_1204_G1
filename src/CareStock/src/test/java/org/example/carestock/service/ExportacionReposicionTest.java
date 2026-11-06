package org.example.carestock.service;

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.proxy.ConsultaInventario;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExportacionReposicionTest {

    @TempDir Path temporal;

    private static RegistroReposicion registro(
            Categoria categoria, int id, String codigo,
            String nombre, int stock, int minimo) {

        return new RegistroReposicion(
                categoria, id, codigo, nombre, stock, minimo,
                Math.max(0, minimo - stock),
                stock == 0
                        ? Severidad.CRITICA
                        : Severidad.ADVERTENCIA);
    }

    private static SesionUsuario.DatosUsuario sesion(
            String rol, Integer farmacia) {

        return new SesionUsuario.DatosUsuario(
                1, "QA", "qa@test.local",
                rol, farmacia, "Farmacia QA");
    }

    @Test
    void csvContieneLasSieteColumnasYValoresCorrectos() {
        var fila = registro(
                Categoria.ASEO, 5, "A-5", "Jabón", 2, 8);

        String csv = ExportadorReposicionCsv.generar(
                List.of(fila));

        assertEquals(
                ExportadorReposicionCsv.ENCABEZADO + "\n"
                + "A-5;Jabón;Aseo;2;8;6;Advertencia\n",
                csv);
    }

    @Test
    void protegeFormulasYEscapaCaracteresEspeciales() {
        var fila = registro(
                Categoria.MATERNIDAD, 5, "=1+1",
                "Producto; \"especial\"\nX", 0, 3);

        String csv = ExportadorReposicionCsv.generar(
                List.of(fila));

        assertTrue(csv.contains(
                "'=1+1;\"Producto; \"\"especial\"\"\nX\""));

        for (String texto : List.of(
                "+SUM(1;2)", "  -5", "\t@cmd", "\n=2+2")) {

            String campoCsv =
                    ExportadorReposicionCsv.campo(texto);

            String valor = campoCsv;

            // Interpretar las comillas y escapes del CSV.
            if (campoCsv.length() >= 2
                    && campoCsv.startsWith("\"")
                    && campoCsv.endsWith("\"")) {

                valor = campoCsv
                        .substring(1, campoCsv.length() - 1)
                        .replace("\"\"", "\"");
            }

            // El valor real debe empezar con un apostrofo
            // para neutralizar formulas.
            assertEquals(
                    "'" + texto,
                    valor,
                    "El campo CSV debe proteger el texto");
        }
    }

    @Test
    void guardaUtf8ConBom() throws Exception {
        Path destino = temporal.resolve("reposicion.csv");

        ExportadorReposicionCsv.guardar(
                destino,
                List.of(registro(
                        Categoria.MEDICAMENTO, 1,
                        "MED-1", "Ácido", 0, 5)));

        byte[] bytes = Files.readAllBytes(destino);

        assertEquals((byte) 0xEF, bytes[0]);
        assertEquals((byte) 0xBB, bytes[1]);
        assertEquals((byte) 0xBF, bytes[2]);

        assertTrue(Files.readString(
                destino, StandardCharsets.UTF_8)
                .contains("Ácido"));
    }

    @Test
    void noGeneraCsvSinRegistros() {
        Path destino = temporal.resolve("vacio.csv");

        assertThrows(IllegalArgumentException.class,
                () -> ExportadorReposicionCsv.guardar(
                        destino, List.of()));

        assertFalse(Files.exists(destino));
    }

    @Test
    void respetaCategoriaSeveridadYBusqueda() {
        var aseo = registro(
                Categoria.ASEO, 1, "MISMO", "Jabón", 1, 4);
        var medicamento = registro(
                Categoria.MEDICAMENTO, 1,
                "MISMO", "Paracetamol", 0, 4);

        var originales = List.of(aseo, medicamento);

        var resultado = FiltroReposicion.aplicar(
                originales, Categoria.ASEO,
                Severidad.ADVERTENCIA, "jab");

        assertEquals(List.of(aseo), resultado);
        assertEquals(2, originales.size());
    }

    @Test
    void usuarioSinPermisosNoPuedeExportar() {
        var fila = registro(
                Categoria.ASEO, 1, "A1", "Jabón", 0, 3);

        try (var mockSesion = mockStatic(SesionUsuario.class)) {
            mockSesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            mockSesion.when(SesionUsuario::actual)
                    .thenReturn(sesion("AUXILIAR", 7));

            var servicio = mock(
                    ServicioPlanificacionReposicion.class);

            assertThrows(SecurityException.class,
                    () -> new AccionExportarReposicion(servicio)
                            .ejecutar(
                                    List.of(fila),
                                    () -> temporal.resolve("no.csv")));

            verifyNoInteractions(servicio);
        }
    }

    @Test
    void cambioDeFarmaciaCancelaExportacion() {
        var fila = registro(
                Categoria.ASEO, 1, "A1", "Jabón", 0, 3);
        Path destino = temporal.resolve("fuga.csv");

        try (var mockSesion = mockStatic(SesionUsuario.class)) {
            mockSesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);

            mockSesion.when(SesionUsuario::actual)
                    .thenReturn(
                            sesion("ADMINISTRADOR", 7),
                            sesion("ADMINISTRADOR", 8));

            var servicio = mock(
                    ServicioPlanificacionReposicion.class);

            assertThrows(SecurityException.class,
                    () -> new AccionExportarReposicion(servicio)
                            .ejecutar(List.of(fila), () -> destino));
        }

        assertFalse(Files.exists(destino));
    }

    @Test
    void filtrosExportanSoloLosProductosVisibles()
            throws Exception {

        var aseo = registro(
                Categoria.ASEO, 1, "A1", "Jabón", 1, 3);
        var maternidad = registro(
                Categoria.MATERNIDAD, 2,
                "M2", "Pañales", 0, 2);

        var servicio = mock(
                ServicioPlanificacionReposicion.class);

        when(servicio.listarPorFarmacia(7))
                .thenReturn(List.of(aseo, maternidad));

        Path destino = temporal.resolve("filtrados.csv");

        try (var mockSesion = mockStatic(SesionUsuario.class)) {
            mockSesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            mockSesion.when(SesionUsuario::actual)
                    .thenReturn(sesion("ADMINISTRADOR", 7));

            var visibles = FiltroReposicion.aplicar(
                    List.of(aseo, maternidad),
                    Categoria.ASEO, null, "");

            assertEquals(
                    AccionExportarReposicion.Resultado.EXPORTADO,
                    new AccionExportarReposicion(servicio)
                            .ejecutar(visibles, () -> destino));
        }

        String contenido = Files.readString(destino);

        assertTrue(contenido.contains("Jabón"));
        assertFalse(contenido.contains("Pañales"));

        verify(servicio).listarPorFarmacia(7);
    }

    @Test
    void datosObsoletosImpidenExportacion() throws Exception {
        var fila = registro(
                Categoria.ASEO, 1, "A1", "Jabón", 0, 3);

        var servicio = mock(
                ServicioPlanificacionReposicion.class);

        // El producto ya no requiere reposicion.
        when(servicio.listarPorFarmacia(7))
                .thenReturn(List.of());

        Path destino = temporal.resolve("obsoleto.csv");

        try (var mockSesion = mockStatic(SesionUsuario.class)) {
            mockSesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            mockSesion.when(SesionUsuario::actual)
                    .thenReturn(sesion("ADMINISTRADOR", 7));

            assertThrows(IllegalStateException.class,
                    () -> new AccionExportarReposicion(servicio)
                            .ejecutar(List.of(fila), () -> destino));
        }

        assertFalse(Files.exists(destino));
    }

    @Test
    void nuevaConsultaDespuesDeEntradaActualizaReposicion()
            throws Exception {

        int farmacia = 7;

        var alertas = mock(ServicioAlertasInventario.class);
        var inventario = mock(ConsultaInventario.class);

        var alerta = new AlertaInventario(
                TipoAlerta.STOCK_MINIMO,
                Categoria.ASEO, 1, "A1", "Jabón",
                null, 0, 3, null, null,
                Severidad.CRITICA, null, null, null);

        // Simula el estado antes y despues de una entrada.
        when(alertas.listar(farmacia))
                .thenReturn(List.of(alerta), List.of());

        when(inventario.listarPorFarmacia(farmacia))
                .thenReturn(List.of(
                        new ProductoInventario(
                                1, "Aseo", "A1", "Jabón",
                                "Unidad", 0, "ACTIVO")));

        var servicio = new ServicioPlanificacionReposicion(
                alertas, inventario);

        try (var mockSesion = mockStatic(SesionUsuario.class)) {
            mockSesion.when(SesionUsuario::estaActiva)
                    .thenReturn(true);
            mockSesion.when(SesionUsuario::actual)
                    .thenReturn(sesion("ADMINISTRADOR", farmacia));

            var antes = servicio.listarPorFarmacia(farmacia);

            assertEquals(1, antes.size());
            assertEquals(3, antes.get(0).cantidadFaltante());

            var despues = servicio.listarPorFarmacia(farmacia);

            assertTrue(despues.isEmpty());
        }

        verify(alertas, times(2)).listar(farmacia);
    }
}
