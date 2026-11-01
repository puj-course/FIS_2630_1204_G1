package org.example.carestock.DataTransferObject;

public final class LecturaCampos {

    private LecturaCampos() { }

    public static double precio(String texto) {
        String valor = texto == null ? "" : texto.trim();

        if (valor.isEmpty()) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }

        double numero;

        try {
            numero = Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "El precio debe ser un numero valido"
            );
        }

        if (!Double.isFinite(numero) || numero < 0) {
            throw new IllegalArgumentException(
                    "El precio debe ser no negativo"
            );
        }

        return numero;
    }

    public static int entero(String texto, String sujeto) {
        String valor = texto == null ? "" : texto.trim();

        if (valor.isEmpty()) {
            throw new IllegalArgumentException(sujeto + " es obligatorio");
        }

        int numero = convertir(valor, sujeto);

        if (numero < 0) {
            throw new IllegalArgumentException(
                    sujeto + " no puede ser negativo"
            );
        }

        return numero;
    }

    public static int enteroOpcional(String texto, String sujeto) {
        String valor = texto == null ? "" : texto.trim();

        if (valor.isEmpty()) {
            return 0;
        }

        return convertir(valor, sujeto);
    }

    private static int convertir(String valor, String sujeto) {
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    sujeto + " debe ser un numero entero"
            );
        }
    }
}
