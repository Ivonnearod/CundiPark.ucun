package com.grupo0.cundipark.validators;

public class ValidadorEmail {
    
    public static boolean esValido(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static String normalizar(String email) {
        if (!esValido(email)) {
            return null;
        }
        return email.toLowerCase().trim();
    }
}
