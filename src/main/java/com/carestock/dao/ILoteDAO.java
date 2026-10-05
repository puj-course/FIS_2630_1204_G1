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

/**
* Top 5 interfaces implementadas numero 4 iLoteDAO sjsjsjs
* Ya dejandonos de chistes si esta es una de las intefaces
*
* */
/**
 * Esto nos dice que nosotros o bueno el sistema CareStock debe ser capaz de Registrar y despachar lotes
 * si no estamos en la mala
 *
 * */
public interface ILoteDAO {
    void registrarNuevoLote(
            Connection connection,
            Lote lote
    ) throws SQLException;

    void despacharLote(
            Connection connection,
            DespachoLote despacho
    ) throws SQLException;
}
