package com.grupo0.cundipark.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.grupo0.cundipark.models.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}