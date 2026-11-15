# Matriz de permisos por rol

Este documento reúne lo que puede hacer cada rol en las pantallas y acciones de inventario, el caso del rol Auxiliar, las contradicciones entre roles y documentos, y una propuesta pendiente de aprobación del equipo. No se implementa nada. Cada celda de la matriz se comprobó contra el código y los scripts del repositorio en el commit `006e676` (12 de noviembre de 2026).

---

## 1. La matriz

**Sí\*** significa permitido solo con una farmacia asignada. El `SUPER_ADMIN` que deja el script `05` (promueve a Admin CareStock y deja su farmacia en `NULL`) no tiene farmacia, y casi todas las pantallas la exigen. La columna **Auxiliar** describe a un usuario cuyo rol no es ninguno de los tres, porque el Auxiliar no existe como rol (sección 2).

| Acción | SUPER_ADMIN | ADMINISTRADOR | FARMACEUTICO | Auxiliar | Dónde se comprueba |
|---|---|---|---|---|---|
| Consultar la disponibilidad | Sí\* | Sí | Sí | Sí\* | `CareStockApp.mostrarConsultaUnificada` pide solo sesión; `Inventario.farmaciaActual` pide usuario activo con farmacia activa |
| Editar el catálogo | No | Sí | No | No | `SesionUsuario.esAdministrador()` en `CareStockApp.abrirFormularioCatalogo`, `ControladorCatalogo` y `ConsultaInventarioController` |
| Crear, editar y retirar lotes | Sí\* | Sí | Sí | No | `ServicioCrudLotes.rolGestor` en `actor()` y en `validarActor`, que lo repite en la base; botones de `ControladorInventario` |
| Dispensar | Variable | Variable | Variable | Variable | `FarmacovigilanciaService.validarOperador` y la variable `CARESTOCK_ROLES_DISPENSACION` |
| Registrar movimientos | Sí\* | Sí | Sí | No | `PoliticaAccionesAlertas.gestorPermitido`, que usa `rolGestor`, en `CareStockApp.mostrarFormularioMovimientos`, `ControladorMovimientos` y `ServicioMovimientosProducto` |
| Ver el historial | No | Sí | Sí | No | `CareStockApp.mostrarHistorialMovimientos`, `ControladorHistorialMovimientos` y el botón de `ControladorInventario` |
| Hacer ajustes | Sí\* | Sí | No | No | `CareStockApp.mostrarFormularioAjustes`, `ControladorAjustes` y `ServicioAjustesInventario`, que lo repite en SQL |
| Ver las alertas | Sí\* | Sí | Sí | Sí\* | `CareStockApp.mostrarAlertas`: sesión y farmacia asignada |
| Gestionar alertas | Sí\* | Sí | Sí | No | `PoliticaAccionesAlertas.disponible` y `exigir`: rol gestor y alerta de la misma farmacia |
| Ver la reposición | No | Sí | No | No | `CareStockApp.mostrarReposicion`; la exportación, `PermisosExportacionReposicion.exigirAdministrador` |
| Configurar el SMS | No | Sí | No | No | `ValidacionFormularioSms.puedeAcceder`, `CareStockApp.mostrarConfiguracionSms`, `ServicioPreferenciasSms`, `RepositorioPreferenciasSmsJdbc` y `ServicioDirectorioSms` |

- **Variable** en "Dispensar": un usuario dispensa solo si su `id_rol` (el número, no el nombre) está en `CARESTOCK_ROLES_DISPENSACION`, y si es un usuario activo de una farmacia activa. Sin la variable, nadie dispensa: el mensaje es "Dispensacion bloqueada: configure CARESTOCK_ROLES_DISPENSACION".
- `esAdministrador()` es verdadero solo para `ADMINISTRADOR`. Por eso `SUPER_ADMIN` queda fuera del catálogo, la reposición y el SMS.
- Se llega a Movimientos solo desde la acción de una alerta de aseo o maternidad. El método `onAbrirMovimientos` del inventario no tiene botón.
- Los botones "Reposición", "Sugerir lote" y "Dispensar medicamento" los ven todos los roles; el rechazo llega después, con un aviso.

