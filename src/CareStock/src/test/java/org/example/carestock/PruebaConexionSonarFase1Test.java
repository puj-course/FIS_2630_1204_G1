package org.example.carestock;

import org.example.carestock.dao.LoteDAOImpl;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PruebaConexionSonarFase1Test {
    @Test
    void errorDeConsultaNoRevelaTrazaAlUsuario() throws Exception {
        try (MockedConstruction<LoteDAOImpl> lotes = mockConstruction(
                LoteDAOImpl.class, (dao, ctx) ->
                        when(dao.listarPorFarmacia(8))
                                .thenThrow(new SQLException("Consulta simulada")))) {
            assertDoesNotThrow(() -> PruebaConexion.main(new String[]{"8"}));
            assertEquals(1, lotes.constructed().size());
            verify(lotes.constructed().get(0)).listarPorFarmacia(8);
        }
    }
}
