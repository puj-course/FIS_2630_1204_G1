# CareStock - Informe de ampliación de cobertura de pruebas

**Proyecto:** CareStock  
**Fecha:** 30 de octubre de 2026  
**Rama:** `test/coverage-55-sms-inventario`  
**Responsable:** Equipo de desarrollo CareStock  
**Estado:** Ejecución local satisfactoria  
**Objetivo:** Incrementar el Coverage hasta el 50-55 % en SonarCloud.

---

## 1. Introducción

Como parte del Plan Maestro de Pruebas de Software de CareStock,
se desarrolló un nuevo bloque de pruebas automatizadas orientado
a incrementar la cobertura del código de producción y fortalecer
la validación de funcionalidades críticas.

Se priorizaron los módulos de persistencia JDBC, preferencias
de notificación SMS, alertas automáticas, consultas de inventario
y la fachada de inventario.

Las pruebas se implementaron mediante JUnit 5 y Mockito,
siguiendo el patrón Arrange-Act-Assert (AAA).

El trabajo se realizó en una rama independiente, utilizando
Git Bash para crear y ejecutar las pruebas.

No se utilizaron archivos de parche ni paquetes ZIP para
implementar los cambios.

## 2. Objetivos

### 2.1. Objetivo general

Incrementar la cobertura automatizada del proyecto CareStock,
fortaleciendo la verificación de operaciones críticas de negocio,
persistencia, seguridad de acceso y manejo de errores.

### 2.2. Objetivos específicos

- Aumentar la cobertura de líneas ejecutables.
- Verificar operaciones de persistencia JDBC.
- Validar restricciones de acceso por farmacia.
- Comprobar reglas de autorización para destinatarios SMS.
- Verificar transacciones y operaciones de rollback.
- Evaluar el funcionamiento de alertas automáticas.
- Comprobar el manejo de errores de base de datos.
- Verificar las reglas de negocio de la fachada Inventario.
- Evaluar la invalidación de caché después de operaciones.
- Mantener la compatibilidad con las pruebas existentes.
- Medir los resultados mediante JaCoCo y Maven.

## 3. Justificación de las clases seleccionadas

La selección de clases se basó en dos criterios principales:

1. Importancia funcional del componente.
2. Oportunidad de cubrir rutas de código poco ejecutadas.

Se priorizaron componentes que participan directamente
en procesos de inventario, notificaciones y seguridad.

| Componente | Justificación |
|---|---|
| RepositorioPreferenciasSmsJdbc | Gestiona la persistencia y autorización de destinatarios SMS. |
| MedicamentoDAOImpl | Implementa operaciones fundamentales de persistencia de medicamentos. |
| ConsultaInventarioDAO | Consolida información de inventario por farmacia. |
| ServicioAlertasSms | Gestiona eventos críticos, destinatarios y procesamiento de alertas. |
| Inventario | Centraliza operaciones y reglas de negocio mediante el patrón Facade. |

Estas clases permiten verificar funcionalidades relevantes
sin depender directamente de la interfaz gráfica JavaFX.

También presentan rutas que pueden ejercitarse mediante
simulaciones controladas de sus dependencias.

## 4. Herramientas utilizadas

| Herramienta | Propósito |
|---|---|
| Java 21 | Lenguaje y plataforma de ejecución. |
| Maven | Compilación y automatización de pruebas. |
| JUnit 5 | Definición y ejecución de pruebas unitarias. |
| Mockito | Simulación de dependencias y objetos JDBC. |
| Maven Surefire 3.2.5 | Ejecución de pruebas automatizadas. |
| JaCoCo 0.8.15 | Medición de cobertura del código. |
| Git Bash | Creación de archivos y ejecución de comandos. |
| Git | Control de versiones mediante ramas. |
| SonarCloud | Plataforma de análisis de calidad del código. |

## 5. Metodología de pruebas

### 5.1. Patrón Arrange-Act-Assert

Se utilizó el patrón Arrange-Act-Assert (AAA)
para estructurar las pruebas.

**Arrange: Preparación**

Se construyen los objetos del dominio y se configuran
los mocks necesarios para simular las dependencias.

