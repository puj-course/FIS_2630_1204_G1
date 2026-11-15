package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.MockedStatic;

import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import org.example.carestock.service.ServicioMovimientosProducto.Categoria;
import org.example.carestock.service.ServicioMovimientosProducto.TipoMovimiento;
import org.example.carestock.service.ServicioMovimientosProducto.ResultadoMovimiento;

class ServicioMovimientosJdbcCoverage87Test {

    private static final int USUARIO = 7;
    private static final int FARMACIA = 8;
    private static final int PRODUCTO = 42;

    private static final class Escenario implements AutoCloseable {
        final Connection con = mock(Connection.class);

        final PreparedStatement permiso = mock(PreparedStatement.class);
        final PreparedStatement stock = mock(PreparedStatement.class);
        final PreparedStatement actualizar = mock(PreparedStatement.class);
        final PreparedStatement historial = mock(PreparedStatement.class);

        final ResultSet rsPermiso = mock(ResultSet.class);
        final ResultSet rsStock = mock(ResultSet.class);
        final ResultSet rsHistorial = mock(ResultSet.class);

        final MockedStatic<SesionUsuario> sesion;
        final MockedStatic<ConexionBD> db;

        Escenario() throws Exception {
            when(con.prepareStatement(anyString())).thenReturn(
                    permiso, stock, actualizar, historial);

            when(permiso.executeQuery()).thenReturn(rsPermiso);
            when(rsPermiso.next()).thenReturn(true);

            when(stock.executeQuery()).thenReturn(rsStock);
            when(rsStock.next()).thenReturn(true);
            when(rsStock.getInt("stock")).thenReturn(11);
            when(rsStock.wasNull()).thenReturn(false);

            when(actualizar.executeUpdate()).thenReturn(1);

            when(historial.executeQuery()).thenReturn(rsHistorial);
            when(rsHistorial.next()).thenReturn(true);
            when(rsHistorial.getLong("id_movimiento"))
                    .thenReturn(701L);

            sesion = mockStatic(SesionUsuario.class);
            sesion.when(SesionUsuario::actual).thenReturn(
                new SesionUsuario.DatosUsuario(
                    USUARIO, "Farmaceutico QA", "qa@test.local",
                    "FARMACEUTICO", FARMACIA, "Farmacia QA"));

            db = mockStatic(ConexionBD.class);
            db.when(ConexionBD::getConexion).thenReturn(con);
        }

        ResultadoMovimiento registrar(
                Categoria categoria,
                TipoMovimiento tipo,
                int cantidad) throws Exception {

            return new ServicioMovimientosProducto().registrar(
                categoria, PRODUCTO, tipo, cantidad,
                "  Movimiento de prueba  ",
                USUARIO, FARMACIA);
        }

        void comprobarRollback() throws Exception {
            verify(con).rollback();
            verify(con, never()).commit();
            verify(con).close();
        }

        void comprobarCommit() throws Exception {
            verify(con).setAutoCommit(false);
            verify(con).commit();
            verify(con, never()).rollback();
            verify(con).close();
        }

        @Override
        public void close() {
            db.close();
            sesion.close();
        }
    }

    @ParameterizedTest
    @EnumSource(Categoria.class)
    void entradaRegistraMovimientoParaCadaCategoria(
            Categoria categoria) throws Exception {

        try (Escenario e = new Escenario()) {
            ResultadoMovimiento r = e.registrar(
                categoria, TipoMovimiento.ENTRADA, 4);

            assertAll(
                () -> assertEquals(701L, r.idMovimiento()),
                () -> assertEquals(categoria, r.categoria()),
                () -> assertEquals(PRODUCTO, r.idProducto()),
                () -> assertEquals(TipoMovimiento.ENTRADA, r.tipo()),
                () -> assertEquals(11, r.stockAnterior()),
                () -> assertEquals(15, r.stockPosterior()),
                () -> assertEquals(4, r.cantidad()),
                () -> assertEquals(FARMACIA, r.idFarmacia()),
                () -> assertEquals(USUARIO, r.idUsuario())
            );

            verify(e.actualizar).setInt(1, 15);
            verify(e.historial).setString(
                7, "Movimiento de prueba");

            if (categoria == Categoria.ASEO) {
                verify(e.historial).setInt(1, PRODUCTO);
                verify(e.historial).setNull(2, Types.INTEGER);
            } else {
                verify(e.historial).setNull(1, Types.INTEGER);
                verify(e.historial).setInt(2, PRODUCTO);
            }

            e.comprobarCommit();
        }
    }

