package com.grupo0.cundipark.services;

import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.repositories.BloqueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BloqueService {

    @Autowired
    private BloqueRepository bloqueRepository;

    public List<Bloque> getAllBloques() {
        return bloqueRepository.findAll();
    }

    public Bloque getBloqueById(Long id) {
        return bloqueRepository.findById(id).orElse(null);
    }

    public Bloque saveBloque(Bloque bloque) {
        return bloqueRepository.save(bloque);
    }

    public List<Bloque> getBloquesActivos() {
        return bloqueRepository.findByActivo(true);
    }

    public boolean existsByNombreIgnoreCase(String nombre) {
        return bloqueRepository.existsByNombreIgnoreCase(nombre);
    }

    public boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id) {
        return bloqueRepository.existsByNombreIgnoreCaseAndIdNot(nombre, id);
    }

    public void deleteBloque(Long id) {
        bloqueRepository.deleteById(id);
    }
}