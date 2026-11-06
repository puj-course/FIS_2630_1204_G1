package org.example.carestock.service;

import org.example.carestock.session.SesionUsuario;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Coordina seleccion, validacion y exportacion de reposicion. */
public final class AccionExportarReposicion {

    public enum Resultado {
        EXPORTADO,
        SIN_REGISTROS,
        CANCELADO
    }

    private final ServicioPlanificacionReposicion servicio;

    public AccionExportarReposicion() {
        this(new ServicioPlanificacionReposicion());
    }

    AccionExportarReposicion(
            ServicioPlanificacionReposicion servicio) {

        this.servicio = Objects.requireNonNull(servicio);
    }

    public Resultado ejecutar(
            List<RegistroReposicion> visibles,
            Supplier<Path> seleccionarDestino) throws Exception {

        SesionUsuario.DatosUsuario origen =
                PermisosExportacionReposicion.exigirAdministrador();

        Objects.requireNonNull(visibles,
                "Los registros visibles son obligatorios");

        Objects.requireNonNull(seleccionarDestino,
                "Se requiere selector de archivo");

        List<RegistroReposicion> instantanea =
                List.copyOf(visibles);

        if (instantanea.isEmpty()) {
            return Resultado.SIN_REGISTROS;
        }

        Path destino = seleccionarDestino.get();

        if (destino == null) {
            return Resultado.CANCELADO;
        }

        PermisosExportacionReposicion.comprobarMismaSesion(origen);

        // Volver a consultar la farmacia activa antes de escribir.
        List<RegistroReposicion> actuales =
                servicio.listarPorFarmacia(origen.idFarmacia());

        PermisosExportacionReposicion.comprobarMismaSesion(origen);

        // Si cambio el stock, solicitar actualizar la tabla.
        if (!actuales.containsAll(instantanea)) {
            throw new IllegalStateException(
                    "La reposición cambió desde la consulta. "
                    + "Actualice la lista antes de exportar.");
        }

        ExportadorReposicionCsv.guardar(destino, instantanea);

        return Resultado.EXPORTADO;
    }
}
