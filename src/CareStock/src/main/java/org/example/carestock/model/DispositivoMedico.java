package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

public class DispositivoMedico extends ProductoBase {

    private int idDispositivo;
    private String claseRiesgo;
    private String registroSanitario;

    @Override
    public void validar() throws ReglaNegocioException {

        if (getNombre() == null || getNombre().trim().isEmpty()) {
            throw new ReglaNegocioException(
                    "El nombre del dispositivo médico es obligatorio.");
        }

        if (claseRiesgo == null || claseRiesgo.trim().isEmpty()) {
            throw new ReglaNegocioException(
                    "La clase de riesgo del dispositivo médico es obligatoria.");
        }

        if (!Double.isFinite(getPrecio()) || getPrecio() < 0) {
            throw new ReglaNegocioException(
                    "El precio debe ser un número válido y no negativo.");
        }

        if (getStock() < 0) {
            throw new ReglaNegocioException(
                    "El stock no puede ser negativo.");
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        return getStock() > 0
                && getNombre() != null
                && !getNombre().trim().isEmpty()
                && claseRiesgo != null
                && !claseRiesgo.trim().isEmpty()
                && Double.isFinite(getPrecio())
                && getPrecio() >= 0;
    }

    public int getIdDispositivo() {
        return idDispositivo;
    }

    public void setIdDispositivo(int idDispositivo) {
        this.idDispositivo = idDispositivo;
    }

    public String getClaseRiesgo() {
        return claseRiesgo;
    }

    public void setClaseRiesgo(String claseRiesgo) {
        this.claseRiesgo = claseRiesgo;
    }

    public String getRegistroSanitario() {
        return registroSanitario;
    }

    public void setRegistroSanitario(String registroSanitario) {
        this.registroSanitario = registroSanitario;
    }
}
