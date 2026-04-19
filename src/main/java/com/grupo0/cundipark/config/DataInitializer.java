package com.grupo0.cundipark.config;

import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.services.BloqueService;
import com.grupo0.cundipark.services.RegistroService;
import com.grupo0.cundipark.services.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.Arrays;

/**
 * Componente para cargar datos iniciales de prueba en la base de datos.
 * Registra 2 usuarios y asigna un vehículo activo a cada uno.
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserService userService, RegistroService registroService, BloqueService bloqueService) {
        return args -> {
            try {
                System.out.println("🚀 Iniciando Seeding de datos...");

                // 1. Inicialización de Bloques (Consolidado)
                if (bloqueService.getAllBloques().isEmpty()) {
                    Bloque b1 = new Bloque(); b1.setNombre("Bloque A (Carros)"); b1.setCapacidad(20); b1.setDisponibles(20); b1.setActivo(true);
                    Bloque b2 = new Bloque(); b2.setNombre("Bloque B (Carros)"); b2.setCapacidad(20); b2.setDisponibles(20); b2.setActivo(true);
                    Bloque b3 = new Bloque(); b3.setNombre("Bloque C (Motos)"); b3.setCapacidad(40); b3.setDisponibles(40); b3.setActivo(true);
                    Bloque b4 = new Bloque(); b4.setNombre("Bloque D (VIP)"); b4.setCapacidad(10); b4.setDisponibles(10); b4.setActivo(true);
                    
                    for(Bloque b : Arrays.asList(b1, b2, b3, b4)) bloqueService.saveBloque(b);
                    System.out.println("✅ Bloques iniciales creados.");
                }
                
                Bloque bloque = bloqueService.getAllBloques().get(0);
                LocalDate fechaVencimiento = LocalDate.of(2025, 12, 31);

                // 2. Usuarios Administrativos Base
                if (userService.getUserByEmail("admin@udec.edu.co") == null) {
                    User admin = new User();
                    admin.setNombre("Administrador");
                    admin.setEmail("admin@udec.edu.co");
                    admin.setPassword("Admin123");
                    admin.setRol(RolUsuario.ADMIN);
                    admin.setActivo(true);
                    userService.registerUser(admin);
                    System.out.println("✅ Usuario Administrador creado.");
                }

                if (userService.getUserByEmail("superadmin@udec.edu.co") == null) {
                    User sa = new User();
                    sa.setNombre("Super Administrador");
                    sa.setEmail("superadmin@udec.edu.co");
                    sa.setPassword("Super123");
                    sa.setRol(RolUsuario.SUPERADMIN);
                    sa.setActivo(true);
                    userService.registerUser(sa);
                    System.out.println("✅ Usuario SuperAdmin creado.");
                }

                // 2. Juan Pérez
                String email1 = "juan.perez@udec.edu.co";
                if (userService.getUserByEmail(email1) == null) {
                    User user1 = new User();
                    user1.setNombre("Juan Pérez");
                    user1.setEmail(email1);
                    user1.setPassword("Cundipark2024*");
                    user1.setRol(RolUsuario.USER);
                    user1.setActivo(true);
                    
                    User savedUser1 = userService.registerUser(user1);
                    System.out.println("✅ Usuario Juan registrado.");
                    
                    registroService.procesarEntradaCompleta(
                        "ABC-123", "Toyota", "Corolla", "Gris",
                        fechaVencimiento, fechaVencimiento,
                        savedUser1.getId(), bloque.getId()
                    );
                    System.out.println("✅ Vehículo Juan registrado.");
                }

                // 3. María López
                String email2 = "maria.lopez@udec.edu.co";
                if (userService.getUserByEmail(email2) == null) {
                    User user2 = new User();
                    user2.setNombre("María López");
                    user2.setEmail(email2);
                    user2.setPassword("Cundipark2024*");
                    user2.setRol(RolUsuario.USER);
                    user2.setActivo(true);
                    
                    User savedUser2 = userService.registerUser(user2);
                    System.out.println("✅ Usuario María registrado.");
                    
                    registroService.procesarEntradaCompleta(
                        "XYZ-789", "Mazda", "3", "Rojo",
                        fechaVencimiento, fechaVencimiento,
                        savedUser2.getId(), bloque.getId()
                    );
                    System.out.println("✅ Vehículo María registrado.");
                }
                
                System.out.println("✅ SEEDING COMPLETADO EXITOSAMENTE.");

            } catch (Exception e) {
                System.err.println("❌ ERROR DURANTE EL SEEDING: " + e.getMessage());
                if (e.getCause() != null) {
                    System.err.println("🔍 CAUSA RAÍZ: " + e.getCause().getMessage());
                }
                e.printStackTrace();
            }
        };
    }
}