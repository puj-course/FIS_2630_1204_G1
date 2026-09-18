package com.carestock.service;

import com.carestock.config.AppConfig;
import com.carestock.dao.LoteDAO;
import com.carestock.dao.UsuarioDAO;
import com.carestock.model.Lote;
import com.carestock.model.Medicamento;
import com.carestock.model.Ubicacion;
import com.carestock.utils.IngresoLoteValidator;

import java.sql.SQLException;
import java.time.LocalDate;

public class IngresoLoteService {

    private final LoteDAO loteDAO;
    private final UsuarioDAO usuarioDAO;

    public IngresoLoteService() {
        this(new LoteDAO(), new UsuarioDAO());
    }

    public IngresoLoteService(LoteDAO loteDAO, UsuarioDAO usuarioDAO) {
        this.loteDAO = loteDAO;
        this.usuarioDAO = usuarioDAO;
    }

    public void registrar(Medicamento medicamento, String numeroLote, String cantidadStr,
                          LocalDate fechaVencimiento, Ubicacion ubicacion) throws SQLException {
        String error = IngresoLoteValidator.validar(
            medicamento, numeroLote, cantidadStr, fechaVencimiento, ubicacion
        );
        if (error != null) {
            throw new IllegalArgumentException(error);
        }

        int cantidad = Integer.parseInt(cantidadStr.trim());
        Lote lote = new Lote(
            numeroLote.trim(),
            Math.toIntExact(medicamento.getIdMedicamento()),
            cantidad,
            fechaVencimiento,
            ubicacion.getIdUbicacion()
        );

        int idUsuario = usuarioDAO.obtenerIdActivoPorEmail(AppConfig.getCurrentUserEmail());
        loteDAO.registrarNuevoLote(lote, idUsuario);
    }
}