---

## 2. El rol Auxiliar

**No existe como rol.** Solo aparece en el comentario de una columna de dos scripts antiguos, fuera de `src/sql/ddl/` y del CI: `src/sql/medicamentos.sql` (línea 35) y `src/sql/carestock_mermas_shrinkage1.sql` (línea 200), con el texto "Usuario (Auxiliar de Farmacia) que registró el medicamento". Los scripts que siembran roles solo crean `SUPER_ADMIN`, `ADMINISTRADOR` y `FARMACEUTICO`, y ninguna clase de Java lo nombra.

**No se puede crear con la función de usuarios.** `fn_crear_usuario_jerarquico` no recibe un rol de destino: lo decide el rol de quien la llama. Lo ejecuté en una base creada con todos los scripts:

| Quién llama | Resultado |
|---|---|
| `SUPER_ADMIN`, con una farmacia | Crea un `ADMINISTRADOR` en esa farmacia |
| `SUPER_ADMIN`, sin farmacia | Error: "SUPER_ADMIN debe seleccionar una farmacia" |
| `ADMINISTRADOR` | Crea un `FARMACEUTICO` en su farmacia; en otra, error: "Un administrador no puede crear usuarios en otra farmacia" |
| `FARMACEUTICO` | Error: "El rol FARMACEUTICO no está autorizado para crear usuarios" |
| Un `AUXILIAR` insertado a mano | Error: "El rol AUXILIAR no está autorizado para crear usuarios" |

**Qué hace el sistema con un rol que no es uno de los tres.** Inserté a mano un rol `AUXILIAR` y un usuario con farmacia, y revisé el código:

- **Puede iniciar sesión:** la consulta de `UsuarioDAOImpl` busca por correo y estado, sin mirar el rol. La consulta de `SesionUsuario` carga el nombre del rol tal cual (`AUXILIAR`), o `Rol #N` si no tuviera nombre.
- **Ve y usa lo que no pide rol:** el inventario con sus lotes, la consulta de disponibilidad, las alertas y el catálogo, este último en "Modo consulta: solo ADMINISTRADOR puede modificar productos.".
- **El resto se le rechaza,** porque todas las reglas comparan con los tres nombres. No ve los botones de lotes, historial, ajustes ni SMS. Los de "Reposición" y "Dispensar medicamento" sí los ve, y responden con un aviso, como "Solo administradores con farmacia activa pueden consultar la reposición." o "Su rol no permite dispensar medicamentos".
- **Una excepción:** dispensar compara el `id_rol`, no el nombre. Si quien despliega el sistema agrega el número de ese rol a `CARESTOCK_ROLES_DISPENSACION`, ese usuario dispensa.
- **La base también lo rechaza** en `fn_validar_gestion_farmacia`: "El rol AUXILIAR no está autorizado para gestionar inventario".

---

## 3. Contradicciones

### Entre los roles y dentro del código

| Qué pasa | Detalle |
|---|---|
| El historial no admite `SUPER_ADMIN` | Solo `ADMINISTRADOR` y `FARMACEUTICO`, en la navegación, el controlador y el botón |
| Los ajustes no admiten `FARMACEUTICO` | Solo `ADMINISTRADOR` y `SUPER_ADMIN`, en la navegación, el controlador, el servicio y el SQL |
| El catálogo, la reposición y el SMS excluyen a `SUPER_ADMIN` | Son solo de `ADMINISTRADOR`, porque `esAdministrador()` no incluye a `SUPER_ADMIN` |
| `SUPER_ADMIN` casi no puede operar | Lotes, movimientos, alertas, ajustes y consulta piden farmacia, y el que deja el script `05` no la tiene |
| Mensajes que no coinciden con la regla | Lotes dice "Solo administradores con farmacia pueden gestionar lotes", pero también pasan `FARMACEUTICO` y `SUPER_ADMIN`. Movimientos dice "Solo administradores y farmacéuticos...", pero también pasa `SUPER_ADMIN` |
| Hay controles solo en la pantalla | Lotes, ajustes, movimientos, exportación de reposición y SMS se repiten en el servicio. El catálogo y el historial se comprueban solo en la pantalla, porque sus DAO no miran el rol |
| Las funciones `fn_*_seguro` del script 06 no se usan | El código Java no las llama. Permiten `SUPER_ADMIN` (cualquier farmacia) y `ADMINISTRADOR` (la suya) y rechazan `FARMACEUTICO`, que en Java sí gestiona lotes |
| Dispensar usa números y no tiene valor por defecto | `.env.example` y `DEPLOY.md` no mencionan `CARESTOCK_ROLES_DISPENSACION`; solo lo hacen el código y los documentos de la HU-147 |

