package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

public class Aseo extends ProductoBase {

    private int idAseo;
    private Integer idFarmacia;
    private Integer idUsuarioCreacion;
    private String estado = "ACTIVO";
    private int stockMinimo;

    // Atributos específicos de aseo
    private String tipoAseo;
    private boolean biodegradable;
    private String componentesActivos;

    public Aseo() {
    }

    // =========================================
    // REGLAS DE NEGOCIO - VALIDABLE
    // =========================================

    @Override
    public void validar() throws ReglaNegocioException {
        validarCamposComunes("aseo");
        if (estado == null ||
                !("ACTIVO".equalsIgnoreCase(estado) || "INACTIVO".equalsIgnoreCase(estado))) {
            throw new ReglaNegocioException("El estado debe ser ACTIVO o INACTIVO.");
        }
        validarStockNoNegativo("aseo");
        if (stockMinimo < 0) {
            throw new ReglaNegocioException(
                    "El stock mínimo del producto de aseo no puede ser negativo."
            );
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        try {
            validar();
            return getStock() > 0 && "ACTIVO".equalsIgnoreCase(estado);
        } catch (ReglaNegocioException e) {
            return false;
        }
    }

    // =========================================
    // GETTERS Y SETTERS
    // =========================================

    public int getIdAseo() {
        return idAseo;
    }

    public void setIdAseo(int idAseo) {
        this.idAseo = idAseo;
    }

    public Integer getIdFarmacia() {
        return idFarmacia;
    }

    public void setIdFarmacia(Integer idFarmacia) {
        this.idFarmacia = idFarmacia;
    }

    public Integer getIdUsuarioCreacion() {
        return idUsuarioCreacion;
    }

    public void setIdUsuarioCreacion(Integer idUsuarioCreacion) {
        this.idUsuarioCreacion = idUsuarioCreacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public String getTipoAseo() {
        return tipoAseo;
    }

    public void setTipoAseo(String tipoAseo) {
        this.tipoAseo = tipoAseo;
    }

    public boolean isBiodegradable() {
        return biodegradable;
    }

    public void setBiodegradable(boolean biodegradable) {
        this.biodegradable = biodegradable;
    }

    public String getComponentesActivos() {
        return componentesActivos;
    }

    public void setComponentesActivos(String componentesActivos) {
        this.componentesActivos = componentesActivos;
    }
}