package com.grupo0.cundipark.config;

import com.grupo0.cundipark.services.UserService;
import com.grupo0.cundipark.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider()) // Vincular explícitamente el proveedor
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                        "/",
                        "/registration**",
                        "/js/**",
                        "/css/**",
                        "/img/**",
                        "/images/**",
                        "/error/**",
                        "/api/users/login", // Login de API público
                        "/login**").permitAll() 
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll() // Registro de API público
                .requestMatchers("/api/**").authenticated() // El resto de la API requiere token o sesión
                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
                .requestMatchers("/superuser/**").hasRole("SUPERADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .defaultSuccessUrl("/home", false) // Permitir redirección a la página solicitada originalmente
                .permitAll()
            )
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // Deshabilitar CSRF para API REST
            .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));

        // Registrar el filtro JWT para dar soporte a la API REST
        http.addFilterBefore(new JwtAuthenticationFilter(jwtUtil, userService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider auth = new DaoAuthenticationProvider();
        auth.setUserDetailsService(userService); // Conecta con tu servicio de usuarios
        auth.setPasswordEncoder(passwordEncoder());
        return auth;
    }
}