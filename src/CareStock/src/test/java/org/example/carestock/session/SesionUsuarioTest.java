package org.example.carestock.session;

import org.example.carestock.model.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class SesionUsuarioTest {

    private void establecerSesion(
            SesionUsuario.DatosUsuario datos) throws Exception {
        Field campo = SesionUsuario.class.getDeclaredField("actual");
        campo.setAccessible(true);
        campo.set(null, datos);
    }

    @AfterEach
    void limpiar() throws Exception {
        establecerSesion(null);
    }

    @Test
    void rechazaAccesoSinSesionActiva() {
        assertFalse(SesionUsuario.estaActiva());
        assertFalse(SesionUsuario.esAdministrador());

        assertThrows(IllegalStateException.class,
                SesionUsuario::actual);

        assertThrows(IllegalStateException.class,
                SesionUsuario::usuarioParaFachada);
    }

    @Test
    void reconoceAdministradorSinImportarMayusculas() {
        var administrador = new SesionUsuario.DatosUsuario(
                1, "Ana", "ana@test.local",
                " administrador ", 2, "Farmacia Central");

        assertTrue(administrador.esAdministrador());
    }

    @Test
    void rechazaRolesNoAdministradores() {
        assertFalse(new SesionUsuario.DatosUsuario(
                1, "Ana", "a@test.local", null, 2, "Farmacia").esAdministrador());

        assertFalse(new SesionUsuario.DatosUsuario(
                1, "Ana", "a@test.local",
                "FARMACEUTICO", 2, "Farmacia").esAdministrador());
    }

    @Test
    void obtieneDatosDeSesionActiva() throws Exception {
        var datos = new SesionUsuario.DatosUsuario(
                10, "Luis", "luis@test.local",
                "ADMINISTRADOR", 3, "Farmacia Norte");

        establecerSesion(datos);

        assertTrue(SesionUsuario.estaActiva());
        assertTrue(SesionUsuario.esAdministrador());
        assertEquals(datos, SesionUsuario.actual());
        assertEquals(3, SesionUsuario.actual().idFarmacia());
    }

    @Test
    void fachadaRecibeIdentificadorDelUsuario() throws Exception {
        establecerSesion(new SesionUsuario.DatosUsuario(
                15, "Ana", "ana@test.local",
                "ADMINISTRADOR", 2, "Farmacia"));

        Usuario usuario = SesionUsuario.usuarioParaFachada();

        assertEquals(15, usuario.getIdUsuario());
        assertNull(usuario.getPassword());
    }

    @Test
    void rechazaIniciarSesionConUsuarioInvalido() {
        assertThrows(IllegalArgumentException.class,
                () -> SesionUsuario.iniciar(null));

        Usuario invalido = new Usuario();
        invalido.setIdUsuario(0);

        assertThrows(IllegalArgumentException.class,
                () -> SesionUsuario.iniciar(invalido));
    }

    @Test
    void cerrarSesionEliminaAutorizacion() throws Exception {
        establecerSesion(new SesionUsuario.DatosUsuario(
                1, "Ana", "ana@test.local",
                "ADMINISTRADOR", 1, "Farmacia"));

        SesionUsuario.cerrar();

        assertFalse(SesionUsuario.estaActiva());
        assertFalse(SesionUsuario.esAdministrador());
        assertThrows(IllegalStateException.class,
                SesionUsuario::actual);
    }
}
