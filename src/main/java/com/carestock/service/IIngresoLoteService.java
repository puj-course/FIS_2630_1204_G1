package com.carestock.service;

import com.carestock.model.Medicamento;
import com.carestock.model.Ubicacion;

import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Contrato de negocio para movimientos de entrada de inventario.
 */
public interface IIngresoLoteService {

    void registrar(
            Medicamento medicamento,
            String numeroLote,
            String cantidadStr,
            LocalDate fechaVencimiento,
            Ubicacion ubicacion
    ) throws SQLException;
}
