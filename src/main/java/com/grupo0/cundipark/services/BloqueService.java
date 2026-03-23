package com.grupo0.cundipark.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.repositories.BloqueRepository;
import com.grupo0.cundipark.exceptions.ResourceNotFoundException;

@Service
public class BloqueService {

    @Autowired
    private BloqueRepository bloqueRepository;

    public List<Bloque> getAllBloques() {
        return bloqueRepository.findAll();
    }

    public List<Bloque> findAll() {
        return bloqueRepository.findAll();
    }

    public Bloque getBloqueById(Long id) {
        return bloqueRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bloque not found with id: " + id));
    }

    public Bloque findById(Long id) {
        return bloqueRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bloque not found with id: " + id));
    }

    public Bloque saveBloque(Bloque bloque) {
        return bloqueRepository.save(bloque);
    }

    public void deleteBloque(Long id) {
        bloqueRepository.deleteById(id);
    }
}
