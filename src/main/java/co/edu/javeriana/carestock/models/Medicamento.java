package co.edu.javeriana.carestock.models;

public class Medicamento {
    private int idMedicamento;
    private String codigoInvima;
    private String nombreComercial;
    private String concentracion;

    public Medicamento(int idMedicamento, String codigoInvima, String nombreComercial, String concentracion) {
        this.idMedicamento = idMedicamento;
        this.codigoInvima = codigoInvima;
        this.nombreComercial = nombreComercial;
        this.concentracion = concentracion;
    }

    public int getIdMedicamento() { return idMedicamento; }
    public String getCodigoInvima() { return codigoInvima; }
    public String getNombreComercial() { return nombreComercial; }
    public String getConcentracion() { return concentracion; }

    @Override
    public String toString() {
        return nombreComercial + " (" + concentracion + ")"; // Texto que mostrará el ComboBox
    }
}