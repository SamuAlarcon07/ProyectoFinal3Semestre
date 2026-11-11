package co.edu.uniquindio.proyectofinal.model.interfaces;

import co.edu.uniquindio.proyectofinal.model.principal.Producto;
import java.util.Map;

/**
 * Contrato para los elementos de confitería que pueden venderse
 * individualmente o formar parte de un combo.
 *
 * Permite descomponer un elemento compuesto en sus productos base
 * para controlar correctamente el inventario.
 *
 * @author Equipo CinemaUQ
 */
public interface ElementoConfiteria extends ElementoVendible {

    /**
     * Devuelve los productos básicos que componen este elemento y
     * la cantidad requerida de cada uno.
     *
     * @return mapa inmodificable de producto y cantidad.
     */
    Map<Producto, Integer> obtenerProductosBase();
}