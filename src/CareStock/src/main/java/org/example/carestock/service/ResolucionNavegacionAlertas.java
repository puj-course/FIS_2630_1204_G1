package org.example.carestock.service;

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.model.Lote;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class ResolucionNavegacionAlertas {

    private ResolucionNavegacionAlertas() { }

    public record Destino(
            TipoAlerta tipo,
            Categoria categoria,
            int idReferencia,
            String codigo,
            String producto,
            String numeroLote,
            int idFarmacia
    ) { }

    public static Destino resolver(
            AlertaInventario alerta,
            Integer farmaciaOrigen,
            Integer farmaciaSesion) {

        Objects.requireNonNull(alerta, "Seleccione una alerta.");

        if (farmaciaOrigen == null || farmaciaOrigen <= 0
                || !farmaciaOrigen.equals(farmaciaSesion)) {
            throw new IllegalStateException(
                    "La farmacia de la alerta no corresponde a la sesión activa.");
        }

        if (alerta.tipoAlerta() == TipoAlerta.VENCIMIENTO
                && (alerta.categoria() != Categoria.MEDICAMENTO
                || alerta.numeroLote() == null
                || alerta.numeroLote().isBlank())) {
            throw new IllegalArgumentException(
                    "La alerta de vencimiento no identifica un lote válido.");
        }

        return new Destino(
                alerta.tipoAlerta(),
                alerta.categoria(),
                alerta.idReferencia(),
                alerta.codigo(),
                alerta.producto(),
                alerta.numeroLote(),
                farmaciaOrigen);
    }

    public static Optional<ProductoInventario> buscarProducto(
            Destino destino,
            List<ProductoInventario> productos) {

        if (destino == null
                || destino.tipo() != TipoAlerta.STOCK_MINIMO) {
            return Optional.empty();
        }

        return productos.stream()
                .filter(p -> p.id() == destino.idReferencia())
                .filter(p -> p.categoria().equalsIgnoreCase(
                        destino.categoria().etiqueta()))
                .filter(p -> coincideSiExiste(
                        destino.codigo(), p.codigo()))
                .findFirst();
    }

    public static Optional<Lote> buscarLote(
            Destino destino,
            List<Lote> lotes) {

        if (destino == null
                || destino.tipo() != TipoAlerta.VENCIMIENTO) {
            return Optional.empty();
        }

        return lotes.stream()
                .filter(l -> l.getIdLote() == destino.idReferencia())
                .filter(l -> coincideSiExiste(
                        destino.numeroLote(), l.getNumeroLote()))
                .findFirst();
    }

    private static boolean coincideSiExiste(
            String esperado, String actual) {

        return esperado == null
                || esperado.isBlank()
                || actual != null
                && esperado.trim().equalsIgnoreCase(actual.trim());
    }
}
