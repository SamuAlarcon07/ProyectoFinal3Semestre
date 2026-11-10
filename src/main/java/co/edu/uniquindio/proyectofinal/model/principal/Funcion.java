package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;
import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Representa una función cinematográfica programada. */
public class Funcion {

    private final String codigo;
    private final Pelicula pelicula;
    private final Sala sala;
    private LocalDateTime fechaHora;
    private BigDecimal precioBase;
    private final FormatoProyeccion formatoProyeccion;
    private EstadoFuncion estado;

    public Funcion(String codigo, Pelicula pelicula, Sala sala,
                   LocalDateTime fechaHora, BigDecimal precioBase,
                   FormatoProyeccion formatoProyeccion) {
        this.codigo = validarTexto(codigo, "El código");
        this.pelicula = Objects.requireNonNull(pelicula, "La película es obligatoria.");
        this.sala = Objects.requireNonNull(sala, "La sala es obligatoria.");
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora son obligatorias.");
        this.formatoProyeccion = Objects.requireNonNull(formatoProyeccion,
                "El formato de proyección es obligatorio.");
        validarPrecio(precioBase);
        this.precioBase = precioBase;
        validarCompatibilidad();
        if (!pelicula.estaActiva()) {
            throw new IllegalArgumentException("No se puede programar una película inactiva.");
        }
        this.estado = EstadoFuncion.PROGRAMADA;
    }

    /** Comprueba que la sala admita el formato de la función. */
    public final void validarCompatibilidad() {
        if (!sala.permiteFormato(formatoProyeccion)) {
            throw new IllegalArgumentException(
                    "La sala no es compatible con el formato de proyección.");
        }
    }

    public boolean puedeVenderEntradas(LocalDateTime instante) {
        return estado == EstadoFuncion.PROGRAMADA
                && instante != null && fechaHora.isAfter(instante);
    }

    public void actualizarHorario(LocalDateTime nuevaFechaHora) {
        this.fechaHora = Objects.requireNonNull(nuevaFechaHora,
                "La fecha y hora no pueden ser nulas.");
    }

    public void actualizarPrecioBase(BigDecimal nuevoPrecio) {
        validarPrecio(nuevoPrecio);
        this.precioBase = nuevoPrecio;
    }

    public void cancelar() {
        if (estado == EstadoFuncion.FINALIZADA) {
            throw new IllegalStateException("No se puede cancelar una función finalizada.");
        }
        estado = EstadoFuncion.CANCELADA;
    }

    public void finalizar() {
        if (estado != EstadoFuncion.PROGRAMADA) {
            throw new IllegalStateException("Solo se puede finalizar una función programada.");
        }
        estado = EstadoFuncion.FINALIZADA;
    }

    private static void validarPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio base debe ser mayor que cero.");
        }
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    public String getCodigo() { return codigo; }
    public Pelicula getPelicula() { return pelicula; }
    public Sala getSala() { return sala; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public BigDecimal getPrecioBase() { return precioBase; }
    public FormatoProyeccion getFormatoProyeccion() { return formatoProyeccion; }
    public EstadoFuncion getEstado() { return estado; }
}
