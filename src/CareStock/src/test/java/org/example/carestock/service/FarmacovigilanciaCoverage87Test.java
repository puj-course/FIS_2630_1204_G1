package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.utils.FechaNegocio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class FarmacovigilanciaCoverage87Test {

    private static final int LOTE = 35;
    private static final int FARMACIA = 7;
    private static final int USUARIO = 13;

    private static class Escenario implements AutoCloseable {
        final Connection con = mock(Connection.class);
        final PreparedStatement permiso = mock(PreparedStatement.class);
        final PreparedStatement lote = mock(PreparedStatement.class);
        final PreparedStatement actualizarLote = mock(PreparedStatement.class);
        final PreparedStatement actualizarMedicamento =
                mock(PreparedStatement.class);
        final PreparedStatement historial = mock(PreparedStatement.class);

        final ResultSet rsUsuario = mock(ResultSet.class);
        final ResultSet rsLote = mock(ResultSet.class);

        String roles = "1, 2, 3";

        final FarmacovigilanciaService servicio =
            new FarmacovigilanciaService() {
                @Override
                protected String rolesPermitidosDispensacion() {
                    return roles;
                }
            };

        final MockedStatic<ConexionBD> db;

        Escenario() throws Exception {
            when(con.prepareStatement(anyString())).thenAnswer(inv -> {
                String sql = inv.getArgument(0, String.class);

                if (sql.contains("FROM usuarios u")) return permiso;
                if (sql.contains("FROM lotes l") && sql.contains("FOR UPDATE")) return lote;
                if (sql.contains("UPDATE lotes")) return actualizarLote;
                if (sql.contains("UPDATE medicamentos")) {
                    return actualizarMedicamento;
                }
                if (sql.contains("INSERT INTO log_movimientos")) {
                    return historial;
                }

                throw new AssertionError("SQL inesperado: " + sql);
            });

            when(permiso.executeQuery()).thenReturn(rsUsuario);
            when(rsUsuario.next()).thenReturn(true);
            when(rsUsuario.getInt("id_rol")).thenReturn(2);

            when(lote.executeQuery()).thenReturn(rsLote);
            when(rsLote.next()).thenReturn(true);
            when(rsLote.getInt("id_medicamento")).thenReturn(80);
            when(rsLote.getInt("cantidad_actual")).thenReturn(10);
            when(rsLote.getInt("id_farmacia")).thenReturn(FARMACIA);
            when(rsLote.getString("numero_lote"))
                .thenReturn("LOTE-QA");
            when(rsLote.getString("estado_lote"))
                .thenReturn("DISPONIBLE");
            when(rsLote.getString("estado_medicamento"))
                .thenReturn("ACTIVO");
            when(rsLote.getDate("fecha_vencimiento"))
                .thenReturn(Date.valueOf(FechaNegocio.hoy().plusDays(30)));

            when(actualizarLote.executeUpdate()).thenReturn(1);
            when(actualizarMedicamento.executeUpdate()).thenReturn(1);
            when(historial.executeUpdate()).thenReturn(1);

            db = mockStatic(ConexionBD.class);
            db.when(ConexionBD::getConexion).thenReturn(con);
        }

        void dispensar(int cantidad) throws Exception {
            servicio.dispensarMedicamento(
                LOTE, cantidad, FARMACIA, USUARIO);
        }

        void rechaza(String mensaje, int cantidad) throws Exception {
            ReglaNegocioException error = assertThrows(
                ReglaNegocioException.class,
                () -> dispensar(cantidad));

            assertTrue(error.getMessage().contains(mensaje),
                "Mensaje recibido: " + error.getMessage());

            verify(con).rollback();
            verify(con, never()).commit();
            verify(con).setAutoCommit(true);
            verify(con).close();
        }

        void confirma() throws Exception {
            verify(con).setAutoCommit(false);
            verify(con).commit();
            verify(con, never()).rollback();
            verify(con).setAutoCommit(true);
            verify(con).close();
        }

        @Override
        public void close() {
            db.close();
        }
    }

    @Test
    @SuppressWarnings("deprecation")
    void firmaAntiguaNuncaAutorizaDispensacion() {
        var servicio = new FarmacovigilanciaService();

        assertThrows(ReglaNegocioException.class,
            () -> servicio.dispensarMedicamento(1, 1));
    }

    @ParameterizedTest
    @CsvSource({
        "0,1,7,13",
        "-1,1,7,13",
        "35,0,7,13",
        "35,-1,7,13",
        "35,1,0,13",
        "35,1,7,0"
    })
    void rechazaParametrosInvalidosAntesDeConectar(
            int idLote, int cantidad, int farmacia, int usuario) {

        try (MockedStatic<ConexionBD> db =
                 mockStatic(ConexionBD.class)) {

            var servicio = new FarmacovigilanciaService();

            assertThrows(ReglaNegocioException.class,
                () -> servicio.dispensarMedicamento(
                    idLote, cantidad, farmacia, usuario));

            db.verifyNoInteractions();
        }
    }

    @Test
    void bloqueaDispensacionSinConfiguracionDeRoles()
            throws Exception {
        try (Escenario e = new Escenario()) {
            e.roles = null;
            e.rechaza("configure", 2);
            verify(e.permiso, never()).executeQuery();
        }
    }

    @Test
    void bloqueaConfiguracionDeRolesEnBlanco()
            throws Exception {
        try (Escenario e = new Escenario()) {
            e.roles = "   ";
            e.rechaza("configure", 2);
            verify(e.permiso, never()).executeQuery();
        }
    }

    @Test
    void usuarioInactivoNoPuedeDispensar() throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsUsuario.next()).thenReturn(false);
            e.rechaza("Usuario inactivo", 2);
            verify(e.lote, never()).executeQuery();
        }
    }

    @Test
    void rolSinPermisoNoPuedeDispensar() throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsUsuario.getInt("id_rol")).thenReturn(99);
            e.rechaza("rol no permite", 2);
            verify(e.lote, never()).executeQuery();
        }
    }

    @Test
    void loteInexistenteNoCambiaExistencias() throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsLote.next()).thenReturn(false);
            e.rechaza("Lote no encontrado", 2);
            verify(e.actualizarLote, never()).executeUpdate();
        }
    }

    @Test
    void impideDispensarLoteDeOtraFarmacia()
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsLote.getInt("id_farmacia")).thenReturn(99);
            e.rechaza("otra farmacia", 2);
            verify(e.actualizarLote, never()).executeUpdate();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"INACTIVO", "SUSPENDIDO", " "})
    void rechazaMedicamentosNoActivos(String estado)
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsLote.getString("estado_medicamento"))
                .thenReturn(estado);
            e.rechaza("Medicamento inactivo", 2);
        }
    }

    @Test
    void rechazaEstadoDeMedicamentoNulo()
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsLote.getString("estado_medicamento"))
                .thenReturn((String) null);
            e.rechaza("Medicamento inactivo", 2);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"AGOTADO", "VENCIDO", "RETIRADO"})
    void rechazaLotesNoDisponibles(String estado)
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsLote.getString("estado_lote"))
                .thenReturn(estado);
            e.rechaza("Lote no disponible", 2);
        }
    }

    @Test
    void rechazaEstadoDeLoteNulo() throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsLote.getString("estado_lote"))
                .thenReturn((String) null);
            e.rechaza("Lote no disponible", 2);
        }
    }

    @Test
    void rechazaLoteSinFechaDeVencimiento()
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsLote.getDate("fecha_vencimiento"))
                .thenReturn((Date) null);
            e.rechaza("Lote vencido", 2);
        }
    }

    @Test
    void rechazaLoteVencidoAyer() throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsLote.getDate("fecha_vencimiento"))
                .thenReturn(Date.valueOf(FechaNegocio.hoy().minusDays(1)));
            e.rechaza("Lote vencido", 2);
        }
    }

    @Test
    void rechazaLoteQueVenceHoy() throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsLote.getDate("fecha_vencimiento"))
                .thenReturn(Date.valueOf(FechaNegocio.hoy()));
            e.rechaza("Lote vencido", 2);
        }
    }

    @Test
    void impideDispensarMasDelStockDisponible()
            throws Exception {
        try (Escenario e = new Escenario()) {
            e.rechaza("Stock insuficiente", 11);
            verify(e.actualizarLote, never()).executeUpdate();
        }
    }

    @Test
    void dispensacionParcialMantieneLoteDisponible()
            throws Exception {
        try (Escenario e = new Escenario()) {
            e.dispensar(4);

            verify(e.actualizarLote).setInt(1, 6);
            verify(e.actualizarLote).setString(2, "DISPONIBLE");
            verify(e.actualizarLote).setInt(3, USUARIO);
            verify(e.actualizarLote).setInt(4, LOTE);

            verify(e.actualizarMedicamento).setInt(1, USUARIO);
            verify(e.actualizarMedicamento).setInt(2, 80);
            verify(e.actualizarMedicamento).setInt(3, FARMACIA);

            verify(e.historial).setInt(1, USUARIO);
            verify(e.historial).setInt(2, LOTE);
            verify(e.historial).setInt(3, 4);
            verify(e.historial).executeUpdate();

            e.confirma();
        }
    }

    @Test
    void dispensacionTotalMarcaLoteAgotado()
            throws Exception {
        try (Escenario e = new Escenario()) {
            e.dispensar(10);

            verify(e.actualizarLote).setInt(1, 0);
            verify(e.actualizarLote).setString(2, "AGOTADO");

            e.confirma();
        }
    }

    @Test
    void admiteMayusculasYEspaciosEnEstados()
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.rsLote.getString("estado_lote"))
                .thenReturn(" disponible ");
            when(e.rsLote.getString("estado_medicamento"))
                .thenReturn(" activo ");

            e.dispensar(2);
            e.confirma();
        }
    }

    @Test
    void errorAlActualizarLoteEjecutaRollback()
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.actualizarLote.executeUpdate()).thenReturn(0);

            SQLException error = assertThrows(
                SQLException.class, () -> e.dispensar(2));

            assertTrue(error.getMessage().contains("actualizo el lote"));
            verify(e.con).rollback();
            verify(e.con, never()).commit();
        }
    }

    @Test
    void errorAlActualizarStockTotalEjecutaRollback()
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.actualizarMedicamento.executeUpdate())
                .thenReturn(0);

            SQLException error = assertThrows(
                SQLException.class, () -> e.dispensar(2));

            assertTrue(error.getMessage()
                .contains("actualizar el stock total"));
            verify(e.con).rollback();
            verify(e.historial, never()).executeUpdate();
        }
    }

    @Test
    void errorAlRegistrarMovimientoRevierteDispensacion()
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.historial.executeUpdate())
                .thenThrow(new SQLException("Auditoria no disponible"));

            SQLException error = assertThrows(
                SQLException.class, () -> e.dispensar(3));

            assertEquals("Auditoria no disponible",
                error.getMessage());
            verify(e.con).rollback();
            verify(e.con, never()).commit();
        }
    }

    @Test
    void errorSqlAlConsultarLoteRevierteTransaccion()
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.lote.executeQuery())
                .thenThrow(new SQLException("Bloqueo fallido"));

            assertThrows(SQLException.class, () -> e.dispensar(2));
            verify(e.con).rollback();
            verify(e.actualizarLote, never()).executeUpdate();
        }
    }

    @Test
    void errorSqlAlConsultarPermisoRevierteTransaccion()
            throws Exception {
        try (Escenario e = new Escenario()) {
            when(e.permiso.executeQuery())
                .thenThrow(new SQLException("Permisos no disponibles"));

            assertThrows(SQLException.class, () -> e.dispensar(2));
            verify(e.con).rollback();
            verify(e.lote, never()).executeQuery();
        }
    }

    @Test
    void errorDeCommitIntentaRollback()
            throws Exception {
        try (Escenario e = new Escenario()) {
            doThrow(new SQLException("Commit fallido"))
                .when(e.con).commit();

            SQLException error = assertThrows(
                SQLException.class, () -> e.dispensar(2));

            assertEquals("Commit fallido", error.getMessage());
            verify(e.con).commit();
            verify(e.con).rollback();
        }
    }
}
