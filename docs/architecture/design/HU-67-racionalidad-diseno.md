# HU-67 - Racionalidad del Diseño

## Filtrado del inventario por farmacia en la base de datos

**Issue de diseño:** #452  
**Historia de Usuario:** HU-67

---

## 1. Contexto

CareStock debe soportar múltiples farmacias dentro de una misma
base de datos PostgreSQL alojada en Neon.

Cada usuario autenticado posee un contexto de sesión que determina
la farmacia sobre la cual puede consultar y gestionar información.

El flujo implementado es:

```text
Usuario autenticado
        ↓
UserSession
        ↓
SessionContext
        ↓
id_farmacia
        ↓
DAO
        ↓
PostgreSQL / Neon
        ↓
Inventario de la farmacia activa