**Act: Ejecución**

Se invoca el método del componente que será evaluado.

**Assert: Verificación**

Se comprueban resultados, excepciones e interacciones
mediante las aserciones de JUnit y las verificaciones
de Mockito.

### 5.2. Aislamiento de dependencias

Se utilizaron objetos simulados de:

- Connection.
- PreparedStatement.
- ResultSet.
- Servicios SMS.
- DAO de productos.
- Proxy de inventario.
- Sesión de usuario.
- Servicios estáticos de conexión.

Esto permitió ejecutar las pruebas sin realizar cambios
en una base de datos de producción ni enviar SMS reales.

### 5.3. Tipos de escenarios

Se incluyeron escenarios de:

- Ejecución satisfactoria.
- Entradas inválidas.
- Registros inexistentes.
- Restricciones por farmacia.
- Errores SQL.
- Actualización de datos.
- Reversión de transacciones.
- Validación de existencias.
- Preferencias de notificación.
- Invalidación de caché.
- Manejo de sesiones y autorizaciones.

## 6. Pruebas implementadas

En esta rama se incorporaron 54 pruebas automatizadas
distribuidas en cinco clases.

### 6.1. RepositorioPreferenciasSmsJdbcCoberturaTest

**Ubicación:**

`src/test/java/org/example/carestock/sms/RepositorioPreferenciasSmsJdbcCoberturaTest.java`

**Total: 11 pruebas.**

| ID | Caso de prueba | Validación |
|---|---|---|
| SMS-01 | consultaCuentaActiva | Consulta de usuario activo y farmacia. |
| SMS-02 | cuentaInexistenteDevuelveNull | Manejo de cuentas inexistentes. |
| SMS-03 | consultaConfiguracionConHorarios | Recuperación de preferencias y horarios. |
| SMS-04 | consultaConfiguracionInexistente | Manejo de configuración inexistente. |
| SMS-05 | insertaNuevoDestinatarioAutorizado | Registro autorizado y confirmación de transacción. |
| SMS-06 | actualizaDestinatarioExistente | Actualización de preferencias existentes. |
| SMS-07 | reactivaDestinatarioInactivo | Recuperación de un destinatario previamente inactivo. |
| SMS-08 | rechazaAdministradorNoAutorizado | Restricción de operaciones sin autorización. |
| SMS-09 | rechazaDestinatarioDeOtraFarmacia | Validación del aislamiento entre farmacias. |
| SMS-10 | revierteSiFallaInsercion | Rollback ante un error de inserción. |
| SMS-11 | revierteSiActualizacionNoAfectaFilas | Rollback cuando la actualización no modifica registros. |

**Resultado: 11 pruebas aprobadas.**

Durante la primera ejecución se identificó una
configuración incorrecta de Mockito en la simulación
de usuarios pertenecientes a diferentes farmacias.

Se corrigieron los valores simulados sin modificar
el código de producción.

La ejecución final fue satisfactoria.

### 6.2. MedicamentoDaoCoberturaTest

**Ubicación:**

`src/test/java/org/example/carestock/dao/MedicamentoDaoCoberturaTest.java`

**Total: 12 pruebas.**

| ID | Caso de prueba | Validación |
|---|---|---|
| MED-01 | guardaMedicamentoYRecuperaId | Inserción y recuperación del identificador generado. |
| MED-02 | guardaMedicamentoConCamposOpcionalesNulos | Tratamiento de valores opcionales nulos. |
| MED-03 | rechazaMedicamentoInvalidoAntesDeGuardar | Validación previa a la persistencia. |
| MED-04 | buscaMedicamentoPorIdYMapeaCampos | Conversión de registros SQL a objetos del dominio. |
| MED-05 | buscaCodigoInvimaInexistente | Consulta sin resultados. |
| MED-06 | listaMedicamentosDisponibles | Recuperación de registros de medicamentos. |
| MED-07 | listaCategorias | Consulta del catálogo de categorías. |
| MED-08 | relacionaMedicamentosConCategorias | Asociación entre medicamentos y categorías. |
| MED-09 | actualizaEstadoMedicamento | Modificación del estado registrado. |
| MED-10 | cuentaMedicamentosPorCategoria | Conteo agrupado de medicamentos. |
| MED-11 | mapeaCamposOpcionalesNulos | Conversión de campos nulos del ResultSet. |
| MED-12 | propagaErrorDeConsultaSql | Propagación de errores SQL. |

