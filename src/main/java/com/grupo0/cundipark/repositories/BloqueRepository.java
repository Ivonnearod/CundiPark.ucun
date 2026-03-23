package com.grupo0.cundipark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.grupo0.cundipark.models.Bloque;

@Repository
public interface BloqueRepository extends JpaRepository<Bloque, Long> {

}
