# CareStock - Pruebas de Reliability en CatalogoDAO

**Fecha:** 30 de octubre de 2026

**Rama:** `fix/reliability-jdbc-sql-exceptions`

**Modulo:** Persistencia y catalogo de productos

**Herramientas:** Java 21, Maven, JUnit 5, Mockito y JaCoCo

**Estado:** Pendiente de validacion final

## 1. Objetivo

Mejorar la confiabilidad de las operaciones JDBC
de CatalogoDAO, reduciendo la construccion dinamica
de sentencias SQL y conservando el comportamiento
del inventario multifarmacia.

## 2. Justificacion

El analisis del codigo identifico cinco operaciones
que construian consultas concatenando nombres
de tablas y columnas.

Aunque los identificadores proceden de las clases
internas AseoDAO y MaternidadDAO, se decidio
centralizar las consultas SQL estaticas.

Esta modificacion facilita el analisis estatico
y reduce el riesgo de introducir identificadores
SQL no autorizados en futuras modificaciones.

## 3. Cambios implementados

- Incorporacion de ConsultasSqlCatalogo.
- Definicion de SQL fijo para aseo y maternidad.
- Refactorizacion de cinco operaciones en CatalogoDAO.
- Conservacion del patron Template Method.
- Conservacion del filtrado por farmacia.
- Conservacion de los estados ACTIVO e INACTIVO.
- Validacion de tablas autorizadas.
- Incorporacion de pruebas automatizadas.

## 4. Patron de diseño

Se conserva Template Method.

CatalogoDAO mantiene la secuencia de validacion,
preparacion de sentencias, ejecucion SQL y
construccion de resultados.

AseoDAO y MaternidadDAO mantienen la asignacion
de campos especificos y la construccion
de objetos de dominio.

El cambio afecta la seleccion de consultas SQL,
no el flujo general de persistencia.

## 5. Casos de prueba

| ID | Caso | Validacion |
|---|---|---|
| SQL-01 | Inserciones | Diez parametros y columnas correspondientes. |
| SQL-02 | Consultas por ID | Dos parametros y filtro por farmacia. |
| SQL-03 | Listados | Un parametro y ordenamiento por nombre. |
| SQL-04 | Actualizaciones | Once parametros y estado ACTIVO. |
| SQL-05 | Desactivaciones | Tres parametros y estado INACTIVO. |
| SQL-06 | Tablas invalidas | Rechazo de identificadores no autorizados. |

## 6. Metodologia

Las pruebas siguen el patron Arrange-Act-Assert.

Arrange:
Se selecciona la tabla y la operacion.

Act:
Se obtiene la sentencia SQL mediante
ConsultasSqlCatalogo.

Assert:
Se comprueba la estructura de la sentencia,
los filtros de farmacia y la cantidad
de parametros JDBC.

## 7. Pruebas de regresion

Se ejecutaran las pruebas nuevas junto con
CatalogoDaoJdbcTest.

Posteriormente se ejecutara la suite completa
mediante Maven.

Comandos:

```bash
mvn -Dtest=ConsultasSqlCatalogoTest,CatalogoDaoJdbcTest test

mvn clean verify


8. Resultados y evidencias
Pendientes de incorporar despues
de la ejecucion de Maven.

Se registraran:
- Pruebas ejecutadas.
- Pruebas aprobadas.
- Fallos y errores.
- Resultado de compilacion.
- Cobertura JaCoCo.
- Resultado del analisis SonarCloud.
9. Limitaciones
Las pruebas unitarias de consultas SQL no
sustituyen las pruebas de integracion
con una base de datos PostgreSQL real.

La reduccion efectiva de los problemas
de Reliability debera confirmarse
en un nuevo analisis de SonarCloud.
10. Conclusion
La refactorizacion busca mejorar la
confiabilidad y mantenibilidad de
la capa de persistencia sin alterar
la arquitectura funcional de CareStock.

La aceptacion definitiva queda condicionada
a la ejecucion satisfactoria de las pruebas
y a la revision de los resultados de SonarCloud.
