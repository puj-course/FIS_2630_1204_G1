package org.example.carestock.dao;

import org.example.carestock.dto.AlertaInventario;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AlertasInventarioDaoMapeoTest {

    private static AlertaInventario mapear(ResultSet rs) throws Exception {
        Method metodo = AlertasInventarioDao.class
                .getDeclaredMethod("mapear", ResultSet.class);
        metodo.setAccessible(true);
        return (AlertaInventario) metodo.invoke(null, rs);
    }

    @Test
    void rechazaIdentificadoresDeFarmaciaInvalidos() {
        AlertasInventarioDao dao = new AlertasInventarioDao();

        for (int id : new int[]{0, -1, -100}) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> dao.listarPorFarmacia(id)
            );
        }
    }

    @Test
    void mapeaStockCriticoSinFechas() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("tipo_alerta"))
                .thenReturn("STOCK_MINIMO");
        when(rs.getString("categoria"))
                .thenReturn("ASEO");
        when(rs.getString("severidad"))
                .thenReturn("CRITICA");
        when(rs.getString("codigo"))
                .thenReturn("ASE-001");
        when(rs.getString("producto"))
                .thenReturn("Jabon antibacterial");
        when(rs.getInt("id_referencia"))
                .thenReturn(15);
        when(rs.getInt("existencias"))
                .thenReturn(0);
        when(rs.getObject("umbral", Integer.class))
                .thenReturn(5);

        AlertaInventario alerta = mapear(rs);

        assertEquals(
                AlertaInventario.TipoAlerta.STOCK_MINIMO,
                alerta.tipoAlerta()
        );
        assertEquals(
                AlertaInventario.Categoria.ASEO,
                alerta.categoria()
        );
        assertEquals(
                AlertaInventario.Severidad.CRITICA,
                alerta.severidad()
        );

        assertEquals(15, alerta.idReferencia());
        assertEquals(0, alerta.existencias());
        assertEquals(5, alerta.umbral());
        assertNull(alerta.fechaVencimiento());
        assertNull(alerta.ultimoMovimientoFecha());

        verify(rs).getDate("fecha_vencimiento");
        verify(rs).getTimestamp("ultimo_movimiento_fecha");
    }

    @Test
    void mapeaVencimientoConMovimientoRegistrado()
            throws Exception {
        ResultSet rs = mock(ResultSet.class);

        LocalDate vencimiento = LocalDate.of(2026, 11, 10);
        LocalDateTime movimiento =
                LocalDateTime.of(2026, 10, 30, 14, 25);

        when(rs.getString("tipo_alerta"))
                .thenReturn("VENCIMIENTO");
        when(rs.getString("categoria"))
                .thenReturn("MEDICAMENTO");
        when(rs.getString("severidad"))
                .thenReturn("ADVERTENCIA");
        when(rs.getString("codigo"))
                .thenReturn("MED-002");
        when(rs.getString("producto"))
                .thenReturn("Acetaminofen");
        when(rs.getString("numero_lote"))
                .thenReturn("LOT-123");
        when(rs.getInt("id_referencia"))
                .thenReturn(20);
        when(rs.getInt("existencias"))
                .thenReturn(8);
        when(rs.getObject("dias_para_vencer", Integer.class))
                .thenReturn(10);
        when(rs.getDate("fecha_vencimiento"))
                .thenReturn(java.sql.Date.valueOf(vencimiento));
        when(rs.getTimestamp("ultimo_movimiento_fecha"))
                .thenReturn(java.sql.Timestamp.valueOf(movimiento));
        when(rs.getString("ultimo_movimiento_tipo"))
                .thenReturn("ENTRADA");
        when(rs.getString("ultimo_movimiento_usuario"))
                .thenReturn("Administrador");

        AlertaInventario alerta = mapear(rs);

        assertEquals(
                AlertaInventario.TipoAlerta.VENCIMIENTO,
                alerta.tipoAlerta()
        );
        assertEquals(vencimiento, alerta.fechaVencimiento());
        assertEquals(movimiento, alerta.ultimoMovimientoFecha());
        assertEquals(10, alerta.diasParaVencer());
        assertEquals("LOT-123", alerta.numeroLote());
        assertEquals("ENTRADA", alerta.ultimoMovimientoTipo());
        assertEquals(
                "Administrador",
                alerta.ultimoMovimientoUsuario()
        );
    }

    @Test
    void rechazaTipoDeAlertaDesconocido() throws Exception {
        ResultSet rs = mock(ResultSet.class);

        when(rs.getString("tipo_alerta"))
                .thenReturn("TIPO_INVALIDO");

        InvocationTargetException error = assertThrows(
                InvocationTargetException.class,
                () -> mapear(rs)
        );

        assertInstanceOf(
                IllegalArgumentException.class,
                error.getCause()
        );
    }
}
