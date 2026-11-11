# CareStock — Documentación de arquitectura de software con C4
## 1. Diagrama de contexto — C4 nivel 1

<img width="913" height="382" alt="Diagrama de Contexto" src="https://github.com/user-attachments/assets/97db709b-bd43-4d00-ab70-065202d7505f" />


**Figura 1.** Vista de contexto del sistema CareStock, elaborada en Visual Paradigm Online.

### Objetivo

Identificar el sistema de software desde el punto de vista de sus usuarios y su dependencia del servicio externo de mensajería. En esta vista se representa **CareStock como una unidad**, sin descomponer su interfaz, servicios ni base de datos.

### Elementos

| Elemento | Clasificación | Responsabilidad o relación con CareStock |
|---|---|---|
| CareStock | Sistema de software | Gestionar inventarios farmacéuticos y productos de aseo y maternidad |
| Administrador | Persona | Utilizar las funciones administrativas autorizadas |
| Auxiliar | Persona | Consultar y registrar operaciones de acuerdo con sus permisos |
| Farmacéutico | Persona | Gestionar operaciones de inventario y dispensaciones autorizadas |
| Proveedor de SMS | Sistema externo | Entregar las notificaciones SMS solicitadas desde el sistema |

### Relaciones representadas

| Origen | Destino | Etiqueta en el diagrama |
|---|---|---|
| Administrador | CareStock | Usa |
| Auxiliar | CareStock | Usa |
| Farmacéutico | CareStock | Usa |
| CareStock | Proveedor de SMS | Usa servicio de SMS |
| Proveedor de SMS | Administrador | Envía alertas por SMS |

**Nota de alcance:** la relación de entrega al Administrador describe el escenario representado gráficamente. La identidad de los destinatarios efectivos y el proveedor activado dependen de la configuración de notificaciones; no se debe interpretar como una garantía de envío para todas las instalaciones.

## 2. Diagrama de contenedores — C4 nivel 2


<img width="885" height="626" alt="Diagrama de Contenedores" src="https://github.com/user-attachments/assets/8027fb8d-c294-4900-abf2-081e95d43ed6" />



**Figura 2.** Vista de contenedores de CareStock, elaborada en Visual Paradigm Online.

### Objetivo

Descomponer el sistema CareStock en sus unidades tecnológicas principales y mostrar sus interacciones con los usuarios, la base de datos y el proveedor externo de SMS.

### Contenedores dentro del límite de CareStock

| Contenedor | Tecnología | Responsabilidad |
|---|---|---|
| Aplicación de escritorio CareStock | Java 21 / JavaFX 21 | Interfaz gráfica, lógica de negocio y acceso a datos, integrados en la misma aplicación |
| Base de datos CareStock | PostgreSQL 16, instalación local o Neon | Persistencia de usuarios, productos, lotes, inventarios y movimientos |

Los **tres actores** y el **proveedor SMS** están fuera del límite del sistema. PostgreSQL se representa **dentro del límite del sistema CareStock**, aunque su infraestructura física pueda estar alojada externamente en Neon: sigue siendo un almacén de datos propio del sistema.

### Relaciones principales

| Origen | Destino | Función o tecnología |
|---|---|---|
| Administrador / Auxiliar / Farmacéutico | Aplicación de escritorio CareStock | Uso de la interfaz JavaFX |
| Aplicación de escritorio CareStock | Base de datos CareStock | Lectura y escritura de datos mediante JDBC / SQL; TLS cuando la conexión esté cifrada |
| Aplicación de escritorio CareStock | Proveedor de SMS | Solicitud de envío de notificaciones, según la integración configurada |

**Precisiones arquitectónicas:** no existe un backend HTTP independiente entre JavaFX y PostgreSQL en la arquitectura descrita. Si el proceso `sms/ProcesoSms.java` se despliega de forma autónoma en un entorno específico, corresponde documentar ese despliegue como un contenedor adicional o una vista complementaria, previa verificación de su ejecución real.

## 3. Diagrama de componentes — C4 nivel 3


<img width="976" height="787" alt="Diagrama de Componentes" src="https://github.com/user-attachments/assets/3d7b7b79-97a0-43c9-ad42-9def4ab0b968" />



**Figura 3.** Componentes lógicos de la aplicación de escritorio CareStock, elaborados en Visual Paradigm Online.

### Objetivo

