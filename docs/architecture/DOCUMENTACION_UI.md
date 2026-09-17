# Documentación de Interfaz Gráfica y Diseño UI - CareStock

Este documento consolida la definición del sistema de diseño, paleta cromática e interfaz de usuario para el sistema **CareStock**.

## 1. Paleta de Colores Oficial

Luego del proceso de evaluación, se seleccionó la **Opción B — Pastel farmacéutico** por sus tonos suaves que reducen el estrés visual y facilitan el uso prolongado en entornos de salud.

![Paleta Oficial de Colores](./paleta_seleccionada_pastel.png)

### Especificación Cromática

| Rol | Código Hex | Color / Tono | Uso en la Interfaz |
| :--- | :--- | :--- | :--- |
| **Dominante (60%)** | `#FDFBF7` | Marfil suave | Fondo general de la interfaz y tarjetas contenedoras |
| **Secundario (30%)** | `#A7D8D8` | Menta pastel | Navegación, encabezados y barra lateral izquierda |
| **Acento (10%)** | `#B7A6E0` | Lavanda pastel | Botones primarios, elementos activos y acciones principales |
| **Alerta - Stock bajo** | `#F6CE8E` | Durazno pastel | Indicadores de reabastecimiento próximo |
| **Alerta - Crítico** | `#F0A8A8` | Coral pastel | Medicamentos vencidos, agotados o alertas críticas |

---

## 2. Alternativas Evaluadas

Se diseñaron e integraron en la encuesta de decisión 5 alternativas de diseño para el sistema de inventario:

![Opciones de Paleta de Color](./PaletasColores_Opciones.jpeg)

* **Opción A:** Clínico confiable (Azul médico + Verde OK).
* **Opción B:** Pastel farmacéutico *(Seleccionada por el equipo)*.
* **Opción C:** Tech moderno (Índigo + Teal).
* **Opción D:** Alto contraste institucional.
* **Opción E:** Aguamarina y azul pastel.



## 3. Prototipo de Interfaz (Dashboard General)

Vista previa del panel principal aplicando el sistema de diseño y paleta oficial seleccionada:

![Dashboard General CareStock](./Interfaz_Mockup.jpeg)


## 4. Tipografía y Jerarquía de Textos

| Nivel | Tamaño | Peso | Uso |
| :--- | :--- | :--- | :--- |
| **Título principal** | 22px | Bold | Nombre de módulo ("CareStock"), título de vista ("Dashboard general") |
| **Subtítulo** | 16px | Semi-bold | Encabezado de sección ("Inventario reciente"), números grandes en tarjetas ("1,284", "37", "12") |
| **Cuerpo** | 13px | Regular | Texto del menú lateral ("Dashboard"), botón ("+ Agregar Medicamento"), encabezados de tabla ("Código INVIMA", "Producto", "Principio Activo", "Cantidad") y estados ("Tabla sin contenido") |
| **Etiqueta pequeña** | 11px | Regular | Subtextos de tarjetas ("Unidades en stock", "Próximos a vencer", "Alertas críticas") |

**Familia tipográfica:**  
System default de JavaFX (Segoe UI / San Francisco según SO) — no requiere fuente externa, evita problemas de licencias y carga.



## 5. Catálogo de Botones e Inputs

### Botones
| Estado | Color de fondo | Color de texto | Uso / Contexto |
| :--- | :--- | :--- | :--- |
| **Primario - Normal** | `#B7A6E0` | `#FFFFFF` | Botón principal de acción ("+ Agregar Medicamento") |
| **Primario - Hover** | `#A08BD1` | `#FFFFFF` | Estado activo al pasar el cursor sobre botón principal |
| **Primario - Disabled** | `#E0E0E0` | `#9E9E9E` | Acciones no disponibles en el estado actual |
| **Alerta crítica** | `#F0A8A8` | `#7A2E2E` | Botones de eliminación o acciones destructivas |

### Inputs de Texto
| Estado | Borde | Fondo | Uso / Contexto |
| :--- | :--- | :--- | :--- |
| **Normal** | `#D0D0D0` | `#FFFFFF` | Campo de texto en estado reposo |
| **Focus** | `#B7A6E0` | `#FFFFFF` | Campo de texto activo/seleccionado |
| **Error** | `#F0A8A8` | `#FDF5F5` | Campo de texto con validación fallida |


## 6. Catálogo de Badges/Etiquetas de Alerta

