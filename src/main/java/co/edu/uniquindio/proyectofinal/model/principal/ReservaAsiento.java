package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoReserva;

import java.time.LocalDateTime;
import java.util.Objects;

/** Representa una reserva temporal de un asiento para una función. */
public class ReservaAsiento {

    private final String codigo;
    private final Funcion funcion;
    private final Asiento asiento;
    private final Cliente cliente;
    private final LocalDateTime fechaReserva;
    private final LocalDateTime fechaExpiracion;
    private EstadoReserva estado;

    public ReservaAsiento(String codigo, Funcion funcion, Asiento asiento,
                          Cliente cliente, LocalDateTime fechaReserva,
                          LocalDateTime fechaExpiracion) {
        this.codigo = validarTexto(codigo, "El código");
        this.funcion = Objects.requireNonNull(funcion, "La función es obligatoria.");
        this.asiento = Objects.requireNonNull(asiento, "El asiento es obligatorio.");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        this.fechaReserva = Objects.requireNonNull(fechaReserva, "La fecha de reserva es obligatoria.");
        this.fechaExpiracion = Objects.requireNonNull(fechaExpiracion, "La fecha de expiración es obligatoria.");
        if (!fechaExpiracion.isAfter(fechaReserva)) {
            throw new IllegalArgumentException("La expiración debe ser posterior a la reserva.");
        }
        if (!funcion.getSala().contieneAsiento(asiento) || !asiento.isActivo()) {
            throw new IllegalArgumentException("El asiento no pertenece a la sala o está inactivo.");
        }
        this.estado = EstadoReserva.ACTIVA;
    }

    public boolean estaVigente(LocalDateTime instante) {
        return estado == EstadoReserva.ACTIVA
                && instante != null && instante.isBefore(fechaExpiracion);
    }

    public void confirmar(LocalDateTime instante) {
        if (!estaVigente(instante)) {
            throw new IllegalStateException("La reserva no está vigente.");
        }
        estado = EstadoReserva.CONFIRMADA;
    }

    public void cancelar() {
        if (estado == EstadoReserva.CONFIRMADA) {
            throw new IllegalStateException("Una reserva confirmada debe gestionarse mediante la compra.");
        }
        if (estado == EstadoReserva.ACTIVA) {
            estado = EstadoReserva.CANCELADA;
        }
    }

    public void expirar(LocalDateTime instante) {
        if (estado == EstadoReserva.ACTIVA && instante != null
                && !instante.isBefore(fechaExpiracion)) {
            estado = EstadoReserva.EXPIRADA;
        }
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    public String getCodigo() { return codigo; }
    public Funcion getFuncion() { return funcion; }
    public Asiento getAsiento() { return asiento; }
    public Cliente getCliente() { return cliente; }
    public LocalDateTime getFechaReserva() { return fechaReserva; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; }
    public EstadoReserva getEstado() { return estado; }
}
