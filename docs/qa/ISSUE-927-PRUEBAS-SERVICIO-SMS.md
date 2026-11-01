# Issue #927 - Pruebas del servicio de preferencias SMS

**Proyecto:** CareStock
**Issue:** #927
**Fecha:** 28/10/2026
**Metodologia:** TDD
**Tecnologias:** Java 21, JUnit 5, Mockito y PostgreSQL

## Objetivo

Validar las reglas de negocio de administracion de
destinatarios, preferencias, autorizaciones y horarios SMS.

## Entorno

- Rama Git: feature/issue-927-servicio-preferencias-sms
- Base Neon: qa-issue-927
- Rama productiva: production (no utilizada)
- Proveedor SMS: simulado mediante Mockito

## Casos de prueba

| ID | Validacion | Resultado esperado |
|---|---|---|
| SV-01 | Administrador activo | Operacion permitida |
| SV-02 | Usuario sin permisos | Acceso rechazado |
| SV-03 | Farmacia diferente | Acceso rechazado |
| SV-04 | Telefono E.164 invalido | Validacion rechazada |
| SV-05 | Sin consentimiento | Envio bloqueado |
| SV-06 | Stock deshabilitado | No generar ni enviar SMS |
| SV-07 | Vencimiento deshabilitado | No generar ni enviar SMS |
| SV-08 | Fuera de horario no urgente | Envio aplazado |
| SV-09 | Alerta critica fuera de horario | Envio permitido si esta autorizada |
| SV-10 | Usuario o farmacia inactiva | Envio bloqueado |
| SV-11 | Consentimiento revocado | No llamar al proveedor |
| SV-12 | Actualizar y consultar | Datos actualizados |

## Ciclo TDD

### RED
Crear y ejecutar pruebas antes de implementar el servicio.

### GREEN
Implementar las reglas, el repositorio y las validaciones.

### REFACTOR
Revisar concurrencia, codigo repetido y seguridad.

### Integracion
Ejecutar pruebas sobre PostgreSQL de pruebas.

### Regresion
Ejecutar la suite completa del proyecto.

## Reglas de seguridad

- La farmacia se obtiene de la sesion autenticada.
- Se revalidan el rol y los estados en PostgreSQL.
- No se confia en un ID de farmacia enviado por la interfaz.
- No se permite enviar SMS sin consentimiento explicito.
- Las preferencias deshabilitadas bloquean envios.
- Las alertas criticas solo pueden omitir el horario.
- La autorizacion se revalida antes del proveedor SMS.
- Las operaciones no modifican el inventario.

## Evidencias

Pendientes de registrar durante RED, GREEN y regresion.

## Estado

- [x] RED
- [x] GREEN
- [x] Pruebas SV-01 a SV-12 (unitarias)
- [x] Integracion PostgreSQL
- [x] Regresion
- [x] Maven verify
- [x] Pull Request
