# CareStock - Informe de pruebas de persistencia JDBC y transacciones

**Proyecto:** CareStock  
**Fecha de ejecución:** 30 de octubre de 2026  
**Rama:** `test/coverage-40-jdbc`  
**Objetivo de cobertura:** 40 %  
**Cobertura de líneas JaCoCo obtenida:** 31,45 %  
**Estado:** Pruebas aprobadas

---

## 1. Introducción

Como parte del Plan Maestro de Pruebas de Software de CareStock,
se implementó un nuevo conjunto de pruebas automatizadas para
incrementar la cobertura del código y verificar el comportamiento
de los componentes de persistencia y registro de lotes.

Las pruebas se desarrollaron con JUnit 5 y Mockito, utilizando
simulaciones de conexiones JDBC para validar las operaciones
de acceso a datos sin depender de una base de datos externa.

Esta estrategia permite ejecutar las pruebas de manera aislada,
reproducible y segura, sin modificar los datos de producción.

Se implementaron 25 casos de prueba nuevos, distribuidos
en cuatro clases.

La ejecución completa del proyecto finalizó correctamente
con 207 pruebas aprobadas.

## 2. Objetivos

### 2.1. Objetivo general

Incrementar la cobertura de pruebas automatizadas de CareStock
mediante la validación de operaciones JDBC, reglas de negocio,
segregación por farmacia y manejo de transacciones.

### 2.2. Objetivos específicos

- Verificar operaciones de creación, consulta y actualización.
- Comprobar la validación de identificadores y datos obligatorios.
- Verificar el filtrado de información por farmacia.
- Evaluar el comportamiento ante registros inexistentes.
- Comprobar las operaciones de confirmación y reversión de
  transacciones.
- Verificar que los errores no provoquen confirmaciones
  indebidas de transacciones.
- Incorporar pruebas ejecutables mediante Maven.
- Generar evidencia de ejecución y cobertura mediante JaCoCo.

## 3. Justificación de las clases seleccionadas

Se seleccionaron componentes de la capa de persistencia
porque representan operaciones fundamentales para el
funcionamiento del inventario de CareStock.

El análisis previo del reporte de cobertura permitió
identificar clases con cobertura nula o baja.

Las clases priorizadas fueron:

| Componente | Justificación |
|---|---|
| CatalogoDAO | Centraliza operaciones comunes del catálogo. |
| AseoDAO | Gestiona la persistencia de productos de aseo. |
| MaternidadDAO | Gestiona la persistencia de productos de maternidad. |
| MedicamentoCatalogoDAO | Permite consultar y modificar medicamentos por farmacia. |
| LoteDAOImpl | Gestiona la persistencia de lotes de medicamentos. |
| RegistroLoteService | Coordina el registro transaccional de lotes. |

La selección también responde a criterios de riesgo.

Un fallo en la persistencia puede provocar inconsistencias
de inventario, registros incorrectos o exposición de
información entre farmacias.

Por esta razón, se priorizaron los casos asociados con
integridad de datos, validaciones y transacciones.

## 4. Herramientas y tecnologías

| Herramienta | Utilización |
|---|---|
| Java 21 | Lenguaje y entorno de ejecución. |
| Maven | Compilación y ejecución automatizada. |
| JUnit 5 | Definición y ejecución de casos de prueba. |
| Mockito | Simulación de dependencias JDBC. |
| Maven Surefire 3.2.5 | Ejecución de pruebas unitarias. |
| JaCoCo 0.8.15 | Medición de cobertura del código. |
| Git Bash | Creación, ejecución y versionamiento. |
| Git | Control de versiones mediante rama independiente. |

Las pruebas utilizan objetos simulados de las interfaces
`Connection`, `PreparedStatement` y `ResultSet`.

También se utiliza `mockStatic` para simular el método
`ConexionBD.getConexion()`.

No se requiere una conexión real a PostgreSQL para ejecutar
este bloque de pruebas.

## 5. Metodología de pruebas

### 5.1. Patrón Arrange-Act-Assert (AAA)

Las pruebas se organizaron siguiendo el patrón AAA:

**Arrange (Preparar):**

Se construyen los datos de prueba y se configuran los
objetos simulados mediante Mockito.

**Act (Actuar):**

Se ejecuta el método de producción que se desea evaluar.

**Assert (Verificar):**

Se comprueba el resultado utilizando aserciones de JUnit
y verificaciones de interacciones con Mockito.