**Resultado: 12 pruebas aprobadas.**

Se verificaron operaciones de persistencia,
consultas y conversión de resultados JDBC.

### 6.3. ConsultaInventarioDaoCoberturaTest

**Ubicación:**

`src/test/java/org/example/carestock/dao/ConsultaInventarioDaoCoberturaTest.java`

**Total: 5 pruebas.**

| ID | Caso de prueba | Validación |
|---|---|---|
| INV-01 | rechazaFarmaciaInvalida | Rechazo de identificadores de farmacia inválidos. |
| INV-02 | consultaInventarioVacio | Consulta que no devuelve productos. |
| INV-03 | listaTresCategoriasEnUnaFarmacia | Consulta consolidada de medicamentos, aseo y maternidad. |
| INV-04 | rechazaResultadoConExistenciaNegativa | Manejo de existencias inválidas. |
| INV-05 | propagaErroresDeBaseDeDatos | Propagación de excepciones SQL. |

**Resultado: 5 pruebas aprobadas.**

Se comprobó la utilización de parámetros por farmacia
y el manejo de resultados para diferentes categorías.

### 6.4. ServicioAlertasSmsCoberturaTest

**Ubicación:**

`src/test/java/org/example/carestock/sms/ServicioAlertasSmsCoberturaTest.java`

**Total: 10 pruebas.**

| ID | Caso de prueba | Validación |
|---|---|---|
| ALT-01 | rechazaDependenciasNulas | Validación de dependencias obligatorias. |
| ALT-02 | cicloSinEventosFinalizaCorrectamente | Ejecución cuando no existen alertas. |
| ALT-03 | registraAlertaDeStockAutorizada | Registro de evento crítico para destinatario autorizado. |
| ALT-04 | noRegistraStockSiPreferenciaEstaDesactivada | Respeto por preferencias de notificación. |
| ALT-05 | noMezclaEventosDeFarmaciasDistintas | Separación lógica entre farmacias. |
| ALT-06 | registraAlertaDeLoteVencido | Generación de alertas por vencimiento. |
| ALT-07 | revierteSincronizacionSiFallaConsulta | Rollback ante errores de consulta. |
| ALT-08 | revierteSiPlantillaSmsEsInvalida | Manejo de configuraciones de mensaje inválidas. |
| ALT-09 | rechazaLimiteDeEnviosFueraDeRango | Validación del límite de envíos. |
| ALT-10 | reclamaAlertaSinEnviarSiYaNoExiste | Procesamiento de eventos que dejan de existir. |

**Resultado: cobertura de líneas del 100 %.**

JaCoCo registró 101 líneas cubiertas de 101
líneas ejecutables para la clase ServicioAlertasSms.

Este resultado corresponde específicamente a
cobertura de líneas y no certifica cobertura
total de ramas, condiciones o combinaciones.

### 6.5. InventarioFachadaCoberturaTest

**Ubicación:**

`src/test/java/org/example/carestock/facade/InventarioFachadaCoberturaTest.java`

**Total: 16 pruebas.**

