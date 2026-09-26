# Documentación del mensaje "Error de conexión"

**Historia de Usuario:** HU.60
**Tarea:** #337
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 25 de septiembre de 2026

---

## 1. Objetivo

Documentar el mensaje de error de conexión del inicio de sesión, su causa y recomendación.

---

## 2. Mensaje

No fue posible iniciar sesión. Intente nuevamente.

---

## 3. Causa

El mensaje se muestra cuando ocurre un error de conexión con la base de datos PostgreSQL (NeonDB). Las causas pueden ser:

| # | Causa |
|---|-------|
| 1 | No hay conexión a internet |
| 2 | El servidor de NeonDB no está disponible |
| 3 | Las credenciales de la base de datos son incorrectas |
| 4 | La URL de conexión JDBC está mal configurada |
| 5 | El driver de PostgreSQL no está en el classpath |

---

## 4. Recomendación

- Verificar la conexión a internet.
- Verificar que el servidor de NeonDB esté activo.
- Verificar las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD`.
- Si el problema persiste, contactar al Administrador del sistema.

---

## 5. Archivos involucrados

| Archivo | Función |
|---------|---------|
| `view/LoginFX.java` | Captura `SQLException` y muestra el mensaje |
| `config/DatabaseConfig.java` | Gestiona la conexión a PostgreSQL |

---

**Fin del documento.**
