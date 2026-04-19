package com.grupo0.cundipark.validators;

public class ValidadorPlaca {
    
    public static boolean esValida(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            return false;
        }
        // Formato colombiano para carro: ABC-123 o ABC123
        return placa.toUpperCase().matches("^[A-Z]{3}[-]?\\d{3}$");
    }

    public static String formatear(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            throw new IllegalArgumentException("La placa no puede estar vacía.");
        }
        String limpia = placa.replaceAll("-", "").toUpperCase().trim();
        if (limpia.length() != 6) {
            throw new IllegalArgumentException("La placa debe tener exactamente 6 caracteres.");
        }
        if (!limpia.matches("^[A-Z]{3}[0-9]{3}$")) {
            throw new IllegalArgumentException("El formato de la placa debe ser 3 letras y 3 números (Ej: ABC-123).");
        }
        return limpia.substring(0, 3) + "-" + limpia.substring(3);
    }
}
