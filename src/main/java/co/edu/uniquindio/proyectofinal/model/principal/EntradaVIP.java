package co.edu.uniquindio.proyectofinal.model.principal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Representa una entrada de tipo VIP. */
public class EntradaVIP extends Entrada {

    public EntradaVIP(String codigo, String codigoQR, Funcion funcion,
                      Asiento asiento, BigDecimal precio,
                      LocalDateTime fechaEmision) {
        super(codigo, codigoQR, funcion, asiento, precio, fechaEmision);
    }

    @Override
    public String consultarDescripcion() {
        return "Entrada VIP para " + getFuncion().getPelicula().getTitulo();
    }
}
