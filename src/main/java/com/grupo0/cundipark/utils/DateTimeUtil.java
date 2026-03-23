package com.grupo0.cundipark.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    
    public static final DateTimeFormatter FORMATO_COMPLETO = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    
    public static final DateTimeFormatter FORMATO_FECHA = 
        DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    public static final DateTimeFormatter FORMATO_HORA = 
        DateTimeFormatter.ofPattern("HH:mm:ss");

    public static String formatearFecha(LocalDateTime fecha) {
        return fecha != null ? fecha.format(FORMATO_COMPLETO) : "";
    }

    public static String formatearFechaSolo(LocalDateTime fecha) {
        return fecha != null ? fecha.format(FORMATO_FECHA) : "";
    }

    public static String formatearHoraSolo(LocalDateTime fecha) {
        return fecha != null ? fecha.format(FORMATO_HORA) : "";
    }

    public static boolean estaDentroDelRango(LocalDateTime fecha, LocalDateTime inicio, LocalDateTime fin) {
        if (fecha == null) return false;
        if (inicio != null && fecha.isBefore(inicio)) return false;
        if (fin != null && fecha.isAfter(fin)) return false;
        return true;
    }
}