| ID | Caso de prueba | Validación |
|---|---|---|
| FAC-01 | rechazaUsuarioNoAutenticado | Validación de usuario obligatorio. |
| FAC-02 | rechazaCambioDeSesion | Restricción de operaciones ante cambios de sesión. |
| FAC-03 | rechazaUsuarioSinFarmaciaActiva | Validación de farmacia activa. |
| FAC-04 | buscaAseoDeFarmaciaActiva | Consulta de productos en la farmacia correspondiente. |
| FAC-05 | registraAseoEInvalidaCache | Registro e invalidación de caché. |
| FAC-06 | noInvalidaCacheSiRegistroFalla | Conservación de caché ante errores. |
| FAC-07 | impideDesactivarAseoConExistencias | Protección de productos con stock. |
| FAC-08 | impideDesactivarMaternidadConExistencias | Restricción de desactivación con existencias. |
| FAC-09 | listaMedicamentosConSuEstado | Consulta de medicamentos y estados. |
| FAC-10 | rechazaMedicamentoSinCategoria | Validación de reglas del catálogo. |
| FAC-11 | cuentaProductosPorCategoria | Conteo por categoría de inventario. |
| FAC-12 | consultaInventarioSinInvalidarCache | Consulta sin invalidación innecesaria. |
| FAC-13 | refrescaInventarioInvalidandoCache | Actualización de la información almacenada. |
| FAC-14 | consultaMedicamentosConStockBajo | Identificación de medicamentos con stock mínimo. |
| FAC-15 | obtieneResumenDeAlertasPorFarmacia | Consulta de indicadores de alerta. |
| FAC-16 | obtieneListaDeAlertasDeStockYVencimiento | Recuperación de alertas de inventario. |

**Resultado: 16 pruebas aprobadas.**

Durante la primera ejecución se presentó un problema
de configuración de Mockito.

El método InventarioConsultas.compartida()
requiere un objeto ConsultaInventarioProxy.

La prueba utilizaba inicialmente un mock de tipo
ConsultaInventario.

Se corrigió el tipo simulado sin modificar
el código de producción.

La segunda ejecución confirmó:

```text
Tests run: 16, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

La cobertura final de la fachada Inventario fue:

**118 de 173 líneas ejecutables: 68,21 %.**

## 7. Resultados generales

### 7.1. Distribución de las nuevas pruebas

| Clase de prueba | Cantidad | Resultado |
|---|---:|---|
| RepositorioPreferenciasSmsJdbcCoberturaTest | 11 | Aprobadas |
| MedicamentoDaoCoberturaTest | 12 | Aprobadas |
| ConsultaInventarioDaoCoberturaTest | 5 | Aprobadas |
| ServicioAlertasSmsCoberturaTest | 10 | Aprobadas |
| InventarioFachadaCoberturaTest | 16 | Aprobadas |
| **Total** | **54** | **Aprobadas** |

Antes de este bloque existían 219 pruebas automatizadas.

Después de implementar las 54 pruebas nuevas,
la suite completa contiene 273 pruebas.

### 7.2. Ejecución completa

Comando utilizado:

```bash
mvn clean verify
```

Resultado confirmado el 30 de octubre de 2026:

```text
Tests run: 273, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Maven generó correctamente el artefacto JAR
y el reporte de cobertura JaCoCo.

Se analizaron 125 clases.

## 8. Evolución de cobertura JaCoCo

Las mediciones locales obtenidas durante el
desarrollo de la rama fueron:

| Etapa | Cobertura de líneas |
|---|---:|
| Después de las pruebas de preferencias SMS | 33,90 % |
| Después de las pruebas adicionales de DAO | 36,22 % |
| Después de las pruebas de alertas SMS | 38,22 % |
| Después de las pruebas de la fachada Inventario | 40,44 % |

Entre la primera y la última medición de esta
secuencia se obtuvo un incremento de 6,54
puntos porcentuales.

El incremento corresponde a cobertura local
de líneas medida por JaCoCo.

### 8.1. Cobertura de componentes destacados

| Componente | Líneas cubiertas | Total | Cobertura |
|---|---:|---:|---:|
| ServicioAlertasSms | 101 | 101 | 100,00 % |
| Inventario | 118 | 173 | 68,21 % |

Estas métricas representan líneas cubiertas,
no necesariamente todas las condiciones
y ramas posibles.

### 8.2. Diferencia entre JaCoCo y SonarCloud

La última referencia de Coverage de SonarCloud
antes de esta rama era 31,4 %.

La medición local actual de JaCoCo es 40,44 %
de líneas cubiertas.

Ambas métricas no son directamente equivalentes.

SonarCloud puede utilizar cobertura combinada
de líneas y condiciones, además de su propia
configuración y alcance de análisis.

Por esta razón, el porcentaje de Coverage
que alcanzará SonarCloud deberá verificarse
mediante un nuevo análisis de la rama integrada.

No se afirma todavía haber alcanzado
el 50-55 % de Coverage en SonarCloud.

