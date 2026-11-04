package org.example.carestock.service;

import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.session.SesionUsuario.DatosUsuario;

import java.util.Objects;
import java.util.Optional;

/** Decide las acciones de alertas sin ejecutar escrituras de inventario. */
public final class PoliticaAccionesAlertas {

    public enum Accion { REPONER_STOCK, GESTIONAR_LOTE }

    private PoliticaAccionesAlertas() { }

    public static boolean gestorPermitido(String rol) {
        return ServicioCrudLotes.rolGestor(rol);
    }

    public static Optional<Accion> disponible(
            AlertaInventario alerta, DatosUsuario actor, Integer farmaciaOrigen) {
        if (alerta == null || actor == null || farmaciaOrigen == null
                || farmaciaOrigen <= 0
                || !Objects.equals(farmaciaOrigen, actor.idFarmacia())
                || !gestorPermitido(actor.rol())) {
            return Optional.empty();
        }
        if (alerta.tipoAlerta() == TipoAlerta.STOCK_MINIMO) {
            return Optional.of(Accion.REPONER_STOCK);
        }
        if (alerta.tipoAlerta() == TipoAlerta.VENCIMIENTO
                && alerta.categoria() == Categoria.MEDICAMENTO
                && alerta.numeroLote() != null
                && !alerta.numeroLote().isBlank()) {
            return Optional.of(Accion.GESTIONAR_LOTE);
        }
        return Optional.empty();
    }

    public static Accion exigir(
            AlertaInventario alerta, DatosUsuario actor, Integer farmaciaOrigen) {
        return disponible(alerta, actor, farmaciaOrigen)
                .orElseThrow(() -> new SecurityException(
                        "La acción no está autorizada para el rol o la farmacia activos."));
    }
}
