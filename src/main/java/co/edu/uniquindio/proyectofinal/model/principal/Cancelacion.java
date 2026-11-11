package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoCancelacion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/** Representa la solicitud y el resultado de cancelar una compra. */
public class Cancelacion {

    private final String codigo;
    private final Compra compra;
    private final LocalDateTime fecha;
    private final String motivo;
    private EstadoCancelacion estado;
    private BigDecimal porcentajeReembolso;

    public Cancelacion(String codigo, Compra compra, String motivo) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        this.codigo = codigo.trim();
        this.compra = Objects.requireNonNull(compra, "La compra es obligatoria.");
        if (motivo == null || motivo.isBlank()) throw new IllegalArgumentException("El motivo es obligatorio.");
        this.motivo = motivo.trim();
        this.fecha = LocalDateTime.now();
        this.estado = EstadoCancelacion.SOLICITADA;
        this.porcentajeReembolso = BigDecimal.ZERO;
    }

    /** Aprueba la cancelación e indica el porcentaje autorizado para devolver. */
    public void aprobar(BigDecimal porcentaje) {
        if (estado != EstadoCancelacion.SOLICITADA) throw new IllegalStateException("Solo se puede aprobar una solicitud pendiente.");
        if (porcentaje == null || porcentaje.compareTo(BigDecimal.ZERO) < 0
                || porcentaje.compareTo(new BigDecimal("100")) > 0) throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 100.");
        this.porcentajeReembolso = porcentaje;
        this.estado = EstadoCancelacion.APROBADA;
    }

    public void rechazar() {
        if (estado != EstadoCancelacion.SOLICITADA) throw new IllegalStateException("Solo se puede rechazar una solicitud pendiente.");
        estado = EstadoCancelacion.RECHAZADA;
    }

    public BigDecimal calcularValorReembolso() {
        if (estado != EstadoCancelacion.APROBADA && estado != EstadoCancelacion.PROCESADA)
            throw new IllegalStateException("La cancelación debe estar aprobada para calcular el reembolso.");
        return compra.getTotal().multiply(porcentajeReembolso)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public void marcarProcesada() {
        if (estado != EstadoCancelacion.APROBADA) throw new IllegalStateException("La cancelación debe estar aprobada.");
        estado = EstadoCancelacion.PROCESADA;
    }

    public String getCodigo() { return codigo; }
    public Compra getCompra() { return compra; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public EstadoCancelacion getEstado() { return estado; }
    public BigDecimal getPorcentajeReembolso() { return porcentajeReembolso; }
}