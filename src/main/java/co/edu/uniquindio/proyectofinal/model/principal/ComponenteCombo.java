package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;
import java.util.Objects;

/** Representa un elemento y su cantidad dentro de un combo. */
public class ComponenteCombo {

    private final ElementoConfiteria elemento;
    private final int cantidad;

    public ComponenteCombo(ElementoConfiteria elemento, int cantidad) {
        this.elemento = Objects.requireNonNull(elemento, "El elemento del combo es obligatorio.");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        this.cantidad = cantidad;
    }

    public ElementoConfiteria getElemento() { return elemento; }
    public int getCantidad() { return cantidad; }
}