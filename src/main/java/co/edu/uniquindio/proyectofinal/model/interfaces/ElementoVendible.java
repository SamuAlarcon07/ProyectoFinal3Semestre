package co.edu.uniquindio.proyectofinal.model.interfaces;

import java.math.BigDecimal;

/**
 * Contrato común para los elementos que pueden venderse en CinemaUQ.
 * Se utilizará posteriormente en Composite y Decorator.
 */
public interface ElementoVendible {

    BigDecimal consultarPrecio();

    String consultarDescripcion();
}
