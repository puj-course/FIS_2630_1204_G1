package org.example.carestock.sms;

import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicioPreferenciasSmsTest {

    @Mock
    ServicioPreferenciasSms.Repositorio repo;

    private final SesionUsuario.DatosUsuario sesion =
            new SesionUsuario.DatosUsuario(
                    1, "Admin", "qa@example.invalid",
                    "ADMINISTRADOR", 10, "Farmacia QA"
            );

    private final ServicioPreferenciasSms.Cuenta admin =
            new ServicioPreferenciasSms.Cuenta(
                    1, 10, "ADMINISTRADOR", true, true
            );

    private final ServicioPreferenciasSms.Cuenta destinatario =
            new ServicioPreferenciasSms.Cuenta(
                    2, 10, "FARMACEUTICO", true, true
            );

    private final ServicioPreferenciasSms.Solicitud solicitud =
            new ServicioPreferenciasSms.Solicitud(
                    "+573001234567",
                    true,
                    true,
                    true,
                    LocalTime.of(8, 0),
                    LocalTime.of(20, 0),
                    "America/Bogota"
            );

    private ServicioPreferenciasSms servicio() {
        return new ServicioPreferenciasSms(repo, () -> sesion);
    }

    private void prepararRoles() throws SQLException {
        when(repo.cuenta(1)).thenReturn(admin);
        when(repo.cuenta(2)).thenReturn(destinatario);
    }

    @Test
    void sv01AdministradorPuedeGuardar() throws SQLException {
        prepararRoles();

        servicio().guardar(2, solicitud);

        verify(repo).guardar(1, 2, 10, solicitud);
    }

    @Test
    void sv02RechazaUsuarioSinPermisos() throws SQLException {
        when(repo.cuenta(1)).thenReturn(
                new ServicioPreferenciasSms.Cuenta(
                        1, 10, "AUXILIAR", true, true
                )
        );

        assertThrows(
                SecurityException.class,
                () -> servicio().guardar(2, solicitud)
        );

        verify(repo, never()).guardar(
                anyInt(), anyInt(), anyInt(), any()
        );
    }

    @Test
    void sv03RechazaOtraFarmacia() throws SQLException {
        when(repo.cuenta(1)).thenReturn(admin);

        when(repo.cuenta(2)).thenReturn(
                new ServicioPreferenciasSms.Cuenta(
                        2, 11, "FARMACEUTICO", true, true
                )
        );

        assertThrows(
                SecurityException.class,
                () -> servicio().guardar(2, solicitud)
        );
    }

    @Test
    void sv04RechazaTelefonoInvalido() throws SQLException {
        prepararRoles();

        var invalida = new ServicioPreferenciasSms.Solicitud(
                "3001234567", true, true, true,
                null, null, "America/Bogota"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> servicio().guardar(2, invalida)
        );
    }

    @Test
    void sv05SinConsentimientoNoGenera() {
        assertFalse(ReglasNotificacionSms.puedeGenerar(
                true, false, false, true, true,
                "STOCK", true, true
        ));
    }

    @Test
    void sv06StockDeshabilitado() {
        assertFalse(ReglasNotificacionSms.puedeGenerar(
                true, true, true, true, true,
                "STOCK", false, true
        ));
    }

    @Test
    void sv07VencimientoDeshabilitado() {
        assertFalse(ReglasNotificacionSms.puedeGenerar(
                true, true, true, true, true,
                "VENCIMIENTO", true, false
        ));
    }

    @Test
    void sv08EventoNoUrgenteFueraHorario() {
        assertFalse(ReglasNotificacionSms.puedeEnviar(
                true,
                false,
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                "America/Bogota",
                Instant.parse("2026-10-28T04:00:00Z")
        ));
    }

    @Test
    void sv09CriticaFueraHorario() {
        assertTrue(ReglasNotificacionSms.puedeEnviar(
                true,
                true,
                LocalTime.of(8, 0),
                LocalTime.of(20, 0),
                "America/Bogota",
                Instant.parse("2026-10-28T04:00:00Z")
        ));
    }

    @Test
    void sv10UsuarioOFarmaciaInactivos() {
        assertFalse(ReglasNotificacionSms.puedeGenerar(
                true, true, true, false, true,
                "STOCK", true, true
        ));

        assertFalse(ReglasNotificacionSms.puedeGenerar(
                true, true, true, true, false,
                "STOCK", true, true
        ));
    }

    @Test
    void sv11RevocacionNoLlamaProveedor() {
        ServicioSms proveedor = mock(ServicioSms.class);

        var resultado = new DespachadorProtegidoSms(
                proveedor
        ).enviarSiPermitido(
                false,
                true,
                null,
                null,
                "America/Bogota",
                Instant.now(),
                "+573001234567",
                "Alerta"
        );

        assertTrue(resultado.isEmpty());
        verifyNoInteractions(proveedor);
    }

    @Test
    void sv12ActualizarYConsultar() throws SQLException {
        prepararRoles();

        var nueva = new ServicioPreferenciasSms.Solicitud(
                "+573001234567",
                true,
                false,
                true,
                null,
                null,
                "America/Bogota"
        );

        var retorno = new ServicioPreferenciasSms.Configuracion(
                42L, 2, 10, nueva
        );

        when(repo.consultar(2, 10)).thenReturn(retorno);

        var servicio = servicio();

        servicio.guardar(2, nueva);

        assertEquals(
                nueva,
                servicio.consultar(2).solicitud()
        );

        verify(repo).guardar(1, 2, 10, nueva);
    }
}
