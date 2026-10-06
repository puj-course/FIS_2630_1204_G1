package com.carestock.service;

import com.carestock.dao.AlertaVencimientoDAO;
import com.carestock.model.AlertaVencimiento;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlertaVencimientoService {

    private final AlertaVencimientoDAO alertaDAO = new AlertaVencimientoDAO();

    public List<AlertaVencimiento> obtenerTodas() throws SQLException {
        return alertaDAO.listarTodas();
    }

    public List<AlertaVencimiento> obtenerCriticas() throws SQLException {
        return alertaDAO.listarPorNivel("ROJO");
    }

    public List<AlertaVencimiento> obtenerPorVencer() throws SQLException {
        return alertaDAO.listarPorNivel("AMARILLO");
    }

    public List<AlertaVencimiento> obtenerVencidas() throws SQLException {
        return alertaDAO.listarPorNivel("VENCIDO");
    }

    public Map<String, List<AlertaVencimiento>> obtenerAgrupadasPorNivel() throws SQLException {
        return alertaDAO.listarTodas().stream()
                .collect(Collectors.groupingBy(AlertaVencimiento::getNivelAlerta));
    }

    public List<AlertaVencimiento> obtenerPorFarmacia(int idFarmacia) throws SQLException {
        return alertaDAO.listarPorFarmacia(idFarmacia);
    }

    public List<AlertaVencimiento> obtenerCriticasPorFarmacia(int idFarmacia) throws SQLException {
        return alertaDAO.listarPorFarmaciaYNivel(idFarmacia, "ROJO");
    }

    public List<AlertaVencimiento> obtenerPorVencerPorFarmacia(int idFarmacia) throws SQLException {
        return alertaDAO.listarPorFarmaciaYNivel(idFarmacia, "AMARILLO");
    }

    public List<AlertaVencimiento> obtenerVencidasPorFarmacia(int idFarmacia) throws SQLException {
        return alertaDAO.listarPorFarmaciaYNivel(idFarmacia, "VENCIDO");
    }

    public Map<String, List<AlertaVencimiento>> obtenerAgrupadasPorNivelYFarmacia(int idFarmacia) throws SQLException {
        return alertaDAO.listarPorFarmacia(idFarmacia).stream()
                .collect(Collectors.groupingBy(AlertaVencimiento::getNivelAlerta));
    }
}
