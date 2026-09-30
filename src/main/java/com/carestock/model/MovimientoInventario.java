package com.carestock.model;

import java.time.LocalDateTime;

public class MovimientoInventario {

    private final int idLog;
    private final int idFarmacia;
    private final String tipoMovimiento;
    private final String numeroLote;
    private final String medicamento;
    private final int cantidadAfectada;
    private final String usuarioResponsable;
    private final LocalDateTime fechaHora;

    public MovimientoInventario(
            int idLog,
            int idFarmacia,
            String tipoMovimiento,
            String numeroLote,
            String medicamento,
            int cantidadAfectada,
            String usuarioResponsable,
            LocalDateTime fechaHora
    ) {
        this.idLog = idLog;
        this.idFarmacia = idFarmacia;
        this.tipoMovimiento = tipoMovimiento;
        this.numeroLote = numeroLote;
        this.medicamento = medicamento;
        this.cantidadAfectada = cantidadAfectada;
        this.usuarioResponsable = usuarioResponsable;
        this.fechaHora = fechaHora;
    }

    public int getIdLog() {
        return idLog;
    }

    public int getIdFarmacia() {
        return idFarmacia;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public String getMedicamento() {
        return medicamento;
    }

    public int getCantidadAfectada() {
        return cantidadAfectada;
    }

    public String getUsuarioResponsable() {
        return usuarioResponsable;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }
}
