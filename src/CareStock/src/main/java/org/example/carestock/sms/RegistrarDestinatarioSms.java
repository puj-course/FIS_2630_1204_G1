package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;

public final class RegistrarDestinatarioSms {

    private RegistrarDestinatarioSms() {
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2 ||
                !"SI".equalsIgnoreCase(
                        System.getenv("SMS_CONSENTIMIENTO_CONFIRMADO")
                )) {
            throw new IllegalArgumentException(
                    "Uso: RegistrarDestinatarioSms email +57numero. "
                    + "Requiere SMS_CONSENTIMIENTO_CONFIRMADO=SI."
            );
        }

        ValidacionSms.exigirNumeroValido(args[1]);

        String sql = """
            INSERT INTO sms_destinatarios
                (id_usuario, id_farmacia, telefono,
                 autorizado, autorizado_en)
            SELECT u.id_usuario, u.id_farmacia, ?, TRUE, NOW()
            FROM usuarios u
            JOIN roles r ON r.id_rol = u.id_rol
            WHERE LOWER(u.email) = LOWER(?)
              AND u.estado = 'ACTIVO'
              AND u.id_farmacia IS NOT NULL
              AND UPPER(r.nombre_rol) IN (
                  'ADMINISTRADOR',
                  'JEFE_FARMACIA',
                  'JEFE DE FARMACIA'
              )
            ON CONFLICT (id_usuario, id_farmacia, telefono)
            DO UPDATE SET
                autorizado = TRUE,
                autorizado_en = NOW(),
                activo = TRUE
            """;

        try (Connection con =
                     ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, args[1]);
            ps.setString(2, args[0]);

            int filas = ps.executeUpdate();

            if (filas != 1) {
                throw new IllegalArgumentException(
                    "Usuario no encontrado, sin farmacia "
                    + "o sin rol autorizado."
                );
            }

            System.out.println(
                    "Destinatario autorizado registrado."
            );

        } finally {
            ConexionBD.cerrarPool();
        }
    }
}
