package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Medicamento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MedicamentoDaoCoberturaTest {

    private Connection con;
    private PreparedStatement ps;
    private Statement st;
    private ResultSet rs;
    private MockedStatic<ConexionBD> bd;
    private MedicamentoDAOImpl dao;

    @BeforeEach
    void preparar() throws Exception {
        con = mock(Connection.class);
        ps = mock(PreparedStatement.class);
        st = mock(Statement.class);
        rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(con.prepareStatement(anyString(), anyInt()))
                .thenReturn(ps);
        when(con.createStatement()).thenReturn(st);

        when(ps.executeQuery()).thenReturn(rs);
        when(ps.getGeneratedKeys()).thenReturn(rs);
        when(st.executeQuery(anyString())).thenReturn(rs);

        bd = mockStatic(ConexionBD.class);
        bd.when(ConexionBD::getConexion).thenReturn(con);

        dao = new MedicamentoDAOImpl();
    }

    @AfterEach
    void liberar() {
        bd.close();
    }

    private Medicamento medicamento() {
        Medicamento m = new Medicamento();

        m.setCodigoInvima("INV-100");
        m.setNombreComercial("Acetaminofen");
        m.setPrincipioActivo("Paracetamol");
        m.setConcentracion("500 mg");
        m.setFormaFarmaceutica("Tableta");
        m.setIdCategoria(3);
        m.setStockMinimo(10);
        m.setStockTotal(25);
        m.setEstado("ACTIVO");
        m.setIdFarmacia(7);
        m.setPresentacion("Caja");
        m.setIdUsuarioCreacion(9);
        m.setPrecio(new BigDecimal("2500.00"));

        return m;
    }

    @Test
    void guardaMedicamentoYRecuperaId() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getInt(1)).thenReturn(101);

        Medicamento m = medicamento();
        dao.guardar(m);

        assertEquals(101, m.getIdMedicamento());

        verify(ps).setString(1, "INV-100");
        verify(ps).setString(2, "Acetaminofen");
        verify(ps).setInt(6, 3);
        verify(ps).setInt(7, 10);
        verify(ps).setInt(8, 25);
        verify(ps).setInt(10, 7);
        verify(ps).setInt(12, 9);
        verify(ps).setBigDecimal(
                13, new BigDecimal("2500.00"));

        verify(ps).executeUpdate();
    }

    @Test
    void guardaMedicamentoConCamposOpcionalesNulos()
            throws Exception {

        Medicamento m = medicamento();

        m.setIdCategoria(null);
        m.setIdFarmacia(null);
        m.setIdUsuarioCreacion(null);
        m.setPrecio(null);
        m.setEstado(null);

        when(rs.next()).thenReturn(false);

        dao.guardar(m);

        verify(ps).setNull(6, Types.INTEGER);
        verify(ps).setNull(10, Types.INTEGER);
        verify(ps).setNull(12, Types.INTEGER);
        verify(ps).setNull(13, Types.NUMERIC);
        verify(ps).setString(9, "ACTIVO");

        assertEquals(0, m.getIdMedicamento());
    }

    @Test
    void rechazaMedicamentoInvalidoAntesDeGuardar() {
        Medicamento m = medicamento();
        m.setNombreComercial(" ");

        assertThrows(ReglaNegocioException.class,
                () -> dao.guardar(m));

        verifyNoInteractions(con);
    }

    @Test
    void buscaMedicamentoPorIdYMapeaCampos()
            throws Exception {

        when(rs.next()).thenReturn(true);
        when(rs.getInt("id_medicamento")).thenReturn(101);
        when(rs.getInt("id_categoria")).thenReturn(3);
        when(rs.getInt("id_farmacia")).thenReturn(7);
        when(rs.getInt("stock_total")).thenReturn(25);
        when(rs.getInt("stock_minimo")).thenReturn(10);
        when(rs.getInt("id_usuario_creacion")).thenReturn(9);
        when(rs.getInt("id_usuario_modificacion"))
                .thenReturn(12);

        when(rs.getString("nombre_comercial"))
                .thenReturn("Acetaminofen");
        when(rs.getString("codigo_invima"))
                .thenReturn("INV-100");

        when(rs.getBigDecimal("precio"))
                .thenReturn(new BigDecimal("2500.00"));

        Timestamp fecha = Timestamp.valueOf(
                LocalDateTime.of(2026, 10, 30, 10, 0));

        when(rs.getTimestamp("fecha_modificacion"))
                .thenReturn(fecha);

        Medicamento m = dao.buscarPorId(101);

        assertNotNull(m);
        assertEquals(101, m.getIdMedicamento());
        assertEquals("Acetaminofen", m.getNombreComercial());
        assertEquals(3, m.getIdCategoria());
        assertEquals(7, m.getIdFarmacia());
        assertEquals(25, m.getStockTotal());
        assertEquals(12, m.getIdUsuarioModificacion());
        assertEquals(fecha.toLocalDateTime(),
                m.getFechaModificacion());

        verify(ps).setInt(1, 101);
    }

    @Test
    void buscaCodigoInvimaInexistente() throws Exception {
        when(rs.next()).thenReturn(false);

        assertNull(dao.buscarPorCodigoInvima("NO-EXISTE"));

        verify(ps).setString(1, "NO-EXISTE");
    }

    @Test
    void listaMedicamentosDisponibles() throws Exception {
        when(rs.next()).thenReturn(true, true, false);

        when(rs.getInt("id_medicamento"))
                .thenReturn(11, 12);

        when(rs.getString("nombre_comercial"))
                .thenReturn("Producto A", "Producto B");

        var lista = dao.listarTodos();

        assertEquals(2, lista.size());
        assertEquals(11, lista.get(0).getIdMedicamento());
        assertEquals(12, lista.get(1).getIdMedicamento());
        assertEquals("Producto A",
                lista.get(0).getNombreComercial());
        assertEquals("Producto B",
                lista.get(1).getNombreComercial());

        verify(st).executeQuery(
                contains("ORDER BY nombre_comercial"));
    }

    @Test
    void listaCategorias() throws Exception {
        when(rs.next()).thenReturn(true, true, false);

        when(rs.getInt("id_categoria"))
                .thenReturn(1, 2);

        when(rs.getString("nombre_categoria"))
                .thenReturn("Analgesicos", "Antibioticos");

        Map<Integer, String> categorias =
                dao.listarCategorias();

        assertEquals(2, categorias.size());
        assertEquals("Analgesicos", categorias.get(1));
        assertEquals("Antibioticos", categorias.get(2));
    }

    @Test
    void relacionaMedicamentosConCategorias()
            throws Exception {

        ResultSet rsCategorias = mock(ResultSet.class);
        ResultSet rsMedicamentos = mock(ResultSet.class);

        when(st.executeQuery(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);

            return sql.contains("FROM categorias")
                    ? rsCategorias : rsMedicamentos;
        });

        when(rsCategorias.next()).thenReturn(true, false);
        when(rsCategorias.getInt("id_categoria"))
                .thenReturn(3);
        when(rsCategorias.getString("nombre_categoria"))
                .thenReturn("Analgesicos");

        when(rsMedicamentos.next())
                .thenReturn(true, false);

        when(rsMedicamentos.getInt("id_medicamento"))
                .thenReturn(101);
        when(rsMedicamentos.getInt("id_categoria"))
                .thenReturn(3);

        var resultado = dao.categoriaPorMedicamento();

        assertEquals(1, resultado.size());
        assertEquals("Analgesicos", resultado.get(101));
    }

    @Test
    void actualizaEstadoMedicamento() throws Exception {
        dao.actualizarEstado(101, "INACTIVO");

        verify(ps).setString(1, "INACTIVO");
        verify(ps).setInt(2, 101);
        verify(ps).executeUpdate();
    }

    @Test
    void cuentaMedicamentosPorCategoria()
            throws Exception {

        when(rs.next()).thenReturn(true, true, false);

        when(rs.getString("nombre_categoria"))
                .thenReturn("Analgesicos", "Antibioticos");

        when(rs.getInt("total")).thenReturn(4, 7);

        var conteo = dao.contarPorCategoria();

        assertEquals(4, conteo.get("Analgesicos"));
        assertEquals(7, conteo.get("Antibioticos"));

        verify(st).executeQuery(
                contains("COUNT(m.id_medicamento)"));
    }

    @Test
    void mapeaCamposOpcionalesNulos() throws Exception {
        when(rs.next()).thenReturn(true);
        when(rs.getInt("id_medicamento")).thenReturn(101);
        when(rs.getString("nombre_comercial"))
                .thenReturn("Producto historico");
        when(rs.getString("codigo_invima"))
                .thenReturn("INV-101");
        when(rs.wasNull()).thenReturn(true);

        Medicamento m = dao.buscarPorId(101);

        assertNotNull(m);
        assertNull(m.getIdCategoria());
        assertNull(m.getIdFarmacia());
        assertNull(m.getIdUsuarioCreacion());
        assertNull(m.getIdUsuarioModificacion());
        assertNull(m.getFechaModificacion());
    }

    @Test
    void propagaErrorDeConsultaSql() throws Exception {
        when(ps.executeQuery()).thenThrow(
                new SQLException("Consulta fallida"));

        SQLException error = assertThrows(
                SQLException.class,
                () -> dao.buscarPorId(101));

        assertEquals("Consulta fallida",
                error.getMessage());
    }
}
