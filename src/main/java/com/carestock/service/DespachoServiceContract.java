package com.carestock.service;

import java.sql.SQLException;

/**
 * Contrato de negocio para movimientos de salida de inventario.
 */
public interface DespachoServiceContract {

    void despachar(
            int idLote,
            int cantidad
    ) throws SQLException;
}
