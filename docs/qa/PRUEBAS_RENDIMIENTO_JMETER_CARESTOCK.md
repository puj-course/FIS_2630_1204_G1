# Informe de pruebas de rendimiento JDBC con Apache JMeter — CareStock

> **Proyecto:** CareStock — Sistema de inventario para farmacias  
> **Fecha de ejecución:** 28 de octubre de 2026 (COT, UTC−05:00)  
> **Tipo:** pruebas exploratorias de rendimiento y carga, lectura de inventario  
> **Estado:** ejecutadas; 4 escenarios con 0 errores JDBC registrados  
> **Documento relacionado:** [`PLAN_MAESTRO_PRUEBAS_CARESTOCK.md`](../PLAN_MAESTRO_PRUEBAS_CARESTOCK.md)

## 1. Objetivo

Establecer una **línea base exploratoria** de latencia, rendimiento (*throughput*) y errores al ejecutar consultas concurrentes del catálogo unificado de inventario de CareStock, filtradas por farmacia. Investigar además la diferencia entre el tiempo de respuesta de la primera consulta y las posteriores.

La consulta reproduce la sentencia SQL del método `listarPorFarmacia(int idFarmacia)` de `src/CareStock/src/main/java/org/example/carestock/dao/ConsultaInventarioDAO.java`, que combina las categorías **Medicamento**, **Aseo** y **Maternidad** con `UNION ALL` y filtra cada tabla mediante `id_farmacia = ?`.

> **Alcance preciso:** JMeter ejecuta SQL directamente contra PostgreSQL mediante su propio pool JDBC. **No invoca** el controlador JavaFX, la fachada, `ConexionBD`/HikariCP del programa ni `ConsultaInventarioProxy`. Por tanto, estos resultados **no equivalen** al tiempo de respuesta extremo a extremo de CareStock ni certifican el comportamiento de la caché o de las reglas de negocio.

## 2. Entorno de prueba

| Elemento | Configuración observada |
| --- | --- |
| Sistema anfitrión | Windows; ejecución desde Git Bash |
| JMeter | Apache JMeter 5.6.3, modo no gráfico (`-n`) |
| Controlador JDBC de JMeter | PostgreSQL `postgresql-42.7.14.jar` |
| Contenedor | Docker Desktop / motor Linux WSL2; imagen `postgres:16` |
| Base de datos | PostgreSQL aislado `carestock_jmeter` |
| Conexión | `jdbc:postgresql://127.0.0.1:55432/carestock_jmeter` |
| Host Docker reportado | 8 CPU lógicas y 9,654 GiB de RAM asignada/disponible al motor |
| Datos de prueba previstos | 300 medicamentos + 300 productos de aseo + 300 de maternidad = 900 registros sintéticos para la farmacia `1` |
| Consulta | `Prepared Select Statement` con tres parámetros `INTEGER`, todos con el ID de farmacia |
| Ubicación local de trabajo | `C:\Users\mateo\Desktop\PruebasCareStock` |

**Control de seguridad:** se utilizó un contenedor local aislado; no se ejecutó carga contra la base Neon de producción. No incluir usuarios, contraseñas, archivos `.properties` con secretos ni cadenas de conexión de producción en Git.

**Nota sobre los datos:** las tablas de pruebas contienen un esquema simplificado que satisface la consulta SQL; **no** representan una restauración íntegra del esquema productivo. La cantidad exacta de registros deberá verificarse mediante `COUNT(*)` en la instancia utilizada.

## 3. Procedimiento y preparación

### 3.1. Verificar infraestructura

Desde Git Bash:

```bash
docker ps
docker exec carestock-jmeter-postgres \
  pg_isready -U carestock_test -d carestock_jmeter
```

La verificación observada devolvió `accepting connections` y mostró el contenedor `carestock-jmeter-postgres` activo con mapeo `127.0.0.1:55432 -> 5432`.

### 3.2. Verificar los datos