Hacer zoom sobre **un solo contenedor del nivel 2**, la *Aplicación de escritorio CareStock (Java 21 / JavaFX 21)*. Los actores, el proveedor SMS y PostgreSQL se muestran únicamente para contextualizar sus entradas y dependencias externas.

### Componentes destacados en la vista gráfica

| Componente | Tecnología o clases representativas | Responsabilidad |
|---|---|---|
| Presentación JavaFX | JavaFX, FXML, controladores | Interacción, formularios, navegación y eventos |
| Autenticación y sesión | `AuthenticationService`, `SesionUsuario` | Verificación de credenciales y contexto de usuario |
| Fachada de inventario | `Inventario` — GoF Facade | Centralización de operaciones de inventario |
| Proxy de consultas | `ConsultaInventarioProxy` — GoF Proxy | Caché y segregación de consultas por farmacia |
| Servicios de negocio | `service/*` | Movimientos, alertas y planificación de reposición |
| Factory Method | `FabricaProducto` y fábricas concretas | Creación y validación de productos |
| Dominio y DTO | `model/*`, `dto/*` | Entidades, validaciones y transporte de datos |
| Persistencia DAO | `dao/*`, `CatalogoDAO` | SQL, operaciones CRUD y mapeo de resultados |
| Gestión de conexiones | `ConexionBD`, HikariCP | Administración de conexiones a PostgreSQL |

El **Template Method** se documenta como un comportamiento del componente de persistencia, a través de `CatalogoDAO`, aunque no se haya dibujado como una caja independiente en la imagen. **Facade, Proxy y Factory Method** se señalan como responsabilidades de sus componentes respectivos.

**Lectura del diagrama:** las flechas representan dependencias o colaboraciones lógicas; no necesariamente llamadas en red. Para validar relaciones entre clases concretas deben consultarse el diagrama UML y el código fuente. En la versión gráfica algunas etiquetas de estereotipo se repiten por limitaciones de edición; la clasificación correcta es `[Componente: Java]` o `[Componente: JavaFX / FXML]`, no `[Contenedor: Componente]`.

## 4. Detalle técnico y trazabilidad del nivel 3

A continuación se conserva la descripción técnica original del componente C4, incluyendo el modelo Mermaid complementario, el análisis de patrones GoF, los flujos y la correspondencia con archivos Java reales. **La Figura 3** ofrece una vista sintética para documentación; **Mermaid** profundiza en las dependencias y la sesión.

### Objetivo y límites

Identificar los componentes lógicos dentro de la **misma aplicación de escritorio** y sus dependencias.

En C4, un componente representa una agrupación de responsabilidades dentro de un contenedor, no necesariamente una clase individual ni un proceso independiente.

La arquitectura se organiza mediante el patrón **Modelo-Vista-Controlador (MVC)**, complementado con patrones GoF que permiten separar responsabilidades, controlar el acceso a los datos y simplificar la creación de objetos de dominio.

Los patrones implementados y representados son:

- **Factory Method (Creacional):** creación especializada de medicamentos, productos de aseo y maternidad.
- **Facade (Estructural):** centralización de operaciones del inventario mediante la clase `Inventario`.
- **Proxy (Estructural):** optimización de consultas mediante caché TTL y segregación por farmacia.
- **Template Method (Comportamiento):** reutilización del proceso de persistencia en `CatalogoDAO`.

**PostgreSQL/Neon está fuera del contenedor JavaFX.** La comunicación se realiza mediante JDBC y el pool de conexiones HikariCP.

### Diagrama de componentes

### Cómo leer el diagrama

#### 1. Presentación

La **presentación JavaFX** constituye el punto de entrada del usuario.

Los controladores gestionan eventos, formularios, validaciones de entrada e interacciones con los componentes internos.

Desde esta capa se invoca:

- `AuthenticationService` para autenticar usuarios.
- `Inventario` para operaciones de negocio.
- Las fábricas del patrón **Factory Method** para construir los objetos correspondientes a cada categoría.

La presentación no debería ejecutar directamente sentencias SQL.

#### 2. Factory Method: creación de productos

El patrón creacional **Factory Method reemplaza al anterior Builder** en la creación de productos del catálogo.

Su implementación se concentra en el paquete:

`org.example.carestock.factory`

La clase abstracta `FabricaProducto<T extends Validable>` define el mecanismo de creación, configuración y validación de productos.

Las implementaciones concretas son:

- `FabricaAseo`: crea instancias de `Aseo`.
- `FabricaMaternidad`: crea instancias de `Maternidad`.
- `FabricaMedicamento`: crea instancias de `Medicamento`.

