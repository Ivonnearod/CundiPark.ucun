package com.grupo0.cundipark.models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "registros")
public class Registro extends BaseModel {

    @Column(nullable = false, length = 6)
    private String placa;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column
    private LocalDateTime fechaSalida;

    @ManyToOne
    @JoinColumn(name = "users_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "bloques_id", nullable = false)
    private Bloque bloque;
}

