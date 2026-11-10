package co.edu.uniquindio.proyectofinal.model.principal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Representa una entrada de tipo preferencial. */
public class EntradaPreferencial extends Entrada {

    public EntradaPreferencial(String codigo, String codigoQR, Funcion funcion,
                               Asiento asiento, BigDecimal precio,
                               LocalDateTime fechaEmision) {
        super(codigo, codigoQR, funcion, asiento, precio, fechaEmision);
    }

    @Override
    public String consultarDescripcion() {
        return "Entrada preferencial para " + getFuncion().getPelicula().getTitulo();
    }
}