```bash
docker exec carestock-jmeter-postgres \
  psql -U carestock_test -d carestock_jmeter \
  -c "SELECT (SELECT COUNT(*) FROM medicamentos WHERE id_farmacia = 1) AS medicamentos, (SELECT COUNT(*) FROM aseo WHERE id_farmacia = 1) AS aseo, (SELECT COUNT(*) FROM maternidad WHERE id_farmacia = 1) AS maternidad;"
```

La carga sintética preparada contempla 300 registros por tabla para la farmacia `1`. Registrar la salida de este comando como evidencia de cantidad real de datos; no inferirla solamente de `responseCode=200`.

### 3.3. Configuración local de JMeter

El plan se creó localmente en `CareStock_JMeter_ConsultaInventario.jmx`, con:

- `JDBC Connection Configuration`: driver `org.postgresql.Driver`, pool `carestockPool` y conexión al PostgreSQL local.
- `JDBC Request`: consulta preparada con tres parámetros de farmacia; lectura únicamente.
- `Thread Group` parametrizado con `USERS`, `RAMP` y `LOOPS`.
- Informes: `Summary Report` y `Aggregate Report`; archivo `.jtl` y dashboard HTML para cada escenario.

Archivo local **no versionado** `carestock-local.properties` (ejemplo sin credenciales):

```properties
DB_URL=jdbc:postgresql://127.0.0.1:55432/carestock_jmeter
DB_USER=carestock_test
DB_PASSWORD=REEMPLAZAR_CON_CLAVE_LOCAL
FARMACIA_ID=1
```

### 3.4. Comando reproducible

Desde Git Bash, con las rutas de la prueba local:

```bash
export JMETER=/c/Users/mateo/Desktop/Herramientas/apache-jmeter-5.6.3
export WORK=/c/Users/mateo/Desktop/PruebasCareStock

ejecutar_prueba() {
  local nombre="$1" usuarios="$2" ramp="$3" loops="$4"

  cmd.exe //c "$(cygpath -w "$JMETER/bin/jmeter.bat")" \
    -n \
    -t "$(cygpath -w "$WORK/CareStock_JMeter_ConsultaInventario.jmx")" \
    -q "$(cygpath -w "$WORK/carestock-local.properties")" \
    -JUSERS="$usuarios" \
    -JRAMP="$ramp" \
    -JLOOPS="$loops" \
    -l "$(cygpath -w "$WORK/resultados_$nombre.jtl")" \
    -e -o "$(cygpath -w "$WORK/reporte_$nombre")"
}
```

**Precaución:** el directorio de reporte de cada ejecución debe ser nuevo o estar vacío; usar nombres de escenario distintos para no sobrescribir evidencia. Las variables de entorno de Git Bash se vuelven a definir al abrir una nueva sesión.

## 4. Escenarios ejecutados y resultados

**Fuente:** salidas de consola de JMeter aportadas durante la ejecución del 28/10/2026. La tabla indica resultados observados, no valores simulados.

| ID | Escenario | Hilos | Ramp-up | Iteraciones/hilo | Muestras | Promedio | Mínimo | Máximo | Throughput | Errores |
| --- | --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| JM-01 | Primera ejecución | 1 | 1 s | 1 | 1 | 945 ms | 945 ms | 945 ms | 0,9/s | 0 (0,00 %) |
| JM-02 | Carga de 5 usuarios | 5 | 5 s | 20 | 100 | 44 ms | 11 ms | 1139 ms | 21,6/s | 0 (0,00 %) |
| JM-03 | Carga de 10 usuarios | 10 | 10 s | 20 | 200 | 35 ms | 9 ms | 1168 ms | 20,7/s | 0 (0,00 %) |
| JM-04 | Un usuario, 100 consultas | 1 | 1 s | 100 | 100 | 26 ms | 9 ms | 737 ms | 35,9/s | 0 (0,00 %) |

