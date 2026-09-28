package com.carestock.model;

import java.time.LocalDate;

public class AlertaVencimiento {

    private final int idLote;
    private final int idMedicamento;
    private final String nombreComercial;
    private final String numeroLote;
    private final int cantidadActual;
    private final LocalDate fechaVencimiento;
    private final long diasParaVencer;
    private final String nivelAlerta; // ROJO, AMARILLO, VERDE, VENCIDO

    public AlertaVencimiento(int idLote, int idMedicamento, String nombreComercial, String numeroLote,
                              int cantidadActual, LocalDate fechaVencimiento, long diasParaVencer,
                              String nivelAlerta) {
        this.idLote = idLote;
        this.idMedicamento = idMedicamento;
        this.nombreComercial = nombreComercial;
        this.numeroLote = numeroLote;
        this.cantidadActual = cantidadActual;
        this.fechaVencimiento = fechaVencimiento;
        this.diasParaVencer = diasParaVencer;
        this.nivelAlerta = nivelAlerta;
    }

    public int getIdLote() {
        return idLote;
    }

    public int getIdMedicamento() {
        return idMedicamento;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public String getNumeroLote() {
        return numeroLote;
    }

    public int getCantidadActual() {
        return cantidadActual;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public long getDiasParaVencer() {
        return diasParaVencer;
    }

    public String getNivelAlerta() {
        return nivelAlerta;
    }
}
