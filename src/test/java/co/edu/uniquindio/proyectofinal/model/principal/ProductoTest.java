
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoProducto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    private Producto producto;

    @BeforeEach
    void setUp() {
        producto = new Producto(
                "P001",
                "Gaseosa",
                "Bebida de 400 ml",
                new BigDecimal("5000"),
                20,
                5,
                "unidades"
        );
    }

    @Test
    void constructorDebeCrearProductoCorrectamente() {
        assertEquals("P001", producto.getCodigo());
        assertEquals("Gaseosa", producto.getNombre());
        assertEquals("Bebida de 400 ml", producto.getDescripcion());
        assertEquals(new BigDecimal("5000"), producto.getPrecio());
        assertEquals(EstadoProducto.ACTIVO, producto.getEstado());
        assertNotNull(producto.getInventario());
    }

    @Test
    void constructorDebeCrearInventarioConExistenciasIniciales() {
        assertEquals(20, producto.getInventario().getStockActual());
        assertEquals(5, producto.getInventario().getStockMinimo());
        assertEquals("unidades", producto.getInventario().getUnidadMedida());
    }

    @Test
    void constructorDebeRechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto(null, "Gaseosa", "Bebida",
                        new BigDecimal("5000"), 20, 5, "unidades"));
    }

    @Test
    void constructorDebeRechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto(" ", "Gaseosa", "Bebida",
                        new BigDecimal("5000"), 20, 5, "unidades"));
    }

    @Test
    void constructorDebeRechazarNombreNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", null, "Bebida",
                        new BigDecimal("5000"), 20, 5, "unidades"));
    }

    @Test
    void constructorDebeRechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", " ", "Bebida",
                        new BigDecimal("5000"), 20, 5, "unidades"));
    }

    @Test
    void constructorDebeRechazarDescripcionNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", "Gaseosa", null,
                        new BigDecimal("5000"), 20, 5, "unidades"));
    }

    @Test
    void constructorDebeRechazarDescripcionVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", "Gaseosa", " ",
                        new BigDecimal("5000"), 20, 5, "unidades"));
    }

    @Test
    void constructorDebeRechazarPrecioNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", "Gaseosa", "Bebida",
                        null, 20, 5, "unidades"));
    }

    @Test
    void constructorDebeRechazarPrecioCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", "Gaseosa", "Bebida",
                        BigDecimal.ZERO, 20, 5, "unidades"));
    }

    @Test
    void constructorDebeRechazarPrecioNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", "Gaseosa", "Bebida",
                        new BigDecimal("-100"), 20, 5, "unidades"));
    }

    @Test
    void constructorDebeRechazarStockInicialNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", "Gaseosa", "Bebida",
                        new BigDecimal("5000"), -1, 5, "unidades"));
    }

    @Test
    void constructorDebeRechazarStockMinimoNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", "Gaseosa", "Bebida",
                        new BigDecimal("5000"), 20, -1, "unidades"));
    }

    @Test
    void constructorDebeRechazarUnidadMedidaNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", "Gaseosa", "Bebida",
                        new BigDecimal("5000"), 20, 5, null));
    }

    @Test
    void constructorDebeRechazarUnidadMedidaVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                new Producto("P001", "Gaseosa", "Bebida",
                        new BigDecimal("5000"), 20, 5, " "));
    }

    @Test
    void constructorDebeEliminarEspaciosDeTextos() {
        Producto resultado = new Producto(
                " P002 ", " Jugo ", " Natural ",
                new BigDecimal("3000"), 10, 2, " unidades "
        );

        assertEquals("P002", resultado.getCodigo());
        assertEquals("Jugo", resultado.getNombre());
        assertEquals("Natural", resultado.getDescripcion());
        assertEquals("unidades", resultado.getInventario().getUnidadMedida());
    }

    @Test
    void actualizarInformacionDebeCambiarLosDatos() {
        producto.actualizarInformacion(
                "Agua",
                "Botella de 600 ml",
                new BigDecimal("2500")
        );

        assertEquals("Agua", producto.getNombre());
        assertEquals("Botella de 600 ml", producto.getDescripcion());
        assertEquals(new BigDecimal("2500"), producto.getPrecio());
    }

    @Test
    void actualizarInformacionDebeRechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                producto.actualizarInformacion(
                        " ", "Descripción", new BigDecimal("1000")));
    }

    @Test
    void actualizarInformacionDebeRechazarDescripcionVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                producto.actualizarInformacion(
                        "Nombre", " ", new BigDecimal("1000")));
    }

    @Test
    void actualizarInformacionDebeRechazarPrecioNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                producto.actualizarInformacion("Nombre", "Descripción", null));
    }

    @Test
    void actualizarInformacionDebeRechazarPrecioCero() {
        assertThrows(IllegalArgumentException.class, () ->
                producto.actualizarInformacion(
                        "Nombre", "Descripción", BigDecimal.ZERO));
    }

    @Test
    void actualizarInformacionDebeRechazarPrecioNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                producto.actualizarInformacion(
                        "Nombre", "Descripción", new BigDecimal("-1")));
    }

    @Test
    void activarDebeDejarProductoActivo() {
        producto.desactivar();

        producto.activar();

        assertTrue(producto.estaActivo());
        assertEquals(EstadoProducto.ACTIVO, producto.getEstado());
    }

    @Test
    void desactivarDebeDejarProductoInactivo() {
        producto.desactivar();

        assertFalse(producto.estaActivo());
        assertEquals(EstadoProducto.INACTIVO, producto.getEstado());
    }

    @Test
    void estaDisponibleDebeRetornarTrueCuandoHayStock() {
        assertTrue(producto.estaDisponible(5));
    }

    @Test
    void estaDisponibleDebeRetornarFalseCuandoNoHayStockSuficiente() {
        assertFalse(producto.estaDisponible(25));
    }

    @Test
    void estaDisponibleDebeRetornarFalseCuandoLaCantidadEsCero() {
        assertFalse(producto.estaDisponible(0));
    }

    @Test
    void estaDisponibleDebeRetornarFalseCuandoLaCantidadEsNegativa() {
        assertFalse(producto.estaDisponible(-1));
    }

    @Test
    void productoInactivoNoDebeEstarDisponible() {
        producto.desactivar();

        assertFalse(producto.estaDisponible(1));
    }

    @Test
    void consultarPrecioDebeRetornarElPrecio() {
        assertEquals(new BigDecimal("5000"), producto.consultarPrecio());
    }

    @Test
    void consultarDescripcionDebeCombinarNombreYDescripcion() {
        assertEquals(
                "Gaseosa - Bebida de 400 ml",
                producto.consultarDescripcion()
        );
    }

    @Test
    void obtenerProductosBaseDebeRetornarElMismoProducto() {
        Map<Producto, Integer> productos = producto.obtenerProductosBase();

        assertEquals(1, productos.size());
        assertEquals(Integer.valueOf(1), productos.get(producto));
    }

    @Test
    void obtenerProductosBaseDebeRetornarUnMapaInmodificable() {
        Map<Producto, Integer> productos = producto.obtenerProductosBase();

        assertThrows(UnsupportedOperationException.class, () ->
                productos.put(producto, 2));
    }
}