**Flujo de creación desde JavaFX:**

1. El usuario selecciona la categoría del producto.
2. `CatalogoController` recopila los datos del formulario.
3. El controlador utiliza la fábrica correspondiente.
4. La fábrica concreta instancia el modelo de dominio.
5. Se configuran los atributos y se ejecuta la validación mediante `Validable`.
6. El producto creado se entrega a la fachada `Inventario` para completar el proceso de registro.

**Flujo de reconstrucción desde PostgreSQL:**

1. `AseoDAO` o `MaternidadDAO` ejecuta una consulta.
2. El DAO obtiene los atributos mediante `ResultSet`.
3. Utiliza su fábrica concreta para reconstruir el objeto de dominio.
4. Retorna el producto dentro de `RegistroCatalogo`.

La creación de objetos no sustituye las responsabilidades de persistencia ni autorización de la fachada.

#### 3. Fachada de inventario

La clase `Inventario` implementa el patrón estructural **Facade**.

Centraliza las operaciones de:

- Registro, consulta y actualización de productos.
- Gestión de medicamentos, aseo y maternidad.
- Administración de lotes.
- Dispensación de medicamentos.
- Consulta de inventario unificado.
- Invalidación de caché después de modificaciones.

La fachada coordina servicios y DAO, manteniendo el contexto de la farmacia activa.

No representa una API REST ni un componente ejecutable independiente.

#### 4. Proxy de consultas

`ConsultaInventarioProxy` implementa el patrón estructural **Proxy** y controla el acceso a consultas del inventario.

Sus responsabilidades principales son:

- Consultar información segregada por farmacia.
- Mantener resultados temporales en caché.
- Aplicar TTL y políticas LRU.
- Evitar consultas simultáneas duplicadas.
- Validar la autorización de farmacia.
- Invalidar resultados después de operaciones que modifican existencias.

Cuando no existe una entrada válida en caché, el proxy delega la consulta al componente real de persistencia.

#### 5. Persistencia y Template Method

Los DAO encapsulan la comunicación con PostgreSQL, evitando que los controladores gestionen directamente las consultas SQL.

El componente de persistencia incluye:

- `CatalogoDAO`
- `AseoDAO`
- `MaternidadDAO`
- `MedicamentoDAOImpl`
- `MedicamentoCatalogoDAO`
- `LoteDAOImpl`
- `UsuarioDAOImpl`
- `ConsultaInventarioDAO`

El patrón **Template Method** está presente en `CatalogoDAO`, que define operaciones comunes y permite que los DAO concretos especialicen los pasos relacionados con cada categoría.

Para productos de aseo y maternidad, los DAO también utilizan las fábricas correspondientes para reconstruir los objetos.

#### 6. Conexión a PostgreSQL

La clase `ConexionBD` administra el acceso a la base de datos.

Utiliza **HikariCP** para proporcionar conexiones reutilizables y reducir el costo de establecer una conexión nueva en cada consulta.

El acceso se realiza mediante:

`DAO / Servicios → ConexionBD → HikariCP → JDBC/TLS → PostgreSQL/Neon`

En esta arquitectura no existe un backend HTTP intermedio.

#### 7. Sesión y autorización

`SesionUsuario` mantiene el contexto del usuario autenticado, incluyendo:

- Identificador del usuario.
- Nombre y correo electrónico.
- Rol.
- Identificador de farmacia.
- Farmacia activa.

El contexto de sesión es consultado por los componentes que necesitan validar identidad o farmacia.

La fachada valida el contexto de las operaciones y el proxy restringe las consultas a la farmacia autorizada.

La sesión no administra directamente el pool de conexiones JDBC.

### Componentes y trazabilidad al código

