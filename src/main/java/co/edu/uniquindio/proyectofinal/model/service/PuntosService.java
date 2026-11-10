package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.principal.Cliente;

import co.edu.uniquindio.proyectofinal.model.principal.Compra;

import co.edu.uniquindio.proyectofinal.model.principal.CuentaPuntos;

import co.edu.uniquindio.proyectofinal.model.principal.LotePuntos;



import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.LinkedHashMap;

import java.util.Map;

import java.util.Optional;

import java.util.UUID;



/** Administra las cuentas, acumulaciones, redenciones y vencimientos de puntos. */

public class PuntosService {



    private static final BigDecimal VALOR_PUNTO = new BigDecimal("50.00");

    private final Map<String, CuentaPuntos> cuentas = new LinkedHashMap<>();



    public CuentaPuntos registrarCuenta(Cliente cliente) {

        if (cliente == null) throw new IllegalArgumentException("El cliente es obligatorio.");

        if (cuentas.containsKey(cliente.getId())) {

            throw new IllegalArgumentException("El cliente ya tiene una cuenta de puntos.");

        }

        CuentaPuntos cuenta = new CuentaPuntos(cliente);

        cuentas.put(cliente.getId(), cuenta);

        return cuenta;

    }



    public CuentaPuntos obtenerOCrearCuenta(Cliente cliente) {

        if (cliente == null) throw new IllegalArgumentException("El cliente es obligatorio.");

        return cuentas.computeIfAbsent(cliente.getId(), id -> new CuentaPuntos(cliente));

    }



    public Optional<CuentaPuntos> buscarCuenta(Cliente cliente) {

        return cliente == null ? Optional.empty() : Optional.ofNullable(cuentas.get(cliente.getId()));

    }



    public int consultarSaldo(Cliente cliente, LocalDate fecha) {

        return obtenerOCrearCuenta(cliente).consultarSaldo(fecha);

    }



    public LotePuntos acumularPuntos(Cliente cliente, int cantidad,

                                     LocalDate fecha, String referencia) {

        return obtenerOCrearCuenta(cliente).acumularPuntos(

                UUID.randomUUID().toString(), cantidad, fecha, referencia);

    }



    /** Regla propuesta para CinemaUQ: un punto por cada $10.000 efectivamente pagados. */

    public int acumularPorCompra(Compra compra) {

        if (compra == null) throw new IllegalArgumentException("La compra es obligatoria.");

        int puntos = compra.getTotal().divideToIntegralValue(new BigDecimal("10000"))

                .intValueExact();

        if (puntos > 0) {

            acumularPuntos(compra.getCliente(), puntos, LocalDate.now(), compra.getCodigo());

        }

        return puntos;

    }



    /** Redime puntos y aplica su valor monetario a una compra, con tope del 50 % del subtotal. */

    public BigDecimal redimirParaCompra(Cliente cliente, Compra compra, int cantidadPuntos) {

        if (compra == null || cliente == null || !compra.getCliente().getId().equals(cliente.getId())) {

            throw new IllegalArgumentException("La compra no corresponde al cliente.");

        }

        if (cantidadPuntos <= 0) throw new IllegalArgumentException("La cantidad de puntos debe ser positiva.");

        BigDecimal valor = VALOR_PUNTO.multiply(BigDecimal.valueOf(cantidadPuntos));

        BigDecimal subtotal = compra.calcularSubtotal();

        BigDecimal limite = subtotal.multiply(new BigDecimal("0.50"));

        BigDecimal restanteElegible = subtotal.subtract(compra.getDescuentos());

        if (valor.compareTo(limite) > 0 || valor.compareTo(restanteElegible) > 0) {

            throw new IllegalArgumentException("Los puntos no pueden cubrir más del 50 % ni superar el valor elegible de la compra.");

        }

        obtenerOCrearCuenta(cliente).redimirPuntos(cantidadPuntos, LocalDate.now(), compra.getCodigo());

        compra.aplicarValorPuntosRedimidos(valor);

        return valor;

    }



    public int procesarVencimientos(Cliente cliente, LocalDate fecha) {

        return obtenerOCrearCuenta(cliente).procesarVencimientos(fecha);

    }

}