# Issue #891 - Integracion SMS

Historia principal: HU.130 (#885).

## Objetivo

Integrar un proveedor SMS desacoplado de JavaFX
mediante la interfaz ServicioSms.

## Arquitectura

- ServicioSms: contrato de envio.
- ServicioSmsTwilio: implementacion HTTP real.
- ServicioSmsSimulado: pruebas sin consumo SMS.
- FabricaServicioSms: seleccion por configuracion.
- ValidacionSms: validacion E.164 y contenido.
- DestinatariosPermitidosSms: autorizacion inicial.
- ResultadoSms: resultado de la solicitud.

## Configuracion

Variables:

- SMS_PROVIDER: simulado o twilio.
- SMS_ALLOWED_TO: telefonos autorizados separados por comas.
- SMS_CONFIRM_REAL_SEND: SI para habilitar Twilio.
- TWILIO_ACCOUNT_SID: identificador de cuenta.
- TWILIO_AUTH_TOKEN: credencial privada.
- TWILIO_FROM_NUMBER: numero remitente habilitado.

No guardar credenciales reales en Git.

## Seguridad

El servicio solo acepta destinatarios incluidos
en SMS_ALLOWED_TO.

La autorizacion por usuario y farmacia se integrara
posteriormente con la configuracion de destinatarios.

Las credenciales nunca deben incluirse en la
interfaz grafica, el codigo fuente ni los logs.

En produccion, las credenciales deben permanecer
en un proceso seguro fuera del escritorio JavaFX.

## Pruebas

Desde src/CareStock:

./mvnw clean verify

Para probar el simulador:

export SMS_PROVIDER=simulado
export SMS_ALLOWED_TO=+573001234567

java -cp target/classes \
org.example.carestock.sms.PruebaSmsConsola \
+573001234567

## Limitaciones del alcance

Esta subissue implementa el canal de comunicacion.

No incluye:
- deteccion de alertas del inventario;
- ejecucion programada;
- registro persistente de envios;
- reintentos;
- control persistente de duplicados;
- asociacion de destinatarios por farmacia.

Esas responsabilidades pertenecen a las
siguientes subissues de HU.130.
