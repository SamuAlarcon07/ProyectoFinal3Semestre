package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.principal.Cliente;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;
import co.edu.uniquindio.proyectofinal.model.principal.CuentaPuntos;
import co.edu.uniquindio.proyectofinal.model.principal.LotePuntos;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

/** Administra cuentas, acumulaciones, redenciones y vencimientos de puntos. */
public class PuntosService {

    private static final BigDecimal VALOR_PUNTO = new BigDecimal("50.00");
    private static final BigDecimal VALOR_POR_PUNTO = new BigDecimal("10000.00");
    private static final BigDecimal LIMITE_REDENCION = new BigDecimal("0.50");

    private final Map<String, CuentaPuntos> cuentas = new LinkedHashMap<>();
    private final Set<String> comprasConPuntosAcumulados = new HashSet<>();

    public synchronized CuentaPuntos registrarCuenta(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        if (cuentas.containsKey(cliente.getId())) {
            throw new IllegalArgumentException(
                    "El cliente ya tiene una cuenta de puntos.");
        }

        CuentaPuntos cuenta = new CuentaPuntos(cliente);
        cuentas.put(cliente.getId(), cuenta);
        return cuenta;
    }

    public synchronized CuentaPuntos obtenerOCrearCuenta(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("El cliente es obligatorio.");
        }
        return cuentas.computeIfAbsent(
                cliente.getId(), id -> new CuentaPuntos(cliente));
    }

    public synchronized Optional<CuentaPuntos> buscarCuenta(Cliente cliente) {
        return cliente == null
                ? Optional.empty()
                : Optional.ofNullable(cuentas.get(cliente.getId()));
    }

    public synchronized int consultarSaldo(Cliente cliente, LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        return obtenerOCrearCuenta(cliente).consultarSaldo(fecha);
    }

    public synchronized LotePuntos acumularPuntos(Cliente cliente,
                                                  int cantidad,
                                                  LocalDate fecha,
                                                  String referencia) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        return obtenerOCrearCuenta(cliente).acumularPuntos(
                UUID.randomUUID().toString(), cantidad, fecha, referencia);
    }

    /**
     * Calcula los puntos según la regla propuesta para CinemaUQ:
     * un punto por cada $10.000 efectivamente pagados.
     */
    public synchronized int calcularPuntosPorCompra(Compra compra) {
        if (compra == null) {
            throw new IllegalArgumentException("La compra es obligatoria.");
        }

        BigDecimal totalPagado = compra.calcularTotal();
        if (totalPagado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "El total de la compra no puede ser negativo.");
        }

        return totalPagado.divideToIntegralValue(VALOR_POR_PUNTO)
                .intValueExact();
    }

    /** Valida antes del cobro que la compra pueda acumular puntos una sola vez. */
    public synchronized void validarPuedeAcumularPorCompra(Compra compra) {
        if (compra == null) {
            throw new IllegalArgumentException("La compra es obligatoria.");
        }
        if (comprasConPuntosAcumulados.contains(compra.getCodigo())) {
            throw new IllegalStateException(
                    "Ya se acumularon puntos por esta compra.");
        }
        calcularPuntosPorCompra(compra);
    }

    /** Acumula puntos una sola vez y solamente después de confirmar el pago. */
    public synchronized int acumularPorCompra(Compra compra) {
        if (compra == null) {
            throw new IllegalArgumentException("La compra es obligatoria.");
        }
        if (!"PAGADA".equals(compra.consultarNombreEstado())) {
            throw new IllegalStateException(
                    "Solo una compra pagada puede generar puntos.");
        }
        validarPuedeAcumularPorCompra(compra);

        int puntos = calcularPuntosPorCompra(compra);
        if (puntos > 0) {
            acumularPuntos(
                    compra.getCliente(), puntos, LocalDate.now(), compra.getCodigo());
        }

        // También se registra cuando el total no alcanza para generar un punto,
        // evitando que el mismo código se procese varias veces.
        comprasConPuntosAcumulados.add(compra.getCodigo());
        return puntos;
    }

    /**
     * Redime puntos de forma acumulativa y limita el total redimido al 50 %
     * del subtotal y al valor que queda después de aplicar descuentos.
     */
    public synchronized BigDecimal redimirParaCompra(Cliente cliente,
                                                     Compra compra,
                                                     int cantidadPuntos) {
        if (compra == null || cliente == null
                || !compra.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException(
                    "La compra no corresponde al cliente.");
        }
        if (!"PENDIENTE".equals(compra.consultarNombreEstado())) {
            throw new IllegalStateException(
                    "Solo se pueden redimir puntos en una compra pendiente.");
        }
        if (cantidadPuntos <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad de puntos debe ser positiva.");
        }

        BigDecimal valorNuevo = VALOR_PUNTO.multiply(
                BigDecimal.valueOf(cantidadPuntos));
        BigDecimal subtotal = compra.calcularSubtotal();
        BigDecimal limitePorcentaje = subtotal.multiply(LIMITE_REDENCION);
        BigDecimal valorTotalRedimido = compra.getValorPuntosRedimidos()
                .add(valorNuevo);
        BigDecimal valorMaximoTrasDescuento = subtotal.subtract(compra.getDescuentos());

        if (valorTotalRedimido.compareTo(limitePorcentaje) > 0
                || valorTotalRedimido.compareTo(valorMaximoTrasDescuento) > 0) {
            throw new IllegalArgumentException(
                    "Los puntos no pueden cubrir más del 50 % ni superar el valor restante de la compra.");
        }

        // La cuenta valida el saldo y consume primero los lotes más próximos a vencer.
        obtenerOCrearCuenta(cliente).redimirPuntos(
                cantidadPuntos, LocalDate.now(), compra.getCodigo());

        // Se acumula sobre el valor previamente redimido; no se reemplaza.
        compra.aplicarValorPuntosRedimidos(valorTotalRedimido);
        return valorNuevo;
    }

    public synchronized int procesarVencimientos(Cliente cliente, LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha es obligatoria.");
        }
        return obtenerOCrearCuenta(cliente).procesarVencimientos(fecha);
    }
}
