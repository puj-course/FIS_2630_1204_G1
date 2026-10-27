# Plan Maestro de Pruebas de Software — CareStock

> **Proyecto:** CareStock — Sistema de gestión de inventario para farmacias  
> **Versión del plan:** 1.0 (propuesta inicial)  
> **Fecha de elaboración:** 27 de octubre de 2026  
> **Estado:** Planificación; ejecución y cobertura por verificar  
> **Ubicación:** `docs/PLAN_MAESTRO_PRUEBAS_CARESTOCK.md`  
> **Código principal:** `src/CareStock/` (Java 21, JavaFX 21, Maven, PostgreSQL/Neon)

## 1. Propósito

Establecer una estrategia verificable, incremental y automatizable de aseguramiento de calidad (QA) para CareStock. Este plan define **qué se probará, cómo, con qué herramientas, en qué orden, bajo qué condiciones y con qué evidencias**, sin exigir la reescritura del sistema existente.

La planificación utilizará dos ejes complementarios:

1. **Módulos técnicos:** `model`, `dto`, `factory`, `service`, `dao`, `session`, `proxy`, `facade`, `controller` y `config`.
2. **Procesos de negocio:** autenticación, permisos, segregación por farmacia, catálogo, medicamentos/lotes, aseo, maternidad, entradas, salidas, ajustes, auditoría y vencimientos.

**Regla central:** una prueba unitaria demuestra el comportamiento de una unidad; **no** sustituye una prueba de integración, ni demuestra por sí sola el funcionamiento de un flujo completo.

## 2. Objetivos de calidad

- Verificar el cumplimiento de requisitos funcionales y no funcionales, vinculando pruebas con historias de usuario y criterios de aceptación.
- Detectar defectos antes de integrar cambios mediante pruebas automáticas en los Pull Requests.
- Prevenir regresiones al modificar código, dependencias, configuraciones o reglas de inventario.
- Demostrar la integridad del stock, la atomicidad de las transacciones y la trazabilidad de operaciones.
- Verificar los límites de acceso por rol, sesión y farmacia activa.
- Medir cobertura de código, calidad estática, tiempos de respuesta y comportamiento bajo concurrencia.
- Establecer evidencia reproducible para revisiones académicas, Sprint Reviews y releases.

## 3. Alcance y límites

### 3.1 Incluye

- Pruebas **estáticas**: revisiones de código, convenciones, análisis SonarQube y revisión de dependencias/configuración.
- Pruebas **dinámicas**: unitarias, integración, funcionales/UI, seguridad, concurrencia, rendimiento, carga, estrés y regresión.
- Flujos críticos de inventario en sus tres categorías: medicamentos, aseo y maternidad.
- Pruebas con datos artificiales, independientes y repetibles.
- Automatización progresiva mediante Maven y GitHub Actions.

### 3.2 No incluye inicialmente

- Pruebas de producción que modifiquen inventario real o provoquen saturación de la base de datos operativa.
- Suponer que CareStock expone una API REST: es principalmente una **aplicación de escritorio JavaFX**.
- Garantizar ausencia total de defectos: la cobertura y las herramientas reducen riesgos, no prueban corrección absoluta.
- Considerar un indicador de cobertura como sustituto de criterios de aceptación, pruebas de seguridad o revisión humana.

## 4. Línea base del repositorio — 27/10/2026

| Elemento | Estado observado en el ZIP revisado | Acción inicial |
|---|---|---|
| JUnit 5 | Dependencia declarada en `src/CareStock/pom.xml` | Mantener y ampliar |
| Maven Surefire | Plugin configurado para pruebas | Confirmar ejecución y reportes |
| GitHub Actions | `.github/workflows/ci.yml` ejecuta `./mvnw -B -ntp clean verify` | Agregar reportes y controles gradualmente |
| Pruebas en ruta Maven estándar | 6 archivos `*Test.java` en `src/CareStock/src/test/java/`, con 42 anotaciones `@Test` identificadas | Ejecutar y clasificar su alcance |
| Copia anidada | Otra prueba de ajustes en `src/CareStock/src/CareStock/src/test/java/` | Revisar duplicación; no contarla como parte de la suite Maven estándar |
| JaCoCo | No declarado en el `pom.xml` revisado | Integrar para cobertura |
| Mockito | No declarado en el `pom.xml` revisado | Integrar cuando existan dependencias simulables |
| SonarQube | Integración no observada en CI | Definir SonarQube Cloud o servidor y configurar análisis |
| JMeter | No se observó plan automatizado | Diseñar escenarios controlados más adelante |
| Documentación QA | Ya existe `docs/qa/` con documentos y `docs/qa/evidence/` | Conservar y organizar progresivamente |

