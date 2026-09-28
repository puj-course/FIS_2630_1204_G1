package com.carestock.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LoteGestion {

    private final int idLote;
    private final int idFarmacia;
    private final int idMedicamento;
    private final String numeroLote;
    private final String medicamento;
    private final int cantidadActual;
    private final LocalDate fechaVencimiento;
    private final int idUbicacion;
    private final String ubicacion;
    private final String estadoLote;
    private final LocalDateTime fechaIngreso;

    public LoteGestion(
            int idLote,
            int idFarmacia,
            int idMedicamento,
            String numeroLote,
            String medicamento,
            int cantidadActual,
            LocalDate fechaVencimiento,
            int idUbicacion,
            String ubicacion,
            String estadoLote,
            LocalDateTime fechaIngreso
    ) {
        this.idLote = idLote;
        this.idFarmacia = idFarmacia;
        this.idMedicamento = idMedicamento;
        this.numeroLote = numeroLote;
        this.medicamento = medicamento;
        this.cantidadActual = cantidadActual;
        this.fechaVencimiento = fechaVencimiento;
        this.idUbicacion = idUbicacion;
        this.ubicacion = ubicacion;
        this.estadoLote = estadoLote;
        this.fechaIngreso = fechaIngreso;
    }

    public int getIdLote() {
        return idLote;
    }

    public int getIdFarmacia() {
        return idFarmacia;
    }

    public int getIdMedicamento() {
        return idMedicamento;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public String getMedicamento() {
        return medicamento;
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

    public String getUbicacion() {
        return ubicacion;
    }

    public String getEstadoLote() {
        return estadoLote;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public boolean estaRetenido() {
        return "RETENIDO".equalsIgnoreCase(
                estadoLote
        );
    }
}
