package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;
import co.edu.uniquindio.proyectofinal.model.enums.EstadoReserva;
import co.edu.uniquindio.proyectofinal.model.principal.Asiento;
import co.edu.uniquindio.proyectofinal.model.principal.Entrada;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;
import co.edu.uniquindio.proyectofinal.model.principal.ReservaAsiento;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Consulta y controla la disponibilidad de asientos por función. */
public class DisponibilidadAsientosService {

    private final List<ReservaAsiento> reservas = new ArrayList<>();
    private final List<Entrada> entradas = new ArrayList<>();

    public synchronized List<Asiento> consultarAsientosDisponibles(
            Funcion funcion, LocalDateTime instante) {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");

        return funcion.getSala().getAsientos().stream()
                .filter(Asiento::isActivo)
                .filter(asiento -> estaDisponible(funcion, asiento, instante))
                .toList();
    }

    public synchronized boolean estaDisponible(
            Funcion funcion, Asiento asiento, LocalDateTime instante) {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(asiento, "El asiento es obligatorio.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");

        if (!funcion.getSala().contieneAsiento(asiento) || !asiento.isActivo()) {
            return false;
        }

        boolean tieneReserva = reservas.stream().anyMatch(reserva ->
                reserva.getFuncion().getCodigo().equals(funcion.getCodigo())
                        && mismoAsiento(reserva.getAsiento(), asiento)
                        && (reserva.getEstado() == EstadoReserva.CONFIRMADA
                        || reserva.estaVigente(instante)));

        boolean tieneEntrada = entradas.stream().anyMatch(entrada ->
                entrada.getFuncion().getCodigo().equals(funcion.getCodigo())
                        && mismoAsiento(entrada.getAsiento(), asiento)
                        && entrada.getEstado() != EstadoEntrada.ANULADA);

        return !tieneReserva && !tieneEntrada;
    }

    public synchronized void registrarReserva(ReservaAsiento reserva,
                                              LocalDateTime instante) {
        Objects.requireNonNull(reserva, "La reserva es obligatoria.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");

        if (!estaDisponible(reserva.getFuncion(), reserva.getAsiento(), instante)) {
            throw new IllegalStateException(
                    "El asiento ya no está disponible para esta función.");
        }
        reservas.add(reserva);
    }

    /** Compatibilidad con el registro individual anterior. */
    public synchronized void registrarEntrada(Entrada entrada) {
        registrarEntradas(List.of(Objects.requireNonNull(entrada)), LocalDateTime.now());
    }

    /**
     * Valida un grupo completo de entradas y lo registra de forma conjunta.
     * Si una entrada falla, ninguna de las entradas del grupo se agrega.
     */
    public synchronized void registrarEntradas(Collection<Entrada> nuevasEntradas,
                                               LocalDateTime instante) {
        Objects.requireNonNull(nuevasEntradas, "La colección de entradas es obligatoria.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");

        List<Entrada> copia = List.copyOf(nuevasEntradas);
        Set<String> codigos = new HashSet<>();
        Set<String> asientosDelGrupo = new HashSet<>();

        for (Entrada entrada : copia) {
            Objects.requireNonNull(entrada, "No se permiten entradas nulas.");

            if (!codigos.add(entrada.getCodigo())) {
                throw new IllegalArgumentException(
                        "El grupo contiene códigos de entrada duplicados.");
            }
            if (entradas.stream().anyMatch(actual ->
                    actual.getCodigo().equals(entrada.getCodigo()))) {
                throw new IllegalArgumentException(
                        "La entrada ya está registrada.");
            }
            if (entrada.getEstado() != EstadoEntrada.EMITIDA) {
                throw new IllegalStateException(
                        "Solo se pueden registrar entradas emitidas.");
            }
            if (!entrada.getFuncion().puedeVenderEntradas(instante)) {
                throw new IllegalStateException(
                        "La función no está disponible para la venta.");
            }

            String identificadorAsiento = identificadorAsiento(entrada);
            if (!asientosDelGrupo.add(identificadorAsiento)) {
                throw new IllegalArgumentException(
                        "El grupo intenta registrar dos entradas para el mismo asiento.");
            }
            if (!estaDisponible(entrada.getFuncion(), entrada.getAsiento(), instante)) {
                throw new IllegalStateException(
                        "El asiento ya no está disponible para esta función.");
            }
        }

        entradas.addAll(copia);
    }

    /**
     * Revierte únicamente las instancias que acaba de registrar una operación.
     * Se usa como compensación si el pago de una compra no llega a completarse.
     */
    public synchronized void eliminarEntradasRegistradas(Collection<Entrada> entradasAEliminar) {
        Objects.requireNonNull(entradasAEliminar,
                "La colección de entradas es obligatoria.");
        Set<Entrada> referencias = java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>());
        referencias.addAll(entradasAEliminar);
        entradas.removeIf(referencias::contains);
    }

    public synchronized int expirarReservas(LocalDateTime instante) {
        Objects.requireNonNull(instante, "El instante es obligatorio.");
        int expiradas = 0;
        for (ReservaAsiento reserva : reservas) {
            if (reserva.getEstado() == EstadoReserva.ACTIVA
                    && !reserva.estaVigente(instante)) {
                reserva.expirar(instante);
                expiradas++;
            }
        }
        return expiradas;
    }

    private String identificadorAsiento(Entrada entrada) {
        return entrada.getFuncion().getCodigo()
                + "|" + entrada.getAsiento().getFila().toUpperCase()
                + "|" + entrada.getAsiento().getNumero();
    }

    private boolean mismoAsiento(Asiento primero, Asiento segundo) {
        return primero.getFila().equalsIgnoreCase(segundo.getFila())
                && primero.getNumero() == segundo.getNumero();
    }
}
