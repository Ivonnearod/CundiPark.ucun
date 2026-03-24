package com.grupo0.cundipark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.grupo0.cundipark.models.Bloque;

public interface BloqueRepository extends JpaRepository<Bloque, Long> {
    // Métodos de consulta personalizados si son necesarios
}