package org.example.carestock.model;

public class ProductoDirector {

    private Producto builder;

    public ProductoDirector(Producto builder) {
        if (builder == null) {
            throw new IllegalArgumentException(
                    "El builder no puede ser null"
            );
        }
        this.builder = builder;
    }

    public void changeBuilder(Producto builder) {
        if (builder == null) {
            throw new IllegalArgumentException(
                    "El builder no puede ser null"
            );
        }
        this.builder = builder;
    }

    public void make(String type) {

        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException(
                    "Debe indicar el tipo de producto"
            );
        }

        builder.reset();

        if ("estandar".equalsIgnoreCase(type)) {

            builder.setCodigo("PROD-001")
                    .setNombre("Producto Comercial Estandar")
                    .setDescripcion(
                            "Articulo de uso general en inventario")
                    .setPrecio(25000.0)
                    .setStock(50);

        } else if ("premium".equalsIgnoreCase(type)) {

            builder.setCodigo("PROD-999")
                    .setNombre("Producto de Alta Gama")
                    .setDescripcion(
                            "Articulo premium importado")
                    .setPrecio(120000.0)
                    .setStock(10);

        } else if ("aseo_ecologico".equalsIgnoreCase(type)) {

            if (!(builder instanceof AseoBuilder)) {
                throw new IllegalArgumentException(
                        "Este producto requiere AseoBuilder"
                );
            }

            builder.setCodigo("ASEO-050")
                    .setNombre("Limpiador Multiusos Botanico")
                    .setDescripcion(
                            "Limpiador multiusos de formula "
                                    + "biodegradable para el hogar")
                    .setPrecio(19500.0)
                    .setStock(40);

            ((AseoBuilder) builder)
                    .tipoAseo("Hogar")
                    .biodegradable(true)
                    .componentesActivos(
                            "Aceite de pino, tensoactivos naturales"
                    );

        } else if (
                "maternidad_cuidados".equalsIgnoreCase(type)) {

            if (!(builder instanceof MaternidadBuilder)) {
                throw new IllegalArgumentException(
                        "Este producto requiere MaternidadBuilder"
                );
            }

            builder.setCodigo("MAT-100")
                    .setNombre("Kit de Cuidado Premama")
                    .setDescripcion(
                            "Set completo para el bienestar "
                                    + "durante el embarazo y postparto")
                    .setPrecio(85000.0)
                    .setStock(15);

            ((MaternidadBuilder) builder)
                    .etapaRecomendada("Embarazo y Postparto")
                    .hipoalergenico(true)
                    .edadGestacionalSugerida(16);

        } else {

            throw new IllegalArgumentException(
                    "Tipo de producto no reconocido: " + type
            );
        }
    }
}
