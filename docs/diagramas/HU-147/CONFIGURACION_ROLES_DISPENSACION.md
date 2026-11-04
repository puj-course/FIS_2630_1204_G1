# Configuración de los roles autorizados para dispensar

Para dispensar un medicamento, el rol del usuario debe estar en la variable de entorno `CARESTOCK_ROLES_DISPENSACION`. Este documento explica su formato, los mensajes que produce, cómo obtener los ids de rol, dónde se configura y cómo agregarla a la documentación. Verificado sobre el código y los scripts del repositorio en el commit `0b7e935` (2 de noviembre de 2026).

---

## 1. La variable

- **Qué es:** una lista de ids de rol, la columna `id_rol` de la tabla `roles`, separados por comas. Por ejemplo, `2,3`.
- **Quién la lee:** `FarmacovigilanciaService.validarOperador`, con `System.getenv`, cada vez que se dispensa. Como es una variable de entorno del proceso, un cambio exige reiniciar la aplicación.
- **Cómo se interpreta:** se separa por comas, se quitan los espacios de cada elemento y se compara el texto exacto con el `id_rol` del usuario.

Resultado para un usuario cuyo rol tiene el id 3:

| Valor de la variable | Resultado |
|---|---|
| `2,3`, `2, 3`, ` 3 ` y `3` | Autorizado; los espacios se quitan |
| `03` | Rechazado: se compara el texto exacto |
| `2;3` | Rechazado: solo la coma separa |
| `ADMINISTRADOR,FARMACEUTICO` | Rechazado: son ids, no nombres |
| `abc,3` y `,3` | Autorizado: lo que no es un id se ignora sin avisar |
| Sin definir, vacía o solo con espacios | Dispensación bloqueada |

---

## 2. Mensajes

| Mensaje | Cuándo aparece | Qué hacer |
|---|---|---|
| `Dispensacion bloqueada: configure CARESTOCK_ROLES_DISPENSACION` | La variable no existe, está vacía o solo tiene espacios | Definirla con los ids de rol |
| `Usuario inactivo o farmacia no autorizada` | El usuario no está `ACTIVO` en una farmacia `ACTIVA` que sea la de la dispensación: está `INACTIVO` o `BLOQUEADO`, la farmacia está `INACTIVA` o el usuario es de otra farmacia | Revisar el estado del usuario y de la farmacia |
| `Su rol no permite dispensar medicamentos` | El usuario es válido, pero su `id_rol` no está en la lista | Agregar su id a la variable o asignarle otro rol |

Se comprueban en ese orden, y el de la variable va antes de mirar al usuario. Los tres son `ReglaNegocioException`, y la pantalla de inventario los muestra en un diálogo de advertencia titulado "ALERTA DE FARMACOVIGILANCIA".

Antes de llegar ahí, la fachada `Inventario` ya rechaza a un usuario o una farmacia inactivos con "Usuario inactivo o sin farmacia activa asignada". Por eso, en la práctica, el segundo mensaje de la tabla solo aparece si el estado cambia entre las dos comprobaciones.

---

## 3. Cómo obtener los ids de rol

Los ids los genera la base de datos, así que se consultan en la base real:

```sql
SELECT id_rol, nombre_rol, descripcion FROM roles ORDER BY id_rol;
```

En una base nueva con los datos semilla son 1 (`SUPER_ADMIN`), 2 (`ADMINISTRADOR`) y 3 (`FARMACEUTICO`), pero en una base con historial pueden ser otros, por ejemplo 105, 106 y 107. Para obtener el valor ya armado:

```sql
SELECT string_agg(id_rol::text, ',' ORDER BY id_rol) FROM roles WHERE nombre_rol IN ('ADMINISTRADOR', 'FARMACEUTICO');
```

Y para ver el rol de un usuario concreto:

```sql
SELECT u.email, r.id_rol, r.nombre_rol FROM usuarios u JOIN roles r ON r.id_rol = u.id_rol WHERE u.email = 'correo@dominio';
```

Qué roles incluir es una decisión del equipo. `ADMINISTRADOR` es el responsable de una farmacia, `FARMACEUTICO` el usuario operativo, y `SUPER_ADMIN` el administrador global; un `SUPER_ADMIN` sin farmacia asignada no puede dispensar aunque su id esté en la lista, porque la fachada lo rechaza antes por no tener farmacia activa.

---

## 4. Dónde se configura

La aplicación solo lee variables de entorno, y ningún código carga un archivo `.env`. Tampoco puede ir en `conexion-local.properties`.

| Cómo se ejecuta | Dónde se define |
|---|---|
| `scripts/run.sh` en Linux, macOS o Git Bash | En el archivo `.env`, que el script carga con `source` |
| PowerShell o `scripts/run.ps1` | Con `$env:CARESTOCK_ROLES_DISPENSACION="2,3"` antes de ejecutar; en Windows `.env` no se carga |
| Docker con `compose.yaml` | Bajo `environment:` del servicio, que hoy solo define `JAVA_TOOL_OPTIONS` |

---

## 5. Estado y propuesta

**La variable no aparece en `.env.example` ni en `docs/user_guide/DEPLOY.md`.** Tampoco en `compose.yaml`, en los scripts `scripts/run.*` y `scripts/setup.*`, en `conf/config.yaml` ni en la guía `docs/user_guide/dispensacion.md`; solo la nombran `FarmacovigilanciaService` y dos documentos de diseño de segregación. Quien siga esas guías y copie `.env.example` queda con la dispensación bloqueada. Ninguna prueba cubre estos tres mensajes.

**Propuesta para agregarla**, sin cambiar nada en este sprint:

1. En `.env.example`, junto a `CARESTOCK_USER_EMAIL`:

```text
# Ids de rol (columna id_rol de la tabla roles) autorizados para dispensar, separados por comas.
# Sin esta variable la dispensación queda bloqueada.
CARESTOCK_ROLES_DISPENSACION=2,3
```

2. En `docs/user_guide/DEPLOY.md`: una viñeta en la lista de variables, `- CARESTOCK_ROLES_DISPENSACION (obligatoria para dispensar; ids de rol separados por comas)`, y una línea en cada bloque, `$env:CARESTOCK_ROLES_DISPENSACION="2,3"` en PowerShell y `export CARESTOCK_ROLES_DISPENSACION='2,3'` en Bash.
3. En `compose.yaml`, bajo `environment:`:

```yaml
CARESTOCK_ROLES_DISPENSACION: "2,3"
```

4. Opcional: nombrarla en el mensaje de `scripts/setup.ps1` y `scripts/setup.sh`, en el comentario de `conf/config.yaml`, y advertir en `scripts/run.ps1` si falta.

Los ids `2,3` son los de una base nueva; hay que reemplazarlos por los de la base real. De paso, `DEPLOY.md` todavía lista `CARESTOCK_USER_EMAIL`, que ya no lee ninguna clase Java.
