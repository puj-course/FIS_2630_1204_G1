package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ConsultaInventarioDaoCoberturaTest {

    @Test
    void rechazaFarmaciaInvalida() {
        ConsultaInventarioDAO dao =
                new ConsultaInventarioDAO();

        assertAll(
            () -> assertThrows(
                    IllegalArgumentException.class,
                    () -> dao.listarPorFarmacia(0)),
            () -> assertThrows(
                    IllegalArgumentException.class,
                    () -> dao.listarPorFarmacia(-1))
        );
    }

    @Test
    void consultaInventarioVacio() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString()))
                .thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            var productos = new ConsultaInventarioDAO()
                    .listarPorFarmacia(7);

            assertTrue(productos.isEmpty());

            verify(ps).setInt(1, 7);
            verify(ps).setInt(2, 7);
            verify(ps).setInt(3, 7);
            verify(ps).executeQuery();

            verify(con).prepareStatement(
                    argThat(sql ->
                        sql.contains("UNION ALL")
                        && sql.contains("FROM medicamentos")
                        && sql.contains("FROM aseo")
                        && sql.contains("FROM maternidad")
                    )
            );
        }
    }

    @Test
    void listaTresCategoriasEnUnaFarmacia()
            throws Exception {

        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString()))
                .thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(
                true, true, true, false);

        when(rs.getInt("id"))
                .thenReturn(10, 20, 30);

        when(rs.getString("categoria"))
                .thenReturn("Medicamento", "Aseo", "Maternidad");

        when(rs.getString("codigo"))
                .thenReturn("MED-01", "ASE-01", "MAT-01");

        when(rs.getString("nombre"))
                .thenReturn("Acetaminofen", "Jabon", "Kit maternal");

        when(rs.getString("presentacion"))
                .thenReturn("Caja", "Unidad", "Kit");

        when(rs.getInt("existencia"))
                .thenReturn(15, 0, 8);

        when(rs.getString("estado"))
                .thenReturn("ACTIVO");

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            var productos = new ConsultaInventarioDAO()
                    .listarPorFarmacia(7);

            assertEquals(3, productos.size());

            assertEquals("Medicamento",
                    productos.get(0).categoria());

            assertEquals("Aseo",
                    productos.get(1).categoria());

            assertEquals("Maternidad",
                    productos.get(2).categoria());

            assertEquals(0,
                    productos.get(1).existencia());

            assertEquals("Kit maternal",
                    productos.get(2).nombre());

            verify(ps).setInt(1, 7);
            verify(ps).setInt(2, 7);
            verify(ps).setInt(3, 7);
        }
    }

    @Test
    void rechazaResultadoConExistenciaNegativa()
            throws Exception {

        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString()))
                .thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getInt("id")).thenReturn(10);
        when(rs.getInt("existencia")).thenReturn(-5);
        when(rs.getString("categoria"))
                .thenReturn("Medicamento");
        when(rs.getString("codigo"))
                .thenReturn("MED-01");
        when(rs.getString("nombre"))
                .thenReturn("Acetaminofen");
        when(rs.getString("presentacion"))
                .thenReturn("Caja");
        when(rs.getString("estado"))
                .thenReturn("ACTIVO");

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertThrows(
                    IllegalArgumentException.class,
                    () -> new ConsultaInventarioDAO()
                            .listarPorFarmacia(7));
        }
    }

    @Test
    void propagaErroresDeBaseDeDatos()
            throws Exception {

        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString()))
                .thenReturn(ps);

        when(ps.executeQuery()).thenThrow(
                new SQLException("Error de consulta"));

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            SQLException error = assertThrows(
                    SQLException.class,
                    () -> new ConsultaInventarioDAO()
                            .listarPorFarmacia(7));

            assertEquals("Error de consulta",
                    error.getMessage());
        }
    }
}