| Badge | Color de Fondo | Cuándo se usa |
| :--- | :--- | :--- |
| **Stock OK** | `#A7D8D8` | Stock por encima del mínimo |
| **Próximos a vencer** | `#F6CE8E` | Vencimiento dentro de 30 días |
| **Crítico / Vencido** | `#F0A8A8` | Stock bajo el mínimo o medicamento vencido |

---

## 7. Especificación de Alertas de Vencimiento y Accesibilidad (HU-30)

### Guía de Reglas de Color para Vencimientos
| Estado | Condición | Tono | Código Hex |
| :--- | :--- | :--- | :--- |
| **OK** | Más de 30 días para vencer | Menta pastel | `#A7D8D8` |
| **Próximo a vencer** | Entre 1 y 30 días | Durazno pastel | `#F6CE8E` |
| **Vencido** | Fecha de vencimiento cumplida | Coral pastel | `#F0A8A8` |

### Criterios de Accesibilidad (WCAG 2.1 AA)
- **Contraste Mínimo de Texto (Criterio 1.4.3):** Para garantizar una relación de contraste mínima de 4.5:1 sobre los fondos pastel (`#A7D8D8`, `#F6CE8E`, `#F0A8A8`), se establece el uso obligatorio de tipografía en tonos oscuros (`#1E1E1E` / `#222222`). Se prohíbe el uso de texto blanco sobre estas etiquetas.
- **Uso del Color (Criterio 1.4.1 - Doble Codificación):** El color no debe ser el único medio visual para transmitir la alerta. Cada badge debe integrar texto explícito con el nombre del estado (*OK*, *Próximo a vencer*, *Vencido*) acompañado de su respectivo tono indicador.


## 8. Mensajes de Confirmación y Error (HU-43)

| Situación | Título | Mensaje |
|---|---|---|
| Registro exitoso | Operación exitosa | "El [elemento] se registró correctamente." |
| Error genérico |  No se pudo completar la operación | "No se pudo guardar en la base de datos. Verifica los datos e intenta nuevamente." |
| Fecha bloqueada |  Fecha no permitida | "La fecha de vencimiento ingresada no cumple con la política mínima de CareStock." |

**Tono:** directo, sin tecnicismos, sin culpar al usuario.

## 9. Selector de Ubicación Física (HU-44)

### Estructura del componente
Dropdown (ComboBox de JavaFX) dentro del formulario de registro de lote.
Formato de cada opción: "Bodega [X] - Estante [Y] - Nivel [Z]"
Ej: "Bodega A - Estante 3 - Nivel 2"

### Estados visuales
| Estado | Borde | Comportamiento |
|---|---|---|
| Normal | #D0D0D0 | Placeholder "Selecciona una ubicación" |
| Focus | #B7A6E0 (lavanda oficial) | Despliega la lista |
| Error (vacío al guardar) | #F0A8A8 (coral oficial) | Mensaje: "Debes asignar una ubicación al lote" |

### Flujo de asignación
1. Usuario abre el formulario de registro de lote.
2. Sistema carga automáticamente las ubicaciones físicas disponibles.
3. Usuario selecciona una ubicación de la lista.
4. Si intenta guardar sin seleccionar, se bloquea con el estado de error de arriba.

MockUp Ubicacion


<img width="500" height="388" alt="MockupUbicacion_HU44" src="https://github.com/user-attachments/assets/cda4bcb3-02c7-4777-979f-41bb76777347" />


## 10. Reglas de Fecha de Vencimiento Bloqueada (HU-45)

### Reglas de negocio (tomadas de Reglas_Modelo_Negocio.md, sección 2)
| Rango | Color | Comportamiento |
|---|---|---|
| Más de 90 días | Sin alerta | Se guarda normal |
| Entre 16 y 90 días | #F6CE8E (durazno) | Muestra advertencia de confirmación, pero permite guardar |
| 15 días o menos | #F0A8A8 (coral) | Bloqueo absoluto, no permite guardar |


### Estados visuales del campo de fecha
- **Normal:** borde gris estándar.
- **Advertencia (16-90 días):** borde durazno #F6CE8E + ícono ⚠️ + texto "Vida útil corta, confirma para continuar".
- **Bloqueado (≤15 días):** borde coral #F0A8A8 + ícono 🚫 + texto "Fecha no permitida: vida útil mínima de 15 días" + botón Guardar deshabilitado.

MockUp campo de fecha Bloqueado


<img width="500" height="388" alt="MockupFecha_HU45" src="https://github.com/user-attachments/assets/5e8882a6-5b10-49ad-bb28-81702c9d707c" />


