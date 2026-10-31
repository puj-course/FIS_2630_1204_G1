# CareStock - Refactorizacion y reduccion de codigo duplicado

**Fecha:** 31 de octubre de 2026  
**Rama:** `refactor/sonar-duplicacion-213`  
**Estado:** Compilacion y pruebas unitarias exitosas.

## 1. Objetivo

Reducir la duplicacion de codigo detectada por SonarCloud,
mejorar la mantenibilidad y conservar la funcionalidad
existente de CareStock.

### Metricas iniciales de SonarCloud

| Indicador | Valor inicial |
|---|---|
| Duplicacion | 21.3 % |
| Coverage | 39.2 % |
| Quality Gate | FAILED |
| Meta de duplicacion | 0.3 % |

Las metricas finales quedan pendientes del nuevo
analisis de SonarCloud.

## 2. Cambios implementados

### Consolidacion de controladores y navegacion

- Consolidacion de la navegacion en CareStockApp.
- Eliminacion de AplicacionCareStock.
- Eliminacion de CatalogoController duplicado.
- Eliminacion de InventarioController duplicado.
- Conservacion de ControladorCatalogo.
- Conservacion de ControladorInventario.
- Actualizacion de referencias entre controladores.
- Mantenimiento de la integracion con pantallas FXML.

### Refactorizacion de modelos

- Reutilizacion de ProductoBase.
- Centralizacion de atributos comunes.
- Consolidacion de validaciones.
- Conservacion de reglas propias de Aseo y Maternidad.

### Correcciones de compilacion

- Normalizacion de referencias DAO.
- Correccion de nombres de clases.
- Actualizacion de importaciones DTO.
- Correccion de referencias a servicios.
- Ajustes de dependencias en la fachada Inventario.

### Persistencia del catalogo

- Incorporacion de stock_minimo en INSERT.
- Actualizacion de sentencias UPDATE.
- Correccion del orden de parametros JDBC.
- Actualizacion de pruebas sobre las consultas SQL.

## 3. Pruebas realizadas

Herramientas:

- Java 21.
- Maven.
- JUnit 5.
- Mockito.
- JaCoCo 0.8.15.

Comando de validacion:

```bash
./mvnw clean verify
```

### Resultados

| Indicador | Resultado |
|---|---|
| Pruebas ejecutadas | 307 |
| Pruebas exitosas | 307 |
| Fallos | 0 |
| Errores | 0 |
| Omitidas | 0 |
| Clases analizadas por JaCoCo | 137 |
| Compilacion Maven | BUILD SUCCESS |

### Pruebas actualizadas

- InventarioFachadaCoberturaTest.
- CatalogoDaoJdbcTest.
- ConsultasSqlCatalogoTest.
- DashboardFxmlHandlersTest.

Se conservaron las pruebas existentes y se actualizaron
las verificaciones necesarias para la API y las
consultas SQL vigentes.

## 4. Calidad y mantenimiento

La refactorizacion busca:

- Evitar implementaciones paralelas.
- Reducir duplicacion de codigo.
- Mejorar la cohesion de los componentes.
- Facilitar el mantenimiento.
- Conservar la cobertura automatizada.
- Prevenir regresiones funcionales.

## 5. Verificaciones pendientes

- Confirmar cobertura actual en SonarCloud.
- Medir duplicacion despues del Pull Request.
- Verificar el Quality Gate.
- Validar navegacion y pantallas JavaFX manualmente.
- Comprobar stock_minimo en PostgreSQL de pruebas.
- Verificar operaciones reales de INSERT y UPDATE.

No se considera verificada la meta del 0.3 %
hasta obtener el resultado de SonarCloud.

## 6. Conclusion

CareStock supera la compilacion y las 307 pruebas
automatizadas ejecutadas.

La refactorizacion consolida componentes duplicados,
corrige inconsistencias de codigo y mantiene
la validacion automatizada del proyecto.

El cumplimiento final de las metricas de duplicacion
y cobertura depende del siguiente analisis de SonarCloud.
