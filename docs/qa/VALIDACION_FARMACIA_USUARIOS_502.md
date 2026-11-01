# Validación de farmacia en creación de usuarios

CareStock   Subissue 502   Versión 1   2 de octubre de 2026

Este documento define las pruebas manuales para verificar la carga de farmacias, la selección de su identificador y la persistencia de la sede del nuevo usuario. También permite registrar el comportamiento ante una lista vacía, una selección ausente y un error de base de datos.

**Estado de ejecución:** Pendiente. Los resultados esperados son criterios de aceptación; cada resultado real debe registrarse después de ejecutar el caso y adjuntar su evidencia.

## Alcance y versión a validar

Se validará el formulario CrearUsuarioFX sobre la versión que integre las subissues 500 y 501. Los siete casos mínimos se ejecutan con una sesión SUPER_ADMIN, que crea ADMINISTRADORES y selecciona su farmacia. El cambio de sede se realiza antes de guardar un nuevo usuario; no corresponde a reasignar un usuario existente.

**Trazabilidad:** [Issue #502](https://github.com/puj-course/FIS_2630_1204_G1/issues/502)

## Preparación de la ejecución

- Registrar responsable, fecha, rama, commit, versión de Java y base de datos de prueba en la sección de registro de resultados.
- Disponer de dos farmacias activas A y B con IDs distintos. Consultar sus IDs reales mediante Q1 y anotarlos antes de iniciar.
- Usar nombres y correos de prueba exclusivos para cada ejecución. Los ejemplos de este documento se deben sustituir si ya existen.
- Completar nombre y correo válidos y una contraseña de al menos ocho caracteres para aislar la validación de farmacia.
- Preparar una base de prueba aislada sin farmacias activas para CP05 y una falla controlada de conexión para CP07. Conservar la configuración normal para recuperarla.

## Relación con el diseño de software

4.1 MBD: los casos contrastan el flujo previsto de selección y guardado. 4.2 Estructura: CrearUsuarioFX utiliza FarmaciaDAO para cargar sedes y UsuarioService y UsuarioDAO para crear el usuario. 4.3 Comportamiento: carga, selección, validación y persistencia, con salidas de error y estado vacío. 4.4 Patrones: la separación de vista, servicio y DAO permite comprobar cada responsabilidad. Este registro vincula las pruebas al diseño; no certifica la actualización de diagramas de la HU.

## Pruebas de carga y persistencia

Ejecutar CP01 a CP04 con farmacias A y B activas. Anotar los IDs reales como ID_A e ID_B. Registrar la evidencia con el código del caso y la fecha de ejecución.

### CP01 Cargar farmacias registradas

**Procedimiento:** Ejecutar Q1. Iniciar sesión como SUPER_ADMIN, abrir Crear usuario y desplegar el ComboBox de farmacia. Comparar las opciones con la consulta.

**Resultado esperado:** El ComboBox está habilitado y muestra las farmacias activas disponibles, con sus IDs y nombres. Las sedes inactivas no aparecen.

**Evidencia:** Captura del ComboBox desplegado y resultado de Q1 del mismo ambiente.

### CP02 Obtener el identificador seleccionado

**Procedimiento:** Seleccionar la farmacia A. Comparar su ID visible con Q1. En el depurador, detener la ejecución después de farmacia.getIdFarmacia() en intentarCrearUsuario y revisar idFarmaciaSeleccionada.

**Resultado esperado:** La selección contiene la farmacia A e idFarmaciaSeleccionada coincide con ID_A. El valor corresponde al identificador, no al índice de la opción.

**Evidencia:** Captura de la selección y del valor de idFarmaciaSeleccionada en el depurador. Continuar o cancelar sin crear un usuario adicional para este caso.

### CP03 Persistir la farmacia seleccionada

**Procedimiento:** Completar los datos de un usuario nuevo con correo qa502.a@example.com, seleccionar A y pulsar Crear usuario. Ejecutar Q2 con ese correo.

**Resultado esperado:** Se informa la creación correcta. Q2 devuelve exactamente una fila y USUARIOS.id_farmacia coincide con ID_A; la farmacia consultada corresponde a A.

**Evidencia:** Captura del formulario antes de guardar, confirmación de creación y resultado de Q2 con el ID persistido.

### CP04 Persistir la última farmacia seleccionada

**Procedimiento:** Abrir un formulario nuevo con correo qa502.b@example.com. Seleccionar primero A y después B, antes de pulsar Crear usuario. Guardar y ejecutar Q2 para ese correo.

**Resultado esperado:** Q2 devuelve exactamente una fila con id_farmacia igual a ID_B y distinto de ID_A. La selección anterior de A no se conserva como destino.

**Evidencia:** Captura de la selección final B y resultado de Q2 para el segundo usuario.

**Registro:** Anotar el resultado real y la ruta de los archivos de evidencia en la sección de registro de resultados. Si un resultado difiere de lo esperado, marcar Fallido y describir la diferencia.

## Pruebas de estados vacíos y errores

### CP05 Abrir el formulario sin farmacias

**Procedimiento:** Usar un ambiente aislado donde Q1 devuelva cero filas. Iniciar sesión como SUPER_ADMIN y abrir Crear usuario. Revisar el ComboBox, el mensaje y el botón de guardado.

**Resultado esperado:** El ComboBox está deshabilitado, se muestra “No hay sedes registradas.” y Crear usuario está deshabilitado. La ventana permanece abierta.

**Evidencia:** Captura completa del formulario y resultado vacío de Q1. No eliminar sedes del ambiente compartido para preparar este caso.

### CP06 Intentar crear sin selección

**Procedimiento:** En un ambiente con sedes activas, abrir el formulario sin elegir farmacia. Completar los demás campos con correo qa502.sinseleccion@example.com. Confirmar con Q2 que el correo no existe, pulsar Crear usuario y repetir Q2.

**Resultado esperado:** Se informa que debe seleccionarse la farmacia del nuevo administrador. No ocurre NullPointerException y Q2 continúa devolviendo cero filas.

**Evidencia:** Captura del mensaje y consultas Q2 antes y después del intento.

### CP07 Manejar un error de base de datos

**Procedimiento:** Con la sesión SUPER_ADMIN iniciada en el ambiente de prueba, interrumpir la conexión de forma controlada. Abrir Crear usuario para provocar el error de carga. En otra ejecución, cargar y seleccionar A antes de interrumpir la conexión e intentar guardar qa502.errorbd@example.com. Restablecer la conexión y ejecutar Q2.

**Resultado esperado:** La aplicación permanece abierta y muestra feedback del error. Durante el fallo de carga, el ComboBox y el guardado quedan bloqueados. Durante el fallo de creación, no se anuncia éxito y Q2 devuelve cero filas tras restablecer la conexión.

**Evidencia:** Capturas de ambas variantes y resultado de Q2 tras la recuperación. Si aparece un registro, documentarlo como fallo; no repetir el guardado sin consultar primero.

## Comprobaciones adicionales recomendadas

Recuperación del estado vacío: en el ambiente de CP05, crear la primera farmacia desde el formulario. Verificar que se habilitan el ComboBox y Crear usuario y que la nueva sede queda seleccionada.

Asignación automática: con un ADMINISTRADOR que tenga farmacia, crear un FARMACÉUTICO y verificar por Q2 que hereda la sede del administrador. Registrar estas comprobaciones por separado de los siete casos mínimos.

## Consultas y registro de resultados

Ejecutar las consultas en la misma base de datos utilizada por CareStock. Registrar el ID observado y compararlo con ID_A o ID_B. Las consultas son de lectura y no incluyen contraseñas.

### Q1 Consultar farmacias activas

```sql
SELECT id_farmacia, codigo, nombre, estado
FROM farmacias
WHERE estado = 'ACTIVA'
ORDER BY nombre ASC;
```

### Q2 Consultar el usuario de prueba

Sustituir el correo por el del caso ejecutado. Para CP03 y CP04 se espera una fila; para CP06 y el fallo de creación de CP07 se esperan cero filas.

```sql
SELECT u.id_usuario, u.email, u.id_farmacia,
       f.codigo, f.nombre AS farmacia, f.estado
FROM usuarios u
LEFT JOIN farmacias f ON f.id_farmacia = u.id_farmacia
WHERE LOWER(u.email) = LOWER('qa502.a@example.com');
```

**Responsable y fecha:** __________________________________________

**Rama y commit:** ______________________________________________

**Ambiente y versión de Java:** __________________________________

**Datos de prueba:** ID_A ______  ID_B ______  Correos __________________

| Caso | Estado | Resultado real y archivo de evidencia |
| --- | --- | --- |
| CP01 | Pendiente | Por completar |
| CP02 | Pendiente | Por completar |
| CP03 | Pendiente | Por completar |
| CP04 | Pendiente | Por completar |
| CP05 | Pendiente | Por completar |
| CP06 | Pendiente | Por completar |
| CP07 | Pendiente | Por completar |

Estados: Aprobado, Fallido o Pendiente. Para CP07 registrar ambas variantes. Identificar las evidencias como CP01_fecha, CP02_fecha, etc., y adjuntarlas al PR o indicar su ruta verificable.

### Criterio de cierre

Cerrar la subissue 502 cuando los siete casos estén aprobados, cada resultado real tenga evidencia y la ejecución identifique el commit validado. La elaboración del documento deja definido el procedimiento de prueba; la validación queda completa al registrar los resultados de su ejecución.