Por ejemplo, en una consulta JDBC:

- Arrange: configurar una conexión y un resultado simulado.
- Act: ejecutar `buscarPorId`.
- Assert: verificar el registro recuperado y los parámetros
  enviados a la sentencia SQL.

### 5.2. Enfoque de las pruebas

Se aplicaron pruebas de:

- Casos exitosos.
- Entradas inválidas.
- Registros inexistentes.
- Filtrado por farmacia.
- Manejo de excepciones.
- Actualización de datos.
- Confirmación de transacciones.
- Reversión de transacciones.

Las pruebas JDBC con Mockito son pruebas aisladas
de comportamiento y de interacción.

No sustituyen las pruebas de integración con una instancia
real de PostgreSQL.

## 6. Clases de prueba implementadas

### 6.1. CatalogoDaoJdbcTest

**Ubicación:**

`src/test/java/org/example/carestock/dao/CatalogoDaoJdbcTest.java`

**Total:** 8 pruebas.

Esta clase valida operaciones de persistencia relacionadas
con productos de aseo y maternidad.

| ID | Caso de prueba | Validación |
|---|---|---|
| CAT-01 | registraAseoConFarmaciaYUsuario | Registro con identificadores de farmacia y usuario. |
| CAT-02 | registraMaternidadConEdadOpcional | Registro y tratamiento de un campo opcional. |
| CAT-03 | buscaAseoRespetandoFarmacia | Consulta utilizando el identificador de farmacia. |
| CAT-04 | listaMaternidadDeUnaFarmacia | Recuperación de productos asociados a una farmacia. |
| CAT-05 | consultaSinResultadosDevuelveNull | Respuesta cuando no existe un registro. |
| CAT-06 | actualizaYDesactivaSegunFilasAfectadas | Interpretación de las filas afectadas por SQL. |
| CAT-07 | rechazaRegistrosYContextosInvalidos | Validación de datos e identificadores inválidos. |
| CAT-08 | convierteRegistroInvalidoEnErrorSql | Manejo de un registro con información inválida. |

**Resultado:** 8 pruebas aprobadas.

Durante la primera ejecución, CAT-08 presentó un fallo.

La simulación de `ResultSet` no proporcionaba el valor
de `precio`, lo que provocó una `NullPointerException`
antes de alcanzar el comportamiento esperado.

Se corrigió la configuración del objeto simulado
proporcionando un valor válido para el precio.

Después de la corrección, la prueba se ejecutó
satisfactoriamente.

No fue necesario modificar código de producción.

### 6.2. LoteDaoJdbcTest

**Ubicación:**

`src/test/java/org/example/carestock/dao/LoteDaoJdbcTest.java`

**Total:** 8 pruebas.

Esta clase verifica operaciones de persistencia
relacionadas con lotes de medicamentos.

| ID | Caso de prueba | Validación |
|---|---|---|
| LOT-01 | guardaLoteYRecuperaIdGenerado | Registro del lote y recuperación del ID generado. |
| LOT-02 | buscaLoteExistente | Consulta de un lote registrado. |
| LOT-03 | buscaLoteInexistente | Comportamiento cuando no existe el lote. |
| LOT-04 | listaLotesDeFarmaciaEspecifica | Filtrado por farmacia. |
| LOT-05 | listaLotesDeMedicamento | Consulta de lotes asociados a un medicamento. |
| LOT-06 | actualizaCantidadYEstado | Actualización de cantidad y estado. |
| LOT-07 | impideConsultasGlobalesYFarmaciaInvalida | Restricción de consultas globales y validación de farmacia. |
| LOT-08 | rechazaLoteConCantidadNegativa | Rechazo de cantidades inválidas. |

**Resultado:** 8 pruebas aprobadas.

Estas pruebas contribuyen a verificar la consistencia
de las operaciones de lotes, especialmente las relacionadas
con cantidades, identificadores y restricciones de consulta.

### 6.3. MedicamentoCatalogoJdbcTest

**Ubicación:**

`src/test/java/org/example/carestock/dao/MedicamentoCatalogoJdbcTest.java`

**Total:** 6 pruebas.

Esta clase evalúa las operaciones JDBC del catálogo
de medicamentos.

