package org.example.carestock.sms;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServicioSmsTwilioCurl implements ServicioSms {

    private static final String PLANTILLA =
            "sms_appointment_reminders";
    private static final String MARCA_HTTP =
            "__CARESTOCK_HTTP__:";

    private static final Pattern CUENTA =
            Pattern.compile("AC[0-9a-fA-F]{32}");
    private static final Pattern TOKEN =
            Pattern.compile("[A-Za-z0-9]{16,128}");
    private static final Pattern SID =
            Pattern.compile(
                "\"sid\"\\s*:\\s*\"((?:SM|MM)[0-9a-fA-F]{32})\"");
    private static final Pattern CODIGO_ERROR =
            Pattern.compile("\"code\"\\s*:\\s*(\\d{4,6})");

    @FunctionalInterface
    interface Ejecutor {
        Salida ejecutar(String configuracion)
                throws IOException, InterruptedException;
    }

    record Salida(int salidaProceso, String texto) { }

    private final String cuenta;
    private final String token;
    private final String remitente;
    private final DestinatariosPermitidosSms permitidos;
    private final Ejecutor ejecutor;

    public ServicioSmsTwilioCurl(
            String cuenta,
            String token,
            String remitente,
            DestinatariosPermitidosSms permitidos) {
        this(cuenta, token, remitente, permitidos,
                ServicioSmsTwilioCurl::ejecutarCurl);
    }

    ServicioSmsTwilioCurl(
            String cuenta,
            String token,
            String remitente,
            DestinatariosPermitidosSms permitidos,
            Ejecutor ejecutor) {

        if (cuenta == null ||
                !CUENTA.matcher(cuenta).matches()) {
            throw new IllegalArgumentException(
                    "TWILIO_ACCOUNT_SID invalido");
        }

        if (token == null ||
                !TOKEN.matcher(token).matches()) {
            throw new IllegalArgumentException(
                    "TWILIO_AUTH_TOKEN invalido");
        }

        ValidacionSms.exigirNumeroValido(remitente);

        this.cuenta = cuenta;
        this.token = token;
        this.remitente = remitente;
        this.permitidos = Objects.requireNonNull(permitidos);
        this.ejecutor = Objects.requireNonNull(ejecutor);
    }

    @Override
    public ResultadoSms enviar(String destino, String mensaje) {
        permitidos.exigirAutorizado(destino);

        if (!PLANTILLA.equals(mensaje)) {
            throw new IllegalArgumentException(
                    "Twilio Trial solo permite la plantilla configurada");
        }

        try {
            Salida salida = ejecutor.ejecutar(
                    configuracionCurl(destino, mensaje));

            if (salida.salidaProceso() != 0) {
                throw new ExcepcionSms(
                        "curl fallo (salida "
                        + salida.salidaProceso() + ")");
            }

            String texto = salida.texto() == null
                    ? "" : salida.texto();

            int posicion = texto.lastIndexOf(MARCA_HTTP);

            if (posicion < 0) {
                throw new ExcepcionSms(
                        "curl no devolvio estado HTTP");
            }

            String estado = texto.substring(
                    posicion + MARCA_HTTP.length()).trim();

            if (!estado.matches("[0-9]{3}")) {
                throw new ExcepcionSms(
                        "Respuesta HTTP de curl invalida");
            }

            int http = Integer.parseInt(estado);
            String cuerpo = texto.substring(0, posicion);

            if (http < 200 || http >= 300) {
                Matcher codigo = CODIGO_ERROR.matcher(cuerpo);
                throw new ExcepcionSms(
                        "Twilio rechazo el envio (HTTP " + http
                        + (codigo.find()
                            ? ", codigo " + codigo.group(1)
                            : "")
                        + ")");
            }

            Matcher sid = SID.matcher(cuerpo);

            if (!sid.find()) {
                throw new ExcepcionSms(
                        "Twilio no devolvio SID de mensaje valido");
            }

            return new ResultadoSms(
                    true, false, http, sid.group(1));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExcepcionSms("curl interrumpido", e);

        } catch (IOException e) {
            throw new ExcepcionSms(
                    "No fue posible iniciar curl", e);
        }
    }

    private String configuracionCurl(
            String destino, String mensaje) {

        // Credenciales por stdin, no por argumentos del proceso.
        return "silent\nshow-error\n"
                + "connect-timeout = 5\nmax-time = 20\n"
                + "basic\nrequest = \"POST\"\n"
                + "url = \"https://api.twilio.com/2010-04-01/Accounts/"
                + cuenta + "/Messages.json\"\n"
                + "header = \"Content-Type: application/x-www-form-urlencoded\"\n"
                + "user = \"" + cuenta + ":" + token + "\"\n"
                + "data-urlencode = \"To=" + destino + "\"\n"
                + "data-urlencode = \"From=" + remitente + "\"\n"
                + "data-urlencode = \"Body=" + mensaje + "\"\n"
                + "write-out = \"" + MARCA_HTTP
                + "%{http_code}\"\n";
    }

    private static Salida ejecutarCurl(String configuracion)
            throws IOException, InterruptedException {

        Process proceso = new ProcessBuilder(
                "curl.exe", "--config", "-")
                .redirectErrorStream(true)
                .start();

        try (var entrada = proceso.getOutputStream()) {
            entrada.write(
                    configuracion.getBytes(StandardCharsets.UTF_8));
        }

        String respuesta;

        try (var salida = proceso.getInputStream()) {
            respuesta = new String(
                    salida.readAllBytes(), StandardCharsets.UTF_8);
        }

        return new Salida(
                proceso.waitFor(), respuesta);
    }
}
