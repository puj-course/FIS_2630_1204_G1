package com.carestock.model;

public class Medicamento extends Producto {
    private String formaFarmaceutica;
    private String presentacion;
    private Boolean requiereReceta;

    public Medicamento() {
        super();
    }

    public Medicamento(Long idMedicamento, String codigoInvima, String nombreComercial,
                       String principioActivo, String concentracion, String categoria,
                       Integer stockTotal, Integer stockMinimo) {
        this(idMedicamento, codigoInvima, nombreComercial, principioActivo, concentracion,
             categoria, stockTotal, stockMinimo, "SIN ESPECIFICAR", "SIN ESPECIFICAR");
    }

    public Medicamento(Long idMedicamento, String codigoInvima, String nombreComercial,
                       String principioActivo, String concentracion, String categoria,
                       Integer stockTotal, Integer stockMinimo, String formaFarmaceutica,
                       String presentacion) {
        super(idMedicamento, codigoInvima, nombreComercial, principioActivo, concentracion,
              categoria, stockTotal, stockMinimo);
        this.formaFarmaceutica = formaFarmaceutica;
        this.presentacion = presentacion;
        this.requiereReceta = false;
    }

    public Medicamento(String codigoInvima, String nombreComercial, String formaFarmaceutica) {
        this(0L, codigoInvima, nombreComercial, "", "", "General", 0, 10,
             formaFarmaceutica, "SIN ESPECIFICAR");
    }

    public String getFormaFarmaceutica() { return formaFarmaceutica; }
    public void setFormaFarmaceutica(String formaFarmaceutica) { this.formaFarmaceutica = formaFarmaceutica; }
    public String getPresentacion() { return presentacion; }
    public void setPresentacion(String presentacion) { this.presentacion = presentacion; }
    public Boolean getRequiereReceta() { return requiereReceta; }
    public void setRequiereReceta(Boolean requiereReceta) { this.requiereReceta = requiereReceta; }

    @Override
    public String toString() {
        String concentracionTexto = getConcentracion() == null || getConcentracion().isBlank()
                ? "" : " (" + getConcentracion() + ")";
        return getNombreComercial() + concentracionTexto;
    }
}
