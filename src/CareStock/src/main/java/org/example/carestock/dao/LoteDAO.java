package org.example.carestock.dao;

import org.example.carestock.model.Lote;
import java.util.List;

/**
 *  usamos interfaces dentro del DAO por que estamos usando el estilo arquitectonico de
 *  capas de esa forma las intefaces definen los metodos y
 *  ya las clases implementan su consulta en el postgreSQL
 */
public interface LoteDAO {
    void guardar(Lote lote) throws Exception;
    Lote buscarPorId(int idLote) throws Exception;
    List<Lote> listarPorMedicamento(int idMedicamento) throws Exception;
    List<Lote> listarTodos() throws Exception;
    void actualizarCantidad(int idLote, int nuevaCantidad) throws Exception;
    void actualizarEstado(int idLote, String nuevoEstado) throws Exception;
}