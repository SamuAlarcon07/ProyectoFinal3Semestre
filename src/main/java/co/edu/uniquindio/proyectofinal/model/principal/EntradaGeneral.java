package co.edu.uniquindio.proyectofinal.model.principal;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Representa una entrada de tipo general. */
public class EntradaGeneral extends Entrada {

    public EntradaGeneral(String codigo, String codigoQR, Funcion funcion,
                          Asiento asiento, BigDecimal precio,
                          LocalDateTime fechaEmision) {
        super(codigo, codigoQR, funcion, asiento, precio, fechaEmision);
    }

    @Override
    public String consultarDescripcion() {
        return "Entrada general para " + getFuncion().getPelicula().getTitulo();
    }
}
