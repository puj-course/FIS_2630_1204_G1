package com.carestock.model;

public class Medicamento extends Producto {
    private String formaFarmaceutica;
    private Boolean requiereReceta;

    public Medicamento() {
        super();
    }

    // Constructor completo usado por MedicamentoDAO y MainApp
    public Medicamento(Long idMedicamento, String codigoInvima, String nombreComercial, String principioActivo, String concentracion, String categoria, Integer stockTotal, Integer stockMinimo) {
        super(idMedicamento, codigoInvima, nombreComercial, principioActivo, concentracion, categoria, stockTotal, stockMinimo);
        this.formaFarmaceutica = "Tableta";
        this.requiereReceta = false;
    }

    // Constructor simplificado
    public Medicamento(String codigoInvima, String nombreComercial, String formaFarmaceutica) {
        super(0L, codigoInvima, nombreComercial, "", "", "General", 0, 10);
        this.formaFarmaceutica = formaFarmaceutica;
        this.requiereReceta = false;
    }

    public String getFormaFarmaceutica() { return formaFarmaceutica; }
    public void setFormaFarmaceutica(String formaFarmaceutica) { this.formaFarmaceutica = formaFarmaceutica; }
    public Boolean getRequiereReceta() { return requiereReceta; }
    public void setRequiereReceta(Boolean requiereReceta) { this.requiereReceta = requiereReceta; }
}
