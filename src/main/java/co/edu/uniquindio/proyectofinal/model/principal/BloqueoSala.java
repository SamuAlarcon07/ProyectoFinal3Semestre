package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoBloqueoSala;
import co.edu.uniquindio.proyectofinal.model.enums.TipoBloqueoSala;

import java.time.LocalDateTime;
import java.util.Objects;

/** Representa un bloqueo temporal de una sala de CinemaUQ. */
public class BloqueoSala {

    private final String codigo;
    private final Sala sala;
    private final TipoBloqueoSala tipoBloqueo;
    private final LocalDateTime fechaInicio;
    private final LocalDateTime fechaFin;
    private final String motivo;
    private final Administrador administrador;
    private EstadoBloqueoSala estado;

    public BloqueoSala(String codigo, Sala sala, TipoBloqueoSala tipoBloqueo,
                       LocalDateTime fechaInicio, LocalDateTime fechaFin,
                       String motivo, Administrador administrador) {
        this.codigo = validarTexto(codigo, "El código");
        this.sala = Objects.requireNonNull(sala, "La sala es obligatoria.");
        this.tipoBloqueo = Objects.requireNonNull(tipoBloqueo, "El tipo de bloqueo es obligatorio.");
        this.fechaInicio = Objects.requireNonNull(fechaInicio, "La fecha inicial es obligatoria.");
        this.fechaFin = Objects.requireNonNull(fechaFin, "La fecha final es obligatoria.");
        if (!fechaFin.isAfter(fechaInicio)) {
            throw new IllegalArgumentException("La fecha final debe ser posterior a la inicial.");
        }
        this.motivo = validarTexto(motivo, "El motivo");
        this.administrador = Objects.requireNonNull(administrador,
                "Debe identificarse el administrador responsable.");
        this.estado = EstadoBloqueoSala.PROGRAMADO;
    }

    /** Indica si el bloqueo cubre el instante indicado. */
    public boolean estaVigente(LocalDateTime instante) {
        return instante != null
                && (estado == EstadoBloqueoSala.PROGRAMADO
                || estado == EstadoBloqueoSala.EN_CURSO)
                && !instante.isBefore(fechaInicio)
                && instante.isBefore(fechaFin);
    }

    /** Comprueba si el intervalo del bloqueo se cruza con otro intervalo. */
    public boolean seSuperponeCon(LocalDateTime inicio, LocalDateTime fin) {
        Objects.requireNonNull(inicio, "El inicio no puede ser nulo.");
        Objects.requireNonNull(fin, "El fin no puede ser nulo.");
        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException("El intervalo consultado no es válido.");
        }
        return (estado == EstadoBloqueoSala.PROGRAMADO
                || estado == EstadoBloqueoSala.EN_CURSO)
                && inicio.isBefore(fechaFin) && fin.isAfter(fechaInicio);
    }

    public void iniciar() {
        if (estado != EstadoBloqueoSala.PROGRAMADO) {
            throw new IllegalStateException("Solo se puede iniciar un bloqueo programado.");
        }
        estado = EstadoBloqueoSala.EN_CURSO;
    }

    public void finalizar() {
        if (estado != EstadoBloqueoSala.EN_CURSO) {
            throw new IllegalStateException("Solo se puede finalizar un bloqueo en curso.");
        }
        estado = EstadoBloqueoSala.FINALIZADO;
    }

    public void cancelar() {
        if (estado == EstadoBloqueoSala.FINALIZADO) {
            throw new IllegalStateException("No se puede cancelar un bloqueo finalizado.");
        }
        estado = EstadoBloqueoSala.CANCELADO;
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    public String getCodigo() { return codigo; }
    public Sala getSala() { return sala; }
    public TipoBloqueoSala getTipoBloqueo() { return tipoBloqueo; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public String getMotivo() { return motivo; }
    public Administrador getAdministrador() { return administrador; }
    public EstadoBloqueoSala getEstado() { return estado; }
}