**Importante:** el inventario anterior describe archivos y configuración, **no** certifica que las pruebas hayan pasado ni informa un porcentaje real de cobertura. No se registra una línea base de ejecución hasta obtener los reportes de Maven y JaCoCo.

## 5. Enfoque de diseño de pruebas

### 5.1 TDD para funcionalidades nuevas

Aplicar ciclos cortos **RED → GREEN → REFACTOR**:

1. Definir una regla observable y su criterio de aceptación.
2. Crear primero una prueba automatizada que falle por ausencia del comportamiento.
3. Implementar el mínimo código necesario para hacerla pasar.
4. Refactorizar sin cambiar el resultado esperado.
5. Incorporar la prueba a la suite de regresión.

Para funcionalidades **ya desarrolladas**, utilizar pruebas de caracterización y regresión, y refactorizar después de establecer un comportamiento conocido; no reescribir componentes únicamente para declarar que se usó TDD.

### 5.2 Principios obligatorios

- Una prueba debe comprobar **un comportamiento claramente identificable**; un método puede necesitar múltiples casos.
- Incluir casos positivos, negativos, límites, excepciones y, cuando corresponda, concurrencia.
- Las pruebas unitarias deben ser rápidas, autocontenidas y deterministas; usar Mockito para aislar dependencias externas cuando sea pertinente.
- No simular con mocks la persistencia en pruebas cuyo objetivo sea verificar SQL, transacciones o restricciones reales.
- No depender del orden de ejecución de los tests, de usuarios reales ni de datos persistidos por otras pruebas.
- Nombrar claramente los casos y registrar precondiciones, entradas, resultado esperado y postcondiciones.
- Mantener la **trazabilidad** entre historia/issue, regla de negocio, caso, automatización y evidencia.

## 6. Matriz de pruebas por módulo

| Prioridad | Módulo/capa | Ejemplos de comportamiento | Enfoque y herramientas |
|---|---|---|---|
| P0 | `service` | Entradas/salidas, ajustes, validación de cantidades, inventario no negativo, errores | JUnit 5 + Mockito; integración transaccional |
| P0 | `dao` | CRUD, filtrado por farmacia, bloqueo/concurrencia, commit, rollback, auditoría | JUnit 5 + PostgreSQL aislado; Testcontainers a evaluar |
| P0 | `session` / autenticación | Sesión válida, roles, sesión ausente/caducada, farmacia asignada | JUnit + integración |
| P0 | `facade` | Validar farmacia/usuario activos, autorización y coordinación de operaciones | JUnit + Mockito + integración |
| P1 | `model` | Entidades `Medicamento`, `Lote`, `Aseo`, `Maternidad`, `Usuario` y sus reglas | JUnit 5 |
| P1 | `proxy` | Aciertos/fallos caché, TTL, invalidación, aislamiento por farmacia, concurrencia | JUnit 5 + dobles de prueba |
| P1 | `factory` | Fabricación de productos válidos/inválidos | JUnit 5 |
| P1 | `dto` | Filtros, normalización y transporte de datos | JUnit 5 |
| P1 | `controller` | Formularios, validación de entradas, navegación, mensajes, estados UI | Pruebas funcionales JavaFX y revisión manual guiada |
| P2 | `config` | Inicialización JDBC, configuración de HikariCP, errores de conexión | Integración y rendimiento |

**Clasificación:** P0 = afecta integridad, seguridad o trazabilidad; P1 = funcionalidad significativa; P2 = infraestructura y optimización que se abordará una vez controlados los riesgos P0. Las prioridades pueden modificarse ante incidencias.

## 7. Matriz de pruebas por proceso de negocio

