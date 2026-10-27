# Changelog — CareStock

Registro histórico de los cambios funcionales, arquitectónicos y de infraestructura realizados en **CareStock**, proyecto académico de gestión y trazabilidad de inventario hospitalario desarrollado por el Grupo 1 de Fundamentos de Ingeniería de Software de la Pontificia Universidad Javeriana.

El historial se presenta en orden cronológico inverso, conservando las decisiones arquitectónicas y los principales avances de los sprints.

> **Nota:** los cambios documentados como pendientes o en preparación no deben considerarse funcionalidades validadas hasta contar con las pruebas correspondientes.

---

## [0.1.0-beta.1] — 2026-10-27

### Primera versión preliminar de CareStock

Publicación de una versión beta del sistema académico de gestión y trazabilidad de inventario hospitalario.

### Funcionalidades

- Autenticación y gestión de sesiones mediante BCrypt.
- Administración de usuarios y contexto de farmacia.
- Gestión de medicamentos, productos de aseo y maternidad.
- Consulta unificada del inventario por farmacia.
- Búsqueda y filtrado de productos.
- Administración de lotes y existencias.
- Registro de entradas y salidas de inventario.
- Ajustes de existencias con validaciones de negocio.
- Registro de movimientos e historial de auditoría.
- Validaciones para preservar la consistencia del inventario.

### Arquitectura

- Aplicación de escritorio desarrollada en Java 21 y JavaFX 21.
- Gestión de dependencias mediante Apache Maven.
- Persistencia mediante PostgreSQL/Neon y JDBC.
- Pool de conexiones mediante HikariCP.
- Separación de responsabilidades mediante MVC, servicios, DAO y DTO.

### Patrones de diseño GoF

- Proxy: optimización y control de consultas del inventario.
- Facade: centralización de operaciones de inventario.
- Factory Method: creación especializada de productos.
- Template Method: reutilización de procedimientos comunes.

### Infraestructura

- Integración continua mediante GitHub Actions.
- Compilación y verificación mediante Maven.
- Configuración externa de conexión a la base de datos.
- Estructura de documentación y seguimiento del desarrollo.

### Alcance de la versión

Esta versión se publica como pre-release académica.

Las integraciones de alertas y SMS que todavía estén en desarrollo quedan fuera del alcance de esta publicación.

Las funcionalidades requieren validación de sus escenarios operativos antes de su utilización en producción.

---
## [En preparación] — 2026-10-14 — Consolidación de estructura Maven, Docker y CI/CD

**Rama:** `refactor/estructura-unica-carestock`

### Cambiado

- Se definió `src/CareStock` como el directorio oficial y único proyecto Maven de la aplicación JavaFX.
- Se reorganizó la infraestructura para eliminar los archivos Maven redundantes de la raíz del repositorio: `pom.xml`, `mvnw`, `mvnw.cmd` y la configuración `.mvn/` duplicada.
- Se adaptó la configuración de construcción al entorno de **Java 21**.
- Se actualizaron las rutas utilizadas por GitHub Actions para compilar y empaquetar el módulo ubicado en `src/CareStock`.
- Se reorganizó la configuración de Docker, utilizando el `Dockerfile` del módulo oficial y actualizando el contexto de construcción de `compose.yaml`.
- Se incorporó una configuración de ejecución JavaFX en Linux mediante una pantalla virtual con Xvfb.
- Se actualizó el README para describir la arquitectura, instalación, ejecución, componentes y organización actual del repositorio.

### Seguridad

- Se estableció la exclusión de `src/CareStock/config/conexion-local.properties` del control de versiones.
- Se mantuvo una plantilla `.example` como referencia para configurar conexiones sin publicar credenciales.
- Se configuró Docker para montar el archivo local de conexión en modo de solo lectura, sin incorporarlo directamente en la imagen.

### Verificación

- Se obtuvo `BUILD SUCCESS` en la compilación de referencia de `Proxypatch`, con 52 archivos Java compilados.
- Se corrigió la integración de `main` que previamente presentaba 97 errores por ausencia de `LoteDao` y `ReglaNegocioException` en el árbol compilado.
- Queda pendiente registrar la evidencia definitiva de CI/CD y Docker después de la refactorización completa.