### Entre `documentacion-roles.md` (22 de septiembre de 2026) y el sistema

| El documento dice | El sistema hoy |
|---|---|
| Hay cinco roles: Administrador, Encargado de Supervisión General, Jefe de Farmacia, Auxiliar de Farmacia y Auditor | Hay tres: `SUPER_ADMIN`, `ADMINISTRADOR` y `FARMACEUTICO`. No existen los otros |
| El Auxiliar registra entradas de lotes | Crear lotes lo hacen `ADMINISTRADOR`, `FARMACEUTICO` y `SUPER_ADMIN` con farmacia |
| El Auxiliar despacha con regla FEFO | Quién dispensa lo decide la variable de roles. FEFO existe solo como el botón "Sugerir lote" (HU-152); el usuario elige y confirma el lote |
| El Auxiliar busca por nombre o principio activo | La consulta busca por código o nombre; no por principio activo |
| El Auxiliar consulta lotes y ve el panel de alertas | Coincide, y lo puede hacer cualquier usuario con farmacia |
| El Auxiliar no modifica el stock mínimo | Se edita en el catálogo, que es solo de `ADMINISTRADOR`: coincide, pero por esa regla y no por una del Auxiliar |
| El Administrador gestiona usuarios (crear, editar, desactivar) y asigna roles | No hay pantalla de usuarios; solo la función SQL que crea `ADMINISTRADOR` y `FARMACEUTICO`, sin editar ni desactivar |
| El Administrador consulta el registro de auditoría y genera reportes | No hay pantalla de auditoría ni de reportes; solo el historial de movimientos |
| El Administrador "no tiene restricciones" | Opera solo su farmacia y no ve nada de otras |
| El Jefe de Farmacia aprueba pedidos, gestiona proveedores y parametriza alertas de 30 y 60 días | No hay pantallas ni tablas de pedidos de compra ni de proveedores. Los umbrales de alertas están fijos en la vista `vw_alertas_inventario` |
| El Auditor consulta el historial de solo lectura | No existe ese rol; el historial lo ven `ADMINISTRADOR` y `FARMACEUTICO`, que además operan |
| El Encargado de Supervisión General ve todas las sedes | `SUPER_ADMIN` es global, pero sin farmacia no abre las pantallas de inventario |

---

## 4. Propuesta

**Estado: pendiente de aprobación del equipo.**

| Opción | Qué implica | Qué corrige |
|---|---|---|
| **A. Retirar al Auxiliar de la documentación** | Reescribir `documentacion-roles.md` con los tres roles reales y esta matriz, y quitar el comentario de los dos scripts antiguos | Las contradicciones entre el documento y el sistema; no toca el código |
| **B. Crear el rol Auxiliar** | Sembrarlo en `roles`; cambiar `fn_crear_usuario_jerarquico` para que el `ADMINISTRADOR` pueda elegirlo como destino; definir sus permisos, por ejemplo consultar, ver alertas y dispensar (agregando su `id_rol` a la variable) sin gestionar lotes | Que el rol del documento exista; exige cambios en SQL, en la función y en la documentación |
| **C. Ordenar los permisos** | Reunir las reglas en una sola clase y no repetir cadenas en ocho archivos; corregir los dos mensajes; decidir si `SUPER_ADMIN` recibe una farmacia o si las pantallas dejan de exigirla; documentar `CARESTOCK_ROLES_DISPENSACION` en `.env.example` y `DEPLOY.md` | Las contradicciones de la sección 3 entre roles |