| ID de área | Proceso | Escenarios mínimos | Riesgo |
|---|---|---|---|
| AUT | Autenticación y permisos | Credenciales válidas/inválidas, BCrypt, cierre, expiración, acción sin rol | Crítico |
| FAR | Segregación por farmacia | Usuario A no accede a datos de B; farmacia inactiva; sesión sin farmacia | Crítico |
| INV | Catálogo e inventario | Consulta con/sin stock, filtros, estados, alta/cambio/baja permitidos | Alto |
| LOT | Medicamentos y lotes | Disponibilidad, vencimientos, restricciones sanitarias, operación por lote | Alto |
| MOV | Entradas y salidas | Cantidad positiva, salidas mayores al stock, transacción simultánea, registro | Crítico |
| AJU | Ajustes | Valor final no negativo, motivo obligatorio, diferencia y auditoría | Crítico |
| AUD | Historial y trazabilidad | Fecha, usuario, farmacia, tipo, cantidad, motivo y filtros | Alto |
| CAC | Proxy y caché | TTL, hit/miss, invalidación tras escritura y segregación | Alto |
| UI | JavaFX | Validación visible, manejo de errores, permisos en navegación | Medio/alto |
| RND | Rendimiento | Latencia, throughput, recursos y pool de conexiones | Alto |

Vincular cada área con las HU/issues exactas al elaborar sus casos. Como punto de partida, priorizar las funcionalidades de movimientos, ajustes e historial (issues `#789` a `#796`, según corresponda), y la segregación multifarmacia.

## 8. Herramientas y responsabilidades

| Herramienta | Uso previsto | Observaciones |
|---|---|---|
| **JUnit 5** | Aserciones, excepciones, casos parametrizados y ejecución de tests | Ya declarada |
| **Mockito** | Simular DAO, dependencias de servicios y verificar interacciones | Planificado; un mock no prueba PostgreSQL real |
| **Maven Surefire** | Ejecutar suite unitaria en `test`/`verify` | Ya configurado |
| **JaCoCo** | Generar cobertura de líneas y ramas; XML para SonarQube | Planificado; medir antes de imponer umbrales globales |
| **SonarQube** | Análisis estático, mantenibilidad, revisión de hallazgos y Quality Gate | Planificado; no sustituye pruebas dinámicas |
| **Testcontainers** | Base PostgreSQL efímera para integración | A evaluar; requiere entorno de contenedores |
| **Apache JMeter** | Pruebas repetibles de carga y estrés, métricas y reportes | Planificado; ejecución separada de los tests unitarios |
| **GitHub Actions** | Automatización y publicación de artefactos/reporte | CI actual como punto de partida |

Para SonarQube, utilizar la opción de despliegue que decida el equipo (Cloud o servidor) y configurar de forma segura tokens y variables en el repositorio. No incluir secretos en archivos Markdown, logs ni ejemplos versionados.

## 9. Estrategia de rendimiento, carga y estrés

### 9.1 Particularidad arquitectónica

CareStock es una aplicación JavaFX con acceso a PostgreSQL por JDBC. **JMeter no debe asumir endpoints HTTP inexistentes**. Se contemplan dos niveles:

1. **JMeter con JDBC:** evaluar consultas SQL, latencia de base de datos y concurrencia de conexiones con datos sintéticos. Esto **no** representa automáticamente el tiempo completo de las operaciones de negocio.
2. **Harness Java de carga/integración:** invocar servicios reales desde un ejecutor de pruebas o un sampler Java correctamente aislado, para medir reglas y transacciones; debe evitar efectos no deseados en producción.

Para tiempos de interfaz JavaFX se requieren mediciones o automatización de UI independientes: ni JDBC ni los samplers Java representan por sí solos el tiempo de renderizado.

### 9.2 Escenarios exploratorios

| Escenario | Concurrencia de referencia | Objetivo |
|---|---:|---|
| Línea base | 1 cliente | Referencia sin competencia |
| Carga baja | 5 clientes | Comparar latencias e identificar errores |
| Carga moderada | 20 clientes | Observar bloqueos y uso de conexiones |
| Estrés controlado | 50 clientes o incremento gradual | Hallar saturación y evaluar recuperación |

Estas cargas son **hipótesis para calibración**, no niveles de servicio comprometidos. Definir duración, rampa, conjunto de datos y umbrales luego de medir el entorno de pruebas y el número esperado de usuarios de CareStock.

**Indicadores obligatorios:** latencia mediana, p95 y p99; transacciones/segundo; tasa de fallos; conexiones activas/espera; CPU y memoria cuando haya observabilidad; consistencia final del stock y auditoría después de las pruebas.

**Experimentos específicos:**

