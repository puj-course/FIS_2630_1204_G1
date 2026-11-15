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

class RegistroLoteCadenaFrioTest {

    private Lote lote() {
        return new Lote(
                "LOTE-FRIO-001",
                15,
                20,
                LocalDate.now().plusDays(90),
                2,
                "DISPONIBLE",
                9
        );
    }

    private Connection conexion(
            boolean requiereFrio,
            String estante,
            String nivel) throws Exception {

        Connection con = mock(Connection.class);

        when(con.prepareStatement(anyString()))
                .thenAnswer(inv -> {
                    String sql = inv.getArgument(0);
                    PreparedStatement ps = mock(PreparedStatement.class);
                    ResultSet rs = mock(ResultSet.class);

                    when(ps.executeQuery()).thenReturn(rs);
                    when(rs.next()).thenReturn(true);
                    when(ps.executeUpdate()).thenReturn(1);

                    if (sql.contains("requiere_refrigeracion")) {
                        when(rs.getBoolean("requiere_refrigeracion"))
                                .thenReturn(requiereFrio);
                    }

                    if (sql.contains("SELECT estante, nivel")) {
                        when(rs.getString("estante"))
                                .thenReturn(estante);
                        when(rs.getString("nivel"))
                                .thenReturn(nivel);
                    }

                    if (sql.contains("RETURNING id_lote")) {
                        when(rs.getInt(1)).thenReturn(88);
                    }

                    return ps;
                });

        return con;
    }

    @Test
    void permiteMedicamentoRefrigeradoEnRefrigerador()
            throws Exception {

        Connection con = conexion(
                true, "Refrigerador", "Nivel 1");

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            Lote registrado = new RegistroLoteService()
                    .registrarMedicamentoLote(lote(), 9, 7);

            assertEquals(88, registrado.getIdLote());
            verify(con).commit();
            verify(con, never()).rollback();
        }
    }

    @Test
    void rechazaMedicamentoRefrigeradoEnEstanteNormal()
            throws Exception {

        Connection con = conexion(
                true, "Estante normal", "Nivel 2");

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            ReglaNegocioException error = assertThrows(
                    ReglaNegocioException.class,
                    () -> new RegistroLoteService()
                            .registrarMedicamentoLote(lote(), 9, 7)
            );

            assertTrue(error.getMessage()
                    .contains("cadena de frío"));

            verify(con).rollback();
            verify(con, never()).commit();
            verify(con, never()).prepareStatement(
                    contains("INSERT INTO lotes"));
        }
    }

    @Test
    void permiteMedicamentoSinRefrigeracionEnEstanteNormal()
            throws Exception {

        Connection con = conexion(
                false, "Estante normal", "Nivel 2");

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            Lote registrado = new RegistroLoteService()
                    .registrarMedicamentoLote(lote(), 9, 7);

            assertEquals(88, registrado.getIdLote());
            verify(con).commit();
            verify(con, never()).rollback();
            verify(con, never()).prepareStatement(
                    contains("SELECT estante, nivel"));
        }
    }

    @Test
    void aceptaUbicacionRefrigeradorSinDistinguirMayusculas()
            throws Exception {

        Connection con = conexion(
                true, "REFRIGERADOR", "NIVEL 1");

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            Lote registrado = new RegistroLoteService()
                    .registrarMedicamentoLote(lote(), 9, 7);

            assertEquals(88, registrado.getIdLote());
            verify(con).commit();
        }
    }
}
