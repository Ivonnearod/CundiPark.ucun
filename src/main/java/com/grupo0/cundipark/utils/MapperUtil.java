package com.grupo0.cundipark.utils;

import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.dtos.UserDTO;
import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.dtos.BloqueDTO;
import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.dtos.RegistroDTO;

public class MapperUtil {

    public static UserDTO toUserDTO(User user) {
        if (user == null) return null;
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nombre(user.getNombre())
                .activo(user.getPassword() != null)
                .build();
    }

    public static BloqueDTO toBloqueDTO(Bloque bloque) {
        if (bloque == null) return null;
        return BloqueDTO.builder()
                .id(bloque.getId())
                .nombre(bloque.getNombre())
                .capacidad(bloque.getCapacidad())
                .disponibles(bloque.getDisponibles())
                .activo(bloque.getActivo())
                .build();
    }

    public static RegistroDTO toRegistroDTO(Registro registro) {
        if (registro == null) return null;
        return RegistroDTO.builder()
                .id(registro.getId())
                .placa(registro.getPlaca())
                .activo(registro.getActivo())
                .userId(registro.getUser() != null ? registro.getUser().getId() : null)
                .bloqueId(registro.getBloque() != null ? registro.getBloque().getId() : null)
                .bloqueNombre(registro.getBloque() != null ? registro.getBloque().getNombre() : null)
                .createdAt(registro.getCreatedAt())
                .updatedAt(registro.getUpdatedAt())
                .build();
    }

}
