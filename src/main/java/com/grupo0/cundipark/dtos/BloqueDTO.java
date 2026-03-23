package com.grupo0.cundipark.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BloqueDTO {
    private Long id;
    
    @NotBlank(message = "El nombre del bloque es requerido")
    private String nombre;
    
    private Integer capacidad;
    private Integer disponibles;
    private Boolean activo;
}