    @ParameterizedTest
    @EnumSource(Categoria.class)
    void salidaDescuentaStockParaCadaCategoria(
            Categoria categoria) throws Exception {

        try (Escenario e = new Escenario()) {
            ResultadoMovimiento r = e.registrar(
                categoria, TipoMovimiento.SALIDA, 6);

            assertEquals(11, r.stockAnterior());
            assertEquals(5, r.stockPosterior());
            assertEquals(TipoMovimiento.SALIDA, r.tipo());

            verify(e.actualizar).setInt(1, 5);
            verify(e.historial).setString(5, "SALIDA");
            verify(e.historial).setInt(8, 11);
            verify(e.historial).setInt(9, 5);

            e.comprobarCommit();
        }
    }

    @Test
    void validaUsuarioFarmaciaYBloqueoAntesDeModificar()
            throws Exception {

        try (Escenario e = new Escenario()) {
            e.registrar(Categoria.ASEO, TipoMovimiento.ENTRADA, 2);

            verify(e.permiso).setInt(1, USUARIO);
            verify(e.permiso).setInt(2, FARMACIA);
            verify(e.stock).setInt(1, PRODUCTO);
            verify(e.stock).setInt(2, FARMACIA);
            verify(e.actualizar).setInt(2, USUARIO);
            verify(e.actualizar).setInt(3, PRODUCTO);
            verify(e.actualizar).setInt(4, FARMACIA);
            e.comprobarCommit();
        }
    }

