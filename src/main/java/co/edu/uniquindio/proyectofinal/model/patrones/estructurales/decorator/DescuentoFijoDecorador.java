package co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator;



import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;



import java.math.BigDecimal;

import java.math.RoundingMode;

import java.util.Objects;



/** Aplica una rebaja de valor fijo sin modificar el elemento original. */

public class DescuentoFijoDecorador extends ElementoVendibleDecorador {



    private final BigDecimal valorDescuento;



    public DescuentoFijoDecorador(

            ElementoVendible elemento,

            BigDecimal valorDescuento

    ) {

        super(elemento);

        this.valorDescuento = validarDescuento(

                valorDescuento,

                elemento.consultarPrecio()

        );

    }



    @Override

    public BigDecimal consultarPrecio() {

        return getElementoDecorado().consultarPrecio()

                .subtract(valorDescuento)

                .setScale(2, RoundingMode.HALF_UP);

    }



    @Override

    public String consultarDescripcion() {

        return getElementoDecorado().consultarDescripcion()

                + " (descuento de $"

                + valorDescuento.stripTrailingZeros().toPlainString()

                + ")";

    }



    private static BigDecimal validarDescuento(

            BigDecimal descuento,

            BigDecimal precioOriginal

    ) {

        Objects.requireNonNull(descuento, "El descuento es obligatorio.");

        if (descuento.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(

                    "El descuento debe ser mayor que cero."

            );

        }

        if (precioOriginal == null || descuento.compareTo(precioOriginal) >= 0) {

            throw new IllegalArgumentException(

                    "El descuento debe ser menor que el precio original."

            );

        }

        return descuento;

    }

}
