package com.carestock.model;

public class Farmacia {

    private final int idFarmacia;
    private final String codigo;
    private final String nombre;
    private final String estado;

    public Farmacia(
            int idFarmacia,
            String codigo,
            String nombre,
            String estado
    ) {
        this.idFarmacia = idFarmacia;
        this.codigo = codigo;
        this.nombre = nombre;
        this.estado = estado;
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

    public boolean estaActiva() {
        return "ACTIVA".equalsIgnoreCase(estado);
    }

    @Override
    public String toString() {
        return nombre;
    }
}
