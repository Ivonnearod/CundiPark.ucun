package com.grupo0.cundipark.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseModel {

    @Column(nullable = false, unique = true, length = 64)
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 64)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RolUsuario rol;

    private Boolean activo = true;

    @Transient
    private String passwordConfirmation;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (rol == null) {
            rol = RolUsuario.USER;
        }
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }
}