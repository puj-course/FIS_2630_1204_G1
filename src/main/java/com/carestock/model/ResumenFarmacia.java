package com.carestock.model;

public class ResumenFarmacia {

    private final int idFarmacia;
    private final String codigo;
    private final String nombre;
    private final String estado;
    private final long stockTotal;
    private final long medicamentosActivos;
    private final long alertasCriticas;

    public ResumenFarmacia(int idFarmacia, String codigo, String nombre, String estado,
                            long stockTotal, long medicamentosActivos, long alertasCriticas) {
        this.idFarmacia = idFarmacia;
        this.codigo = codigo;
        this.nombre = nombre;
        this.estado = estado;
        this.stockTotal = stockTotal;
        this.medicamentosActivos = medicamentosActivos;
        this.alertasCriticas = alertasCriticas;
    }

    public int getIdFarmacia() {
        return idFarmacia;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEstado() {
        return estado;
    }

    public long getStockTotal() {
        return stockTotal;
    }

    public long getMedicamentosActivos() {
        return medicamentosActivos;
    }

    public long getAlertasCriticas() {
        return alertasCriticas;
    }
}
