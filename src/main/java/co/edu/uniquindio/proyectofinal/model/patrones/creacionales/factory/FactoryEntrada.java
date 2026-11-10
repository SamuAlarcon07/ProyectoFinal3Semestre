package co.edu.uniquindio.proyectofinal.model.patrones.creacionales.factory;

import co.edu.uniquindio.proyectofinal.model.principal.Asiento;
import co.edu.uniquindio.proyectofinal.model.principal.Entrada;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Fábrica abstracta para crear los diferentes tipos de entradas.
 */
public abstract class FactoryEntrada {

    public abstract Entrada crearEntrada(
            String codigo,
            String codigoQR,
            Funcion funcion,
            Asiento asiento,
            BigDecimal precio,
            LocalDateTime fechaEmision
    );
}
