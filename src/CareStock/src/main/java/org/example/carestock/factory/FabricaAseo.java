package org.example.carestock.factory;

import org.example.carestock.model.Aseo;

public final class FabricaAseo
        extends FabricaProducto<Aseo> {

    @Override
    protected Aseo crearInstancia() {
        return new Aseo();
    }
}
