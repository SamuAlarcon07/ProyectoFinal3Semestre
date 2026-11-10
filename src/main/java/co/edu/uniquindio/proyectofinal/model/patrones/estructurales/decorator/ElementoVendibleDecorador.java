package co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator;

import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Decorador base que envuelve un elemento vendible y delega en él
 * las operaciones comunes del contrato ElementoVendible.
 */
public abstract class ElementoVendibleDecorador implements ElementoVendible {

    private final ElementoVendible elementoDecorado;

    protected ElementoVendibleDecorador(ElementoVendible elementoDecorado) {
        this.elementoDecorado = Objects.requireNonNull(
                elementoDecorado,
                "El elemento que se desea decorar es obligatorio."
        );
    }

    protected ElementoVendible getElementoDecorado() {
        return elementoDecorado;
    }

    @Override
    public BigDecimal consultarPrecio() {
        return elementoDecorado.consultarPrecio();
    }

    @Override
    public String consultarDescripcion() {
        return elementoDecorado.consultarDescripcion();
    }
}

