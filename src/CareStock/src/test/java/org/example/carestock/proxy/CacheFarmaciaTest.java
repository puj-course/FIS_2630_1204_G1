package org.example.carestock.proxy;

import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.junit.jupiter.api.Assertions.*;

class CacheFarmaciaTest {

    private CacheFarmacia<String> cache() {
        return new CacheFarmacia<>(30, 2, 100, String::length);
    }

    @Test
    void rechazaConfiguracionInvalida() {
        assertThrows(IllegalArgumentException.class,
                () -> new CacheFarmacia<>(0, 2, 100, String::length));
        assertThrows(IllegalArgumentException.class,
                () -> new CacheFarmacia<>(30, 0, 100, String::length));
        assertThrows(IllegalArgumentException.class,
                () -> new CacheFarmacia<>(30, 2, 0, String::length));
        assertThrows(NullPointerException.class,
                () -> new CacheFarmacia<String>(30, 2, 100, null));
    }

    @Test
    void reutilizaResultadoDeLaMismaFarmacia() throws Exception {
        var c = cache();
        AtomicInteger consultas = new AtomicInteger();
        Callable<String> consulta = () -> "dato-" + consultas.incrementAndGet();

        assertEquals("dato-1", c.obtener(1, consulta, () -> true));
        assertEquals("dato-1", c.obtener(1, consulta, () -> true));
        assertEquals(1, consultas.get());
        assertEquals("hits=1, cargasDAO=1", c.estadisticas());
    }

    @Test
    void separaResultadosPorFarmacia() throws Exception {
        var c = cache();
        assertEquals("Farmacia 1", c.obtener(1, () -> "Farmacia 1", () -> true));
        assertEquals("Farmacia 2", c.obtener(2, () -> "Farmacia 2", () -> true));
        assertEquals("Farmacia 1", c.obtener(1, () -> "Otro", () -> true));
    }

    @Test
    void rechazaFarmaciaInvalidaOSinPermisos() {
        var c = cache();
        assertThrows(IllegalArgumentException.class,
                () -> c.obtener(0, () -> "dato", () -> true));
        assertThrows(SecurityException.class,
                () -> c.obtener(1, () -> "dato", () -> false));
    }

    @Test
    void revocacionImpideAccederAlDatoCacheado() throws Exception {
        var c = cache();
        c.obtener(1, () -> "protegido", () -> true);
        assertThrows(SecurityException.class,
                () -> c.obtener(1, () -> "nuevo", () -> false));
    }

    @Test
    void revocacionDuranteCargaDescartaElResultado() {
        var c = cache();
        AtomicInteger verificaciones = new AtomicInteger();
        assertThrows(SecurityException.class,
                () -> c.obtener(1, () -> "dato",
                        () -> verificaciones.incrementAndGet() == 1));
        assertThrows(SecurityException.class,
                () -> c.obtener(1, () -> "otro", () -> false));
    }

    @Test
    void invalidaSolamenteFarmaciaSolicitada() throws Exception {
        var c = cache();
        AtomicInteger cargas = new AtomicInteger();

        c.obtener(1, () -> "A" + cargas.incrementAndGet(), () -> true);
        c.obtener(2, () -> "B" + cargas.incrementAndGet(), () -> true);
        c.invalidarFarmacia(1);

        assertEquals("B2", c.obtener(2, () -> "ERROR", () -> true));
        assertEquals("A3", c.obtener(1,
                () -> "A" + cargas.incrementAndGet(), () -> true));
        assertEquals(3, cargas.get());
    }

    @Test
    void invalidarTodoObligaARecargar() throws Exception {
        var c = cache();
        AtomicInteger contador = new AtomicInteger();
        Callable<String> consulta = () -> "v" + contador.incrementAndGet();

        assertEquals("v1", c.obtener(1, consulta, () -> true));
        c.invalidarTodo();
        assertEquals("v2", c.obtener(1, consulta, () -> true));
    }

    @Test
    void eliminaLaFarmaciaMenosRecientementeUsada() throws Exception {
        var c = cache();
        AtomicInteger cargas = new AtomicInteger();
        Callable<String> consulta = () -> "v" + cargas.incrementAndGet();

        c.obtener(1, consulta, () -> true);
        c.obtener(2, consulta, () -> true);
        c.obtener(1, consulta, () -> true);
        c.obtener(3, consulta, () -> true);
        c.obtener(2, consulta, () -> true);

        assertEquals(4, cargas.get());
    }

    @Test
    void noGuardaResultadosQueSuperanElLimite() throws Exception {
        var c = new CacheFarmacia<>(30, 2, 3, String::length);
        AtomicInteger consultas = new AtomicInteger();
        Callable<String> consulta = () -> "largo" + consultas.incrementAndGet();

        assertNotEquals(c.obtener(1, consulta, () -> true),
                c.obtener(1, consulta, () -> true));
        assertEquals(2, consultas.get());
    }

    @Test
    void fallosDeConsultaNoQuedanCacheados() throws Exception {
        var c = cache();
        AtomicInteger intentos = new AtomicInteger();

        Callable<String> consulta = () -> {
            if (intentos.incrementAndGet() == 1)
                throw new SQLException("Fallo simulado");
            return "recuperado";
        };

        assertThrows(SQLException.class,
                () -> c.obtener(1, consulta, () -> true));
        assertEquals("recuperado", c.obtener(1, consulta, () -> true));
        assertEquals(2, intentos.get());
    }

    @Test
    void resultadoExpiradoSeConsultaNuevamente() throws Exception {
        var c = new CacheFarmacia<>(1, 2, 100, String::length);
        AtomicInteger cargas = new AtomicInteger();
        Callable<String> consulta = () -> "v" + cargas.incrementAndGet();

        assertEquals("v1", c.obtener(1, consulta, () -> true));
        Thread.sleep(1100);
        assertEquals("v2", c.obtener(1, consulta, () -> true));
    }

    @Test
    void consultasConcurrentesCompartenCarga() throws Exception {
        var c = cache();
        AtomicInteger consultas = new AtomicInteger();
        CountDownLatch iniciada = new CountDownLatch(1);
        CountDownLatch liberar = new CountDownLatch(1);
        CountDownLatch segundoListo = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<String> consulta = () -> {
            consultas.incrementAndGet();
            iniciada.countDown();
            if (!liberar.await(5, TimeUnit.SECONDS))
                throw new SQLException("Tiempo de espera agotado");
            return "compartido";
        };

        try {
            Future<String> primero =
                    executor.submit(() -> c.obtener(1, consulta, () -> true));

            assertTrue(iniciada.await(3, TimeUnit.SECONDS));

            Future<String> segundo = executor.submit(() -> {
                segundoListo.countDown();
                return c.obtener(1, consulta, () -> true);
            });

            assertTrue(segundoListo.await(3, TimeUnit.SECONDS));
            liberar.countDown();

            assertEquals("compartido", primero.get(5, TimeUnit.SECONDS));
            assertEquals("compartido", segundo.get(5, TimeUnit.SECONDS));
            assertTrue(consultas.get() >= 1);
            assertTrue(consultas.get() <= 2);
        } finally {
            liberar.countDown();
            executor.shutdownNow();
        }
    }
}
