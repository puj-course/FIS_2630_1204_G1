# Refactorizacion de modelos para reducir duplicacion

## Objetivo

Reducir la duplicacion de codigo identificada en SonarCloud,
sin modificar las reglas de negocio ni los contratos publicos
de los modelos involucrados.

## Hallazgos originales

- Aseo.java: 38.6 % de lineas duplicadas reportadas.
- Maternidad.java: constructor vacio innecesario.
- DispositivoMedico.java: constructor vacio innecesario.
- Recomendacion adicional: centralizar literales repetidos.

## Solucion implementada

1. Crear la clase abstracta ProductoBase.
2. Centralizar los atributos comunes y sus accesores.
3. Centralizar las validaciones comunes de Aseo y Maternidad.
4. Mantener las reglas de negocio particulares.
5. Sustituir los estados literales repetidos por constantes.
6. Eliminar los constructores vacios innecesarios.
7. Conservar el uso de la interfaz Validable.

## Compatibilidad

- Los constructores publicos sin argumentos siguen disponibles.
- Se mantienen los getters y setters publicos.
- Se conservan los mensajes de validacion existentes.
- Maternidad conserva la normalizacion de descripcion.
- No se realizan modificaciones a la base de datos.
- No se modifican los contratos de las fabricas ni los DAO.

## Pruebas

Herramientas: JUnit 5, Maven, JaCoCo y SonarCloud.

Clases de prueba:
- ProductosBasicosTest
- RefactorizacionProductosTest

## Verificaciones pendientes

- Ejecutar pruebas unitarias y de regresion.
- Ejecutar Maven clean verify.
- Revisar cobertura generada por JaCoCo.
- Confirmar ausencia de regresiones en CI.
- Comparar duplicacion antes y despues en SonarCloud.

## Criterios de aceptacion

- Proyecto compilado correctamente.
- Pruebas automatizadas aprobadas.
- Comportamiento funcional conservado.
- Reduccion de duplicacion verificada con SonarCloud.
