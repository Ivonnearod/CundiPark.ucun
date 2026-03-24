package com.grupo0.cundipark.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;

public interface RegistroRepository extends JpaRepository<Registro, Long>, JpaSpecificationExecutor<Registro> {
    
    List<Registro> findByActivo(Boolean activo);

    Registro findByUserAndActivo(User user, boolean activo);

    List<Registro> findTop5ByUserOrderByCreatedAtDesc(User user);
}