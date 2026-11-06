package org.example.carestock.service;

import org.example.carestock.session.SesionUsuario;
import java.util.Objects;

/** Valida administrador y farmacia activa. */
public final class PermisosExportacionReposicion {

    private PermisosExportacionReposicion() { }

    public static SesionUsuario.DatosUsuario exigirAdministrador() {
        if (!SesionUsuario.estaActiva()) {
            throw new SecurityException(
                    "Debe iniciar sesión para exportar reposición");
        }

        SesionUsuario.DatosUsuario sesion = SesionUsuario.actual();

        if (!sesion.esAdministrador()
                || sesion.idFarmacia() == null
                || sesion.idFarmacia() <= 0) {

            throw new SecurityException(
                    "Solo el administrador con farmacia activa puede exportar");
        }

        return sesion;
    }

    public static void comprobarMismaSesion(
            SesionUsuario.DatosUsuario original) {

        Objects.requireNonNull(original,
                "Falta la sesión original");

        SesionUsuario.DatosUsuario actual = exigirAdministrador();

        if (actual.id() != original.id()
                || !Objects.equals(
                        actual.idFarmacia(),
                        original.idFarmacia())) {

            throw new SecurityException(
                    "La sesión o farmacia cambió durante la exportación");
        }
    }
}