| ID | Caso de prueba | Validación |
|---|---|---|
| MED-01 | validaCamposObligatorios | Rechazo de información obligatoria inválida. |
| MED-02 | consultaMedicamentosDeFarmacia | Recuperación de medicamentos filtrados por farmacia. |
| MED-03 | actualizaMedicamentoSinAlterarStock | Actualización de información sin modificar el stock. |
| MED-04 | actualizaMedicamentoSinPrecio | Tratamiento de un precio opcional nulo. |
| MED-05 | desactivaSoloEnFarmaciaCorrespondiente | Parámetros de farmacia y condiciones de desactivación. |
| MED-06 | rechazaIdentificadoresNoValidos | Rechazo de identificadores inválidos. |

**Resultado:** 6 pruebas aprobadas.

Se prestó especial atención a las actualizaciones
del catálogo que no deben modificar directamente
el inventario disponible.

También se verificaron los parámetros utilizados
para limitar las operaciones a una farmacia.

### 6.4. RegistroLoteTransaccionTest

**Ubicación:**

`src/test/java/org/example/carestock/service/RegistroLoteTransaccionTest.java`

**Total:** 3 pruebas.

Esta clase evalúa el comportamiento transaccional
del registro de lotes mediante conexiones simuladas.

| ID | Caso de prueba | Validación |
|---|---|---|
| TX-01 | registraLoteYConfirmaTransaccion | Confirmación mediante commit y registro esperado. |
| TX-02 | revierteSiMedicamentoNoPerteneceAFarmacia | Ejecución de rollback al rechazar la operación. |
| TX-03 | revierteSiFallaActualizacionDeStock | Ejecución de rollback cuando falla la actualización. |

**Resultado:** 3 pruebas aprobadas.

Estas pruebas verifican las interacciones de control
transaccional con la conexión JDBC.

Su alcance consiste en comprobar las llamadas a
`commit`, `rollback` y `setAutoCommit` según el escenario.

La atomicidad real sobre PostgreSQL deberá comprobarse
posteriormente mediante pruebas de integración.

## 7. Resultados de ejecución

### 7.1. Ejecución de las pruebas nuevas

Comando utilizado:

```bash
mvn -Dtest=CatalogoDaoJdbcTest,MedicamentoCatalogoJdbcTest,LoteDaoJdbcTest,RegistroLoteTransaccionTest test
```

Resultados:

| Clase | Ejecutadas | Fallidas | Errores | Omitidas |
|---|---:|---:|---:|---:|
| CatalogoDaoJdbcTest | 8 | 0 | 0 | 0 |
| LoteDaoJdbcTest | 8 | 0 | 0 | 0 |
| MedicamentoCatalogoJdbcTest | 6 | 0 | 0 | 0 |
| RegistroLoteTransaccionTest | 3 | 0 | 0 | 0 |
| **Total** | **25** | **0** | **0** | **0** |

**Resultado final: BUILD SUCCESS.**

### 7.2. Ejecución de la suite completa

Comando utilizado:

```bash
mvn clean verify
```

La ejecución incluyó las pruebas previamente
implementadas y el nuevo bloque JDBC.

Resultado de Maven:

