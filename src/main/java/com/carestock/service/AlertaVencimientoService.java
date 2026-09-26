package com.carestock.service;

import com.carestock.dao.AlertaVencimientoDAO;
import com.carestock.model.AlertaVencimiento;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Capa de negocio para el panel de alertas de vencimiento (HU.39).
 * El controlador/vista JavaFX del dashboard debe consumir esta clase,
 * no llamar directamente a AlertaVencimientoDAO.
 */
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

    /** Agrupa todas las alertas activas por nivel, para pintar el resumen del dashboard. */
    public Map<String, List<AlertaVencimiento>> obtenerAgrupadasPorNivel() throws SQLException {
        return alertaDAO.listarTodas().stream()
                .collect(Collectors.groupingBy(AlertaVencimiento::getNivelAlerta));
    }
}
