package com.carestock.model;

public class Usuario {

    private final int idUsuario;
    private final String nombreCompleto;
    private final String email;
    private final String passwordHash;
    private final int idRol;
    private final String nombreRol;
    private final String estado;

    public Usuario(
            int idUsuario,
            String nombreCompleto,
            String email,
            String passwordHash,
            int idRol,
            String nombreRol,
            String estado) {

        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.passwordHash = passwordHash;
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.estado = estado;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public int getIdRol() {
        return idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public String getEstado() {
        return estado;
    }

    public boolean estaActivo() {
        return "ACTIVO".equalsIgnoreCase(estado);
    }
}
