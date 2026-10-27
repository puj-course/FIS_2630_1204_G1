
package org.example.carestock.proxy;

import org.example.carestock.dao.ConsultaInventarioDAO;

import java.time.Duration;

public final class InventarioConsultas {

    private static final String VARIABLE_TTL =
            "CARESTOCK_INVENTARIO_CACHE_TTL_SECONDS";

    private static final ConsultaInventarioProxy PROXY =
            new ConsultaInventarioProxy(
                    new ConsultaInventarioDAO(),
                    obtenerTtl()
            );

    private InventarioConsultas() {}

    private static Duration obtenerTtl() {
        String valor = System.getenv(VARIABLE_TTL);

        // Valor por defecto: 30 segundos.
        if (valor == null || valor.isBlank()) {
            return Duration.ofSeconds(30);
        }

        try {
            long segundos = Long.parseLong(valor.trim());

            if (segundos < 1 || segundos > 3600) {
                throw new IllegalArgumentException(
                        "TTL fuera del rango permitido: 1-3600 segundos"
                );
            }

            return Duration.ofSeconds(segundos);

        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                    "TTL de inventario debe ser un numero entero",
                    ex
            );
        }
    }

    public static ConsultaInventarioProxy compartida() {
        return PROXY;
    }

    public static void invalidarFarmacia(int idFarmacia) {
        PROXY.invalidarFarmacia(idFarmacia);
    }

    public static void invalidar() {
        PROXY.invalidar();
    }
}
