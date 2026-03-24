package com.grupo0.cundipark.validators;

import java.util.regex.Pattern;

public class ValidadorContrasena {
    
    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 100;
    private static final Pattern CONTIENE_MAYUSCULA = Pattern.compile(".*[A-Z].*");
    private static final Pattern CONTIENE_MINUSCULA = Pattern.compile(".*[a-z].*");
    private static final Pattern CONTIENE_NUMERO = Pattern.compile(".*\\d.*");

    public static boolean esValida(String contrasena) {
        if (contrasena == null) {
            return false;
        }
        
        if (contrasena.length() < MIN_LENGTH || contrasena.length() > MAX_LENGTH) {
            return false;
        }
        
        // Al menos una mayúscula, una minúscula y un número
        boolean tieneMayuscula = CONTIENE_MAYUSCULA.matcher(contrasena).matches();
        boolean tieneMinuscula = CONTIENE_MINUSCULA.matcher(contrasena).matches();
        boolean tieneNumero = CONTIENE_NUMERO.matcher(contrasena).matches();
        
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
        if (!CONTIENE_MAYUSCULA.matcher(contrasena).matches()) {
            errores.add("Debe contener al menos una mayúscula");
        }
        if (!CONTIENE_MINUSCULA.matcher(contrasena).matches()) {
            errores.add("Debe contener al menos una minúscula");
        }
        if (!CONTIENE_NUMERO.matcher(contrasena).matches()) {
            errores.add("Debe contener al menos un número");
        }
        return errores;
    }
}
