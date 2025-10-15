package com.grupo0.cundipark.repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.grupo0.cundipark.models.Registro;

@Repository
public interface RegistroRepository extends JpaRepository<Registro, Long>, JpaSpecificationExecutor<Registro> {

    List<Registro> findByActivoTrue();
    
    Optional<Registro> findByPlacaAndActivoTrue(String placa);

    List<Registro> findByBloqueIdAndCreatedAtBetweenAndActivo(Long bloqueId, LocalDateTime desde, LocalDateTime hasta, Boolean activo);
    
    List<Registro> findByBloqueIdAndActivo(Long bloqueId, Boolean activo);

    List<Registro> findByPlacaContainingIgnoreCaseAndActivo(String placa, Boolean activo);

    List<Registro> findByCreatedAtBetweenAndActivo(LocalDateTime desde, LocalDateTime hasta, Boolean activo);

    List<Registro> findByActivo(Boolean activo);

    List<Registro> findByBloqueId(Long bloqueId);

    List<Registro> findByPlacaContainingIgnoreCase(String placa);

    List<Registro> findByCreatedAtBetween(LocalDateTime desde, LocalDateTime hasta);

}
