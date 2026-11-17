
package co.edu.uniquindio.proyectofinal.model.principal;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DetalleCompraTest {

    private Producto crearProducto(String precio) {
        return new Producto(
                "P01",
                "Gaseosa",
                "Gaseosa personal",
                new BigDecimal(precio),
                50,
                5,
                "unidad"
        );
    }

    private void assertDecimalEquals(String esperado, BigDecimal actual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(actual));
    }

    @Test
    void crearDetalleCorrectamente() {
        Producto producto = crearProducto("5000");

        DetalleCompra detalle = new DetalleCompra("D01", producto, 2);

        assertEquals("D01", detalle.getCodigo());
        assertSame(producto, detalle.getElemento());
        assertEquals(2, detalle.getCantidad());
        assertDecimalEquals("5000", detalle.getPrecioUnitario());
        assertDecimalEquals("10000", detalle.getSubtotal());
    }

    @Test
    void eliminarEspaciosDelCodigo() {
        DetalleCompra detalle = new DetalleCompra(
                " D01 ",
                crearProducto("5000"),
                1
        );

        assertEquals("D01", detalle.getCodigo());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new DetalleCompra(null, crearProducto("5000"), 1)
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new DetalleCompra(" ", crearProducto("5000"), 1)
        );
    }

    @Test
    void rechazarElementoNulo() {
        assertThrows(NullPointerException.class, () ->
                new DetalleCompra("D01", null, 1)
        );
    }

    @Test
    void rechazarCantidadCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new DetalleCompra("D01", crearProducto("5000"), 0)
        );
    }

    @Test
    void rechazarCantidadNegativa() {
        assertThrows(IllegalArgumentException.class, () ->
                new DetalleCompra("D01", crearProducto("5000"), -2)
        );
    }

    @Test
    void calcularSubtotalConCantidadUno() {
        DetalleCompra detalle = new DetalleCompra(
                "D01",
                crearProducto("5000"),
                1
        );

        assertDecimalEquals("5000", detalle.calcularSubtotal());
    }

    @Test
    void calcularSubtotalConVariasUnidades() {
        DetalleCompra detalle = new DetalleCompra(
                "D01",
                crearProducto("3500"),
                4
        );

        assertDecimalEquals("14000", detalle.calcularSubtotal());
    }

    @Test
    void conservarPrecioUnitarioOriginal() {
        Producto producto = crearProducto("5000");
        DetalleCompra detalle = new DetalleCompra("D01", producto, 2);

        producto.actualizarInformacion(
                "Gaseosa",
                "Gaseosa personal",
                new BigDecimal("7000")
        );

        assertDecimalEquals("5000", detalle.getPrecioUnitario());
        assertDecimalEquals("10000", detalle.getSubtotal());
    }

    @Test
    void subtotalEsIgualAlCalculoInicial() {
        DetalleCompra detalle = new DetalleCompra(
                "D01",
                crearProducto("2500"),
                3
        );

        assertDecimalEquals("7500", detalle.getSubtotal());
        assertDecimalEquals("7500", detalle.calcularSubtotal());
    }

    @Test
    void gettersConservanLosDatos() {
        Producto producto = crearProducto("6000");
        DetalleCompra detalle = new DetalleCompra("D02", producto, 5);

        assertEquals("D02", detalle.getCodigo());
        assertSame(producto, detalle.getElemento());
        assertEquals(5, detalle.getCantidad());
        assertDecimalEquals("6000", detalle.getPrecioUnitario());
        assertDecimalEquals("30000", detalle.getSubtotal());
    }

    @Test
    void subtotalNoCambiaAlConsultarVariasVeces() {
        DetalleCompra detalle = new DetalleCompra(
                "D01",
                crearProducto("4000"),
                3
        );

        assertDecimalEquals("12000", detalle.getSubtotal());
        assertDecimalEquals("12000", detalle.getSubtotal());
        assertDecimalEquals("12000", detalle.calcularSubtotal());
    }
}