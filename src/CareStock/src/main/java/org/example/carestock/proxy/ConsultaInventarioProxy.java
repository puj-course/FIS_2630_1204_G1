
package org.example.carestock.proxy;

import org.example.carestock.DataTransferObject.ProductoInventario;
import org.example.carestock.session.SesionUsuario;

import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.IntPredicate;
import java.util.function.LongSupplier;

/**
 * Patron GoF Proxy: Caching Proxy para inventario unificado.
 *
 * Almacena lecturas por farmacia, controla TTL y memoria,
 * y evita consultas concurrentes redundantes.
 */
public final class ConsultaInventarioProxy
        implements ConsultaInventario {

    private static final int MAX_ENTRADAS = 16;
    private static final int MAX_FILAS_POR_ENTRADA = 10_000;

    private record Entrada(
            List<ProductoInventario> datos,
            long creadaNanos
    ) {}

    private static final class Carga {
        final CompletableFuture<List<ProductoInventario>> futuro =
                new CompletableFuture<>();
    }

    private static final class CargaInvalidadaException
            extends RuntimeException {

        private CargaInvalidadaException() {
            super("Consulta invalidada mientras se cargaba");
        }
    }

    private final ConsultaInventario real;
    private final long ttlNanos;
    private final LongSupplier reloj;
    private final IntPredicate accesoPermitido;

    private final Object monitor = new Object();

    // Cache LRU: una entrada independiente por farmacia.
    private final LinkedHashMap<Integer, Entrada> cache =
            new LinkedHashMap<>(16, 0.75f, true);

    // Consultas actualmente ejecutandose por farmacia.
    private final Map<Integer, Carga> pendientes =
            new HashMap<>();

    private final AtomicLong aciertos = new AtomicLong();
    private final AtomicLong consultas = new AtomicLong();

    /**
     * Constructor utilizado por CareStock.
     * La autorizacion se obtiene de la sesion activa.
     */
    public ConsultaInventarioProxy(
            ConsultaInventario real,
            Duration ttl
    ) {
        this(real, ttl, System::nanoTime,
                ConsultaInventarioProxy::autorizarSesion);
    }

    /**
     * Constructor de paquete para pruebas unitarias.
     * Permite simular el reloj y la autorizacion.
     */
    ConsultaInventarioProxy(
            ConsultaInventario real,
            Duration ttl,
            LongSupplier reloj,
            IntPredicate accesoPermitido
    ) {
        this.real = Objects.requireNonNull(real);
        this.reloj = Objects.requireNonNull(reloj);
        this.accesoPermitido =
                Objects.requireNonNull(accesoPermitido);

        this.ttlNanos =
                Objects.requireNonNull(ttl).toNanos();

        if (ttlNanos <= 0) {
            throw new IllegalArgumentException(
                    "El TTL debe ser mayor que cero"
            );
        }
    }

    private static boolean autorizarSesion(int farmacia) {
        try {
            Integer asignada =
                    SesionUsuario.actual().idFarmacia();

            return asignada != null && asignada == farmacia;

        } catch (IllegalStateException ex) {
            return false;
        }
    }

    private void verificarAcceso(int farmacia) {
        if (farmacia <= 0) {
            throw new IllegalArgumentException(
                    "Id de farmacia invalido"
            );
        }

        if (!accesoPermitido.test(farmacia)) {
            throw new SecurityException(
                    "Sin acceso a la farmacia solicitada"
            );
        }
    }

    @Override
    public List<ProductoInventario> listarPorFarmacia(
            int idFarmacia
    ) throws SQLException {

        for (;;) {
            verificarAcceso(idFarmacia);

            Carga carga;
            boolean responsable;

            synchronized (monitor) {
                limpiarVencidas(reloj.getAsLong());

                Entrada entrada = cache.get(idFarmacia);

                // CACHE HIT
                if (entrada != null) {
                    aciertos.incrementAndGet();
                    return entrada.datos();
                }

                // CACHE MISS
                carga = pendientes.get(idFarmacia);
                responsable = carga == null;

                if (responsable) {
                    carga = new Carga();
                    pendientes.put(idFarmacia, carga);
                }
            }

            // Solo un hilo realiza la consulta real
            // para esta farmacia.
            if (responsable) {
                cargarDesdeDao(idFarmacia, carga);
            }

            try {
                List<ProductoInventario> datos =
                        carga.futuro.join();

                verificarAcceso(idFarmacia);

                return datos;

            } catch (CompletionException ex) {
                Throwable causa = ex.getCause();

                if (causa instanceof CargaInvalidadaException) {
                    // La consulta se invalido mientras cargaba.
                    // Repetir para obtener informacion actual.
                    continue;
                }

                if (causa instanceof SQLException sql) {
                    throw sql;
                }

                if (causa instanceof RuntimeException runtime) {
                    throw runtime;
                }

                if (causa instanceof Error error) {
                    throw error;
                }

                throw new SQLException(
                        "Fallo inesperado consultando inventario",
                        causa
                );
            }
        }
    }

    private void cargarDesdeDao(
            int farmacia,
            Carga carga
    ) {
        try {
            consultas.incrementAndGet();

            // Consulta real a PostgreSQL mediante DAO.
            List<ProductoInventario> datos =
                    List.copyOf(
                            real.listarPorFarmacia(farmacia)
                    );

            synchronized (monitor) {

                // No almacenar una consulta invalidada.
                if (pendientes.get(farmacia) != carga) {
                    carga.futuro.completeExceptionally(
                            new CargaInvalidadaException()
                    );
                    return;
                }

                pendientes.remove(farmacia);

                // Evita cachear resultados demasiado grandes.
                if (datos.size() <= MAX_FILAS_POR_ENTRADA) {
                    cache.put(
                            farmacia,
                            new Entrada(
                                    datos,
                                    reloj.getAsLong()
                            )
                    );

                    reducirTamano();
                }

                carga.futuro.complete(datos);
            }

        } catch (Throwable fallo) {

            synchronized (monitor) {
                if (pendientes.get(farmacia) == carga) {
                    pendientes.remove(farmacia);
                }

                // Los errores no se almacenan en cache.
                carga.futuro.completeExceptionally(fallo);
            }
        }
    }

    private void limpiarVencidas(long ahora) {
        Iterator<Entrada> iterador =
                cache.values().iterator();

        while (iterador.hasNext()) {
            Entrada entrada = iterador.next();

            if (ahora - entrada.creadaNanos()
                    >= ttlNanos) {
                iterador.remove();
            }
        }
    }

    private void reducirTamano() {
        while (cache.size() > MAX_ENTRADAS) {
            Integer masAntigua =
                    cache.keySet().iterator().next();

            cache.remove(masAntigua);
        }
    }

    /**
     * Invalida exclusivamente la farmacia indicada.
     */
    public void invalidarFarmacia(int idFarmacia) {
        if (idFarmacia <= 0) {
            throw new IllegalArgumentException(
                    "Id de farmacia invalido"
            );
        }

        synchronized (monitor) {
            cache.remove(idFarmacia);

            Carga carga =
                    pendientes.remove(idFarmacia);

            if (carga != null) {
                carga.futuro.completeExceptionally(
                        new CargaInvalidadaException()
                );
            }
        }
    }

    /**
     * Invalida todas las farmacias.
     * Utilizado al iniciar o cerrar sesion.
     */
    public void invalidar() {
        synchronized (monitor) {
            cache.clear();

            for (Carga carga : pendientes.values()) {
                carga.futuro.completeExceptionally(
                        new CargaInvalidadaException()
                );
            }

            pendientes.clear();
        }
    }

    public long aciertos() {
        return aciertos.get();
    }

    public long consultasReales() {
        return consultas.get();
    }
}
