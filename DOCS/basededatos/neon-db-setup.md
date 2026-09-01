# 🗄️ Documentación de Base de Datos - CARESTOCK (Neon PostgreSQL)

Esta documentación detalla la estructura, relaciones y scripts de migración utilizados en la base de datos alojada en **Neon (PostgreSQL)** para el módulo de inventario del proyecto **CARESTOCK**.

---

## 🛠️ Tecnologías Utilizadas

* **Motor de BBDD:** PostgreSQL (v15+)
* **Hosting Cloud:** Neon DB
* **Integración:** JDBC Driver para Java (PostgreSQL)

---

## 📐 Modelo de Datos y Esquema

El modelo relacional administra el inventario de medicamentos asegurando la integridad referencial a través de categorías farmacéuticas.

### 1. Tabla `CATEGORIAS`
Almacena las clasificaciones farmacéuticas de los medicamentos.

```sql
CREATE TABLE CATEGORIAS (
    id_categoria SERIAL PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE
);
