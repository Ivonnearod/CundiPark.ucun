package com.grupo0.cundipark.services;

import com.grupo0.cundipark.exceptions.ResourceNotFoundException;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.models.Vehiculo;
import com.grupo0.cundipark.repositories.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VehiculoService {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    public List<Vehiculo> getAllVehiculos() {
        return vehiculoRepository.findAll();
    }

    public Optional<Vehiculo> getVehiculoById(Long id) {
        return vehiculoRepository.findById(id);
    }

    public Optional<Vehiculo> getVehiculoByPlaca(String placa) {
        return vehiculoRepository.findByPlaca(placa);
    }

    public List<Vehiculo> getVehiculosByUser(User user) {
        return vehiculoRepository.findByUser(user);
    }

    public Vehiculo saveVehiculo(Vehiculo vehiculo) {
        return vehiculoRepository.save(vehiculo);
    }
}