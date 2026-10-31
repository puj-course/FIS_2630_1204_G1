# Alertas de inventario: reglas de negocio

Este documento define **cuándo** CareStock genera una alerta, **qué severidad** le asigna y qué casos no la generan. Las reglas viven en la vista `vw_alertas_inventario` (`src/sql/ddl/16_vista_alertas_inventario.sql`), por eso se citan sus líneas.

## 1. Resumen

| Tipo de alerta | Se genera cuando | Severidad |
|---|---|---|
| **Stock mínimo** | Las existencias de un producto activo son **iguales o menores** que su stock mínimo | **Crítica** si las existencias son 0; **advertencia** si son mayores que 0 |
| **Vencimiento** | Un lote disponible, con unidades, vence en **30 días o menos** | **Crítica** si faltan 15 días o menos (incluye los ya vencidos); **advertencia** si faltan de 16 a 30 |

## 2. Regla de stock mínimo

| Elemento | Regla | Dónde |
|---|---|---|
| Medicamentos | Compara `stock_total` con `stock_minimo` | `16_vista_alertas_inventario.sql` L61 |
| Aseo | Compara `stock` con `stock_minimo` | L103 |
| Maternidad | Compara `stock` con `stock_minimo` | L145 |
| Condición | `existencias <= stock_minimo` (el igual **sí** alerta) | las tres líneas anteriores |
| Solo productos activos | `estado = 'ACTIVO'` | cada bloque, junto a la condición |
| Severidad | `CASE WHEN existencias = 0 THEN 'CRITICA' ELSE 'ADVERTENCIA'` | L33 y bloques equivalentes |

Casos que conviene tener claros:

| Situación | ¿Alerta? | Por qué |
|---|---|---|
| Stock 4, mínimo 10 | Sí, advertencia | 4 es menor que 10 |
| Stock 10, mínimo 10 | Sí, advertencia | La regla incluye el valor igual |
| Stock 0, mínimo 5 | Sí, **crítica** | Producto agotado |
| Stock 0, mínimo 0 | Sí, **crítica** | Un producto sin umbral definido alerta cuando se agota |
| Stock 5, mínimo 0 | No | No hay umbral que superar |
| Stock 10, mínimo 5 | No | Está por encima del mínimo |
| Producto inactivo con stock 0 | No | Un producto desactivado no se repone |

## 3. Regla de vencimiento

La alerta nace de cada **lote**, no del medicamento. Un lote entra a la vista si cumple todas estas condiciones (`16_vista_alertas_inventario.sql`):

| Condición | Dónde |
|---|---|
| El medicamento está activo | bloque de vencimiento, junto a `FROM lotes l` (L168) |
| El lote está en estado `DISPONIBLE` | L191 |
| El lote tiene unidades (`cantidad_actual > 0`) | L192 |
| Faltan 30 días o menos para vencer | L193 |

La severidad sale de los días que faltan (`fecha_vencimiento - hoy`, calculados en L160):

| Días para vencer | Severidad | Dónde |
|---|---|---|
| 0 o negativo (ya venció o vence hoy) | Crítica | L162 |
| De 1 a 15 | Crítica | L162 |
| De 16 a 30 | Advertencia | bloque de vencimiento |
| 31 o más | Sin alerta | fuera de la condición de 30 días |

| Situación del lote | ¿Alerta? | Por qué |
|---|---|---|
| Vence en 10 días, 5 unidades | Sí, crítica | Menos de 15 días |
| Vence en 15 días | Sí, crítica | El límite de 15 es inclusivo |
| Vence en 25 días | Sí, advertencia | Entre 16 y 30 |
| Vence en 30 días | Sí, advertencia | El límite de 30 es inclusivo |
| Vence en 31 días | No | Aún está lejos |
| Venció ayer y sigue `DISPONIBLE` | Sí, crítica, "pendiente de deshabilitar" | Todavía no lo deshabilitó el control de vencimientos |
| Lote `AGOTADO` (0 unidades) | No | No hay nada que perder |
| Lote `VENCIDO` | No | Ya está deshabilitado |

