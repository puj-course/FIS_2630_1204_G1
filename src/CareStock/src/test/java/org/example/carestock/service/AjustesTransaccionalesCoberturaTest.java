package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.AjusteExistencias;
import org.example.carestock.proxy.InventarioConsultas;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AjustesTransaccionalesCoberturaTest {

    private SesionUsuario.DatosUsuario sesion() {
        return new SesionUsuario.DatosUsuario(
                9, "Administrador", "qa@test.com",
                "ADMINISTRADOR", 7, "Farmacia QA");
    }

    private AjusteExistencias ajuste(
            AjusteExistencias.Categoria categoria,
            int anterior, int nuevo) {
        return new AjusteExistencias(
                categoria, 12, 9, 7, "Conteo fisico",
                anterior, nuevo);
    }

    @Test
    void ajustesExitososConfirmanYInvalidanCache() throws Exception {
        for (var categoria : AjusteExistencias.Categoria.values()) {

            Connection con = mock(Connection.class);
            PreparedStatement permiso = mock(PreparedStatement.class);
            PreparedStatement stock = mock(PreparedStatement.class);
            PreparedStatement actualizar = mock(PreparedStatement.class);
            PreparedStatement auditoria = mock(PreparedStatement.class);

            ResultSet rsPermiso = mock(ResultSet.class);
            ResultSet rsStock = mock(ResultSet.class);
            ResultSet rsAuditoria = mock(ResultSet.class);

            if (categoria == AjusteExistencias.Categoria.MEDICAMENTO) {
                PreparedStatement lote = mock(PreparedStatement.class);
                ResultSet rsLote = mock(ResultSet.class);
                when(con.prepareStatement(anyString())).thenReturn(
                        permiso, stock, lote, actualizar, auditoria);
                when(lote.executeQuery()).thenReturn(rsLote);
                when(rsLote.next()).thenReturn(true);
                when(rsLote.getInt(1)).thenReturn(10);
            } else {
                when(con.prepareStatement(anyString())).thenReturn(
                        permiso, stock, actualizar, auditoria);
            }

            when(permiso.executeQuery()).thenReturn(rsPermiso);
            when(rsPermiso.next()).thenReturn(true);
            when(stock.executeQuery()).thenReturn(rsStock);
            when(rsStock.next()).thenReturn(true);
            when(rsStock.getInt(1)).thenReturn(10);

            when(actualizar.executeUpdate()).thenReturn(1);
            when(con.getAutoCommit()).thenReturn(false);
            when(auditoria.executeQuery()).thenReturn(rsAuditoria);
            when(rsAuditoria.next()).thenReturn(true);
            when(rsAuditoria.getLong(1)).thenReturn(85L);

            try (var bd = mockStatic(ConexionBD.class);
                 var usuario = mockStatic(SesionUsuario.class);
                 var cache = mockStatic(InventarioConsultas.class)) {

                bd.when(ConexionBD::getConexion).thenReturn(con);
                usuario.when(SesionUsuario::estaActiva).thenReturn(true);
                usuario.when(SesionUsuario::actual).thenReturn(sesion());

                var resultado = new ServicioAjustesInventario()
                        .ajustar(ajuste(categoria, 10, 15));

                assertEquals(85L, resultado.idAuditoria());
                assertEquals(10, resultado.anterior());
                assertEquals(15, resultado.nuevo());
                assertEquals(5, resultado.diferencia());
                assertEquals(categoria, resultado.categoria());

                verify(con).commit();
                verify(con, never()).rollback();
                cache.verify(() ->
                        InventarioConsultas.invalidarFarmacia(7));
            }
        }
    }

    @Test
    void saldoConcurrenteDistintoProvocaRollback() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement permiso = mock(PreparedStatement.class);
        PreparedStatement stock = mock(PreparedStatement.class);
        ResultSet rsPermiso = mock(ResultSet.class);
        ResultSet rsStock = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenReturn(permiso, stock);
        when(permiso.executeQuery()).thenReturn(rsPermiso);
        when(rsPermiso.next()).thenReturn(true);
        when(stock.executeQuery()).thenReturn(rsStock);
        when(rsStock.next()).thenReturn(true);
        when(rsStock.getInt(1)).thenReturn(8);

        try (var bd = mockStatic(ConexionBD.class);
             var usuario = mockStatic(SesionUsuario.class);
             var cache = mockStatic(InventarioConsultas.class)) {

            bd.when(ConexionBD::getConexion).thenReturn(con);
            usuario.when(SesionUsuario::estaActiva).thenReturn(true);
            usuario.when(SesionUsuario::actual).thenReturn(sesion());

            assertThrows(ReglaNegocioException.class, () ->
                    new ServicioAjustesInventario().ajustar(
                            ajuste(AjusteExistencias.Categoria.ASEO,
                                    10, 15)));

            verify(con).rollback();
            verify(con, never()).commit();
            cache.verifyNoInteractions();
        }
    }

    @Test
    void usuarioDeOtraFarmaciaNoPuedeAjustar() {
        try (var usuario = mockStatic(SesionUsuario.class);
             var bd = mockStatic(ConexionBD.class)) {

            usuario.when(SesionUsuario::estaActiva).thenReturn(true);
            usuario.when(SesionUsuario::actual).thenReturn(sesion());

            AjusteExistencias aj = new AjusteExistencias(
                    AjusteExistencias.Categoria.ASEO,
                    12, 9, 99, "Conteo", 5, 6);

            assertThrows(ReglaNegocioException.class, () ->
                    new ServicioAjustesInventario().ajustar(aj));

            bd.verifyNoInteractions();
        }
    }

    @Test
    void noPermiteAjustesSinSesion() {
        try (var usuario = mockStatic(SesionUsuario.class);
             var bd = mockStatic(ConexionBD.class)) {

            usuario.when(SesionUsuario::estaActiva).thenReturn(false);

            assertThrows(ReglaNegocioException.class, () ->
                    new ServicioAjustesInventario().ajustar(
                            ajuste(AjusteExistencias.Categoria.MATERNIDAD,
                                    5, 8)));

            bd.verifyNoInteractions();
        }
    }
}