- Salidas concurrentes del mismo producto/lote: impedir stock negativo y escrituras parciales.
- Consultas simultáneas de inventario por farmacias diferentes: impedir filtración de resultados.
- Cache **activada vs. desactivada**: medir beneficio real del Proxy con igualdad de datos y carga.
- Capacidad y espera del pool HikariCP: determinar cuándo se degrada el rendimiento.
- Fallos y recuperación de BD: comprobar control de errores y ausencia de movimientos parciales.

No realizar carga ni estrés sobre la BD Neon operativa. Usar un entorno aislado o una instancia expresamente autorizada para pruebas; limpiar y restaurar los datos en cada ejecución.

## 10. Seguridad, concurrencia y persistencia

Los casos de mayor riesgo deben verificar **efectos persistidos**, no solo mensajes de la interfaz:

- **Stock:** `stock_final >= 0`, incluso con operaciones simultáneas; el total debe coincidir con los movimientos confirmados.
- **Atomicidad:** ante un fallo intermedio, no debe persistirse un movimiento sin su actualización de inventario, ni viceversa.
- **Aislamiento:** un usuario de farmacia A no consulta ni modifica datos de farmacia B, aun usando parámetros manipulados.
- **Autorización:** una pantalla oculta no equivale a una regla de seguridad; los servicios deben rechazar acciones no autorizadas.
- **Trazabilidad:** actor, farmacia, fecha, producto/lote, tipo, cantidad y motivo deben corresponder a la operación confirmada.
- **Confidencialidad:** excepciones visibles y artefactos de prueba no exponen contraseñas, JDBC URLs, tokens ni datos reales.

Los hallazgos de SonarQube deben revisarse y clasificarse: el análisis estático no reemplaza pruebas dinámicas de autorización ni evaluaciones de seguridad especializadas.

## 11. Ambientes, datos de prueba y aislamiento

| Ambiente | Uso | Restricciones |
|---|---|---|
| Local | Pruebas unitarias, mocks y TDD | No requerir red/BD para tests unitarios puros |
| CI Pull Request | Compilación, unitarias, cobertura y análisis estático | Sin credenciales productivas; ejecución rápida |
| Integración aislada | DAO y servicios con PostgreSQL de prueba | Datos artificiales; esquema reproducible y limpieza |
| Rendimiento aislado | JMeter y carga sobre PostgreSQL/servicios de ensayo | Cargas autorizadas, métricas y recursos medidos |
| Producción | Operación real | **No ejecutar pruebas destructivas, carga o estrés** |

Los fixtures deben incluir como mínimo dos farmacias activas, una inactiva, usuarios de roles distintos, productos de las tres categorías, lotes vencidos/vigentes y existencias suficientes/insuficientes. No versionar datos personales reales.

La inicialización de datos y el aislamiento entre tests deben ser reproducibles mediante scripts, transacciones revertidas o contenedores efímeros, según el tipo de prueba.

## 12. Objetivos, métricas y criterios de aceptación

### 12.1 Cobertura

- **Fase inicial:** medir la cobertura efectiva sin afirmar un porcentaje previo.
- **Objetivo evolutivo:** alcanzar al menos **80 % de cobertura de líneas del código de negocio elegible**; vigilar además cobertura de ramas y calidad de aserciones.
- **Código nuevo:** proponer un Quality Gate con cobertura >= 80 % del código nuevo **cuando la línea base y exclusiones estén acordadas**.
- No excluir clases difíciles únicamente para mejorar la métrica; documentar exclusiones justificadas (por ejemplo, clases de lanzamiento sin lógica).
- No interpretar 80 % de cobertura como 80 % de requisitos aprobados.

### 12.2 Aprobación de una prueba

Una prueba se considera aprobada cuando el comportamiento observado coincide con las aserciones, las postcondiciones se cumplen, el resultado es repetible y no deja cambios no controlados en otros datos.

### 12.3 Criterios iniciales para fusionar un PR

1. Compilación y pruebas unitarias requeridas en verde.
2. Ninguna regresión conocida de severidad crítica o alta sin decisión explícita y documentada.
3. Casos automatizados para toda nueva regla de negocio o corrección de defecto, cuando sean factibles.
4. Análisis SonarQube y Quality Gate obligatorio **solo desde su activación formal**.
5. Documentación de pruebas y criterios de aceptación actualizados cuando corresponda.

### 12.4 Criterios de salida antes de un release

