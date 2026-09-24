package com.carestock.dao;

import com.carestock.config.DatabaseConfig;
import com.carestock.model.DespachoLote;
import com.carestock.model.Lote;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoteDAO {

   
    public void registrarNuevoLote(
            Lote lote
    ) throws SQLException {

        String sql =
                "CALL sp_registrar_nuevo_lote(?, ?, ?, ?, ?, ?)";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                CallableStatement stmt =
                        conn.prepareCall(sql)
        ) {

            stmt.setString(
                    1,
                    lote.getNumeroLote()
            );

            stmt.setInt(
                    2,
                    lote.getIdMedicamento()
            );

            stmt.setInt(
                    3,
                    lote.getCantidadActual()
            );

            stmt.setDate(
                    4,
                    Date.valueOf(
                            lote.getFechaVencimiento()
                    )
            );

            stmt.setInt(
                    5,
                    lote.getIdUbicacion()
            );

            /*
             * El ID ya fue fijado por SessionContext
             * en la capa de servicio.
             */
            stmt.setInt(
                    6,
                    lote.getIdUsuario()
            );

            stmt.execute();
        }
    }

   
    public void despacharLote(
            DespachoLote despacho
    ) throws SQLException {

        String sql =
                "SELECT fn_despachar_lote(?, ?, ?)";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(
                    1,
                    despacho.getIdLote()
            );

            stmt.setInt(
                    2,
                    despacho.getCantidad()
            );

            stmt.setInt(
                    3,
                    despacho.getIdUsuario()
            );

            stmt.execute();
        }
    }

    public int contarProximosAVencer(
            int dias
    ) throws SQLException {

        String sql =
                "SELECT COUNT(*) FROM LOTES " +
                "WHERE estado_lote = 'DISPONIBLE' " +
                "AND fecha_vencimiento > CURRENT_DATE " +
                "AND fecha_vencimiento <= CURRENT_DATE + " +
                "(? * INTERVAL '1 day')";

        try (
                Connection conn =
                        DatabaseConfig.getConnection();

                PreparedStatement stmt =
                        conn.prepareStatement(sql)
        ) {

            stmt.setInt(1, dias);

            try (
                    ResultSet rs =
                            stmt.executeQuery()
            ) {

                return rs.next()
                        ? rs.getInt(1)
                        : 0;
            }
        }
    }
}
