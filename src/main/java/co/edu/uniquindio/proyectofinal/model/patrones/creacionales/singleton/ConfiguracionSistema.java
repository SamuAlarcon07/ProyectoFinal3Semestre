package co.edu.uniquindio.proyectofinal.model.patrones.creacionales.singleton;

import java.util.Objects;

/**
 * Mantiene la configuración general compartida de CinemaUQ.
 * Implementa el patrón Singleton.
 */
public final class ConfiguracionSistema {

    private final String nombreAplicacion = "CinemaUQ";
    private volatile String idioma = "es-CO";
    private volatile String moneda = "COP";
    private volatile boolean modoMantenimiento;

    /**
     * Holder que crea la instancia única.
     */
    private static class InstanciaHolder {
        private static final ConfiguracionSistema INSTANCIA =
                new ConfiguracionSistema();
    }

    /**
     * Constructor privado para impedir instancias externas.
     */
    private ConfiguracionSistema() {
    }

    /**
     * Devuelve la única instancia de configuración.
     */
    public static ConfiguracionSistema getInstancia() {
        return InstanciaHolder.INSTANCIA;
    }

    public String getNombreAplicacion() {
        return nombreAplicacion;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = validarTexto(idioma, "El idioma");
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = validarTexto(moneda, "La moneda").toUpperCase();
    }

    public boolean isModoMantenimiento() {
        return modoMantenimiento;
    }

    public void activarModoMantenimiento() {
        this.modoMantenimiento = true;
    }

    public void desactivarModoMantenimiento() {
        this.modoMantenimiento = false;
    }

    private static String validarTexto(String valor, String campo) {
        Objects.requireNonNull(valor, campo + " no puede ser nulo.");

        if (valor.isBlank()) {
            throw new IllegalArgumentException(
                    campo + " no puede estar vacío."
            );
        }

        return valor.trim();
    }
}
