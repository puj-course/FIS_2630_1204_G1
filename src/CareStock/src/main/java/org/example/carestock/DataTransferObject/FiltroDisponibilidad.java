package org.example.carestock.DataTransferObject;

import java.util.List;

/**
 * Filtro de disponibilidad para la consulta de inventario (HU-151).
 *
 * <p>No usa JavaFX ni base de datos, por eso se puede probar sin abrir ventanas.</p>
 */
public final class FiltroDisponibilidad {

    public static final String TODOS = "Todos";
    public static final String CON_EXISTENCIAS = "Con existencias";
    public static final String AGOTADOS = "Agotados";

    private FiltroDisponibilidad() {
    }

    /** Opciones en el orden en que se muestran en el combo. */
    public static List<String> opciones() {
        return List.of(TODOS, CON_EXISTENCIAS, AGOTADOS);
    }

    /**
     * Indica si el producto pasa el filtro. Una opcion nula o desconocida se trata
     * como "Todos" para no esconder productos por error.
     */
    public static boolean coincide(ProductoInventario producto, String opcion) {
        if (producto == null) {
            return false;
        }
        if (CON_EXISTENCIAS.equals(opcion)) {
            return producto.existencia() > 0;
        }
        if (AGOTADOS.equals(opcion)) {
            return producto.existencia() == 0;
        }
        return true;
    }
}
