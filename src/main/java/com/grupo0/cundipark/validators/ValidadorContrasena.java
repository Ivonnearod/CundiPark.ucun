package com.grupo0.cundipark.validators;

public class ValidadorContrasena {
    
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 100;

    public static boolean esValida(String contrasena) {
        if (contrasena == null) {
            return false;
        }
        
        if (contrasena.length() < MIN_LENGTH || contrasena.length() > MAX_LENGTH) {
            return false;
        }
        
        // Al menos una mayúscula, una minúscula y un número
        boolean tieneMayuscula = contrasena.matches(".*[A-Z].*");
        boolean tieneMinuscula = contrasena.matches(".*[a-z].*");
        boolean tieneNumero = contrasena.matches(".*\\d.*");
        
        return tieneMayuscula && tieneMinuscula && tieneNumero;
    }

    public static java.util.List<String> obtenerErrores(String contrasena) {
        java.util.List<String> errores = new java.util.ArrayList<>();
        if (contrasena == null) {
            errores.add("Contraseña requerida");
            return errores;
        }
        if (contrasena.length() < MIN_LENGTH) {
            errores.add("Mínimo " + MIN_LENGTH + " caracteres requeridos");
        }
        if (contrasena.length() > MAX_LENGTH) {
            errores.add("Máximo " + MAX_LENGTH + " caracteres permitidos");
        }
        if (!contrasena.matches(".*[A-Z].*")) {
            errores.add("Debe contener al menos una mayúscula");
        }
        if (!contrasena.matches(".*[a-z].*")) {
            errores.add("Debe contener al menos una minúscula");
        }
        if (!contrasena.matches(".*\\d.*")) {
            errores.add("Debe contener al menos un número");
        }
        return errores;
    }
}
