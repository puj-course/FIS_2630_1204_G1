package com.carestock.session;

import com.carestock.model.Usuario;

/**
 * Contexto global de sesión de CareStock.
 *
 * Implementado como Singleton para mantener en memoria
 * únicamente los datos necesarios del usuario autenticado.
 */
public final class UserSession {

    private static final UserSession INSTANCE = new UserSession();

    private CurrentUser currentUser;

    /**
     * Constructor privado para impedir la creación
     * de múltiples instancias.
     */
    private UserSession() {
    }

    /**
     * Devuelve la única instancia de UserSession.
     */
    public static UserSession getInstance() {
        return INSTANCE;
    }

    /**
     * Inicia la sesión con los datos del usuario autenticado.
     *
     * No se almacena el password_hash.
     */
    public void setCurrentUser(Usuario usuario) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario autenticado no puede ser nulo."
            );
        }

        this.currentUser = new CurrentUser(
                usuario.getIdUsuario(),
                usuario.getNombreCompleto(),
                usuario.getEmail(),
                usuario.getNombreRol()
        );
    }

    /**
     * Devuelve los datos del usuario autenticado.
     */
    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    /**
     * Indica si actualmente existe una sesión iniciada.
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Invalida completamente el contexto de la sesión activa.
     *
     * Al eliminar la referencia a CurrentUser dejan de estar
     * disponibles desde UserSession los datos asociados al
     * usuario autenticado, incluyendo identificador, nombre,
     * correo electrónico y rol.
     *
     * UserSession no almacena contraseñas, hashes, tokens ni
     * permisos independientes del usuario.
     */
    public void clearSession() {

        currentUser = null;
    }

    /**
     * Representación segura del usuario almacenado en sesión.
     *
     * No contiene contraseña ni password_hash.
     */
    public static final class CurrentUser {

        private final int id;
        private final String nombre;
        private final String email;
        private final String rol;

        private CurrentUser(
                int id,
                String nombre,
                String email,
                String rol
        ) {
            this.id = id;
            this.nombre = nombre;
            this.email = email;
            this.rol = rol;
        }

        public int getId() {
            return id;
        }

        public String getNombre() {
            return nombre;
        }

        public String getEmail() {
            return email;
        }

        public String getRol() {
            return rol;
        }
    }
}
