package com.grupo0.cundipark.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }

    public ResourceNotFoundException(String recursos, Long id) {
        super(recursos + " con ID " + id + " no encontrado");
    }
}
