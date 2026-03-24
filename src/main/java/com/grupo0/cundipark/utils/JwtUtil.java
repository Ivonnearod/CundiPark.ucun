package com.grupo0.cundipark.utils;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class JwtUtil {
    
    public String generateToken(String email) {
        // Implementación básica para evitar errores de compilación si faltan dependencias JWT.
        // En producción, usar librerías como jjwt o java-jwt.
        return "dummy-token-" + UUID.randomUUID().toString() + "-" + email;
    }

    public String extractUsername(String token) {
        // Extrae el email asumiendo el formato "dummy-token-UUID-email"
        if (token != null && token.startsWith("dummy-token-")) {
            // "dummy-token-" (12) + UUID (36) + "-" (1) = 49 caracteres de prefijo
            if (token.length() > 49) {
                return token.substring(49);
            }
        }
        return null; 
    }

    public boolean validateToken(String token, String user) {
        if (token == null || user == null) return false;
        String usernameInToken = extractUsername(token);
        return usernameInToken != null && usernameInToken.equals(user);
    }
}