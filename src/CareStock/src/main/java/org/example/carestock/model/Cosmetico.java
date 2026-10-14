package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

public class Cosmetico implements Validable {
    private int idCosmetico;
    private String codigo;
    private String nombre;
    private double precio;
    private int stock;
    private String registroSanitario;
    private String tipoPiel;

    public Cosmetico() {
    }

    @Override
    public void validar() throws ReglaNegocioException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioException("El nombre del cosmético es obligatorio.");
        }
        if (registroSanitario == null || registroSanitario.trim().isEmpty()) {
            throw new ReglaNegocioException("El registro sanitario del cosmético es obligatorio.");
        }
        if (precio < 0 || stock < 0) {
            throw new ReglaNegocioException("El precio y el stock no pueden ser negativos.");
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        return stock > 0 && registroSanitario != null && !registroSanitario.trim().isEmpty();
    }

    // Getters y Setters
    public int getIdCosmetico() {
        return idCosmetico;
    }

    public void setIdCosmetico(int idCosmetico) {
        this.idCosmetico = idCosmetico;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getRegistroSanitario() {
        return registroSanitario;
    }

    public void setRegistroSanitario(String registroSanitario) {
        this.registroSanitario = registroSanitario;
    }

    public String getTipoPiel() {
        return tipoPiel;
    }

    public void setTipoPiel(String tipoPiel) {
        this.tipoPiel = tipoPiel;
    }
}