---

## 2026-10-10 al 2026-10-14 — Optimización de consultas y gestión de conexiones

### Agregado

- Se incorporó **HikariCP** como pool de conexiones JDBC para PostgreSQL/Neon.
- Se implementó la infraestructura del patrón **Caching Proxy** para reutilizar resultados de las consultas unificadas del inventario.
- Se incorporaron clases especializadas para la consulta real, la intermediación Proxy y la administración del caché.
- Se agregaron los componentes `CacheFarmacia` y `VistasCache` para gestionar almacenamiento temporal de resultados por farmacia.
- Se incorporaron DTO como `DatosCatalogo` y `DatosLotes`, orientados a agrupar los datos consultados.

### Cambiado

- Se actualizó `ConexionBaseDatos` para administrar conexiones reutilizables mediante HikariCP, reduciendo la necesidad de crear conexiones físicas repetidamente.
- Se incorporó la lectura de credenciales desde `config/conexion-local.properties`.
- Se habilitaron parámetros configurables para el tamaño del pool de conexiones.
- Se incorporaron políticas de expiración, invalidación y control de concurrencia dentro de los componentes de caché.
- Se mantuvo la segregación de las consultas según la farmacia activa del usuario autenticado.

### Verificación

- Se comprobó localmente la apertura y reutilización de conexiones mediante consultas de prueba a PostgreSQL.
- Se verificó la disponibilidad de conexiones y el comportamiento básico de los indicadores del pool.
- Se obtuvo una compilación Maven exitosa con las nuevas dependencias.
- La integración efectiva de las distintas vistas con los componentes de caché y su comportamiento frente a operaciones concurrentes requiere pruebas funcionales específicas.

### Observaciones

- La optimización con caché no reemplaza las validaciones de permisos ni el aislamiento de datos por farmacia.
- Las modificaciones de inventario deben acompañarse de la invalidación correspondiente para evitar datos obsoletos.

---

## 2026-10-13 — Evolución arquitectónica y patrones GoF

### Diseño y planificación

- Se planteó la consolidación de la gestión de productos mediante una interfaz gráfica unificada.
- Se revisó la utilización de patrones GoF para disminuir el acoplamiento entre controladores, servicios y componentes de persistencia.
- Se documentaron propuestas de aplicación del patrón **Proxy** para optimizar consultas recurrentes.
- Se analizó el uso de **Observer** y **Proxy** para mejorar la trazabilidad de operaciones asociadas al usuario autenticado.
- Se definieron historias orientadas al formulario único de productos y a la optimización del inventario.

### Historias relacionadas

- **HU.52 — #287:** registrar la identidad del usuario activo en las operaciones de dominio.
- **HU.92 — #680:** registrar productos desde un único formulario.
- **HU.97 — #682:** optimizar las consultas del inventario mediante caché y patrón Proxy.
- **#712:** implementar Caching Proxy para las consultas unificadas del inventario.

> La definición de una historia o de un diseño GoF no implica que todos sus criterios de aceptación se encuentren implementados.

---

## 2026-10-06 al 2026-10-10 — Ampliación del catálogo y refactorización del dominio

### Agregado

- Se amplió el dominio de CareStock para contemplar **medicamentos, productos de aseo y productos de maternidad**.
- Se incorporaron modelos especializados para representar diferentes categorías de productos.
- Se desarrollaron clases Builder para facilitar la construcción y validación de objetos del dominio.
- Se trabajó en formularios JavaFX y componentes de persistencia para registrar productos de aseo y maternidad.
- Se incorporaron funcionalidades de consulta y visualización del catálogo de productos.

### Cambiado

- Se revisó y simplificó el diseño de las interfaces del modelo, diferenciando las abstracciones del dominio de las clases de persistencia.
- Se avanzó en la migración hacia **clases DAO concretas**, evitando interfaces DAO innecesarias dentro del diseño adoptado.
- Se reorganizaron dependencias, paquetes y archivos de configuración del proyecto Maven.
- Se consolidó la compilación de la aplicación desde `src/CareStock`.
- Se utilizó el paquete base `org.example.carestock` para la estructura JavaFX actual.

### Historias relacionadas

