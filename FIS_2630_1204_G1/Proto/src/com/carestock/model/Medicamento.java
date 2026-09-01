package com.carestock.model;

public class Medicamento {
    private Long idMedicamento;
    private String codigoInvima;
    private String nombreComercial;
    private String principioActivo;
    private String concentracion;
    private String categoria;
    private int stockTotal;
    private int stockMinimo;

    public Medicamento() {}

    public Medicamento(Long idMedicamento, String codigoInvima, String nombreComercial, 
                       String principioActivo, String concentracion, String categoria, 
                       int stockTotal, int stockMinimo) {
        this.idMedicamento = idMedicamento;
        this.codigoInvima = codigoInvima;
        this.nombreComercial = nombreComercial;
        this.principioActivo = principioActivo;
        this.concentracion = concentracion;
        this.categoria = categoria;
        this.stockTotal = stockTotal;
        this.stockMinimo = stockMinimo;
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

    public int getStockTotal() { return stockTotal; }
    public void setStockTotal(int stockTotal) { this.stockTotal = stockTotal; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public String toCSV() {
        return String.join(",", 
            String.valueOf(idMedicamento),
            "\"" + codigoInvima + "\"",
            "\"" + nombreComercial + "\"",
            "\"" + principioActivo + "\"",
            "\"" + concentracion + "\"",
            "\"" + categoria + "\"",
            String.valueOf(stockTotal),
            String.valueOf(stockMinimo)
        );
    }

    public String toJSON() {
        return String.format(
            "{\"id\": %d, \"codigoInvima\": \"%s\", \"nombre\": \"%s\", \"principioActivo\": \"%s\", \"concentracion\": \"%s\", \"categoria\": \"%s\", \"stockTotal\": %d, \"stockMinimo\": %d}",
            idMedicamento, codigoInvima, nombreComercial, principioActivo, concentracion, categoria, stockTotal, stockMinimo
        );
    }
}
