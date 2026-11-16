package org.example.carestock.facade;

import org.example.carestock.model.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InventarioCoberturaComplementariaFase3Test {

    @Test
    void rechazaUsuarioSinAutenticacion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Inventario(null));

        Usuario usuario = new Usuario();

        assertThrows(
                IllegalArgumentException.class,
                () -> new Inventario(usuario));
    }

    @Test
    void aceptaUsuarioConIdentificadorValido() {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(101);

        assertNotNull(new Inventario(usuario));
    }
}
