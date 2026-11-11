package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoReembolso;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Registra la devolución de dinero asociada a una cancelación aprobada. */
public class Reembolso {

    private final String codigo;
    private final Cancelacion cancelacion;
    private final BigDecimal valor;
    private final LocalDateTime fecha;
    private EstadoReembolso estado;
    private MovimientoTarjeta movimientoTarjeta;

    public Reembolso(String codigo, Cancelacion cancelacion, BigDecimal valor) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        this.codigo = codigo.trim();
        this.cancelacion = Objects.requireNonNull(cancelacion, "La cancelación es obligatoria.");
        if (cancelacion.getEstado() != co.edu.uniquindio.proyectofinal.model.enums.EstadoCancelacion.APROBADA)
            throw new IllegalArgumentException("La cancelación debe estar aprobada para crear un reembolso.");
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("El valor del reembolso debe ser mayor que cero.");
        if (valor.compareTo(cancelacion.calcularValorReembolso()) != 0) throw new IllegalArgumentException("El valor no coincide con el porcentaje aprobado para la cancelación.");
        this.valor = valor;
        this.fecha = LocalDateTime.now();
        this.estado = EstadoReembolso.PENDIENTE;
    }

    /** Marca el reembolso como procesado y conserva el movimiento de tarjeta generado. */
    public void marcarProcesado(MovimientoTarjeta movimiento) {
        if (estado != EstadoReembolso.PENDIENTE) throw new IllegalStateException("El reembolso ya fue procesado o falló.");
        Objects.requireNonNull(movimiento, "El movimiento de tarjeta es obligatorio.");
        if (movimiento.getTipo() != co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoTarjeta.REEMBOLSO
                || movimiento.getValor().compareTo(valor) != 0
                || !codigo.equals(movimiento.getReferenciaOperacion())) throw new IllegalArgumentException("El movimiento no corresponde a este reembolso.");
        this.movimientoTarjeta = movimiento;
        this.estado = EstadoReembolso.PROCESADO;
        cancelacion.marcarProcesada();
    }

    public void marcarFallido() {
        if (estado != EstadoReembolso.PENDIENTE) throw new IllegalStateException("El reembolso ya tiene un resultado.");
        estado = EstadoReembolso.FALLIDO;
    }

    public String getCodigo() { return codigo; }
    public Cancelacion getCancelacion() { return cancelacion; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getFecha() { return fecha; }
    public EstadoReembolso getEstado() { return estado; }
    public MovimientoTarjeta getMovimientoTarjeta() { return movimientoTarjeta; }
}