package org.example.carestock.proxy;

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import static org.junit.jupiter.api.Assertions.*;

class ConsultaInventarioProxyTest {

    private static List<ProductoInventario> resultado(int id) {
        return List.of(new ProductoInventario(
                id, "Aseo", "A-" + id,
                "Producto " + id, "Unidad", 5, "ACTIVO"
        ));
    }

    @Test
    void reutilizaResultadosSinRepetirConsultaDao() throws Exception {
        AtomicInteger consultas = new AtomicInteger();

        ConsultaInventarioProxy proxy = new ConsultaInventarioProxy(
                f -> {
                    consultas.incrementAndGet();
                    return resultado(f);
                },
                Duration.ofSeconds(30),
                System::nanoTime,
                f -> f == 1
        );

        assertEquals(resultado(1), proxy.listarPorFarmacia(1));
        assertEquals(resultado(1), proxy.listarPorFarmacia(1));
        assertEquals(1, consultas.get());
        assertEquals(1, proxy.aciertos());
        assertEquals(1, proxy.consultasReales());

        assertThrows(UnsupportedOperationException.class,
                () -> proxy.listarPorFarmacia(1).clear());
    }

    @Test
    void ttlYListaVaciaSonValidos() throws Exception {
        AtomicLong reloj = new AtomicLong();
        AtomicInteger consultas = new AtomicInteger();

        ConsultaInventarioProxy proxy = new ConsultaInventarioProxy(
                f -> {
                    consultas.incrementAndGet();
                    return List.of();
                },
                Duration.ofSeconds(5),
                reloj::get,
                f -> true
        );

        assertTrue(proxy.listarPorFarmacia(1).isEmpty());

        reloj.addAndGet(Duration.ofSeconds(4).toNanos());
        assertTrue(proxy.listarPorFarmacia(1).isEmpty());
        assertEquals(1, consultas.get());

        reloj.addAndGet(Duration.ofSeconds(2).toNanos());
        assertTrue(proxy.listarPorFarmacia(1).isEmpty());
        assertEquals(2, consultas.get());
    }

    @Test
    void deniegaOtraFarmaciaAunqueExistaCache() throws Exception {
        AtomicBoolean autorizado = new AtomicBoolean(true);

        ConsultaInventarioProxy proxy = new ConsultaInventarioProxy(
                ConsultaInventarioProxyTest::resultado,
                Duration.ofSeconds(30),
                System::nanoTime,
                f -> f == 1 && autorizado.get()
        );

        proxy.listarPorFarmacia(1);
        autorizado.set(false);

        assertThrows(SecurityException.class,
                () -> proxy.listarPorFarmacia(1));

        assertThrows(SecurityException.class,
                () -> proxy.listarPorFarmacia(2));
    }

    @Test
    void soloInvalidaLaFarmaciaAfectada() throws Exception {
        AtomicInteger consultas = new AtomicInteger();

        ConsultaInventarioProxy proxy = new ConsultaInventarioProxy(
                f -> {
                    consultas.incrementAndGet();
                    return resultado(f);
                },
                Duration.ofSeconds(30),
                System::nanoTime,
                f -> true
        );

        proxy.listarPorFarmacia(1);
        proxy.listarPorFarmacia(2);

        proxy.invalidarFarmacia(1);

        proxy.listarPorFarmacia(2);
        proxy.listarPorFarmacia(1);

        assertEquals(3, consultas.get());
    }

    @Test
    void erroresNoSeAlmacenanComoDatosVacios() throws Exception {
        AtomicInteger intentos = new AtomicInteger();

        ConsultaInventarioProxy proxy = new ConsultaInventarioProxy(
                f -> {
                    if (intentos.incrementAndGet() == 1) {
                        throw new SQLException("Sin conexion");
                    }
                    return resultado(f);
                },
                Duration.ofSeconds(30),
                System::nanoTime,
                f -> true
        );

        assertThrows(SQLException.class,
                () -> proxy.listarPorFarmacia(1));

        assertEquals(resultado(1), proxy.listarPorFarmacia(1));
        assertEquals(2, intentos.get());
    }

    @Test
    void invalidacionDeSesionDescartaCargaPendiente()
            throws Exception {

        CountDownLatch inicio = new CountDownLatch(1);
        CountDownLatch liberar = new CountDownLatch(1);
        AtomicInteger consultas = new AtomicInteger();

        ConsultaInventarioProxy proxy = new ConsultaInventarioProxy(
                f -> {
                    consultas.incrementAndGet();
                    inicio.countDown();
                    try {
                        if (!liberar.await(3, TimeUnit.SECONDS)) {
                            throw new SQLException("Espera agotada");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interrumpido", e);
                    }
                    return resultado(f);
                },
                Duration.ofSeconds(30),
                System::nanoTime,
                f -> true
        );

        ExecutorService hilos = Executors.newSingleThreadExecutor();

        try {
            Future<List<ProductoInventario>> futuro =
                    hilos.submit(() -> proxy.listarPorFarmacia(1));

            assertTrue(inicio.await(2, TimeUnit.SECONDS));

            proxy.invalidar();
            liberar.countDown();

            ExecutionException error = assertThrows(
                    ExecutionException.class,
                    () -> futuro.get(3, TimeUnit.SECONDS)
            );

            assertInstanceOf(
                    SecurityException.class,
                    error.getCause()
            );

            assertEquals(1, consultas.get());

        } finally {
            liberar.countDown();
            hilos.shutdownNow();
        }
    }

    @Test
    void consultasConcurrentesCompartenLaMismaCarga()
            throws Exception {

        CountDownLatch inicio = new CountDownLatch(1);
        CountDownLatch liberar = new CountDownLatch(1);
        AtomicInteger consultas = new AtomicInteger();

        ConsultaInventarioProxy proxy = new ConsultaInventarioProxy(
                f -> {
                    consultas.incrementAndGet();
                    inicio.countDown();

                    try {
                        if (!liberar.await(3, TimeUnit.SECONDS)) {
                            throw new SQLException("Espera agotada");
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interrumpido", e);
                    }

                    return resultado(f);
                },
                Duration.ofSeconds(30),
                System::nanoTime,
                f -> true
        );

        ExecutorService hilos = Executors.newFixedThreadPool(2);

        try {
            Future<List<ProductoInventario>> primero =
                    hilos.submit(() -> proxy.listarPorFarmacia(1));

            assertTrue(inicio.await(2, TimeUnit.SECONDS));

            Future<List<ProductoInventario>> segundo =
                    hilos.submit(() -> proxy.listarPorFarmacia(1));

            liberar.countDown();

            assertEquals(resultado(1),
                    primero.get(3, TimeUnit.SECONDS));

            assertEquals(resultado(1),
                    segundo.get(3, TimeUnit.SECONDS));

            assertEquals(1, consultas.get());

        } finally {
            liberar.countDown();
            hilos.shutdownNow();
        }
    }
}
