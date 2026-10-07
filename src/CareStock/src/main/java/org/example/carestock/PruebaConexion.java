package org.example.carestock;

import org.example.carestock.dao.LoteDAO;
import org.example.carestock.dao.LoteDAOImpl;
import org.example.carestock.model.Lote;

import java.util.List;

public class PruebaConexion {
    public static void main(String[] args) {
        LoteDAO loteDAO = new LoteDAOImpl();

        try {
            System.out.println("Consultando lotes registrados en Neon DB...\n");
            List<Lote> lotes = loteDAO.listarTodos();

            if (lotes.isEmpty()) {
                System.out.println("ℹ️ No hay lotes registrados actualmente.");
            } else {
                System.out.printf("%-8s %-15s %-15s %-15s %-12s %-10s%n",
                        "ID LOTE", "NÚMERO LOTE", "ID MEDICAMENTO", "CANTIDAD", "VENCIMIENTO", "ESTADO");
                System.out.println("----------------------------------------------------------------------------------");
                for (Lote l : lotes) {
                    System.out.printf("%-8d %-15s %-15d %-15d %-12s %-10s%n",
                            l.getIdLote(),
                            l.getNumeroLote(),
                            l.getIdMedicamento(),
                            l.getCantidadActual(),
                            l.getFechaVencimiento(),
                            l.getEstadoLote());
                }
            }

            System.out.println("\n✅ ¡Tarea 2 completada con éxito! Modelo Lote, LoteDAO y FarmacovigilanciaService listos.");

        } catch (Exception e) {
            System.err.println("❌ Error al consultar lotes: " + e.getMessage());
            e.printStackTrace();
        }
    }
}