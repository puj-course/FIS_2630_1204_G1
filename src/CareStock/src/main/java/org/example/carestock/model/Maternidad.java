package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

public class Maternidad extends ProductoBase {

    private int stockMinimo;

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

        validarCamposComunes("maternidad");
        validarStockNoNegativo("maternidad");
        if (stockMinimo < 0) {
            throw new ReglaNegocioException(
                    "El stock mínimo del producto de maternidad no puede ser negativo."
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
            return getStock() > 0;
        } catch (ReglaNegocioException e) {
            return false;
        }
    }

    // =========================================
    // GETTERS Y SETTERS
    // =========================================


    @Override
    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException(
                "La descripcion del producto de maternidad es obligatoria."
            );
        }
        super.setDescripcion(descripcion.trim());
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
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