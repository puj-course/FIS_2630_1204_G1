package com.carestock.model;

import java.time.LocalDate;

public class Lote {
    private Integer idLote;
    private final String numeroLote;
    private final int idMedicamento;
    private final int cantidadActual;
    private final LocalDate fechaVencimiento;
    private final int idUbicacion;
    private final int idUsuario;
    private String estadoLote;

    public Lote(String numeroLote, int idMedicamento, int cantidadActual,
                LocalDate fechaVencimiento, int idUbicacion, int idUsuario) {
        this.numeroLote = numeroLote;
        this.idMedicamento = idMedicamento;
        this.cantidadActual = cantidadActual;
        this.fechaVencimiento = fechaVencimiento;
        this.idUbicacion = idUbicacion;
        this.idUsuario = idUsuario;
        this.estadoLote = "DISPONIBLE";
    }

    public Integer getIdLote() { return idLote; }

    public void setIdLote(Integer idLote) {
        this.idLote = idLote;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public int getIdMedicamento() {
        return idMedicamento;
    }

    public int getCantidadActual() {
        return cantidadActual;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public int getIdUbicacion() {
        return idUbicacion;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getEstadoLote() {
        return estadoLote;
    }

    public void setEstadoLote(String estadoLote) {
        this.estadoLote = estadoLote;
    }
}