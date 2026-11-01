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

## Ampliación de cobertura - 01/11/2026

### Línea base

- Coverage on New Code: 78.1%.
- Pruebas automatizadas reportadas: 445.
- Objetivo: Coverage >= 80%.

### Nuevos casos propuestos

Se incorporaron:

- 20 pruebas para escenarios del ControladorInventario.
- 15 pruebas para validaciones del ServicioMovimientosProducto.

### Áreas evaluadas

- Resumen de alertas.
- Contadores de inventario.
- Excepciones de dispensación.
- Notificaciones y marcado como leído.
- Exportación CSV.
- Validación de movimientos y límites de stock.

### Herramientas

JUnit 5, Mockito, JavaFX, JaCoCo y Maven.

### Resultados pendientes

Registrar después de la ejecución:

- Total de pruebas ejecutadas.
- Pruebas exitosas y fallidas.
- Coverage on New Code de SonarCloud.
- Estado del Quality Gate.
