package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

public class Maternidad implements Validable {

    private String codigo;
    private String nombre;
    private String descripcion;
    private double precio;
    private int stock;

    // Atributos específicos de maternidad
    private String etapaRecomendada;
    private boolean hipoalergenico;
    private int edadGestacionalSugerida;

    public Maternidad() {}

    // =========================================
    // REGLAS DE NEGOCIO - VALIDABLE
    // =========================================

    @Override
    public void validar() throws ReglaNegocioException {

        if (codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioException(
                    "El codigo del producto de maternidad es obligatorio."
            );
        }

        if (nombre == null || nombre.isBlank()) {
            throw new ReglaNegocioException(
                    "El nombre del producto de maternidad es obligatorio."
            );
        }

        if (descripcion == null || descripcion.isBlank()) {
            throw new ReglaNegocioException(
                    "La descripcion del producto de maternidad es obligatoria."
            );
        }

        if (!Double.isFinite(precio) || precio < 0) {
            throw new ReglaNegocioException(
                    "El precio del producto de maternidad debe ser valido y no negativo."
            );
        }

        if (stock < 0) {
            throw new ReglaNegocioException(
                    "El stock del producto de maternidad no puede ser negativo."
            );
        }

        if (edadGestacionalSugerida < 0) {
            throw new ReglaNegocioException(
                    "La edad gestacional sugerida no puede ser negativa."
            );
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        try {
            validar();
            return stock > 0;
        } catch (ReglaNegocioException e) {
            return false;
        }
    }

    // =========================================
    // GETTERS Y SETTERS
    // =========================================

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
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException(
                    "La descripcion del producto de maternidad es obligatoria."
            );
        }
        this.descripcion = descripcion.trim();
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

    public String getEtapaRecomendada() {
        return etapaRecomendada;
    }

    public void setEtapaRecomendada(String etapaRecomendada) {
        this.etapaRecomendada = etapaRecomendada;
    }

    public boolean isHipoalergenico() {
        return hipoalergenico;
    }

    public void setHipoalergenico(boolean hipoalergenico) {
        this.hipoalergenico = hipoalergenico;
    }

    public int getEdadGestacionalSugerida() {
        return edadGestacionalSugerida;
    }

    public void setEdadGestacionalSugerida(int edadGestacionalSugerida) {
        this.edadGestacionalSugerida = edadGestacionalSugerida;
    }
}