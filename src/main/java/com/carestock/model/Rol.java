package com.carestock.model;

public class Rol {

    private final int idRol;
    private final String nombreRol;

    public Rol(int idRol, String nombreRol) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
    }

    public int getIdRol() {
        return idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    @Override
    public String toString() {
        return nombreRol;
    }
}
