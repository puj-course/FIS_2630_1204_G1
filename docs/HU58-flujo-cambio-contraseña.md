# Flujo de cambio de contraseña en CareStock

**Historia de Usuario:** HU.58
**Tarea:** #342
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 25 de septiembre de 2026

---

## 1. Objetivo

Documentar el flujo completo de cambio de contraseña en CareStock, desde 
la interfaz JavaFX hasta el almacenamiento cifrado en PostgreSQL.

---

## 2. Descripción del flujo

El flujo de cambio de contraseña permite al usuario autenticado actualizar 
su contraseña desde el perfil, ingresando la contraseña actual, la nueva y 
la confirmación. El sistema valida los datos, verifica la contraseña 
actual contra la base de datos, cifra la nueva contraseña con BCrypt y la 
almacena en PostgreSQL.

---

## 3. Pasos del flujo

| Paso | Acción | Componente |
|------|--------|------------|
| 1 | El usuario hace clic en "Cambiar contraseña" en el dashboard | 
`MainDashboardFX.java` |
| 2 | Se abre la pantalla de cambio de contraseña | 
`CambioPasswordView.java` |
| 3 | El usuario ingresa: contraseña actual, nueva y confirmar | 
`CambioPasswordView.java` |
| 4 | El sistema valida que todos los campos estén completos | 
`CambioPasswordView.java` |
| 5 | El sistema valida que las dos nuevas contraseñas coincidan | 
`CambioPasswordView.java` |
| 6 | El sistema verifica la contraseña actual contra la base de datos | 
`UsuarioDAO.cambiarPassword()` |
| 7 | El sistema cifra la nueva contraseña con BCrypt | `BCrypt.hashpw()` 
|
| 8 | El sistema actualiza la contraseña en PostgreSQL | 
`UsuarioDAO.cambiarPassword()` |
| 9 | El sistema muestra un mensaje de éxito al usuario | 
`CambioPasswordView.java` |

---

## 4. Validaciones implementadas

| # | Validación | ¿Dónde? |
|---|------------|---------|
| 1 | Todos los campos son obligatorios | `CambioPasswordView.java` |
| 2 | Las dos nuevas contraseñas deben coincidir | 
`CambioPasswordView.java` |
| 3 | La contraseña actual debe ser correcta | 
`UsuarioDAO.cambiarPassword()` |
| 4 | La nueva contraseña se cifra con BCrypt | 
`UsuarioDAO.cambiarPassword()` |

---

## 5. Componentes involucrados

| Capa | Componente |
|------|------------|
| Interfaz | `CambioPasswordView.java` |
| Acceso a datos | `UsuarioDAO.java` |
| Seguridad | `BCrypt` (jbcrypt) |
| Base de datos | Tabla `USUARIOS` en PostgreSQL |

---
