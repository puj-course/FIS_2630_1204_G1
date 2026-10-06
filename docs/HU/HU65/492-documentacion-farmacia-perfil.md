# Cómo se muestra la farmacia en el perfil del usuario

**Historia de Usuario:** HU.65
**Tarea:** #492
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 03 de octubre de 2026

---

## 1. Objetivo

Documentar cómo se muestra la farmacia asignada en el perfil del usuario dentro del dashboard de CareStock.

---

## 2. ¿Dónde se muestra?

La farmacia se muestra en **dos lugares** del dashboard:

### 2.1. En la barra superior (topbar)

En la parte superior derecha del dashboard, junto al nombre y rol del usuario:

```
Sesión: Laura Ortiz | ADMINISTRADOR | Farmacia: 1
```

- **Verde** si el usuario tiene farmacia asignada
- **Rojo** si el usuario no tiene farmacia asignada

### 2.2. En la barra lateral (sidebar)

En el panel izquierdo, debajo del nombre y rol del usuario:

```
Laura Ortiz
ADMINISTRADOR
Farmacia: 1
```

- **Verde** si tiene farmacia asignada
- **Rojo** si no tiene farmacia asignada

---

## 3. ¿Cómo se obtiene el dato?

El sistema obtiene la farmacia desde la **sesión activa**:

| Paso | Acción | Método |
|------|--------|--------|
| 1 | Obtener el usuario activo | `UserSession.getInstance().getCurrentUser()` |
| 2 | Verificar si tiene farmacia | `usuario.tieneFarmaciaAsignada()` |
| 3 | Obtener el ID de la farmacia | `usuario.getIdFarmacia()` |
| 4 | Mostrar en la interfaz | `Label` en `MainDashboardFX` |

---

## 4. Estados posibles

| Estado | Condición | Texto mostrado | Color |
|--------|-----------|----------------|-------|
| **Con farmacia** | `idFarmacia != null && idFarmacia > 0` | "Farmacia: X" | Verde (`#2FBF9F`) |
| **Sin farmacia** | `idFarmacia == null || idFarmacia <= 0` | "Sin farmacia" | Rojo (`#C0392B`) |

---

## 5. Componentes involucrados

| Componente | Archivo | Función |
|------------|---------|---------|
| `MainDashboardFX` | `view/MainDashboardFX.java` | Muestra el label con la farmacia |
| `UserSession` | `session/UserSession.java` | Almacena el usuario activo |
| `Usuario` | `model/Usuario.java` | Contiene `idFarmacia` y `tieneFarmaciaAsignada()` |

---

**Fin del documento.**
