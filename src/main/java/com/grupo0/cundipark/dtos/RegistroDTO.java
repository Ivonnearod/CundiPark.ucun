package com.grupo0.cundipark.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegistroDTO {

    private Long id;
    
    @NotBlank(message = "La placa no puede estar vacía")
    private String vehiculoPlaca; 

    @NotNull(message = "El ID de usuario no puede ser nulo")
    private Long userId;

    @NotNull(message = "El ID de bloque no puede ser nulo")
    private Long bloqueId;
    
    private Boolean activo;

    private Long vehiculoId;

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
    
    @NotNull(message = "La fecha de vencimiento del SOAT es obligatoria")
    private LocalDate soatVencimiento;
    
    @NotNull(message = "La fecha de vencimiento de la tecnomecánica es obligatoria")
    private LocalDate tecnomecanicaVencimiento;
}