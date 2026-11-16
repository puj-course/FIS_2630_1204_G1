# Retrospectiva Starfish y plan de mejora - Sprints 12, 13 y 14

**Proyecto:** CareStock
**Curso:** Fundamentos de Ingeniería de Software
**Institución:** Pontificia Universidad Javeriana


## 1. Estrategia consolidada de los sprints 12, 13 y 14
<img width="1068" height="607" alt="image" src="https://github.com/user-attachments/assets/95ca1f0b-ca19-48c8-9c0f-e7baa5477a64" />

### Justificación de cada frase

### START - ¿Qué deberíamos empezar a hacer?

1. **Referenciar la HU y el issue en cada commit con un formato único.** 140 de 233 commits citan una HU o un issue y conviven cuatro formatos: HU-144, H.U 144, HU.150 #1039 y feat(HU-134).
2. **Ejecutar las pruebas y la cobertura en local antes de subir los commits.** En el Sprint 13 hubo correcciones de pruebas después de integrar y en el Sprint 14 la HU.158 #1142 tuvo un revert y una prueba eliminada por conflictos de cobertura.
3. **Fijar el trabajo de calidad de SonarQube desde el inicio del sprint.** La cobertura del código nuevo pasó de 24,1 % (31/10) a 77,63 % (01/11) en pocos días y las fases de mantenibilidad se hicieron el 15 y 16/11.

### MORE - ¿Qué deberíamos hacer más?

1. **Repartir las tareas de código entre los cinco integrantes.** 
2. **Escribir las pruebas junto con cada HU.** HU-142, HU-143, HU-144, HU-151, HU-152 y HU-153 entregaron su clase con pruebas unitarias; hay 24 commits de test en los tres sprints.
3. **Documentar cada HU contra el código y con ejemplos ejecutados.** 30 commits docs; los documentos de las HU-145, HU-146, HU-147 y HU-107 se actualizaron según el código y las HU-155 a HU-157 se reprodujeron en la base.

### CONTINUE - ¿Qué deberíamos seguir haciendo?

1. **Integrar todo el trabajo mediante pull requests.** 123 pull requests integrados en los tres sprints.
2. **Dividir cada HU en subtareas con su propio issue.** HU.121 a HU.123, HU.148 a HU.151 y HU.158 se entregaron con un commit por subtarea numerada.
3. **Automatizar la calidad, las notificaciones y el despliegue en los workflows.** Telegram, cobertura antes del PR, pruebas SQL, métricas estáticas, build y validación de la imagen Docker, y los tags v1.0.1 y v1.0.2.

### LESS - ¿Qué deberíamos hacer menos?

1. **Reducir los commits repetidos y de mensaje genérico.** 28 commits repiten un mensaje y 33 tienen mensajes como Update X.java; el de HU.158 #1142 aparece cinco veces.
2. **Reducir las correcciones posteriores a la entrega.** 24 commits fix: cadena de frío, lotes vencidos y catálogo después del Sprint 13, y tres fix(docker) en el Sprint 14.
3. **Reducir el trabajo concentrado al cierre del sprint.** El 31/10 hubo tres refactor y más de diez commits de cobertura, el 06 y 08/11 más trabajo de cobertura y el 15/11 se registró el 27 % de los commits del Sprint 14.

### STOP - ¿Qué deberíamos dejar de hacer?

1. **Implementar reglas de negocio sin aplicarlas en todos los caminos de la interfaz.** La validación de cadena de frío está en RegistroLoteService, pero ServicioCrudLotes.crear, que usa el diálogo de gestión de lotes, no la aplica.
2. **Duplicar criterios de negocio en varias capas.** Hay dos criterios de reposición: 2 por mínimo minimo de existencias (SugerenciaReposicion, HU-143) y la cantidad faltante de la consulta de reposición (HU.137).
3. **Subir archivos temporales y editar directamente en main.** Commit del 26/10 editando en main, archivos subidos por la web y borrados después, y parches temporales que hubo que ignorar el 09/11.

### Plan de mejora para el último sprint del curso

