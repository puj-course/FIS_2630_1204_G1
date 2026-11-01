# HU.58 - Cambio seguro de contraseña

## Historia

**HU.58:** Cambio de contraseña desde el perfil del usuario.

Issue principal: **#308**

Subissues:

- #338 - Diseño de pantalla.
- #339 - Validación de contraseña actual.
- #340 - Confirmación de nueva contraseña.
- #341 - Hash seguro de nueva contraseña.
- #342 - Documentación del flujo.

---

## Flujo funcional

El usuario autenticado selecciona **Cambiar contraseña** desde CareStock.

Debe ingresar:

1. Contraseña actual.
2. Nueva contraseña.
3. Confirmación de la nueva contraseña.

El sistema valida los datos, compara la contraseña actual mediante BCrypt,
genera un nuevo hash BCrypt y actualiza exclusivamente `password_hash`.

La contraseña en texto plano nunca se almacena en PostgreSQL.

---

# 4. Registro de Diseños de Software

## 4.1 Diseño Basado en Modelos (MBD)

HU.58 se describe mediante modelos estructurales y de comportamiento que
representan los componentes participantes y el flujo de cambio de
contraseña.

---

## 4.2 Descripción de Diseño Estructural

```mermaid
classDiagram

    class CambioPasswordView
    class PasswordChangeService
    class UsuarioDAO
    class UserSession
    class DatabaseConfig

    CambioPasswordView --> PasswordChangeService
    CambioPasswordView --> UserSession

    PasswordChangeService --> UsuarioDAO

    UsuarioDAO --> DatabaseConfig
