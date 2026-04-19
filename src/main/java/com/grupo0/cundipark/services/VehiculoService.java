package com.grupo0.cundipark.services;

import com.grupo0.cundipark.models.Vehiculo;
import com.grupo0.cundipark.repositories.VehiculoRepository;
import com.grupo0.cundipark.validators.ValidadorPlaca;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class VehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    public Optional<Vehiculo> findByPlaca(String placa) {
        if (placa == null) return Optional.empty();
        // Normalizamos usando el validador para asegurar el formato AAA-123
        String placaFormateada = ValidadorPlaca.formatear(placa);
        return vehiculoRepository.findByPlaca(placaFormateada);
    }

    public Vehiculo saveVehiculo(Vehiculo vehiculo) {
        // Aseguramos formato antes de guardar
        vehiculo.setPlaca(ValidadorPlaca.formatear(vehiculo.getPlaca()));
        return vehiculoRepository.save(vehiculo);
    }
}