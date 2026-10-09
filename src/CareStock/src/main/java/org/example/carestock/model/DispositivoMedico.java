
package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;


public class DispositivoMedico implements Validable {

    // ==========================================
    // ATRIBUTOS
    // ==========================================

    private int idDispositivo;
    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;
    private String claseRiesgo;
    private String registroSanitario;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public DispositivoMedico() {
    }

    // ==========================================
    // VALIDACIÓN DE REGLAS DE NEGOCIO
    // ==========================================

    @Override
    public void validar() throws ReglaNegocioException {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioException(
                    "El nombre del dispositivo médico es obligatorio."
            );
        }

        if (claseRiesgo == null || claseRiesgo.trim().isEmpty()) {
            throw new ReglaNegocioException(
                    "La clase de riesgo del dispositivo médico es obligatoria."
            );
        }

        if (!Double.isFinite(precio) || precio < 0) {
            throw new ReglaNegocioException(
                    "El precio debe ser un número válido y no negativo."
            );
        }

        if (stock < 0) {
            throw new ReglaNegocioException(
                    "El stock no puede ser negativo."
            );
        }
    }

    // ==========================================
    // DISPONIBILIDAD PARA DISPENSACIÓN
    // ==========================================

    @Override
    public boolean esAptoParaDispensar() {

        return stock > 0
                && nombre != null
                && !nombre.trim().isEmpty()
                && claseRiesgo != null
                && !claseRiesgo.trim().isEmpty()
                && Double.isFinite(precio)
                && precio >= 0;
    }

    // ==========================================
    // FACTORY METHOD DEL BUILDER
    // ==========================================

    public static DispositivoMedicoBuilder builder() {
        return new DispositivoMedicoBuilder();
    }

    // ==========================================
    // GETTERS Y SETTERS
    // ==========================================

    public int getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(int idDispositivo) {
        this.idDispositivo = idDispositivo;
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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

    public String getClaseRiesgo() {
        return claseRiesgo;
    }

    public void setClaseRiesgo(String claseRiesgo) {
        this.claseRiesgo = claseRiesgo;
    }

    public String getRegistroSanitario() {
        return registroSanitario;
    }

    public void setRegistroSanitario(String registroSanitario) {
        this.registroSanitario = registroSanitario;
    }
}
