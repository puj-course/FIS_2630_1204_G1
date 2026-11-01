# CareStock - Segunda fase de incremento de cobertura

## 1. Objetivo

Ampliar la cobertura de pruebas automatizadas de CareStock
mediante pruebas unitarias de servicios, autenticacion,
gestion de sesiones, cache, lotes y notificaciones SMS.

Esta fase da continuidad a las pruebas de modelos y DTO
implementadas anteriormente.

La meta establecida es alcanzar aproximadamente el 25 %
de cobertura global en SonarQube.

## 2. Informacion general

- Proyecto: CareStock
- Rama: test/coverage-servicios-seguridad-cache
- Fecha: 29 de octubre de 2026
- Lenguaje: Java 21
- Framework: JUnit 5
- Simulacion de dependencias: Mockito
- Construccion: Maven
- Ejecucion: Maven Surefire
- Cobertura: JaCoCo
- Calidad de codigo: SonarQube

## 3. Justificacion de las clases seleccionadas

Durante el analisis anterior de SonarQube se identificaron
servicios y componentes con baja cobertura de codigo.

Se seleccionaron clases relacionadas con funcionalidades
criticas de CareStock:

1. Autenticacion y validacion de credenciales.
2. Gestion de sesiones y autorizacion.
3. Segregacion de informacion por farmacia.
4. Almacenamiento temporal y control de cache.
5. Registro, modificacion y retiro de lotes.
6. Control de vencimientos.
7. Procesamiento seguro de notificaciones SMS.

Estas clases fueron priorizadas porque contienen reglas
de negocio, decisiones condicionales y operaciones cuyo
comportamiento puede verificarse mediante pruebas.

Adicionalmente, se busco incrementar la cobertura de
ramas y fortalecer la deteccion de regresiones.

## 4. Metodologia de pruebas

Se utilizo el patron Arrange-Act-Assert (AAA).

### Arrange

Preparacion de objetos, datos, escenarios y dependencias
simuladas con Mockito.

### Act

Ejecucion del metodo o comportamiento que se desea evaluar.

### Assert

Verificacion de resultados, excepciones, estados e
interacciones esperadas.

Se utilizaron aserciones de JUnit 5 y verificaciones
de interacciones mediante Mockito.

Las pruebas JDBC se ejecutaron con conexiones simuladas,
sin utilizar la base de datos de produccion.

Las pruebas de SMS utilizaron proveedores simulados,
sin realizar envios reales.

## 5. Pruebas implementadas

| Clase de prueba | Pruebas |
|---|---:|
| CacheFarmaciaTest | 13 |
| VistasCacheTest | 3 |
| AuthenticationServiceTest | 7 |
| ControlVencimientosServiceTest | 5 |
| ServicioCrudLotesValidacionesTest | 15 |
| ServicioCrudLotesJdbcTest | 6 |
| SesionUsuarioTest | 7 |
| EnvioProtegidoSmsTest | 7 |
| **Total** | **63** |

### 5.1. CacheFarmaciaTest

Se evaluaron:

- Validacion de parametros de configuracion.
- Reutilizacion de resultados almacenados.
- Segregacion de cache por farmacia.
- Restricciones de acceso.
- Revocacion de permisos.
- Invalidacion individual y general.
- Eviccion LRU.
- Limites de almacenamiento.
- Manejo de errores de consulta.
- Expiracion por TTL.
- Comportamiento ante consultas concurrentes.

Resultado: 13 pruebas satisfactorias.

### 5.2. VistasCacheTest

Se verificaron:

- Reutilizacion de datos del catalogo.
- Cache de lotes por farmacia.
- Invalidacion de informacion.
- Acceso a metricas.
- Restricciones de autorizacion.

Resultado: 3 pruebas satisfactorias.

### 5.3. AuthenticationServiceTest

Se evaluaron:

- Credenciales vacias o nulas.
- Usuarios inexistentes.
- Autenticacion correcta mediante BCrypt.
- Contrasenas incorrectas.
- Rechazo de contrasenas sin hash BCrypt valido.
- Hashes nulos o malformados.
- Propagacion de errores del repositorio.

Se utilizo Mockito para simular el acceso a usuarios.

Resultado: 7 pruebas satisfactorias.

### 5.4. ControlVencimientosServiceTest

Se verificaron:

- Rechazo de identificadores invalidos.
- Transacciones sin lotes vencidos.
- Actualizacion de lotes vencidos.
- Registro de auditoria.
- Reconciliacion de existencias.
- Confirmacion mediante COMMIT.
- Reversion de operaciones mediante ROLLBACK.
- Conservacion de excepciones ante errores.

Se simularon las operaciones JDBC.

Resultado: 5 pruebas satisfactorias.

### 5.5. ServicioCrudLotesValidacionesTest

Se evaluaron:

- Autorizacion de roles administrativos.
- Rechazo de roles no autorizados.
- Creacion con datos validos.
- Validacion de numeros de lote.
- Restricciones de cantidades.
- Validacion de vencimientos.
- Ubicaciones obligatorias.
- Retiro de lotes sin existencias.
- Restricciones por existencia disponible.
- Retiro de lotes deshabilitados.
- Retiro de lotes vencidos.
- Estados no permitidos.
- Representacion de opciones.
- Validaciones previas al acceso a datos.

