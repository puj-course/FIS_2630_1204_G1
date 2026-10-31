package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

public class Aseo extends ProductoBase {

    private static final String ACTIVO = "ACTIVO";
    private static final String INACTIVO = "INACTIVO";
    private static final String TIPO = "aseo";

    private int idAseo;
    private Integer idFarmacia;
    private Integer idUsuarioCreacion;
    private String estado = ACTIVO;

    private String tipoAseo;
    private boolean biodegradable;
    private String componentesActivos;

    @Override
    public void validar() throws ReglaNegocioException {
        validarCamposComunes(TIPO);

        if (estado == null || !(ACTIVO.equalsIgnoreCase(estado)
                || INACTIVO.equalsIgnoreCase(estado))) {
            throw new ReglaNegocioException(
                    "El estado debe ser ACTIVO o INACTIVO.");
        }

        validarStockNoNegativo(TIPO);
    }

    @Override
    public boolean esAptoParaDispensar() {
        return esValidoYConStock()
                && ACTIVO.equalsIgnoreCase(estado);
    }

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
