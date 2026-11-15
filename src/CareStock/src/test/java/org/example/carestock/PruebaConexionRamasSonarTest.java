package org.example.carestock;

import org.example.carestock.dao.LoteDAOImpl;
import org.example.carestock.model.Lote;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PruebaConexionRamasSonarTest {

    private static final int FARMACIA = 7;

    @Test
    void consultaSinLotesFinalizaCorrectamente() throws Exception {
        try (MockedConstruction<LoteDAOImpl> construidos =
                     mockConstruction(LoteDAOImpl.class,
                             (dao, contexto) -> when(dao.listarPorFarmacia(FARMACIA))
                                     .thenReturn(List.of()))) {

            assertDoesNotThrow(() ->
                    PruebaConexion.main(new String[]{"7"}));

            assertEquals(1, construidos.constructed().size());
            verify(construidos.constructed().get(0))
                    .listarPorFarmacia(FARMACIA);
        }
    }

    @Test
    void consultaConLotesProcesaLosDatosYFinaliza() throws Exception {
        Lote lote = new Lote();
        lote.setIdLote(21);
        lote.setNumeroLote("LOTE-21");
        lote.setIdMedicamento(8);
        lote.setCantidadActual(12);
        lote.setFechaVencimiento(LocalDate.of(2027, 12, 31));
        lote.setEstadoLote("DISPONIBLE");

        try (MockedConstruction<LoteDAOImpl> construidos =
                     mockConstruction(LoteDAOImpl.class,
                             (dao, contexto) -> when(dao.listarPorFarmacia(FARMACIA))
                                     .thenReturn(List.of(lote)))) {

            assertDoesNotThrow(() ->
                    PruebaConexion.main(new String[]{"7"}));

            assertEquals(1, construidos.constructed().size());
            verify(construidos.constructed().get(0))
                    .listarPorFarmacia(FARMACIA);
        }
    }

    @Test
    void errorDeConsultaSeManejaSinPropagarse() throws Exception {
        try (MockedConstruction<LoteDAOImpl> construidos =
                     mockConstruction(LoteDAOImpl.class,
                             (dao, contexto) -> when(dao.listarPorFarmacia(FARMACIA))
                                     .thenThrow(new SQLException(
                                             "Fallo simulado de lectura")))) {

            assertDoesNotThrow(() ->
                    PruebaConexion.main(new String[]{"7"}));

            assertEquals(1, construidos.constructed().size());
            verify(construidos.constructed().get(0))
                    .listarPorFarmacia(FARMACIA);
        }
    }
}
