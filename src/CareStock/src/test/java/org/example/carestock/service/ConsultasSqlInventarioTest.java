package org.example.carestock.service;

import org.example.carestock.model.AjusteExistencias;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsultasSqlInventarioTest {

    private void verificarSql(
            String sql,
            String tabla,
            String columnaId,
            int parametros) {

        assertTrue(sql.contains(tabla));
        assertTrue(sql.contains(columnaId + " = ?"));
        assertTrue(sql.contains("id_farmacia = ?"));

        long cantidad = sql.chars()
                .filter(c -> c == '?')
                .count();

        assertEquals(parametros, cantidad);
    }

    @Test
    void bloqueaAseoEnMovimientos() {
        String sql = ConsultasSqlInventario
                .bloquearMovimiento(
                        ServicioMovimientosProducto.Categoria.ASEO);

        verificarSql(sql, "FROM aseo", "id_aseo", 2);
        assertTrue(sql.contains("FOR UPDATE"));
    }

    @Test
    void bloqueaMaternidadEnMovimientos() {
        String sql = ConsultasSqlInventario
                .bloquearMovimiento(
                        ServicioMovimientosProducto.Categoria.MATERNIDAD);

        verificarSql(sql, "FROM maternidad",
                "id_maternidad", 2);

        assertTrue(sql.contains("FOR UPDATE"));
    }

    @Test
    void actualizaMovimientosConParametrosCorrectos() {
        for (var categoria :
                ServicioMovimientosProducto.Categoria.values()) {

            String sql = ConsultasSqlInventario
                    .actualizarMovimiento(categoria);

            String tabla = categoria ==
                    ServicioMovimientosProducto.Categoria.ASEO
                    ? "UPDATE aseo" : "UPDATE maternidad";

            String columna = categoria ==
                    ServicioMovimientosProducto.Categoria.ASEO
                    ? "id_aseo" : "id_maternidad";

            verificarSql(sql, tabla, columna, 4);

            assertTrue(sql.contains(
                    "id_usuario_modificacion = ?"));
        }
    }

    @Test
    void bloqueaAjustesPorFarmacia() {
        for (var categoria : new AjusteExistencias.Categoria[]{
                AjusteExistencias.Categoria.ASEO,
                AjusteExistencias.Categoria.MATERNIDAD}) {

            String sql = ConsultasSqlInventario
                    .bloquearAjuste(categoria);

            String tabla = categoria ==
                    AjusteExistencias.Categoria.ASEO
                    ? "FROM aseo" : "FROM maternidad";

            String columna = categoria ==
                    AjusteExistencias.Categoria.ASEO
                    ? "id_aseo" : "id_maternidad";

            verificarSql(sql, tabla, columna, 2);

            assertTrue(sql.contains("FOR UPDATE"));
        }
    }

    @Test
    void actualizaAjustesConControlDeConcurrencia() {
        for (var categoria : new AjusteExistencias.Categoria[]{
                AjusteExistencias.Categoria.ASEO,
                AjusteExistencias.Categoria.MATERNIDAD}) {

            String sql = ConsultasSqlInventario
                    .actualizarAjuste(categoria);

            String tabla = categoria ==
                    AjusteExistencias.Categoria.ASEO
                    ? "UPDATE aseo" : "UPDATE maternidad";

            String columna = categoria ==
                    AjusteExistencias.Categoria.ASEO
                    ? "id_aseo" : "id_maternidad";

            verificarSql(sql, tabla, columna, 5);

            assertTrue(sql.contains("AND stock = ?"));
        }
    }

    @Test
    void impideUsarConsultasDeProductoParaLotes() {
        assertAll(
            () -> assertThrows(
                    IllegalArgumentException.class,
                    () -> ConsultasSqlInventario.bloquearAjuste(
                            AjusteExistencias.Categoria.MEDICAMENTO)),
            () -> assertThrows(
                    IllegalArgumentException.class,
                    () -> ConsultasSqlInventario.actualizarAjuste(
                            AjusteExistencias.Categoria.MEDICAMENTO))
        );
    }
}
