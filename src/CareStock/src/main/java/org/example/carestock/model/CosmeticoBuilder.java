package org.example.carestock.model;


public  class CosmeticoBuilder implements Producto {
    private Cosmetico cosmetico;

    public CosmeticoBuilder() {
        this.reset();
    }

    @Override
    public void reset() {
        this.cosmetico = new Cosmetico();
    }

    @Override
    public CosmeticoBuilder setCodigo(String codigo) {
        cosmetico.setCodigo(codigo);
        return this;
    }

    @Override
    public CosmeticoBuilder setNombre(String nombre) {
        cosmetico.setNombre(nombre);
        return this;
    }

    @Override
    public Producto setDescripcion(String descripcion) {
        return null;
    }

    @Override
    public CosmeticoBuilder setPrecio(double precio) {
        cosmetico.setPrecio(precio);
        return this;
    }

    @Override
    public CosmeticoBuilder setStock(int stock) {
        cosmetico.setStock(stock);
        return this;
    }

    public CosmeticoBuilder setRegistroSanitario(String registroSanitario) {
        cosmetico.setRegistroSanitario(registroSanitario);
        return this;
    }

    public CosmeticoBuilder setTipoPiel(String tipoPiel) {
        cosmetico.setTipoPiel(tipoPiel);
        return this;
    }

    public Cosmetico build() {
        Cosmetico productoFinal = this.cosmetico;
        this.reset();
        return productoFinal;
    }
}
