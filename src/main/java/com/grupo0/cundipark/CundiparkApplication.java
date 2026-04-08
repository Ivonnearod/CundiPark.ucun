package com.grupo0.cundipark;

import com.grupo0.cundipark.models.Bloque;
import com.grupo0.cundipark.models.User;
import com.grupo0.cundipark.models.RolUsuario;
import com.grupo0.cundipark.repositories.BloqueRepository;
import com.grupo0.cundipark.repositories.UserRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class CundiparkApplication {

	public static void main(String[] args) {
		SpringApplication.run(CundiparkApplication.class, args);
	}

	@Bean
	public CommandLineRunner iniciarConsola(BloqueRepository bloqueRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
		return args -> {
			System.out.println("🔍 [SISTEMA] Iniciando limpieza total y sincronización de Administrador...");
			String adminEmail = "admin@udec.edu.co";
			String userEmail = "user@udec.edu.co";
			String superAdminEmail = "superadmin@udec.edu.co";

			// 1. Borrar todas las cuentas menos el administrador principal
			// Habilitamos la limpieza para que puedas registrar usuarios desde cero con el nuevo formulario
			// DESACTIVADO: Ahora permitimos cualquier correo y queremos que persistan en la DB
			/* 
			|userRepository.findAll().forEach(u -> {
				if (u.getEmail() == null || 
					(!u.getEmail().trim().equalsIgnoreCase(adminEmail) && !u.getEmail().trim().equalsIgnoreCase(userEmail))) {
					System.out.println("🗑️  Borrando cuenta: " + u.getEmail());
					userRepository.delete(u);
				}
			});
			*/
			

			// 2. Asegurar existencia del administrador principal
			String cleanAdminEmail = adminEmail.toLowerCase().trim();
			User adminUser = userRepository.findByEmail(cleanAdminEmail).orElse(null);

			if (adminUser == null) {
				System.out.println("👤 [SISTEMA] Creando nuevo Administrador...");
				User admin = new User();
				admin.setNombre("Administrador Principal");
				admin.setEmail(cleanAdminEmail);
				admin.setPassword(passwordEncoder.encode("Admin123"));
				admin.setRol(RolUsuario.ADMIN);
				admin.setActivo(true);
				userRepository.save(admin);
				System.out.println("✅ [SISTEMA] Administrador creado exitosamente.");
			} else {
				System.out.println("🆙 [SISTEMA] Sincronizando Administrador...");
				adminUser.setRol(RolUsuario.ADMIN);
				adminUser.setPassword(passwordEncoder.encode("Admin123"));
				userRepository.save(adminUser);
				System.out.println("✅ [SISTEMA] Administrador sincronizado y habilitado.");
			}

			// 2.5 Asegurar existencia del SuperAdministrador
			String cleanSuperEmail = superAdminEmail.toLowerCase().trim();
			User superAdmin = userRepository.findByEmail(cleanSuperEmail).orElse(null);
			if (superAdmin == null) {
				User sa = new User();
				sa.setNombre("Super Administrador");
				sa.setEmail(cleanSuperEmail);
				sa.setPassword(passwordEncoder.encode("Super123"));
				sa.setRol(RolUsuario.SUPERADMIN);
				sa.setActivo(true);
				userRepository.save(sa);
				System.out.println("✅ [SISTEMA] Super Administrador creado.");
			} else {
				System.out.println("🆙 [SISTEMA] Sincronizando Super Administrador...");
				superAdmin.setRol(RolUsuario.SUPERADMIN);
				superAdmin.setPassword(passwordEncoder.encode("Super123"));
				userRepository.save(superAdmin);
				System.out.println("✅ [SISTEMA] Super Administrador sincronizado.");
			}

			// 3. Asegurar existencia de un Usuario de prueba normal
			String cleanUserEmail = userEmail.toLowerCase().trim();
			User normalUser = userRepository.findByEmail(cleanUserEmail).orElse(null);
			if (normalUser == null) {
				System.out.println("👤 [SISTEMA] Creando nuevo Usuario de prueba...");
				User testUser = new User();
				testUser.setNombre("Usuario de Prueba");
				testUser.setEmail(cleanUserEmail);
				testUser.setPassword(passwordEncoder.encode("User123"));
				testUser.setRol(RolUsuario.USER);
				testUser.setActivo(true);
				userRepository.save(testUser);
				System.out.println("✅ [SISTEMA] Usuario de prueba creado.");
			} else {
				System.out.println("🆙 [SISTEMA] Sincronizando Usuario de prueba...");
				normalUser.setEmail(cleanUserEmail);
				normalUser.setRol(RolUsuario.USER);
				normalUser.setPassword(passwordEncoder.encode("User123"));
				normalUser.setActivo(true);
				userRepository.save(normalUser);
			}

			// Verificar si existen bloques, si no, crear datos semilla
			if (bloqueRepository.count() == 0) {
				System.out.println("⚙️  Inicializando base de datos con bloques por defecto...");
				
				Bloque b1 = new Bloque();
				b1.setNombre("Bloque A (Carros)");
				b1.setCapacidad(20);
				b1.setDisponibles(20);
				b1.setActivo(true);

				Bloque b2 = new Bloque();
				b2.setNombre("Bloque B (Carros)");
				b2.setCapacidad(20);
				b2.setDisponibles(20);
				b2.setActivo(true);

				Bloque b3 = new Bloque();
				b3.setNombre("Bloque C (Motos)");
				b3.setCapacidad(40);
				b3.setDisponibles(40);
				b3.setActivo(true);

				Bloque b4 = new Bloque();
				b4.setNombre("Bloque D (VIP)");
				b4.setCapacidad(10);
				b4.setDisponibles(10);
				b4.setActivo(true);

				bloqueRepository.saveAll(Arrays.asList(b1, b2, b3, b4));
				System.out.println("✅ Bloques creados exitosamente.");
			} else {
				System.out.println("ℹ️  La base de datos ya contiene bloques.");
			}

			System.out.println("\n----------------------------------------------------------");
			System.out.println("✅ APLICACIÓN CUNDIPARK INICIADA CORRECTAMENTE");
			System.out.println("🌐 Accede en: http://localhost:8080");
			System.out.println("----------------------------------------------------------\n");
		};
	}

}
