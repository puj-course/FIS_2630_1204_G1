package org.example.carestock.exception;

/**
 * Esto nos permite en cada clase que creemos dentro de carestock arrojar una
 * Excepcion personalizada dependiendo de que queramos decir en cada clase
 */
public class ReglaNegocioException extends Exception {
    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}