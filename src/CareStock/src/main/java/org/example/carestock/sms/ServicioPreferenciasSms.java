package org.example.carestock.sms;

import org.example.carestock.session.SesionUsuario;

import java.sql.SQLException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.DateTimeException;
import java.util.Objects;
import java.util.function.Supplier;

public final class ServicioPreferenciasSms {

    public record Cuenta(
            int id,
            int farmaciaId,
            String rol,
            boolean usuarioActivo,
            boolean farmaciaActiva
    ) {}

    public record Solicitud(
            String telefono,
            boolean autorizado,
            boolean notificaStockCritico,
            boolean notificaVencimientoCritico,
            LocalTime horarioInicio,
            LocalTime horarioFin,
            String zonaHoraria
    ) {}

    public record Configuracion(
            long idDestinatario,
            int idUsuario,
            int farmaciaId,
            Solicitud solicitud
    ) {}

    public interface Repositorio {
        Cuenta cuenta(int usuarioId) throws SQLException;

        Configuracion consultar(
                int usuarioId, int farmaciaId
        ) throws SQLException;

        void guardar(
                int administradorId,
                int usuarioId,
                int farmaciaId,
                Solicitud solicitud
        ) throws SQLException;
    }

    private final Repositorio repositorio;
    private final Supplier<SesionUsuario.DatosUsuario> sesion;

    public ServicioPreferenciasSms(
            Repositorio repositorio,
            Supplier<SesionUsuario.DatosUsuario> sesion) {

        this.repositorio = Objects.requireNonNull(repositorio);
        this.sesion = Objects.requireNonNull(sesion);
    }

    public ServicioPreferenciasSms() {
        this(
                new RepositorioPreferenciasSmsJdbc(),
                SesionUsuario::actual
        );
    }

    public void guardar(
            int destinatarioId,
            Solicitud solicitud) throws SQLException {

        int administradorId = autorizadoPara(destinatarioId);
        validarSolicitud(solicitud);

        int farmaciaId = Objects.requireNonNull(
                sesion.get().idFarmacia()
        );

        repositorio.guardar(
                administradorId,
                destinatarioId,
                farmaciaId,
                solicitud
        );
    }

    public Configuracion consultar(int destinatarioId)
            throws SQLException {

        autorizadoPara(destinatarioId);

        int farmaciaId = Objects.requireNonNull(
                sesion.get().idFarmacia()
        );

        return repositorio.consultar(
                destinatarioId,
                farmaciaId
        );
    }

    private int autorizadoPara(int destinatarioId)
            throws SQLException {

        SesionUsuario.DatosUsuario actual = sesion.get();

        if (actual == null
                || actual.idFarmacia() == null
                || actual.id() <= 0
                || !actual.esAdministrador()) {
            throw new SecurityException(
                    "Sesion sin permiso de administrador"
            );
        }

        Cuenta administrador = repositorio.cuenta(actual.id());

        if (administrador == null
                || administrador.id() != actual.id()
                || !administrador.usuarioActivo()
                || !administrador.farmaciaActiva()
                || administrador.farmaciaId() != actual.idFarmacia()
                || !"ADMINISTRADOR".equalsIgnoreCase(
                        administrador.rol())) {

            throw new SecurityException(
                    "Administrador sin autorizacion vigente"
            );
        }

        Cuenta destinatario = repositorio.cuenta(destinatarioId);

        if (destinatario == null
                || !destinatario.usuarioActivo()
                || !destinatario.farmaciaActiva()
                || destinatario.farmaciaId()
                        != administrador.farmaciaId()) {

            throw new SecurityException(
                    "Destinatario fuera de la farmacia autorizada"
            );
        }

        return actual.id();
    }

    private static void validarSolicitud(Solicitud solicitud) {
        Objects.requireNonNull(
                solicitud, "Configuracion requerida"
        );

        ValidacionSms.exigirNumeroValido(solicitud.telefono());

        if ((solicitud.horarioInicio() == null)
                    != (solicitud.horarioFin() == null)
                || (solicitud.horarioInicio() != null
                    && solicitud.horarioInicio().equals(
                            solicitud.horarioFin()))) {

            throw new IllegalArgumentException(
                    "Horario SMS incompleto o vacio"
            );
        }

        try {
            ZoneId.of(
                    Objects.requireNonNull(solicitud.zonaHoraria())
            );
        } catch (DateTimeException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "Zona horaria no valida", e
            );
        }
    }
}
