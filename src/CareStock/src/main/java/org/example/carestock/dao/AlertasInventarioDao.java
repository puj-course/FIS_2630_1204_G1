package org.example.carestock.dao;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.dto.AlertaInventario;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class AlertasInventarioDao {

    static final String SQL = """
            SELECT tipo_alerta, categoria, id_referencia, codigo, producto, numero_lote,
                   existencias, umbral, fecha_vencimiento, dias_para_vencer, severidad,
                   ultimo_movimiento_fecha, ultimo_movimiento_tipo, ultimo_movimiento_usuario
            FROM vw_alertas_inventario
            WHERE id_farmacia = ?
            ORDER BY CASE severidad WHEN 'CRITICA' THEN 0 ELSE 1 END,
                     tipo_alerta,
                     dias_para_vencer NULLS LAST,
                     existencias,
                     producto
            """;

    public List<AlertaInventario> listarPorFarmacia(int idFarmacia) throws SQLException {
        if (idFarmacia <= 0) {
            throw new IllegalArgumentException("Farmacia inválida");
        }

        List<AlertaInventario> alertas = new ArrayList<>();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(SQL)) {
            ps.setInt(1, idFarmacia);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    alertas.add(mapear(rs));
                }
            }
        }

        return alertas;
    }

    private static AlertaInventario mapear(ResultSet rs) throws SQLException {
        Date vencimiento = rs.getDate("fecha_vencimiento");
        Timestamp movimiento = rs.getTimestamp("ultimo_movimiento_fecha");

        return new AlertaInventario(
                AlertaInventario.TipoAlerta.valueOf(rs.getString("tipo_alerta")),
                AlertaInventario.Categoria.valueOf(rs.getString("categoria")),
                rs.getInt("id_referencia"),
                rs.getString("codigo"),
                rs.getString("producto"),
                rs.getString("numero_lote"),
                rs.getInt("existencias"),
                rs.getObject("umbral", Integer.class),
                vencimiento == null ? null : vencimiento.toLocalDate(),
                rs.getObject("dias_para_vencer", Integer.class),
                AlertaInventario.Severidad.valueOf(rs.getString("severidad")),
                movimiento == null ? null : movimiento.toLocalDateTime(),
                rs.getString("ultimo_movimiento_tipo"),
                rs.getString("ultimo_movimiento_usuario"));
    }
    public void marcarComoLeida(int idUsuario, int idFarmacia, String tipoAlerta, int referenciaId) throws Exception {
        
        String sqlValidar = "SELECT 1 FROM vw_alertas_inventario WHERE id_farmacia = ? AND tipo_alerta = ? AND id_referencia = ?";
        try (java.sql.Connection con = org.example.carestock.config.ConexionBD.getConexion();
             java.sql.PreparedStatement ps = con.prepareStatement(sqlValidar)) {
            ps.setInt(1, idFarmacia);
            ps.setString(2, tipoAlerta);
            ps.setInt(3, referenciaId);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new org.example.carestock.exception.ReglaNegocioException("La alerta que intenta marcar no existe o ya fue resuelta.");
                }
            }
        }

        String sqlInsert = "INSERT INTO ALERTAS_LEIDAS (id_usuario, id_farmacia, tipo_alerta, referencia_id) VALUES (?, ?, ?, ?)";
            
        try (java.sql.Connection con = org.example.carestock.config.ConexionBD.getConexion();
             java.sql.PreparedStatement ps = con.prepareStatement(sqlInsert)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idFarmacia);
            ps.setString(3, tipoAlerta);
            ps.setInt(4, referenciaId);
            ps.executeUpdate();
        }
    }

}
