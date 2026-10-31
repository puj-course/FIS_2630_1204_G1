package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class MovimientosTransaccionalesCoberturaTest {

    private static final int USUARIO = 9;
    private static final int FARMACIA = 7;

    private SesionUsuario.DatosUsuario sesion() {
        return new SesionUsuario.DatosUsuario(
                USUARIO, "Administrador", "qa@test.com",
                "ADMINISTRADOR", FARMACIA, "Farmacia QA");
    }

    @Test
    void entradaYSalidaConfirmanTransaccion() throws Exception {
        for (var categoria :
                ServicioMovimientosProducto.Categoria.values()) {
            for (var tipo :
                    ServicioMovimientosProducto.TipoMovimiento.values()) {

                Connection con = mock(Connection.class);
                PreparedStatement permiso = mock(PreparedStatement.class);
                PreparedStatement stock = mock(PreparedStatement.class);
                PreparedStatement actualizar = mock(PreparedStatement.class);
                PreparedStatement registrar = mock(PreparedStatement.class);

                ResultSet rsPermiso = mock(ResultSet.class);
                ResultSet rsStock = mock(ResultSet.class);
                ResultSet rsRegistro = mock(ResultSet.class);

                when(con.prepareStatement(anyString())).thenReturn(
                        permiso, stock, actualizar, registrar);

                when(permiso.executeQuery()).thenReturn(rsPermiso);
                when(rsPermiso.next()).thenReturn(true);

                when(stock.executeQuery()).thenReturn(rsStock);
                when(rsStock.next()).thenReturn(true);
                when(rsStock.getInt("stock")).thenReturn(10);

                when(actualizar.executeUpdate()).thenReturn(1);

                when(registrar.executeQuery()).thenReturn(rsRegistro);
                when(rsRegistro.next()).thenReturn(true);
                when(rsRegistro.getLong("id_movimiento")).thenReturn(81L);

                try (MockedStatic<ConexionBD> bd =
                             mockStatic(ConexionBD.class);
                     MockedStatic<SesionUsuario> usuario =
                             mockStatic(SesionUsuario.class)) {

                    bd.when(ConexionBD::getConexion).thenReturn(con);
                    usuario.when(SesionUsuario::actual)
                            .thenReturn(sesion());

                    var resultado = new ServicioMovimientosProducto()
                            .registrar(categoria, 12, tipo, 3,
                                    "  Reposicion QA  ", USUARIO, FARMACIA);

                    assertEquals(81L, resultado.idMovimiento());
                    assertEquals(10, resultado.stockAnterior());
                    assertEquals(
                            tipo == ServicioMovimientosProducto
                                    .TipoMovimiento.ENTRADA ? 13 : 7,
                            resultado.stockPosterior());

                    verify(con).setAutoCommit(false);
                    verify(con).commit();
                    verify(con, never()).rollback();
                    verify(registrar).setString(7, "Reposicion QA");
                    verify(permiso).setInt(2, FARMACIA);
                }
            }
        }
    }

    @Test
    void operadorNoAutorizadoProvocaRollback() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement permiso = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(permiso);
        when(permiso.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (var bd = mockStatic(ConexionBD.class);
             var usuario = mockStatic(SesionUsuario.class)) {

            bd.when(ConexionBD::getConexion).thenReturn(con);
            usuario.when(SesionUsuario::actual).thenReturn(sesion());

            assertThrows(ReglaNegocioException.class, () ->
                    new ServicioMovimientosProducto().registrar(
                            ServicioMovimientosProducto.Categoria.ASEO,
                            12,
                            ServicioMovimientosProducto.TipoMovimiento.SALIDA,
                            2, "Venta", USUARIO, FARMACIA));

            verify(con).rollback();
            verify(con, never()).commit();
        }
    }

    @Test
    void errorAlActualizarStockRevierteOperacion() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement permiso = mock(PreparedStatement.class);
        PreparedStatement stock = mock(PreparedStatement.class);
        PreparedStatement actualizar = mock(PreparedStatement.class);
        ResultSet rsPermiso = mock(ResultSet.class);
        ResultSet rsStock = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(
                permiso, stock, actualizar);
        when(permiso.executeQuery()).thenReturn(rsPermiso);
        when(rsPermiso.next()).thenReturn(true);
        when(stock.executeQuery()).thenReturn(rsStock);
        when(rsStock.next()).thenReturn(true);
        when(rsStock.getInt("stock")).thenReturn(5);
        when(actualizar.executeUpdate()).thenReturn(0);

        try (var bd = mockStatic(ConexionBD.class);
             var usuario = mockStatic(SesionUsuario.class)) {

            bd.when(ConexionBD::getConexion).thenReturn(con);
            usuario.when(SesionUsuario::actual).thenReturn(sesion());

            assertThrows(SQLException.class, () ->
                    new ServicioMovimientosProducto().registrar(
                            ServicioMovimientosProducto.Categoria.MATERNIDAD,
                            15,
                            ServicioMovimientosProducto.TipoMovimiento.SALIDA,
                            2, "Salida QA", USUARIO, FARMACIA));

            verify(con).rollback();
            verify(con, never()).commit();
        }
    }

    @Test
    void rechazaSesionDeOtraFarmaciaAntesDeConectar() {
        try (var usuario = mockStatic(SesionUsuario.class);
             var bd = mockStatic(ConexionBD.class)) {

            usuario.when(SesionUsuario::actual).thenReturn(sesion());

            assertThrows(ReglaNegocioException.class, () ->
                    new ServicioMovimientosProducto().registrar(
                            ServicioMovimientosProducto.Categoria.ASEO,
                            12,
                            ServicioMovimientosProducto.TipoMovimiento.ENTRADA,
                            5, "Entrada", USUARIO, 99));

            bd.verifyNoInteractions();
        }
    }
}
