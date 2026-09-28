package com.carestock.security;

import com.carestock.exception.AccesoDenegadoException;

/**
 * Política de autorización para gestión del catálogo
 * y del inventario.
 *
 * FARMACEUTICO queda fuera del CRUD administrativo.
 */
public final class MedicamentoAccessPolicy {

    private MedicamentoAccessPolicy() {
    }


    public static boolean puedeRegistrarMedicamento(
            String rol
    ) {

        return puedeGestionarInventario(
                rol
        );
    }


    public static boolean puedeGestionarInventario(
            String rol
    ) {

        if (rol == null) {
            return false;
        }


        return "SUPER_ADMIN"
                .equalsIgnoreCase(
                        rol
                )
                || "ADMINISTRADOR"
                .equalsIgnoreCase(
                        rol
                );
    }


    public static void requireRegistrarMedicamento(
            String rol
    ) {

        requireGestionarInventario(
                rol
        );
    }


    public static void requireGestionarInventario(
            String rol
    ) {

        if (
                !puedeGestionarInventario(
                        rol
                )
        ) {

            throw new AccesoDenegadoException(
                    "El usuario autenticado no tiene permisos "
                    + "para gestionar medicamentos o lotes."
            );
        }
    }
}
