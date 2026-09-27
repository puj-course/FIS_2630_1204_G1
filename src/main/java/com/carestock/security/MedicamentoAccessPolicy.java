package com.carestock.security;

import com.carestock.exception.AccesoDenegadoException;

/**
 * Centraliza las reglas de autorización relacionadas
 * con la gestión del catálogo de medicamentos.
 */
public final class MedicamentoAccessPolicy {

    private MedicamentoAccessPolicy() {
    }

    public static boolean puedeRegistrarMedicamento(
            String rol
    ) {

        if (rol == null) {
            return false;
        }

        return "ADMINISTRADOR".equalsIgnoreCase(rol)
                || "FARMACEUTICO".equalsIgnoreCase(rol);
    }

    public static void requireRegistrarMedicamento(
            String rol
    ) {

        if (!puedeRegistrarMedicamento(rol)) {

            throw new AccesoDenegadoException(
                    "El usuario autenticado no tiene permisos "
                    + "para registrar medicamentos."
            );
        }
    }
}
