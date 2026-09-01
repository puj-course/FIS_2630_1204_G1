# Validación de consistencia — Requerimientos funcionales vs. modelo actual

| Código(s) | Tema | Cumple | Qué falta implementar |
|---|---|---|---|
| R01–R10, R21–R28 | Órdenes de despacho | No | No existe la entidad Orden de Despacho ni su detalle en ningún diagrama. Nota: R01–R10 y R21–R28 describen prácticamente lo mismo con distinta redacción, parecen dos borradores mezclados en la hoja; conviene consolidarlos antes de modelar. |
| R11, R26 | FEFO al despachar | No | El campo `fecha_vencimiento` en `LOTES` permite ordenar por FEFO, pero no hay ninguna consulta ni método que lo implemente todavía. |
| R12, R27 | Ingreso de lotes con INVIMA/ubicación | Sí | — |
| R13, R31 | Panel semáforo de vencimiento | No | Los datos existen (`fecha_vencimiento`), pero falta la vista/función que calcule el color y el job diario que la ejecute. |
| R14, R16, R32 | Alertas de stock mínimo, umbral configurable | No | `stock_minimo` ya es configurable por producto, pero la alerta en sí hoy es solo el resultado de un `SELECT` (no persiste, no tiene estado "resuelta"). Falta crear la tabla `ALERTAS`. |
| R15 | Autocompletado técnico | No aplica | Requisito de interfaz/aplicación, no de base de datos. |
| R17, R34, R36 | CRUD de proveedores | No | `Proveedor` está en el diagrama de clases pero no existe tabla `PROVEEDORES` ni FK desde `LOTES`. |
| R18, R37 | Reportes de mermas por vencimiento | No | Se puede calcular filtrando `LOTES` con `estado_lote = 'VENCIDO'`, pero no hay valor financiero asociado, falta un campo de costo en `MEDICAMENTOS` o `LOTES`. |
| R19, R41 | RBAC por rol | No | El mecanismo (`ROLES` → `USUARIOS`) existe, pero los roles sembrados (`ADMINISTRADOR`, `FARMACEUTICO`) no coinciden con los nombres que usan los requerimientos (`Jefe de Farmacia`, `Auxiliar de Farmacia`, `Auditor`). Falta alinear el seed con la terminología real. |
| R20 | Búsqueda en tiempo real / full-text | No | El requisito especifica Oracle Database explícitamente. Falta definir el motor final y crear el índice correspondiente (`GIN`/`pg_trgm` en Postgres, Oracle Text en Oracle). |
| R21 | Código de seguimiento único | Parcial, contar como No | Las PK autoincrementales garantizan unicidad interna, pero si se requiere un código visible al usuario (no solo el ID técnico), falta una columna dedicada tipo UUID/correlativo legible. |
| R24 | Integración con receta médica digital | No | No hay entidad `Receta`, ni el modelo contempla pacientes ni médicos. |
| R25 | Entregas fraccionadas de tratamientos crónicos | No | No hay tabla de saldos pendientes por paciente/formulación. |
| R29 | Transferencias entre bodegas | No | No existe una tabla de transferencias; hoy un cambio de ubicación sería un `UPDATE` directo sin dejar rastro. |
| R30 | Conteo físico / acta de ajuste | No | `LOG_MOVIMIENTOS` con `tipo_movimiento = 'AJUSTE'` registra el efecto, pero falta una tabla que agrupe un conteo completo con su justificación formal. |
| R33 | Notificaciones multicanal | No aplica | Requisito de integración externa (Twilio/SendGrid), no de modelo de datos. |
| R35 | Sugerencia de compra por rotación histórica | No | Los datos base están en `LOG_MOVIMIENTOS`, pero falta una vista o tabla que calcule consumo promedio y punto de reorden. |
| R38 | Reporte normativo medicamentos de control especial | No | Falta el campo `control_especial` en `MEDICAMENTOS`. |
| R39 | Dashboard analítico | No aplica | Requisito de aplicación/BI, depende de que existan las tablas de movimientos e histórico, pero no requiere cambios de modelo en sí. |
| R40 | Log de auditoría inmutable general | No | `LOG_MOVIMIENTOS` audita solo movimientos de lote. Falta un mecanismo que audite también cambios en `USUARIOS`, `MEDICAMENTOS`, etc. |
| R42 | MFA / OTP | No | Falta `otp_secret` (u otra estrategia) en `USUARIOS`. |
| R43 | Temperatura/humedad, cadena de frío | No | No existe tabla de lecturas de sensor. |
| R44 | Trazabilidad inversa (lote hacia receptores) | No | Depende de que existan Orden de Despacho y Paciente, ninguna existe hoy. |

## Requerimiento sugerido (para completar R45, que está vacío en la hoja)

R45 — El sistema debe permitir registrar y consultar los datos básicos
del paciente o cliente receptor de una orden de despacho (identificación,
nombre completo, datos de contacto).

Por qué hace falta: R44 pide poder consultar todos los pacientes que
recibieron unidades de un lote, pero ningún requerimiento actual pide
registrar al paciente en primer lugar. Sin R45, R44 es imposible de
cumplir porque no hay dónde consultar esos datos.
