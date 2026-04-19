package com.grupo0.cundipark.utils;

import com.grupo0.cundipark.dtos.BloqueDTO;
import com.grupo0.cundipark.dtos.RegistroDTO;
import com.grupo0.cundipark.dtos.UserDTO;
import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;

public class MapperUtil {

    public static RegistroDTO toRegistroDTO(Registro registro) {
        if (registro == null) return null;

        RegistroDTO dto = new RegistroDTO();
        dto.setId(registro.getId());
        dto.setActivo(registro.getActivo());
        dto.setFechaEntrada(registro.getFechaEntrada());
        dto.setFechaSalida(registro.getFechaSalida());
        dto.setCreatedAt(registro.getCreatedAt());
        dto.setUpdatedAt(registro.getUpdatedAt());

        if (registro.getUser() != null) {
            dto.setUserId(registro.getUser().getId());
            // Opcional: para mostrar en el DTO
            dto.setUserName(registro.getUser().getNombre());
            dto.setUserEmail(registro.getUser().getEmail());
        }

        if (registro.getBloque() != null) {
            dto.setBloqueId(registro.getBloque().getId());
            // Opcional: para mostrar en el DTO
            dto.setBloqueNombre(registro.getBloque().getNombre());
        }

        if (registro.getVehiculo() != null) {
            dto.setVehiculoId(registro.getVehiculo().getId());
            // Opcional: para mostrar en el DTO
            dto.setVehiculoPlaca(registro.getVehiculo().getPlaca());
            dto.setVehiculoMarca(registro.getVehiculo().getMarca());
            dto.setVehiculoModelo(registro.getVehiculo().getModelo()); // Corregido de getModel() a getModelo()
            dto.setVehiculoColor(registro.getVehiculo().getColor());
            dto.setSoatVencimiento(registro.getVehiculo().getSoatVencimiento());
            dto.setTecnomecanicaVencimiento(registro.getVehiculo().getTecnomecanicaVencimiento());
        }

        return dto;
    }

    public static UserDTO toUserDTO(User user) {
        if (user == null) return null;

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setNombre(user.getNombre());
        dto.setActivo(user.getActivo());
        if (user.getRol() != null) {
            dto.setRol(user.getRol().name());
        }
        // No mapeamos password por seguridad
        return dto;
    }

    public static BloqueDTO toBloqueDTO(Bloque bloque) {
        if (bloque == null) return null;

        BloqueDTO dto = new BloqueDTO();
        dto.setId(bloque.getId());
        dto.setNombre(bloque.getNombre());
        dto.setCapacidad(bloque.getCapacidad());
        dto.setDisponibles(bloque.getDisponibles());
        dto.setActivo(bloque.getActivo());
        return dto;
    }
}