# Correccion de analisis duplicados en SonarCloud

## Incidente

El analisis de CareStock en main presento
CE Task FAILED despues de subir correctamente
el reporte a SonarCloud.

## Diagnostico

Se detectaron dos workflows que analizaban
el mismo proyecto y las mismas ramas:

- .github/workflows/sonar.yml
- .github/workflows/SonarQube.yml

La concurrencia de analisis puede ocasionar
errores de procesamiento fuera de orden.

La causa definitiva requiere consultar el
errorMessage de la tarea del Compute Engine.

## Correccion preventiva

- Conservar sonar.yml como workflow oficial.
- Eliminar el workflow redundante.
- Controlar la concurrencia por rama.
- Conservar las pruebas JUnit.
- Conservar la importacion JaCoCo.
- Conservar el Quality Gate.
- Mantener Xvfb para las pruebas JavaFX.

## Validacion pendiente

- Ejecutar el analisis en GitHub Actions.
- Confirmar procesamiento correcto de main.
- Confirmar importacion de cobertura.
- Verificar el resultado del Quality Gate.

No se modifican las exclusiones ni los
umbrales de calidad para ocultar errores.
