package com.carestock.model;

public class MedicamentoGestion {

    private final long idMedicamento;
    private final int idFarmacia;
    private final String codigoInvima;
    private final String nombreComercial;
    private final String principioActivo;
    private final String concentracion;
    private final String formaFarmaceutica;
    private final String presentacion;
    private final String categoria;
    private final int stockTotal;
    private final int stockMinimo;
    private final String estado;

    public MedicamentoGestion(
            long idMedicamento,
            int idFarmacia,
            String codigoInvima,
            String nombreComercial,
            String principioActivo,
            String concentracion,
            String formaFarmaceutica,
            String presentacion,
            String categoria,
            int stockTotal,
            int stockMinimo,
            String estado
    ) {
        this.idMedicamento = idMedicamento;
        this.idFarmacia = idFarmacia;
        this.codigoInvima = codigoInvima;
        this.nombreComercial = nombreComercial;
        this.principioActivo = principioActivo;
        this.concentracion = concentracion;
        this.formaFarmaceutica = formaFarmaceutica;
        this.presentacion = presentacion;
        this.categoria = categoria;
        this.stockTotal = stockTotal;
        this.stockMinimo = stockMinimo;
        this.estado = estado;
    }

    public long getIdMedicamento() {
        return idMedicamento;
    }

    public int getIdFarmacia() {
        return idFarmacia;
    }

    public String getCodigoInvima() {
        return codigoInvima;
    }

    public String getNombreComercial() {
        return nombreComercial;
    }

    public String getPrincipioActivo() {
        return principioActivo;
    }

    public String getConcentracion() {
        return concentracion;
    }

    public String getFormaFarmaceutica() {
        return formaFarmaceutica;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public String getCategoria() {
        return categoria;
    }

    public int getStockTotal() {
        return stockTotal;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public String getEstado() {
        return estado;
    }

    public boolean estaActivo() {
        return "ACTIVO".equalsIgnoreCase(
                estado
        );
    }

    @Override
    public String toString() {
        return nombreComercial
                + " ("
                + codigoInvima
                + ")";
    }
}
