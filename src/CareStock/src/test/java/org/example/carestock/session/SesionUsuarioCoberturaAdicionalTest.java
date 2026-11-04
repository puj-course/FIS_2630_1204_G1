package org.example.carestock.session;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Usuario;
import org.example.carestock.proxy.InventarioConsultas;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SesionUsuarioCoberturaAdicionalTest {

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(42);
        return usuario;
    }

    private void establecerSesion(
            SesionUsuario.DatosUsuario datos) throws Exception {

        Field campo = SesionUsuario.class
                .getDeclaredField("actual");

        campo.setAccessible(true);
        campo.set(null, datos);
    }

    @AfterEach
    void limpiar() throws Exception {
        establecerSesion(null);
    }

    @Test
    void iniciaSesionConFarmaciaYRolDesdeJdbc()
            throws Exception {

        Connection conexion = mock(Connection.class);
        PreparedStatement sentencia = mock(PreparedStatement.class);
        ResultSet resultado = mock(ResultSet.class);

        when(conexion.prepareStatement(anyString()))
                .thenReturn(sentencia);

        when(sentencia.executeQuery()).thenReturn(resultado);
        when(resultado.next()).thenReturn(true);
        when(resultado.getInt("id_usuario")).thenReturn(42);
        when(resultado.getInt("id_farmacia")).thenReturn(8);
        when(resultado.wasNull()).thenReturn(false);

        when(resultado.getString("nombre_completo"))
                .thenReturn("Ana Perez");
        when(resultado.getString("email"))
                .thenReturn("ana@test.local");
        when(resultado.getString("nombre_rol"))
                .thenReturn("ADMINISTRADOR");
        when(resultado.getString("farmacia_nombre"))
                .thenReturn("Farmacia Norte");

        try (MockedStatic<ConexionBD> db =
                     mockStatic(ConexionBD.class);
             MockedStatic<InventarioConsultas> cache =
                     mockStatic(InventarioConsultas.class)) {

            db.when(ConexionBD::getConexion)
                    .thenReturn(conexion);

            SesionUsuario.iniciar(usuario());

            assertTrue(SesionUsuario.estaActiva());
            assertTrue(SesionUsuario.esAdministrador());

            var datos = SesionUsuario.actual();

            assertEquals(42, datos.id());
            assertEquals("Ana Perez", datos.nombre());
            assertEquals("ana@test.local", datos.email());
            assertEquals("ADMINISTRADOR", datos.rol());
            assertEquals(8, datos.idFarmacia());
            assertEquals("Farmacia Norte", datos.farmacia());

            verify(sentencia).setInt(1, 42);
            verify(resultado).close();
            verify(sentencia).close();
            verify(conexion).close();

            cache.verify(InventarioConsultas::invalidar);
        }
    }

    @Test
    void permiteUsuarioSinFarmaciaPeroNoInventaUna()
            throws Exception {

        Connection conexion = mock(Connection.class);
        PreparedStatement sentencia = mock(PreparedStatement.class);
        ResultSet resultado = mock(ResultSet.class);

        when(conexion.prepareStatement(anyString()))
                .thenReturn(sentencia);
        when(sentencia.executeQuery()).thenReturn(resultado);
        when(resultado.next()).thenReturn(true);

        when(resultado.getInt("id_usuario")).thenReturn(42);
        when(resultado.wasNull()).thenReturn(true);
        when(resultado.getString("nombre_rol"))
                .thenReturn("FARMACEUTICO");

        try (MockedStatic<ConexionBD> db =
                     mockStatic(ConexionBD.class);
             MockedStatic<InventarioConsultas> cache =
                     mockStatic(InventarioConsultas.class)) {

            db.when(ConexionBD::getConexion)
                    .thenReturn(conexion);

            SesionUsuario.iniciar(usuario());

            assertTrue(SesionUsuario.estaActiva());
            assertNull(SesionUsuario.actual().idFarmacia());
            assertFalse(SesionUsuario.esAdministrador());

            cache.verify(InventarioConsultas::invalidar);
        }
    }

    @Test
    void usuarioInactivoNoConservaSesionAnterior()
            throws Exception {

        establecerSesion(new SesionUsuario.DatosUsuario(
                10, "Anterior", "anterior@test.local",
                "ADMINISTRADOR", 2, "Farmacia anterior"));

        Connection conexion = mock(Connection.class);
        PreparedStatement sentencia = mock(PreparedStatement.class);
        ResultSet resultado = mock(ResultSet.class);

        when(conexion.prepareStatement(anyString()))
                .thenReturn(sentencia);
        when(sentencia.executeQuery()).thenReturn(resultado);
        when(resultado.next()).thenReturn(false);

        try (MockedStatic<ConexionBD> db =
                     mockStatic(ConexionBD.class);
             MockedStatic<InventarioConsultas> cache =
                     mockStatic(InventarioConsultas.class)) {

            db.when(ConexionBD::getConexion)
                    .thenReturn(conexion);

            assertThrows(IllegalStateException.class,
                    () -> SesionUsuario.iniciar(usuario()));

            assertFalse(SesionUsuario.estaActiva());
            assertFalse(SesionUsuario.esAdministrador());

            assertThrows(IllegalStateException.class,
                    SesionUsuario::actual);

            cache.verify(InventarioConsultas::invalidar);
        }
    }

    @Test
    void errorJdbcNoDejaSesionAutorizada()
            throws Exception {

        establecerSesion(new SesionUsuario.DatosUsuario(
                10, "Anterior", "anterior@test.local",
                "ADMINISTRADOR", 2, "Farmacia"));

        SQLException fallo = new SQLException(
                "Fallo de conexion simulado");

        try (MockedStatic<ConexionBD> db =
                     mockStatic(ConexionBD.class);
             MockedStatic<InventarioConsultas> cache =
                     mockStatic(InventarioConsultas.class)) {

            db.when(ConexionBD::getConexion)
                    .thenThrow(fallo);

            SQLException recibida = assertThrows(
                    SQLException.class,
                    () -> SesionUsuario.iniciar(usuario()));

            assertSame(fallo, recibida);
            assertFalse(SesionUsuario.estaActiva());

            cache.verify(InventarioConsultas::invalidar);
        }
    }

    @Test
    void usuarioInvalidoNoReemplazaSesionExistente()
            throws Exception {

        var anterior = new SesionUsuario.DatosUsuario(
                10, "Ana", "ana@test.local",
                "ADMINISTRADOR", 2, "Farmacia");

        establecerSesion(anterior);

        Usuario invalido = new Usuario();
        invalido.setIdUsuario(-1);

        assertThrows(IllegalArgumentException.class,
                () -> SesionUsuario.iniciar(invalido));

        assertSame(anterior, SesionUsuario.actual());
    }
}
