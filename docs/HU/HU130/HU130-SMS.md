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

## Integracion con Simple SMS Gateway

### Implementacion

Se incorporo Simple SMS Gateway como proveedor adicional
de notificaciones SMS para CareStock.

El servicio utiliza un telefono Android con SIM activa
para enviar mensajes SMS tradicionales mediante la API local.

El proveedor se selecciona con:

SMS_PROVIDER=simple-android

### Arquitectura

CareStock / ProcesoSms
    |
    v
ServicioAlertasSms
    |
    v
ServicioSmsSimpleAndroid
    |
    v
Simple SMS Gateway (Android)
    |
    v
Red del operador movil
    |
    v
Destinatario autorizado

### Funcionalidades

- Envio de SMS reales con contenido personalizado.
- Identificacion de farmacia y productos afectados.
- Reutilizacion de la interfaz ServicioSms.
- Compatibilidad con el sistema de alertas existente.
- Validacion de destinatarios autorizados.
- Registro de resultados mediante ResultadoSms.
- Conservacion de las implementaciones Twilio y simulada.

### Pruebas realizadas

Se comprobo el envio de un SMS personalizado desde
CareStock utilizando un Android con Simple SMS Gateway.

Se verifico la conectividad local del servidor
y se adopto HttpURLConnection para la comunicacion HTTP.

### Limitaciones conocidas

- El Android emisor debe permanecer encendido.
- Simple SMS Gateway debe permanecer activo.
- El telefono necesita cobertura y una SIM habilitada.
- CareStock debe mantener activo el proceso de monitoreo.
- La comunicacion local requiere conectividad de red.
- El envio puede generar cargos segun el operador.
- La API local debe mantenerse en una red privada segura.
- Una respuesta HTTP exitosa no confirma entrega al receptor.
- Los envios fallidos no se reintentan automaticamente.

### Pendientes

- Validar el flujo automatico completo con datos de Neon.
- Definir una politica segura de reintentos.
- Evaluar la disponibilidad permanente del servicio SMS.
- Consultar con el docente las condiciones de despliegue.
- Determinar si se requiere infraestructura dedicada.

### Conclusion

CareStock dispone de una integracion capaz de enviar
SMS tradicionales y personalizados mediante Android.

La arquitectura permite sustituir el proveedor SMS
sin modificar los servicios principales del inventario.

La disponibilidad permanente queda pendiente
de una decision de despliegue.