Resultado: 15 pruebas satisfactorias.

### 5.6. ServicioCrudLotesJdbcTest

Se verificaron mediante Mockito:

- Consulta de medicamentos por farmacia.
- Consulta de ubicaciones.
- Registro de nuevos lotes.
- Edicion de informacion de lotes.
- Retiro logico.
- Registro de movimientos.
- Confirmacion de transacciones.
- ROLLBACK ante errores de validacion.

Estas pruebas verifican interacciones simuladas con JDBC;
no sustituyen las pruebas de integracion con PostgreSQL.

Resultado: 6 pruebas satisfactorias.

### 5.7. SesionUsuarioTest

Se evaluaron:

- Rechazo del acceso sin sesion.
- Identificacion del administrador.
- Restricciones por rol.
- Recuperacion de datos de sesion.
- Asociacion entre sesion y usuario.
- Rechazo de usuarios invalidos.
- Cierre de sesion y eliminacion del acceso.

Resultado: 7 pruebas satisfactorias.

### 5.8. EnvioProtegidoSmsTest

Se verificaron:

- Configuracion obligatoria.
- Alertas inexistentes.
- Rechazo de destinatarios sin consentimiento.
- Envio simulado de alertas.
- Registro de respuestas rechazadas.
- Tratamiento de errores del proveedor.
- ROLLBACK ante errores SQL.

No se enviaron mensajes SMS reales.

Resultado: 7 pruebas satisfactorias.

## 6. Ejecucion de las pruebas

La bateria completa fue ejecutada desde Git Bash.

Comando utilizado:

    ./mvnw.cmd -B clean verify

Maven compilo 90 archivos fuente y 28 archivos de prueba.

JaCoCo analizo un conjunto de 122 clases.

### Resultados

| Indicador | Resultado |
|---|---:|
| Pruebas anteriores | 119 |
| Pruebas nuevas | 63 |
| Pruebas ejecutadas | 182 |
| Pruebas satisfactorias | 182 |
| Fallos | 0 |
| Errores | 0 |
| Omitidas | 0 |
| Estado de Maven | BUILD SUCCESS |

La totalidad de las 182 pruebas finalizo correctamente.

## 7. Resultados de cobertura

Los porcentajes se obtuvieron del archivo:

target/site/jacoco/jacoco.csv

### Cobertura de lineas

| Indicador | Resultado |
|---|---:|
| Cobertura inicial JaCoCo | 15.26 % |
| Cobertura final JaCoCo | 24.67 % |
| Incremento absoluto | 9.41 puntos porcentuales |

### Cobertura de ramas

| Indicador | Resultado |
|---|---:|
| Cobertura final de ramas | 26.31 % |

El incremento de lineas representa una mejora importante
respecto a la primera fase de pruebas.

La cobertura de ramas evidencia que tambien se han
ejercitado decisiones y caminos alternativos.

### Relacion con SonarQube

El ultimo Coverage global conocido de SonarQube,
antes de esta segunda fase, era del 16.3 %.

El nuevo valor global de SonarQube queda pendiente
de confirmacion mediante el analisis de CI.

Los porcentajes de lineas y ramas de JaCoCo no deben
confundirse con la metrica global de SonarQube.

## 8. Advertencias identificadas

La compilacion termino correctamente, aunque Maven
mostro advertencias relacionadas con:

- Uso de jbcrypt como modulo automatico.
- APIs obsoletas en determinadas clases.
- Operaciones genericas no verificadas.
- Carga dinamica del agente utilizado por Mockito.

Estas advertencias no impidieron ejecutar las pruebas.

Se recomienda revisar su impacto y compatibilidad
en futuras actualizaciones de Java y dependencias.

## 9. Alcance y limitaciones

Las pruebas desarrolladas verifican comportamientos
de servicios y componentes mediante aislamiento de
dependencias y simulaciones.

No constituyen evidencia de funcionamiento de
PostgreSQL, del proveedor externo de SMS o de
los componentes visuales de JavaFX.

Las pruebas de integracion y de interfaz deben
ejecutarse de manera complementaria.

La cobertura no garantiza por si sola la ausencia
de defectos; es una metrica de ejecucion del codigo.

## 10. Conclusiones

1. Se implementaron 63 pruebas nuevas.
2. Se ampliaron 8 clases de prueba.
3. La bateria completa alcanzo 182 pruebas.
4. No se presentaron fallos ni errores.
5. Maven finalizo con BUILD SUCCESS.
6. La cobertura de lineas aumento 9.41 puntos.
7. Se alcanzo una cobertura de lineas del 24.67 %.
8. La cobertura de ramas alcanzo el 26.31 %.
9. Se fortalecieron pruebas de seguridad, inventario,
   cache, lotes, vencimientos y notificaciones.
10. La cobertura global de SonarQube sigue pendiente
    de confirmacion tras publicar los cambios.

## 11. Proximas actividades

- Confirmar el Coverage global en SonarQube.
- Ampliar las pruebas de servicios de inventario.
- Implementar pruebas adicionales de la fachada.
- Fortalecer pruebas de integracion con PostgreSQL.
- Evaluar controladores JavaFX.
- Mantener la ejecucion automatizada en CI.
- Continuar hacia la meta general del 30-35 %.