| N. | Frase de la estrella que atiende | Acción concreta en el último sprint | Cómo se mide |
|---|---|---|---|
| 1 | Referenciar la HU y el issue en cada commit con un formato único | Usar tipo(HU-N): mensaje (#issue) en los commits y Closes #N en cada PR | Al menos 85 % de commits con referencia (hoy 60 %) |
| 2 | Ejecutar las pruebas y la cobertura en local antes de subir los commits | Correr mvn test con JaCoCo antes de cada push y no subir pruebas que luego se eliminen | Cero revert y menos de 5 commits fix de pruebas o cobertura en el sprint |
| 3 | Fijar el trabajo de calidad de SonarQube desde el inicio del sprint | Revisar el Quality Gate a mitad del sprint y fijar un corte de cambios dos días antes del cierre | Cobertura global mayor a 80 % con captura de SonarQube y no más del 20 % de commits en el último día |
| 4 | Repartir las tareas de código entre los cinco integrantes | Asignar en la planeación al menos una tarea de código o pruebas a cada integrante | 5 de 5 integrantes con commits y ninguno por encima del 40 % |
| 5 | Escribir las pruebas junto con cada HU | Una clase de pruebas por cada clase nueva en el mismo PR | 100 % de las HU nuevas con pruebas |
| 6 | Documentar cada HU contra el código y con ejemplos ejecutados | Revisar los documentos afectados dentro del mismo PR que cambia el código | Cero documentos corregidos en commits posteriores |
| 7 | Implementar reglas de negocio sin aplicarlas en todos los caminos de la interfaz | Aplicar la validación de cadena de frío también en ServicioCrudLotes.crear con una prueba que falle si se quita | Una regla aplicada en todos los caminos de registro de lotes y su prueba |
| 8 | Duplicar criterios de negocio en varias capas | Unificar los criterios de reposición y de stock en un solo servicio con pruebas | Un solo criterio documentado y probado |
| 9 | Automatizar la calidad, las notificaciones y el despliegue en los workflows | Crear 2 workflows que muevan o validen el tablero kanban | 2 workflows exitosos y justificados en el reporte de DevOps |
| 10 | Dividir cada HU en subtareas con su propio issue | Crear un issue por subtarea en la planeación del último sprint y cerrar el sprint con un tag de versión que enlace las HU incluidas | Todas las subtareas con issue y un tag nuevo con trazabilidad a las HU |

### Discusión del impacto en el último sprint

Las acciones 1 a 6 dependen del proceso del equipo y no del código, por lo que se aplican desde el primer día del último sprint. Las acciones 7 y 8 corrigen los defectos de implementación que más cambios posteriores generaron (24 commits fix en tres sprints). Las acciones 9 y 10 atienden los criterios de DevOps y de control de versiones de la rúbrica. Al cierre del sprint se medirá cada indicador y se comparará con los datos de este reporte.

---

## 2. Estrella de cada sprint

### Sprint 12 (26/10/2026 al 01/11/2026)

<img width="1044" height="612" alt="image" src="https://github.com/user-attachments/assets/40b93ba0-58da-4012-ae3b-41830098bc4e" />

#### Sprint 12: justificación de cada frase

**START - ¿Qué deberíamos empezar a hacer?**

1. **Referenciar el issue en cada commit.** Solo 50 de 102 commits (49 %) citan una HU o un issue.
2. **Integrar JaCoCo y SonarQube desde el inicio del sprint.** JaCoCo y el workflow de SonarQube se agregaron el 29/10 y la subida de cobertura se concentró entre el 29/10 y el 01/11.
3. **Asignar un responsable único a cada workflow de CI.** El 29/10 cinco commits editaron SonarQube.yml y luego hubo que restaurar el CD y evitar análisis duplicados en SonarCloud (fix(ci)).

**MORE - ¿Qué deberíamos hacer más?**

1. **Documentar cada HU de alertas con su modelo, reglas y flujo.** Se entregaron documentos de las HU-125, HU-126, HU-127, HU-129 y HU-886 (12 commits docs).
2. **Ampliar las pruebas del código crítico.** 16 commits de test: 35 escenarios de inventario y movimientos, pruebas de JDBC, SMS, seguridad y caché.

**CONTINUE - ¿Qué deberíamos seguir haciendo?**

1. **Dividir cada HU en subtareas con su propio issue.** HU.121 (#869 a #871), HU.122 (#872 a #874) y HU.123 (#875 a #877) se entregaron con un commit por subtarea.
2. **Integrar todo el trabajo mediante pull requests.** 56 pull requests integrados en la semana.
3. **Mantener la integración de SMS con varios proveedores.** Twilio, simulador y gateway Android (#891, #893, HU-130) y las preferencias por farmacia (#926 a #928).

**LESS - ¿Qué deberíamos hacer menos?**

1. **Reducir los commits repetidos y de mensaje genérico.** 14 commits repiten un mensaje y 14 tienen mensajes como Update Inventario.java.
2. **Reducir las reversiones.** Dos commits Revert: uno de la HU.123 #876 y otro de las pruebas de controladores y servicios críticos.
3. **Reducir los refactors grandes cerca del cierre del sprint.** El 31/10 se hicieron tres refactor (duplicación, modelo y arquitectura) junto con más de diez commits de cobertura el mismo día.

**STOP - ¿Qué deberíamos dejar de hacer?**

1. **Editar directamente en main.** Commit del 26/10: me equivoque y edite en el main.
2. **Subir archivos por la web y borrarlos después.** Add files via upload (dos veces) y Delete docs/PRUEBAS_COBERTURA_JDBC_2026-10-30.md.
3. **Confundir el número de issue con el de la HU en los mensajes.** docs(HU-886) usa el número de un issue como si fuera una HU.

---

### Sprint 13 (02/11/2026 al 08/11/2026)

<img width="1032" height="618" alt="image" src="https://github.com/user-attachments/assets/eec3247b-f94a-4ab0-a4f9-1c339144622b" />

#### Sprint 13: justificación de cada frase

**START - ¿Qué deberíamos empezar a hacer?**

1. **Validar la cadena de frío de punta a punta antes de integrar.** La HU.150 (#1037 a #1039) se entregó el 08/11 y el 09/11 hubo que corregir la validación de cadena de frío (commit cc6cda9).
2. **Escribir los mensajes de commit con un solo formato de HU.** En el mismo sprint conviven HU-144, H.U 144, HU.150 #1039 y HU-145 ... (#1014).
3. **Definir una sola regla de reposición sugerida.** La columna Reponer de la HU-143 usa 2 × mínimo - existencias y la consulta de reposición de la HU.137 usa la cantidad faltante respecto al mínimo.

**MORE - ¿Qué deberíamos hacer más?**

1. **Incluir las pruebas unitarias en el mismo commit de cada HU.** Las HU-142, HU-143 y HU-144 entregaron ExportadorAlertasCsv, SugerenciaReposicion y ValidadorDispensacion con sus pruebas.
2. **Proponer mejoras de diseño a partir de lo que se documenta.** Los documentos HU-146 y HU-147 propusieron reponer desde la alerta y elegir el lote con FEFO; la HU-152 implementó después el botón Sugerir lote.
3. **Distribuir las tareas para que los cinco integrantes registren commits.** Un integrante no tuvo commits en el sprint (4 de 5 activos).

**CONTINUE - ¿Qué deberíamos seguir haciendo?**

1. **Mantener la trazabilidad de los commits hacia su issue.** Subió de 49 % en el Sprint 12 a 78 % (51 de 65).
2. **Acompañar cada funcionalidad de alertas con su guía de usuario y captura.** Las HU-142, HU-143 y HU-144 incluyeron un commit de documentación con captura.
3. **Automatizar la calidad y las notificaciones en CI.** El 05/11 se agregaron Telegram, la cobertura del código nuevo antes del PR, las pruebas SQL y las tres métricas estáticas.

**LESS - ¿Qué deberíamos hacer menos?**

1. **Reducir los commits que repiten el mismo mensaje.** 8 commits repiten mensaje; el de HU.150 #1038 aparece cuatro veces y el de #1039 tres.
2. **Reducir el trabajo de cobertura al final del sprint.** test(ui): ampliar cobertura al 80 por ciento (06/11) y test: superar 80% de cobertura global (08/11).
3. **Reducir los cambios grandes de interfaz mezclados con lógica de negocio.** El 06/11 se integraron el catálogo al dashboard, la consulta de reposición HU.137 y pruebas de cobertura el mismo día.

**STOP - ¿Qué deberíamos dejar de hacer?**

1. **Editar archivos por la web con mensajes automáticos.** 12 commits genéricos, por ejemplo Update Medicamento.java y Rename Stock total (solo lectura) to catalogo-view.fxml.
2. **Concentrar la entrega de cada integrante en pocos días.** Un integrante registró 22 commits en dos días (04 y 08/11) y otro 19 en un solo día (02/11).
3. **Entregar las HU.148, HU.149 y HU.150 sin documentación.** No hay commits docs para estas tres HU en el sprint.

---

### Sprint 14 (09/11/2026 al 15/11/2026)

<img width="1083" height="615" alt="image" src="https://github.com/user-attachments/assets/900a2173-15c3-4e65-8a11-1575a3c1a530" />

#### Sprint 14: justificación de cada frase

**START - ¿Qué deberíamos empezar a hacer?**

1. **Ejecutar las pruebas y la cobertura en local antes de subir los commits de una HU.** La HU.158 #1142 tuvo un revert, un fix para eliminar una prueba que generaba conflictos de cobertura y el mismo commit de redireccionamiento repetido cinco veces.
2. **Revisar los hallazgos de SonarQube cada semana.** Las fases 1 a 3 de refactor(sonar) y los test(sonar) de cobertura del código nuevo se hicieron entre el 15 y el 16/11.
3. **Documentar el panel de tareas pendientes de la HU.158 con guía y captura.** No hay commits docs de la HU.158 (#1140 a #1142).

**MORE - ¿Qué deberíamos hacer más?**

1. **Documentar cada HU de inventario con ejemplos ejecutados en la base.** Siete commits docs de las HU-155, HU-156 y HU-157 (FEFO, dispensación, existencias, movimientos y criterios de atención).
2. **Actualizar los diagramas UML y C4 con cada cambio de arquitectura.** El 11/11 se actualizaron el diagrama UML, el diagrama de despliegue, el C4 de componentes y el README.
3. **Cubrir con pruebas todos los caminos de cada manejador de eventos.** Las pruebas de onSugerirLote cubren los cinco caminos y se agregaron pruebas del filtro de disponibilidad y de la vista previa del stock.

**CONTINUE - ¿Qué deberíamos seguir haciendo?**

1. **Contenerizar el sistema y validar su despliegue con Docker Compose y Docker Hub.** feat(docker) y ci(docker) del 10 al 13/11: build de la imagen, validación de la imagen publicada y registro de la verificación.
2. **Publicar versiones etiquetadas del sistema.** Los tags v1.0.1 (10/11) y v1.0.2 (13/11) quedaron sobre commits de main.
3. **Corregir los hallazgos de seguridad del análisis estático.** fix(security): asegurar ejecución de curl y corregir convenciones SMS (11/11).

**LESS - ¿Qué deberíamos hacer menos?**

1. **Reducir las correcciones sucesivas del pipeline Docker.** Tres commits fix(docker) entre el 11 y el 13/11 (DNS, permisos de red y automatización del build).
2. **Reducir la concentración de commits en el último día del sprint.** 18 de los 66 commits (27 %) son del 15/11.
3. **Reducir las correcciones sobre lo entregado la semana anterior.** El 09/11 tres fix corrigieron lotes vencidos, la cadena de frío y el catálogo del Sprint 13.

**STOP - ¿Qué deberíamos dejar de hacer?**

1. **Reimplementar el mismo cambio varias veces sin diagnosticar la causa.** La HU.158 #1142 tiene un revert seguido de cinco commits con el mismo mensaje de redireccionamiento.
2. **Agregar pruebas que se eliminan después por conflictos de cobertura.** fix(HU.158 #1142): eliminar prueba para evitar conflictos de cobertura.
3. **Dejar scripts y parches temporales sin ignorar en el repositorio.** El 09/11 hubo que borrar coverage_catalogo_prepr.txt y corregir_scroll_catalogo.py y agregar chore: limpiar e ignorar parches temporales.

---
