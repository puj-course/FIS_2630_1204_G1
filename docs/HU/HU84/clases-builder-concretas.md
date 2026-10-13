# Clases Builder Concretas

## Descripción

Los builders concretos implementan la interfaz `Producto` y construyen cada tipo de producto paso a paso.

---

## 1. MedicamentoBuilder

**Propósito:** Construir objetos `Medicamento` paso a paso.

**Métodos específicos:**

| Método | Descripción |
|--------|-------------|
| `principioActivo(String)` | Establece el principio activo del medicamento. |
| `concentracion(String)` | Establece la concentración del medicamento. |
| `build()` | Construye y devuelve el `Medicamento` final. |

---

## 2. CosmeticoBuilder

**Propósito:** Construir objetos `Cosmetico` paso a paso.

**Métodos específicos:**

| Método | Descripción |
|--------|-------------|
| `setRegistroSanitario(String)` | Establece el registro sanitario del cosmético. |
| `setTipoPiel(String)` | Establece el tipo de piel recomendado. |
| `build()` | Construye y devuelve el `Cosmetico` final. |

---

## 3. DispositivoMedicoBuilder

**Propósito:** Construir objetos `DispositivoMedico` paso a paso.

**Métodos específicos:**

| Método | Descripción |
|--------|-------------|
| `setClaseRiesgo(String)` | Establece la clase de riesgo del dispositivo. |
| `setRegistroSanitario(String)` | Establece el registro sanitario. |
| `build()` | Construye y devuelve el `DispositivoMedico` final. |

---

## Relación con el patrón Builder

| Rol | Clase |
|-----|-------|
| **Builder** | `Producto` (interfaz) |
| **ConcreteBuilder** | `MedicamentoBuilder`, `CosmeticoBuilder`, `DispositivoMedicoBuilder` |
| **Director** | `ProductoDirector` |
| **Product** | `Medicamento`, `Cosmetico`, `DispositivoMedico` |
