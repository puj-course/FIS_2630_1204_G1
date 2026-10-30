package org.example.carestock.service;

import org.example.carestock.dao.UsuarioDAO;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Usuario;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {

    private AuthenticationService servicio(UsuarioDAO dao) throws Exception {
        AuthenticationService servicio = new AuthenticationService();
        Field campo = AuthenticationService.class.getDeclaredField("usuarioDAO");
        campo.setAccessible(true);
        campo.set(servicio, dao);
        return servicio;
    }

    @Test
    void rechazaCredencialesVaciasONulas() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);
        AuthenticationService servicio = servicio(dao);

        assertThrows(ReglaNegocioException.class,
                () -> servicio.autenticar(null, "123"));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.autenticar(" ", "123"));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.autenticar("ana@test.local", null));
        assertThrows(ReglaNegocioException.class,
                () -> servicio.autenticar("ana@test.local", " "));

        verifyNoInteractions(dao);
    }

    @Test
    void rechazaUsuarioInexistente() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);
        when(dao.buscarPorEmail("desconocido@test.local")).thenReturn(null);

        assertThrows(ReglaNegocioException.class,
                () -> servicio(dao).autenticar("desconocido@test.local", "clave"));
    }

    @Test
    void permiteAutenticacionConBcryptValido() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);
        String hash = BCrypt.hashpw("clave-segura", BCrypt.gensalt(4));

        Usuario usuario = new Usuario(
                5, "Ana", "ana@test.local", hash, "ADMINISTRADOR");

        when(dao.buscarPorEmail("ana@test.local")).thenReturn(usuario);

        Usuario autenticado = servicio(dao)
                .autenticar("ana@test.local", "clave-segura");

        assertEquals(5, autenticado.getIdUsuario());
        assertEquals("Ana", autenticado.getNombre());
        assertNull(autenticado.getPassword(),
                "El hash no debe conservarse tras autenticar");
    }

    @Test
    void rechazaContrasenaIncorrecta() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);
        String hash = BCrypt.hashpw("correcta", BCrypt.gensalt(4));

        when(dao.buscarPorEmail("ana@test.local")).thenReturn(
                new Usuario(5, "Ana", "ana@test.local", hash, "ADMINISTRADOR"));

        assertThrows(ReglaNegocioException.class,
                () -> servicio(dao).autenticar("ana@test.local", "incorrecta"));
    }

    @Test
    void rechazaContrasenaSinHashBcrypt() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);

        when(dao.buscarPorEmail("ana@test.local")).thenReturn(
                new Usuario(5, "Ana", "ana@test.local",
                        "password-plano", "ADMINISTRADOR"));

        assertThrows(ReglaNegocioException.class,
                () -> servicio(dao).autenticar("ana@test.local", "password-plano"));
    }

    @Test
    void rechazaHashNuloOMalformado() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);
        Usuario usuario = new Usuario(
                5, "Ana", "ana@test.local", null, "ADMINISTRADOR");

        when(dao.buscarPorEmail("ana@test.local")).thenReturn(usuario);
        AuthenticationService autenticacion = servicio(dao);

        assertThrows(ReglaNegocioException.class,
                () -> autenticacion.autenticar("ana@test.local", "clave"));

        usuario.setPassword("$2z$10$hash-invalido");
        assertThrows(ReglaNegocioException.class,
                () -> autenticacion.autenticar("ana@test.local", "clave"));
    }

    @Test
    void propagaErroresDelRepositorio() throws Exception {
        UsuarioDAO dao = mock(UsuarioDAO.class);
        when(dao.buscarPorEmail("ana@test.local"))
                .thenThrow(new IllegalStateException("Repositorio no disponible"));

        assertThrows(IllegalStateException.class,
                () -> servicio(dao).autenticar("ana@test.local", "clave"));
    }
}
