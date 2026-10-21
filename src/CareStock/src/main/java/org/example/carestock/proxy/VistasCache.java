package org.example.carestock.proxy;

import org.example.carestock.dto.DatosCatalogo;
import org.example.carestock.dto.DatosLotes;

import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

public final class VistasCache {

    private static final int TTL = 45;

    private static final CacheFarmacia<DatosCatalogo>
            CATALOGO = new CacheFarmacia<>(
                    TTL, 8, 10000,
                    d -> d.productos().size());

    private static final CacheFarmacia<DatosLotes>
            LOTES = new CacheFarmacia<>(
                    TTL, 8, 10000,
                    d -> d.lotes().size());

    private VistasCache() {}

    public static DatosCatalogo catalogo(
            int farmacia,
            Callable<DatosCatalogo> consulta,
            BooleanSupplier autorizado) throws Exception {

        return CATALOGO.obtener(
                farmacia, consulta, autorizado);
    }

    public static DatosLotes lotes(
            int farmacia,
            Callable<DatosLotes> consulta,
            BooleanSupplier autorizado) throws Exception {

        return LOTES.obtener(
                farmacia, consulta, autorizado);
    }

    public static void invalidarFarmacia(int farmacia) {
        CATALOGO.invalidarFarmacia(farmacia);
        LOTES.invalidarFarmacia(farmacia);
    }

    public static void invalidarTodo() {
        CATALOGO.invalidarTodo();
        LOTES.invalidarTodo();
    }

    public static long ttlCatalogoNanos() {
        return java.util.concurrent.TimeUnit.SECONDS
                .toNanos(TTL);
    }

    public static String metricas() {
        return "Catalogo " + CATALOGO.estadisticas()
                + "; Lotes " + LOTES.estadisticas();
    }
}
