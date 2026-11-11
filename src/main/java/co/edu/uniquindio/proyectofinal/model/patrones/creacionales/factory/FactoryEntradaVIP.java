package co.edu.uniquindio.proyectofinal.model.patrones.creacionales.factory;

import co.edu.uniquindio.proyectofinal.model.principal.Asiento;
import co.edu.uniquindio.proyectofinal.model.principal.Entrada;
import co.edu.uniquindio.proyectofinal.model.principal.EntradaVIP;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Fábrica encargada de crear entradas VIP. */
public class FactoryEntradaVIP extends FactoryEntrada {

    @Override
    public Entrada crearEntrada(
            String codigo,
            String codigoQR,
            Funcion funcion,
            Asiento asiento,
            BigDecimal precio,
            LocalDateTime fechaEmision
    ) {
        return new EntradaVIP(
                codigo, codigoQR, funcion, asiento, precio, fechaEmision
        );
    }
}
