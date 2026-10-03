package com.carestock.exception;

public class MedicamentoDuplicadoException extends RuntimeException {

    public MedicamentoDuplicadoException(String codigoInvima) {
        super("Ya existe un medicamento con el código INVIMA '" + codigoInvima +
                "' registrado en esta farmacia.");
    }
}
