package org.example.carestock.dao;

import org.example.carestock.model.Lote;
import java.util.List;

public interface LoteDAO {
    void guardar(Lote lote) throws Exception;
    Lote buscarPorId(int idLote) throws Exception;
    List<Lote> listarPorMedicamento(int idMedicamento) throws Exception;

    /** Solo para procesos internos con autorizacion global explicita. */
    @Deprecated
    List<Lote> listarTodos() throws Exception;

    /** Consulta de inventario acotada a una farmacia. */
    List<Lote> listarPorFarmacia(int idFarmacia) throws Exception;

    void actualizarCantidad(int idLote, int nuevaCantidad) throws Exception;
    void actualizarEstado(int idLote, String nuevoEstado) throws Exception;
}
