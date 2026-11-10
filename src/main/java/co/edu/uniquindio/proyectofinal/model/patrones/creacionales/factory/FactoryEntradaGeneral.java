package co.edu.uniquindio.proyectofinal.model.patrones.creacionales.factory;

import co.edu.uniquindio.proyectofinal.model.principal.Asiento;
import co.edu.uniquindio.proyectofinal.model.principal.Entrada;
import co.edu.uniquindio.proyectofinal.model.principal.EntradaGeneral;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Fábrica de entradas generales. */
public class FactoryEntradaGeneral extends FactoryEntrada {

    @Override
    public Entrada crearEntrada(
            String codigo, String codigoQR, Funcion funcion,
            Asiento asiento, BigDecimal precio, LocalDateTime fechaEmision
    ) {
        return new EntradaGeneral(
                codigo, codigoQR, funcion, asiento, precio, fechaEmision
        );
    }
}

