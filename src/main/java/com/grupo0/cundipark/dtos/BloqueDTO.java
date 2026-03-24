package com.grupo0.cundipark.dtos;

import lombok.Data;

@Data
public class BloqueDTO {
    private Long id;
    private String nombre;
    private Integer capacidad;
    private Integer disponibles;
    private Boolean activo;
}