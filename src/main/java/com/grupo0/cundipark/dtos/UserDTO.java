package com.grupo0.cundipark.dtos;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String email;
    private String nombre;
    private String rol;
    private Boolean activo;
    
    // Campo opcional para registro/actualización (no se devuelve en respuestas GET por seguridad)
    private String password;
}