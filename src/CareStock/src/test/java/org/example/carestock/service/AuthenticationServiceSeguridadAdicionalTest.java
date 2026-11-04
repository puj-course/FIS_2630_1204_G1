package org.example.carestock.service;

import org.example.carestock.dao.UsuarioDAO;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Usuario;

import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.lang.reflect.Field;
import java.sql.SQLTimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceSeguridadAdicionalTest {

    private static final String CLAVE = "Clave#Valida2026";

    private static final String HASH =
            BCrypt.hashpw(CLAVE, BCrypt.gensalt(4));

    private AuthenticationService servicio(
            UsuarioDAO dao) throws Exception {

        AuthenticationService auth = new AuthenticationService();

        Field campo = AuthenticationService.class
                .getDeclaredField("usuarioDAO");

        campo.setAccessible(true);
        campo.set(auth, dao);

        return auth;
    }

    private Usuario usuario(String correo, String hash) {
        return new Usuario(
                25, "Usuario de prueba", correo,
                hash, "FARMACEUTICO");
    }

    @Test
    void contrasenaEsSensibleAMayusculas() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);

        Usuario usuario = usuario("ana@test.local", HASH);

        when(dao.buscarPorEmail("ana@test.local"))
                .thenReturn(usuario);

        assertThrows(ReglaNegocioException.class,
                () -> servicio(dao).autenticar(
                        "ana@test.local", "clave#valida2026"));

        assertEquals(HASH, usuario.getPassword());
        verify(dao, times(1)).buscarPorEmail("ana@test.local");
    }

    @Test
    void hashTruncadoSeRechazaSinErrorInesperado() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);

        Usuario usuario = usuario(
                "ana@test.local", HASH.substring(0, 31));

        when(dao.buscarPorEmail("ana@test.local"))
                .thenReturn(usuario);

        assertThrows(ReglaNegocioException.class,
                () -> servicio(dao).autenticar(
                        "ana@test.local", CLAVE));
    }

    @Test
    void hashNoPuedeUsarseComoContrasena() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);

        when(dao.buscarPorEmail("ana@test.local"))
                .thenReturn(usuario("ana@test.local", HASH));

        assertThrows(ReglaNegocioException.class,
                () -> servicio(dao).autenticar(
                        "ana@test.local", HASH));
    }

    @Test
    void respetaEspaciosSignificativosEnContrasena()
            throws Exception {

        UsuarioDAO dao = mock(UsuarioDAO.class);

        String claveConEspacios = "  Clave con espacios  ";
        String hash = BCrypt.hashpw(
                claveConEspacios, BCrypt.gensalt(4));

        Usuario usuario = usuario("ana@test.local", hash);

        when(dao.buscarPorEmail("ana@test.local"))
                .thenReturn(usuario);

        Usuario resultado = servicio(dao).autenticar(
                "ana@test.local", claveConEspacios);

        assertSame(usuario, resultado);
        assertNull(resultado.getPassword());
    }

    @Test
    void noRevelaSiLaCuentaExiste() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);

        when(dao.buscarPorEmail("desconocido@test.local"))
                .thenReturn(null);

        when(dao.buscarPorEmail("ana@test.local"))
                .thenReturn(usuario("ana@test.local", HASH));

        AuthenticationService auth = servicio(dao);

        var errorSinUsuario = assertThrows(
                ReglaNegocioException.class,
                () -> auth.autenticar(
                        "desconocido@test.local", CLAVE));

        var errorConClaveIncorrecta = assertThrows(
                ReglaNegocioException.class,
                () -> auth.autenticar(
                        "ana@test.local", "clave-incorrecta"));

        assertEquals(
                errorSinUsuario.getMessage(),
                errorConClaveIncorrecta.getMessage());

        assertFalse(errorConClaveIncorrecta.getMessage()
                .contains("ana@test.local"));

        assertFalse(errorConClaveIncorrecta.getMessage()
                .contains(HASH));
    }

    @Test
    void errorDeBaseDeDatosNoAutorizaIngreso()
            throws Exception {

        UsuarioDAO dao = mock(UsuarioDAO.class);

        SQLTimeoutException error =
                new SQLTimeoutException("Conexion agotada");

        when(dao.buscarPorEmail("ana@test.local"))
                .thenThrow(error);

        Exception recibido = assertThrows(
                Exception.class,
                () -> servicio(dao).autenticar(
                        "ana@test.local", CLAVE));

        assertSame(error, recibido);
    }

    @Test
    void autenticaUsuariosIndependientesSinMezclarCredenciales()
            throws Exception {

        UsuarioDAO dao = mock(UsuarioDAO.class);

        String hashAna = BCrypt.hashpw(
                "clave-ana", BCrypt.gensalt(4));

        String hashLuis = BCrypt.hashpw(
                "clave-luis", BCrypt.gensalt(4));

        Usuario ana = usuario("ana@test.local", hashAna);
        Usuario luis = usuario("luis@test.local", hashLuis);
        luis.setIdUsuario(26);

        when(dao.buscarPorEmail("ana@test.local"))
                .thenReturn(ana);

        when(dao.buscarPorEmail("luis@test.local"))
                .thenReturn(luis);

        AuthenticationService auth = servicio(dao);

        assertSame(ana, auth.autenticar(
                "ana@test.local", "clave-ana"));

        assertSame(luis, auth.autenticar(
                "luis@test.local", "clave-luis"));

        assertNull(ana.getPassword());
        assertNull(luis.getPassword());

        verify(dao).buscarPorEmail("ana@test.local");
        verify(dao).buscarPorEmail("luis@test.local");
        verifyNoMoreInteractions(dao);
    }
}
