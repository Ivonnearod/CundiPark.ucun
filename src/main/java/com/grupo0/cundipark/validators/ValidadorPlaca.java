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
        if (placa == null) {
            return null;
        }
        String limpia = placa.replaceAll("-", "").toUpperCase();
        return limpia.length() == 6 ? limpia.substring(0, 3) + "-" + limpia.substring(3) : null;
    }
}
