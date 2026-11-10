package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoAsiento;

import java.util.Objects;

/** Representa un asiento físico dentro de una sala. */
public class Asiento {

    private final String fila;
    private final int numero;
    private final TipoAsiento tipo;
    private boolean activo;

    public Asiento(String fila, int numero, TipoAsiento tipo) {
        if (fila == null || fila.isBlank()) {
            throw new IllegalArgumentException("La fila del asiento es obligatoria.");
        }
        if (numero <= 0) {
            throw new IllegalArgumentException("El número del asiento debe ser positivo.");
        }
        this.fila = fila.trim().toUpperCase();
        this.numero = numero;
        this.tipo = Objects.requireNonNull(tipo, "El tipo de asiento es obligatorio.");
        this.activo = true;
    }

    public String obtenerIdentificador() { return fila + numero; }
    public boolean esPreferencial() { return tipo == TipoAsiento.PREFERENCIAL; }
    public void activar() { activo = true; }
    public void desactivar() { activo = false; }

    public String getFila() { return fila; }
    public int getNumero() { return numero; }
    public TipoAsiento getTipo() { return tipo; }
    public boolean isActivo() { return activo; }
}
