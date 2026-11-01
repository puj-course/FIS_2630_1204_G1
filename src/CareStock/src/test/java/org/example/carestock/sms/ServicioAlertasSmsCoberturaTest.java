package org.example.carestock.sms;

import org.example.carestock.config.ConexionBD;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ServicioAlertasSmsCoberturaTest {

    private Connection sincronizacion;
    private Connection limpieza;
    private Connection recuperacion;
    private Connection reclamacion;
    private Connection procesamiento;

    private PreparedStatement psEventos;
    private PreparedStatement psDestinatarios;
    private PreparedStatement psInsertar;
    private PreparedStatement psResolver;
    private PreparedStatement psLimpiar;
    private PreparedStatement psRecuperar;
    private PreparedStatement psReclamar;
    private PreparedStatement psProcesar;

    private ResultSet rsEventos;
    private ResultSet rsDestinatarios;
    private ResultSet rsReclamar;
    private ResultSet rsProcesar;

    private MockedStatic<ConexionBD> bd;
    private ServicioSms proveedor;

    @BeforeEach
    void preparar() throws Exception {
        sincronizacion = mock(Connection.class);
        limpieza = mock(Connection.class);
        recuperacion = mock(Connection.class);
        reclamacion = mock(Connection.class);
        procesamiento = mock(Connection.class);

        psEventos = mock(PreparedStatement.class);
        psDestinatarios = mock(PreparedStatement.class);
        psInsertar = mock(PreparedStatement.class);
        psResolver = mock(PreparedStatement.class);
        psLimpiar = mock(PreparedStatement.class);
        psRecuperar = mock(PreparedStatement.class);
        psReclamar = mock(PreparedStatement.class);
        psProcesar = mock(PreparedStatement.class);

        rsEventos = mock(ResultSet.class);
        rsDestinatarios = mock(ResultSet.class);
        rsReclamar = mock(ResultSet.class);
        rsProcesar = mock(ResultSet.class);

        proveedor = mock(ServicioSms.class);

        when(sincronizacion.prepareStatement(anyString()))
                .thenAnswer(inv -> {
                    String sql = inv.getArgument(0);

                    if (sql.contains("FROM medicamentos m")
                            && sql.contains("UNION ALL")) {
                        return psEventos;
                    }

                    if (sql.contains("SELECT d.id_destinatario")) {
                        return psDestinatarios;
                    }

                    if (sql.contains("INSERT INTO sms_alertas")) {
                        return psInsertar;
                    }

                    if (sql.contains("NOT (clave = ANY")) {
                        return psResolver;
                    }

                    throw new SQLException(
                            "SQL inesperado: " + sql);
                });

        when(limpieza.prepareStatement(anyString()))
                .thenReturn(psLimpiar);

        when(recuperacion.prepareStatement(anyString()))
                .thenReturn(psRecuperar);

        when(reclamacion.prepareStatement(anyString()))
                .thenReturn(psReclamar);

        when(procesamiento.prepareStatement(anyString()))
                .thenReturn(psProcesar);

        when(psEventos.executeQuery())
                .thenReturn(rsEventos);

        when(psDestinatarios.executeQuery())
                .thenReturn(rsDestinatarios);

        when(psReclamar.executeQuery())
                .thenReturn(rsReclamar);

        when(psProcesar.executeQuery())
                .thenReturn(rsProcesar);

        bd = mockStatic(ConexionBD.class);

        bd.when(ConexionBD::getConexion).thenReturn(
                sincronizacion,
                limpieza,
                recuperacion,
                reclamacion,
                procesamiento
        );
    }

    @AfterEach
    void liberar() {
        bd.close();
    }

    private void eventoStock(int farmacia) throws Exception {
        when(rsEventos.next()).thenReturn(true, false);

        when(rsEventos.getInt("farmacia"))
                .thenReturn(farmacia);

        when(rsEventos.getString("clave"))
                .thenReturn("STOCK:MED:15");

        when(rsEventos.getString("tipo"))
                .thenReturn("STOCK");

        when(rsEventos.getString("categoria"))
                .thenReturn("Medicamento");

        when(rsEventos.getString("producto"))
                .thenReturn("Acetaminofen");

        when(rsEventos.getString("lote"))
                .thenReturn("");

        when(rsEventos.getInt("unidades"))
                .thenReturn(0);

        when(rsEventos.getInt("dias"))
                .thenReturn(0);
    }

    private void destinatario(
            int farmacia,
            boolean stock,
            boolean vencimiento) throws Exception {

        when(rsDestinatarios.next())
                .thenReturn(true, false);

        when(rsDestinatarios.getLong("id_destinatario"))
                .thenReturn(71L);

        when(rsDestinatarios.getInt("id_farmacia"))
                .thenReturn(farmacia);

        when(rsDestinatarios.getString("farmacia"))
                .thenReturn("Farmacia Central");

        when(rsDestinatarios.getBoolean(
                "notifica_stock_critico"))
                .thenReturn(stock);

        when(rsDestinatarios.getBoolean(
                "notifica_vencimiento_critico"))
                .thenReturn(vencimiento);
    }

    @Test
    void rechazaDependenciasNulas() {
        assertAll(
            () -> assertThrows(
                    NullPointerException.class,
                    () -> new ServicioAlertasSms(
                            null, Map.of())),
            () -> assertThrows(
                    NullPointerException.class,
                    () -> new ServicioAlertasSms(
                            proveedor, null))
        );
    }

    @Test
    void cicloSinEventosFinalizaCorrectamente()
            throws Exception {

        new ServicioAlertasSms(
                proveedor, Map.of()).ejecutarCiclo();

        verify(sincronizacion).commit();
        verify(psLimpiar).executeUpdate();
        verify(psRecuperar).executeUpdate();
        verify(psReclamar).executeQuery();

        verify(proveedor, never())
                .enviar(anyString(), anyString());
    }

    @Test
    void registraAlertaDeStockAutorizada()
            throws Exception {

        eventoStock(7);
        destinatario(7, true, false);

        new ServicioAlertasSms(
                proveedor, Map.of()).ejecutarCiclo();

        verify(psInsertar).setLong(1, 71L);
        verify(psInsertar).setInt(2, 7);
        verify(psInsertar).setString(
                3, "STOCK:MED:15");

        verify(psInsertar).setString(
                eq(4), contains("Acetaminofen"));

        verify(psInsertar).executeUpdate();
        verify(psResolver).setLong(1, 71L);
        verify(sincronizacion).commit();
    }

    @Test
    void noRegistraStockSiPreferenciaEstaDesactivada()
            throws Exception {

        eventoStock(7);
        destinatario(7, false, true);

        new ServicioAlertasSms(
                proveedor, Map.of()).ejecutarCiclo();

        verify(psInsertar, never()).executeUpdate();
        verify(psResolver).executeUpdate();
        verify(sincronizacion).commit();
    }

    @Test
    void noMezclaEventosDeFarmaciasDistintas()
            throws Exception {

        eventoStock(7);
        destinatario(8, true, true);

        new ServicioAlertasSms(
                proveedor, Map.of()).ejecutarCiclo();

        verify(psInsertar, never()).executeUpdate();
        verify(psResolver).executeUpdate();
        verify(sincronizacion).commit();
    }

    @Test
    void registraAlertaDeLoteVencido()
            throws Exception {

        when(rsEventos.next()).thenReturn(true, false);

        when(rsEventos.getInt("farmacia"))
                .thenReturn(7);

        when(rsEventos.getString("clave"))
                .thenReturn("LOTE:88");

        when(rsEventos.getString("tipo"))
                .thenReturn("VENCIMIENTO");

        when(rsEventos.getString("categoria"))
                .thenReturn("Medicamento");

        when(rsEventos.getString("producto"))
                .thenReturn("Ibuprofeno");

        when(rsEventos.getString("lote"))
                .thenReturn("L-88");

        when(rsEventos.getInt("unidades"))
                .thenReturn(12);

        when(rsEventos.getInt("dias"))
                .thenReturn(-2);

        destinatario(7, false, true);

        new ServicioAlertasSms(
                proveedor, Map.of()).ejecutarCiclo();

        verify(psInsertar).setString(3, "LOTE:88");

        verify(psInsertar).setString(
                eq(4), contains("Vencido hace 2 dias"));

        verify(psInsertar).executeUpdate();
    }

    @Test
    void revierteSincronizacionSiFallaConsulta()
            throws Exception {

        when(psEventos.executeQuery()).thenThrow(
                new SQLException("Fallo de consulta"));

        SQLException error = assertThrows(
                SQLException.class,
                () -> new ServicioAlertasSms(
                        proveedor, Map.of()).ejecutarCiclo());

        assertEquals("Fallo de consulta",
                error.getMessage());

        verify(sincronizacion).rollback();
        verify(sincronizacion, never()).commit();

        verifyNoInteractions(proveedor);
    }

    @Test
    void revierteSiPlantillaSmsEsInvalida()
            throws Exception {

        eventoStock(7);
        destinatario(7, true, true);

        Map<String, String> config = Map.of(
                "SMS_STOCK_TEMPLATE", "X".repeat(1601)
        );

        SQLException error = assertThrows(
                SQLException.class,
                () -> new ServicioAlertasSms(
                        proveedor, config).ejecutarCiclo());

        assertNotNull(error.getCause());
        assertInstanceOf(
                IllegalArgumentException.class,
                error.getCause());

        verify(sincronizacion).rollback();
        verify(sincronizacion, never()).commit();
    }

    @Test
    void rechazaLimiteDeEnviosFueraDeRango()
            throws Exception {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ServicioAlertasSms(
                        proveedor,
                        Map.of("SMS_MAX_POR_CICLO", "0"))
                        .ejecutarCiclo());

        verify(psLimpiar).executeUpdate();
        verify(psRecuperar).executeUpdate();

        verify(psReclamar, never()).executeQuery();
    }

    @Test
    void reclamaAlertaSinEnviarSiYaNoExiste()
            throws Exception {

        when(rsReclamar.next()).thenReturn(true);

        when(rsReclamar.getLong("id_alerta"))
                .thenReturn(44L);

        when(rsReclamar.getString("telefono"))
                .thenReturn("+573001234567");

        when(rsReclamar.getString("mensaje"))
                .thenReturn("Alerta");

        when(rsProcesar.next()).thenReturn(false);

        new ServicioAlertasSms(
                proveedor, Map.of()).ejecutarCiclo();

        verify(psProcesar).setLong(1, 44L);
        verify(procesamiento).commit();

        verify(proveedor, never())
                .enviar(anyString(), anyString());
    }
}
