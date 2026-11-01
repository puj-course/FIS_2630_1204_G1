package org.example.carestock.dao;

import org.example.carestock.model.Medicamento;
import java.util.List;
import java.util.Map;

public interface MedicamentoDAO {
    void guardar(Medicamento medicamento) throws Exception;
    Medicamento buscarPorId(int idMedicamento) throws Exception;
    Medicamento buscarPorCodigoInvima(String codigoInvima) throws Exception;
    List<Medicamento> listarTodos() throws Exception;
    void actualizarEstado(int idMedicamento, String nuevoEstado) throws Exception;
    Map<Integer, String> listarCategorias() throws Exception;
    Map<Integer, String> categoriaPorMedicamento() throws Exception;
}