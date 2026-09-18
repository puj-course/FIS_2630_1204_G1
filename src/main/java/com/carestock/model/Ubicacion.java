package com.carestock.model;

public class Ubicacion {
    private final int idUbicacion;
    private final String estante;
    private final String nivel;
    private final String descripcion;

    public Ubicacion(int idUbicacion, String estante, String nivel, String descripcion) {
        this.idUbicacion = idUbicacion;
        this.estante = estante;
        this.nivel = nivel;
        this.descripcion = descripcion;
    }

    public int getIdUbicacion() {
        return idUbicacion;
    }

    public String getEstante() {
        return estante;
    }

    public String getNivel() {
        return nivel;
    }

    public String getDescripcion() {
        return descripcion;
    }

    @Override
    public String toString() {
        return estante + " - " + nivel;
    }
}
