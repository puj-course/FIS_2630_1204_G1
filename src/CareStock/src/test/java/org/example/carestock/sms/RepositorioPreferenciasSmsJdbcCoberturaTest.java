package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RepositorioPreferenciasSmsJdbcCoberturaTest {

    private Connection con;
    private PreparedStatement cuenta, configuracion, permisos;
    private PreparedStatement activo, inactivo, insertar, actualizar;

    private ResultSet rsCuenta, rsConfiguracion, rsPermisos;
    private ResultSet rsActivo, rsInactivo;

    private MockedStatic<ConexionBD> bd;

    private final RepositorioPreferenciasSmsJdbc dao =
            new RepositorioPreferenciasSmsJdbc();

    private ServicioPreferenciasSms.Solicitud solicitud() {
        return new ServicioPreferenciasSms.Solicitud(
                "+573001234567",
                true,
                true,
                false,
                LocalTime.of(7, 0),
                LocalTime.of(21, 0),
                "America/Bogota"
        );
    }

    @BeforeEach
    void preparar() throws Exception {
        con = mock(Connection.class);

        cuenta = mock(PreparedStatement.class);
        configuracion = mock(PreparedStatement.class);
        permisos = mock(PreparedStatement.class);
        activo = mock(PreparedStatement.class);
        inactivo = mock(PreparedStatement.class);
        insertar = mock(PreparedStatement.class);
        actualizar = mock(PreparedStatement.class);

        rsCuenta = mock(ResultSet.class);
        rsConfiguracion = mock(ResultSet.class);
        rsPermisos = mock(ResultSet.class);
        rsActivo = mock(ResultSet.class);
        rsInactivo = mock(ResultSet.class);

        when(con.prepareStatement(anyString())).thenAnswer(inv -> {
            String sql = inv.getArgument(0);

            if (sql.contains("WHERE u.id_usuario IN (?, ?)"))
                return permisos;

            if (sql.contains("SELECT id_destinatario, telefono"))
                return configuracion;

            if (sql.contains("SELECT id_destinatario FROM sms_destinatarios"))
                return sql.contains("AND telefono = ?")
                        ? inactivo : activo;

            if (sql.contains("INSERT INTO sms_destinatarios"))
                return insertar;

            if (sql.contains("UPDATE sms_destinatarios"))
                return actualizar;

            if (sql.contains("FROM usuarios u"))
                return cuenta;

            throw new SQLException("SQL inesperado: " + sql);
        });

        when(cuenta.executeQuery()).thenReturn(rsCuenta);
        when(configuracion.executeQuery())
                .thenReturn(rsConfiguracion);
        when(permisos.executeQuery()).thenReturn(rsPermisos);
        when(activo.executeQuery()).thenReturn(rsActivo);
        when(inactivo.executeQuery()).thenReturn(rsInactivo);

        bd = mockStatic(ConexionBD.class);
        bd.when(ConexionBD::getConexion).thenReturn(con);
    }

    @AfterEach
    void liberar() {
        bd.close();
    }

    private void permitirAdministrador() throws Exception {
        when(rsPermisos.next()).thenReturn(true, true, false);

        when(rsPermisos.getInt("id_usuario"))
                .thenReturn(1, 1, 2, 2);

        when(rsPermisos.getInt("id_farmacia"))
                .thenReturn(7);

        when(rsPermisos.getString("nombre_rol"))
                .thenReturn("ADMINISTRADOR", "FARMACEUTICO");

        when(rsPermisos.getString("usuario_estado"))
                .thenReturn("ACTIVO");

        when(rsPermisos.getString("farmacia_estado"))
                .thenReturn("ACTIVA");
    }

    @Test
    void consultaCuentaActiva() throws Exception {
        when(rsCuenta.next()).thenReturn(true);
        when(rsCuenta.getInt("id_usuario")).thenReturn(1);
        when(rsCuenta.getInt("id_farmacia")).thenReturn(7);
        when(rsCuenta.getString("nombre_rol"))
                .thenReturn("ADMINISTRADOR");
        when(rsCuenta.getString("usuario_estado"))
                .thenReturn("ACTIVO");
        when(rsCuenta.getString("farmacia_estado"))
                .thenReturn("ACTIVA");

        var resultado = dao.cuenta(1);

        assertNotNull(resultado);
        assertEquals(1, resultado.id());
        assertEquals(7, resultado.farmaciaId());
        assertTrue(resultado.usuarioActivo());
        assertTrue(resultado.farmaciaActiva());

        verify(cuenta).setInt(1, 1);
    }

    @Test
    void cuentaInexistenteDevuelveNull() throws Exception {
        when(rsCuenta.next()).thenReturn(false);

        assertNull(dao.cuenta(999));

        verify(cuenta).setInt(1, 999);
    }

    @Test
    void consultaConfiguracionConHorarios() throws Exception {
        when(rsConfiguracion.next()).thenReturn(true);
        when(rsConfiguracion.getLong("id_destinatario"))
                .thenReturn(55L);
        when(rsConfiguracion.getString("telefono"))
                .thenReturn("+573001234567");
        when(rsConfiguracion.getBoolean("autorizado"))
                .thenReturn(true);
        when(rsConfiguracion.getBoolean("notifica_stock_critico"))
                .thenReturn(true);
        when(rsConfiguracion.getTime("horario_inicio"))
                .thenReturn(Time.valueOf("07:00:00"));
        when(rsConfiguracion.getTime("horario_fin"))
                .thenReturn(Time.valueOf("21:00:00"));
        when(rsConfiguracion.getString("zona_horaria"))
                .thenReturn("America/Bogota");

        var resultado = dao.consultar(2, 7);

        assertNotNull(resultado);
        assertEquals(55L, resultado.idDestinatario());
        assertEquals(7, resultado.farmaciaId());
        assertEquals(LocalTime.of(7, 0),
                resultado.solicitud().horarioInicio());

        verify(configuracion).setInt(1, 2);
        verify(configuracion).setInt(2, 7);
    }

    @Test
    void consultaConfiguracionInexistente() throws Exception {
        when(rsConfiguracion.next()).thenReturn(false);

        assertNull(dao.consultar(2, 7));
    }

    @Test
    void insertaNuevoDestinatarioAutorizado()
            throws Exception {

        permitirAdministrador();

        when(rsActivo.next()).thenReturn(false);
        when(rsInactivo.next()).thenReturn(false);
        when(insertar.executeUpdate()).thenReturn(1);

        dao.guardar(1, 2, 7, solicitud());

        verify(insertar).setInt(1, 2);
        verify(insertar).setInt(2, 7);
        verify(insertar).setString(3, "+573001234567");
        verify(insertar).setBoolean(4, true);

        verify(insertar).setTime(
                8, Time.valueOf("07:00:00"));
        verify(insertar).setTime(
                9, Time.valueOf("21:00:00"));

        verify(con).commit();
        verify(con, never()).rollback();
    }

    @Test
    void actualizaDestinatarioExistente() throws Exception {
        permitirAdministrador();

        when(rsActivo.next()).thenReturn(true);
        when(rsActivo.getLong(1)).thenReturn(55L);
        when(actualizar.executeUpdate()).thenReturn(1);

        dao.guardar(1, 2, 7, solicitud());

        verify(actualizar).setLong(9, 55L);
        verify(actualizar).executeUpdate();

        verify(insertar, never()).executeUpdate();
        verify(con).commit();
    }

    @Test
    void reactivaDestinatarioInactivo() throws Exception {
        permitirAdministrador();

        when(rsActivo.next()).thenReturn(false);
        when(rsInactivo.next()).thenReturn(true);
        when(rsInactivo.getLong(1)).thenReturn(66L);
        when(actualizar.executeUpdate()).thenReturn(1);

        dao.guardar(1, 2, 7, solicitud());

        verify(inactivo).setBoolean(3, false);
        verify(inactivo).setString(
                4, "+573001234567");

        verify(actualizar).setLong(9, 66L);
        verify(con).commit();
    }

    @Test
    void rechazaAdministradorNoAutorizado() throws Exception {
        when(rsPermisos.next()).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> dao.guardar(1, 2, 7, solicitud()));

        verify(con).rollback();
        verify(con, never()).commit();
        verify(insertar, never()).executeUpdate();
    }

    @Test
    void rechazaDestinatarioDeOtraFarmacia()
            throws Exception {

        permitirAdministrador();

        when(rsPermisos.getInt("id_farmacia"))
                .thenReturn(7, 8);

        assertThrows(SecurityException.class,
                () -> dao.guardar(1, 2, 7, solicitud()));

        verify(con).rollback();
        verify(con, never()).commit();
    }

    @Test
    void revierteSiFallaInsercion() throws Exception {
        permitirAdministrador();

        when(rsActivo.next()).thenReturn(false);
        when(rsInactivo.next()).thenReturn(false);

        when(insertar.executeUpdate()).thenThrow(
                new SQLException("Fallo de insercion"));

        assertThrows(SQLException.class,
                () -> dao.guardar(1, 2, 7, solicitud()));

        verify(con).rollback();
        verify(con, never()).commit();
    }

    @Test
    void revierteSiActualizacionNoAfectaFilas()
            throws Exception {

        permitirAdministrador();

        when(rsActivo.next()).thenReturn(true);
        when(rsActivo.getLong(1)).thenReturn(55L);
        when(actualizar.executeUpdate()).thenReturn(0);

        assertThrows(SQLException.class,
                () -> dao.guardar(1, 2, 7, solicitud()));

        verify(con).rollback();
        verify(con, never()).commit();
    }
}
