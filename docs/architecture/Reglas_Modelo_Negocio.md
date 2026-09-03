### 1.1. Formato y Expresión Regular (Regex)
* **Regla:** El número de lote debe ser un texto alfanumérico flexible para adaptarse a los estándares de diversos laboratorios fabricantes (Nacionales e Internacionales).
* **Restricciones:**
  * Permitir letras (mayúsculas y minúsculas), números, guiones (`-`), guiones bajos (`_`) y barras (`/`).
  * Sin espacios en blanco ni caracteres especiales (`@`, `#`, `$`, `%`, etc.).
  * **Longitud mínima:** 3 caracteres.
  * **Longitud máxima:** 30 caracteres.
* **Expresión Regular Recomendada:**
  ```regex
  ^[a-zA-Z0-9\-\_\/]{3,30}$
  ```

### 1.2. Unicidad del Lote por Producto
* **Regla:** El `numero_lote` **es único únicamente en combinación con el `id_producto`**.
* **Comportamiento en Sistema:**
  * **Mismo Producto, Mismo Lote, Diferente Fecha de Vencimiento:** No permitido. Genera error de inconsistencia.
  * **Mismo Producto, Mismo Lote (Reingreso/Nueva Ubicación):** Permitido. El sistema sumará la cantidad al lote existente y registrará la sub-ubicación física o actualizará el stock total del lote.
  * **Diferente Producto, Mismo Lote:** Permitido (dos laboratorios o productos distintos pueden compartir accidentalmente un código de lote similar).

---

## 2. Política de Días Mínimos de Vencimiento (HU.22)

* **Política de Ingreso Normal:**
  * Todo lote nuevo que ingrese al almacén debe contar con una **vida útil mínima de 90 días (3 meses)** a partir de la fecha actual de registro.
* **Excepción / Margen Crítico:**
  * Medicamentos de urgencia o insumos de alta rotación con vida útil menor a 90 días (pero estrictamente superior a 15 días) requerirán una confirmación de advertencia en pantalla antes de guardar.
* **Bloqueo Absoluto:**
  * Bloqueo del sistema para cualquier lote con fecha de vencimiento **menor o igual a 15 días** desde la fecha de registro actual (`fecha_vencimiento <= fecha_actual + 15 días`).

---

## 3. Catálogo de Motivos de Ajuste de Inventario / Merma (HU.23)

| ID Motivo | Nombre del Motivo | Requiere Justificación Escrita | Requiere Evidencia / Foto | Impacto en Stock |
| :---: | :--- | :---: | :---: | :---: |
| **MOT-01** | Vencimiento o Caducidad | No | No | Resta (-) |
| **MOT-02** | Rotura o Daño Físico (Empaque / Frasco) | Sí | Sí | Resta (-) |
| **MOT-03** | Deterioro por Cadena de Frío | Sí | No | Resta (-) |
| **MOT-04** | Pérdida o Faltante por Conteo | Sí | No | Resta (-) |
| **MOT-05** | Muestra Médica / Control de Calidad | No | No | Resta (-) |
| **MOT-06** | Error de Digitación (Corrección de Entrada) | Sí | No | Reajuste (+ / -) |

---