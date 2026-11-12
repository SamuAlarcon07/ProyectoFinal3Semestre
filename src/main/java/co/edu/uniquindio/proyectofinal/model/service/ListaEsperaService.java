package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoSolicitudEspera;
import co.edu.uniquindio.proyectofinal.model.principal.Cliente;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;
import co.edu.uniquindio.proyectofinal.model.principal.ListaEspera;
import co.edu.uniquindio.proyectofinal.model.principal.SolicitudEspera;

import java.time.LocalDateTime;
import java.util.*;

/** Administra las listas de espera y las ofertas de asientos liberados. */
public class ListaEsperaService {

    private final Map<String, ListaEspera> listasPorFuncion = new LinkedHashMap<>();

    public synchronized ListaEspera obtenerOCrearLista(Funcion funcion) {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        return listasPorFuncion.computeIfAbsent(
                funcion.getCodigo(),
                codigo -> new ListaEspera(UUID.randomUUID().toString(), funcion, LocalDateTime.now())
        );
    }

    public synchronized SolicitudEspera registrarSolicitud(
            Funcion funcion, Cliente cliente, int cantidad) {
        return registrarSolicitud(funcion, cliente, cantidad, LocalDateTime.now());
    }

    /** Permite registrar una solicitud con una fecha explícita, útil para pruebas. */
    public synchronized SolicitudEspera registrarSolicitud(
            Funcion funcion, Cliente cliente, int cantidad, LocalDateTime fechaSolicitud) {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        Objects.requireNonNull(fechaSolicitud, "La fecha de solicitud es obligatoria.");
        procesarOfertasVencidas(fechaSolicitud);

        ListaEspera lista = obtenerOCrearLista(funcion);
        SolicitudEspera solicitud = new SolicitudEspera(
                UUID.randomUUID().toString(), cliente, fechaSolicitud, cantidad);
        lista.agregarSolicitud(solicitud);
        return solicitud;
    }

    /**
     * Antes de ofrecer los asientos a la siguiente persona, marca como EXPIRADA
     * cualquier oferta cuyo plazo ya terminó.
     */
    public synchronized Optional<SolicitudEspera> notificarSiguiente(
            Funcion funcion, int asientosLiberados,
            int minutosParaAceptar, LocalDateTime ahora) {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(ahora, "La fecha de consulta es obligatoria.");

        procesarOfertasVencidas(ahora);

        if (asientosLiberados <= 0 || minutosParaAceptar <= 0) {
            return Optional.empty();
        }

        ListaEspera lista = listasPorFuncion.get(funcion.getCodigo());
        if (lista == null) {
            return Optional.empty();
        }

        Optional<SolicitudEspera> siguiente = lista.obtenerSiguienteSolicitud()
                .filter(solicitud -> solicitud.getCantidadAsientos() <= asientosLiberados);

        siguiente.ifPresent(solicitud ->
                solicitud.notificarDisponibilidad(ahora.plusMinutes(minutosParaAceptar)));
        return siguiente;
    }

    /**
     * Recorre todas las listas y expira las ofertas vencidas.
     * Devuelve el número de solicitudes que cambiaron a EXPIRADA.
     * Este método puede invocarse periódicamente desde la aplicación JavaFX.
     */
    public synchronized int procesarOfertasVencidas(LocalDateTime ahora) {
        Objects.requireNonNull(ahora, "La fecha de procesamiento es obligatoria.");
        int expiradas = 0;

        for (ListaEspera lista : listasPorFuncion.values()) {
            for (SolicitudEspera solicitud : lista.consultarSolicitudes()) {
                boolean estabaNotificada =
                        solicitud.getEstado() == EstadoSolicitudEspera.NOTIFICADA;
                solicitud.expirar(ahora);
                if (estabaNotificada
                        && solicitud.getEstado() == EstadoSolicitudEspera.EXPIRADA) {
                    expiradas++;
                }
            }
        }
        return expiradas;
    }

    public synchronized void atenderSolicitud(
            SolicitudEspera solicitud, LocalDateTime instante) {
        Objects.requireNonNull(solicitud, "La solicitud es obligatoria.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");
        solicitud.expirar(instante);
        solicitud.atender(instante);
    }

    public synchronized void cancelarSolicitud(SolicitudEspera solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud es obligatoria.");
        solicitud.cancelar();
    }

    public synchronized Optional<ListaEspera> buscarLista(Funcion funcion) {
        return funcion == null
                ? Optional.empty()
                : Optional.ofNullable(listasPorFuncion.get(funcion.getCodigo()));
    }

    /** Consulta solicitudes y actualiza primero el estado de las ofertas vencidas. */
    public synchronized List<SolicitudEspera> consultarSolicitudes(
            Funcion funcion, LocalDateTime ahora) {
        Objects.requireNonNull(ahora, "La fecha de consulta es obligatoria.");
        procesarOfertasVencidas(ahora);
        return buscarLista(funcion)
                .map(ListaEspera::consultarSolicitudes)
                .orElseGet(List::of);
    }
}
