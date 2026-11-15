package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.AjusteExistencias;
import org.example.carestock.model.AjusteExistencias.Categoria;
import org.example.carestock.proxy.InventarioConsultas;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class ServicioAjustesJdbcCoverage87Test {

    private static final int USUARIO = 10;
    private static final int FARMACIA = 7;
    private static final int REFERENCIA = 25;

    private static AjusteExistencias ajuste(Categoria categoria) {
        return new AjusteExistencias(
            categoria, REFERENCIA, USUARIO, FARMACIA,
            "Conteo físico QA", 5, 9);
    }

    private static final class Escenario implements AutoCloseable {
        final Connection con = mock(Connection.class);

        final PreparedStatement permiso = mock(PreparedStatement.class);
        final PreparedStatement medicamento = mock(PreparedStatement.class);
        final PreparedStatement lote = mock(PreparedStatement.class);
        final PreparedStatement producto = mock(PreparedStatement.class);
        final PreparedStatement actualizar = mock(PreparedStatement.class);
        final PreparedStatement auditoria = mock(PreparedStatement.class);

        final ResultSet rsPermiso = mock(ResultSet.class);
        final ResultSet rsMedicamento = mock(ResultSet.class);
        final ResultSet rsLote = mock(ResultSet.class);
        final ResultSet rsProducto = mock(ResultSet.class);
        final ResultSet rsAuditoria = mock(ResultSet.class);

        final MockedStatic<SesionUsuario> sesion;
        final MockedStatic<ConexionBD> db;
        final MockedStatic<InventarioConsultas> cache;

        Escenario() throws Exception {
            when(con.prepareStatement(anyString())).thenAnswer(inv -> {
                String sql = inv.getArgument(0, String.class);

                if (sql.contains("FROM usuarios u")) {
                    return permiso;
                }
                if (sql.contains("SELECT m.id_medicamento")) {
                    return medicamento;
                }
                if (sql.contains("SELECT l.cantidad_actual")) {
                    return lote;
                }
                if (sql.contains("SELECT stock FROM")) {
                    return producto;
                }
                if (sql.contains("INSERT INTO auditoria_ajustes")) {
                    return auditoria;
                }
                if (sql.contains("UPDATE ")) {
                    return actualizar;
                }
                throw new AssertionError("SQL no reconocido: " + sql);
            });

            when(con.getAutoCommit()).thenReturn(false);

            when(permiso.executeQuery()).thenReturn(rsPermiso);
            when(rsPermiso.next()).thenReturn(true);

            when(medicamento.executeQuery())
                .thenReturn(rsMedicamento);
            when(rsMedicamento.next()).thenReturn(true);

            when(lote.executeQuery()).thenReturn(rsLote);
            when(rsLote.next()).thenReturn(true);
            when(rsLote.getInt(1)).thenReturn(5);

            when(producto.executeQuery()).thenReturn(rsProducto);
            when(rsProducto.next()).thenReturn(true);
            when(rsProducto.getInt(1)).thenReturn(5);

            when(actualizar.executeUpdate()).thenReturn(1);

            when(auditoria.executeQuery()).thenReturn(rsAuditoria);
            when(rsAuditoria.next()).thenReturn(true);
            when(rsAuditoria.getLong(1)).thenReturn(177L);

            sesion = mockStatic(SesionUsuario.class);
            sesion.when(SesionUsuario::estaActiva).thenReturn(true);
            sesion.when(SesionUsuario::actual).thenReturn(
                new SesionUsuario.DatosUsuario(
                    USUARIO, "Administrador QA", "qa@test.local",
                    "ADMINISTRADOR", FARMACIA, "Farmacia QA"));

            db = mockStatic(ConexionBD.class);
            db.when(ConexionBD::getConexion).thenReturn(con);

            cache = mockStatic(InventarioConsultas.class);
        }

        ServicioAjustesInventario.ResultadoAjuste aplicar(
                Categoria categoria) throws Exception {
            return new ServicioAjustesInventario()
                .ajustar(ajuste(categoria));
        }

        void rollbackSinCache() throws Exception {
            verify(con).rollback();
            verify(con, never()).commit();
            verify(con).close();
            cache.verifyNoInteractions();
        }

        @Override
        public void close() {
            cache.close();
            db.close();
            sesion.close();
        }
    }

    @ParameterizedTest
    @EnumSource(Categoria.class)
    void ajusteCompletoConfirmaYRegistraAuditoria(
            Categoria categoria) throws Exception {

        try (Escenario e = new Escenario()) {
            var resultado = e.aplicar(categoria);

            assertAll(
                () -> assertEquals(177L, resultado.idAuditoria()),
                () -> assertEquals(categoria, resultado.categoria()),
                () -> assertEquals(REFERENCIA, resultado.idReferencia()),
                () -> assertEquals(5, resultado.anterior()),
                () -> assertEquals(9, resultado.nuevo()),
                () -> assertEquals(4, resultado.diferencia()),
                () -> assertEquals(USUARIO, resultado.idUsuario()),
                () -> assertEquals(FARMACIA, resultado.idFarmacia())
            );

            verify(e.actualizar).setInt(1, 9);
            verify(e.auditoria).setInt(4, USUARIO);
            verify(e.auditoria).setInt(5, FARMACIA);
            verify(e.auditoria).setInt(7, 5);
            verify(e.auditoria).setInt(8, 9);
            verify(e.auditoria).setInt(9, 4);

            if (categoria == Categoria.MEDICAMENTO) {
                verify(e.medicamento).executeQuery();
                verify(e.lote).executeQuery();
                verify(e.actualizar).setInt(2, 9);
                verify(e.auditoria).setInt(1, REFERENCIA);
            } else if (categoria == Categoria.ASEO) {
                verify(e.producto).executeQuery();
                verify(e.auditoria).setInt(2, REFERENCIA);
            } else {
                verify(e.producto).executeQuery();
                verify(e.auditoria).setInt(3, REFERENCIA);
            }

            verify(e.con).commit();
            verify(e.con, never()).rollback();

            e.cache.verify(() ->
                InventarioConsultas.invalidarFarmacia(FARMACIA));
        }
    }

    @Test
    void permisoRevocadoAntesDeBloquearStock()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsPermiso.next()).thenReturn(false);

            assertThrows(ReglaNegocioException.class,
                () -> e.aplicar(Categoria.ASEO));

            verify(e.producto, never()).executeQuery();
            e.rollbackSinCache();
        }
    }

    @Test
    void medicamentoDeOtraFarmaciaSeRechaza()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsMedicamento.next()).thenReturn(false);

            assertThrows(ReglaNegocioException.class,
                () -> e.aplicar(Categoria.MEDICAMENTO));

            verify(e.lote, never()).executeQuery();
            e.rollbackSinCache();
        }
    }

    @Test
    void loteVencidoOInexistenteSeRechaza()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsLote.next()).thenReturn(false);

            assertThrows(ReglaNegocioException.class,
                () -> e.aplicar(Categoria.MEDICAMENTO));

            verify(e.actualizar, never()).executeUpdate();
            e.rollbackSinCache();
        }
    }

    @Test
    void productoDeOtraFarmaciaSeRechaza()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsProducto.next()).thenReturn(false);

            assertThrows(ReglaNegocioException.class,
                () -> e.aplicar(Categoria.MATERNIDAD));

            e.rollbackSinCache();
        }
    }

    @Test
    void cambioConcurrenteDeExistenciasRevierte()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsProducto.getInt(1)).thenReturn(4);

            assertThrows(ReglaNegocioException.class,
                () -> e.aplicar(Categoria.ASEO));

            verify(e.actualizar, never()).executeUpdate();
            e.rollbackSinCache();
        }
    }

    @Test
    void updateSinFilasNoGeneraAuditoria()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.actualizar.executeUpdate()).thenReturn(0);

            assertThrows(SQLException.class,
                () -> e.aplicar(Categoria.MATERNIDAD));

            verify(e.auditoria, never()).executeQuery();
            e.rollbackSinCache();
        }
    }

    @Test
    void loteConUpdateFallidoHaceRollback()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.actualizar.executeUpdate()).thenReturn(0);

            assertThrows(SQLException.class,
                () -> e.aplicar(Categoria.MEDICAMENTO));

            e.rollbackSinCache();
        }
    }

    @Test
    void errorJdbcAlActualizarNoConfirma()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.actualizar.executeUpdate())
                .thenThrow(new SQLException("Error de stock QA"));

            SQLException error = assertThrows(SQLException.class,
                () -> e.aplicar(Categoria.ASEO));

            assertEquals("Error de stock QA", error.getMessage());
            e.rollbackSinCache();
        }
    }

    @Test
    void auditoriaSinIdRevierteCambioDeStock()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsAuditoria.next()).thenReturn(false);

            assertThrows(SQLException.class,
                () -> e.aplicar(Categoria.MEDICAMENTO));

            verify(e.actualizar).executeUpdate();
            e.rollbackSinCache();
        }
    }

    @Test
    void auditoriaRequiereTransaccionActiva()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.con.getAutoCommit()).thenReturn(true);

            assertThrows(ReglaNegocioException.class,
                () -> e.aplicar(Categoria.ASEO));

            verify(e.auditoria, never()).executeQuery();
            e.rollbackSinCache();
        }
    }

    @Test
    void sesionRevocadaAntesDeAuditoriaRevierte()
            throws Exception {

        try (Escenario e = new Escenario()) {
            e.sesion.when(SesionUsuario::estaActiva)
                .thenReturn(true, false);

            assertThrows(ReglaNegocioException.class,
                () -> e.aplicar(Categoria.ASEO));

            verify(e.actualizar).executeUpdate();
            e.rollbackSinCache();
        }
    }

    @Test
    void errorDeCommitIntentaRevertirOperacion()
            throws Exception {

        try (Escenario e = new Escenario()) {
            doThrow(new SQLException("Commit fallido"))
                .when(e.con).commit();

            assertThrows(SQLException.class,
                () -> e.aplicar(Categoria.ASEO));

            verify(e.con).commit();
            verify(e.con).rollback();
            e.cache.verifyNoInteractions();
        }
    }

    @Test
    void rollbackFallidoConservaExcepcionOriginal()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsPermiso.next()).thenReturn(false);
            doThrow(new SQLException("Rollback fallido"))
                .when(e.con).rollback();

            ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class,
                    () -> e.aplicar(Categoria.ASEO));

            assertEquals(1, error.getSuppressed().length);
            assertEquals("Rollback fallido",
                error.getSuppressed()[0].getMessage());

            e.cache.verifyNoInteractions();
        }
    }

    @Test
    void fachadaRechazaSesionInactivaSinConexion()
            throws Exception {

        try (Escenario e = new Escenario()) {
            e.sesion.when(SesionUsuario::estaActiva)
                .thenReturn(false);

            assertThrows(ReglaNegocioException.class,
                () -> new ServicioAjustesInventario().ajustar(
                    Categoria.ASEO, REFERENCIA, 5, 9,
                    "Conteo QA"));

            e.db.verifyNoInteractions();
        }
    }

    @Test
    void fachadaRechazaUsuarioSinFarmacia()
            throws Exception {

        try (Escenario e = new Escenario()) {
            e.sesion.when(SesionUsuario::actual).thenReturn(
                new SesionUsuario.DatosUsuario(
                    USUARIO, "Sin farmacia", "qa@test.local",
                    "ADMINISTRADOR", null, null));

            assertThrows(ReglaNegocioException.class,
                () -> new ServicioAjustesInventario().ajustar(
                    Categoria.MATERNIDAD, REFERENCIA, 5, 9,
                    "Conteo QA"));

            e.db.verifyNoInteractions();
        }
    }

    @Test
    void rolAuxiliarNoPuedeModificarExistencias()
            throws Exception {

        try (Escenario e = new Escenario()) {
            e.sesion.when(SesionUsuario::actual).thenReturn(
                new SesionUsuario.DatosUsuario(
                    USUARIO, "Auxiliar QA", "qa@test.local",
                    "AUXILIAR", FARMACIA, "Farmacia QA"));

            assertThrows(ReglaNegocioException.class,
                () -> e.aplicar(Categoria.ASEO));

            e.db.verifyNoInteractions();
        }
    }

    @Test
    void cambioDeUsuarioBloqueaAntesDeConectar()
            throws Exception {

        try (Escenario e = new Escenario()) {
            e.sesion.when(SesionUsuario::actual).thenReturn(
                new SesionUsuario.DatosUsuario(
                    99, "Otro usuario", "otro@test.local",
                    "ADMINISTRADOR", FARMACIA, "Farmacia QA"));

            assertThrows(ReglaNegocioException.class,
                () -> e.aplicar(Categoria.ASEO));

            e.db.verifyNoInteractions();
        }
    }

    @Test
    void cambioDeFarmaciaBloqueaAntesDeConectar()
            throws Exception {

        try (Escenario e = new Escenario()) {
            e.sesion.when(SesionUsuario::actual).thenReturn(
                new SesionUsuario.DatosUsuario(
                    USUARIO, "Administrador QA", "qa@test.local",
                    "ADMINISTRADOR", 99, "Otra farmacia"));

            assertThrows(ReglaNegocioException.class,
                () -> e.aplicar(Categoria.ASEO));

            e.db.verifyNoInteractions();
        }
    }
}
