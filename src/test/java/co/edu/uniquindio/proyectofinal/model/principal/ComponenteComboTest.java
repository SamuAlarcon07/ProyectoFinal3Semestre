
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ComponenteComboTest {

    private Producto crearProducto() {
        return new Producto(
                "P01",
                "Gaseosa",
                "Gaseosa personal",
                new BigDecimal("5000"),
                50,
                5,
                "unidad"
        );
    }

    @Test
    void crearComponenteCorrectamente() {
        Producto producto = crearProducto();

        ComponenteCombo componente = new ComponenteCombo(producto, 2);

        assertSame(producto, componente.getElemento());
        assertEquals(2, componente.getCantidad());
    }

    @Test
    void rechazarElementoNulo() {
        assertThrows(NullPointerException.class, () ->
                new ComponenteCombo(null, 1)
        );
    }

    @Test
    void rechazarCantidadCero() {
        Producto producto = crearProducto();

        assertThrows(IllegalArgumentException.class, () ->
                new ComponenteCombo(producto, 0)
        );
    }

    @Test
    void rechazarCantidadNegativa() {
        Producto producto = crearProducto();

        assertThrows(IllegalArgumentException.class, () ->
                new ComponenteCombo(producto, -1)
        );
    }

    @Test
    void aceptarCantidadUno() {
        Producto producto = crearProducto();

        ComponenteCombo componente = new ComponenteCombo(producto, 1);

        assertEquals(1, componente.getCantidad());
    }

    @Test
    void aceptarCantidadMayorQueUno() {
        Producto producto = crearProducto();

        ComponenteCombo componente = new ComponenteCombo(producto, 10);

        assertEquals(10, componente.getCantidad());
    }

    @Test
    void conservarReferenciaDelElemento() {
        Producto producto = crearProducto();

        ComponenteCombo componente = new ComponenteCombo(producto, 3);

        assertSame(producto, componente.getElemento());
        assertInstanceOf(ElementoConfiteria.class, componente.getElemento());
    }

    @Test
    void conservarCantidadOriginal() {
        Producto producto = crearProducto();

        ComponenteCombo componente = new ComponenteCombo(producto, 4);

        assertEquals(4, componente.getCantidad());

        // La cantidad es inmutable y no cambia después de consultar el elemento.
        componente.getElemento().consultarDescripcion();

        assertEquals(4, componente.getCantidad());
    }
}