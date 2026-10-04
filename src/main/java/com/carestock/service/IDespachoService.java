package com.carestock.service;

import java.sql.SQLException;

/**
 * Contrato de negocio para movimientos de salida de inventario.
 */
public interface IDespachoService {

    void despachar(
            int idLote,
            int cantidad
    ) throws SQLException;
}
