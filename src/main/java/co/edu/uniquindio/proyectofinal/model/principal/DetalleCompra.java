package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;
import java.math.BigDecimal;
import java.util.Objects;

/** Representa una línea de una compra con su cantidad y precio aplicado. */
public class DetalleCompra {

    private final String codigo;
    private final ElementoVendible elemento;
    private final int cantidad;
    private final BigDecimal precioUnitario;
    private final BigDecimal subtotal;

    public DetalleCompra(String codigo, ElementoVendible elemento, int cantidad) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        this.codigo = codigo.trim();
        this.elemento = Objects.requireNonNull(elemento, "El elemento vendible es obligatorio.");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        this.cantidad = cantidad;
        this.precioUnitario = Objects.requireNonNull(elemento.consultarPrecio(), "El precio es obligatorio.");
        if (precioUnitario.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El precio no puede ser negativo.");
        this.subtotal = calcularSubtotal();
    }

    public BigDecimal calcularSubtotal() { return precioUnitario.multiply(BigDecimal.valueOf(cantidad)); }

    public String getCodigo() { return codigo; }
    public ElementoVendible getElemento() { return elemento; }
    public int getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}