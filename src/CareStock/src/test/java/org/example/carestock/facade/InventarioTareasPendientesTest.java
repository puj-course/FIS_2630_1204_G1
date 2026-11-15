package org.example.carestock.facade;

import org.example.carestock.model.Usuario;
import org.example.carestock.session.SesionUsuario;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventarioTareasPendientesTest {

    @Test
    void testObtenerTareasPendientes() throws Exception {
        // 1. Simulamos el usuario
        Usuario usuarioMock = new Usuario();
        usuarioMock.setIdUsuario(1);

        // 2. Simulamos la sesión activa
        SesionUsuario.DatosUsuario sesionMock = mock(SesionUsuario.DatosUsuario.class);
        when(sesionMock.id()).thenReturn(1);
        when(sesionMock.idFarmacia()).thenReturn(10);

        try (MockedStatic<SesionUsuario> sesionStatic = Mockito.mockStatic(SesionUsuario.class)) {
            sesionStatic.when(SesionUsuario::actual).thenReturn(sesionMock);
            sesionStatic.when(SesionUsuario::estaActiva).thenReturn(true);

            Inventario inventario = new Inventario(usuarioMock);

            // 3. Ejecutamos el método
            Map<String, Integer> resultado = inventario.obtenerTareasPendientes();

            // 4. Verificamos que el mapa tenga las llaves esperadas
            assertNotNull(resultado, "El mapa de tareas no debe ser nulo");
            assertTrue(resultado.containsKey("alertasCriticas"));
            assertTrue(resultado.containsKey("lotesPorVencer"));
            assertTrue(resultado.containsKey("movimientosSinAuditar"));
        }
    }
}
