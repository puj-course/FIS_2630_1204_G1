# Segregación por farmacia y trazabilidad de las alertas

Cómo se limitaría cada alerta a la farmacia del usuario y qué queda registrado de ellas. Verificado sobre el código y los scripts del repositorio en el commit `e89a0fa` (19 de octubre de 2026).

---

## 1. Alertas, farmacia y registro

| Alerta | Vista | Columna de farmacia | Cómo se limitaría a la farmacia del usuario | ¿Queda registro? |
|---|---|---|---|---|
| Stock mínimo | `vw_resumen_farmacias` | `id_farmacia`, tomada de `farmacias`: una fila por farmacia | `WHERE id_farmacia = ?` con el valor que devuelve `Inventario.farmaciaActual()` | **No** |
| Vencimiento | `vw_alertas_vencimiento` | `id_farmacia`, tomada de `medicamentos`: una fila por lote | `WHERE id_farmacia = ?` con el valor que devuelve `Inventario.farmaciaActual()` | **Solo el `AJUSTE`** que registra la sincronización al pasar un lote a `VENCIDO`; la alerta en sí no deja registro |

---

## 2. Cómo se limitaría a la farmacia

Las dos vistas devuelven datos de **todas** las farmacias, así que la aplicación tendría que filtrar siempre por `id_farmacia`. Ya lo hace con el resto del inventario: `Inventario.farmaciaActual()` obtiene la farmacia del usuario autenticado, comprueba que la sesión sigue siendo la suya y que el usuario y su farmacia están activos, y la pasa a los DAO y servicios, como `LoteDAOImpl.listarPorFarmacia` y `ControlVencimientosService.sincronizar`. Un usuario sin farmacia activa asignada es rechazado con el mensaje "Usuario inactivo o sin farmacia activa asignada". Para usar las vistas habría que hacer lo mismo. Es el mismo mecanismo que describe [`SEGREGACION_FARMACIA_APLICACION.md`](../../architecture/design/SEGREGACION_FARMACIA_APLICACION.md).

---

## 3. Qué queda registrado

- **Hoy la aplicación no consulta esas vistas.** Ninguna clase Java las usa.
- **Lo único que se registra es el `AJUSTE` por vencimiento** en `log_movimientos`: uno por cada lote que la sincronización pasa a `VENCIDO` con cantidad mayor que cero, a nombre del usuario que cargó los lotes. `log_movimientos` no guarda la farmacia: se deduce a través de `lotes` y `medicamentos`, como hace `VW_MOVIMIENTOS_FARMACIA`.
- **Mostrar una alerta no deja ningún registro.** No se guarda quién la vio ni cuándo, y no existe ninguna tabla de alertas ni de notificaciones.
