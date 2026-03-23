package com.grupo0.cundipark.exceptions;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String mensaje) {
        super(mensaje);
    }

    public DuplicateResourceException(String recurso, String campo, Object valor) {
        super(recurso + " con " + campo + " '" + valor + "' ya existe");
    }
}
