package co.edu.javeriana.carestock.models;

import java.time.LocalDate;

public class Lote {
    private int idLote;
    private String numeroLote;
    private int idMedicamento;
    private int cantidadActual;
    private LocalDate fechaVencimiento;
    private int idUbicacion;
    private String estadoLote;

    public Lote(String numeroLote, int idMedicamento, int cantidadActual, LocalDate fechaVencimiento, int idUbicacion) {
        this.numeroLote = numeroLote;
        this.idMedicamento = idMedicamento;
        this.cantidadActual = cantidadActual;
        this.fechaVencimiento = fechaVencimiento;
        this.idUbicacion = idUbicacion;
        this.estadoLote = "DISPONIBLE";
    }

    // Getters y Setters
    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }
    public String getNumeroLote() { return numeroLote; }
    public int getIdMedicamento() { return idMedicamento; }
    public int getCantidadActual() { return cantidadActual; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public int getIdUbicacion() { return idUbicacion; }
    public String getEstadoLote() { return estadoLote; }
}