package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoSolicitudEspera;

import java.time.LocalDateTime;
import java.util.Objects;

/** Representa la solicitud de un cliente en una lista de espera. */
public class SolicitudEspera {

    private final String codigo;
    private final Cliente cliente;
    private final LocalDateTime fechaSolicitud;
    private final int cantidadAsientos;
    private EstadoSolicitudEspera estado;
    private LocalDateTime fechaExpiracionOferta;

    public SolicitudEspera(String codigo, Cliente cliente,
                           LocalDateTime fechaSolicitud, int cantidadAsientos) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código es obligatorio.");
        }
        this.codigo = codigo.trim();
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        this.fechaSolicitud = Objects.requireNonNull(fechaSolicitud,
                "La fecha de solicitud es obligatoria.");
        if (cantidadAsientos <= 0) {
            throw new IllegalArgumentException("La cantidad de asientos debe ser positiva.");
        }
        this.cantidadAsientos = cantidadAsientos;
        this.estado = EstadoSolicitudEspera.ACTIVA;
    }

    /** Cambia la solicitud a notificada y establece el límite para aceptar la oferta. */
    public void notificarDisponibilidad(LocalDateTime fechaExpiracionOferta) {
        if (estado != EstadoSolicitudEspera.ACTIVA) {
            throw new IllegalStateException("Solo se puede notificar una solicitud activa.");
        }
        Objects.requireNonNull(fechaExpiracionOferta,
                "La fecha de expiración de la oferta es obligatoria.");
        if (!fechaExpiracionOferta.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("La expiración debe estar en el futuro.");
        }
        this.fechaExpiracionOferta = fechaExpiracionOferta;
        this.estado = EstadoSolicitudEspera.NOTIFICADA;
    }

    public void atender(LocalDateTime instante) {
        if (estado != EstadoSolicitudEspera.NOTIFICADA) {
            throw new IllegalStateException("La solicitud no tiene una oferta pendiente.");
        }
        if (instante == null || !instante.isBefore(fechaExpiracionOferta)) {
            expirar(instante);
            throw new IllegalStateException("La oferta de disponibilidad expiró.");
        }
        estado = EstadoSolicitudEspera.ATENDIDA;
    }

    public void expirar(LocalDateTime instante) {
        if (estado == EstadoSolicitudEspera.NOTIFICADA
                && instante != null && !instante.isBefore(fechaExpiracionOferta)) {
            estado = EstadoSolicitudEspera.EXPIRADA;
        }
    }

    public void cancelar() {
        if (estado == EstadoSolicitudEspera.ACTIVA
                || estado == EstadoSolicitudEspera.NOTIFICADA) {
            estado = EstadoSolicitudEspera.CANCELADA;
        }
    }

    public String getCodigo() { return codigo; }
    public Cliente getCliente() { return cliente; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; }
    public int getCantidadAsientos() { return cantidadAsientos; }
    public EstadoSolicitudEspera getEstado() { return estado; }
    public LocalDateTime getFechaExpiracionOferta() { return fechaExpiracionOferta; }
}
