# Diseño de la interfaz de registro de farmacia

**Historia de Usuario:** HU.64
**Tarea:** #489
**Responsable:** Laura Sofía Ortiz Gómez (Scrum Master)
**Fecha:** 30 de septiembre de 2026

---

## 1. Objetivo

Documentar el diseño de la interfaz de registro de farmacia, incluyendo el wireframe, la paleta aplicada y las validaciones visuales implementadas.

---

## 2. Wireframe

[Ver Wireframe Registro Farmacia](https://github.com/puj-course/FIS_2630_1204_G1/blob/main/docs/HU/HU64/wireframe-registro-farmacia.png)

---

## 3. Paleta aplicada

| Elemento | Color | Código |
|----------|-------|--------|
| Fondo general | Blanco clínico | `#F4FAF9` |
| Fondo tarjeta | Blanco | `#FFFFFF` |
| Botón principal | Teal | `#0E7C7B` |
| Botón hover | Teal oscuro | `#0B615F` |
| Bordes | Gris claro | `#D6E6E4` |
| Texto principal | Oscuro | `#20302F` |
| Texto secundario | Gris | `#6D8683` |
| Error | Rojo | `#C0392B` |
| Éxito | Verde | `#2FBF9F` |

---

## 4. Componentes de la interfaz

| Componente | Tipo | Descripción |
|------------|------|-------------|
| Título | Label | "Crear nuevo usuario" |
| Nombre completo | TextField | Campo obligatorio |
| Correo electrónico | TextField | Campo obligatorio |
| Contraseña | PasswordField | Campo obligatorio |
| Rol a crear | Label | Muestra el rol destino |
| Farmacia | ComboBox | Selector de farmacia |
| Panel crear farmacia | TitledPane | Formulario expandible |
| Código de farmacia | TextField | Campo obligatorio |
| Nombre de farmacia | TextField | Campo obligatorio |
| Crear farmacia | Button | Botón principal (teal) |
| Nota campos obligatorios | Label | "* Campos obligatorios" |

---

## 5. Validaciones visuales implementadas

| Validación | Comportamiento visual |
|------------|----------------------|
| Campos obligatorios | Marcados con asterisco (*) |
| Botón "Crear farmacia" deshabilitado | Si código o nombre están vacíos |
| Registro exitoso | Mensaje verde de confirmación |
| Error de conexión | Mensaje rojo de error |
| Código duplicado | Mensaje rojo "Ya existe una farmacia con ese código" |

---

## 6. Mensajes implementados

### Mensajes de éxito (verde)

| Mensaje | Cuándo aparece |
|---------|----------------|
| "La farmacia 'X' fue creada correctamente." | Al registrar una farmacia exitosamente |

### Mensajes de error (rojo)

| Mensaje | Cuándo aparece |
|---------|----------------|
| "Ya existe una farmacia con ese código." | Cuando el código ya está registrado |
| "No fue posible crear la farmacia. Verifique su conexión e intente nuevamente." | Error de conexión con la base de datos |

---

## 7. Archivo modificado

| Archivo | Descripción |
|---------|-------------|
| `src/main/java/com/carestock/view/CrearUsuarioFX.java` | Contiene el panel de registro de farmacia con la paleta oficial y las validaciones visuales |

---

**Fin del documento.**
