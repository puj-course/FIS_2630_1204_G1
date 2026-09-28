package com.carestock.model;

public class Usuario {

    private final int idUsuario;
    private final String nombreCompleto;
    private final String email;
    private final String passwordHash;
    private final int idRol;
    private final String nombreRol;
    private final String estado;
    private final Integer idFarmacia;

    /**
     * Constructor de compatibilidad para usuarios que todavía no
     * tienen una farmacia cargada en el modelo.
     */
    public Usuario(
            int idUsuario,
            String nombreCompleto,
            String email,
            String passwordHash,
            int idRol,
            String nombreRol,
            String estado) {

        this(
                idUsuario,
                nombreCompleto,
                email,
                passwordHash,
                idRol,
                nombreRol,
                estado,
                null
        );
    }

    /**
     * Constructor que incluye el contexto de farmacia.
     */
    public Usuario(
            int idUsuario,
            String nombreCompleto,
            String email,
            String passwordHash,
            int idRol,
            String nombreRol,
            String estado,
            Integer idFarmacia) {

        this.idUsuario = idUsuario;
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.passwordHash = passwordHash;
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.estado = estado;
        this.idFarmacia = idFarmacia;
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

    public Integer getIdFarmacia() {
        return idFarmacia;
    }

    public boolean tieneFarmaciaAsignada() {
        return idFarmacia != null && idFarmacia > 0;
    }

    public boolean estaActivo() {
        return "ACTIVO".equalsIgnoreCase(estado);
    }
}
