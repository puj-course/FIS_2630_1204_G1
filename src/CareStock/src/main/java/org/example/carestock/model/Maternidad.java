package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

public class Maternidad extends ProductoBase {

    private static final String TIPO = "maternidad";
    private static final String MENSAJE_DESCRIPCION =
            "La descripcion del producto de maternidad es obligatoria.";

    private String etapaRecomendada;
    private boolean hipoalergenico;
    private int edadGestacionalSugerida;

    @Override
    public void validar() throws ReglaNegocioException {
        validarCamposComunes(TIPO);
        validarStockNoNegativo(TIPO);

        if (edadGestacionalSugerida < 0) {
            throw new ReglaNegocioException(
                    "La edad gestacional sugerida no puede ser negativa.");
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        return esValidoYConStock();
    }

    @Override
    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException(MENSAJE_DESCRIPCION);
        }
        super.setDescripcion(descripcion.trim());
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
