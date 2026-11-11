package co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator;



import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;



import java.math.BigDecimal;

import java.math.RoundingMode;

import java.util.Objects;



/** Aplica un descuento porcentual sin modificar el elemento original. */

public class DescuentoPorcentajeDecorador extends ElementoVendibleDecorador {



    private static final BigDecimal CIEN = new BigDecimal("100");

    private final BigDecimal porcentaje;



    public DescuentoPorcentajeDecorador(

            ElementoVendible elemento,

            BigDecimal porcentaje

    ) {

        super(elemento);

        this.porcentaje = validarPorcentaje(porcentaje);

    }



    @Override

    public BigDecimal consultarPrecio() {

        BigDecimal precioOriginal = getElementoDecorado().consultarPrecio();

        BigDecimal valorDescuento = precioOriginal.multiply(porcentaje)

                .divide(CIEN, 2, RoundingMode.HALF_UP);



        return precioOriginal.subtract(valorDescuento)

                .setScale(2, RoundingMode.HALF_UP);

    }



    @Override

    public String consultarDescripcion() {

        return getElementoDecorado().consultarDescripcion()

                + " (descuento del "

                + porcentaje.stripTrailingZeros().toPlainString()

                + "%)";

    }



    private static BigDecimal validarPorcentaje(BigDecimal porcentaje) {

        Objects.requireNonNull(porcentaje, "El porcentaje es obligatorio.");

        if (porcentaje.compareTo(BigDecimal.ZERO) <= 0

                || porcentaje.compareTo(CIEN) >= 0) {

            throw new IllegalArgumentException(

                    "El porcentaje debe ser mayor que 0 y menor que 100."

            );

        }

        return porcentaje;

    }

}