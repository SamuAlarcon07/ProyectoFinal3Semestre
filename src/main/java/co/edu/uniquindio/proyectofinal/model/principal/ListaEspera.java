package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoSolicitudEspera;

import java.time.LocalDateTime;
import java.util.*;

/** Agrupa las solicitudes de espera asociadas a una función. */
public class ListaEspera {

    private final String codigo;
    private final Funcion funcion;
    private final LocalDateTime fechaCreacion;
    private final List<SolicitudEspera> solicitudes;

    public ListaEspera(String codigo, Funcion funcion, LocalDateTime fechaCreacion) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }
        this.codigo = codigo.trim();
        this.funcion = Objects.requireNonNull(funcion, "La función es obligatoria.");
        this.fechaCreacion = Objects.requireNonNull(fechaCreacion,
                "La fecha de creación es obligatoria.");
        this.solicitudes = new ArrayList<>();
    }

    /** Agrega una solicitud y evita duplicar solicitudes activas del mismo cliente. */
    public void agregarSolicitud(SolicitudEspera solicitud) {
        Objects.requireNonNull(solicitud, "La solicitud no puede ser nula.");
        boolean duplicada = solicitudes.stream().anyMatch(actual ->
                actual.getCliente().getId().equals(solicitud.getCliente().getId())
                        && (actual.getEstado() == EstadoSolicitudEspera.ACTIVA
                        || actual.getEstado() == EstadoSolicitudEspera.NOTIFICADA));
        if (duplicada) {
            throw new IllegalArgumentException("El cliente ya tiene una solicitud activa para esta función.");
        }
        solicitudes.add(solicitud);
    }

    /** Devuelve la solicitud activa más antigua, si existe. */
    public Optional<SolicitudEspera> obtenerSiguienteSolicitud() {
        return solicitudes.stream()
                .filter(s -> s.getEstado() == EstadoSolicitudEspera.ACTIVA)
                .min(Comparator.comparing(SolicitudEspera::getFechaSolicitud));
    }

    public List<SolicitudEspera> consultarSolicitudes() {
        return List.copyOf(solicitudes);
    }

    public String getCodigo() { return codigo; }
    public Funcion getFuncion() { return funcion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}
