# Diccionario de datos — CareStock

> Generado a partir de `MedicamentoDAO.java` (consultas reales contra Neon) y del
> script de migración de `MEDICAMENTOS` compartido por el equipo.
> Las columnas marcadas como **"No confirmado"** no aparecen en el código ni en
> los scripts que he visto — verifícalas directamente en DataGrip/Neon antes de
> confiar en este documento al 100%.

---

## Tabla `MEDICAMENTOS`

| Columna | Tipo | Obligatorio | Origen de la confirmación |
|---|---|---|---|
| `id_medicamento` | (numérico, probable `SERIAL`/`BIGINT`) | Sí (PK) | Usado como PK en `MedicamentoDAO.obtenerTodos()` |
| `codigo_invima` | VARCHAR | No confirmado | Usado en `MedicamentoDAO` (insert y select) |
| `nombre_comercial` | VARCHAR | **Sí** | `ALTER COLUMN nombre_comercial SET NOT NULL` |
| `principio_activo` | VARCHAR | **Sí** | `ALTER COLUMN principio_activo SET NOT NULL` |
| `concentracion` | VARCHAR | **Sí** | `ALTER COLUMN concentracion SET NOT NULL` |
| `forma_farmaceutica` | VARCHAR | **Sí** | `ALTER COLUMN forma_farmaceutica SET NOT NULL` (no se usa en `MedicamentoDAO`, pero sí en la migración) |
| `presentacion` | VARCHAR(100) | **Sí** | Agregada por migración: `ADD COLUMN presentacion VARCHAR(100)` + `SET NOT NULL` |
| `id_categoria` | INTEGER | **Sí** | FK hacia `CATEGORIAS.id_categoria` (join visible en `MedicamentoDAO`); `SET NOT NULL` en migración |
| `stock_total` | INTEGER | No confirmado (tiene default en el DAO: `0` si es null) | Usado en `MedicamentoDAO.obtenerTodos()` y en `obtenerTotalUnidadesStock()` |
| `stock_minimo` | INTEGER | No confirmado (default `0` en el DAO) | Usado en `MedicamentoDAO.obtenerAlertasCriticas()`: `stock_total <= stock_minimo` |
| `id_usuario_creacion` | INTEGER | **Sí** | FK hacia `USUARIOS.id_usuario`; agregada por migración, `SET NOT NULL` |
| `fecha_creacion` | TIMESTAMP | **Sí** | `DEFAULT CURRENT_TIMESTAMP`, agregada por migración |

**Llave primaria:** `id_medicamento`

**Llaves foráneas:**
- `id_categoria` → `CATEGORIAS(id_categoria)`
- `id_usuario_creacion` → `USUARIOS(id_usuario)` — constraint `fk_medicamento_usuario_creacion`

**Restricciones adicionales:**
- `presentacion`, `nombre_comercial`, `principio_activo`, `concentracion`, `forma_farmaceutica`, `id_categoria`, `id_usuario_creacion` son `NOT NULL`.

**No confirmado / pendiente de verificar en la BD real:**
- Si `MEDICAMENTOS` tiene columna `estado` (activo/inactivo). No aparece en el DAO ni en la migración que compartiste.
- Tipo de dato exacto de `id_medicamento`, `id_categoria`, `stock_total`, `stock_minimo` (asumidos numéricos por su uso en Java, pero no vi el `CREATE TABLE` original completo).
- Estructura completa de la tabla `CATEGORIAS` (solo se sabe que tiene `id_categoria` y `nombre_categoria`).

---

## Tabla `USUARIOS`

| Columna | Tipo | Obligatorio | Origen de la confirmación |
|---|---|---|---|
| `id_usuario` | (numérico) | Sí (PK) | Referenciado como FK desde `MEDICAMENTOS.id_usuario_creacion` |
| `email` | VARCHAR | No confirmado | Usado en el backfill de la migración: `WHERE email = 'admin@carestock.com'` |

**Llave primaria:** `id_usuario`

**No confirmado / pendiente de verificar:**
- El resto de columnas de `USUARIOS` (nombre, rol, contraseña, etc.) no aparecen en ningún archivo compartido hasta ahora — HU.25 (registro de usuarios) fue reportada como **no completada** en el Sprint Retrospective 5, lo que es consistente con que su documentación esté incompleta.

---

## Nota sobre inconsistencia encontrada

Un script anterior del equipo (`lotes` / `shrinkage_events`) referenciaba `medicamentos(id)` y `usuarios(id)` como nombres de columna, mientras que el código real en `MedicamentoDAO.java` usa `id_medicamento`. Este documento asume que **`id_medicamento` / `id_usuario` son los nombres correctos**, por ser los que efectivamente usa el código que ya corre contra Neon. Si encuentran ese script antiguo dando vueltas, está desactualizado.
