package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Aseo;
import org.example.carestock.model.Maternidad;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CatalogoDaoJdbcTest {

    private Aseo aseo() {
        Aseo p = new Aseo();
        p.setCodigo("ASE-001");
        p.setNombre("Jabon");
        p.setDescripcion("Jabon liquido");
        p.setPrecio(12000);
        p.setStock(8);
        p.setTipoAseo("Higiene");
        p.setBiodegradable(true);
        p.setComponentesActivos("Glicerina");
        return p;
    }

    private Maternidad maternidad() {
        Maternidad p = new Maternidad();
        p.setCodigo("MAT-001");
        p.setNombre("Kit maternal");
        p.setDescripcion("Kit de cuidado");
        p.setPrecio(45000);
        p.setStock(3);
        p.setEtapaRecomendada("Prenatal");
        p.setHipoalergenico(true);
        p.setEdadGestacionalSugerida(0);
        return p;
    }

    private MockedStatic<ConexionBD> simular(
            Connection con, PreparedStatement ps, ResultSet rs
    ) throws Exception {
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class);
        bd.when(ConexionBD::getConexion).thenReturn(con);
        return bd;
    }

    @Test
    void registraAseoConFarmaciaYUsuario() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true);
        when(rs.getInt("id_aseo")).thenReturn(101);

        try (var bd = simular(con, ps, rs)) {
            int id = new AseoDAO().guardar(aseo(), 7, 9);

            assertEquals(101, id);
            verify(ps).setInt(9, 7);
            verify(ps).setInt(10, 9);
            verify(ps).setString(1, "ASE-001");
            verify(ps).setBoolean(7, true);
        }
    }

    @Test
    void registraMaternidadConEdadOpcional() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true);
        when(rs.getInt("id_maternidad")).thenReturn(202);

        try (var bd = simular(con, ps, rs)) {
            int id = new MaternidadDAO()
                    .guardar(maternidad(), 7, 9);

            assertEquals(202, id);
            verify(ps).setNull(8, Types.INTEGER);
            verify(ps).setInt(9, 7);
            verify(ps).setInt(10, 9);
        }
    }

    @Test
    void buscaAseoRespetandoFarmacia() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true);
        when(rs.getInt("id_aseo")).thenReturn(101);
        when(rs.getString("codigo")).thenReturn("ASE-001");
        when(rs.getString("nombre")).thenReturn("Jabon");
        when(rs.getString("descripcion")).thenReturn("Liquido");
        when(rs.getBigDecimal("precio"))
                .thenReturn(new BigDecimal("12000"));
        when(rs.getInt("stock")).thenReturn(8);
        when(rs.getString("tipo_aseo")).thenReturn("Higiene");
        when(rs.getBoolean("biodegradable")).thenReturn(true);
        when(rs.getString("componentes_activos"))
                .thenReturn("Glicerina");
        when(rs.getString("estado")).thenReturn("ACTIVO");

        try (var bd = simular(con, ps, rs)) {
            var resultado = new AseoDAO().buscarPorId(101, 7);

            assertNotNull(resultado);
            assertEquals(101, resultado.id());
            assertEquals("Jabon", resultado.producto().getNombre());
            assertEquals(8, resultado.producto().getStock());
            assertTrue(resultado.producto().isBiodegradable());

            verify(ps).setInt(1, 101);
            verify(ps).setInt(2, 7);
        }
    }

    @Test
    void listaMaternidadDeUnaFarmacia() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true, false);
        when(rs.getInt("id_maternidad")).thenReturn(202);
        when(rs.getInt("edad_gestacional_sugerida")).thenReturn(0);
        when(rs.wasNull()).thenReturn(true);
        when(rs.getString("codigo")).thenReturn("MAT-001");
        when(rs.getString("nombre")).thenReturn("Kit maternal");
        when(rs.getString("descripcion")).thenReturn("Cuidado");
        when(rs.getBigDecimal("precio"))
                .thenReturn(new BigDecimal("45000"));
        when(rs.getInt("stock")).thenReturn(3);
        when(rs.getString("etapa_recomendada")).thenReturn("Prenatal");
        when(rs.getBoolean("hipoalergenico")).thenReturn(true);
        when(rs.getString("estado")).thenReturn("ACTIVO");

        try (var bd = simular(con, ps, rs)) {
            var productos = new MaternidadDAO().listarPorFarmacia(7);

            assertEquals(1, productos.size());
            assertEquals("Kit maternal",
                    productos.get(0).producto().getNombre());
            assertEquals(0, productos.get(0).producto()
                    .getEdadGestacionalSugerida());
            verify(ps).setInt(1, 7);
        }
    }

    @Test
    void consultaSinResultadosDevuelveNull() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(false);

        try (var bd = simular(con, ps, rs)) {
            assertNull(new AseoDAO().buscarPorId(999, 7));
        }
    }

    @Test
    void actualizaYDesactivaSegunFilasAfectadas() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(ps.executeUpdate()).thenReturn(1, 0);

        try (var bd = simular(con, ps, rs)) {
            AseoDAO dao = new AseoDAO();

            assertTrue(dao.actualizar(101, aseo(), 7, 9));
            assertFalse(dao.desactivar(101, 7, 9));

            verify(ps, atLeastOnce()).setInt(3, 7);
        }
    }

    @Test
    void rechazaRegistrosYContextosInvalidos() {
        AseoDAO dao = new AseoDAO();

        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.guardar(null, 7, 9)),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.guardar(aseo(), 0, 9)),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.guardar(aseo(), 7, 0)),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.buscarPorId(0, 7)),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.buscarPorId(101, 0)),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.listarPorFarmacia(0)),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.desactivar(0, 7, 9))
        );
    }

    @Test
    void convierteRegistroInvalidoEnErrorSql() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(rs.next()).thenReturn(true);
        when(rs.getString("descripcion")).thenReturn(null);
        when(rs.getBigDecimal("precio")).thenReturn(BigDecimal.ONE);

        try (var bd = simular(con, ps, rs)) {
            assertThrows(SQLException.class,
                    () -> new MaternidadDAO().buscarPorId(202, 7));
        }
    }
}
