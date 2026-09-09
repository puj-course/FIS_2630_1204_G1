# Especificación Técnica: Estructura de Datos del Catálogo Maestro de Medicamentos (CareStock)

## 1. Introducción y Contexto Operativo
Este documento define la estructura de datos técnica para el **Catálogo Maestro de Medicamentos** del sistema **CareStock** (conectado a **Neon DB**). Su propósito es servir como contrato de arquitectura y especificación de diseño para el equipo de desarrollo, estableciendo con precisión los atributos, tipos de datos, restricciones y relaciones necesarios para garantizar la integridad operativa, la trazabilidad de inventarios y una correcta integración entre la base de datos relacional y la interfaz de usuario (Dashboard general).

---

## 2. Análisis de Interfaz y Requerimientos del Inventario
A partir de la interfaz gráfica de CareStock, el módulo de inventarios visualiza los siguientes campos principales por cada registro de medicamento:
* **ID**: Identificador único interno del registro en la base de datos.
* **INVIMA**: Código oficial de registro sanitario (ej. `INVIMA-2024M-001`), fundamental para la regulación y control farmacéutico en Colombia.
* **NOMBRE**: Nombre comercial del medicamento con su respectiva concentración (ej. `Acetaminofén 500mg`, `Amoxicilina 500mg`).
* **PRINCIPIO**: Principio activo del fármaco (ej. `Paracetamol`, `Amoxicilina`).
* **CATEGORIA**: Clasificación terapéutica o farmacológica (ej. `ANALGESICOS`, `ANTIBIOTICOS`, `CARDIOVASCULAR`, `GASTROENTEROLOGIA`, `ENDOCRINOLOGIA`, `RESPIRATORIO`).
* **STOCK**: Cantidad actual disponible físicamente en inventario (unidades enteras).
* **STOCK MÍN**: Límite inferior de stock que activa alertas críticas o de reabastecimiento en el dashboard.

---

## 3. Diseño del Modelo de Base de Datos (Neon DB - PostgreSQL)

Para soportar de manera óptima el catálogo maestro y sus operaciones de control de stock, se propone el siguiente esquema relacional normalizado:

### 3.1. Tabla: `categorias`
Almacena las categorías farmacológicas para mantener la integridad referencial y evitar redundancias de texto.

| Columna | Tipo de Dato | Restricciones / Atributos | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | `SERIAL` o `INT` | `PRIMARY KEY`, `AUTO_INCREMENT` | Identificador único de la categoría. |
| `nombre` | `VARCHAR(100)` | `NOT NULL`, `UNIQUE` | Nombre de la categoría (ej. ANALGESICOS). |
| `created_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP` | Fecha de creación del registro. |

### 3.2. Tabla Principal: `medicamentos`
Contiene el catálogo maestro de medicamentos vinculado al inventario y a su respectiva categoría.

| Columna | Tipo de Dato | Restricciones / Atributos | Descripción |
| :--- | :--- | :--- | :--- |
| `id` | `SERIAL` o `INT` | `PRIMARY KEY`, `AUTO_INCREMENT` | Identificador interno del medicamento (`ID` en UI). |
| `invima` | `VARCHAR(50)` | `NOT NULL`, `UNIQUE` | Registro sanitario INVIMA (ej. `INVIMA-2024M-001`). |
| `nombre` | `VARCHAR(150)` | `NOT NULL` | Nombre comercial y concentración (`NOMBRE` en UI). |
| `principio_activo` | `VARCHAR(150)` | `NOT NULL` | Sustancia farmacológica principal (`PRINCIPIO` en UI). |
| `categoria_id` | `INT` | `FOREIGN KEY` (references `categorias(id)`), `NOT NULL` | Relación con la categoría del medicamento. |
| `stock` | `INT` | `NOT NULL`, `DEFAULT 0`, `CHECK (stock >= 0)` | Unidades actuales en stock (`STOCK` en UI). |
| `stock_minimo` | `INT` | `NOT NULL`, `DEFAULT 0`, `CHECK (stock_minimo >= 0)` | Límite mínimo para alertas (`STOCK MÍN` en UI). |
| `fecha_vencimiento` | `DATE` | `NULL` (Opcional según lote) | Fecha de expiración para control de próximos a vencer. |
| `updated_at` | `TIMESTAMP` | `DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP` | Última actualización del registro de inventario. |

---

## 4. Estructura de Datos en la Interfaz de Usuario (Contrato Frontend - Backend)

Para la comunicación entre la capa de presentación (interfaz de CareStock) y la base de datos PostgreSQL, se define el siguiente modelo de transferencia de datos (**DTO / Objeto JSON**):

```json
{
  "id": 1,
  "invima": "INVIMA-2024M-001",
  "nombre": "Acetaminofén 500mg",
  "principioActivo": "Paracetamol",
  "categoria": "ANALGESICOS",
  "stock": 20,
  "stockMinimo": 100,
  "estadoAlerta": "CRITICO" 
}
```

### 4.1. Reglas de Negocio e Interfaz
1. **Control de Stock Crítico:** Si `stock <= stock_minimo`, el sistema genera automáticamente una alerta visual en el Dashboard general (como se observa en las tarjetas de alertas críticas del módulo).
2. **Validación de Entradas (Formulario de "+ Agregar Medicamento"):**
   * El campo `invima` debe respetar elfanuméricos y guiones según el formato normativo.
   * Los campos `stock` y `stock_minimo` aceptan únicamente valores enteros mayores o iguales a 0.
   * `nombre` y `principio_activo` son obligatorios con un límite mínimo de longitud de 3 caracteres.

---

## 5. Script SQL de Creación (PostgreSQL / Neon DB)

```sql
-- Creación de la tabla de categorías
CREATE TABLE IF NOT EXISTS categorias (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Creación de la tabla maestra de medicamentos
CREATE TABLE IF NOT EXISTS medicamentos (
    id SERIAL PRIMARY KEY,
    invima VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    principio_activo VARCHAR(150) NOT NULL,
    categoria_id INT NOT NULL,
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0),
    stock_minimo INT NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    fecha_vencimiento DATE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_categoria FOREIGN KEY (categoria_id) REFERENCES categorias(id) ON DELETE RESTRICT
);

-- Índices recomendados para optimización de consultas en el Dashboard
CREATE INDEX IF NOT EXISTS idx_medicamentos_invima ON medicamentos(invima);
CREATE INDEX IF NOT EXISTS idx_medicamentos_categoria ON medicamentos(categoria_id);
```

---
*Documento generado para el equipo de desarrollo de CareStock.*
