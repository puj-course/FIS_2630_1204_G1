# Informe de pruebas unitarias e incremento de cobertura — CareStock

## 1. Objetivo

Incrementar progresivamente la cobertura de código del sistema
CareStock mediante la implementación de pruebas unitarias
automatizadas con JUnit 5 y la medición de cobertura con JaCoCo.

Esta primera fase se enfocó en modelos del dominio,
validaciones de negocio y objetos de transferencia de datos (DTO).

La meta general del proyecto es alcanzar una cobertura
de entre el 30 % y el 35 %.

## 2. Información general

- Proyecto: CareStock
- Rama: test/incrementar-coverage-modelos
- Fecha: 29 de octubre de 2026
- Lenguaje: Java 21
- Gestión de dependencias: Maven
- Framework de pruebas: JUnit 5
- Ejecución de pruebas: Maven Surefire
- Cobertura de código: JaCoCo
- Análisis de calidad: SonarQube

## 3. Justificación de las clases seleccionadas

Las clases seleccionadas pertenecen principalmente al modelo
de dominio y a los objetos de transferencia de datos.

Se priorizaron porque presentan las siguientes características:

1. Contienen reglas de negocio que pueden verificarse de
   manera independiente.
2. Permiten ejecutar pruebas sin conexión a PostgreSQL.
3. No requieren inicializar interfaces gráficas JavaFX.
4. Presentaban oportunidades de incrementar la cobertura.
5. Permiten comprobar escenarios válidos e inválidos.
6. Facilitan la detección temprana de regresiones.

Las pruebas incluyen validaciones relacionadas con:

- Identificadores y campos obligatorios.
- Precios y cantidades permitidas.
- Existencias de inventario.
- Fechas de vencimiento.
- Estados sanitarios.
- Restricciones de dispensación.
- Ajustes de inventario.
- Conversiones de datos.
- Inmutabilidad de colecciones.

Estas características permiten mejorar la calidad del sistema
mediante pruebas deterministas y reproducibles.

## 4. Metodología de pruebas

Se empleó el patrón Arrange-Act-Assert (AAA).

### Arrange

Se preparan las instancias, dependencias y datos necesarios
para construir el escenario evaluado.

### Act

Se ejecuta el método o comportamiento que se desea verificar.

### Assert

Se comprueba que el resultado coincida con el comportamiento
esperado mediante las aserciones de JUnit 5.

Se utilizaron principalmente:

- assertEquals
- assertTrue
- assertFalse
- assertAll
- assertThrows
- assertDoesNotThrow

También se implementaron pruebas negativas para comprobar
el manejo de entradas inválidas y excepciones.

## 5. Pruebas nuevas implementadas

| Clase de prueba | Pruebas | Funcionalidad evaluada |
|---|---:|---|
| MedicamentoTest | 6 | Validación de medicamentos, precios, stock y dispensación |
| LoteTest | 6 | Vencimientos, cantidades, estados sanitarios y trazabilidad |
| ProductosBasicosTest | 7 | Aseo, maternidad, cosméticos, dispositivos médicos y usuarios |
| AjusteExistenciasTest | 4 | Diferencias de inventario, cantidades y motivos |
| LecturaCamposTest | 4 | Conversión y validación de precios y enteros |
| DatosTransferenciaTest | 2 | Copia e inmutabilidad de colecciones |
| **Total** | **29** | **Pruebas nuevas** |

## 6. Ejecución de pruebas

Se ejecutó la batería de pruebas mediante Maven desde
la terminal Git Bash.

Comando:

```bash
./mvnw.cmd -B clean verify


Los resultados se verificaron consultando los informes
generados por Maven Surefire.

Comando:
grep -h "Tests run:" target/surefire-reports/*.txt


Resultados de ejecución
Indicador	Resultado
Clases de prueba ejecutadas	20
Pruebas existentes	90
Pruebas nuevas	29
Total de pruebas ejecutadas	119
Pruebas satisfactorias	119
Fallos	0
Errores	0
Pruebas omitidas	0

Resultado: ejecución satisfactoria de la batería
de pruebas sin fallos ni errores.
7. Medición de cobertura
La cobertura se calculó a partir del archivo:

target/site/jacoco/jacoco.csv

Se utilizaron los contadores LINE_MISSED y LINE_COVERED
de JaCoCo.

Fórmula:

Cobertura de líneas (%) =
líneas cubiertas / (líneas cubiertas + líneas no cubiertas) * 100
Comparación antes y después
Indicador	Resultado
Cobertura inicial de líneas	10.67 %
Cobertura final de líneas	15.26 %
Incremento absoluto	4.59 puntos porcentuales
Incremento relativo	43.02 %
Meta mínima	30 %
Meta máxima	35 %

La implementación consiguió aumentar la cobertura
de líneas en 4.59 puntos porcentuales.

El aumento se obtuvo agregando pruebas sin requerir
modificaciones a la lógica de producción.
Diferencia respecto a SonarQube
Antes de esta implementación, SonarQube reportaba
un Coverage global del 11.3 %.

Este valor no debe confundirse con la cobertura
de líneas calculada mediante el CSV de JaCoCo,
porque SonarQube puede combinar cobertura de
líneas y condiciones.

El nuevo valor global de SonarQube deberá verificarse
cuando se ejecute el análisis de la rama o del Pull Request.
8. Clases pendientes de cobertura
El reporte de JaCoCo identificó diversas clases
con 0 % de cobertura de líneas, entre ellas:
- CareStockApp
- ConexionBD
- PrecalentadorConexionBD
- CatalogoController
- ConsultaInventarioController
- ControladorAjustes
- ControladorMovimientos
Estas clases requieren un análisis particular
porque algunas interactúan con JavaFX,
la base de datos o componentes externos.

No se recomienda priorizarlas únicamente por tener
0 % de cobertura. Se debe considerar la complejidad,
el riesgo funcional y las líneas que podrían cubrirse.
9. Conclusiones
1. Se implementaron 29 pruebas nuevas con JUnit 5.
2. Se ejecutaron satisfactoriamente 119 pruebas.
3. No se registraron fallos, errores ni pruebas omitidas.
4. La cobertura de líneas aumentó del 10.67 % al 15.26 %.
5. Se incrementó la protección frente a regresiones
   en modelos y validaciones del dominio.
6. La ejecución no necesitó servicios externos ni
   inicialización de interfaces JavaFX.
7. El proyecto aún no alcanza su objetivo de cobertura
   global del 30-35 %.
10. Próximas actividades
Para continuar aumentando la cobertura se propone:
1. Identificar las clases de servicio con más líneas
   descubiertas y lógica de negocio comprobable.
2. Incorporar pruebas de condiciones y rutas alternativas.
3. Ampliar la cobertura de autenticación y sesión.
4. Evaluar servicios de inventario y validaciones críticas.
5. Emplear Mockito cuando sea necesario aislar dependencias.
6. Ejecutar pruebas de regresión tras cada implementación.
7. Verificar los resultados en SonarQube mediante CI.
La estrategia será continuar progresivamente hasta
alcanzar la cobertura objetivo, priorizando pruebas
significativas y evitando aumentar las métricas
únicamente mediante pruebas triviales.
