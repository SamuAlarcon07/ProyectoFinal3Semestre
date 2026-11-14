package co.edu.uniquindio.proyectofinal.model.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatea fechas y horas de los registros sin mostrar fracciones de segundo.
 * El valor original almacenado no se modifica.
 */
public final class FechaHoraUtil {

    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    private FechaHoraUtil() {
        // Clase utilitaria.
    }

    /** Devuelve yyyy-MM-dd HH:mm:ss, o un guion cuando la fecha es nula. */
    public static String formatear(LocalDateTime fechaHora) {
        return fechaHora == null ? "—" : FORMATO.format(fechaHora);
    }
}
