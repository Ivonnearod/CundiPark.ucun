package com.grupo0.cundipark.models;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User extends BaseModel {

    @Email
    @Column(nullable = false, unique = true, length = 64)
    private String email;

    @Column(length = 64)
    private String nombre;

    @Column(nullable = false)
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    @Transient
    private String passwordConfirmation;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registro> registros;

}
