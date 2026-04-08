package com.grupo0.cundipark.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDTO {

    private Long id;

    @NotBlank(message = "La placa no puede estar vacía")
    private String vehiculoPlaca; // Ahora se refiere a la placa del vehículo asociado

    @NotNull(message = "El ID de usuario no puede ser nulo")
    private Long userId;

    @NotNull(message = "El ID de bloque no puede ser nulo")
    private Long bloqueId;
    
    @NotNull(message = "El ID del vehículo no puede ser nulo")
    private Long vehiculoId; // Nuevo campo para vincular al vehículo

    private Boolean activo;

    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private LocalDateTime createdAt; // Heredado de BaseModel, útil para DTO
    private LocalDateTime updatedAt; // Heredado de BaseModel, útil para DTO

    // Campos adicionales para mostrar en el DTO si se necesitan detalles del usuario/bloque
    private String userName;
    private String userEmail;
    private String bloqueNombre;

    // Detalles del vehículo para mostrar
    private String vehiculoMarca;
    private String vehiculoModelo;
    private String vehiculoColor;
}