package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ControlVencimientosServiceTest {

    @Test
    void rechazaIdentificadoresInvalidos() {
        ControlVencimientosService servicio = new ControlVencimientosService();

        assertThrows(IllegalArgumentException.class,
                () -> servicio.sincronizar(0, 1));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.sincronizar(1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> servicio.sincronizar(-1, -1));
    }

    @Test
    void sinVencimientosConfirmaSinModificarInventario() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement bloqueo = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.getAutoCommit()).thenReturn(true);
        when(con.prepareStatement(anyString())).thenReturn(bloqueo);
        when(bloqueo.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertEquals(0, new ControlVencimientosService().sincronizar(1, 5));

            verify(con).commit();
            verify(con, never()).rollback();
            verify(con).setAutoCommit(false);
            verify(con).setAutoCommit(true);
        }
    }

    @Test
    void venceLotesRegistraAuditoriaYRecalculaStock() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement bloqueo = mock(PreparedStatement.class);
        PreparedStatement caducar = mock(PreparedStatement.class);
        PreparedStatement auditoria = mock(PreparedStatement.class);
        PreparedStatement reconciliar = mock(PreparedStatement.class);

        ResultSet medicamentos = mock(ResultSet.class);
        ResultSet lotes = mock(ResultSet.class);

        when(con.getAutoCommit()).thenReturn(true);
        when(con.prepareStatement(anyString()))
                .thenReturn(bloqueo, caducar, auditoria, reconciliar);

        when(bloqueo.executeQuery()).thenReturn(medicamentos);
        when(medicamentos.next()).thenReturn(true, false);
        when(medicamentos.getInt(1)).thenReturn(10);

        when(caducar.executeQuery()).thenReturn(lotes);
        when(lotes.next()).thenReturn(true, true, false);
        when(lotes.getInt(1)).thenReturn(101, 102);
        when(lotes.getInt(2)).thenReturn(8, 0);

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            int actualizados =
                    new ControlVencimientosService().sincronizar(1, 5);

            assertEquals(2, actualizados);
            verify(auditoria, times(1)).addBatch();
            verify(auditoria).executeBatch();
            verify(reconciliar, times(1)).addBatch();
            verify(reconciliar).executeBatch();
            verify(con).commit();
            verify(con, never()).rollback();
            verify(con).setAutoCommit(true);
        }
    }

    @Test
    void errorSqlProvocaRollback() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement bloqueo = mock(PreparedStatement.class);

        when(con.getAutoCommit()).thenReturn(true);
        when(con.prepareStatement(anyString())).thenReturn(bloqueo);
        when(bloqueo.executeQuery())
                .thenThrow(new SQLException("Error simulado"));

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertThrows(SQLException.class,
                    () -> new ControlVencimientosService().sincronizar(1, 5));

            verify(con).rollback();
            verify(con, never()).commit();
            verify(con).setAutoCommit(true);
        }
    }

    @Test
    void conservaExcepcionOriginalSiFallaRollback() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement bloqueo = mock(PreparedStatement.class);

        when(con.getAutoCommit()).thenReturn(true);
        when(con.prepareStatement(anyString())).thenReturn(bloqueo);
        when(bloqueo.executeQuery())
                .thenThrow(new SQLException("Consulta fallida"));
        doThrow(new SQLException("Rollback fallido"))
                .when(con).rollback();

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            SQLException error = assertThrows(SQLException.class,
                    () -> new ControlVencimientosService().sincronizar(1, 5));

            assertEquals("Consulta fallida", error.getMessage());
            assertEquals(1, error.getSuppressed().length);
            assertEquals("Rollback fallido",
                    error.getSuppressed()[0].getMessage());
        }
    }
}
