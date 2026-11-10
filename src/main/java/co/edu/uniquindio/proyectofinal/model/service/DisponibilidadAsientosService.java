package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoReserva;

import co.edu.uniquindio.proyectofinal.model.principal.Asiento;

import co.edu.uniquindio.proyectofinal.model.principal.Entrada;

import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import co.edu.uniquindio.proyectofinal.model.principal.ReservaAsiento;



import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.List;

import java.util.Objects;



/** Consulta y controla la disponibilidad de asientos por función. */

public class DisponibilidadAsientosService {



    private final List<ReservaAsiento> reservas = new ArrayList<>();

    private final List<Entrada> entradas = new ArrayList<>();



    public List<Asiento> consultarAsientosDisponibles(Funcion funcion, LocalDateTime instante) {

        Objects.requireNonNull(funcion);

        Objects.requireNonNull(instante);

        return funcion.getSala().getAsientos().stream()

                .filter(Asiento::isActivo)

                .filter(asiento -> estaDisponible(funcion, asiento, instante))

                .toList();

    }



    public boolean estaDisponible(Funcion funcion, Asiento asiento, LocalDateTime instante) {

        Objects.requireNonNull(funcion);

        Objects.requireNonNull(asiento);

        Objects.requireNonNull(instante);

        if (!funcion.getSala().contieneAsiento(asiento) || !asiento.isActivo()) return false;



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



    public void registrarReserva(ReservaAsiento reserva, LocalDateTime instante) {

        Objects.requireNonNull(reserva);

        if (!estaDisponible(reserva.getFuncion(), reserva.getAsiento(), instante)) {

            throw new IllegalStateException("El asiento ya no está disponible para esta función.");

        }

        reservas.add(reserva);

    }



    public void registrarEntrada(Entrada entrada) {

        Objects.requireNonNull(entrada);

        if (entradas.stream().anyMatch(actual -> actual.getCodigo().equals(entrada.getCodigo()))) {

            throw new IllegalArgumentException("La entrada ya está registrada.");

        }

        entradas.add(entrada);

    }



    public int expirarReservas(LocalDateTime instante) {

        Objects.requireNonNull(instante);

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



    private boolean mismoAsiento(Asiento primero, Asiento segundo) {

        return primero.getFila().equalsIgnoreCase(segundo.getFila())

                && primero.getNumero() == segundo.getNumero();

    }

}