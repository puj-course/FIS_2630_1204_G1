package org.example.carestock.sms;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.UUID;

public final class ServicioSmsSimpleAndroid implements ServicioSms {

    private final URI endpoint;
    private final DestinatariosPermitidosSms autorizados;

    public ServicioSmsSimpleAndroid(
            String servidor,
            DestinatariosPermitidosSms autorizados) {

        this.endpoint = validarServidor(servidor);
        this.autorizados = Objects.requireNonNull(autorizados);
    }

    @Override
    public ResultadoSms enviar(String destino, String mensaje) {

        autorizados.exigirAutorizado(destino);
        ValidacionSms.exigirMensajeValido(mensaje);

        String json = "{\"phone\":\"" + escapar(destino)
                + "\",\"message\":\"" + escapar(mensaje) + "\"}";

        byte[] datos = json.getBytes(StandardCharsets.UTF_8);

        HttpURLConnection conexion = null;

        try {
            conexion = (HttpURLConnection) endpoint
                    .toURL()
                    .openConnection(Proxy.NO_PROXY);

            conexion.setConnectTimeout(5000);
            conexion.setReadTimeout(20000);
            conexion.setRequestMethod("POST");
            conexion.setInstanceFollowRedirects(false);
            conexion.setDoOutput(true);

            conexion.setRequestProperty(
                    "Content-Type",
                    "application/json; charset=UTF-8"
            );

            conexion.setFixedLengthStreamingMode(datos.length);

            try (var salida = conexion.getOutputStream()) {
                salida.write(datos);
            }

            int codigo = conexion.getResponseCode();

            if (codigo < 200 || codigo >= 300) {
                throw new ExcepcionSms(
                        "Simple SMS Gateway rechazo el envio. HTTP "
                        + codigo
                );
            }

            return new ResultadoSms(
                    true,
                    false,
                    codigo,
                    "LOCAL-" + UUID.randomUUID()
            );

        } catch (IOException e) {
            throw new ExcepcionSms(
                    "Error de comunicacion con Android: "
                    + e.getClass().getSimpleName()
                    + ": " + e.getMessage(),
                    e
            );

        } finally {
            if (conexion != null) {
                conexion.disconnect();
            }
        }
    }

    private static URI validarServidor(String servidor) {
        try {
            URI uri = URI.create(Objects.requireNonNull(servidor));

            if (!"http".equalsIgnoreCase(uri.getScheme())
                    || uri.getHost() == null
                    || !uri.getHost().matches("[0-9.]+")
                    || uri.getPort() < 1
                    || uri.getUserInfo() != null
                    || uri.getQuery() != null
                    || uri.getFragment() != null
                    || !(uri.getPath().isEmpty()
                         || "/".equals(uri.getPath()))) {
                throw new IllegalArgumentException(
                        "Se requiere http://IP_PRIVADA:PUERTO");
            }

            InetAddress direccion =
                    InetAddress.getByName(uri.getHost());

            if (direccion.getAddress().length != 4
                    || !direccion.isSiteLocalAddress()) {
                throw new IllegalArgumentException(
                        "El gateway debe estar en una red IPv4 privada.");
            }

            return URI.create(
                    uri.toString().replaceAll("/$", "") + "/send-sms"
            );

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Direccion del gateway invalida.", e);
        }
    }

    private static String escapar(String texto) {
        StringBuilder resultado = new StringBuilder();

        for (char c : texto.toCharArray()) {
            switch (c) {
                case '"' -> resultado.append("\\\"");
                case '\\' -> resultado.append("\\\\");
                case '\n' -> resultado.append("\\n");
                case '\r' -> resultado.append("\\r");
                case '\t' -> resultado.append("\\t");
                default -> {
                    if (c < 32) {
                        resultado.append(
                                String.format("\\u%04x", (int) c));
                    } else {
                        resultado.append(c);
                    }
                }
            }
        }

        return resultado.toString();
    }
}
