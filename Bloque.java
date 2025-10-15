package com.grupo0.cundipark.models;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "bloques")
public class Bloque extends BaseModel {

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private Integer totalPlazas;

    @Column(nullable = false)
    private Integer plazasDisponibles;

    @OneToMany(mappedBy = "bloque", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registro> registros;
}
