package com.carestock.model;

import java.time.LocalDateTime;

/**
 * Representa un registro del historial de accesos (login) al sistema.
 *
 * Se alimenta desde la vista PostgreSQL vw_historial_accesos.
 */
public class AccesoLog {

    private final int idAcceso;
    private final int idUsuario;
    private final String nombreCompleto;
    private final String email;
    private final String resultado;
    private final LocalDateTime fechaHora;

    public AccesoLog(
            int idAcceso,
            int idUsuario,
            String nombreCompleto,
            String email,
            String resultado,
            LocalDateTime fechaHora) {

        this.idAcceso = idAcceso;
        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.resultado = resultado;
        this.fechaHora = fechaHora;
    }

    public int getIdAcceso() {
        return idAcceso;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public String getResultado() {
        return resultado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
