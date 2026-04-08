package com.grupo0.cundipark.repositories;

import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroRepository extends JpaRepository<Registro, Long>, JpaSpecificationExecutor<Registro> {
    
    List<Registro> findByActivo(boolean activo);
    
    Registro findByUserAndActivo(User user, boolean activo);
    
    List<Registro> findTop5ByUserOrderByCreatedAtDesc(User user);

    boolean existsByVehiculoPlacaAndActivo(String placa, boolean activo);
}