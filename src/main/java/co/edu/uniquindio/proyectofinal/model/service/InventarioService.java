package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;

import co.edu.uniquindio.proyectofinal.model.principal.Compra;

import co.edu.uniquindio.proyectofinal.model.principal.DetalleCompra;

import co.edu.uniquindio.proyectofinal.model.principal.Inventario;

import co.edu.uniquindio.proyectofinal.model.principal.Producto;



import java.util.Collection;

import java.util.HashSet;

import java.util.Map;

import java.util.Set;



/** Controla las existencias y descuenta los productos de compras confirmadas. */

public class InventarioService {



    private final Set<String> comprasDescontadas = new HashSet<>();



    public void registrarEntrada(Producto producto, int cantidad, String observacion) {

        obtenerInventario(producto).registrarEntrada(cantidad, observacion);

    }



    public void registrarMerma(Producto producto, int cantidad, String observacion) {

        obtenerInventario(producto).registrarMerma(cantidad, observacion);

    }



    public synchronized boolean validarDisponibilidadCompra(Compra compra) {

        for (Map.Entry<Producto, Integer> requisito : calcularRequisitos(compra).entrySet()) {

            Producto producto = requisito.getKey();

            if (!producto.estaActivo()

                    || !producto.getInventario().verificarDisponibilidad(requisito.getValue())) {

                return false;

            }

        }

        return true;

    }



    /**
     * Indica si el inventario de una compra ya fue descontado.
     * Se utiliza para evitar procesar dos veces la misma venta.
     */
    public synchronized boolean fueDescontada(String codigoCompra) {
        if (codigoCompra == null || codigoCompra.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de compra es obligatorio.");
        }
        return comprasDescontadas.contains(codigoCompra.trim());
    }

    public synchronized void registrarVentaCompra(Compra compra) {

        if (compra == null) throw new IllegalArgumentException("La compra es obligatoria.");

        if (comprasDescontadas.contains(compra.getCodigo())) {

            throw new IllegalStateException("El inventario de esta compra ya fue descontado.");

        }

        Map<Producto, Integer> requisitos = calcularRequisitos(compra);

        for (Map.Entry<Producto, Integer> requisito : requisitos.entrySet()) {

            if (!requisito.getKey().estaActivo()

                    || !requisito.getKey().getInventario().verificarDisponibilidad(requisito.getValue())) {

                throw new IllegalStateException("Existencias insuficientes para " + requisito.getKey().getNombre());

            }

        }

        requisitos.forEach((producto, cantidad) ->

                producto.getInventario().registrarVenta(cantidad,

                        "Venta asociada a compra " + compra.getCodigo()));

        comprasDescontadas.add(compra.getCodigo());

    }



    public java.util.List<Producto> consultarProductosPorReponer(Collection<Producto> productos) {

        return productos.stream().filter(p -> p.getInventario().necesitaReposicion()).toList();

    }



    private Map<Producto, Integer> calcularRequisitos(Compra compra) {

        if (compra == null) throw new IllegalArgumentException("La compra es obligatoria.");

        Map<Producto, Integer> requisitos = new java.util.LinkedHashMap<>();

        for (DetalleCompra detalle : compra.getDetalles()) {

            if (detalle.getElemento() instanceof ElementoConfiteria confiteria) {

                for (Map.Entry<Producto, Integer> base : confiteria.obtenerProductosBase().entrySet()) {

                    requisitos.merge(base.getKey(), base.getValue() * detalle.getCantidad(), Integer::sum);

                }

            }

        }

        return requisitos;

    }



    private Inventario obtenerInventario(Producto producto) {

        if (producto == null) throw new IllegalArgumentException("El producto es obligatorio.");

        return producto.getInventario();

    }

}