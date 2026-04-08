package com.grupo0.cundipark.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

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

    @Column(length = 20)
    private String telefono;

    @Column(length = 50)
    private String tipoVinculacion;

    @Column(length = 100)
    private String programa;

    private LocalDate soatVencimiento;

    private LocalDate tecnoVencimiento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private RolUsuario rol;

    private Boolean activo = true;

    @Transient
    private String passwordConfirmation;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Registro> registros;

    @Override
    protected void onCreate() {
        super.onCreate();
        if (rol == null) {
            rol = RolUsuario.USUARIO;
        }
    }

    public String getPasswordConfirmation() {
        return passwordConfirmation;
    }
}