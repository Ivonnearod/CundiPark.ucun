package com.grupo0.cundipark.utils;

import com.grupo0.cundipark.dtos.BloqueDTO;
import com.grupo0.cundipark.dtos.RegistroDTO;
import com.grupo0.cundipark.dtos.UserDTO;
import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;

public class MapperUtil {

    public static UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setNombre(user.getNombre());
        dto.setActivo(user.getActivo());
        if (user.getRol() != null) {
            dto.setRol(user.getRol().name());
        }
        return dto;
    }

    public static BloqueDTO toBloqueDTO(Bloque bloque) {
        BloqueDTO dto = new BloqueDTO();
        dto.setId(bloque.getId());
        dto.setNombre(bloque.getNombre());
        dto.setCapacidad(bloque.getCapacidad());
        dto.setDisponibles(bloque.getDisponibles());
        dto.setActivo(bloque.getActivo());
        return dto;
    }

    public static RegistroDTO toRegistroDTO(Registro registro) {
        RegistroDTO dto = new RegistroDTO();
        dto.setId(registro.getId());
        dto.setPlaca(registro.getPlaca());
        dto.setActivo(registro.getActivo());
        dto.setFechaSalida(registro.getFechaSalida());
        dto.setCreatedAt(registro.getCreatedAt());
        
        if (registro.getUser() != null) {
            dto.setUserId(registro.getUser().getId());
            dto.setUserEmail(registro.getUser().getEmail());
        }
        if (registro.getBloque() != null) {
            dto.setBloqueId(registro.getBloque().getId());
            dto.setBloqueNombre(registro.getBloque().getNombre());
        }
        return dto;
    }
}