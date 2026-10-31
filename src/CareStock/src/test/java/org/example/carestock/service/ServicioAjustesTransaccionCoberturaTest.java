package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.AjusteExistencias;
import org.example.carestock.model.AjusteExistencias.Categoria;
import org.example.carestock.proxy.InventarioConsultas;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ServicioAjustesTransaccionCoberturaTest {

    private void simularAdministrador(
            MockedStatic<SesionUsuario> sesion) {

        sesion.when(SesionUsuario::estaActiva)
                .thenReturn(true);

        sesion.when(SesionUsuario::actual)
                .thenReturn(new SesionUsuario.DatosUsuario(
                        10, "Administrador",
                        "admin@test.local",
                        "ADMINISTRADOR", 7,
                        "Farmacia Central"));
    }

    @Test
    void ajusteDeAseoConfirmaAuditoriaEInvalidaCache()
            throws Exception {

        Connection con = mock(Connection.class);

        PreparedStatement autorizacion =
                mock(PreparedStatement.class);
        PreparedStatement bloqueo =
                mock(PreparedStatement.class);
        PreparedStatement actualizacion =
                mock(PreparedStatement.class);
        PreparedStatement auditoria =
                mock(PreparedStatement.class);

        ResultSet rsAutorizacion = mock(ResultSet.class);
        ResultSet rsStock = mock(ResultSet.class);
        ResultSet rsAuditoria = mock(ResultSet.class);

        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> bd =
                     mockStatic(ConexionBD.class);
             MockedStatic<InventarioConsultas> cache =
                     mockStatic(InventarioConsultas.class)) {

            simularAdministrador(sesion);

            bd.when(ConexionBD::getConexion)
                    .thenReturn(con);

            when(con.prepareStatement(anyString()))
                    .thenReturn(
                            autorizacion,
                            bloqueo,
                            actualizacion,
                            auditoria);

            when(autorizacion.executeQuery())
                    .thenReturn(rsAutorizacion);
            when(rsAutorizacion.next()).thenReturn(true);

            when(bloqueo.executeQuery())
                    .thenReturn(rsStock);
            when(rsStock.next()).thenReturn(true);
            when(rsStock.getInt(1)).thenReturn(5);

            when(actualizacion.executeUpdate())
                    .thenReturn(1);

            when(auditoria.executeQuery())
                    .thenReturn(rsAuditoria);
            when(rsAuditoria.next()).thenReturn(true);
            when(rsAuditoria.getLong(1)).thenReturn(123L);

            AjusteExistencias ajuste =
                    new AjusteExistencias(
                            Categoria.ASEO,
                            25, 10, 7,
                            "Conteo fisico", 5, 8);

            var resultado =
                    new ServicioAjustesInventario()
                            .ajustar(ajuste);

            assertEquals(123L, resultado.idAuditoria());
            assertEquals(5, resultado.anterior());
            assertEquals(8, resultado.nuevo());
            assertEquals(3, resultado.diferencia());
            assertEquals(7, resultado.idFarmacia());

            verify(con).setAutoCommit(false);
            verify(con).commit();
            verify(con, never()).rollback();

            cache.verify(() ->
                    InventarioConsultas.invalidarFarmacia(7));

            verify(autorizacion).setInt(1, 10);
            verify(autorizacion).setInt(2, 7);
            verify(actualizacion).executeUpdate();
            verify(auditoria).executeQuery();
            verify(con).close();
        }
    }

    @Test
    void stockConcurrenteProvocaRollbackSinInvalidarCache()
            throws Exception {

        Connection con = mock(Connection.class);

        PreparedStatement autorizacion =
                mock(PreparedStatement.class);
        PreparedStatement bloqueo =
                mock(PreparedStatement.class);

        ResultSet rsAutorizacion = mock(ResultSet.class);
        ResultSet rsStock = mock(ResultSet.class);

        try (MockedStatic<SesionUsuario> sesion =
                     mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> bd =
                     mockStatic(ConexionBD.class);
             MockedStatic<InventarioConsultas> cache =
                     mockStatic(InventarioConsultas.class)) {

            simularAdministrador(sesion);

            bd.when(ConexionBD::getConexion)
                    .thenReturn(con);

            when(con.prepareStatement(anyString()))
                    .thenReturn(autorizacion, bloqueo);

            when(autorizacion.executeQuery())
                    .thenReturn(rsAutorizacion);
            when(rsAutorizacion.next()).thenReturn(true);

            when(bloqueo.executeQuery())
                    .thenReturn(rsStock);
            when(rsStock.next()).thenReturn(true);
            when(rsStock.getInt(1)).thenReturn(9);

            AjusteExistencias ajuste =
                    new AjusteExistencias(
                            Categoria.ASEO,
                            25, 10, 7,
                            "Conteo fisico", 5, 8);

            assertThrows(
                    ReglaNegocioException.class,
                    () -> new ServicioAjustesInventario()
                            .ajustar(ajuste));

            verify(con).rollback();
            verify(con, never()).commit();

            cache.verifyNoInteractions();

            verify(con).close();
        }
    }
}
