package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoInventario;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Administra las existencias y el historial de movimientos de un producto. */
public class Inventario {

    private int stockActual;
    private int stockMinimo;
    private final String unidadMedida;
    private LocalDateTime fechaActualizacion;
    private final List<MovimientoInventario> movimientos;

    public Inventario(int stockInicial, int stockMinimo, String unidadMedida) {
        if (stockInicial < 0 || stockMinimo < 0) throw new IllegalArgumentException("Las existencias no pueden ser negativas.");
        if (unidadMedida == null || unidadMedida.isBlank()) throw new IllegalArgumentException("La unidad de medida es obligatoria.");
        this.stockActual = stockInicial;
        this.stockMinimo = stockMinimo;
        this.unidadMedida = unidadMedida.trim();
        this.fechaActualizacion = LocalDateTime.now();
        this.movimientos = new ArrayList<>();
    }

    /** Registra una entrada de mercancía. */
    public void registrarEntrada(int cantidad, String observacion) {
        validarCantidad(cantidad);
        registrar(TipoMovimientoInventario.ENTRADA, cantidad, stockActual + cantidad, observacion);
    }

    /** Descuenta unidades por una venta confirmada. */
    public void registrarVenta(int cantidad, String observacion) {
        descontar(TipoMovimientoInventario.VENTA, cantidad, observacion);
    }

    /** Descuenta unidades por pérdida, daño o vencimiento. */
    public void registrarMerma(int cantidad, String observacion) {
        descontar(TipoMovimientoInventario.MERMA, cantidad, observacion);
    }

    /** Establece una nueva existencia luego de un conteo físico. */
    public void ajustarExistencias(int nuevoStock, String observacion) {
        if (nuevoStock < 0) throw new IllegalArgumentException("El stock nuevo no puede ser negativo.");
        int diferencia = Math.abs(nuevoStock - stockActual);
        if (diferencia == 0) throw new IllegalArgumentException("El ajuste no modifica las existencias.");
        registrar(TipoMovimientoInventario.AJUSTE, diferencia, nuevoStock, observacion);
    }

    private void descontar(TipoMovimientoInventario tipo, int cantidad, String observacion) {
        validarCantidad(cantidad);
        if (cantidad > stockActual) throw new IllegalStateException("No hay existencias suficientes.");
        registrar(tipo, cantidad, stockActual - cantidad, observacion);
    }

    private void registrar(TipoMovimientoInventario tipo, int cantidad,
                           int nuevoStock, String observacion) {
        int anterior = stockActual;
        this.stockActual = nuevoStock;
        this.fechaActualizacion = LocalDateTime.now();
        movimientos.add(new MovimientoInventario(UUID.randomUUID().toString(), tipo,
                cantidad, fechaActualizacion, anterior, nuevoStock, observacion));
    }

    private static void validarCantidad(int cantidad) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
    }

    public boolean verificarDisponibilidad(int cantidad) { return cantidad > 0 && stockActual >= cantidad; }
    public boolean necesitaReposicion() { return stockActual <= stockMinimo; }

    public void actualizarStockMinimo(int nuevoStockMinimo) {
        if (nuevoStockMinimo < 0) throw new IllegalArgumentException("El stock mínimo no puede ser negativo.");
        this.stockMinimo = nuevoStockMinimo;
    }

    public int getStockActual() { return stockActual; }
    public int getStockMinimo() { return stockMinimo; }
    public String getUnidadMedida() { return unidadMedida; }
    public LocalDateTime getFechaActualizacion() { return fechaActualizacion; }
    public List<MovimientoInventario> getMovimientos() { return List.copyOf(movimientos); }
}