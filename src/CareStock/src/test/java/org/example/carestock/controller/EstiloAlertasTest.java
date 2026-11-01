package org.example.carestock.controller;

import org.example.carestock.controller.EstiloAlertas.Nivel;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EstiloAlertasTest {

    @Test
    void severidadCriticaEsNivelCritico() {
        assertEquals(Nivel.CRITICO, EstiloAlertas.nivel(Severidad.CRITICA));
    }

    @Test
    void severidadAdvertenciaEsNivelAdvertencia() {
        assertEquals(Nivel.ADVERTENCIA, EstiloAlertas.nivel(Severidad.ADVERTENCIA));
    }

    @Test
    void severidadNulaNoTieneNivel() {
        assertEquals(Nivel.NINGUNO, EstiloAlertas.nivel(null));
    }

    @Test
    void sinExistenciasEsCriticoYConExistenciasEsAdvertencia() {
        assertEquals(Nivel.CRITICO, EstiloAlertas.nivelPorExistencias(0));
        assertEquals(Nivel.CRITICO, EstiloAlertas.nivelPorExistencias(-1));
        assertEquals(Nivel.ADVERTENCIA, EstiloAlertas.nivelPorExistencias(1));
    }

    @Test
    void filaCriticaUsaCoralConTextoOscuroYNegrita() {
        String estilo = EstiloAlertas.estiloFila(Nivel.CRITICO, false);

        assertTrue(estilo.contains("-fx-background-color: #F0A8A8"));
        assertTrue(estilo.contains("-fx-text-fill: #7A2E2E"));
        assertTrue(estilo.contains("-fx-font-weight: bold"));
    }

    @Test
    void filaAdvertenciaUsaDurazno() {
        String estilo = EstiloAlertas.estiloFila(Nivel.ADVERTENCIA, false);

        assertTrue(estilo.contains("-fx-background-color: #F6CE8E"));
        assertTrue(estilo.contains("-fx-text-fill: #20302F"));
        assertFalse(estilo.contains("-fx-font-weight"));
    }

    @Test
    void filaSinNivelNoTieneEstilo() {
        assertEquals("", EstiloAlertas.estiloFila(Nivel.NINGUNO, false));
        assertEquals("", EstiloAlertas.estiloFila(Nivel.NINGUNO, true));
        assertEquals("", EstiloAlertas.estiloFila(null, false));
    }

    @Test
    void filaSeleccionadaUsaLavandaEnCualquierNivel() {
        assertTrue(EstiloAlertas.estiloFila(Nivel.CRITICO, true)
                .contains("-fx-background-color: #B7A6E0"));
        assertTrue(EstiloAlertas.estiloFila(Nivel.ADVERTENCIA, true)
                .contains("-fx-background-color: #B7A6E0"));
    }

    @Test
    void insigniaPriorizaCriticasSobreAdvertencias() {
        assertTrue(EstiloAlertas.estiloInsignia(2, 1).contains("-fx-background-color: #F0A8A8"));
        assertTrue(EstiloAlertas.estiloInsignia(2, 1).contains("-fx-text-fill: #7A2E2E"));
        assertTrue(EstiloAlertas.estiloInsignia(0, 2).contains("-fx-background-color: #F6CE8E"));
        assertTrue(EstiloAlertas.estiloInsignia(0, 2).contains("-fx-text-fill: #20302F"));
    }

    @Test
    void insigniaSinAlertasUsaLavandaClaro() {
        String estilo = EstiloAlertas.estiloInsignia(0, 0);

        assertTrue(estilo.contains("-fx-background-color: #EDE7F7"));
        assertTrue(estilo.contains("-fx-text-fill: #6B5B95"));
        assertTrue(estilo.contains("-fx-background-radius: 8"));
    }

    @Test
    void tooltipDeAlertaUsaCoralConTextoOscuro() {
        assertTrue(EstiloAlertas.ESTILO_TOOLTIP_ALERTA.contains("-fx-background-color: #F0A8A8"));
        assertTrue(EstiloAlertas.ESTILO_TOOLTIP_ALERTA.contains("-fx-text-fill: #7A2E2E"));
    }

    @Test
    void ningunEstiloUsaTextoBlancoSobreUnPastel() {
        List<String> estilos = List.of(
                EstiloAlertas.estiloFila(Nivel.CRITICO, false),
                EstiloAlertas.estiloFila(Nivel.ADVERTENCIA, false),
                EstiloAlertas.estiloFila(Nivel.CRITICO, true),
                EstiloAlertas.estiloInsignia(1, 0),
                EstiloAlertas.estiloInsignia(0, 1),
                EstiloAlertas.estiloInsignia(0, 0),
                EstiloAlertas.ESTILO_TOOLTIP_ALERTA);

        for (String estilo : estilos) {
            String minusculas = estilo.toLowerCase();
            assertFalse(minusculas.contains("white"), estilo);
            assertFalse(minusculas.contains("#ffffff"), estilo);
        }
    }
}
