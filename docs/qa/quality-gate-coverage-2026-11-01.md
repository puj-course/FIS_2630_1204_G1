# CareStock - Incremento de cobertura del Quality Gate

Fecha: 01/11/2026

## Objetivo

Superar el 80% de Coverage on New Code exigido por SonarCloud.

## Línea base

- Coverage on New Code: 77.63%.
- Security Rating: B.
- Duplicación de código nuevo: 0.02%.

## Pruebas ampliadas

- InventarioFachadaCoberturaTest:
  operaciones CRUD, validaciones y segregación por farmacia.
- ControladorCatalogoFormularioFxTest:
  ejecución de funciones de presentación de alertas.
- ServicioSmsTest:
  errores HTTP, SID inválido e interrupciones.
- DispositivoMedicoQualityGateTest:
  validaciones y condiciones de dispensación.

## Herramientas

- Java 21.
- JUnit 5.
- Mockito.
- JaCoCo.
- Maven.
- SonarCloud.

## Validación

Ejecutar `mvnw.cmd clean verify`.

Registrar el resultado de las pruebas y el nuevo porcentaje
de cobertura publicado por SonarCloud.

## Criterios de éxito

- Coverage on New Code >= 80%.
- Security Rating A.
- Duplicación de código nuevo <= 3%.
- Pruebas automatizadas exitosas.
