package co.edu.uniquindio.proyectofinal.model.service;


import co.edu.uniquindio.proyectofinal.model.principal.Compra;
import co.edu.uniquindio.proyectofinal.model.principal.MovimientoTarjeta;
import co.edu.uniquindio.proyectofinal.model.principal.TarjetaVirtual;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;


/** Ejecuta el cobro de una compra mediante una tarjeta virtual. */

public class PagoService {



    private final Set<String> pagosProcesados = new HashSet<>();



    public MovimientoTarjeta procesarPago(Compra compra, TarjetaVirtual tarjeta) {

        Objects.requireNonNull(compra, "La compra es obligatoria.");

        Objects.requireNonNull(tarjeta, "La tarjeta es obligatoria.");

        if (!compra.getCliente().getId().equals(tarjeta.getTitular().getId())) {

            throw new IllegalArgumentException("La tarjeta no pertenece al cliente de la compra.");

        }

        if (pagosProcesados.contains(compra.getCodigo())) {

            throw new IllegalStateException("Esta compra ya tiene un pago procesado.");

        }

        BigDecimal total = compra.calcularTotal();

        if (total.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("El total debe ser mayor que cero para procesar el pago.");

        }

        tarjeta.pagar(total, compra.getCodigo());

        pagosProcesados.add(compra.getCodigo());

        List<MovimientoTarjeta> movimientos = tarjeta.getMovimientos();

        return movimientos.get(movimientos.size() - 1);

    }

}