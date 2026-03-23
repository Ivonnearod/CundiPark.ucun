package com.grupo0.cundipark.enums;

public enum RolUsuario {
    ADMIN("Administrador"),
    OPERARIO("Operario"),
    USUARIO("Usuario General");

    private final String descripcion;

    RolUsuario(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
