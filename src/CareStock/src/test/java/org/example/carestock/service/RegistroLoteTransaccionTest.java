package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Lote;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RegistroLoteTransaccionTest {

    private Lote lote() {
        return new Lote(
                "LOTE-TX-001",
                15,
                20,
                LocalDate.now().plusDays(90),
                2,
                "DISPONIBLE",
                9
        );
    }

    @Test
    void registraLoteYConfirmaTransaccion() throws Exception {
        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenAnswer(inv -> {
                    PreparedStatement ps =
                            mock(PreparedStatement.class);
                    ResultSet rs = mock(ResultSet.class);

                    when(ps.executeQuery()).thenReturn(rs);
                    when(rs.next()).thenReturn(true);
                    when(rs.getInt(1)).thenReturn(88);
                    when(ps.executeUpdate()).thenReturn(1);

                    return ps;
                });

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            Lote registrado = new RegistroLoteService()
                    .registrarMedicamentoLote(lote(), 9, 7);

            assertEquals(88, registrado.getIdLote());
            assertEquals(9, registrado.getIdUsuario());

            verify(con).setAutoCommit(false);
            verify(con).commit();
            verify(con, never()).rollback();
            verify(con).setAutoCommit(true);

            verify(con).prepareStatement(
                    contains("INSERT INTO log_movimientos"));
        }
    }

    @Test
    void revierteSiMedicamentoNoPerteneceAFarmacia()
            throws Exception {

        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertThrows(ReglaNegocioException.class,
                    () -> new RegistroLoteService()
                            .registrarMedicamentoLote(lote(), 9, 7));

            verify(con).rollback();
            verify(con, never()).commit();
            verify(con).setAutoCommit(true);
        }
    }

    @Test
    void revierteSiFallaActualizacionDeStock()
            throws Exception {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenAnswer(inv -> {
                    String sql = inv.getArgument(0);
                    PreparedStatement ps =
                            mock(PreparedStatement.class);
                    ResultSet rs = mock(ResultSet.class);

                    when(ps.executeQuery()).thenReturn(rs);
                    when(rs.next()).thenReturn(true);
                    when(rs.getInt(1)).thenReturn(88);

                    if (sql.contains("UPDATE medicamentos")) {
                        when(ps.executeUpdate()).thenReturn(0);
                    } else {
                        when(ps.executeUpdate()).thenReturn(1);
                    }

                    return ps;
                });

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertThrows(SQLException.class,
                    () -> new RegistroLoteService()
                            .registrarMedicamentoLote(lote(), 9, 7));

            verify(con).rollback();
            verify(con, never()).commit();
            verify(con).setAutoCommit(true);
        }
    }
}
