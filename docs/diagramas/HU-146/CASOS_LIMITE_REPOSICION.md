# Casos límite de la cantidad sugerida de reposición

Casos ejecutados contra la clase `SugerenciaReposicion` y contrastados con la vista `vw_alertas_inventario`. La regla, la fórmula y su uso están en la [guía de usuario](../../user_guide/alertas-exportar-y-reponer.md); este documento solo recoge los casos límite. Verificado sobre el código y los scripts del repositorio en el commit `0b7e935` (2 de noviembre de 2026).

---

## 1. Casos ejecutados

| Existencias | Mínimo | Resultado | ¿Genera alerta en la pantalla? |
|---|---|---|---|
| 2 | 5 | 8 | Sí, advertencia |
| 0 | 5 | 10 | Sí, crítica |
| 5 | 5 | 5 | Sí, advertencia |
| 20 | 5 | 0 | No |
| 0 | 0 | **0** | Sí, crítica |
| 3 | 0 | 0 | No |
| 0 | 1.500.000.000 | **0** | Sí, crítica |
| Alerta de vencimiento | — | — | Sí, un lote que vence en 10 días es crítica |

Cada resultado salió de llamar a `SugerenciaReposicion.texto` con una alerta construida con los valores de la fila. La última columna se comprobó creando un producto por fila en una base con los scripts `01` a `17` y ejecutando la misma consulta de `AlertasInventarioDao` sobre `vw_alertas_inventario`.

---

## 2. Casos donde la sugerencia no ayuda

1. **Mínimo 0 con 0 existencias.** Muestra `0`, no "—", aunque la alerta es crítica: el usuario ve una alerta crítica que sugiere reponer 0. No es un caso raro, porque el mínimo vale 0 por defecto en la base de datos y los formularios de aseo y maternidad convierten el campo vacío en 0. Cualquier producto de esas categorías sin mínimo configurado que se agote cae aquí.
2. **Existencias por encima del mínimo (20 y 5, 3 y 0).** La clase devuelve 0, pero la pantalla nunca los muestra, porque la vista solo genera la alerta de stock cuando las existencias son menores o iguales al mínimo. Por eso, en una alerta real la sugerencia es siempre al menos el mínimo, y el único 0 posible es el del caso anterior.
3. **Mínimo desde 1.073.741.824.** Al duplicar el mínimo el entero desborda y la sugerencia pasa a `0`, aunque el producto esté agotado. Es muy poco probable, pero ninguno de los formularios limita el valor máximo del mínimo.
4. **Alertas de vencimiento.** Muestran "—" porque la sugerencia solo aplica al stock mínimo; la guía explica el motivo.
