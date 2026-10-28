
---

## 10. Resultados de implementacion y validacion

**Fecha:** 28 de octubre de 2026

**Issue:** #928

**Rama Git:** `feature/issue-928-formulario-preferencias-sms`

**Entorno PostgreSQL de pruebas:** `qa-issue-928`

### 10.1. Componentes implementados

Se desarrollo el formulario JavaFX para administrar
destinatarios y preferencias SMS desde el dashboard.

Componentes incorporados:

- `ControladorPreferenciasSms`: controlador JavaFX.
- `ValidacionFormularioSms`: validaciones de formulario.
- `ServicioDirectorioSms`: listado y deshabilitacion.
- `preferencias-sms-view.fxml`: interfaz grafica.
- Integracion con `ServicioPreferenciasSms`.
- Acceso desde `InventarioController`.
- Navegacion mediante `CareStockApp`.

La interfaz utiliza los servicios de negocio
y no ejecuta operaciones SQL directamente.

### 10.2. Funcionalidades implementadas

- Acceso restringido a administradores.
- Consulta de destinatarios por farmacia.
- Seleccion de usuarios autorizados.
- Registro y modificacion de telefonos E.164.
- Visualizacion del estado de consentimiento.
- Preferencias independientes de stock y vencimientos.
- Configuracion de horarios y zona horaria.
- Guardado mediante tareas JavaFX asincronas.
- Deshabilitacion de destinatarios.
- Mensajes de validacion y errores controlados.
- Actualizacion de configuraciones persistidas.

### 10.3. Pruebas TDD

#### RED

Se ejecutaron las pruebas antes de implementar
`ValidacionFormularioSms`.

Resultado observado:

- BUILD FAILURE.
- Codigo de salida: 1.
- Error esperado: clase de validacion inexistente.

Evidencia:

`docs/qa/evidence/issue-928-red.txt`

#### GREEN

Se implemento `ValidacionFormularioSms`.

Resultado observado:

- Pruebas ejecutadas: 10.
- Fallos: 0.
- Errores: 0.
- Pruebas omitidas: 0.
- BUILD SUCCESS.
- Codigo de salida: 0.

Evidencia:

`docs/qa/evidence/issue-928-green-validaciones.txt`

### 10.4. Correcciones durante la integracion

Durante las verificaciones se identificaron y
corrigieron los siguientes problemas:

1. Anotaciones `@FXML` duplicadas en el controlador.
2. Clase principal desactualizada en `pom.xml`.
3. Metodo `onAbrirNotificaciones` faltante.
4. Manejadores de filtros del dashboard faltantes.
5. Captura de errores durante la navegacion.
6. Configuracion incorrecta del entorno de pruebas
   que apuntaba inicialmente a `production`.

Se agrego `DashboardFxmlHandlersTest` para comprobar
la correspondencia entre los metodos referenciados
por el FXML y su controlador.

### 10.5. Validacion funcional de JavaFX

Se verifico visualmente la apertura del dashboard
despues del inicio de sesion.

Se comprobo que el dashboard muestra:

- Informacion del usuario autenticado.
- Rol del usuario.
- Farmacia asignada.
- Indicadores de inventario.
- Boton SMS para administradores.

Tambien se comprobo la apertura del formulario
de preferencias SMS.

La consulta inicial presento un error debido a
la conexion utilizada durante las pruebas.

Se identifico que la conexion apuntaba a
`production`, en lugar de la rama QA.

Posteriormente se configuro la conexion de pruebas.

El usuario reporto que el funcionamiento del
formulario fue satisfactorio tras corregir
la configuracion.

Los resultados individuales de UI-01 a UI-12
deberan conservar evidencias especificas cuando
se requiera una verificacion formal completa.

### 10.6. Seguridad

Se establecieron las siguientes medidas:

- Restriccion de acceso por rol.
- Uso de la farmacia de la sesion autenticada.
- Validaciones adicionales en los servicios.
- Confirmacion explicita de consentimiento.
- Rechazo de telefonos invalidos.
- Segregacion de destinatarios por farmacia.
- Consultas y escrituras asincronas.
- Manejo controlado de errores PostgreSQL.

Las pruebas deben ejecutarse sin enviar SMS reales.

### 10.7. Regresion

Comando:

`./mvnw clean verify`

Se obtuvo BUILD SUCCESS despues de corregir
la anotacion FXML duplicada.

Se requiere conservar el resultado de una ultima
ejecucion de regresion posterior a las correcciones
de los manejadores FXML.

Evidencia:

`docs/qa/evidence/issue-928-regresion.txt`

### 10.8. Evidencias visuales

Durante el desarrollo se observaron:

- Inicio de sesion de CareStock.
- Dashboard cargado correctamente.
- Boton SMS integrado.
- Formulario JavaFX de preferencias.
- Mensaje de error de consulta, posteriormente
  diagnosticado.

Las capturas compartidas durante la revision
no se consideran automaticamente archivos
versionados en el repositorio.

### 10.9. Validaciones de cierre

- [x] Plan de pruebas definido.
- [x] Fase RED ejecutada.
- [x] Fase GREEN ejecutada.
- [x] Diez pruebas de validacion satisfactorias.
- [x] Formulario JavaFX implementado.
- [x] Integracion con dashboard.
- [x] Correccion de errores de navegacion.
- [x] Apertura visual del dashboard.
- [x] Apertura visual del formulario SMS.
- [x] Conexion de pruebas diagnosticada y corregida.
- [ ] Evidencias individuales de UI-01 a UI-12 completas.
- [ ] Pruebas de dos farmacias documentadas individualmente.
- [ ] Regresion final posterior a las ultimas correcciones.
- [ ] Pull Request aprobado e integrado.

### 10.10. Conclusion

Se implemento la interfaz JavaFX de administracion
de destinatarios y preferencias SMS de CareStock.

Las pruebas de validacion finalizaron correctamente
y se corrigieron los problemas de integracion
detectados durante la ejecucion.

La implementacion se encuentra preparada para la
verificacion final de regresion y su integracion
mediante Pull Request.
