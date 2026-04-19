package com.grupo0.cundipark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.grupo0.cundipark.models.Bloque;
import java.util.List;

public interface BloqueRepository extends JpaRepository<Bloque, Long> {
    // Métodos de consulta personalizados
    boolean existsByNombreIgnoreCase(String nombre);
    
    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    List<Bloque> findByActivo(boolean activo);
}