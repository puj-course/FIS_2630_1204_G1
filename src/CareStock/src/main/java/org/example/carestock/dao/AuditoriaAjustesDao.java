package org.example.carestock.dao;

import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.AjusteExistencias;
import org.example.carestock.session.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Objects;

/**
 * HU-792: Persistencia de auditoría de ajustes.
 *
 * Debe ejecutarse dentro de la misma transacción
 * JDBC que actualiza las existencias.
 *
 * El llamador debe:
 * 1. Bloquear y consultar el stock anterior.
 * 2. Actualizar las existencias.
 * 3. Registrar la auditoría.
 * 4. Confirmar o revertir toda la transacción.
 */
public final class AuditoriaAjustesDao {

    public long registrar(
            Connection conexion,
            AjusteExistencias ajuste
    ) throws SQLException, ReglaNegocioException {

        Objects.requireNonNull(
                conexion,
                "La conexión es obligatoria"
        );

        Objects.requireNonNull(
                ajuste,
                "El ajuste es obligatorio"
        );

        if (conexion.getAutoCommit()) {
            throw new ReglaNegocioException(
                    "La auditoría requiere una transacción activa (autoCommit=false)."
            );
        }

        if (!SesionUsuario.estaActiva()) {
            throw new ReglaNegocioException(
                    "No hay una sesión autenticada."
            );
        }

        var sesion = SesionUsuario.actual();

        if (sesion.id() != ajuste.idUsuario()
                || !Objects.equals(
                        sesion.idFarmacia(),
                        ajuste.idFarmacia()
                )) {

            throw new ReglaNegocioException(
                    "El usuario o la farmacia no corresponden a la sesión activa."
            );
        }

        String sql = """
                INSERT INTO auditoria_ajustes
                (
                    id_lote,
                    id_aseo,
                    id_maternidad,
                    id_usuario,
                    id_farmacia,
                    motivo,
                    cantidad_anterior,
                    cantidad_nueva,
                    diferencia
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING id_ajuste
                """;

        try (PreparedStatement ps =
                     conexion.prepareStatement(sql)) {

            if (ajuste.categoria()
                    == AjusteExistencias.Categoria.MEDICAMENTO) {

                ps.setInt(1, ajuste.idReferencia());
            } else {
                ps.setNull(1, Types.INTEGER);
            }

            if (ajuste.categoria()
                    == AjusteExistencias.Categoria.ASEO) {

                ps.setInt(2, ajuste.idReferencia());
            } else {
                ps.setNull(2, Types.INTEGER);
            }

            if (ajuste.categoria()
                    == AjusteExistencias.Categoria.MATERNIDAD) {

                ps.setInt(3, ajuste.idReferencia());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setInt(4, ajuste.idUsuario());
            ps.setInt(5, ajuste.idFarmacia());
            ps.setString(6, ajuste.motivo());
            ps.setInt(7, ajuste.cantidadAnterior());
            ps.setInt(8, ajuste.cantidadNueva());
            ps.setInt(9, ajuste.diferencia());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    throw new SQLException(
                            "No se generó el identificador de auditoría."
                    );
                }

                return rs.getLong(1);
            }
        }
    }
}
