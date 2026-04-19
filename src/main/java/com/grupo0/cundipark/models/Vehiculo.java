package com.grupo0.cundipark.models;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "vehiculos")
@Getter
@Setter
public class Vehiculo extends BaseModel {

    @NotBlank(message = "La placa es obligatoria")
    @Size(min = 6, max = 10, message = "La placa debe tener entre 6 y 10 caracteres")
    @Column(unique = true, nullable = false)
    private String placa;

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    private String color;

    @NotNull(message = "La fecha de vencimiento del SOAT es obligatoria")
    private LocalDate soatVencimiento;

    @NotNull(message = "La fecha de vencimiento de la tecnomecánica es obligatoria")
    private LocalDate tecnomecanicaVencimiento;

    // Relación con el usuario propietario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Relación con los registros de parqueo
    @OneToMany(mappedBy = "vehiculo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Registro> registros;

    // Métodos de conveniencia para verificar vigencia
    public Boolean isSoatVigente() {
        return soatVencimiento != null && !soatVencimiento.isBefore(LocalDate.now());
    }

    public Boolean isTecnomecanicaVigente() {
        return tecnomecanicaVencimiento != null && !tecnomecanicaVencimiento.isBefore(LocalDate.now());
    }
}