## 9. Análisis de clases pendientes

Se utilizó el archivo jacoco.csv para identificar
las clases con mayor cantidad de líneas sin cubrir.

Los principales resultados fueron:

| Clase | Líneas sin cubrir | Cobertura de líneas |
|---|---:|---:|
| CatalogoController | 429 | 0,0 % |
| InventarioController | 307 | 0,0 % |
| ControladorMovimientos | 202 | 0,0 % |
| ConsultaInventarioController | 199 | 0,0 % |
| ControladorHistorialMovimientos | 180 | 12,6 % |
| ControladorAjustes | 178 | 0,0 % |
| ControladorPreferenciasSms | 177 | 0,0 % |
| GestorLotesDialogo | 138 | 0,0 % |
| CareStockApp | 138 | 0,0 % |
| ServicioAjustesInventario | 106 | 8,6 % |

Los controladores JavaFX presentan una oportunidad
importante para continuar incrementando la cobertura.

Sin embargo, deben probarse de forma controlada
para evitar dependencias innecesarias del entorno gráfico.

## 10. Limitaciones

Las pruebas implementadas utilizan principalmente
JUnit 5 y Mockito para evaluar componentes aislados.

Por tanto, no sustituyen:

- Pruebas de integración con PostgreSQL real.
- Pruebas de extremo a extremo de la interfaz JavaFX.
- Pruebas de carga mediante JMeter.
- Validación real de envío y recepción de SMS.
- Pruebas de concurrencia sobre la base de datos.
- Validaciones completas de seguridad y permisos.
- Ejecución de todos los flujos funcionales del usuario.

La cobertura elevada tampoco garantiza
ausencia de errores de software.

Los resultados deben complementarse con pruebas
de integración, aceptación, seguridad y rendimiento.

## 11. Próximos pasos

La meta final del plan continúa siendo
alcanzar un Coverage del 50-55 % en SonarCloud.

Se recomienda priorizar:

1. Pruebas de los controladores JavaFX.
2. Ampliación de pruebas de ServicioAjustesInventario.
3. Cobertura adicional de movimientos de inventario.
4. Pruebas de validación en formularios.
5. Pruebas de excepciones y rutas alternativas.
6. Ejecución completa mediante Maven.
7. Actualización de métricas en SonarCloud.

La selección de nuevas pruebas deberá basarse
en líneas sin cubrir, importancia funcional
y riesgos identificados.

## 12. Control de versiones

El trabajo se desarrolló en la rama:

`test/coverage-55-sms-inventario`

Los archivos se crearon y modificaron desde
Git Bash mediante comandos de consola.

El proceso seguido fue:

1. Identificación de clases prioritarias.
2. Creación de pruebas automatizadas.
3. Ejecución individual de cada bloque.
4. Corrección de configuraciones de Mockito.
5. Ejecución de regresión.
6. Medición de cobertura mediante JaCoCo.
7. Documentación de resultados.
8. Preparación de commit y Pull Request.

## 13. Conclusiones

La ampliación de pruebas automatizadas permitió
fortalecer la validación de módulos críticos
de CareStock.

Se implementaron 54 pruebas nuevas que cubren
persistencia, notificaciones SMS, reglas de negocio,
consultas de inventario y operaciones de la fachada.

La ejecución final confirmó:

- 273 pruebas automatizadas aprobadas.
- Cero fallos.
- Cero errores.
- Cero pruebas omitidas.
- Compilación Maven satisfactoria.
- Cobertura de líneas JaCoCo del 40,44 %.
- Cobertura del 100 % de líneas en ServicioAlertasSms.
- Cobertura del 68,21 % de líneas en Inventario.

Los resultados demuestran un avance medible
en el aseguramiento de calidad del proyecto.

Se mantiene como siguiente objetivo alcanzar
el 50-55 % de Coverage en SonarCloud,
priorizando nuevos casos de prueba sobre
componentes con cobertura insuficiente.

---

**Proyecto:** CareStock  
**Área:** Aseguramiento de calidad de software  
**Informe:** Ampliación de cobertura automatizada  
**Fecha:** 30 de octubre de 2026
