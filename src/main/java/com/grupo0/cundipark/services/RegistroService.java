package com.grupo0.cundipark.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.grupo0.cundipark.models.Registro;
import com.grupo0.cundipark.repositories.RegistroRepository;
import com.grupo0.cundipark.exceptions.ResourceNotFoundException;

import jakarta.persistence.criteria.Predicate;

@Service
public class RegistroService {

    @Autowired
    private RegistroRepository registroRepository;

    public List<Registro> getAllRegistros() {
        return registroRepository.findAll();
    }

    public Registro getRegistroById(Long id) {
        return registroRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Registro not found with id: " + id));
    }

    public Registro saveRegistro(Registro registro) {
        return registroRepository.save(registro);
    }

    public void deleteRegistro(Long id) {
        registroRepository.deleteById(id);
    }

    public List<Registro> findByActivoTrue() {
        return registroRepository.findAll().stream()
            .filter(r -> Boolean.TRUE.equals(r.getActivo()))
            .toList();
    }

    public Page<Registro> buscarConFiltros(LocalDateTime desde, LocalDateTime hasta,
                                           Long bloqueId, String placa, Boolean activo, Pageable pageable) {
        return registroRepository.findAll((Specification<Registro>) (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (desde != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), desde));
            }
            if (hasta != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), hasta));
            }
            if (placa != null && !placa.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("placa")), "%" + placa.toLowerCase() + "%"));
            }
            if (bloqueId != null) {
                predicates.add(criteriaBuilder.equal(root.get("bloque").get("id"), bloqueId));
            }
            if (activo != null) {
                predicates.add(criteriaBuilder.equal(root.get("activo"), activo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        }, pageable);
    }
}
