package co.edu.uniquindio.proyectofinal.model.patrones.creacionales.builder;

import co.edu.uniquindio.proyectofinal.model.interfaces.EstadoCompra;
import co.edu.uniquindio.proyectofinal.model.principal.Cliente;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;
import co.edu.uniquindio.proyectofinal.model.principal.DetalleCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Construye objetos Compra paso a paso.
 */
public class CompraBuilder {

    private final String codigo;
    private final Cliente cliente;
    private final EstadoCompra estadoInicial;

    private LocalDateTime fecha = LocalDateTime.now();
    private final List<DetalleCompra> detalles = new ArrayList<>();
    private BigDecimal descuentos = BigDecimal.ZERO;
    private BigDecimal valorPuntosRedimidos = BigDecimal.ZERO;

    public CompraBuilder(String codigo, Cliente cliente,
                         EstadoCompra estadoInicial) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de la compra es obligatorio."
            );
        }

        this.codigo = codigo.trim();
        this.cliente = Objects.requireNonNull(cliente);
        this.estadoInicial = Objects.requireNonNull(estadoInicial);
    }

    public CompraBuilder conFecha(LocalDateTime fecha) {
        this.fecha = Objects.requireNonNull(fecha);
        return this;
    }

    public CompraBuilder agregarDetalle(DetalleCompra detalle) {
        this.detalles.add(Objects.requireNonNull(detalle));
        return this;
    }

    public CompraBuilder conDescuentos(BigDecimal descuentos) {
        validarValorNoNegativo(descuentos);
        this.descuentos = descuentos;
        return this;
    }

    public CompraBuilder conValorPuntosRedimidos(BigDecimal valor) {
        validarValorNoNegativo(valor);
        this.valorPuntosRedimidos = valor;
        return this;
    }

    public Compra build() {
        Compra compra = new Compra(
                codigo, cliente, fecha, estadoInicial
        );

        for (DetalleCompra detalle : detalles) {
            compra.agregarDetalle(detalle);
        }

        if (descuentos.compareTo(BigDecimal.ZERO) > 0) {
            compra.aplicarDescuentos(descuentos);
        }

        if (valorPuntosRedimidos.compareTo(BigDecimal.ZERO) > 0) {
            compra.aplicarValorPuntosRedimidos(
                    valorPuntosRedimidos
            );
        }

        return compra;
    }

    private static void validarValorNoNegativo(BigDecimal valor) {
        if (valor == null ||
                valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El valor no puede ser nulo ni negativo."
            );
        }
    }
}
