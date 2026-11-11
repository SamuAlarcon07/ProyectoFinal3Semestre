package co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator;



import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;

import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;

import co.edu.uniquindio.proyectofinal.model.principal.Producto;



import java.util.Map;

import java.util.Objects;



/**

 * Decorador que conserva el contrato ElementoConfiteria.

 * Es importante para que InventarioService siga identificando

 * los productos base aunque el elemento tenga un descuento.

 */

public abstract class ElementoConfiteriaDecorador

        extends ElementoVendibleDecorador implements ElementoConfiteria {



    private final ElementoConfiteria confiteriaOriginal;



    protected ElementoConfiteriaDecorador(

            ElementoVendible elementoDecorado,

            ElementoConfiteria confiteriaOriginal

    ) {

        super(elementoDecorado);

        this.confiteriaOriginal = Objects.requireNonNull(

                confiteriaOriginal,

                "El elemento de confitería original es obligatorio."

        );

    }



    @Override

    public Map<Producto, Integer> obtenerProductosBase() {

        return confiteriaOriginal.obtenerProductosBase();

    }

}