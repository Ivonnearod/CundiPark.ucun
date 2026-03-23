package com.grupo0.cundipark.utils;

public class StringUtil {

    public static boolean esVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    public static String limpiar(String texto) {
        return texto != null ? texto.trim() : "";
    }

    public static String capitalizar(String texto) {
        if (esVacio(texto)) return "";
        return texto.substring(0, 1).toUpperCase() + texto.substring(1).toLowerCase();
    }

    public static String removerEspacios(String texto) {
        return esVacio(texto) ? "" : texto.replaceAll("\\s+", "");
    }

    public static String generarId() {
        return System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
}
