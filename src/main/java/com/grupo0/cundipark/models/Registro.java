package com.grupo0.cundipark.models;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros")
@Getter
@Setter
public class Registro extends BaseModel {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bloque_id", nullable = false)
    private Bloque bloque;
    
    @NotNull(message = "La placa es obligatoria")
    @Column(nullable = false)
    private String placa;

    @NotNull(message = "El estado activo/inactivo es obligatorio")
    private Boolean activo;

    private LocalDateTime fechaEntrada;

    private LocalDateTime fechaSalida;

    // Campo para auditoría y notas del sistema
    private String observaciones;
}