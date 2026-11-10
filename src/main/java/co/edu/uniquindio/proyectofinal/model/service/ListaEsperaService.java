package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.principal.Cliente;

import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import co.edu.uniquindio.proyectofinal.model.principal.ListaEspera;

import co.edu.uniquindio.proyectofinal.model.principal.SolicitudEspera;



import java.time.LocalDateTime;

import java.util.LinkedHashMap;

import java.util.Map;

import java.util.Optional;

import java.util.UUID;



/** Administra las listas de espera y las ofertas de asientos liberados. */

public class ListaEsperaService {



    private final Map<String, ListaEspera> listasPorFuncion = new LinkedHashMap<>();



    public ListaEspera obtenerOCrearLista(Funcion funcion) {

        if (funcion == null) throw new IllegalArgumentException("La función es obligatoria.");

        return listasPorFuncion.computeIfAbsent(funcion.getCodigo(),

                codigo -> new ListaEspera(UUID.randomUUID().toString(), funcion, LocalDateTime.now()));

    }



    public SolicitudEspera registrarSolicitud(Funcion funcion, Cliente cliente, int cantidad) {

        ListaEspera lista = obtenerOCrearLista(funcion);

        SolicitudEspera solicitud = new SolicitudEspera(UUID.randomUUID().toString(),

                cliente, LocalDateTime.now(), cantidad);

        lista.agregarSolicitud(solicitud);

        return solicitud;

    }



    public Optional<SolicitudEspera> notificarSiguiente(Funcion funcion,

                                                        int asientosLiberados, int minutosParaAceptar, LocalDateTime ahora) {

        if (funcion == null || ahora == null) throw new IllegalArgumentException("La función y la fecha son obligatorias.");

        if (asientosLiberados <= 0 || minutosParaAceptar <= 0) return Optional.empty();

        ListaEspera lista = listasPorFuncion.get(funcion.getCodigo());

        if (lista == null) return Optional.empty();

        Optional<SolicitudEspera> siguiente = lista.obtenerSiguienteSolicitud()

                .filter(s -> s.getCantidadAsientos() <= asientosLiberados);

        siguiente.ifPresent(s -> s.notificarDisponibilidad(ahora.plusMinutes(minutosParaAceptar)));

        return siguiente;

    }



    public void atenderSolicitud(SolicitudEspera solicitud, LocalDateTime instante) {

        if (solicitud == null) throw new IllegalArgumentException("La solicitud es obligatoria.");

        solicitud.atender(instante);

    }



    public void cancelarSolicitud(SolicitudEspera solicitud) {

        if (solicitud == null) throw new IllegalArgumentException("La solicitud es obligatoria.");

        solicitud.cancelar();

    }



    public Optional<ListaEspera> buscarLista(Funcion funcion) {

        return funcion == null ? Optional.empty() : Optional.ofNullable(listasPorFuncion.get(funcion.getCodigo()));

    }

}

