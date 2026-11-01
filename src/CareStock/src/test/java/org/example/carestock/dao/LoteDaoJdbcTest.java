package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Lote;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoteDaoJdbcTest {

    private Lote lote() {
        return new Lote(
                "LOTE-100",
                15,
                20,
                LocalDate.now().plusDays(90),
                2,
                "DISPONIBLE",
                9
        );
    }

    @Test
    void guardaLoteYRecuperaIdGenerado() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(
                anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(88);

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            Lote lote = lote();
            new LoteDAOImpl().guardar(lote);

            assertEquals(88, lote.getIdLote());
            verify(ps).setString(1, "LOTE-100");
            verify(ps).setInt(2, 15);
            verify(ps).setInt(3, 20);
            verify(ps).setInt(5, 2);
            verify(ps).setInt(7, 9);
            verify(ps).executeUpdate();
        }
    }

    @Test
    void buscaLoteExistente() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);

        when(rs.getInt("id_lote")).thenReturn(88);
        when(rs.getString("numero_lote"))
                .thenReturn("LOTE-100");
        when(rs.getInt("id_medicamento")).thenReturn(15);
        when(rs.getInt("cantidad_actual")).thenReturn(20);
        when(rs.getDate("fecha_vencimiento"))
                .thenReturn(Date.valueOf(
                        LocalDate.now().plusDays(90)));
        when(rs.getInt("id_ubicacion")).thenReturn(2);
        when(rs.getString("estado_lote"))
                .thenReturn("DISPONIBLE");

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            Lote resultado = new LoteDAOImpl().buscarPorId(88);

            assertNotNull(resultado);
            assertEquals(88, resultado.getIdLote());
            assertEquals("LOTE-100", resultado.getNumeroLote());
            assertEquals(20, resultado.getCantidadActual());

            verify(ps).setInt(1, 88);
        }
    }

    @Test
    void buscaLoteInexistente() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertNull(new LoteDAOImpl().buscarPorId(999));
        }
    }

    @Test
    void listaLotesDeFarmaciaEspecifica() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("id_lote")).thenReturn(88);
        when(rs.getString("numero_lote"))
                .thenReturn("LOTE-100");
        when(rs.getInt("id_medicamento")).thenReturn(15);
        when(rs.getInt("cantidad_actual")).thenReturn(20);
        when(rs.getDate("fecha_vencimiento"))
                .thenReturn(Date.valueOf(
                        LocalDate.now().plusDays(90)));
        when(rs.getString("estado_lote"))
                .thenReturn("DISPONIBLE");

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            var lista = new LoteDAOImpl().listarPorFarmacia(7);

            assertEquals(1, lista.size());
            assertEquals(88, lista.get(0).getIdLote());
            verify(ps).setInt(1, 7);

            verify(con).prepareStatement(
                    argThat(sql -> sql.contains("m.id_farmacia")));
        }
    }

    @Test
    void listaLotesDeMedicamento() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            var lista = new LoteDAOImpl()
                    .listarPorMedicamento(15);

            assertTrue(lista.isEmpty());
            verify(ps).setInt(1, 15);
        }
    }

    @Test
    void actualizaCantidadYEstado() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            LoteDAOImpl dao = new LoteDAOImpl();

            dao.actualizarCantidad(88, 4);
            dao.actualizarEstado(88, "RETENIDO");

            verify(ps).setInt(1, 4);
            verify(ps, times(2)).setInt(2, 88);
            verify(ps).setString(1, "RETENIDO");
            verify(ps, times(2)).executeUpdate();
        }
    }

    @Test
    void impideConsultasGlobalesYFarmaciaInvalida() {
        LoteDAOImpl dao = new LoteDAOImpl();

        assertThrows(UnsupportedOperationException.class,
                dao::listarTodos);

        assertThrows(IllegalArgumentException.class,
                () -> dao.listarPorFarmacia(0));
    }

    @Test
    void rechazaLoteConCantidadNegativa() {
        Lote invalido = lote();
        invalido.setCantidadActual(-5);

        assertThrows(Exception.class,
                () -> new LoteDAOImpl().guardar(invalido));
    }
}