**Total:** **401 consultas/samples**, **0 errores JDBC registrados**. El promedio de cada escenario es el que JMeter redondeó y mostró en consola; los porcentajes y latencias no sustituyen una verificación de completitud del resultado de SQL.

Comandos usados para tres escenarios con la función anterior:

```bash
ejecutar_prueba 5usuarios 5 5 20
ejecutar_prueba 10usuarios 10 10 20
ejecutar_prueba 1usuario_100 1 1 100
```

JM-01 se ejecutó inicialmente con `1` hilo y `1` iteración antes de habilitar la función reutilizable.

**Interpretación de concurrencia:** el `ramp-up` incorpora hilos de manera gradual; diez hilos configurados **no implican** diez consultas ejecutándose al mismo tiempo durante toda la prueba. Estas ejecuciones breves no son ensayos de carga sostenida ni de estrés.

## 5. Análisis del tiempo de arranque (*cold start*)

La ejecución JM-04 registró, en el archivo `resultados_1usuario_100.jtl`, la siguiente primera muestra:

```csv
timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect
1793215184690,737,ConsultaInventarioDAO - por farmacia,200,OK,Usuarios virtuales 1-1,text,true,,58786,0,1,1,null,706,0,660
```

Se observó:

- **Primera consulta:** 737 ms; fue la **más lenta**, en la posición 1 de 100.
- **Connect reportado por JMeter:** 660 ms en esa primera consulta, aproximadamente **89,6 %** de su duración total.
- **Promedio de las otras 99 consultas:** **19,22 ms**, calculado a partir del `.jtl`.
- **Muestras posteriores:** `Connect` generalmente `0` ms; la segunda muestra duró 125 ms y las siguientes fueron reduciendo su latencia.
- **Salida del sampler:** `200 OK`, `success=true`, `bytes=58786` para las primeras muestras mostradas.

**Hallazgo:** existe una marcada penalización en la primera muestra, compatible con el establecimiento inicial de la conexión JDBC y otros costos de arranque. Los **660 ms de `Connect`** explican gran parte de los **737 ms** registrados para esa muestra. El costo de establecimiento es un **dato medido por JMeter**, no una prueba de que PostgreSQL haya ejecutado el SQL durante 660 ms.

**No atribuir** este resultado a `HikariCP` de CareStock: el archivo `.jmx` utiliza un pool gestionado por JMeter, sin pasar por `ConexionBD`. Tampoco demuestra que el Proxy de caché de la aplicación haya intervenido. El primer valor de 945 ms de JM-01 no es representativo, por sí solo, de la latencia habitual de una consulta con conexión reutilizada.

### Método de verificación

```bash
awk -F, '
NR == 1 {
  for (i = 1; i <= NF; i++) if ($i == "elapsed") col = i
  next
}
col {
  n++
  if (n == 1) primera = $col
  if (n == 1 || $col > max) { max = $col; posicion = n }
  if (n > 1) { suma += $col; cantidad++ }
}
END {
  if (n > 0) {
    print "Primera consulta:", primera, "ms"
    print "Consulta mas lenta:", max, "ms"
    print "Posicion de la mas lenta:", posicion
    if (cantidad > 0)
      printf "Promedio excluyendo primera: %.2f ms\n", suma/cantidad
  }
}' "$WORK/resultados_1usuario_100.jtl"
```

Salida registrada:

```text
Primera consulta: 737 ms
Consulta más lenta: 737 ms
Posición de la más lenta: 1
Promedio excluyendo primera: 19.22 ms
```

## 6. Conclusiones

