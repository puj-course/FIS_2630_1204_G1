package org.example.carestock.model;

// Dentro de la clase Medicamento, actualizamos el builder:
public class MedicamentoBuilder implements Producto {
    private Medicamento medicamento;

    public MedicamentoBuilder() {
        this.reset();
    }

    @Override
    public void reset() {
        this.medicamento = new Medicamento();
    }

    @Override
    public MedicamentoBuilder setCodigo(String codigo) {
        medicamento.setCodigoInvima(codigo); // Mapea al código del medicamento
        return this;
    }

    @Override
    public MedicamentoBuilder setNombre(String nombre) {
        medicamento.setNombreComercial(nombre);
        return this;
    }

    @Override
    public Producto setDescripcion(String descripcion) {
        return null;
    }

    @Override
    public MedicamentoBuilder setPrecio(double precio) {
        // Si Medicamento maneja precio, o se omite si se gestiona en otra capa.
        // Aquí lo dejamos adaptado a los métodos genéricos.
        return this;
    }

    @Override
    public MedicamentoBuilder setStock(int stock) {
        medicamento.setStockTotal(stock);
        return this;
    }

    public MedicamentoBuilder principioActivo(String principioActivo) {
        medicamento.setPrincipioActivo(principioActivo);
        return this;
    }

    public MedicamentoBuilder concentracion(String concentracion) {
        medicamento.setConcentracion(concentracion);
        return this;
    }

    public Medicamento build() {
        Medicamento productoFinal = this.medicamento;
        this.reset();
        return productoFinal;
    }
}