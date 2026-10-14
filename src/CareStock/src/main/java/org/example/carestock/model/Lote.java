package org.example.carestock.model;

import org.example.carestock.exception.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Lote implements Validable {

    private int idLote;
    private String numeroLote;
    private int idMedicamento;
    private int cantidadActual;
    private LocalDate fechaVencimiento;
    private Integer idUbicacion;
    private String estadoLote; // e.g., "DISPONIBLE", "VENCIDO", "CUARENTENA", "AGOTADO"
    private LocalDateTime fechaIngreso;
    private Integer idUsuario;
    private Integer idUsuarioModificacion;
    private LocalDateTime fechaModificacion;

    public Lote() {}

    public Lote(String numeroLote, int idMedicamento, int cantidadActual,
                LocalDate fechaVencimiento, Integer idUbicacion, String estadoLote, Integer idUsuario) {
        this.numeroLote = numeroLote;
        this.idMedicamento = idMedicamento;
        this.cantidadActual = cantidadActual;
        this.fechaVencimiento = fechaVencimiento;
        this.idUbicacion = idUbicacion;
        this.estadoLote = estadoLote;
        this.idUsuario = idUsuario;
    }

    @Override
    public void validar() throws ReglaNegocioException {
        if (numeroLote == null || numeroLote.trim().isEmpty()) {
            throw new ReglaNegocioException("El número de lote es obligatorio.");
        }
        if (idMedicamento <= 0) {
            throw new ReglaNegocioException("El lote debe estar vinculado a un medicamento válido.");
        }
        if (cantidadActual < 0) {
            throw new ReglaNegocioException("La cantidad disponible no puede ser negativa.");
        }
        if (fechaVencimiento == null) {
            throw new ReglaNegocioException("La fecha de vencimiento es obligatoria.");
        }
        if (fechaVencimiento.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("RIESGO SANITARIO: El lote " + numeroLote + " se encuentra VENCIDO (" + fechaVencimiento + ").");
        }
        if ("CUARENTENA".equalsIgnoreCase(estadoLote) || "BLOQUEADO".equalsIgnoreCase(estadoLote)) {
            throw new ReglaNegocioException("BLOQUEO DE SEGURIDAD: El lote " + numeroLote + " está en estado " + estadoLote + ".");
        }
    }

    @Override
    public boolean esAptoParaDispensar() {
        return fechaVencimiento != null
                && fechaVencimiento.isAfter(LocalDate.now())
                && cantidadActual > 0
                && "DISPONIBLE".equalsIgnoreCase(estadoLote);
    }

    // Getters y Setters
    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }

    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }

    public int getIdMedicamento() { return idMedicamento; }
    public void setIdMedicamento(int idMedicamento) { this.idMedicamento = idMedicamento; }

    public int getCantidadActual() { return cantidadActual; }
    public void setCantidadActual(int cantidadActual) { this.cantidadActual = cantidadActual; }

    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public Integer getIdUbicacion() { return idUbicacion; }
    public void setIdUbicacion(Integer idUbicacion) { this.idUbicacion = idUbicacion; }

    public String getEstadoLote() { return estadoLote; }
    public void setEstadoLote(String estadoLote) { this.estadoLote = estadoLote; }

    public LocalDateTime getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(LocalDateTime fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public Integer getIdUsuarioModificacion() { return idUsuarioModificacion; }
    public void setIdUsuarioModificacion(Integer idUsuarioModificacion) { this.idUsuarioModificacion = idUsuarioModificacion; }

    public LocalDateTime getFechaModificacion() { return fechaModificacion; }
    public void setFechaModificacion(LocalDateTime fechaModificacion) { this.fechaModificacion = fechaModificacion; }
}