package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

import java.time.LocalDate;

public class AlertaInvima implements Validable {

    private String idAlerta;
    private String cmAfectado;
    private String loteAfectado;
    private String motivo;
    private LocalDate fechaEmision;
    private boolean activa;

    public AlertaInvima() {}

    public AlertaInvima(String idAlerta, String cmAfectado, String loteAfectado, String motivo, LocalDate fechaEmision) {
        this.idAlerta = idAlerta;
        this.cmAfectado = cmAfectado;
        this.loteAfectado = loteAfectado;
        this.motivo = motivo;
        this.fechaEmision = fechaEmision;
        this.activa = true;
    }

    @Override
    public void validar() throws ReglaNegocioException {
        if (cmAfectado == null || cmAfectado.trim().isEmpty()) {
            throw new ReglaNegocioException("Toda Alerta INVIMA debe asociarse obligatoriamente a un código de medicamento.");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ReglaNegocioException("Se requiere un motivo o reporte técnico para la alerta de farmacovigilancia.");
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        return !activa;
    }

    // Getters y Setters
    public String getIdAlerta() { return idAlerta; }
    public void setIdAlerta(String idAlerta) { this.idAlerta = idAlerta; }

    public String getCmAfectado() { return cmAfectado; }
    public void setCmAfectado(String cmAfectado) { this.cmAfectado = cmAfectado; }

    public String getLoteAfectado() { return loteAfectado; }
    public void setLoteAfectado(String loteAfectado) { this.loteAfectado = loteAfectado; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public LocalDate getFechaEmision() { return fechaEmision; }
    public void setFechaEmision(LocalDate fechaEmision) { this.fechaEmision = fechaEmision; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }
}