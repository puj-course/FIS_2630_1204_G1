package org.example.carestock.sms;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ServicioSmsTwilio implements ServicioSms {

    private static final Pattern PATRON_CUENTA_SID =
            Pattern.compile("AC[0-9a-fA-F]{32}");

    private static final Pattern SID_MENSAJE =
            Pattern.compile(
                "\"sid\"\\s*:\\s*\"((?:SM|MM)[0-9a-fA-F]{32})\""
            );

    @FunctionalInterface
    interface Transporte {
        RespuestaHttp ejecutar(HttpRequest solicitud)
                throws IOException, InterruptedException;
    }

    record RespuestaHttp(int codigo, String contenido) {
    }

    private final String cuenta;
    private final String token;
    private final String remitente;
    private final DestinatariosPermitidosSms autorizados;
    private final Transporte transporte;

    public ServicioSmsTwilio(
            String cuenta,
            String token,
            String remitente,
            DestinatariosPermitidosSms autorizados) {

        this(cuenta, token, remitente, autorizados,
                crearTransporte());
    }

    ServicioSmsTwilio(
            String cuenta,
            String token,
            String remitente,
            DestinatariosPermitidosSms autorizados,
            Transporte transporte) {

        if (cuenta == null || !PATRON_CUENTA_SID.matcher(cuenta).matches()) {
            throw new IllegalArgumentException(
                    "TWILIO_ACCOUNT_SID invalido."
            );
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException(
                    "TWILIO_AUTH_TOKEN faltante."
            );
        }

        ValidacionSms.exigirNumeroValido(remitente);

        this.cuenta = cuenta;
        this.token = token;
        this.remitente = remitente;
        this.autorizados = Objects.requireNonNull(autorizados);
        this.transporte = Objects.requireNonNull(transporte);
    }

    @Override
    public ResultadoSms enviar(String destino, String mensaje) {

        autorizados.exigirAutorizado(destino);
        ValidacionSms.exigirMensajeValido(mensaje);

        String formulario =
                "To=" + codificar(destino)
                + "&From=" + codificar(remitente)
                + "&Body=" + codificar(mensaje);

        String autenticacion = Base64.getEncoder()
                .encodeToString(
                    (cuenta + ":" + token)
                    .getBytes(StandardCharsets.UTF_8)
                );

        HttpRequest solicitud = HttpRequest.newBuilder()
                .uri(URI.create(
                    "https://api.twilio.com/2010-04-01/Accounts/"
                    + cuenta + "/Messages.json"
                ))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Basic " + autenticacion)
                .header(
                    "Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8"
                )
                .POST(HttpRequest.BodyPublishers.ofString(
                        formulario, StandardCharsets.UTF_8))
                .build();

        try {
            RespuestaHttp respuesta =
                    transporte.ejecutar(solicitud);

            if (respuesta.codigo() < 200
                    || respuesta.codigo() >= 300) {
                throw new ExcepcionSms(
                    "Twilio rechazo el envio (HTTP "
                    + respuesta.codigo() + ")."
                );
            }

            Matcher sid = SID_MENSAJE.matcher(
                    respuesta.contenido() == null
                    ? "" : respuesta.contenido()
            );

            if (!sid.find()) {
                throw new ExcepcionSms(
                    "Twilio respondio sin un identificador "
                    + "de mensaje valido."
                );
            }

            return new ResultadoSms(
                    true,
                    false,
                    respuesta.codigo(),
                    sid.group(1)
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExcepcionSms(
                    "Envio SMS interrumpido.", e
            );

        } catch (IOException e) {
            throw new ExcepcionSms(
                    "No fue posible conectar con Twilio.", e
            );
        }
    }

    private static String codificar(String valor) {
        return URLEncoder.encode(
                valor, StandardCharsets.UTF_8
        );
    }

    private static Transporte crearTransporte() {

        HttpClient cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        return solicitud -> {
            HttpResponse<String> respuesta = cliente.send(
                    solicitud,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8)
            );

            return new RespuestaHttp(
                    respuesta.statusCode(),
                    respuesta.body()
            );
        };
    }
}
