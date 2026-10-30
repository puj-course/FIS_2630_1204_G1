package org.example.carestock.DataTransferObject;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LecturaCamposTest {
    @Test
    void interpretaPreciosValidos() {
        assertEquals(1200.50, LecturaCampos.precio(" 1200.50 "), 0.001);
        assertEquals(0, LecturaCampos.precio("0"), 0.001);
    }

    @Test
    void rechazaPreciosInvalidos() {
        for (String texto : new String[]{null, " ", "abc", "-1", "NaN", "Infinity"}) {
            assertThrows(IllegalArgumentException.class, () -> LecturaCampos.precio(texto));
        }
    }

    @Test
    void interpretaEnterosValidos() {
        assertEquals(10, LecturaCampos.entero(" 10 ", "Stock"));
        assertEquals(0, LecturaCampos.entero("0", "Stock"));
        assertEquals(0, LecturaCampos.enteroOpcional(null, "Stock"));
        assertEquals(0, LecturaCampos.enteroOpcional(" ", "Stock"));
        assertEquals(5, LecturaCampos.enteroOpcional("5", "Stock"));
    }

    @Test
    void rechazaEnterosInvalidos() {
        for (String texto : new String[]{null, " ", "-1", "uno", "2.5"}) {
            assertThrows(IllegalArgumentException.class,
                    () -> LecturaCampos.entero(texto, "Stock"));
        }
        assertThrows(IllegalArgumentException.class,
                () -> LecturaCampos.enteroOpcional("abc", "Stock"));
    }
}
