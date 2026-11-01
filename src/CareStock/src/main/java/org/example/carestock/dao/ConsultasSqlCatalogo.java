package org.example.carestock.dao;

/**
 * Consultas SQL predefinidas para los catalogos
 * de aseo y maternidad.
 *
 * Evita concatenar identificadores SQL dinamicos.
 * Los valores se asignan mediante PreparedStatement.
 */
final class ConsultasSqlCatalogo {

    private ConsultasSqlCatalogo() {
    }

    private record Sentencias(
            String guardar,
            String buscarPorId,
            String listarPorFarmacia,
            String actualizar,
            String desactivar) {
    }

    private static final Sentencias ASEO = new Sentencias(
        """
        INSERT INTO aseo
        (codigo, nombre, descripcion, precio, stock,
         tipo_aseo, biodegradable, componentes_activos, stock_minimo,
         id_farmacia, id_usuario_creacion, estado)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVO')
        RETURNING id_aseo
        """,
        """
        SELECT * FROM aseo
        WHERE id_aseo = ? AND id_farmacia = ?
        """,
        """
        SELECT * FROM aseo
        WHERE id_farmacia = ?
        ORDER BY nombre
        """,
        """
        UPDATE aseo
        SET codigo = ?, nombre = ?, descripcion = ?,
            precio = ?, stock = ?,
            tipo_aseo = ?, biodegradable = ?,
            componentes_activos = ?, stock_minimo = ?,
            id_usuario_modificacion = ?,
            fecha_modificacion = CURRENT_TIMESTAMP
        WHERE id_aseo = ? AND id_farmacia = ?
          AND estado = 'ACTIVO'
        """,
        """
        UPDATE aseo
        SET estado = 'INACTIVO',
            id_usuario_modificacion = ?,
            fecha_modificacion = CURRENT_TIMESTAMP
        WHERE id_aseo = ? AND id_farmacia = ?
          AND estado = 'ACTIVO'
        """
    );

    private static final Sentencias MATERNIDAD = new Sentencias(
        """
        INSERT INTO maternidad
        (codigo, nombre, descripcion, precio, stock,
         etapa_recomendada, hipoalergenico,
         edad_gestacional_sugerida, stock_minimo,
         id_farmacia, id_usuario_creacion, estado)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVO')
        RETURNING id_maternidad
        """,
        """
        SELECT * FROM maternidad
        WHERE id_maternidad = ? AND id_farmacia = ?
        """,
        """
        SELECT * FROM maternidad
        WHERE id_farmacia = ?
        ORDER BY nombre
        """,
        """
        UPDATE maternidad
        SET codigo = ?, nombre = ?, descripcion = ?,
            precio = ?, stock = ?,
            etapa_recomendada = ?, hipoalergenico = ?,
            edad_gestacional_sugerida = ?, stock_minimo = ?,
            id_usuario_modificacion = ?,
            fecha_modificacion = CURRENT_TIMESTAMP
        WHERE id_maternidad = ? AND id_farmacia = ?
          AND estado = 'ACTIVO'
        """,
        """
        UPDATE maternidad
        SET estado = 'INACTIVO',
            id_usuario_modificacion = ?,
            fecha_modificacion = CURRENT_TIMESTAMP
        WHERE id_maternidad = ? AND id_farmacia = ?
          AND estado = 'ACTIVO'
        """
    );

    private static Sentencias obtener(String tabla) {
        if (tabla == null) {
            throw new IllegalArgumentException(
                "La tabla del catalogo es obligatoria");
        }

        return switch (tabla) {
            case "aseo" -> ASEO;
            case "maternidad" -> MATERNIDAD;
            default -> throw new IllegalArgumentException(
                "Catalogo no autorizado: " + tabla);
        };
    }

    static String guardar(String tabla) {
        return obtener(tabla).guardar();
    }

    static String buscarPorId(String tabla) {
        return obtener(tabla).buscarPorId();
    }

    static String listarPorFarmacia(String tabla) {
        return obtener(tabla).listarPorFarmacia();
    }

    static String actualizar(String tabla) {
        return obtener(tabla).actualizar();
    }

    static String desactivar(String tabla) {
        return obtener(tabla).desactivar();
    }
}
