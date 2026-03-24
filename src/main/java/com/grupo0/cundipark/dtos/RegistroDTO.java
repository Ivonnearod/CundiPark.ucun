package com.grupo0.cundipark.dtos;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RegistroDTO {
    private Long id;
    private String placa;
    private boolean activo;
    private LocalDateTime fechaSalida;
    private Long userId;
    private String userEmail;
    private Long bloqueId;
    private String bloqueNombre;
    private LocalDateTime createdAt;
}