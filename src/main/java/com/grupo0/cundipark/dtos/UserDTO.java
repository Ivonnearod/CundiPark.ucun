package com.grupo0.cundipark.dtos;

import com.grupo0.cundipark.enums.RolUsuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long id;
    private String email;
    private String nombre;
    private String password;
    private RolUsuario rol;
    private Boolean activo;
}
