package org.example.carestock.factory;

import org.example.carestock.model.Maternidad;

public final class FabricaMaternidad
        extends FabricaProducto<Maternidad> {

    @Override
    protected Maternidad crearInstancia() {
        return new Maternidad();
    }
}
