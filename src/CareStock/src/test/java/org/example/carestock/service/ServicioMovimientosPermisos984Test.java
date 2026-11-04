package org.example.carestock.service;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.example.carestock.service.ServicioMovimientosProducto.Categoria.ASEO;
import static org.example.carestock.service.ServicioMovimientosProducto.TipoMovimiento.ENTRADA;

class ServicioMovimientosPermisos984Test {

    private ServicioMovimientosProducto servicio() {
        return new ServicioMovimientosProducto();
    }

    @Test
    void auxiliarNoPuedeRegistrarYNoSeAbreConexion() {
        try (MockedStatic<SesionUsuario> sesion = mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> db = mockStatic(ConexionBD.class)) {
            sesion.when(SesionUsuario::actual).thenReturn(
                    new SesionUsuario.DatosUsuario(7, "Aux", "a@t.test", "AUXILIAR", 4, "Farmacia"));

            assertThrows(ReglaNegocioException.class, () ->
                    servicio().registrar(ASEO, 21, ENTRADA, 3, "Reposición", 7, 4));
            db.verifyNoInteractions();
        }
    }

    @Test
    void cambioDeFarmaciaBloqueaAntesDeConectar() {
        try (MockedStatic<SesionUsuario> sesion = mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> db = mockStatic(ConexionBD.class)) {
            sesion.when(SesionUsuario::actual).thenReturn(
                    new SesionUsuario.DatosUsuario(7, "Farm", "a@t.test", "FARMACEUTICO", 5, "Otra"));

            assertThrows(ReglaNegocioException.class, () ->
                    servicio().registrar(ASEO, 21, ENTRADA, 3, "Reposición", 7, 4));
            db.verifyNoInteractions();
        }
    }

    @Test
    void revalidaRolYFarmaciaContraLaBaseAntesDeEscribir() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (MockedStatic<SesionUsuario> sesion = mockStatic(SesionUsuario.class);
             MockedStatic<ConexionBD> db = mockStatic(ConexionBD.class)) {
            sesion.when(SesionUsuario::actual).thenReturn(
                    new SesionUsuario.DatosUsuario(7, "Farm", "a@t.test", "FARMACEUTICO", 4, "Farmacia"));
            db.when(ConexionBD::getConexion).thenReturn(con);

            assertThrows(ReglaNegocioException.class, () ->
                    servicio().registrar(ASEO, 21, ENTRADA, 3, "Reposición", 7, 4));
            verify(con).prepareStatement(contains("INNER JOIN roles"));
            verify(con).rollback();
            verify(con, never()).commit();
            verify(ps, never()).executeUpdate();
        }
    }
}
