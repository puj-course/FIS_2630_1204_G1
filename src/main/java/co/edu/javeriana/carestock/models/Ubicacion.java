package co.edu.javeriana.carestock.models;

public class Ubicacion {
    private int idUbicacion;
    private String estante;
    private String nivel;
    private String descripcion;

    public Ubicacion(int idUbicacion, String estante, String nivel, String descripcion) {
        this.idUbicacion = idUbicacion;
        this.estante = estante;
        this.nivel = nivel;
        this.descripcion = descripcion;
    }

    public int getIdUbicacion() { return idUbicacion; }
    public String getEstante() { return estante; }
    public String getNivel() { return nivel; }
    public String getDescripcion() { return descripcion; }

    @Override
    public String toString() {
        return estante + " - " + nivel; // Texto que mostrará el ComboBox en la vista
    }
}