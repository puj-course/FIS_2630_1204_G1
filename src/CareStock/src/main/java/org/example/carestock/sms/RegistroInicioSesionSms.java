package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;
import org.example.carestock.session.SesionUsuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Encola eventos para administradores con consentimiento y preferencia activa. */
public final class RegistroInicioSesionSms {

    private static final String INSERTAR = """
        INSERT INTO sms_alertas (id_destinatario, id_farmacia, clave, mensaje)
        SELECT d.id_destinatario, d.id_farmacia, ?, ?
        FROM sms_destinatarios d
        JOIN usuarios u ON u.id_usuario = d.id_usuario
             AND u.id_farmacia = d.id_farmacia AND u.estado = 'ACTIVO'
        JOIN roles r ON r.id_rol = u.id_rol
             AND UPPER(BTRIM(r.nombre_rol)) = 'ADMINISTRADOR'
        JOIN farmacias f ON f.id_farmacia = d.id_farmacia
             AND f.estado = 'ACTIVA'
        JOIN usuarios actor ON actor.id_usuario = ?
             AND actor.id_farmacia = d.id_farmacia
             AND actor.estado = 'ACTIVO'
        WHERE d.id_farmacia = ?
          AND d.activo AND d.autorizado
          AND d.autorizado_en IS NOT NULL
          AND d.notifica_inicio_sesion
        ON CONFLICT (id_destinatario, clave) WHERE activa DO NOTHING
        """;

    private final FormateadorInicioSesionSms formateador;

    public RegistroInicioSesionSms() {
        this(new FormateadorInicioSesionSms());
    }

    RegistroInicioSesionSms(FormateadorInicioSesionSms formateador) {
        this.formateador = Objects.requireNonNull(formateador);
    }

    public int registrar(SesionUsuario.DatosUsuario sesion, Instant instante)
            throws SQLException {
        Objects.requireNonNull(sesion, "Sesion requerida");
        Objects.requireNonNull(instante, "Instante requerido");
        if (sesion.idFarmacia() == null || sesion.idFarmacia() != 1
                || !sesion.esAdministrador()) {
            return 0; // Administradores globales sin farmacia no generan SMS.
        }
        String mensaje = formateador.generar(sesion, instante);
        String clave = "ACCESO:" + UUID.randomUUID();

        try (Connection con = ConexionBD.getConexion();
             PreparedStatement ps = con.prepareStatement(INSERTAR)) {
            ps.setString(1, clave);
            ps.setString(2, mensaje);
            ps.setInt(3, sesion.id());
            ps.setInt(4, sesion.idFarmacia());
            return ps.executeUpdate();
        }
    }
}
