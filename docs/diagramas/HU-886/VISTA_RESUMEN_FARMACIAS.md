# Vista `vw_resumen_farmacias`

Resumen por farmacia de sus medicamentos y de las alertas de stock mínimo. Está definida en `src/sql/vw_resumen_farmacias.sql`. Verificado sobre el código y los scripts del repositorio en el commit `e89a0fa` (19 de octubre de 2026).

---

## 1. Columnas

| Columna | Qué contiene |
|---|---|
| `id_farmacia` | Identificador de la farmacia |
| `codigo` | Código de la farmacia |
| `nombre` | Nombre de la farmacia |
| `estado` | Estado de la farmacia |
| `stock_total` | Suma del `stock_total` de **todos** los medicamentos de la farmacia, activos o no; 0 si no tiene |
| `medicamentos_activos` | Cantidad de medicamentos con estado `ACTIVO` |
| `alertas_criticas` | Cantidad de medicamentos `ACTIVO` con `stock_total` igual o inferior a `stock_minimo` |

---

## 2. Cómo calcula `alertas_criticas`

La vista une `farmacias` con `medicamentos` mediante un `LEFT JOIN` y agrupa por farmacia, así que devuelve **una fila por farmacia**, incluso las que no tienen medicamentos, que aparecen con ceros. `alertas_criticas` cuenta los medicamentos activos cuyo `stock_total` es menor o **igual** a su `stock_minimo`, y el resultado sale ordenado por el nombre de la farmacia.

---

## 3. Consulta de ejemplo por farmacia

```sql
SELECT id_farmacia, nombre, medicamentos_activos, alertas_criticas
FROM vw_resumen_farmacias
WHERE id_farmacia = 1;
```

Reemplaza `1` por el identificador de la farmacia. Devuelve una sola fila.

---

## 4. Límites

- **Solo cuenta medicamentos.** No incluye productos de aseo ni de maternidad, que no tienen stock mínimo.
- **Ninguna clase Java la usa.** La aplicación no consulta la vista. Vive en `src/sql/`, fuera de `ddl/`, y el `README` de esa carpeta no la menciona.
- **Su `stock_total` incluye los medicamentos inactivos,** a diferencia de los contadores `medicamentos_activos` y `alertas_criticas`.
