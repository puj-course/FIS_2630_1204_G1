package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Medicamento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MedicamentoCatalogoJdbcTest {

    private Medicamento medicamento() {
        Medicamento m = new Medicamento();
        m.setCodigoInvima("INV-123");
        m.setNombreComercial("Acetaminofen");
        m.setPrincipioActivo("Paracetamol");
        m.setConcentracion("500 mg");
        m.setFormaFarmaceutica("Tableta");
        m.setIdCategoria(3);
        m.setStockMinimo(10);
        m.setStockTotal(25);
        m.setEstado("ACTIVO");
        m.setPresentacion("Caja");
        m.setPrecio(new BigDecimal("2500.00"));
        return m;
    }

    @Test
    void validaCamposObligatorios() throws Exception {
        assertThrows(IllegalArgumentException.class,
                () -> MedicamentoCatalogoDAO.validarCampos(null));

        Medicamento m = medicamento();
        m.setCodigoInvima(null);

        assertThrows(IllegalArgumentException.class,
                () -> MedicamentoCatalogoDAO.validarCampos(m));

        m.setCodigoInvima("INV-123");
        m.setNombreComercial("");

        assertThrows(IllegalArgumentException.class,
                () -> MedicamentoCatalogoDAO.validarCampos(m));

        m.setNombreComercial("Acetaminofen");
        m.setPrincipioActivo("");

        assertThrows(IllegalArgumentException.class,
                () -> MedicamentoCatalogoDAO.validarCampos(m));

        m.setPrincipioActivo("Paracetamol");
        m.setIdCategoria(0);

        assertThrows(IllegalArgumentException.class,
                () -> MedicamentoCatalogoDAO.validarCampos(m));

        m.setIdCategoria(3);
        m.setStockMinimo(-1);

        assertThrows(IllegalArgumentException.class,
                () -> MedicamentoCatalogoDAO.validarCampos(m));

        assertDoesNotThrow(() ->
                MedicamentoCatalogoDAO.validarCampos(medicamento()));
    }

    @Test
    void consultaMedicamentosDeFarmacia() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("id_medicamento")).thenReturn(15);
        when(rs.getString("codigo_invima")).thenReturn("INV-123");
        when(rs.getString("nombre_comercial"))
                .thenReturn("Acetaminofen");
        when(rs.getString("principio_activo"))
                .thenReturn("Paracetamol");
        when(rs.getString("estado")).thenReturn("ACTIVO");
        when(rs.getInt("id_categoria")).thenReturn(3);
        when(rs.getInt("stock_total")).thenReturn(25);
        when(rs.getInt("id_farmacia")).thenReturn(7);
        when(rs.getBigDecimal("precio"))
                .thenReturn(new BigDecimal("2500.00"));

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            var lista = new MedicamentoCatalogoDAO()
                    .listarPorFarmacia(7);

            assertEquals(1, lista.size());
            assertEquals(15, lista.get(0).getIdMedicamento());
            assertEquals("Acetaminofen",
                    lista.get(0).getNombreComercial());
            assertEquals(25, lista.get(0).getStockTotal());
            assertEquals(7, lista.get(0).getIdFarmacia());

            verify(ps).setInt(1, 7);
        }
    }

    @Test
    void actualizaMedicamentoSinAlterarStock() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            boolean actualizado = new MedicamentoCatalogoDAO()
                    .actualizar(15, medicamento(), 7, 9);

            assertTrue(actualizado);
            verify(ps).setInt(11, 15);
            verify(ps).setInt(12, 7);
            verify(ps).setInt(10, 9);
            verify(ps).setBigDecimal(9,
                    new BigDecimal("2500.00"));

            verify(con).prepareStatement(
                    argThat(sql -> !sql.contains("stock_total =")));
        }
    }

    @Test
    void actualizaMedicamentoSinPrecio() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        Medicamento m = medicamento();
        m.setPrecio(null);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertTrue(new MedicamentoCatalogoDAO()
                    .actualizar(15, m, 7, 9));

            verify(ps).setNull(9, Types.NUMERIC);
        }
    }

    @Test
    void desactivaSoloEnFarmaciaCorrespondiente() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        try (var bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertTrue(new MedicamentoCatalogoDAO()
                    .desactivar(15, 7, 9));

            verify(ps).setInt(1, 9);
            verify(ps).setInt(2, 15);
            verify(ps).setInt(3, 7);

            verify(con).prepareStatement(
                    argThat(sql -> sql.contains("NOT EXISTS")));
        }
    }

    @Test
    void rechazaIdentificadoresNoValidos() {
        MedicamentoCatalogoDAO dao =
                new MedicamentoCatalogoDAO();

        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.listarPorFarmacia(0)),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.actualizar(0, medicamento(), 7, 9)),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.actualizar(15, medicamento(), 0, 9)),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.actualizar(15, medicamento(), 7, 0)),
            () -> assertThrows(IllegalArgumentException.class,
                    () -> dao.desactivar(15, 0, 9))
        );
    }
}
