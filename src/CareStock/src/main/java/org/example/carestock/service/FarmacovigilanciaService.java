package org.example.carestock.service;

import org.example.carestock.dao.LoteDAO;
import org.example.carestock.dao.LoteDAOImpl;
import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.Lote;

public class FarmacovigilanciaService {

    private final LoteDAO loteDAO;

    public FarmacovigilanciaService() {
        this.loteDAO = new LoteDAOImpl();
    }

  
    public void dispensarMedicamento(int idLote, int cantidadADispensar) throws Exception {
        Lote lote = loteDAO.buscarPorId(idLote);

        if (lote == null) {
            throw new ReglaNegocioException("El lote especificado no existe en la base de datos.");
        }

        // 1. Ejecutar validación estricta de la entidad Lote (frena si está vencido o bloqueado)
        lote.validar();

        // 2. Verificar disponibilidad de stock
        if (lote.getCantidadActual() < cantidadADispensar) {
            throw new ReglaNegocioException("Stock insuficiente en el lote " + lote.getNumeroLote() +
                    ". Disponible: " + lote.getCantidadActual() + ", Solicitado: " + cantidadADispensar);
        }

        // 3. Descontar stock e invocar al DAO
        int nuevaCantidad = lote.getCantidadActual() - cantidadADispensar;
        loteDAO.actualizarCantidad(idLote, nuevaCantidad);

        if (nuevaCantidad == 0) {
            loteDAO.actualizarEstado(idLote, "AGOTADO");
        }
    }
}
