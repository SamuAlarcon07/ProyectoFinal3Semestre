package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoInventario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InventarioTest {

    private Inventario crearInventario() {
        return new Inventario(100, 10, "unidades");
    }

    @Test
    void crearInventarioCorrectamente() {
        Inventario inventario = crearInventario();

        assertEquals(100, inventario.getStockActual());
        assertEquals(10, inventario.getStockMinimo());
        assertEquals("unidades", inventario.getUnidadMedida());
        assertNotNull(inventario.getFechaActualizacion());
        assertTrue(inventario.getMovimientos().isEmpty());
    }

    @Test
    void rechazarStockInicialNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Inventario(-1, 10, "unidades")
        );
    }

    @Test
    void rechazarStockMinimoNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Inventario(100, -1, "unidades")
        );
    }

    @Test
    void rechazarUnidadMedidaNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Inventario(100, 10, null)
        );
    }

    @Test
    void rechazarUnidadMedidaVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                new Inventario(100, 10, " ")
        );
    }

    @Test
    void eliminarEspaciosDeUnidadMedida() {
        Inventario inventario = new Inventario(100, 10, " unidades ");

        assertEquals("unidades", inventario.getUnidadMedida());
    }

    @Test
    void registrarEntradaCorrectamente() {
        Inventario inventario = crearInventario();

        inventario.registrarEntrada(20, "Compra de mercancía");

        assertEquals(120, inventario.getStockActual());
        assertEquals(1, inventario.getMovimientos().size());
    }

    @Test
    void registrarEntradaGuardaDatosDelMovimiento() {
        Inventario inventario = crearInventario();

        inventario.registrarEntrada(20, "Reposición");

        MovimientoInventario movimiento =
                inventario.getMovimientos().get(0);

        assertEquals(TipoMovimientoInventario.ENTRADA, movimiento.getTipo());
        assertEquals(20, movimiento.getCantidad());
        assertEquals(100, movimiento.getStockAnterior());
        assertEquals(120, movimiento.getStockResultante());
        assertEquals("Reposición", movimiento.getObservacion());
    }

    @Test
    void rechazarEntradaConCantidadCero() {
        Inventario inventario = crearInventario();

        assertThrows(IllegalArgumentException.class, () ->
                inventario.registrarEntrada(0, "Entrada")
        );
    }

    @Test
    void rechazarEntradaConCantidadNegativa() {
        Inventario inventario = crearInventario();

        assertThrows(IllegalArgumentException.class, () ->
                inventario.registrarEntrada(-5, "Entrada")
        );
    }

    @Test
    void registrarVentaCorrectamente() {
        Inventario inventario = crearInventario();

        inventario.registrarVenta(30, "Venta de productos");

        assertEquals(70, inventario.getStockActual());
    }

    @Test
    void registrarVentaGuardaDatosDelMovimiento() {
        Inventario inventario = crearInventario();

        inventario.registrarVenta(25, "Venta");

        MovimientoInventario movimiento =
                inventario.getMovimientos().get(0);

        assertEquals(TipoMovimientoInventario.VENTA, movimiento.getTipo());
        assertEquals(25, movimiento.getCantidad());
        assertEquals(100, movimiento.getStockAnterior());
        assertEquals(75, movimiento.getStockResultante());
    }

    @Test
    void rechazarVentaConStockInsuficiente() {
        Inventario inventario = crearInventario();

        assertThrows(IllegalStateException.class, () ->
                inventario.registrarVenta(150, "Venta")
        );

        assertEquals(100, inventario.getStockActual());
        assertTrue(inventario.getMovimientos().isEmpty());
    }

    @Test
    void registrarMermaCorrectamente() {
        Inventario inventario = crearInventario();

        inventario.registrarMerma(15, "Productos dañados");

        assertEquals(85, inventario.getStockActual());
    }

    @Test
    void registrarMermaGuardaTipoCorrecto() {
        Inventario inventario = crearInventario();

        inventario.registrarMerma(10, "Merma");

        assertEquals(
                TipoMovimientoInventario.MERMA,
                inventario.getMovimientos().get(0).getTipo()
        );
    }

    @Test
    void rechazarMermaConStockInsuficiente() {
        Inventario inventario = crearInventario();

        assertThrows(IllegalStateException.class, () ->
                inventario.registrarMerma(200, "Merma")
        );

        assertEquals(100, inventario.getStockActual());
    }

    @Test
    void ajustarExistenciasAumentandoStock() {
        Inventario inventario = crearInventario();

        inventario.ajustarExistencias(150, "Conteo físico");

        assertEquals(150, inventario.getStockActual());
        assertEquals(50,
                inventario.getMovimientos().get(0).getCantidad());
        assertEquals(
                TipoMovimientoInventario.AJUSTE,
                inventario.getMovimientos().get(0).getTipo()
        );
    }

    @Test
    void ajustarExistenciasDisminuyendoStock() {
        Inventario inventario = crearInventario();

        inventario.ajustarExistencias(80, "Corrección de inventario");

        assertEquals(80, inventario.getStockActual());
        assertEquals(20,
                inventario.getMovimientos().get(0).getCantidad());
    }

    @Test
    void rechazarAjusteConStockNegativo() {
        Inventario inventario = crearInventario();

        assertThrows(IllegalArgumentException.class, () ->
                inventario.ajustarExistencias(-5, "Ajuste")
        );
    }

    @Test
    void rechazarAjusteSinCambios() {
        Inventario inventario = crearInventario();

        assertThrows(IllegalArgumentException.class, () ->
                inventario.ajustarExistencias(100, "Sin cambios")
        );
    }

    @Test
    void verificarDisponibilidadCorrectamente() {
        Inventario inventario = crearInventario();

        assertTrue(inventario.verificarDisponibilidad(50));
        assertTrue(inventario.verificarDisponibilidad(100));
        assertFalse(inventario.verificarDisponibilidad(101));
        assertFalse(inventario.verificarDisponibilidad(0));
        assertFalse(inventario.verificarDisponibilidad(-1));
    }

    @Test
    void verificarNecesidadDeReposicion() {
        Inventario inventario = crearInventario();

        assertFalse(inventario.necesitaReposicion());

        inventario.registrarVenta(90, "Venta");

        assertTrue(inventario.necesitaReposicion());
    }

    @Test
    void actualizarStockMinimoCorrectamente() {
        Inventario inventario = crearInventario();

        inventario.actualizarStockMinimo(20);

        assertEquals(20, inventario.getStockMinimo());
    }

    @Test
    void rechazarStockMinimoNegativoAlActualizar() {
        Inventario inventario = crearInventario();

        assertThrows(IllegalArgumentException.class, () ->
                inventario.actualizarStockMinimo(-1)
        );
    }

    @Test
    void actualizarFechaAlRegistrarMovimiento() {
        Inventario inventario = crearInventario();
        LocalDateTime fechaAnterior = inventario.getFechaActualizacion();

        inventario.registrarEntrada(10, "Reposición");

        assertNotNull(inventario.getFechaActualizacion());
        assertFalse(inventario.getFechaActualizacion().isBefore(fechaAnterior));
    }

    @Test
    void listaDeMovimientosNoPuedeModificarseDesdeFuera() {
        Inventario inventario = crearInventario();
        inventario.registrarEntrada(10, "Reposición");

        assertThrows(UnsupportedOperationException.class, () ->
                inventario.getMovimientos().clear()
        );
    }
}