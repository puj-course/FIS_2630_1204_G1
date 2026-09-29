package com.carestock.session;

import com.carestock.model.Usuario;

public final class UserSession {

    private static final UserSession INSTANCE =
            new UserSession();

    private CurrentUser currentUser;

    private UserSession() {
    }

    public static UserSession getInstance() {
        return INSTANCE;
    }

    public void setCurrentUser(
            Usuario usuario
    ) {

        if (usuario == null) {
            throw new IllegalArgumentException(
                    "El usuario autenticado no puede ser nulo."
            );
        }

        this.currentUser =
                new CurrentUser(
                        usuario.getIdUsuario(),
                        usuario.getNombreCompleto(),
                        usuario.getEmail(),
                        usuario.getNombreRol(),
                        usuario.getIdFarmacia()
                );
    }

    public CurrentUser getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void clearSession() {
        currentUser = null;
    }

    public static final class CurrentUser {

        private final int id;
        private final String nombre;
        private final String email;
        private final String rol;
        private final Integer idFarmacia;

        private CurrentUser(
                int id,
                String nombre,
                String email,
                String rol,
                Integer idFarmacia
        ) {
            this.id = id;
            this.nombre = nombre;
            this.email = email;
            this.rol = rol;
            this.idFarmacia = idFarmacia;
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

        public Integer getIdFarmacia() {
            return idFarmacia;
        }

        public boolean tieneFarmaciaAsignada() {
            return idFarmacia != null
                    && idFarmacia > 0;
        }

        public boolean esSuperAdmin() {
            return "SUPER_ADMIN".equalsIgnoreCase(
                    rol
            );
        }

        public boolean esAdministrador() {
            return "ADMINISTRADOR".equalsIgnoreCase(
                    rol
            );
        }

        public boolean esFarmaceutico() {
            return "FARMACEUTICO".equalsIgnoreCase(
                    rol
            );
        }
    }
}