    @Test
    void operadorRevocadoProvocaRollback()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsPermiso.next()).thenReturn(false);

            assertThrows(ReglaNegocioException.class,
                () -> e.registrar(
                    Categoria.ASEO, TipoMovimiento.ENTRADA, 2));

            verify(e.stock, never()).executeQuery();
            e.comprobarRollback();
        }
    }

    @Test
    void productoInexistenteProvocaRollback()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsStock.next()).thenReturn(false);

            assertThrows(ReglaNegocioException.class,
                () -> e.registrar(
                    Categoria.MATERNIDAD, TipoMovimiento.ENTRADA, 2));

            verify(e.actualizar, never()).executeUpdate();
            e.comprobarRollback();
        }
    }

    @Test
    void stockNuloNoSeInterpretaComoCero()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsStock.wasNull()).thenReturn(true);

            assertThrows(ReglaNegocioException.class,
                () -> e.registrar(
                    Categoria.ASEO, TipoMovimiento.ENTRADA, 2));

            verify(e.actualizar, never()).executeUpdate();
            e.comprobarRollback();
        }
    }

    @Test
    void stockNegativoEnBaseDeDatosSeRechaza()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsStock.getInt("stock")).thenReturn(-3);

            assertThrows(ReglaNegocioException.class,
                () -> e.registrar(
                    Categoria.ASEO, TipoMovimiento.ENTRADA, 2));

            e.comprobarRollback();
        }
    }

    @Test
    void salidaSinExistenciasSuficientesRevierte()
            throws Exception {

        try (Escenario e = new Escenario()) {
            assertThrows(ReglaNegocioException.class,
                () -> e.registrar(
                    Categoria.MATERNIDAD, TipoMovimiento.SALIDA, 12));

            verify(e.actualizar, never()).executeUpdate();
            e.comprobarRollback();
        }
    }

    @Test
    void entradaConOverflowNoModificaInventario()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsStock.getInt("stock"))
                .thenReturn(Integer.MAX_VALUE);

            assertThrows(ReglaNegocioException.class,
                () -> e.registrar(
                    Categoria.ASEO, TipoMovimiento.ENTRADA, 1));

            verify(e.actualizar, never()).executeUpdate();
            e.comprobarRollback();
        }
    }

    @Test
    void actualizaciónSinFilasRevierteTransaccion()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.actualizar.executeUpdate()).thenReturn(0);

            assertThrows(SQLException.class,
                () -> e.registrar(
                    Categoria.ASEO, TipoMovimiento.ENTRADA, 2));

            verify(e.historial, never()).executeQuery();
            e.comprobarRollback();
        }
    }

    @Test
    void excepcionSqlAlActualizarProvocaRollback()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.actualizar.executeUpdate())
                .thenThrow(new SQLException("Fallo de escritura QA"));

            SQLException error = assertThrows(SQLException.class,
                () -> e.registrar(
                    Categoria.ASEO, TipoMovimiento.ENTRADA, 2));

            assertEquals("Fallo de escritura QA",
                error.getMessage());
            e.comprobarRollback();
        }
    }

    @Test
    void historialSinIdentificadorNoConfirmaOperacion()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsHistorial.next()).thenReturn(false);

            assertThrows(SQLException.class,
                () -> e.registrar(
                    Categoria.ASEO, TipoMovimiento.SALIDA, 2));

            verify(e.actualizar).executeUpdate();
            e.comprobarRollback();
        }
    }

    @Test
    void errorSqlDeHistorialRevierteStock()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.historial.executeQuery())
                .thenThrow(new SQLException("Historial no disponible"));

            SQLException error = assertThrows(SQLException.class,
                () -> e.registrar(
                    Categoria.MATERNIDAD, TipoMovimiento.ENTRADA, 3));

            assertEquals("Historial no disponible",
                error.getMessage());
            e.comprobarRollback();
        }
    }

    @Test
    void excepcionAlBloquearStockRevierteOperacion()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.stock.executeQuery())
                .thenThrow(new SQLException("Bloqueo fallido"));

            assertThrows(SQLException.class,
                () -> e.registrar(
                    Categoria.ASEO, TipoMovimiento.ENTRADA, 2));

            verify(e.actualizar, never()).executeUpdate();
            e.comprobarRollback();
        }
    }

    @Test
    void errorAlConfirmarIntentaRollback()
            throws Exception {

        try (Escenario e = new Escenario()) {
            doThrow(new SQLException("Commit rechazado"))
                .when(e.con).commit();

            SQLException error = assertThrows(SQLException.class,
                () -> e.registrar(
                    Categoria.ASEO, TipoMovimiento.ENTRADA, 2));

            assertEquals("Commit rechazado",
                error.getMessage());
            verify(e.con).commit();
            verify(e.con).rollback();
            verify(e.con).close();
        }
    }

    @Test
    void errorDeRollbackSeAdjuntaAlErrorOriginal()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsPermiso.next()).thenReturn(false);
            doThrow(new SQLException("Rollback también falló"))
                .when(e.con).rollback();

            ReglaNegocioException error =
                assertThrows(ReglaNegocioException.class,
                    () -> e.registrar(
                        Categoria.ASEO, TipoMovimiento.ENTRADA, 2));

            assertEquals(1, error.getSuppressed().length);
            assertEquals("Rollback también falló",
                error.getSuppressed()[0].getMessage());
            verify(e.con).close();
        }
    }

    @Test
    void cerrarRecursosNoConfirmaMovimientoFallido()
            throws Exception {

        try (Escenario e = new Escenario()) {
            when(e.rsStock.next()).thenReturn(false);

            assertThrows(ReglaNegocioException.class,
                () -> e.registrar(
                    Categoria.ASEO, TipoMovimiento.SALIDA, 3));

            verify(e.permiso).close();
            verify(e.rsPermiso).close();
            verify(e.stock).close();
            verify(e.rsStock).close();
            verify(e.con).close();
            verify(e.con, never()).commit();
        }
    }
}