- Casos P0 ejecutados y aprobados, con excepciones justificadas y aceptadas por el equipo.
- Suite de regresión y pruebas de integración críticas aprobadas.
- Sin incidentes críticos abiertos que comprometan integridad del inventario, seguridad o trazabilidad.
- Reportes guardados con versión/tag, commit, fecha, entorno y responsable.
- Desempeño comparado contra la línea base; objetivos p95, tasa de error y capacidad definidos antes de usar rendimiento como criterio bloqueante.

## 13. Flujo de ejecución automatizada propuesto

### Por Pull Request (rápido)

1. Checkout y configuración de Java 21.
2. Compilar y ejecutar JUnit mediante Maven.
3. Generar reporte JaCoCo cuando esté integrado.
4. Ejecutar SonarQube y evaluar Quality Gate cuando esté configurado.
5. Publicar reportes como artefactos de CI; bloquear integración si falla un control obligatorio.

### Por integración programada o antes de release (más costoso)

1. Crear PostgreSQL aislado y cargar fixtures.
2. Ejecutar pruebas DAO/integración, concurrencia y flujos críticos.
3. Ejecutar escenarios JMeter de forma controlada.
4. Analizar tendencias de latencia, errores e integridad de datos.
5. Registrar evidencias vinculadas a versión y commit.

**No conectar JMeter al flujo obligatorio de cada PR al comienzo:** las pruebas de carga son más lentas y sensibles al entorno; convertirlas en controles bloqueantes solo cuando la infraestructura y los umbrales sean estables.

### Comandos disponibles hoy

Desde `src/CareStock/`:

```bash
./mvnw -B -ntp clean test
./mvnw -B -ntp clean verify
```

En Windows con Maven Wrapper:

```powershell
.\mvnw.cmd -B -ntp clean verify
```

Los comandos de cobertura JaCoCo, análisis SonarQube, integración aislada y JMeter se documentarán **después** de incorporarlos; este plan no afirma que estén configurados actualmente.

## 14. Estructura futura de documentación

**Crear progresivamente** las subcarpetas conforme se implementen pruebas. Se conservarán los documentos y evidencias actualmente ubicados en `docs/qa/`.

```text
docs/
├── PLAN_MAESTRO_PRUEBAS_CARESTOCK.md   # Este documento
└── qa/                                 # Ya existe
    ├── [documentos QA existentes]       # Conservar y referenciar
    ├── evidence/                        # Ya existe; conservar
    ├── 01_estrategia/                   # Alcance, riesgos y cronograma
    ├── 02_casos_prueba/                 # Matrices y casos por módulo/HU
    │   ├── unitarias/
    │   ├── integracion/
    │   ├── funcionales_ui/
    │   └── seguridad/
    ├── 03_rendimiento/                  # JMeter: escenarios y umbrales
    ├── 04_regresion/                    # Suites y checklist por release
    ├── 05_reportes/                     # Resúmenes de cobertura y calidad
    └── 06_plantillas/                   # Formatos reutilizables
```

Las **pruebas Java ejecutables** seguirán en `src/CareStock/src/test/java/...` y los recursos correspondientes en `src/CareStock/src/test/resources/` cuando se creen. Los planes JMeter ejecutables (`.jmx`) se ubicarán en una carpeta de pruebas del código o de infraestructura que se acuerde posteriormente; la documentación de sus escenarios permanecerá en `docs/qa/03_rendimiento/`.

Los reportes generados (`target/`, resultados extensos JMeter, temporales y posibles secretos) **no** deben versionarse por defecto. Guardar en Git solo resúmenes y evidencia deliberadamente seleccionada, o utilizar artefactos de GitHub Actions con retención limitada.

## 15. Plantilla estándar de caso de prueba

Usar identificadores `CP-<AREA>-<NNN>`; por ejemplo, `CP-MOV-001`, `CP-AJU-001`, `CP-FAR-001`.

| Campo | Contenido obligatorio |
|---|---|
| ID y título | Código único y comportamiento concreto |
| HU / Issue / requisito | Identificador y criterio de aceptación asociado |
| Módulo / proceso | Clase o componente y funcionalidad de negocio |
| Tipo y nivel | Unitaria, integración, funcional, seguridad, carga, etc. |
| Prioridad | P0, P1 o P2 |
| Precondiciones | Estado inicial, sesión, permisos y datos requeridos |
| Datos artificiales | Entradas concretas y límites usados |
| Pasos | Acción o secuencia reproducible |
| Resultado esperado | Aserciones y mensajes/restricciones esperados |
| Postcondiciones | Datos persistidos, stock, auditoría y limpieza |
| Resultado observado | Completar **después de ejecutar**, nunca inferir |
| Estado y evidencia | Aprobada/fallida/bloqueada/no ejecutada; log/reporte/commit |
| Ejecución | Fecha, ambiente, versión y responsable |

