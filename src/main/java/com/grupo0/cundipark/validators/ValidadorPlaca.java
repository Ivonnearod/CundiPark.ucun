package com.grupo0.cundipark.validators;

public class ValidadorPlaca {
    
    public static boolean esValida(String placa) {
        if (placa == null || placa.trim().isEmpty()) {
            return false;
        }
        // Formato colombiano: ABC-1234 o ABC1234
        return placa.matches("^[A-Z]{3}[-]?\\d{4}$");
    }

    public static String formatear(String placa) {
        if (!esValida(placa)) {
            return null;
        }
        String limpia = placa.replaceAll("-", "").toUpperCase();
        return limpia.substring(0, 3) + "-" + limpia.substring(3);
    }
}
