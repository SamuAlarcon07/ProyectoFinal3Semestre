package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoInventario;
import java.time.LocalDateTime;
import java.util.Objects;

/** Registra una modificación de las existencias de un producto. */
public class MovimientoInventario {

    private final String codigo;
    private final TipoMovimientoInventario tipo;
    private final int cantidad;
    private final LocalDateTime fecha;
    private final int stockAnterior;
    private final int stockResultante;
    private final String observacion;

    public MovimientoInventario(String codigo, TipoMovimientoInventario tipo,
                                int cantidad, LocalDateTime fecha,
                                int stockAnterior, int stockResultante,
                                String observacion) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        if (stockAnterior < 0 || stockResultante < 0) throw new IllegalArgumentException("El stock no puede ser negativo.");
        this.codigo = codigo.trim();
        this.tipo = Objects.requireNonNull(tipo, "El tipo de movimiento es obligatorio.");
        this.cantidad = cantidad;
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria.");
        this.stockAnterior = stockAnterior;
        this.stockResultante = stockResultante;
        this.observacion = observacion == null ? "" : observacion.trim();
    }

    public String getCodigo() { return codigo; }
    public TipoMovimientoInventario getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public LocalDateTime getFecha() { return fecha; }
    public int getStockAnterior() { return stockAnterior; }
    public int getStockResultante() { return stockResultante; }
    public String getObservacion() { return observacion; }
}