### Ejemplo — CP-MOV-001: salida mayor a las existencias

- **Proceso:** movimiento de aseo.
- **Precondición:** usuario autorizado en farmacia A y producto con 10 unidades.
- **Datos:** salida solicitada de 15 unidades.
- **Acción:** solicitar salida usando el servicio correspondiente.
- **Esperado:** rechazo controlado; no se genera stock negativo.
- **Postcondición de integración:** el stock sigue en 10 y no existe movimiento confirmado para esa salida.
- **Resultado observado:** pendiente de ejecución.
- **Nota:** en un test unitario verificar la regla; en un test de integración verificar además la ausencia de escrituras parciales.

## 16. Ruta de implantación por fases

| Fase | Entregable verificable | Condición de cierre |
|---|---|---|
| **F1. Línea base e infraestructura** | Inventario de tests, ejecución real, JaCoCo y primer reporte | Suite ejecutada; cobertura registrada; duplicados revisados |
| **F2. Unidades y TDD** | Mockito, casos de modelos/servicios, reglas de inventario | Casos positivos, negativos y límites con resultados reproducibles |
| **F3. Calidad continua** | SonarQube, revisión de hallazgos, Quality Gate en CI | Reporte por PR y políticas acordadas |
| **F4. Integración y seguridad** | PostgreSQL aislado, tests DAO, segregación y atomicidad | Flujos P0 probados con persistencia real |
| **F5. Rendimiento y estrés** | JMeter/harness, línea base y escenarios medidos | Reporte con métricas, entorno y análisis de consistencia |
| **F6. Regresión y releases** | Suite por versión y checklist de salida | Evidencias vinculadas a tags y decisiones de liberación |

Las fases pueden superponerse, pero **no** se debe fijar un umbral de rendimiento sin un entorno controlado ni bloquear PR por cobertura histórica antes de medirla.

### Issues sugeridas para comenzar

- **QA-01 — Establecer línea base:** inventariar tests estándar, ejecutar `clean verify`, registrar fallos y revisar ruta duplicada.
- **QA-02 — Incorporar JaCoCo:** generar HTML/XML y documentar cobertura de partida.
- **QA-03 — Incorporar Mockito:** crear tests aislados para reglas críticas de movimientos y ajustes.
- **QA-04 — Integrar SonarQube:** análisis de PR, revisión de hallazgos y propuesta de Quality Gate.
- **QA-05 — Integración PostgreSQL:** diseñar fixtures, rollback y segregación multifarmacia.
- **QA-06 — Diseñar pruebas JMeter:** escenarios, infraestructura de prueba y mediciones iniciales.

Los códigos QA anteriores son **identificadores propuestos**, no números de Issues ya creadas en GitHub.

## 17. Responsabilidades y mantenimiento

- **Desarrollador de la funcionalidad:** define escenarios, implementa pruebas unitarias y adjunta resultados del PR.
- **Revisor de PR:** comprueba trazabilidad, calidad de aserciones y ausencia de regresiones.
- **Responsable de pruebas/QA (rol del equipo):** mantiene matriz, prioridades, evidencias y resultados de integración/carga.
- **Equipo:** acuerda los umbrales, excepciones del Quality Gate y condiciones de release.

Actualizar este plan cuando cambie la arquitectura, aparezcan nuevas categorías de riesgo, se añadan herramientas o se acuerden umbrales medidos. Registrar cambios relevantes en el historial siguiente.

## 18. Historial de revisiones

| Versión | Fecha | Descripción |
|---|---|---|
| 1.0 | 2026-10-27 | Primera propuesta: estrategia modular, TDD, JaCoCo, Mockito, SonarQube, PostgreSQL aislado, JMeter, CI y estructura documental futura. |

---

**Principio de cierre:** una funcionalidad no se considera verificada solo porque compile o la interfaz muestre un mensaje correcto. Debe contar con casos reproducibles, resultados observables y evidencia apropiada a su riesgo.