| Componente C4 | Archivos y paquetes reales | Responsabilidad |
|---|---|---|
| Presentación | `CareStockApp.java`, `controller/*`, recursos FXML | Navegación, formularios y eventos JavaFX |
| Autenticación | `service/AuthenticationService.java`, `dao/UsuarioDAOImpl.java` | Verificación de credenciales mediante BCrypt |
| Sesión | `session/SesionUsuario.java` | Identidad, rol y farmacia activa |
| Fachada | `facade/Inventario.java` | Centralizar operaciones y validar el contexto de farmacia |
| Proxy | `proxy/ConsultaInventarioProxy.java`, `proxy/InventarioConsultas.java` | Caché por farmacia, TTL, LRU y autorización |
| Servicios | `service/RegistroLoteService.java`, `service/FarmacovigilanciaService.java`, `service/ControlVencimientosService.java` | Operaciones de negocio, dispensación y lotes |
| Factory Method | `factory/FabricaProducto.java`, `factory/FabricaAseo.java`, `factory/FabricaMaternidad.java`, `factory/FabricaMedicamento.java` | Crear, configurar y validar productos especializados |
| Dominio | `model/*` | Entidades y reglas de validación |
| DTO | `dto/*` | Transferencia de información entre componentes y vistas |
| DAO | `dao/*`, incluyendo `CatalogoDAO` y `ConsultaInventarioDAO` | SQL, reconstrucción de entidades y persistencia |
| Conexión | `config/ConexionBD.java` | Administración de conexiones JDBC mediante HikariCP |

### Reglas arquitectónicas que se deben mostrar

#### 1. Un solo contenedor ejecutable

CareStock es una aplicación de escritorio desarrollada con JavaFX.

Los componentes de presentación, dominio, servicios, seguridad y persistencia forman parte del mismo contenedor.

No son microservicios independientes.

#### 2. Modelo-Vista-Controlador (MVC)

Las vistas FXML presentan información y capturan entradas del usuario.

Los controladores coordinan las interacciones y delegan operaciones a las capas correspondientes.

El dominio representa los productos y sus reglas, mientras que los DAO encapsulan SQL.

#### 3. Factory Method

La creación de productos se delega a fábricas especializadas.

La clase abstracta `FabricaProducto` define el Factory Method `crearInstancia()`.

Las fábricas concretas determinan qué modelo se instancia.

Este patrón permite incorporar nuevas categorías mediante nuevas implementaciones, manteniendo una estructura uniforme de creación.

#### 4. Facade

`Inventario` constituye una interfaz de aplicación que centraliza las operaciones del inventario.

Su responsabilidad consiste en coordinar los componentes internos y aplicar las reglas relacionadas con el contexto del usuario y la farmacia.

#### 5. Proxy

`ConsultaInventarioProxy` controla el acceso a consultas y utiliza caché para optimizar el rendimiento.

Las operaciones deben respetar la segregación por farmacia.

La invalidación permite evitar que se mantengan datos obsoletos después de modificaciones.

#### 6. Template Method

`CatalogoDAO` define una secuencia común de operaciones de persistencia.

Los DAO especializados implementan los pasos variables sin duplicar completamente el algoritmo.

#### 7. Acceso a datos

La aplicación accede a PostgreSQL/Neon mediante conexiones proporcionadas por `ConexionBD`.

HikariCP administra el pool y JDBC ejecuta las operaciones SQL.

La base de datos se representa fuera del contenedor JavaFX.

#### 8. Seguridad y segregación por farmacia

La autenticación identifica al usuario y establece el contexto de sesión.

La fachada y los componentes responsables de consultas deben validar las restricciones aplicables a la farmacia.

La presencia de `SesionUsuario` no implica por sí sola que todas las operaciones estén autorizadas.

### Relación entre los patrones GoF implementados

Los patrones implementados desempeñan responsabilidades complementarias:

| Patrón | Categoría | Componente principal | Responsabilidad |
|---|---|---|---|
| Factory Method | Creacional | `FabricaProducto` y fábricas concretas | Creación y validación de productos |
| Facade | Estructural | `Inventario` | Coordinación de operaciones de negocio |
| Proxy | Estructural | `ConsultaInventarioProxy` | Caché y control de acceso a consultas |
| Template Method | Comportamiento | `CatalogoDAO` | Reutilización de algoritmos de persistencia |

Estos patrones colaboran sin reemplazar la arquitectura MVC.

### Flujo de registro de productos

El registro de un producto desde JavaFX sigue conceptualmente esta secuencia:

1. El usuario autenticado abre el catálogo.
2. Selecciona medicamentos, aseo o maternidad.
3. Completa los campos del formulario.
4. `CatalogoController` selecciona la fábrica correspondiente.
5. Factory Method crea y valida el objeto de dominio.
6. El controlador entrega el objeto a `Inventario`.
7. La fachada utiliza la farmacia activa del usuario.
8. Se ejecuta la operación de persistencia mediante el DAO correspondiente.
9. El sistema invalida las entradas de caché afectadas por el cambio.
10. La interfaz actualiza la información presentada al usuario.

Este flujo permite separar la creación de objetos, las reglas de negocio, la persistencia y la presentación.
