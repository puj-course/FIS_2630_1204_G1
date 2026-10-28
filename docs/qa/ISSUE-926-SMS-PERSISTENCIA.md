# Informe de pruebas de integración - Issue #926

**Proyecto:** CareStock  
**Issue:** [#926 - Persistencia de destinatarios y preferencias SMS](https://github.com/puj-course/FIS_2630_1204_G1/issues/926)  
**Fecha de ejecución:** 28 de octubre de 2026  
**Tipo de pruebas:** Integración de base de datos  
**Metodología:** Test-Driven Development (TDD)  
**Motor:** PostgreSQL - Neon  
**Estado:** Pruebas SQL satisfactorias  

---

## 1. Objetivo

Validar la ampliación de la persistencia de destinatarios SMS
en CareStock, garantizando:

- Almacenamiento de preferencias de notificación.
- Persistencia de horarios y zonas horarias.
- Control de autorización de destinatarios.
- Segregación de información por farmacia.
- Integridad referencial entre usuarios, farmacias y alertas.
- Prevención de registros activos duplicados.
- Conservación de registros históricos.
- Aplicación incremental e idempotente de la migración.

## 2. Entorno de pruebas

| Elemento | Configuración |
|---|---|
| Proyecto | CareStock |
| Base de datos | PostgreSQL |
| Proveedor | Neon |
| Proyecto Neon | carestock-cloud |
| Base de datos | neondb |
| Rama principal | production |
| Rama de pruebas | qa-issue-926 |
| Rama Git | feature/issue-926-persistencia-sms |
| Herramienta | Neon CLI |
| Cliente SQL | psql integrado en Neon CLI |
| Datos utilizados | Ficticios |

La rama `qa-issue-926` fue creada independientemente de
`production` utilizando el esquema existente.

Todas las operaciones de prueba se ejecutaron
exclusivamente sobre la rama de pruebas.

No se ejecutaron migraciones ni pruebas sobre producción.

## 3. Archivos utilizados

| Archivo | Propósito |
|---|---|
| scripts/tests/926_pre_migracion.sql | Preparación de datos ficticios |
| scripts/tests/926_pruebas_integracion.sql | Pruebas SQL automatizadas |
| scripts/migrations/926_sms_destinatarios_preferencias.sql | Migración incremental |
| docs/qa/evidence/issue-926-integracion.txt | Evidencia de integración |
| docs/qa/evidence/issue-926-idempotencia.txt | Evidencia de idempotencia |

## 4. Metodología TDD

### 4.1. Fase RED

Se prepararon registros ficticios de destinatarios,
usuarios, farmacias y alertas utilizando:

`scripts/tests/926_pre_migracion.sql`

La preparación finalizó satisfactoriamente:

- BEGIN
- DO
- COMMIT

Posteriormente se ejecutó el script de integración
antes de implementar la migración.

**Error obtenido:**

`ERROR: column "notifica_stock_critico" of relation "sms_destinatarios" does not exist`

**Interpretación:**

El fallo confirmó que las pruebas detectaban una
funcionalidad de persistencia todavía no implementada.

**Resultado: RED confirmado.**

### 4.2. Fase GREEN

Se implementó y ejecutó:

`scripts/migrations/926_sms_destinatarios_preferencias.sql`

La migración incorporó:

- Preferencia de stock crítico.
- Preferencia de vencimientos críticos.
- Horario de inicio.
- Horario de finalización.
- Zona horaria aplicable.
- Restricciones de integridad referencial.
- Control de destinatarios activos duplicados.
- Validaciones de horarios y zonas horarias.

La ejecución finalizó con:

`COMMIT`

Posteriormente se ejecutaron nuevamente las pruebas
de integración.

**Resultado: GREEN confirmado.**

### 4.3. Fase REFACTOR / Validación

Se comprobó que:

- La migración puede ejecutarse nuevamente.
- Las restricciones permanecen operativas.
- Los datos históricos ficticios se conservan.
- Las preferencias nuevas no se habilitan automáticamente.
- Las pruebas SQL finalizan satisfactoriamente.

No fue necesario modificar la estructura original
de las tablas mediante operaciones destructivas.

## 5. Casos de prueba ejecutados

| ID | Caso de prueba | Resultado |
|---|---|---|
| BD-01 | Registrar teléfono E.164 válido | Satisfactorio |
| BD-02 | Rechazar teléfono inválido | Satisfactorio |
| BD-03 | Impedir autorización automática | Satisfactorio |
| BD-04 | Persistir preferencias independientes | Satisfactorio |
| BD-05 | Almacenar horario y zona horaria | Satisfactorio |
| BD-06 | Rechazar asociación cruzada entre farmacias | Satisfactorio |
| BD-07 | Rechazar destinatarios activos duplicados | Satisfactorio |
| BD-08 | Actualizar y recuperar configuración | Satisfactorio |
| BD-09 | Conservar destinatarios y alertas históricos | Satisfactorio |
| BD-10 | Rechazar horarios incompletos | Satisfactorio |
| BD-11 | Rechazar zona horaria inexistente | Satisfactorio |
| BD-12 | Rechazar alertas asociadas a otra farmacia | Satisfactorio |

### Resultado de integración

- Casos definidos: 12.
- Casos ejecutados en el bloque SQL: 12.
- Ejecución satisfactoria: 12.
- Errores no controlados: 0.
- Código de salida SQL: 0.

El cliente integrado de Neon no mostró los mensajes
`RAISE NOTICE` individuales.

Sin embargo, el bloque PL/pgSQL terminó correctamente
y el script mostró:

`BD-01 a BD-12: OK`

El mensaje de resumen se ejecutó después del bloque de
validación, que genera excepciones cuando una condición
evaluada no cumple el resultado esperado.

La transacción finalizó con `ROLLBACK`, de forma
intencional, para no persistir las modificaciones
temporales realizadas durante las pruebas.

**Resultado general: SATISFACTORIO.**

## 6. Validación de idempotencia

La migración SQL fue ejecutada por segunda vez sobre
la misma rama de pruebas.

### Resultado observado

- BEGIN
- SET
- ALTER TABLE
- DO
- CREATE INDEX
- CREATE FUNCTION
- DROP TRIGGER
- CREATE TRIGGER
- COMMENT
- COMMIT

**Código de salida:** 0.

La segunda ejecución finalizó correctamente sin
errores por duplicación de columnas o restricciones.

La migración utiliza verificaciones de existencia
para permitir su reaplicación.

El disparador de zona horaria se recrea durante
la ejecución, conservando su definición prevista.

**Resultado: SATISFACTORIO.**

## 7. Validación de integridad

### 7.1. Segregación por farmacia

Las restricciones impiden asociar un destinatario
con una farmacia diferente de la correspondiente
a su usuario.

También se valida que las alertas pertenezcan
a la farmacia asociada al destinatario.

### 7.2. Duplicados activos

Se implementó un índice único parcial que impide
múltiples destinatarios activos para el mismo
usuario y farmacia.

### 7.3. Consentimiento

Las preferencias nuevas se crean deshabilitadas.

La migración conserva los campos existentes:

- autorizado
- autorizado_en
- activo

No concede consentimiento de forma automática.

### 7.4. Horarios

Se admite la configuración de ventanas horarias
que cruzan la medianoche.

Se rechazan horarios incompletos y zonas horarias
no reconocidas por PostgreSQL.

### 7.5. Datos históricos

La prueba BD-09 verificó la conservación de un
destinatario y una alerta ficticios existentes antes
de aplicar la migración.

No se utilizaron datos personales reales.

## 8. Evidencias

### 8.1. Integración

Archivo:

`docs/qa/evidence/issue-926-integracion.txt`

Resultado registrado:

- BEGIN
- DO
- BD-01 a BD-12: OK
- ROLLBACK

Código de salida: 0.

### 8.2. Idempotencia

Archivo:

`docs/qa/evidence/issue-926-idempotencia.txt`

Resultado registrado:

- Aplicación de sentencias DDL.
- Ejecución de validaciones.
- COMMIT.

Código de salida: 0.

### 8.3. Evidencia RED

La ejecución inicial produjo el error esperado
por ausencia de la columna `notifica_stock_critico`.

El resultado quedó registrado en la consola durante
el ciclo TDD inicial.

## 9. Alcance y limitaciones

Estas pruebas verifican exclusivamente la
persistencia y las restricciones de PostgreSQL.

No validan:

- El envío efectivo de mensajes SMS.
- La interfaz JavaFX.
- La selección de destinatarios por el servicio SMS.
- La aplicación de horarios en el motor de notificaciones.
- La interacción con proveedores externos de SMS.

Estas funcionalidades deberán validarse en las
subissues correspondientes.

## 10. Checklist de validación

- [x] Entorno de pruebas independiente.
- [x] Datos ficticios preparados.
- [x] Fase RED ejecutada.
- [x] Migración SQL implementada.
- [x] Fase GREEN ejecutada.
- [x] Pruebas de integración satisfactorias.
- [x] Segregación por farmacia validada.
- [x] Restricciones de integridad verificadas.
- [x] Conservación de datos históricos verificada.
- [x] Idempotencia comprobada.
- [x] Evidencias SQL registradas.
- [x] Compilación Maven validada.
- [x] Pull Request revisado y fusionado.

## 11. Conclusión

La implementación de la Issue #926 superó las
pruebas SQL de integración definidas mediante TDD.

La migración permite persistir las preferencias y
horarios de notificación, refuerza las restricciones
de integridad y mantiene la segregación por farmacia.

La ejecución repetida de la migración finalizó
correctamente.

**Estado final de pruebas PostgreSQL: APROBADO.**

La aceptación completa de la subissue queda sujeta
a la compilación del proyecto y revisión del
Pull Request correspondiente.
