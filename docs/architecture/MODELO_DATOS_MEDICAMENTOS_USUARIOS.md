# Plantilla de Especificación: Modelo de Datos (MEDICAMENTOS y USUARIOS)

**Parent Issue:** #183 (HU.37)  
**Autor / PO:** Mateo Salazar  
**Ubicación:** `docs/architecture/MODELO_DATOS_MEDICAMENTOS_USUARIOS.md`  

---

## 📌 Objetivo
Especificar el esquema técnico y la estructura de almacenamiento de las tablas `USUARIOS` y `MEDICAMENTOS` en la base de datos de CareStock. Este documento servirá como guía para la implementación backend y auditoría del Jefe de Farmacia.

---

## 1. Tabla: `USUARIOS`

> **Descripción:** Almacena la información de acceso, roles y datos personales de los usuarios del sistema.

| Nombre de Columna | Tipo de Dato | ¿Obligatorio? | Llave (PK/FK) | Referencia / Descripción |
|-------------------|--------------|---------------|---------------|--------------------------|
| `id`              | BIGINT / INT | NOT NULL      | PK            | Identificador único del usuario |
| `nombre`          | VARCHAR      | NOT NULL      | -             | Nombre completo del usuario |
| `correo`          | VARCHAR      | NOT NULL      | -             | Correo electrónico (Único) |
| `rol`             | VARCHAR      | NOT NULL      | -             | Rol en el sistema (Ej. Auxiliar, Jefe Farmacia) |
| `estado`          | BOOLEAN      | NOT NULL      | -             | Estado de la cuenta (Activo/Inactivo) |

---

## 2. Tabla: `MEDICAMENTOS`

> **Descripción:** Almacena el catálogo base de medicamentos registrados en el inventario.

| Nombre de Columna | Tipo de Dato | ¿Obligatorio? | Llave (PK/FK) | Referencia / Descripción |
|-------------------|--------------|---------------|---------------|--------------------------|
| `id`              | BIGINT / INT | NOT NULL      | PK            | Identificador único del medicamento |
| `nombre`          | VARCHAR      | NOT NULL      | -             | Nombre comercial o genérico |
| `principio_activo`| VARCHAR      | NULL          | -             | Componente activo principal |
| `presentacion`    | VARCHAR      | NOT NULL      | -             | Forma farmacéutica (Ej. Pastillas, Jarabe) |
| `concentracion`   | VARCHAR      | NULL          | -             | Dosis o concentración (Ej. 500mg) |

---

## 📋 Checklist de Validación UAT / PO
- [x] Estructura y campos base definidos por el Product Owner.
- [ ] Mapeo ajustado exactamente a los scripts DDL/DML de `src/sql/` por el Desarrollador Backend.
- [ ] Revisión y aprobación final del Pull Request por el PO.
