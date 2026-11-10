package org.example.carestock.service;

import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.session.SesionUsuario;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExportacionReposicionCasosLimiteTest {

    private static RegistroReposicion registro(
            Categoria categoria, int id, String codigo,
            String nombre, int existencias, int minimo) {

        return new RegistroReposicion(
                categoria, id, codigo, nombre,
                existencias, minimo,
                Math.max(0, minimo - existencias),
                existencias == 0
                        ? Severidad.CRITICA
                        : Severidad.ADVERTENCIA);
    }

    private static SesionUsuario.DatosUsuario usuario(
            int id, String rol, Integer farmacia) {

        return new SesionUsuario.DatosUsuario(
                id, "Usuario QA", "qa@test.local",
                rol, farmacia, "Farmacia QA");
    }

    private static MockedStatic<SesionUsuario> sesion(
            SesionUsuario.DatosUsuario usuario) {

        MockedStatic<SesionUsuario> mock =
                mockStatic(SesionUsuario.class);

        mock.when(SesionUsuario::estaActiva)
                .thenReturn(true);
        mock.when(SesionUsuario::actual)
                .thenReturn(usuario);

        return mock;
    }

    @Test
    void filtroSinRestriccionesConservaRegistrosValidos() {
        var aseo = registro(
                Categoria.ASEO, 1, "A1", "Jabon", 0, 5);
        var medicamento = registro(
                Categoria.MEDICAMENTO, 2, "M2",
                "Acetaminofen", 2, 5);

        var entrada = Arrays.asList(null, aseo, medicamento);

        assertEquals(
                List.of(aseo, medicamento),
                FiltroReposicion.aplicar(
                        entrada, null, null, null));
    }

    @Test
    void filtroBuscaPorCodigoYNombreSinImportarMayusculas() {
        var medicamento = registro(
                Categoria.MEDICAMENTO, 1,
                "MED-ABC", "Acetaminofen", 0, 5);

        var aseo = registro(
                Categoria.ASEO, 2,
                "ASE-XYZ", "Jabon", 1, 4);

        List<RegistroReposicion> datos =
                List.of(medicamento, aseo);

        assertEquals(
                List.of(medicamento),
                FiltroReposicion.aplicar(
                        datos, null, null, "med-abc"));

        assertEquals(
                List.of(aseo),
                FiltroReposicion.aplicar(
                        datos, null, null, "JABON"));

        assertTrue(
                FiltroReposicion.aplicar(
                        datos, null, null, "inexistente")
                        .isEmpty());
    }

    @Test
    void filtrosCombinadosExcluyenSeveridadIncorrecta() {
        var agotado = registro(
                Categoria.ASEO, 1, "A1", "Jabon", 0, 5);
        var bajo = registro(
                Categoria.ASEO, 2, "A2", "Shampoo", 2, 5);

        assertEquals(
                List.of(agotado),
                FiltroReposicion.aplicar(
                        List.of(agotado, bajo),
                        Categoria.ASEO,
                        Severidad.CRITICA, ""));

        assertTrue(
                FiltroReposicion.aplicar(
                        List.of(agotado, bajo),
                        Categoria.MATERNIDAD,
                        Severidad.CRITICA, "")
                        .isEmpty());
    }

    @Test
    void filtroRechazaConsultaNula() {
        assertThrows(
                NullPointerException.class,
                () -> FiltroReposicion.aplicar(
                        null, null, null, null));
    }

    @Test
    void exportacionExigeSesionActiva() {
        try (var mock = mockStatic(SesionUsuario.class)) {
            mock.when(SesionUsuario::estaActiva)
                    .thenReturn(false);

            assertThrows(
                    SecurityException.class,
                    PermisosExportacionReposicion
                            ::exigirAdministrador);

            mock.verify(
                    SesionUsuario::actual, never());
        }
    }

    @Test
    void administradorSinFarmaciaValidaNoPuedeExportar() {
        for (Integer farmacia : Arrays.asList(null, 0, -1)) {
            try (var mock = sesion(
                    usuario(1, "ADMINISTRADOR", farmacia))) {

                assertThrows(
                        SecurityException.class,
                        PermisosExportacionReposicion
                                ::exigirAdministrador);
            }
        }
    }

    @Test
    void validaMismaSesionYDetectaCambioDeUsuario() {
        var original = usuario(
                1, "ADMINISTRADOR", 7);

        try (var mock = sesion(
                usuario(1, "ADMINISTRADOR", 7))) {

            assertDoesNotThrow(
                    () -> PermisosExportacionReposicion
                            .comprobarMismaSesion(original));
        }

        try (var mock = sesion(
                usuario(2, "ADMINISTRADOR", 7))) {

            assertThrows(
                    SecurityException.class,
                    () -> PermisosExportacionReposicion
                            .comprobarMismaSesion(original));
        }

        assertThrows(
                NullPointerException.class,
                () -> PermisosExportacionReposicion
                        .comprobarMismaSesion(null));
    }

    @Test
    void sinRegistrosNoAbreSelectorNiConsultaInventario()
            throws Exception {

        var servicio = mock(
                ServicioPlanificacionReposicion.class);

        var selectorInvocado = new AtomicBoolean(false);

        try (var mock = sesion(
                usuario(1, "ADMINISTRADOR", 7))) {

            var resultado =
                    new AccionExportarReposicion(servicio)
                            .ejecutar(
                                    List.of(),
                                    () -> {
                                        selectorInvocado.set(true);
                                        return Path.of("no.csv");
                                    });

            assertEquals(
                    AccionExportarReposicion.Resultado
                            .SIN_REGISTROS,
                    resultado);
        }

        assertFalse(selectorInvocado.get());
        verifyNoInteractions(servicio);
    }

    @Test
    void cancelarSelectorNoConsultaNiGeneraArchivo()
            throws Exception {

        var servicio = mock(
                ServicioPlanificacionReposicion.class);

        var fila = registro(
                Categoria.ASEO, 1, "A1",
                "Jabon", 0, 5);

        try (var mock = sesion(
                usuario(1, "ADMINISTRADOR", 7))) {

            var resultado =
                    new AccionExportarReposicion(servicio)
                            .ejecutar(
                                    List.of(fila),
                                    () -> null);

            assertEquals(
                    AccionExportarReposicion.Resultado
                            .CANCELADO,
                    resultado);
        }

        verifyNoInteractions(servicio);
    }

    @Test
    void csvValidaArgumentosYCaracteresEspeciales() {
        assertEquals(
                "",
                ExportadorReposicionCsv.campo(null));

        assertEquals(
                "\"Texto; especial\"",
                ExportadorReposicionCsv.campo(
                        "Texto; especial"));

        assertEquals(
                "\"Dijo \"\"hola\"\"\"",
                ExportadorReposicionCsv.campo(
                        "Dijo \"hola\""));

        assertThrows(
                NullPointerException.class,
                () -> ExportadorReposicionCsv
                        .generar(null));

        assertThrows(
                NullPointerException.class,
                () -> ExportadorReposicionCsv
                        .generar(Arrays.asList(
                                (RegistroReposicion) null)));
    }
}
