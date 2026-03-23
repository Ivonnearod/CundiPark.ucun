package com.grupo0.cundipark.dtos;

import com.grupo0.cundipark.enums.EstadoVehiculo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroDTO {
    private Long id;
    private String placa;
    private boolean activo;
    private EstadoVehiculo estado;
    private Long userId;
    private Long bloqueId;
    private String bloqueNombre;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime fechaSalida;
    private UserDTO user;
    private BloqueDTO bloque;
}
