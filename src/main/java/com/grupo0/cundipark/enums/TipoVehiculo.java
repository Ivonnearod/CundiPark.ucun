package com.grupo0.cundipark.enums;

public enum TipoVehiculo {
    AUTO("Automóvil"),
    MOTO("Motocicleta"),
    CAMION("Camión"),
    BUS("Autobús"),
    OTRO("Otro");

    private final String descripcion;

    TipoVehiculo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
