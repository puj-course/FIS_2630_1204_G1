package org.example.carestock.service;

import org.example.carestock.dto.AlertaInventario;
import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;
import org.example.carestock.dto.AlertaInventario.TipoAlerta;
import org.example.carestock.session.SesionUsuario.DatosUsuario;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PoliticaAccionesAlertasTest {

    private static DatosUsuario actor(String rol, Integer farmacia) {
        return new DatosUsuario(7, "Usuario", "test@ejemplo.test", rol, farmacia, "Farmacia");
    }

    private static AlertaInventario alerta(TipoAlerta tipo, Categoria categoria, String lote) {
        return new AlertaInventario(tipo, categoria, 21, "SKU-21", "Producto 21", lote,
                2, tipo == TipoAlerta.STOCK_MINIMO ? 5 : null,
                tipo == TipoAlerta.VENCIMIENTO ? LocalDate.now().plusDays(3) : null,
                tipo == TipoAlerta.VENCIMIENTO ? 3 : null,
                Severidad.ADVERTENCIA, null, null, null);
    }

    @Test
    void administradorPuedeReponerTresCategorias() {
        for (Categoria categoria : Categoria.values()) {
            assertEquals(PoliticaAccionesAlertas.Accion.REPONER_STOCK,
                    PoliticaAccionesAlertas.exigir(
                            alerta(TipoAlerta.STOCK_MINIMO, categoria, null),
                            actor("ADMINISTRADOR", 4), 4));
        }
    }

    @Test
    void farmaceuticoPuedeGestionarLote() {
        assertEquals(PoliticaAccionesAlertas.Accion.GESTIONAR_LOTE,
                PoliticaAccionesAlertas.exigir(
                        alerta(TipoAlerta.VENCIMIENTO, Categoria.MEDICAMENTO, "LOT-21"),
                        actor(" FARMACEUTICO ", 4), 4));
    }

    @Test
    void auxiliarSoloConsulta() {
        for (TipoAlerta tipo : TipoAlerta.values()) {
            var a = alerta(tipo, Categoria.MEDICAMENTO, "LOT-21");
            assertTrue(PoliticaAccionesAlertas.disponible(a,
                    actor("AUXILIAR", 4), 4).isEmpty());
            assertThrows(SecurityException.class, () ->
                    PoliticaAccionesAlertas.exigir(a, actor("AUXILIAR", 4), 4));
        }
    }

    @Test
    void rechazaFarmaciaDistintaYSesionSinFarmacia() {
        var a = alerta(TipoAlerta.STOCK_MINIMO, Categoria.ASEO, null);
        assertTrue(PoliticaAccionesAlertas.disponible(a, actor("ADMINISTRADOR", 4), 5).isEmpty());
        assertTrue(PoliticaAccionesAlertas.disponible(a, actor("ADMINISTRADOR", null), 4).isEmpty());
        assertTrue(PoliticaAccionesAlertas.disponible(a, actor("ADMINISTRADOR", 4), 0).isEmpty());
        assertTrue(PoliticaAccionesAlertas.disponible(a, actor("ADMINISTRADOR", 4), null).isEmpty());
    }

    @Test
    void rechazaDatosIncompletosYRolNoGestor() {
        var a = alerta(TipoAlerta.STOCK_MINIMO, Categoria.MATERNIDAD, null);
        assertTrue(PoliticaAccionesAlertas.disponible(null, actor("ADMINISTRADOR", 4), 4).isEmpty());
        assertTrue(PoliticaAccionesAlertas.disponible(a, null, 4).isEmpty());
        assertFalse(PoliticaAccionesAlertas.gestorPermitido(null));
        assertFalse(PoliticaAccionesAlertas.gestorPermitido("OPERADOR"));
        assertTrue(PoliticaAccionesAlertas.gestorPermitido("SUPER_ADMIN"));
    }

    @Test
    void rechazaVencimientosSinLote() {
        for (String lote : new String[]{null, "  "}) {
            assertTrue(PoliticaAccionesAlertas.disponible(
                    alerta(TipoAlerta.VENCIMIENTO, Categoria.MEDICAMENTO, lote),
                    actor("ADMINISTRADOR", 4), 4).isEmpty());
        }
        assertTrue(PoliticaAccionesAlertas.disponible(
                alerta(TipoAlerta.VENCIMIENTO, Categoria.ASEO, "LOT-21"),
                actor("ADMINISTRADOR", 4), 4).isEmpty());
    }
}
