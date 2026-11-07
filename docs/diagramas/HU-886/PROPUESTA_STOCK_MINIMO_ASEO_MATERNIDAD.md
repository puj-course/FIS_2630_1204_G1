# Stock mínimo para aseo y maternidad: de la propuesta a la implementación

Este documento nació como una propuesta para agregar el stock mínimo a las tablas `aseo` y `maternidad`. El equipo la implementó con el script `15_stock_minimo_aseo_maternidad.sql`, que llegó en el commit `3ad23b8` (HU.124, 31 de octubre de 2026), y con los cambios de código correspondientes; aquí se compara lo propuesto con lo que existe hoy. Verificado sobre el código y los scripts del repositorio en el commit `72c5fa4` (7 de noviembre de 2026).

---

## 1. Lo propuesto frente a lo implementado

| Aspecto | Propuesta | Implementado |
|---|---|---|
| Columna | `stock_minimo INT NOT NULL DEFAULT 0` en `aseo` y en `maternidad` | La misma, declarada como `INTEGER` |
| Restricción | `ck_aseo_stock_minimo` y `ck_maternidad_stock_minimo`, con `stock_minimo >= 0` | Los mismos nombres, creados solo si no existen, dentro de una transacción |
| Repetir el script | `IF NOT EXISTS` | `ADD COLUMN IF NOT EXISTS` y una comprobación en `pg_constraint` |
| Filas existentes | Quedan con `stock_minimo = 0` | Igual |
| Dónde está | Sin número | `src/sql/ddl/15_stock_minimo_aseo_maternidad.sql` |

---

## 2. Cambios en el código que ya existen

1. **Modelos.** `Aseo` y `Maternidad` tienen el atributo `stockMinimo`, y su método `validar()` rechaza un mínimo negativo ("El stock mínimo del producto de aseo no puede ser negativo."); lo invocan `FabricaProducto` y `CatalogoDAO` antes de guardar, y la base lo vuelve a exigir con las restricciones del script `15`. Los builders ya no existen: el formulario crea los productos con fábricas (`FABRICA_ASEO` y `FABRICA_MATERNIDAD`) y les asigna `setStockMinimo`.
2. **SQL del catálogo.** El `INSERT` y el `UPDATE` de aseo y de maternidad incluyen `stock_minimo` (`ConsultasSqlCatalogo`).
3. **Formulario del catálogo.** Tiene los campos `txtStockMinimoAseo` y `txtStockMinimoMaternidad`, que se leen con `LecturaCampos.enteroOpcional`: un campo vacío se guarda como 0, sin aviso.
4. **Alertas.** `vw_alertas_inventario` (script `16`) incluye aseo y maternidad, así que ya generan alertas de stock mínimo.

---

## 3. Lo que sigue pendiente

- **El valor por defecto 0 genera alertas críticas poco útiles.** Un producto de aseo o de maternidad sin existencias y sin mínimo configurado alerta como crítico, y la cantidad sugerida de reposición es 0. Los casos están en `docs/diagramas/HU-146/CASOS_LIMITE_REPOSICION.md`.
- **El formulario no pide el mínimo en aseo y maternidad,** a diferencia de medicamentos, donde es obligatorio.
- **Nada limita el valor máximo del mínimo** más allá del rango de un entero; solo se exige que no sea negativo.
