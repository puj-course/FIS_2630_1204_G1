package com.carestock.model;

public abstract class Producto {
    protected Long idMedicamento;
    protected String codigoInvima;
    protected String nombreComercial;
    protected String principioActivo;
    protected String concentracion;
    protected String categoria;
    protected Integer stockMinimo;
    protected Integer stockTotal;
    protected String estado;

    public Producto() {}

    public Producto(Long idMedicamento, String codigoInvima, String nombreComercial, String principioActivo, String concentracion, String categoria, Integer stockTotal, Integer stockMinimo) {
        this.idMedicamento = idMedicamento;
        this.codigoInvima = codigoInvima;
        this.nombreComercial = nombreComercial;
        this.principioActivo = principioActivo;
        this.concentracion = concentracion;
        this.categoria = categoria;
        this.stockTotal = stockTotal;
        this.stockMinimo = stockMinimo;
        this.estado = "ACTIVO";
    }

    public Long getIdMedicamento() { return idMedicamento; }
    public void setIdMedicamento(Long idMedicamento) { this.idMedicamento = idMedicamento; }
    public String getCodigoInvima() { return codigoInvima; }
    public void setCodigoInvima(String codigoInvima) { this.codigoInvima = codigoInvima; }
    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }
    public String getPrincipioActivo() { return principioActivo; }
    public void setPrincipioActivo(String principioActivo) { this.principioActivo = principioActivo; }
    public String getConcentracion() { return concentracion; }
    public void setConcentracion(String concentracion) { this.concentracion = concentracion; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Integer getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(Integer stockMinimo) { this.stockMinimo = stockMinimo; }
    public Integer getStockTotal() { return stockTotal; }
    public void setStockTotal(Integer stockTotal) { this.stockTotal = stockTotal; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
