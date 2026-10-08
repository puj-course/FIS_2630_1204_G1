package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;


public class Cosmetico implements Validable {

    private String id;
    private String nombreComercial;
    private String nso; // Notificación Sanitaria Obligatoria (ej. NSOX-12345-20CO)
    private String marca;
    private boolean tieneAlertaActiva;

    public Cosmetico() {}

    public Cosmetico(String id, String nombreComercial, String nso, String marca) {
        this.id = id;
        this.nombreComercial = nombreComercial;
        this.nso = nso;
        this.marca = marca;
        this.tieneAlertaActiva = false;
    }

    @Override
    public void validar() throws ReglaNegocioException {
        if (nombreComercial == null || nombreComercial.trim().isEmpty()) {
            throw new ReglaNegocioException("El nombre del producto cosmético no puede estar vacío.");
        }
        if (nso == null || nso.trim().isEmpty()) {
            throw new ReglaNegocioException("El producto cosmético debe poseer un código NSO (Notificación Sanitaria Obligatoria) vigente.");
        }
        if (tieneAlertaActiva) {
            throw new ReglaNegocioException("BLOQUEO SANITARIO: El cosmético " + nombreComercial + " fue reportado con alerta de seguridad.");
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        return !tieneAlertaActiva && nso != null && !nso.trim().isEmpty();
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }

    public String getNso() { return nso; }
    public void setNso(String nso) { this.nso = nso; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public boolean isTieneAlertaActiva() { return tieneAlertaActiva; }
    public void setTieneAlertaActiva(boolean tieneAlertaActiva) { this.tieneAlertaActiva = tieneAlertaActiva; }
}
