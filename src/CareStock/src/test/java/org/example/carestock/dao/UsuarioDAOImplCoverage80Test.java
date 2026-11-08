package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class UsuarioDAOImplCoverage80Test {

    private Connection conexion;
    private PreparedStatement sentencia;
    private ResultSet resultado;
    private MockedStatic<ConexionBD> baseDatos;

    @BeforeEach
    void preparar() throws Exception {
        conexion = mock(Connection.class);
        sentencia = mock(PreparedStatement.class);
        resultado = mock(ResultSet.class);

        when(conexion.prepareStatement(anyString()))
            .thenReturn(sentencia);

        when(sentencia.executeQuery())
            .thenReturn(resultado);

        baseDatos = mockStatic(ConexionBD.class);
        baseDatos.when(ConexionBD::getConexion)
            .thenReturn(conexion);
    }

    @AfterEach
    void cerrar() {
        baseDatos.close();
    }

    @Test
    void encuentraUsuarioActivoYMapeaCampos()
            throws Exception {
        when(resultado.next()).thenReturn(true);

        when(resultado.getInt("id_usuario"))
            .thenReturn(15);
        when(resultado.getString("nombre_completo"))
            .thenReturn("Administrador QA");
        when(resultado.getString("email"))
            .thenReturn("admin@qa.test");
        when(resultado.getString("password_hash"))
            .thenReturn("HASH_DE_PRUEBA");
        when(resultado.getString("id_rol"))
            .thenReturn("2");

        Usuario usuario = new UsuarioDAOImpl()
            .buscarPorEmail("admin@qa.test");

        assertNotNull(usuario);
        assertEquals(15, usuario.getIdUsuario());
        assertEquals("Administrador QA", usuario.getNombre());
        assertEquals("admin@qa.test", usuario.getEmail());
        assertEquals("HASH_DE_PRUEBA", usuario.getPassword());
        assertEquals("2", usuario.getRol());

        verify(resultado).close();
        verify(sentencia).close();
        verify(conexion).close();
    }

    @Test
    void devuelveNullCuandoNoExisteUsuario()
            throws Exception {
        when(resultado.next()).thenReturn(false);

        Usuario usuario = new UsuarioDAOImpl()
            .buscarPorEmail("inexistente@qa.test");

        assertNull(usuario);
        verify(sentencia).setString(
            1, "inexistente@qa.test");
        verify(resultado).close();
    }

    @Test
    void eliminaEspaciosDelEmailConsultado()
            throws Exception {
        when(resultado.next()).thenReturn(false);

        new UsuarioDAOImpl()
            .buscarPorEmail("  admin@qa.test  ");

        verify(sentencia).setString(
            1, "admin@qa.test");
    }

    @Test
    void consultaSoloUsuariosActivos() throws Exception {
        when(resultado.next()).thenReturn(false);

        new UsuarioDAOImpl()
            .buscarPorEmail("admin@qa.test");

        var consulta = org.mockito.ArgumentCaptor
            .forClass(String.class);

        verify(conexion).prepareStatement(
            consulta.capture());

        assertTrue(consulta.getValue()
            .contains("UPPER(estado) = 'ACTIVO'"));
    }

    @Test
    void propagaExcepcionSqlYCierraRecursos()
            throws Exception {
        when(sentencia.executeQuery())
            .thenThrow(new SQLException("Error JDBC QA"));

        SQLException error = assertThrows(
            SQLException.class,
            () -> new UsuarioDAOImpl()
                .buscarPorEmail("admin@qa.test"));

        assertEquals("Error JDBC QA", error.getMessage());

        verify(sentencia).close();
        verify(conexion).close();
    }

    @Test
    void rechazaEmailNuloSinFiltrarCredenciales()
            throws Exception {
        assertThrows(NullPointerException.class,
            () -> new UsuarioDAOImpl()
                .buscarPorEmail(null));

        verify(sentencia, never()).executeQuery();
        verify(conexion).close();
    }
}
