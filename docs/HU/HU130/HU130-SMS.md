# HU.130 - Notificaciones preventivas por SMS

Issue principal: #885.

## Arquitectura

ProcesoSms:
- Ejecuta ciclos independientes de JavaFX.
- Consulta condiciones criticas en PostgreSQL/Neon.
- Genera alertas por destinatario y farmacia.
- Utiliza ServicioSms de la issue #891.

## Condiciones

- Stock critico: existencias iguales a 0.
- Vencimiento: lotes con unidades y 15 dias o menos.
- Se incluyen lotes ya vencidos con unidades registradas.

## Personalizacion

Variables:
- SMS_MARCA
- SMS_STOCK_TEMPLATE
- SMS_VENCE_TEMPLATE
- SMS_ACCION_STOCK
- SMS_ACCION_VENCE

Marcadores:
- {marca}
- {farmacia}
- {categoria}
- {producto}
- {lote}
- {unidades}
- {dias}
- {plazo}
- {accion}

## Seguridad

- Solo destinatarios registrados y autorizados.
- El destinatario debe pertenecer a la misma farmacia.
- No incluir datos de pacientes en los SMS.
- Credenciales Twilio exclusivamente en variables de entorno.
- Limitar el acceso a datos personales de destinatarios.

## Control de duplicados

Se registra una alerta activa por destinatario y condicion.

Cuando la condicion desaparece, deja de estar activa.
Si reaparece, puede originar una nueva notificacion.

Las solicitudes interrumpidas quedan INDETERMINADO.
No se reintentan automaticamente para evitar duplicados.

## Modos

simulado:
- Prueba el mensaje personalizado completo.
- No envia SMS.
- Registra estado SIMULADA.

twilio con SMS_MODO_TRIAL=SI:
- Envia la plantilla autorizada sms_internal_alerts.
- No envia el contenido personalizado.
- Requiere remitente y destinatario habilitados.

twilio con SMS_MODO_TRIAL=NO:
- Solicita el envio del mensaje personalizado completo.
- Requiere una cuenta habilitada para contenido personalizado.

## Ejecucion

Desde src/CareStock:

./mvnw clean verify

./mvnw -q exec:java \
  -Dexec.mainClass=org.example.carestock.sms.ProcesoSms

## Pendientes para produccion

- Despliegue permanente del worker.
- Gestion administrativa del consentimiento y revocacion.
- Supervision de estados finales de entrega de Twilio.
- Politica de reintentos y reconciliacion de indeterminados.
- Pruebas integrales con dos farmacias en una base de pruebas.

El estado ACEPTADA indica aceptacion por el proveedor,
no entrega confirmada al telefono.
