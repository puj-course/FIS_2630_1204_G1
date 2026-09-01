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
<!-- markdownlint-disable MD033 -->

<p align="center">
  <img 
    src="https://github.com/user-attachments/assets/ced40b84-17ec-4815-be22-b0eee4c4f1b7" 
    alt="Imagen 1" 
    width="80%"
  />
</p>

<p align="center">
  <img 
    src="https://github.com/user-attachments/assets/374521b0-8fa0-47a2-a14b-c9d693fc9e0e" 
    alt="Imagen 2" 
    width="80%"
  />
</p>

---

<p align="center">
  <em>✦ Galería centrada ✦</em>
</p>

### 1. Tabla `CATEGORIAS`
Almacena las clasificaciones farmacéuticas de los medicamentos.

```sql
CREATE TABLE CATEGORIAS (
    id_categoria SERIAL PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL UNIQUE
);







