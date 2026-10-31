package org.example.carestock.dao;

import org.example.carestock.exception.ReglaNegocioException;
import org.example.carestock.model.AjusteExistencias;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AuditoriaAjustesCoberturaTest {

    private SesionUsuario.DatosUsuario sesion() {
        return new SesionUsuario.DatosUsuario(
                9, "Administrador", "qa@test.com",
                "ADMINISTRADOR", 7, "Farmacia QA");
    }

    private AjusteExistencias ajuste(
            AjusteExistencias.Categoria categoria,
            int farmacia) {
        return new AjusteExistencias(
                categoria, 12, 9, farmacia,
                "Conteo QA", 10, 15);
    }

    @Test
    void registraCadaCategoriaEnColumnaCorrecta() throws Exception {
        for (var categoria : AjusteExistencias.Categoria.values()) {

            Connection con = mock(Connection.class);
            PreparedStatement ps = mock(PreparedStatement.class);
            ResultSet rs = mock(ResultSet.class);

            when(con.getAutoCommit()).thenReturn(false);
            when(con.prepareStatement(anyString())).thenReturn(ps);
            when(ps.executeQuery()).thenReturn(rs);
            when(rs.next()).thenReturn(true);
            when(rs.getLong(1)).thenReturn(105L);

            try (var usuario = mockStatic(SesionUsuario.class)) {
                usuario.when(SesionUsuario::estaActiva).thenReturn(true);
                usuario.when(SesionUsuario::actual)
                        .thenReturn(sesion());

                long id = new AuditoriaAjustesDao()
                        .registrar(con, ajuste(categoria, 7));

                assertEquals(105L, id);

                verify(ps).setInt(4, 9);
                verify(ps).setInt(5, 7);
                verify(ps).setString(6, "Conteo QA");
                verify(ps).setInt(7, 10);
                verify(ps).setInt(8, 15);
                verify(ps).setInt(9, 5);

                int columna = switch (categoria) {
                    case MEDICAMENTO -> 1;
                    case ASEO -> 2;
                    case MATERNIDAD -> 3;
                };
                verify(ps).setInt(columna, 12);

                for (int i = 1; i <= 3; i++) {
                    if (i != columna) {
                        verify(ps).setNull(i, Types.INTEGER);
                    }
                }
            }
        }
    }

    @Test
    void prohíbeAuditoriaSinTransaccion() throws Exception {
        Connection con = mock(Connection.class);
        when(con.getAutoCommit()).thenReturn(true);

        assertThrows(ReglaNegocioException.class, () ->
                new AuditoriaAjustesDao().registrar(
                        con,
                        ajuste(AjusteExistencias.Categoria.ASEO, 7)));

        verify(con, never()).prepareStatement(anyString());
    }

    @Test
    void exigeSesionAutenticada() throws Exception {
        Connection con = mock(Connection.class);
        when(con.getAutoCommit()).thenReturn(false);

        try (var usuario = mockStatic(SesionUsuario.class)) {
            usuario.when(SesionUsuario::estaActiva).thenReturn(false);

            assertThrows(ReglaNegocioException.class, () ->
                    new AuditoriaAjustesDao().registrar(
                            con,
                            ajuste(AjusteExistencias.Categoria.ASEO, 7)));
        }

        verify(con, never()).prepareStatement(anyString());
    }

    @Test
    void impideAuditoriaDeOtraFarmacia() throws Exception {
        Connection con = mock(Connection.class);
        when(con.getAutoCommit()).thenReturn(false);

        try (var usuario = mockStatic(SesionUsuario.class)) {
            usuario.when(SesionUsuario::estaActiva).thenReturn(true);
            usuario.when(SesionUsuario::actual).thenReturn(sesion());

            assertThrows(ReglaNegocioException.class, () ->
                    new AuditoriaAjustesDao().registrar(
                            con,
                            ajuste(AjusteExistencias.Categoria.ASEO, 99)));
        }

        verify(con, never()).prepareStatement(anyString());
    }

    @Test
    void fallaSiNoSeGeneraIdentificador() throws Exception {
        Connection con = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(con.getAutoCommit()).thenReturn(false);
        when(con.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        try (var usuario = mockStatic(SesionUsuario.class)) {
            usuario.when(SesionUsuario::estaActiva).thenReturn(true);
            usuario.when(SesionUsuario::actual).thenReturn(sesion());

            assertThrows(SQLException.class, () ->
                    new AuditoriaAjustesDao().registrar(
                            con,
                            ajuste(AjusteExistencias.Categoria.MEDICAMENTO,
                                    7)));
        }
    }
}
