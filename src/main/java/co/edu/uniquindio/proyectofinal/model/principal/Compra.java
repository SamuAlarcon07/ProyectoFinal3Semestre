package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.interfaces.EstadoCompra;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Representa una operación de compra realizada por un cliente. */
public class Compra {

    private final String codigo;
    private final Cliente cliente;
    private final LocalDateTime fecha;
    private final List<DetalleCompra> detalles;
    private BigDecimal subtotal;
    private BigDecimal descuentos;
    private BigDecimal valorPuntosRedimidos;
    private BigDecimal total;
    private EstadoCompra estado;

    public Compra(String codigo, Cliente cliente, LocalDateTime fecha,
                  EstadoCompra estadoInicial) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        this.codigo = codigo.trim();
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        this.fecha = Objects.requireNonNull(fecha, "La fecha de compra es obligatoria.");
        this.estado = Objects.requireNonNull(estadoInicial, "El estado inicial es obligatorio.");
        this.detalles = new ArrayList<>();
        this.subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.descuentos = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.valorPuntosRedimidos = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    public void agregarDetalle(DetalleCompra detalle) {
        Objects.requireNonNull(detalle, "El detalle no puede ser nulo.");
        detalles.add(detalle);
        calcularTotal();
    }

    public void eliminarDetalle(String codigoDetalle) {
        boolean eliminado = detalles.removeIf(detalle -> detalle.getCodigo().equals(codigoDetalle));
        if (!eliminado) throw new IllegalArgumentException("No existe un detalle con ese código.");
        calcularTotal();
    }

    public BigDecimal calcularSubtotal() {
        return detalles.stream().map(DetalleCompra::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotal() {
        this.subtotal = calcularSubtotal();
        BigDecimal resultado = subtotal.subtract(descuentos).subtract(valorPuntosRedimidos);
        if (resultado.compareTo(BigDecimal.ZERO) < 0) throw new IllegalStateException("Los descuentos y puntos superan el subtotal.");
        this.total = resultado.setScale(2, RoundingMode.HALF_UP);
        return total;
    }

    public void aplicarDescuentos(BigDecimal valor) {
        validarValorNoNegativo(valor, "El descuento");
        BigDecimal anterior = this.descuentos;
        this.descuentos = valor;
        try {
            calcularTotal();
        } catch (IllegalStateException ex) {
            this.descuentos = anterior;
            calcularTotal();
            throw ex;
        }
    }

    public void aplicarValorPuntosRedimidos(BigDecimal valor) {
        validarValorNoNegativo(valor, "El valor de puntos redimidos");
        BigDecimal anterior = this.valorPuntosRedimidos;
        this.valorPuntosRedimidos = valor;
        try {
            calcularTotal();
        } catch (IllegalStateException ex) {
            this.valorPuntosRedimidos = anterior;
            calcularTotal();
            throw ex;
        }
    }

    /** Delega la operación a la implementación concreta del patrón State. */
    public void confirmarPago() { estado.pagar(this); }
    public void solicitarCancelacion() { estado.cancelar(this); }
    public void solicitarReembolso() { estado.reembolsar(this); }

    /** Permite que el objeto de estado concreto cambie el estado de la compra. */
    public void cambiarEstado(EstadoCompra nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado, "El nuevo estado es obligatorio.");
    }

    private static void validarValorNoNegativo(BigDecimal valor, String campo) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException(campo + " no puede ser negativo.");
    }

    public String getCodigo() { return codigo; }
    public Cliente getCliente() { return cliente; }
    public LocalDateTime getFecha() { return fecha; }
    public List<DetalleCompra> getDetalles() { return List.copyOf(detalles); }
    public BigDecimal getSubtotal() { return subtotal; }
    public BigDecimal getDescuentos() { return descuentos; }
    public BigDecimal getValorPuntosRedimidos() { return valorPuntosRedimidos; }
    public BigDecimal getTotal() { return total; }
    public EstadoCompra getEstado() { return estado; }
    public String consultarNombreEstado() { return estado.obtenerNombre(); }
}