- **HU.76 — #577:** registrar productos de aseo y maternidad.
- **#612:** implementar registro de productos de aseo y maternidad en modelo, servicio y persistencia.
- **#613:** adaptar el formulario JavaFX para productos de aseo y maternidad.

### Verificación

- Se realizaron ajustes de estructura y dependencias Maven hasta obtener compilaciones exitosas.
- Se revisaron los módulos, controladores y recursos FXML afectados por los cambios.
- Queda sujeta a validación integral la cobertura de todos los escenarios funcionales del catálogo y del registro de movimientos.

---

## 2026-09-28 al 2026-10-03 — Multifarmacia, inventario y transacciones JDBC

### Agregado

- Se incorporó el concepto de **farmacia activa** al contexto de usuario.
- Se desarrollaron mecanismos para asociar usuarios a farmacias registradas en PostgreSQL.
- Se avanzó en la visualización de inventario segregado por farmacia.
- Se incorporaron validaciones para restringir consultas y operaciones según la farmacia asignada.
- Se trabajó en el registro de movimientos de inventario y su trazabilidad mediante estructuras de kardex.
- Se incorporó una implementación transaccional de operaciones de inventario mediante JDBC.

### Cambiado

- Se sustituyó la selección manual de farmacia en determinados flujos por el contexto obtenido de la sesión autenticada.
- Se revisaron las consultas y operaciones DAO para utilizar identificadores de farmacia al acceder a los registros.
- Se incorporó el control explícito de transacciones mediante `setAutoCommit(false)`, `commit()` y `rollback()`.
- Se buscó garantizar que la actualización de existencias y el registro del movimiento correspondiente se ejecuten de forma atómica.
- Se revisaron los componentes `LoteDao`, `DespachoService` y las operaciones de inventario involucradas en las escrituras.

### Historias y tareas relacionadas

- **HU.67:** consultar el inventario correspondiente a la farmacia activa.
- **HU.68:** asociar usuarios a las farmacias existentes.
- **#444:** integridad transaccional de operaciones JDBC.
- **#546:** implementar manejo transaccional de movimientos de stock.
- **#547:** garantizar atomicidad entre inventario y kardex.
- **#471:** evaluación de la ejecución contenerizada con Docker.

### Verificación

- Se realizaron compilaciones y pruebas manuales de los cambios integrados.
- Se comprobó la construcción inicial de imágenes Docker y la ejecución en un entorno Linux controlado.
- La consistencia ante escrituras concurrentes y los casos de rollback deben mantenerse dentro de las prioridades de pruebas automatizadas.

---

## 2026-09-22 al 2026-09-27 — Autenticación y gestión de sesiones

### Agregado

- Se incorporó el inicio de sesión de usuarios mediante correo electrónico y contraseña.
- Se integró **BCrypt** para la verificación segura de contraseñas.
- Se introdujo el contexto de sesión como mecanismo para identificar al usuario autenticado.
- Se agregó la identificación visual de la sesión activa dentro de la interfaz JavaFX.
- Se implementaron componentes para cerrar la sesión y limpiar su contexto.
- Se incorporaron controles relacionados con rutas protegidas y restricciones de acceso sin sesión.

### Cambiado

- Se eliminó la necesidad de seleccionar manualmente la identidad del usuario para determinadas operaciones.
- Se incorporó la identidad de sesión en los procesos de registro y trazabilidad.
- Se revisaron las validaciones de autenticación y los mensajes mostrados ante credenciales incorrectas.
- Se realizaron ajustes para controlar el acceso a funciones que requieren autenticación.

### Historias relacionadas

- **HU.51:** autenticación de usuarios.
- **HU.52:** contexto de sesión e identidad del usuario.
- **HU.53:** cierre de sesión seguro.
- **HU.58:** cambio de contraseña, con trabajo de implementación y ajustes de interfaz durante los sprints posteriores.

### Consideraciones de seguridad

- Las contraseñas no deben almacenarse en texto plano.
- Los mensajes de error de autenticación deben evitar revelar información innecesaria sobre la existencia de una cuenta.
- El cierre de sesión debe invalidar la identidad utilizada por los controladores y servicios.

---

## 2026-09-18 — Integración inicial de estructura

### Agregado

