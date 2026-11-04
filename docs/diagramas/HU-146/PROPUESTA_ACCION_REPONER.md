# Propuesta: acción de reponer desde la alerta

**Estado: propuesta pendiente de aprobación del equipo. No se implementa nada:** este documento no modifica código, pantallas ni base de datos. Describe cómo se podría reponer un producto directamente desde su alerta de stock mínimo. Verificado sobre el código y los scripts del repositorio en el commit `0b7e935` (2 de noviembre de 2026). Parte de [CASOS_LIMITE_REPOSICION.md](CASOS_LIMITE_REPOSICION.md) y de [FLUJO_REPOSICION_ACTUAL.md](FLUJO_REPOSICION_ACTUAL.md).

---

## 1. Qué se propone

Agregar a la tabla de la pantalla de Alertas una columna **Acción** con un botón **Reponer** en las filas de stock mínimo. El botón lleva a la pantalla donde hoy se registra una entrada de esa categoría, con el producto y la cantidad sugerida ya cargados; el usuario completa lo que falta y confirma. Hoy el usuario ve la sugerencia en la columna Reponer, pero tiene que ir por su cuenta a registrar la entrada.

---

## 2. Boceto de la fila

```text
Severidad    Tipo          Categoria    Producto            Existencias  Reponer  Acción
Crítica      Stock mínimo  Aseo         Jabón líquido       0            8        [ Reponer ]
Advertencia  Stock mínimo  Medicamento  Paracetamol 500 mg  2            8        [ Reponer ]
Advertencia  Stock mínimo  Maternidad   Pañales etapa 1     1            11       [ Reponer ]
Crítica      Vencimiento   Medicamento  Amoxicilina 500 mg  40           —        (sin botón)
```

Los productos son ejemplos y las columnas Referencia, Detalle y Último movimiento se omiten. Solo las alertas de stock mínimo tienen el botón; las de vencimiento no, porque un lote por vencer no se resuelve reponiendo.

---

## 3. A qué pantalla lleva y qué llega precargado

| Categoría | Destino | Llega precargado | Lo completa el usuario |
|---|---|---|---|
| Medicamento | Diálogo "Crear lote", abierto sobre la pantalla de Alertas | El medicamento y la cantidad sugerida | Número de lote, fecha de vencimiento y ubicación |
| Aseo | Pantalla de Movimientos | Categoría, producto, movimiento `ENTRADA`, cantidad sugerida y un motivo sugerido ("Reposición por alerta de stock mínimo") | Confirma o edita la cantidad y el motivo |
| Maternidad | Pantalla de Movimientos | Categoría, producto, movimiento `ENTRADA`, cantidad sugerida y un motivo sugerido ("Reposición por alerta de stock mínimo") | Confirma o edita la cantidad y el motivo |

El producto se identifica con la columna `idReferencia` de la alerta, que es el mismo identificador (`id_medicamento`, `id_aseo` o `id_maternidad`) que usan el diálogo de lotes y la pantalla de Movimientos. La cantidad es la que calcula `SugerenciaReposicion`.

---

## 4. Quién puede reponer

- **Ver la alerta:** cualquier usuario con farmacia asignada, porque la pantalla de Alertas no revisa el rol.
- **Medicamentos:** los roles que hoy pueden crear lotes, `ADMINISTRADOR`, `SUPER_ADMIN` y `FARMACEUTICO` con farmacia asignada, que son los tres roles sembrados.
- **Aseo y maternidad:** cualquier usuario activo de la farmacia, como hoy en la pantalla de Movimientos.

La propuesta no cambia ningún permiso: el botón respeta la regla de su destino, los servicios siguen validando al guardar, y si el usuario no puede reponer el botón se deshabilita con un mensaje.

---

## 5. Qué queda registrado

- **Medicamentos:** una `ENTRADA` en `log_movimientos` con el detalle "Registro inicial del lote" y el número del lote, el lote en `lotes` y el `stock_total` recalculado.
- **Aseo y maternidad:** una `ENTRADA` en `movimientos_productos` con la cantidad, el motivo y el stock anterior y posterior.
- **No queda registrado:** el clic en el botón, ni que la entrada vino de una alerta. Para medicamentos el detalle es fijo; para aseo y maternidad solo queda si el usuario conserva el motivo sugerido. Tampoco se guarda el cierre de la alerta, porque desaparece sola cuando las existencias superan el mínimo.
- **No se crea ninguna tabla ni columna nueva.**

---

## 6. Riesgos

1. **El nombre promete más de lo que hace.** El botón registra una entrada de mercancía ya recibida, no crea un pedido. La guía describe la sugerencia como cuántas unidades pedir, y el código no tiene pedidos ni recepción de órdenes de compra, que piden R35 y R36. Mitigación: un tooltip que diga que no crea pedidos.
2. **La sugerencia puede ser 0.** Con mínimo 0 y 0 existencias, el valor por defecto de aseo y maternidad, la sugerencia es 0 y el botón no serviría. Conviene ocultarlo o deshabilitarlo en ese caso.
3. **La pantalla de Movimientos no es accesible hoy.** Su botón se eliminó en el commit `193237d`, así que la acción sería su único acceso, o habría que devolver también el botón del inventario. Hacerla solo para medicamentos dejaría fuera a aseo y maternidad.
4. **Un solo lote precargado.** Lo recibido puede venir en varios lotes con fechas distintas, y un lote que vence en 30 días o menos genera una alerta de vencimiento nueva.
5. **Volver desde Movimientos lleva al inventario, no a Alertas,** porque `onVolver` llama a `mostrarDashboard()`, y el usuario perdería el contexto.
6. **Alerta desactualizada.** La pantalla se refresca cada 60 segundos, así que dos usuarios podrían reponer lo mismo, y nada lo impide porque cada entrada suma. Mitigación: recargar las alertas al abrir el destino y al volver.
7. **Costo de calidad.** Hay que escribir pruebas para el código nuevo, y el quality gate de Sonar exige 80 % de cobertura.

---

## 7. Decisiones pendientes para el equipo

- Nombre del botón: "Reponer", como la columna, o "Registrar entrada", que es lo que hace.
- Aseo y maternidad: reutilizar la pantalla de Movimientos o abrir un diálogo propio como el de lotes.
- Devolver el botón "Movimientos" al inventario, con o sin esta acción.
- Sugerencia 0: ocultar o deshabilitar el botón.

---

## 8. Qué cambiaría si se aprueba

Nada de esto se hace en este sprint:

- `alertas-view.fxml` y `ControladorAlertas`: la columna Acción con el botón.
- `GestorLotesDialogo`: abrirse con el medicamento y la cantidad precargados.
- `ControladorMovimientos` y `CareStockApp.mostrarFormularioMovimientos`: abrirse con categoría, producto y cantidad.
- `ServicioCrudLotes.rolGestor`: hoy es privada del paquete, así que habría que exponerla o usar el rol de la sesión.
