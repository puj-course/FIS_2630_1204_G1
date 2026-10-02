package com.carestock.dao;

import com.carestock.model.DespachoLote;
import com.carestock.model.Lote;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Contrato de persistencia para operaciones de stock asociadas a lotes.
 *
 * Las operaciones transaccionales reciben una Connection externa para
 * permitir que la capa Service controle commit y rollback.
 */
public interface LoteDAOContract {

    void registrarNuevoLote(
            Connection connection,
            Lote lote
    ) throws SQLException;

    void despacharLote(
            Connection connection,
            DespachoLote despacho
    ) throws SQLException;
}
