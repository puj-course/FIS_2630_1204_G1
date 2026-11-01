package org.example.carestock.sms;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Envia SMS personalizados mediante SMSGate en Android.
 * Requiere un telefono con SIM conectado a la red local.
 */
public final class ServicioSmsAndroid implements ServicioSms {

    private static final Pattern ID = Pattern.compile(
            "\"id\"\\s*:\\s*\"([^\"]{1,80})\""
    );

    @FunctionalInterface
    interface Transporte {
        Respuesta ejecutar(HttpRequest solicitud)
                throws IOException, InterruptedException;
    }

    record Respuesta(int codigo, String cuerpo) {}

    private final URI url;
    private final String autenticacion;
    private final DestinatariosPermitidosSms permitidos;
    private final Transporte transporte;

    public ServicioSmsAndroid(
            String urlBase,
            String usuario,
            String clave,
            DestinatariosPermitidosSms permitidos) {

        this(urlBase, usuario, clave, permitidos, transporteReal());
    }

    ServicioSmsAndroid(
            String urlBase,
            String usuario,
            String clave,
            DestinatariosPermitidosSms permitidos,
            Transporte transporte) {

        if (usuario == null || usuario.isBlank()
                || clave == null || clave.isBlank()) {
            throw new IllegalArgumentException(
                    "Faltan credenciales de SMSGate."
            );
        }

        this.url = validarServidorLocal(urlBase);

        this.autenticacion = "Basic "
                + Base64.getEncoder().encodeToString(
                    (usuario + ":" + clave)
                    .getBytes(StandardCharsets.UTF_8)
                );

        this.permitidos = Objects.requireNonNull(permitidos);
        this.transporte = Objects.requireNonNull(transporte);
    }

    @Override
    public ResultadoSms enviar(String destino, String mensaje) {

        permitidos.exigirAutorizado(destino);
        ValidacionSms.exigirMensajeValido(mensaje);

        String json = "{\"textMessage\":{\"text\":\""
                + escaparJson(mensaje)
                + "\"},\"phoneNumbers\":[\""
                + escaparJson(destino)
                + "\"]}";

        HttpRequest solicitud = HttpRequest.newBuilder(url)
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", autenticacion)
                .header(
                    "Content-Type",
                    "application/json; charset=UTF-8"
                )
                .POST(HttpRequest.BodyPublishers.ofString(
                        json, StandardCharsets.UTF_8))
                .build();

        try {
            Respuesta respuesta = transporte.ejecutar(solicitud);

            if (respuesta.codigo() < 200
                    || respuesta.codigo() >= 300) {

                throw new ExcepcionSms(
                        "SMSGate rechazo la solicitud (HTTP "
                        + respuesta.codigo() + ")."
                );
            }

            Matcher id = ID.matcher(
                    respuesta.cuerpo() == null
                    ? ""
                    : respuesta.cuerpo()
            );

            String referencia = id.find()
                    ? id.group(1)
                    : "ANDROID-HTTP-" + respuesta.codigo();

            // Aceptacion HTTP no garantiza entrega al telefono.
            return new ResultadoSms(
                    true,
                    false,
                    respuesta.codigo(),
                    referencia
            );

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            throw new ExcepcionSms(
                    "Solicitud al Android interrumpida.", e
            );

        } catch (IOException e) {
            throw new ExcepcionSms(
                    "No hay conexion con SMSGate en la red local.", e
            );
        }
    }

    private static URI validarServidorLocal(String valor) {

        try {
            URI base = URI.create(Objects.requireNonNull(valor));
            String host = base.getHost();

            if (!"http".equalsIgnoreCase(base.getScheme())
                    || host == null
                    || !host.matches("[0-9.]+")
                    || base.getPort() < 1
                    || base.getUserInfo() != null
                    || base.getQuery() != null
                    || base.getFragment() != null
                    || !(base.getPath().isBlank()
                         || "/".equals(base.getPath()))) {

                throw new IllegalArgumentException(
                        "SMS_GATEWAY_URL debe ser "
                        + "http://IP_PRIVADA:PUERTO"
                );
            }

            InetAddress ip = InetAddress.getByName(host);

            if (ip.getAddress().length != 4
                    || !ip.isSiteLocalAddress()) {

                throw new IllegalArgumentException(
                        "SMS_GATEWAY_URL debe usar "
                        + "una IPv4 privada de la LAN."
                );
            }

            return URI.create(
                    "http://" + host + ":"
                    + base.getPort() + "/message"
            );

        } catch (IllegalArgumentException e) {
            throw e;

        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Direccion SMS_GATEWAY_URL invalida.", e
            );
        }
    }

    private static String escaparJson(String s) {
        StringBuilder b = new StringBuilder(s.length() + 16);

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");

                default -> {
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }

        return b.toString();
    }

    private static Transporte transporteReal() {

        HttpClient cliente = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();

        return solicitud -> {
            HttpResponse<String> respuesta = cliente.send(
                    solicitud,
                    HttpResponse.BodyHandlers.ofString(
                            StandardCharsets.UTF_8)
            );

            return new Respuesta(
                    respuesta.statusCode(),
                    respuesta.body()
            );
        };
    }
}
