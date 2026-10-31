# CareStock - Ampliacion de cobertura y pruebas de regresion

**Fecha:** 31 de octubre de 2026  
**Rama:** test/coverage-50-servicios-criticos  
**Estado:** Pruebas automatizadas aprobadas

## 1. Objetivo

Incrementar la cobertura de codigo de CareStock mediante
JUnit 5, Mockito y JaCoCo, priorizando componentes criticos
de inventario, auditoria, persistencia, alertas y controladores.

La meta global del proyecto es superar el 50% de cobertura
de lineas en SonarCloud.

## 2. Metricas obtenidas

| Indicador | Resultado |
|---|---:|
| Cobertura de referencia | 39.20% |
| Cobertura intermedia | 39.35% |
| Cobertura local final | 40.25% |
| Incremento frente a referencia | +1.05 puntos porcentuales |
| Pruebas ejecutadas | 347 |
| Pruebas fallidas | 0 |
| Errores de prueba | 0 |
| Pruebas omitidas | 0 |
| Clases analizadas por JaCoCo | 146 |
| Compilacion Maven | BUILD SUCCESS |

La cobertura presentada corresponde a JaCoCo local.
La medicion de SonarCloud queda pendiente de verificar
despues de integrar o analizar el Pull Request.

## 3. Herramientas utilizadas

- Java 21
- Maven
- JUnit 5
- Mockito
- JaCoCo 0.8.15
- Git Bash
- GitHub Actions y SonarCloud para validacion posterior

## 4. Pruebas implementadas

### MovimientosTransaccionalesCoberturaTest

- Entradas y salidas de inventario.
- Confirmacion de transacciones.
- Reversion mediante rollback.
- Validacion de permisos por farmacia.
- Manejo de errores de persistencia.

### AjustesTransaccionalesCoberturaTest

- Ajustes de existencias.
- Control de concurrencia.
- Validacion de sesion y farmacia.
- Confirmacion y reversion de transacciones.
- Invalidacion de cache.

### HistorialJdbcCoberturaTest

- Consulta del historial.
- Validacion de paginacion.
- Restriccion por farmacia.
- Verificacion de sesion activa.
- Control de acceso.

### AuditoriaAjustesCoberturaTest

- Registro de movimientos de auditoria.
- Identificacion de categoria.
- Validacion del usuario autorizado.
- Transacciones obligatorias.
- Manejo de errores JDBC.

### DirectorioSmsCoberturaTest

- Consulta de destinatarios.
- Restricciones por rol.
- Segregacion por farmacia.
- Deshabilitacion autorizada.
- Rechazo de operaciones no permitidas.

### GatewaySmsSeguridadCoberturaTest

- Validacion de endpoints.
- Restriccion de destinatarios.
- Validacion de mensajes.
- Rechazo de configuraciones inseguras.

Las pruebas no requieren envios reales de SMS.

### ControladoresLogicaCoberturaTest

- Lectura de atributos de productos.
- Representacion de medicamentos.
- Datos de productos de aseo y maternidad.
- Estados sanitarios de lotes.
- Validacion de roles de ajustes.
- Normalizacion de textos.
- Representacion de horarios SMS.

## 5. Correcciones realizadas durante la ejecucion

Durante el proceso se identificaron problemas de
compilacion y diferencias entre contratos de clases.

Las correcciones abordaron:

- Referencias inconsistentes a clases DAO.
- Nombres de paquetes DTO.
- Referencias de conexion y servicios.
- Contratos de metodos de la fachada.
- Referencias entre FXML y controladores.
- Expectativas JDBC desactualizadas.
- Configuracion de mocks de sesion.
- Simulacion de consultas de alertas.
- Prevencion de ciclos indefinidos en ResultSet.

Estas modificaciones deben revisarse en el diff del
Pull Request para garantizar compatibilidad funcional.

## 6. Metodologia

Se utilizo el patron Arrange-Act-Assert (AAA).

Se incluyeron escenarios exitosos, invalidos,
excepciones, autorizaciones y operaciones transaccionales.

Las dependencias externas se simularon con Mockito.

No se realizaron modificaciones sobre la base
de datos de produccion para ejecutar las pruebas.

## 7. Validacion

Comando:

mvn clean verify

Resultado:

Tests run: 347, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS

Reporte HTML:
src/CareStock/target/site/jacoco/index.html

Reporte CSV:
src/CareStock/target/site/jacoco/jacoco.csv

## 8. Analisis de cobertura pendiente

El paquete controller concentra la mayor parte
de las lineas sin cubrir.

Se identificaron controladores con nombres similares,
por lo que debe comprobarse cuales estan enlazados
realmente a las pantallas FXML.

El avance obtenido confirma una mejora de cobertura,
pero la meta superior al 50% continua pendiente.

## 9. Proximos pasos

1. Validar el Pull Request mediante GitHub Actions.
2. Confirmar las metricas de cobertura en SonarCloud.
3. Revisar posibles regresiones de interfaz JavaFX.
4. Mantener bajo control la duplicacion de codigo.
5. Ampliar las pruebas de controladores activos.
6. Superar el 50% de cobertura global de lineas.

## 10. Conclusion

Se ampliaron las pruebas automatizadas y se
recupero la compilacion del proyecto.

La suite completa finalizo satisfactoriamente
con 347 pruebas aprobadas y 40.25% de cobertura
local de lineas.

El trabajo contribuye a mejorar la confiabilidad,
la trazabilidad y el mantenimiento de CareStock.
