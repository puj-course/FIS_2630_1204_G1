package com.carestock.exception;

/**
 * Se lanza cuando el usuario de la sesión activa intenta ejecutar
 * una operación para la cual su rol no está autorizado.
 *
 * Es una RuntimeException a propósito: la validación de rol es una
 * regla de negocio que debe poder lanzarse desde cualquier capa
 * (DAO, Service) sin obligar a declarar "throws" en toda la cadena
 * de llamadas.
 */
public class AccesoDenegadoException extends RuntimeException {

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
