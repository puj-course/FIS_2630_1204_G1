# Propuesta: stock mínimo para aseo y maternidad

Propuesta para agregar el stock mínimo a las tablas `aseo` y `maternidad`, que hoy solo tienen `stock`. **Es solo una propuesta: no se ejecuta ningún cambio.** Verificado sobre el código y los scripts del repositorio en el commit `e89a0fa` (19 de octubre de 2026).

---

## 1. Qué se propone

Agregar a `aseo` y a `maternidad` una columna `stock_minimo` con la misma definición que `medicamentos.stock_minimo`: entera, obligatoria, con valor por defecto 0 y no negativa.

---

## 2. Sentencia SQL propuesta

```sql
ALTER TABLE aseo
    ADD COLUMN IF NOT EXISTS stock_minimo INT NOT NULL DEFAULT 0
    CONSTRAINT ck_aseo_stock_minimo CHECK (stock_minimo >= 0);

ALTER TABLE maternidad
    ADD COLUMN IF NOT EXISTS stock_minimo INT NOT NULL DEFAULT 0
    CONSTRAINT ck_maternidad_stock_minimo CHECK (stock_minimo >= 0);
```

Con `IF NOT EXISTS`, se puede ejecutar varias veces sin error.

---

## 3. Qué pasa con las filas existentes

- **Quedan con `stock_minimo = 0`.**
- **Las operaciones actuales de `CatalogoDAO` siguen funcionando:** el `INSERT` no menciona la columna y toma 0, el `UPDATE` no la modifica y las lecturas con `SELECT *` la ignoran.
- **Con 0, un producto sin existencias cumpliría `stock <= stock_minimo`,** igual que ocurre hoy con los medicamentos. El mínimo real hay que fijarlo producto por producto.

---

## 4. Qué tendría que cambiar después en el código

1. **Modelos y builders.** `Aseo`, `Maternidad`, `AseoBuilder` y `MaternidadBuilder` necesitan el atributo `stockMinimo`, con la validación de no negativo (hoy solo validan `stock`).
2. **DAO.** `AseoDAO` y `MaternidadDAO` leen cada columna por nombre en `construir`, y ahí habría que leer `stock_minimo`. Para el `INSERT` y el `UPDATE` hay dos caminos: agregarla como columna común en `CatalogoDAO`, lo que obliga a desplazar los índices fijos de los parámetros (1 a 5 para los comunes, 6 en adelante para los específicos y `6 + cantidad`, `7 + cantidad` y `8 + cantidad` en `actualizar`), o agregarla a `columnasEspecificas()` de cada DAO, que no toca `CatalogoDAO` porque su `INSERT` y su `UPDATE` se arman con esa lista.
3. **Formulario del catálogo.** `txtStockMinimo` está dentro de `panelMedicamentos`, que se oculta salvo para medicamentos, así que habría que moverlo a un panel compartido como `panelPrecioStock` o repetirlo en los de aseo y maternidad. En `CatalogoController`, `construirAseo()` y `construirMaternidad()` deberían leerlo, como hoy hace `construirMedicamento()`, y el llenado del formulario al seleccionar una fila debería mostrarlo.
4. **Vistas y scripts.** `vw_resumen_farmacias` solo cuenta medicamentos, así que una alerta que incluya aseo y maternidad necesitaría una vista nueva. Además, el script que cree las tablas, si se versiona en el repositorio, debería incluir la columna para las instalaciones nuevas.

---

## 5. Alcance

Este documento no ejecuta ningún cambio en la base de datos ni en el código. Las tablas reales de Neon no se tocan hasta que el equipo apruebe la propuesta.