1. **Ejecución:** JMeter pudo ejecutar la consulta preparada de inventario por farmacia contra PostgreSQL local; las 401 muestras finalizaron sin errores JDBC.
2. **Latencia:** la carga repetitiva con un hilo mostró 26 ms de promedio global; al excluir la primera muestra, las otras 99 promediaron 19,22 ms.
3. **Costo inicial:** la primera muestra de JM-04 tardó 737 ms; JMeter asignó 660 ms al campo `Connect`. Es una evidencia fuerte de sobrecosto inicial de conexión en **este entorno**, no un diagnóstico exhaustivo de todas las capas.
4. **Carga:** con 5 y 10 hilos se observaron 44 y 35 ms de promedio y tasas de 21,6/s y 20,7/s; **no se puede afirmar** escalabilidad superior con 10 hilos a partir de escenarios tan cortos y con rampas distintas.
5. **Alcance:** los resultados respaldan una línea base **JDBC/PostgreSQL**. No validan aún UI JavaFX, autenticación, permisos, segregación por sesión, integridad de transacciones, HikariCP del sistema o caché Proxy.

**Estado de aceptación:** **prueba técnica ejecutada satisfactoriamente** (sin errores de JMeter/JDBC observados). **Cumplimiento de un SLA de rendimiento: pendiente**, porque todavía no se han definido umbrales p95/p99, duración y carga objetivo.

## 7. Riesgos y limitaciones

- Ensayos de pocos segundos, con tamaños de muestra y rampas distintos.
- No se recolectaron en el resumen de consola los percentiles p95/p99, CPU, memoria o métricas del servidor PostgreSQL.
- Los valores `200 OK` no validan por sí solos el número exacto de filas ni el aislamiento real entre farmacias; se necesita una aserción o una verificación SQL específica.
- El plan JDBC no incluye el tiempo de renderizado JavaFX ni servicios del dominio.
- No se midió la conducta de múltiples usuarios reales de CareStock ni de su pool HikariCP.
- Los datos de prueba son sintéticos y su distribución puede diferir de la carga real.

## 8. Próximas pruebas recomendadas

| Prioridad | Prueba | Criterio a registrar |
| --- | --- | --- |
| Alta | Repetición de línea base con calentamiento separado | Promedio, mediana, p95, p99 sin mezclar inicialización |
| Alta | Verificación de resultados por farmacia 1 y farmacia 2 | Filas correctas; ausencia de registros cruzados |
| Alta | Carga sostenida (p. ej., 5–10 min, según recursos) | Throughput, p95/p99, errores y CPU/RAM |
| Alta | Servicio transaccional de entradas/salidas | Stock nunca negativo, atomicidad y auditoría |
| Media | Integración con HikariCP real de CareStock | Tiempo de préstamo de conexión, saturación y esperas |
| Media | Proxy de caché de inventario | Hit/miss, TTL, invalidación, diferencia de latencias |
| Media | Pruebas UI JavaFX | Tiempo de respuesta percibido y manejo de errores |

Estos escenarios deben ejecutarse en un entorno **aislado de producción** y guardar evidencia por ejecución.

## 9. Evidencias y trazabilidad

Rutas locales de trabajo; **no son archivos presentes automáticamente en Git**:

```text
C:\Users\mateo\Desktop\PruebasCareStock\
  CareStock_JMeter_ConsultaInventario.jmx
  carestock-local.properties       (LOCAL: contiene credenciales; NO subir)
  primera_prueba.jtl
  resultados_5usuarios.jtl
  resultados_10usuarios.jtl
  resultados_1usuario_100.jtl
  reporte_5usuarios\index.html
  reporte_10usuarios\index.html
  reporte_1usuario_100\index.html
```

Es recomendable versionar el `.jmx` **sin secretos** en `tests/performance/` para reproducir el procedimiento. Los `.jtl`, los dashboards y el archivo de propiedades con credenciales deben mantenerse fuera del repositorio, o publicarse como artefactos de CI revisados y saneados cuando corresponda.

**Trazabilidad del plan maestro:** módulo `dao`, proceso `RND`, funcionalidad de catálogo unificado (`ConsultaInventarioDAO`). El informe contiene resultados manuales locales del **28/10/2026**; no se presenta como prueba automatizada en CI.
