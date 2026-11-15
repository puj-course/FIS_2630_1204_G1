package org.example.carestock.utils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Objects;

/** Fecha para vencimientos y operaciones de la farmacia en Colombia. */
public final class FechaNegocio {
    public static final ZoneId ZONA = ZoneId.of("America/Bogota");

    private FechaNegocio() { }

    public static LocalDate hoy() {
        return hoy(Clock.system(ZONA));
    }

    public static LocalDate hoy(Clock reloj) {
        return LocalDate.now(Objects.requireNonNull(reloj, "reloj")
                .withZone(ZONA));
    }
}
