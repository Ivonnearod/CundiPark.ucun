package com.grupo0.cundipark.repositories;

import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    
    long countByActivo(boolean activo);
    long countByRol(RolUsuario rol);
    List<User> findByRol(RolUsuario rol);

    List<User> findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCaseOrTelefonoContainingIgnoreCaseOrProgramaContainingIgnoreCaseOrTipoVinculacionContainingIgnoreCase(
            String nombre, String email, String telefono, String programa, String tipoVinculacion);
}