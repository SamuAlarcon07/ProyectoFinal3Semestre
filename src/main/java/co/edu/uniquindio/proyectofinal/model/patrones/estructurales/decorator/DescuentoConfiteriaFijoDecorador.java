package co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator;



import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;



import java.math.BigDecimal;

import java.util.Objects;



/** Variante de descuento fijo que conserva la composición de confitería. */

public class DescuentoConfiteriaFijoDecorador

        extends ElementoConfiteriaDecorador {



    public DescuentoConfiteriaFijoDecorador(

            ElementoConfiteria elemento,

            BigDecimal valorDescuento

    ) {

        super(

                new DescuentoFijoDecorador(

                        Objects.requireNonNull(elemento), valorDescuento

                ),

                elemento

        );

    }

}
