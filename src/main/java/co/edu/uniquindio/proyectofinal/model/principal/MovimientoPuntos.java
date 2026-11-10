package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoPuntos;
import java.time.LocalDateTime;
import java.util.Objects;

/** Registra una acumulación, redención, vencimiento o ajuste de puntos. */
public class MovimientoPuntos {

    private final String codigo;
    private final TipoMovimientoPuntos tipo;
    private final int cantidad;
    private final LocalDateTime fecha;
    private final String descripcion;
    private final String codigoReferencia;

    public MovimientoPuntos(String codigo, TipoMovimientoPuntos tipo, int cantidad,
                            LocalDateTime fecha, String descripcion,
                            String codigoReferencia) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        this.codigo = codigo.trim();
        this.tipo = Objects.requireNonNull(tipo, "El tipo de movimiento es obligatorio.");
        this.cantidad = cantidad;
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria.");
        this.descripcion = descripcion == null ? "" : descripcion.trim();
        this.codigoReferencia = codigoReferencia == null ? "" : codigoReferencia.trim();
    }

    public String getCodigo() { return codigo; }
    public TipoMovimientoPuntos getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public String getDescripcion() { return descripcion; }
    public String getCodigoReferencia() { return codigoReferencia; }
}