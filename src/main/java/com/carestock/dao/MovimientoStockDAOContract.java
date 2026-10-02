package com.carestock.dao;

import com.carestock.model.DespachoLote;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Contrato de persistencia para el historial de movimientos de stock.
 *
 * La Connection es proporcionada por la capa Service para garantizar
 * que el cambio de inventario y el registro de kardex formen parte
 * de la misma transacción JDBC.
 */
public interface MovimientoStockDAOContract {

    void registrarSalida(
            Connection connection,
            DespachoLote despacho
    ) throws SQLException;
}
