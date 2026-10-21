# Unicidad del código de producto por farmacia

Cómo se trata hoy un código repetido al registrar un producto desde el formulario del catálogo. El flujo completo del registro está en [`FORMULARIO_UNICO_REGISTRO.md`](FORMULARIO_UNICO_REGISTRO.md).

---

## 1. Estado por categoría

El producto siempre se guarda con la farmacia del usuario en sesión (`Inventario.farmaciaActual()`), por lo que la unicidad se evalúa dentro de cada farmacia.

| Categoría | Restricción en la base de datos | Revisión en el código | Con un código repetido |
|---|---|---|---|
| Medicamentos | Índice único `uq_medicamentos_farmacia_invima` sobre `id_farmacia` y `codigo_invima` | Ninguna | La base lo rechaza y la alerta mostraría el mensaje del driver de PostgreSQL tras "No se pudo registrar: " |
| Aseo | No verificable: ningún script del repositorio crea la tabla `aseo` | Ninguna | Depende de la tabla; sin restricción, se crea el duplicado sin ningún aviso |
| Maternidad | No verificable: ningún script del repositorio crea la tabla `maternidad` | Ninguna | Depende de la tabla; sin restricción, se crea el duplicado sin ningún aviso |

---

## 2. Por qué `buscarPorCodigoInvima` no sirve para validar

- `MedicamentoDaoJdbc.buscarPorCodigoInvima` consulta `WHERE codigo_invima = ?` **sin filtrar por farmacia** y devuelve solo la primera fila.
- La base sí permite el mismo INVIMA en farmacias distintas: el script 04 elimina la restricción global y crea el índice por farmacia, con el comentario "el mismo código INVIMA puede existir en más de una sede".
- Usarlo para validar bloquearía registros válidos de otra farmacia.
- Ninguna clase de la aplicación lo invoca hoy.

---

## 3. Observación: el código se recorta solo al editar

El alta de medicamentos guarda el código tal como se escribió (`MedicamentoDaoJdbc.guardar`), mientras que la edición lo recorta con `trim()` (`MedicamentoCatalogoDao.actualizar`). Un código con espacios al final se considera distinto del mismo código sin ellos.

---

## 4. Alternativa para validar (no implementada)

Consultar el código dentro de la farmacia antes de guardar, con un método `existeCodigo(codigo, idFarmacia)` en cada DAO, y lanzar un mensaje propio como "Ya existe un medicamento con el registro INVIMA X en su farmacia". Se estima en unas 18 líneas para medicamentos y unas 56 para las tres categorías, sin modificar el formulario.

---

## 5. Estado de los casos a validar

| Caso | Estado | Evidencia |
|---|---|---|
| Farmacia asignada | **Cumplido** | `Inventario.registrarMedicamento` asigna la farmacia de la sesión antes de guardar |
| Código repetido | **Pendiente** | No hay revisión en el código; en medicamentos lo frena la base con un mensaje técnico |
| Visible al instante | **Cumplido en el catálogo** | `onGuardar` recarga la tabla con `cargarTabla()`; la pantalla de inventario muestra lotes |
