# Metricas adicionales de calidad - CareStock

## Objetivo

Evaluar la mantenibilidad del codigo Java mediante
tres metricas estaticas complementarias a SonarQube
y JaCoCo.

## Metricas seleccionadas

### 1. Longitud de codigo (LOC)

Mide las lineas que contienen codigo Java,
sin contabilizar lineas vacias ni comentarios.

Permite identificar clases y metodos extensos.

Umbral orientativo: mas de 60 LOC por metodo.

### 2. Complejidad ciclomatica (CCN)

Mide la complejidad del flujo de control,
considerando estructuras de decision,
bucles y operadores logicos.

Permite identificar metodos que pueden
requerir mas pruebas y refactorizacion.

Umbral orientativo: CCN mayor que 10.

### 3. Profundidad de anidamiento

Mide el nivel maximo de estructuras
de control anidadas dentro de un metodo.

Permite identificar codigo dificil de leer,
comprender y mantener.

Umbral orientativo: mas de 3 niveles.

## Implementacion

Se utiliza un analizador Java 21 basado
en el arbol sintactico del codigo fuente.

El analizador genera:

- resumen.md
- metodos.csv
- archivos.csv

## Automatizacion

El workflow qa-metricas-calidad.yml
ejecuta las mediciones automaticamente
en GitHub Actions.

Los resultados se almacenan como evidencias
durante 14 dias.

## Alcance

Se analiza codigo Java de produccion.

No se modifican las clases del sistema,
la base de datos ni las pruebas existentes.

Los umbrales generan indicadores de revision,
pero no bloquean los Pull Requests.

Las definiciones locales pueden producir
resultados diferentes a los de SonarQube.

## Conclusion

Las metricas permiten identificar componentes
prioritarios para mejorar la mantenibilidad
y reducir la complejidad tecnica de CareStock.
