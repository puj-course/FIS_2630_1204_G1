package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AlertasInventarioDaoCoberturaTest {

    @Test
    void rechazaFarmaciaInvalida() {
        AlertasInventarioDao dao = new AlertasInventarioDao();

        assertThrows(IllegalArgumentException.class,
                () -> dao.listarPorFarmacia(0));

        assertThrows(IllegalArgumentException.class,
                () -> dao.listarPorFarmacia(-1));
    }

    @Test
    void consultaAlertasDeFarmaciaSinResultados()
            throws Exception {

        Connection conexion = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        try (MockedStatic<ConexionBD> bd =
                     mockStatic(ConexionBD.class)) {

            bd.when(ConexionBD::getConexion)
                    .thenReturn(conexion);

            when(conexion.prepareStatement(anyString()))
                    .thenReturn(ps);

            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(false);

            AlertasInventarioDao dao = new AlertasInventarioDao();

            var resultado = dao.listarPorFarmacia(7);

            assertEquals(List.of(), resultado);

            verify(ps).setInt(1, 7);
            verify(ps).executeQuery();

            verify(rs).close();
            verify(ps).close();
            verify(conexion).close();
        }
    }
}
