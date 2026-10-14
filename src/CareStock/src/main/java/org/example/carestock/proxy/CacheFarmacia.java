package org.example.carestock.proxy;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.ToIntFunction;

/**
 * Cache generica por farmacia.
 * Implementa TTL, limite LRU y deduplicacion
 * de consultas concurrentes.
 */
public final class CacheFarmacia<T> {

    private record Entrada<T>(T valor, long creada) {}

    private static final class Invalidada
            extends RuntimeException {}

    private final Map<Integer, CompletableFuture<T>> pendientes =
            new HashMap<>();

    private final LinkedHashMap<Integer, Entrada<T>> cache =
            new LinkedHashMap<>(16, 0.75f, true);

    private final Object monitor = new Object();

    private final long ttlNanos;
    private final int maxFarmacias;
    private final int maxFilas;
    private final ToIntFunction<T> peso;

    private final AtomicLong aciertos = new AtomicLong();
    private final AtomicLong cargas = new AtomicLong();

    public CacheFarmacia(
            int ttlSegundos,
            int maxFarmacias,
            int maxFilas,
            ToIntFunction<T> peso) {

        if (ttlSegundos <= 0 || maxFarmacias <= 0
                || maxFilas <= 0) {
            throw new IllegalArgumentException(
                    "Configuracion de cache invalida");
        }

        this.ttlNanos =
                TimeUnit.SECONDS.toNanos(ttlSegundos);
        this.maxFarmacias = maxFarmacias;
        this.maxFilas = maxFilas;
        this.peso = Objects.requireNonNull(peso);
    }

    public T obtener(
            int farmacia,
            Callable<T> consulta,
            BooleanSupplier autorizado) throws Exception {

        if (farmacia <= 0) {
            throw new IllegalArgumentException(
                    "Farmacia invalida");
        }

        for (;;) {
            if (!autorizado.getAsBoolean()) {
                throw new SecurityException(
                        "Sesion no autorizada");
            }

            CompletableFuture<T> futuro;
            boolean responsable;

            synchronized (monitor) {
                long ahora = System.nanoTime();

                cache.entrySet().removeIf(e ->
                        ahora - e.getValue().creada()
                                >= ttlNanos);

                Entrada<T> entrada = cache.get(farmacia);

                if (entrada != null) {
                    aciertos.incrementAndGet();
                    return entrada.valor();
                }

                futuro = pendientes.get(farmacia);
                responsable = futuro == null;

                if (responsable) {
                    futuro = new CompletableFuture<>();
                    pendientes.put(farmacia, futuro);
                }
            }

            if (responsable) {
                try {
                    cargas.incrementAndGet();
                    T datos = consulta.call();

                    if (!autorizado.getAsBoolean()) {
                        throw new SecurityException(
                                "Sesion no autorizada");
                    }

                    synchronized (monitor) {
                        if (pendientes.get(farmacia)
                                != futuro) {
                            futuro.completeExceptionally(
                                    new Invalidada());
                        } else {
                            pendientes.remove(farmacia);

                            if (peso.applyAsInt(datos)
                                    <= maxFilas) {
                                cache.put(
                                        farmacia,
                                        new Entrada<>(
                                                datos,
                                                System.nanoTime()));

                                while (cache.size()
                                        > maxFarmacias) {
                                    Integer primera =
                                            cache.keySet()
                                                    .iterator()
                                                    .next();
                                    cache.remove(primera);
                                }
                            }

                            futuro.complete(datos);
                        }
                    }
                } catch (Throwable e) {
                    synchronized (monitor) {
                        if (pendientes.get(farmacia)
                                == futuro) {
                            pendientes.remove(farmacia);
                        }
                        futuro.completeExceptionally(e);
                    }
                }
            }

            try {
                T datos = futuro.get();

                if (!autorizado.getAsBoolean()) {
                    throw new SecurityException(
                            "Sesion no autorizada");
                }

                return datos;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw e;
            } catch (ExecutionException e) {
                Throwable causa = e.getCause();

                if (causa instanceof Invalidada) {
                    continue;
                }
                if (causa instanceof Exception ex) {
                    throw ex;
                }
                if (causa instanceof Error error) {
                    throw error;
                }
                throw new IllegalStateException(causa);
            }
        }
    }

    public void invalidarFarmacia(int farmacia) {
        synchronized (monitor) {
            cache.remove(farmacia);

            CompletableFuture<T> futuro =
                    pendientes.remove(farmacia);

            if (futuro != null) {
                futuro.completeExceptionally(
                        new Invalidada());
            }
        }
    }

    public void invalidarTodo() {
        synchronized (monitor) {
            cache.clear();

            for (CompletableFuture<T> futuro
                    : pendientes.values()) {
                futuro.completeExceptionally(
                        new Invalidada());
            }

            pendientes.clear();
        }
    }

    public String estadisticas() {
        return "hits=" + aciertos.get()
                + ", cargasDAO=" + cargas.get();
    }
}