### Lotes vencidos y el control de vencimientos

El servicio de control de vencimientos (`ServicioControlVencimientos.sincronizar`, L14) cambia a `VENCIDO` los lotes disponibles cuya fecha de vencimiento es hoy o ya pasó. Se ejecuta cada vez que el panel principal consulta los lotes (L439 de `ControladorInventario`). Por eso un lote que venció aparece como alerta crítica solo hasta que alguien abre el panel principal; después se deshabilita y deja de alertar. El contador del panel se calcula **después** de esa limpieza (L457), para que muestre el valor ya depurado.

## 4. Mensajes que ve el usuario

Los textos los arma `AlertaInventario.mensaje()` (`dto/AlertaInventario.java` L104).

| Caso | Mensaje |
|---|---|
| Stock en 0 | Sin existencias (mínimo 5) |
| Stock bajo | Existencias 3 (mínimo 5) |
| Vence en varios días | Vence en 12 días (01/12/2026) |
| Vence en un día | Vence en 1 día (01/12/2026) |
| Vence hoy | El lote vence hoy |
| Venció hace un día | Lote vencido hace 1 día, pendiente de deshabilitar |
| Venció hace varios días | Lote vencido hace 3 días, pendiente de deshabilitar |

## 5. Orden en que se muestran

El DAO ordena así (`dao/AlertasInventarioDao.java` L23):

1. Las **críticas** primero.
2. Dentro de cada severidad, por tipo de alerta.
3. Dentro del tipo, por los días que faltan para vencer (las que vencen antes, primero) y luego por existencias, de menor a mayor.
4. Por último, por nombre de producto.

## 6. Escenarios verificados

Estos casos se cargaron en una base PostgreSQL con los scripts del repositorio y se comprobaron contra la vista y contra la pantalla.

| Producto o lote | Datos | Resultado |
|---|---|---|
| Medicamento A2 | stock 4, mínimo 10 | Advertencia |
| Medicamento A3 | stock 0, mínimo 0 | Crítica |
| Medicamento A4 | stock 5, mínimo 0 | Sin alerta |
| Medicamento A5 | inactivo | Sin alerta |
| Medicamento A6 | stock 10, mínimo 10 | Advertencia |
| Aseo A1 | stock 3, mínimo 5 | Advertencia |
| Aseo A2 | stock 0, mínimo 5 | Crítica |
| Aseo A3 | stock 10, mínimo 5 | Sin alerta |
| Aseo A4 | inactivo | Sin alerta |
| Maternidad A1 | stock 5, mínimo 5 | Advertencia |
| Maternidad A2 | stock 6, mínimo 5 | Sin alerta |
| Lote a 10 días | disponible, 5 unidades | Crítica |
| Lote a 15 días | disponible | Crítica |
| Lote a 25 días | disponible | Advertencia |
| Lote a 30 días | disponible | Advertencia |
| Lote a 31 días | disponible | Sin alerta |
| Lote a 90 días | disponible | Sin alerta |
| Lote vencido ayer | disponible | Crítica |
| Lote agotado | 0 unidades | Sin alerta |
| Lote ya vencido | estado `VENCIDO` | Sin alerta |

Resultado: 11 alertas en la farmacia de prueba (5 críticas y 6 advertencias). Al abrir el panel principal el lote vencido ayer se deshabilita y el contador baja a 10.

## 7. Qué NO cubren estas reglas

- Los umbrales de 15 y 30 días están fijos en la vista; cambiarlos exige modificar el script 16.
- Un producto con `stock_minimo` en 0 no alerta hasta agotarse; no hay un valor por defecto "sensato".
- Las alertas no tienen estado "leída" o "atendida": desaparecen solas cuando la condición deja de cumplirse.
