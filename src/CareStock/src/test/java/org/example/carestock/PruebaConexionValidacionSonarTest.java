package org.example.carestock;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/** Casos de validacion que no deben llegar a consultar la base de datos. */
class PruebaConexionValidacionSonarTest {

    @Test
    void sinArgumentosInformaUsoSinConectar() {
        assertDoesNotThrow(() -> PruebaConexion.main(new String[0]));
    }

    @Test
    void cantidadDeArgumentosIncorrectaNoConsultaLaBase() {
        assertDoesNotThrow(() -> PruebaConexion.main(new String[]{"1", "2"}));
    }

    @Test
    void identificadorNoNumericoNoConsultaLaBase() {
        assertDoesNotThrow(() -> PruebaConexion.main(new String[]{"no-numerico"}));
    }

    @Test
    void identificadorNoPositivoNoConsultaLaBase() {
        assertDoesNotThrow(() -> PruebaConexion.main(new String[]{"0"}));
    }
}