- Se integró el código Java inicial bajo el paquete `com.carestock`, correspondiente a la estructura utilizada en esa etapa del proyecto.
- Se incorporaron los modelos `Lote` y `Ubicacion` sin duplicar la entidad `Medicamento`.
- Se agregaron componentes de persistencia `LoteDao`, `UbicacionDAO` y `UsuarioDao`.
- Se incorporó `IngresoLoteService` para coordinar el registro de lotes.
- Se agregó Maven con dependencias de JavaFX, PostgreSQL JDBC y JUnit.
- Se incorporaron carpetas de configuración, scripts auxiliares y workflows de CI y empaquetado.

### Cambiado

- Se migró el controlador de ingreso de lotes al paquete `com.carestock.controller`.
- Se corrigió `IngresoLoteValidator` y se incorporaron pruebas unitarias en esa etapa.
- Se ubicó el formulario FXML en `src/main/resources/com/carestock/view/IngresoLote.fxml`, según la estructura histórica.
- Se integró el formulario de ingreso de lotes al Dashboard.
- Se habilitó la actualización de métricas y existencias después del registro.
- Se eliminaron dependencias de drivers `.jar` versionadas manualmente.
- Se trasladó la configuración de conexión a variables de entorno utilizadas en la implementación de ese momento.
- Se actualizó el README para describir la arquitectura inicial.

> **Nota histórica:** las rutas `com.carestock` y la configuración mediante variables de entorno pertenecen a la integración del 18 de septiembre. No representan la organización oficial vigente, actualmente basada en `src/CareStock`, `org.example.carestock` y `config/conexion-local.properties`.

---

## Limitaciones y mejoras pendientes

### Calidad y pruebas

- Incrementar la cobertura de pruebas unitarias y de integración.
- Verificar automáticamente las reglas de negocio para los diferentes tipos de producto.
- Comprobar la consistencia transaccional entre inventario y kardex.
- Incorporar escenarios de concurrencia y prevención de stock negativo.
- Validar el aislamiento de información entre farmacias.
- Medir la efectividad del Caching Proxy y comprobar la invalidación después de escrituras.
- Confirmar que los workflows de GitHub Actions ejecuten verificaciones reproducibles en cada Pull Request.

### Infraestructura

- Revisar las advertencias del sistema modular JPMS relacionadas con `jbcrypt-0.4.jar`.
- Evaluar el reemplazo de API obsoletas utilizadas por `PruebaConexion`.
- Completar y verificar la consolidación de Maven, Docker y CI/CD.
- Mantener una única estructura oficial para la aplicación Java.
- Revisar la organización de archivos de prueba y recursos auxiliares fuera del módulo Maven.

### Seguridad

- Mantener las credenciales de PostgreSQL/Neon fuera del repositorio.
- Rotar cualquier contraseña que haya sido expuesta anteriormente.
- Mantener los controles de autorización basados en sesión y farmacia activa.
- Evitar registrar información sensible en archivos de log.

---

## Criterios para registrar cambios futuros

Cada entrada nueva debe identificar:

- **Fecha o versión:** cuándo se incorporó el cambio.
- **Agregado:** funcionalidades o componentes nuevos.
- **Cambiado:** refactorizaciones o modificaciones de comportamiento.
- **Corregido:** defectos solucionados.
- **Seguridad:** medidas implementadas para proteger datos o accesos.
- **Verificación:** resultados comprobables de compilación y pruebas.
- **Referencias:** Issues o Pull Requests asociados, cuando corresponda.

Los cambios deben registrarse preferiblemente después de su integración o señalarse explícitamente como pendientes mientras permanezcan en una rama de desarrollo.

---

## Nota general de seguridad

CareStock utiliza PostgreSQL/Neon como sistema de persistencia. Ninguna contraseña real, cadena de conexión con credenciales ni archivo privado de configuración debe publicarse en GitHub.

Si una contraseña estuvo previamente incluida en el repositorio, en un commit histórico o en archivos compartidos, debe rotarse en el proveedor de base de datos. La eliminación del archivo en un commit posterior no elimina automáticamente la credencial del historial Git.

---

**CareStock — Grupo 1**

**Pontificia Universidad Javeriana — Fundamentos de Ingeniería de Software**

*Última actualización documental: 2026-10-14.*
