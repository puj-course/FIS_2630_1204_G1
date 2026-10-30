package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Lote;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.sql.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ServicioCrudLotesJdbcTest {

    private final LocalDate fecha = LocalDate.now().plusDays(60);

    @BeforeEach
    void iniciarSesionSimulada() throws Exception {
        Field campo = SesionUsuario.class.getDeclaredField("actual");
        campo.setAccessible(true);
        campo.set(null, new SesionUsuario.DatosUsuario(
                3, "Administrador", "admin@test.local",
                "ADMINISTRADOR", 1, "Farmacia Uno"));
    }

    @AfterEach
    void limpiarSesion() throws Exception {
        Field campo = SesionUsuario.class.getDeclaredField("actual");
        campo.setAccessible(true);
        campo.set(null, null);
    }

    private Connection conexionSimulada() throws Exception {
        Connection con = mock(Connection.class);
        when(con.getAutoCommit()).thenReturn(true);

        when(con.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(ps.executeQuery()).thenReturn(rs);
            when(ps.executeUpdate()).thenReturn(1);
            when(rs.next()).thenReturn(true, false);

            when(rs.getInt("id_farmacia")).thenReturn(1);
            when(rs.getString("nombre_rol")).thenReturn("ADMINISTRADOR");

            when(rs.getInt("id_medicamento")).thenReturn(10);
            when(rs.getString("numero_lote")).thenReturn("LOTE-1");
            when(rs.getDate("fecha_vencimiento"))
                    .thenReturn(Date.valueOf(fecha));
            when(rs.getInt("id_ubicacion")).thenReturn(2);
            when(rs.wasNull()).thenReturn(false);
            when(rs.getInt("cantidad_actual")).thenReturn(0);
            when(rs.getString("estado_lote")).thenReturn("DISPONIBLE");

            when(rs.getInt(1)).thenReturn(99);
            when(rs.getString(2)).thenReturn("Acetaminofen");
            when(rs.getString(3)).thenReturn("Nivel 2");

            return ps;
        });

        return con;
    }

    private Lote loteOriginal() {
        Lote lote = new Lote(
                "LOTE-1", 10, 0, fecha,
                2, "DISPONIBLE", 3);
        lote.setIdLote(99);
        return lote;
    }

    @Test
    void consultaMedicamentosDeFarmaciaAutorizada() throws Exception {
        Connection con = conexionSimulada();

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            var medicamentos = new ServicioCrudLotes().medicamentos();

            assertEquals(1, medicamentos.size());
            assertEquals(99, medicamentos.get(0).id());
            assertEquals("Acetaminofen", medicamentos.get(0).nombre());
            verify(con, atLeastOnce())
                    .prepareStatement(contains("FROM medicamentos"));
        }
    }

    @Test
    void consultaUbicacionesDisponibles() throws Exception {
        Connection con = conexionSimulada();

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            var ubicaciones = new ServicioCrudLotes().ubicaciones();

            assertEquals(1, ubicaciones.size());
            assertEquals(99, ubicaciones.get(0).id());
            verify(con, atLeastOnce())
                    .prepareStatement(contains("FROM ubicaciones"));
        }
    }

    @Test
    void registraLoteYConfirmaTransaccion() throws Exception {
        Connection con = conexionSimulada();

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            int id = new ServicioCrudLotes()
                    .crear(10, "LOTE-NUEVO", 12, fecha, 2);

            assertEquals(99, id);
            verify(con).setAutoCommit(false);
            verify(con).commit();
            verify(con, never()).rollback();
            verify(con).setAutoCommit(true);
            verify(con, atLeastOnce())
                    .prepareStatement(contains("INSERT INTO lotes"));
            verify(con, atLeastOnce())
                    .prepareStatement(contains("log_movimientos"));
        }
    }

    @Test
    void editaLoteSinModificarCantidades() throws Exception {
        Connection con = conexionSimulada();

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertDoesNotThrow(() -> new ServicioCrudLotes()
                    .editar(loteOriginal(), "LOTE-2", fecha, 2));

            verify(con).commit();
            verify(con, never()).rollback();
            verify(con, atLeastOnce())
                    .prepareStatement(contains("UPDATE lotes"));
        }
    }

    @Test
    void retiroLogicoConservaHistorial() throws Exception {
        Connection con = conexionSimulada();

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertDoesNotThrow(() -> new ServicioCrudLotes()
                    .retirar(loteOriginal()));

            verify(con).commit();
            verify(con, never()).rollback();
            verify(con, atLeastOnce())
                    .prepareStatement(contains("RETENIDO"));
            verify(con, atLeastOnce())
                    .prepareStatement(contains("log_movimientos"));
        }
    }

    @Test
    void loteInexistenteProvocaRollback() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement actorPs = mock(PreparedStatement.class);
        PreparedStatement medicamentoPs = mock(PreparedStatement.class);
        ResultSet actorRs = mock(ResultSet.class);
        ResultSet medicamentoRs = mock(ResultSet.class);

        when(con.getAutoCommit()).thenReturn(true);
        when(con.prepareStatement(anyString()))
                .thenReturn(actorPs, medicamentoPs);

        when(actorPs.executeQuery()).thenReturn(actorRs);
        when(actorRs.next()).thenReturn(true);
        when(actorRs.getInt("id_farmacia")).thenReturn(1);
        when(actorRs.getString("nombre_rol"))
                .thenReturn("ADMINISTRADOR");

        when(medicamentoPs.executeQuery()).thenReturn(medicamentoRs);
        when(medicamentoRs.next()).thenReturn(false);

        try (MockedStatic<ConexionBD> bd = mockStatic(ConexionBD.class)) {
            bd.when(ConexionBD::getConexion).thenReturn(con);

            assertThrows(SecurityException.class,
                    () -> new ServicioCrudLotes()
                            .crear(10, "LOTE-NUEVO", 12, fecha, 2));

            verify(con).rollback();
            verify(con, never()).commit();
        }
    }
}
