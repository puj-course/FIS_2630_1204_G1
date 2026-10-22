package org.example.carestock.factory;

import org.example.carestock.model.Medicamento;

public final class FabricaMedicamento
        extends FabricaProducto<Medicamento> {

    @Override
    protected Medicamento crearInstancia() {
        return new Medicamento();
    }
}
