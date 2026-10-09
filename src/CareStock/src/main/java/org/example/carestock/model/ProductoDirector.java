package org.example.carestock.model;

public class ProductoDirector {
    private Producto builder;

    public ProductoDirector(Producto builder) {
        this.builder = builder;
    }

    public void changeBuilder(Producto builder) {
        this.builder = builder;
    }

    public void make(String type) {
        builder.reset();
        if ("estandar".equalsIgnoreCase(type)) {
            builder.setCodigo("PROD-001")
                    .setNombre("Producto Comercial Estándar")
                    .setPrecio(25000.0)
                    .setStock(50);
        } else if ("premium".equalsIgnoreCase(type)) {
            builder.setCodigo("PROD-999")
                    .setNombre("Producto de Alta Gama")
                    .setPrecio(120000.0)
                    .setStock(10);
        }
    }
}