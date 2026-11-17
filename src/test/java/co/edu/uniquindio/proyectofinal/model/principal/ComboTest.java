
package co.edu.uniquindio.proyectofinal.model.principal;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ComboTest {

    private Combo crearCombo() {
        return new Combo(
                "C01",
                "Combo familiar",
                new BigDecimal("25000")
        );
    }

    private Producto crearProducto(String codigo, String nombre) {
        return new Producto(
                codigo,
                nombre,
                "Producto de confitería",
                new BigDecimal("5000"),
                50,
                5,
                "unidad"
        );
    }

    @Test
    void crearComboCorrectamente() {
        Combo combo = crearCombo();

        assertEquals("C01", combo.getCodigo());
        assertEquals("Combo familiar", combo.getNombre());
        assertEquals(new BigDecimal("25000"), combo.getPrecio());
        assertTrue(combo.estaActivo());
        assertTrue(combo.getComponentes().isEmpty());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Combo(null, "Combo", new BigDecimal("10000"))
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Combo(" ", "Combo", new BigDecimal("10000"))
        );
    }

    @Test
    void rechazarNombreNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Combo("C01", null, new BigDecimal("10000"))
        );
    }

    @Test
    void rechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Combo("C01", " ", new BigDecimal("10000"))
        );
    }

    @Test
    void rechazarPrecioNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Combo("C01", "Combo", null)
        );
    }

    @Test
    void rechazarPrecioCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new Combo("C01", "Combo", BigDecimal.ZERO)
        );
    }

    @Test
    void rechazarPrecioNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Combo("C01", "Combo", new BigDecimal("-1000"))
        );
    }

    @Test
    void actualizarNombreCorrectamente() {
        Combo combo = crearCombo();

        combo.actualizarNombre("Combo especial");

        assertEquals("Combo especial", combo.getNombre());
    }

    @Test
    void rechazarActualizacionConNombreVacio() {
        Combo combo = crearCombo();

        assertThrows(IllegalArgumentException.class, () ->
                combo.actualizarNombre(" ")
        );
    }

    @Test
    void actualizarPrecioCorrectamente() {
        Combo combo = crearCombo();

        combo.actualizarPrecio(new BigDecimal("30000"));

        assertEquals(new BigDecimal("30000"), combo.getPrecio());
        assertEquals(new BigDecimal("30000"), combo.consultarPrecio());
    }

    @Test
    void rechazarActualizacionConPrecioNulo() {
        Combo combo = crearCombo();

        assertThrows(IllegalArgumentException.class, () ->
                combo.actualizarPrecio(null)
        );
    }

    @Test
    void activarCombo() {
        Combo combo = crearCombo();

        combo.desactivar();
        combo.activar();

        assertTrue(combo.estaActivo());
    }

    @Test
    void desactivarCombo() {
        Combo combo = crearCombo();

        combo.desactivar();

        assertFalse(combo.estaActivo());
    }

    @Test
    void consultarDescripcion() {
        Combo combo = crearCombo();

        assertEquals("Combo: Combo familiar",
                combo.consultarDescripcion());
    }

    @Test
    void agregarProductoAlCombo() {
        Combo combo = crearCombo();
        Producto producto = crearProducto("P01", "Gaseosa");

        combo.agregarComponente(producto, 2);

        assertEquals(1, combo.getComponentes().size());
        assertSame(producto, combo.getComponentes().get(0).getElemento());
        assertEquals(2, combo.getComponentes().get(0).getCantidad());
    }

    @Test
    void rechazarComponenteNulo() {
        Combo combo = crearCombo();

        assertThrows(IllegalArgumentException.class, () ->
                combo.agregarComponente(null, 1)
        );
    }

    @Test
    void rechazarCantidadCero() {
        Combo combo = crearCombo();
        Producto producto = crearProducto("P01", "Gaseosa");

        assertThrows(IllegalArgumentException.class, () ->
                combo.agregarComponente(producto, 0)
        );
    }

    @Test
    void rechazarCantidadNegativa() {
        Combo combo = crearCombo();
        Producto producto = crearProducto("P01", "Gaseosa");

        assertThrows(IllegalArgumentException.class, () ->
                combo.agregarComponente(producto, -2)
        );
    }

    @Test
    void rechazarComboConsigoMismo() {
        Combo combo = crearCombo();

        assertThrows(IllegalArgumentException.class, () ->
                combo.agregarComponente(combo, 1)
        );
    }

    @Test
    void descomponerComboEnProductosBase() {
        Combo combo = crearCombo();
        Producto gaseosa = crearProducto("P01", "Gaseosa");
        Producto crispetas = crearProducto("P02", "Crispetas");

        combo.agregarComponente(gaseosa, 2);
        combo.agregarComponente(crispetas, 1);

        Map<Producto, Integer> productos = combo.obtenerProductosBase();

        assertEquals(2, productos.size());
        assertEquals(2, productos.get(gaseosa));
        assertEquals(1, productos.get(crispetas));
    }

    @Test
    void multiplicarCantidadesEnCombosAnidados() {
        Combo comboPrincipal = crearCombo();
        Combo comboInterno = new Combo(
                "C02",
                "Combo pequeño",
                new BigDecimal("12000")
        );
        Producto gaseosa = crearProducto("P01", "Gaseosa");

        comboInterno.agregarComponente(gaseosa, 2);
        comboPrincipal.agregarComponente(comboInterno, 3);

        Map<Producto, Integer> productos =
                comboPrincipal.obtenerProductosBase();

        assertEquals(6, productos.get(gaseosa));
    }

    @Test
    void rechazarComposicionCircularEntreCombos() {
        Combo comboA = crearCombo();
        Combo comboB = new Combo(
                "C02",
                "Combo secundario",
                new BigDecimal("15000")
        );

        comboA.agregarComponente(comboB, 1);

        assertThrows(IllegalArgumentException.class, () ->
                comboB.agregarComponente(comboA, 1)
        );
    }

    @Test
    void listaDeComponentesNoPuedeModificarseDesdeFuera() {
        Combo combo = crearCombo();
        combo.agregarComponente(crearProducto("P01", "Gaseosa"), 1);

        assertThrows(UnsupportedOperationException.class, () ->
                combo.getComponentes().clear()
        );
    }

    @Test
    void mapaDeProductosBaseNoPuedeModificarseDesdeFuera() {
        Combo combo = crearCombo();
        Producto producto = crearProducto("P01", "Gaseosa");
        combo.agregarComponente(producto, 1);

        Map<Producto, Integer> productos = combo.obtenerProductosBase();

        assertThrows(UnsupportedOperationException.class, () ->
                productos.put(crearProducto("P02", "Agua"), 5)
        );
    }
}