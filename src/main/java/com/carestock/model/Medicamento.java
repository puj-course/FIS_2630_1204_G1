package com.carestock.model;

/**
 * Representa un medicamento gestionado por CareStock.
 *
 * Implementa el contrato de la interfaz Producto y mantiene los atributos
 * propios del medicamento.
 */
public class Medicamento implements Producto {

    /*
     * Atributos correspondientes al contrato Producto.
     */
    private Long idMedicamento;
    private String codigoInvima;
    private String nombreComercial;
    private String principioActivo;
    private String concentracion;
    private String categoria;
    private Integer stockMinimo;
    private Integer stockTotal;
    private String estado;

    /*
     * Atributos específicos de Medicamento.
     */
    private String formaFarmaceutica;
    private String presentacion;
    private Boolean requiereReceta;

    public Medicamento() {
    }

    public Medicamento(Long idMedicamento, String codigoInvima, String nombreComercial,
                       String principioActivo, String concentracion, String categoria,
                       Integer stockTotal, Integer stockMinimo) {

        this(idMedicamento, codigoInvima, nombreComercial, principioActivo, concentracion,
             categoria, stockTotal, stockMinimo,
             "SIN ESPECIFICAR", "SIN ESPECIFICAR");
    }

    public Medicamento(Long idMedicamento, String codigoInvima, String nombreComercial,
                       String principioActivo, String concentracion, String categoria,
                       Integer stockTotal, Integer stockMinimo, String formaFarmaceutica,
                       String presentacion) {

        this.idMedicamento = idMedicamento;
        this.codigoInvima = codigoInvima;
        this.nombreComercial = nombreComercial;
        this.principioActivo = principioActivo;
        this.concentracion = concentracion;
        this.categoria = categoria;
        this.stockTotal = stockTotal;
        this.stockMinimo = stockMinimo;
        this.estado = "ACTIVO";

        this.formaFarmaceutica = formaFarmaceutica;
        this.presentacion = presentacion;
        this.requiereReceta = false;
    }

    public Medicamento(String codigoInvima, String nombreComercial, String formaFarmaceutica) {

        this(0L, codigoInvima, nombreComercial, "", "", "General",
             0, 10, formaFarmaceutica, "SIN ESPECIFICAR");
    }

    /*
     * Implementación del contrato Producto.
     */

    @Override
    public Long getIdMedicamento() {
        return idMedicamento;
    }

    @Override
    public void setIdMedicamento(Long idMedicamento) {
        this.idMedicamento = idMedicamento;
    }

    @Override
    public String getCodigoInvima() {
        return codigoInvima;
    }

    @Override
    public void setCodigoInvima(String codigoInvima) {
        this.codigoInvima = codigoInvima;
    }

    @Override
    public String getNombreComercial() {
        return nombreComercial;
    }

    @Override
    public void setNombreComercial(String nombreComercial) {
        this.nombreComercial = nombreComercial;
    }

    @Override
    public String getPrincipioActivo() {
        return principioActivo;
    }

    @Override
    public void setPrincipioActivo(String principioActivo) {
        this.principioActivo = principioActivo;
    }

    @Override
    public String getConcentracion() {
        return concentracion;
    }

    @Override
    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }

    @Override
    public String getCategoria() {
        return categoria;
    }

    @Override
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public Integer getStockMinimo() {
        return stockMinimo;
    }

    @Override
    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    @Override
    public Integer getStockTotal() {
        return stockTotal;
    }

    @Override
    public void setStockTotal(Integer stockTotal) {
        this.stockTotal = stockTotal;
    }

    @Override
    public String getEstado() {
        return estado;
    }

    @Override
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /*
     * Propiedades específicas de Medicamento.
     */

    public String getFormaFarmaceutica() {
        return formaFarmaceutica;
    }

    public void setFormaFarmaceutica(String formaFarmaceutica) {
        this.formaFarmaceutica = formaFarmaceutica;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public Boolean getRequiereReceta() {
        return requiereReceta;
    }

    public void setRequiereReceta(Boolean requiereReceta) {
        this.requiereReceta = requiereReceta;
    }

    @Override
    public String toString() {
        String concentracionTexto =
                getConcentracion() == null || getConcentracion().isBlank()
                        ? ""
                        : " (" + getConcentracion() + ")";

        return getNombreComercial() + concentracionTexto;
    }
}