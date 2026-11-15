package org.example.carestock;

import org.example.carestock.dao.LoteDAO;
import org.example.carestock.dao.LoteDAOImpl;
import org.example.carestock.model.Lote;

import java.util.List;

public class PruebaConexion {
    private static final System.Logger LOGGER = System.getLogger(PruebaConexion.class.getName());
    public static void main(String[] args) {
        if (args.length != 1) {
            LOGGER.log(System.Logger.Level.WARNING, "Uso: PruebaConexion <idFarmacia>");
            return;
        }

        int idFarmacia;
        try {
            idFarmacia = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            LOGGER.log(System.Logger.Level.WARNING, "El identificador debe ser numerico.");
            return;
        }

        if (idFarmacia <= 0) {
            LOGGER.log(System.Logger.Level.WARNING, "La farmacia debe tener un ID positivo.");
            return;
        }

        LoteDAO loteDAO = new LoteDAOImpl();

        try {
            LOGGER.log(System.Logger.Level.INFO, "Consultando lotes registrados en Neon DB...\n");
            List<Lote> lotes = loteDAO.listarPorFarmacia(idFarmacia);

            if (lotes.isEmpty()) {
                LOGGER.log(System.Logger.Level.INFO, "ℹ️ No hay lotes registrados actualmente.");
            } else {
                LOGGER.log(System.Logger.Level.INFO,
                        String.format("%-8s %-15s %-15s %-15s %-12s %-10s",
                                "ID LOTE", "NÚMERO LOTE", "ID MEDICAMENTO",
                                "CANTIDAD", "VENCIMIENTO", "ESTADO"));
                LOGGER.log(System.Logger.Level.INFO, "----------------------------------------------------------------------------------");
                for (Lote l : lotes) {
                    LOGGER.log(System.Logger.Level.INFO,
                            String.format("%-8d %-15s %-15d %-15d %-12s %-10s",
                                    l.getIdLote(), l.getNumeroLote(),
                                    l.getIdMedicamento(), l.getCantidadActual(),
                                    l.getFechaVencimiento(), l.getEstadoLote()));
                }
            }

            LOGGER.log(System.Logger.Level.INFO, "\n✅ ¡Tarea 2 completada con éxito! Modelo Lote, LoteDAO y FarmacovigilanciaService listos.");

        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING, "❌ Error al consultar lotes: " + e.getMessage());
        }
    }
}