```text
Tests run: 207, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Fecha de ejecución: 30/10/2026.

También se confirmó la compilación del proyecto y
la generación del artefacto JAR.

El informe de Maven registró el análisis de
122 clases mediante JaCoCo.

## 8. Resultados de cobertura

### 8.1. Antecedentes

La última referencia disponible de SonarCloud,
antes de esta implementación, mostraba:

**Coverage: 25,2 %.**

La meta establecida para CareStock es alcanzar
al menos el 40 %.

### 8.2. Medición local mediante JaCoCo

Después de ejecutar las pruebas se generó el
reporte de cobertura.

Archivos de evidencia:

- `target/jacoco.exec`
- `target/site/jacoco/index.html`
- `target/site/jacoco/jacoco.csv`
- `target/surefire-reports/`

Se utilizó el siguiente comando para calcular
la cobertura de líneas:

```bash
awk -F, 'NR>1 {m+=$8; c+=$9}
END {
  if (m+c>0)
    printf "Cobertura de lineas JaCoCo: %.2f%%\n",100*c/(m+c)
}' target/site/jacoco/jacoco.csv
```

**Resultado obtenido: 31,45 % de cobertura de líneas.**

Este valor representa el porcentaje de líneas
cubiertas de acuerdo con el reporte local de JaCoCo.

La métrica Coverage de SonarCloud puede incorporar
también cobertura de condiciones.

Por tanto, el 31,45 % de JaCoCo y el 25,2 % previo
de SonarCloud no deben utilizarse para calcular
un incremento porcentual directo.

Se requiere una nueva ejecución del análisis
de SonarCloud para confirmar el porcentaje
actualizado en esa plataforma.

### 8.3. Estado frente a la meta

| Indicador | Valor |
|---|---|
| Meta de Coverage SonarCloud | 40 % |
| Última referencia SonarCloud | 25,2 % |
| Cobertura local de líneas JaCoCo | 31,45 % |
| Pruebas nuevas aprobadas | 25 |
| Suite completa aprobada | 207 |
| Estado de compilación | BUILD SUCCESS |

La cobertura local indica un avance en la
ejecución de código de producción.

Sin embargo, todavía no se ha confirmado
el cumplimiento de la meta en SonarCloud.

## 9. Advertencias observadas

Durante las ejecuciones Maven presentó
advertencias relacionadas con:

- Dependencias y metadatos de JavaFX 21.
- Uso de módulos automáticos asociados con jBCrypt.
- APIs obsoletas en algunas clases.
- Operaciones sin comprobación de tipos.
- Carga dinámica del agente utilizado por Mockito y Byte Buddy.

Estas advertencias no impidieron la compilación,
ejecución de pruebas ni generación del informe JaCoCo.

Se recomienda evaluar posteriormente la configuración
del agente de Mockito para mantener compatibilidad
con futuras versiones de Java.

Las advertencias deben revisarse por separado
y no considerarse automáticamente errores
de los casos de prueba.

## 10. Limitaciones

Las pruebas implementadas utilizan Mockito
para simular los componentes JDBC.

Por lo tanto, permiten verificar la lógica
de los métodos y las interacciones con las
dependencias simuladas.

No verifican directamente:

- La ejecución real de SQL contra PostgreSQL.
- La correspondencia completa con el esquema de BD.
- Restricciones reales de claves foráneas.
- Comportamiento transaccional bajo concurrencia.
- Aislamiento efectivo de datos con registros reales.
- Rendimiento del motor de base de datos.

Estas verificaciones deberán complementarse
con pruebas de integración utilizando una
base de datos de pruebas independiente.

No se debe utilizar Neon productivo para
ejecutar pruebas que modifiquen información.

## 11. Próximas actividades

Para continuar incrementando la cobertura
se propone el siguiente orden:

1. Revisar el reporte JaCoCo por paquetes y clases.
2. Identificar componentes con mayor cantidad
   de líneas sin cubrir.
3. Priorizar servicios y reglas de negocio.
4. Ampliar pruebas de notificaciones SMS y alertas.
5. Extender las pruebas de la fachada de inventario.
6. Incorporar pruebas adicionales de seguridad
   y segregación por farmacia.
7. Ejecutar nuevamente la suite Maven.
8. Medir la cobertura con JaCoCo y SonarCloud.
9. Confirmar el cumplimiento de la meta del 40 %.

Cada nuevo bloque deberá incorporar casos exitosos,
casos negativos y verificaciones de errores.

## 12. Estrategia de control de versiones

Las implementaciones de este bloque se realizaron
en la siguiente rama:

`test/coverage-40-jdbc`

Los archivos de prueba fueron creados desde Git Bash,
utilizando comandos `cat` y redirección de contenido.

No se utilizaron archivos de parche ni paquetes ZIP
para implementar los cambios.

Los cambios deberán revisarse antes de generar
el commit y el Pull Request.

## 13. Conclusiones

La implementación del bloque de pruebas JDBC permitió
ampliar la validación automatizada de componentes
importantes de CareStock.

Se incorporaron 25 pruebas nuevas orientadas a
persistencia, validación de datos y manejo de
transacciones.

La ejecución final confirmó:

- 25 pruebas nuevas aprobadas.
- 207 pruebas aprobadas en toda la suite.
- Cero fallos y cero errores.
- Compilación correcta del proyecto.
- Generación satisfactoria del reporte JaCoCo.
- Cobertura local de líneas del 31,45 %.

El trabajo fortalece la estrategia de pruebas
automatizadas del proyecto y permite continuar
hacia la meta de Coverage del 40 %.

El siguiente paso consiste en identificar nuevas
áreas del código con baja cobertura, priorizando
las funcionalidades críticas de negocio y las
rutas de ejecución todavía no verificadas.

---

**Proyecto:** CareStock  
**Módulo:** Persistencia JDBC y transacciones  
**Documento:** Informe de ejecución de pruebas  
**Fecha:** 30 de octubre de 2026
