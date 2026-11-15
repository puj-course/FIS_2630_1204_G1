package org.example.carestock.controller;

import org.example.carestock.dto.AlertaInventario.Severidad;

/**
 * Estilos de alerta con la paleta pastel oficial (HU-134).
 *
 * <p>Los colores de las filas y del boton "Alertas" se pintan desde Java y no se
 * pueden definir en el FXML. Esta clase no usa JavaFX: solo devuelve cadenas CSS,
 * por eso se puede probar sin abrir ninguna ventana.</p>
 *
 * <p>El color nunca es la unica senal (WCAG 2.1 AA): cada pantalla conserva el
 * texto de la alerta ("Critica", "Advertencia", "Sin existencias", etc.).</p>
 */
final class EstiloAlertas {

    private static final String CSS_FONDO = "-fx-background-color: ";
    private static final String CSS_TEXTO = "; -fx-text-fill: ";

    /** Nivel visual de una alerta. */
    enum Nivel {
        NINGUNO,
        ADVERTENCIA,
        CRITICO
    }

    static final String CORAL = "#F0A8A8";
    static final String DURAZNO = "#F6CE8E";
    static final String LAVANDA = "#B7A6E0";
    static final String LAVANDA_CLARO = "#EDE7F7";
    static final String TEXTO_CORAL = "#7A2E2E";
    static final String TEXTO_OSCURO = "#20302F";
    static final String TEXTO_LAVANDA = "#6B5B95";

    static final String ESTILO_TOOLTIP_ALERTA =
            CSS_FONDO + CORAL + CSS_TEXTO + TEXTO_CORAL
                    + "; -fx-font-weight: bold;";

    private static final String BOTON =
            " -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 16;";

    private EstiloAlertas() {
    }

    static Nivel nivel(Severidad severidad) {
        if (severidad == Severidad.CRITICA) {
            return Nivel.CRITICO;
        }
        if (severidad == Severidad.ADVERTENCIA) {
            return Nivel.ADVERTENCIA;
        }
        return Nivel.NINGUNO;
    }

    private static String estilo(String fondo, String texto, boolean negrita) {
        return CSS_FONDO + fondo
                + CSS_TEXTO + texto
                + "; -fx-text-background-color: " + texto + ";"
                + (negrita ? " -fx-font-weight: bold;" : "");
    }

    static String estiloFila(Nivel nivel, boolean seleccionada) {
        if (nivel == null || nivel == Nivel.NINGUNO) {
            return "";
        }
        if (seleccionada) {
            return estilo(LAVANDA, TEXTO_OSCURO, true);
        }
        if (nivel == Nivel.CRITICO) {
            return estilo(CORAL, TEXTO_CORAL, true);
        }
        return estilo(DURAZNO, TEXTO_OSCURO, false);
    }

    static String estiloInsignia(int criticas, int advertencias) {
        if (criticas > 0) {
            return CSS_FONDO + CORAL + CSS_TEXTO + TEXTO_CORAL + ";" + BOTON;
        }
        if (advertencias > 0) {
            return CSS_FONDO + DURAZNO + CSS_TEXTO + TEXTO_OSCURO + ";" + BOTON;
        }
        return CSS_FONDO + LAVANDA_CLARO + CSS_TEXTO + TEXTO_LAVANDA + ";" + BOTON;
    }

    static String estiloBotonAccion() {
        return CSS_FONDO + LAVANDA_CLARO
                + CSS_TEXTO + TEXTO_LAVANDA + ";" + BOTON;
    }

    static Nivel nivelPorExistencias(int existencias) {
        return existencias <= 0 ? Nivel.CRITICO : Nivel.ADVERTENCIA;
    }

    static Nivel nivelStockCatalogo(int stock, Integer minimo) {
        if (stock <= 0) return Nivel.CRITICO;
        if (minimo != null && minimo > 0 && stock < minimo) return Nivel.ADVERTENCIA;
        return Nivel.NINGUNO;
    }


    static String estiloFilaCatalogo(String estado, int stock, Integer minimo) {
        if (stock <= 0) return estiloFila(Nivel.CRITICO, false);
        if ("INACTIVO".equalsIgnoreCase(estado)) {
            return "-fx-background-color: #ECEFF2; -fx-text-fill: #59636D;"
                    + " -fx-text-background-color: #59636D;";
        }
        if (nivelStockCatalogo(stock, minimo) == Nivel.ADVERTENCIA) {
            return estiloFila(Nivel.ADVERTENCIA, false);
        }
        return "-fx-background-color: #D8F2DF; -fx-text-fill: #236344;"
                + " -fx-text-background-color: #236344;";
    }
}
