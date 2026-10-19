package org.example.carestock.factory;

import java.util.Objects;
import java.util.function.Consumer;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Validable;

public abstract class FabricaProducto<T extends Validable> {

    // Factory Method GoF
    protected abstract T crearInstancia();

    public final T crear(Consumer<T> configurar) {
        Objects.requireNonNull(configurar,
                "La configuracion es obligatoria");

        T producto = crearInstancia();
        configurar.accept(producto);

        try {
            producto.validar();
        } catch (ReglaNegocioException e) {
            throw new IllegalStateException(
                    "No se pudo crear el producto: "
                    + e.getMessage(), e);
        }
        return producto;
    }
}
