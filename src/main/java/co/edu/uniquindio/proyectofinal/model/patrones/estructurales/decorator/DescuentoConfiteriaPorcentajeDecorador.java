package co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator;

import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Variante para productos o combos. Aplica el descuento y conserva
 * la composición requerida por InventarioService.
 */
public class DescuentoConfiteriaPorcentajeDecorador
        extends ElementoConfiteriaDecorador {

    public DescuentoConfiteriaPorcentajeDecorador(
            ElementoConfiteria elemento,
            BigDecimal porcentaje
    ) {
        super(
                new DescuentoPorcentajeDecorador(
                        Objects.requireNonNull(elemento), porcentaje
                ),
                elemento
        );
    }
}
