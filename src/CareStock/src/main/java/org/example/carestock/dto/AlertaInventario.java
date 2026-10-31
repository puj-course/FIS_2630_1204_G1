package org.example.carestock.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record AlertaInventario(
        TipoAlerta tipoAlerta,
        Categoria categoria,
        int idReferencia,
        String codigo,
        String producto,
        String numeroLote,
        int existencias,
        Integer umbral,
        LocalDate fechaVencimiento,
        Integer diasParaVencer,
        Severidad severidad,
        LocalDateTime ultimoMovimientoFecha,
        String ultimoMovimientoTipo,
        String ultimoMovimientoUsuario
) {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public enum TipoAlerta {
        STOCK_MINIMO("Stock mínimo"),
        VENCIMIENTO("Vencimiento");

        private final String etiqueta;

        TipoAlerta(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String etiqueta() {
            return etiqueta;
        }
    }

    public enum Severidad {
        CRITICA("Crítica"),
        ADVERTENCIA("Advertencia");

        private final String etiqueta;

        Severidad(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String etiqueta() {
            return etiqueta;
        }
    }

    public enum Categoria {
        MEDICAMENTO("Medicamento"),
        ASEO("Aseo"),
        MATERNIDAD("Maternidad");

        private final String etiqueta;

        Categoria(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String etiqueta() {
            return etiqueta;
        }
    }

    public AlertaInventario {
        Objects.requireNonNull(tipoAlerta, "El tipo de alerta es obligatorio");
        Objects.requireNonNull(categoria, "La categoría es obligatoria");
        Objects.requireNonNull(severidad, "La severidad es obligatoria");
        if (idReferencia <= 0) {
            throw new IllegalArgumentException("La referencia debe ser válida");
        }
        if (producto == null || producto.isBlank()) {
            throw new IllegalArgumentException("El producto es obligatorio");
        }
        if (existencias < 0) {
            throw new IllegalArgumentException("Las existencias no pueden ser negativas");
        }
        if (tipoAlerta == TipoAlerta.STOCK_MINIMO && umbral == null) {
            throw new IllegalArgumentException("La alerta de stock requiere un umbral");
        }
        if (tipoAlerta == TipoAlerta.VENCIMIENTO
                && (fechaVencimiento == null || diasParaVencer == null)) {
            throw new IllegalArgumentException(
                    "La alerta de vencimiento requiere fecha y días para vencer");
        }
    }

    public String referencia() {
        if (numeroLote != null && !numeroLote.isBlank()) {
            return "Lote " + numeroLote;
        }
        return codigo == null ? "" : codigo;
    }

    public String mensaje() {
        if (tipoAlerta == TipoAlerta.STOCK_MINIMO) {
            if (existencias == 0) {
                return "Sin existencias (mínimo " + umbral + ")";
            }
            return "Existencias " + existencias + " (mínimo " + umbral + ")";
        }
        if (diasParaVencer < 0) {
            return "Lote vencido hace " + (-diasParaVencer)
                    + (diasParaVencer == -1 ? " día" : " días")
                    + ", pendiente de deshabilitar";
        }
        if (diasParaVencer == 0) {
            return "El lote vence hoy";
        }
        return "Vence en " + diasParaVencer
                + (diasParaVencer == 1 ? " día" : " días")
                + " (" + fechaVencimiento.format(FORMATO_FECHA) + ")";
    }
}
