package org.example.carestock.dao;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConsultasSqlCatalogoTest {

    private static final String[] TABLAS = {
            "aseo", "maternidad"
    };

    private long contarParametros(String sql) {
        return sql.chars()
                .filter(c -> c == '?')
                .count();
    }

    @Test
    void insercionesTienenDiezParametros() {
        for (String tabla : TABLAS) {
            String sql = ConsultasSqlCatalogo.guardar(tabla);

            assertTrue(sql.contains("INSERT INTO " + tabla));
            assertTrue(sql.contains("id_farmacia"));
            assertTrue(sql.contains("id_usuario_creacion"));
            assertEquals(10, contarParametros(sql));

            String id = tabla.equals("aseo")
                    ? "id_aseo" : "id_maternidad";

            assertTrue(sql.contains("RETURNING " + id));
        }
    }

    @Test
    void consultasPorIdRespetanFarmacia() {
        for (String tabla : TABLAS) {
            String sql = ConsultasSqlCatalogo.buscarPorId(tabla);

            String id = tabla.equals("aseo")
                    ? "id_aseo" : "id_maternidad";

            assertTrue(sql.contains("FROM " + tabla));
            assertTrue(sql.contains(id + " = ?"));
            assertTrue(sql.contains("id_farmacia = ?"));
            assertEquals(2, contarParametros(sql));
        }
    }

    @Test
    void listadosFiltranPorFarmacia() {
        for (String tabla : TABLAS) {
            String sql = ConsultasSqlCatalogo
                    .listarPorFarmacia(tabla);

            assertTrue(sql.contains("FROM " + tabla));
            assertTrue(sql.contains("id_farmacia = ?"));
            assertTrue(sql.contains("ORDER BY nombre"));
            assertEquals(1, contarParametros(sql));
        }
    }

    @Test
    void actualizacionesMantienenOnceParametros() {
        for (String tabla : TABLAS) {
            String sql = ConsultasSqlCatalogo.actualizar(tabla);

            String id = tabla.equals("aseo")
                    ? "id_aseo" : "id_maternidad";

            assertTrue(sql.contains("UPDATE " + tabla));
            assertTrue(sql.contains("stock = ?"));
            assertTrue(sql.contains(id + " = ?"));
            assertTrue(sql.contains("id_farmacia = ?"));
            assertTrue(sql.contains("estado = 'ACTIVO'"));
            assertTrue(sql.contains(
                    "id_usuario_modificacion = ?"));

            assertEquals(11, contarParametros(sql));
        }
    }

    @Test
    void desactivacionesConservanTresParametros() {
        for (String tabla : TABLAS) {
            String sql = ConsultasSqlCatalogo.desactivar(tabla);

            String id = tabla.equals("aseo")
                    ? "id_aseo" : "id_maternidad";

            assertTrue(sql.contains("UPDATE " + tabla));
            assertTrue(sql.contains("estado = 'INACTIVO'"));
            assertTrue(sql.contains(id + " = ?"));
            assertTrue(sql.contains("id_farmacia = ?"));
            assertTrue(sql.contains("estado = 'ACTIVO'"));
            assertEquals(3, contarParametros(sql));
        }
    }

    @Test
    void rechazaTablasNoAutorizadas() {
        String invalida = "usuarios";
        String inyeccion = "aseo; DROP TABLE usuarios";

        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> ConsultasSqlCatalogo.guardar(invalida)),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> ConsultasSqlCatalogo.buscarPorId(invalida)),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> ConsultasSqlCatalogo.listarPorFarmacia(invalida)),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> ConsultasSqlCatalogo.actualizar(invalida)),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> ConsultasSqlCatalogo.desactivar(invalida)),
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> ConsultasSqlCatalogo.guardar(inyeccion))
        );
    }
}
