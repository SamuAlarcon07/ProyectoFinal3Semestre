
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoInventario;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MovimientoInventarioTest {

    private MovimientoInventario crearMovimiento() {
        return new MovimientoInventario(
                "M001",
                TipoMovimientoInventario.ENTRADA,
                20,
                LocalDateTime.of(2026, 5, 10, 10, 30),
                100,
                120,
                "Compra de mercancía"
        );
    }

    @Test
    void crearMovimientoCorrectamente() {
        MovimientoInventario movimiento = crearMovimiento();

        assertEquals("M001", movimiento.getCodigo());
        assertEquals(TipoMovimientoInventario.ENTRADA, movimiento.getTipo());
        assertEquals(20, movimiento.getCantidad());
        assertEquals(LocalDateTime.of(2026, 5, 10, 10, 30),
                movimiento.getFecha());
        assertEquals(100, movimiento.getStockAnterior());
        assertEquals(120, movimiento.getStockResultante());
        assertEquals("Compra de mercancía", movimiento.getObservacion());
    }

    @Test
    void eliminarEspaciosDelCodigo() {
        MovimientoInventario movimiento = new MovimientoInventario(
                " M001 ",
                TipoMovimientoInventario.ENTRADA,
                20,
                LocalDateTime.now(),
                100,
                120,
                "Compra"
        );

        assertEquals("M001", movimiento.getCodigo());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoInventario(
                        null,
                        TipoMovimientoInventario.ENTRADA,
                        20,
                        LocalDateTime.now(),
                        100,
                        120,
                        "Compra"
                )
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoInventario(
                        " ",
                        TipoMovimientoInventario.ENTRADA,
                        20,
                        LocalDateTime.now(),
                        100,
                        120,
                        "Compra"
                )
        );
    }

    @Test
    void rechazarTipoNulo() {
        assertThrows(NullPointerException.class, () ->
                new MovimientoInventario(
                        "M001",
                        null,
                        20,
                        LocalDateTime.now(),
                        100,
                        120,
                        "Compra"
                )
        );
    }

    @Test
    void rechazarCantidadCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoInventario(
                        "M001",
                        TipoMovimientoInventario.ENTRADA,
                        0,
                        LocalDateTime.now(),
                        100,
                        100,
                        "Compra"
                )
        );
    }

    @Test
    void rechazarCantidadNegativa() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoInventario(
                        "M001",
                        TipoMovimientoInventario.ENTRADA,
                        -5,
                        LocalDateTime.now(),
                        100,
                        95,
                        "Compra"
                )
        );
    }

    @Test
    void rechazarFechaNula() {
        assertThrows(NullPointerException.class, () ->
                new MovimientoInventario(
                        "M001",
                        TipoMovimientoInventario.ENTRADA,
                        20,
                        null,
                        100,
                        120,
                        "Compra"
                )
        );
    }

    @Test
    void rechazarStockAnteriorNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoInventario(
                        "M001",
                        TipoMovimientoInventario.ENTRADA,
                        20,
                        LocalDateTime.now(),
                        -1,
                        120,
                        "Compra"
                )
        );
    }

    @Test
    void rechazarStockResultanteNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoInventario(
                        "M001",
                        TipoMovimientoInventario.VENTA,
                        20,
                        LocalDateTime.now(),
                        100,
                        -1,
                        "Venta"
                )
        );
    }

    @Test
    void aceptarObservacionNulaComoTextoVacio() {
        MovimientoInventario movimiento = new MovimientoInventario(
                "M001",
                TipoMovimientoInventario.ENTRADA,
                20,
                LocalDateTime.now(),
                100,
                120,
                null
        );

        assertEquals("", movimiento.getObservacion());
    }

    @Test
    void eliminarEspaciosDeLaObservacion() {
        MovimientoInventario movimiento = new MovimientoInventario(
                "M001",
                TipoMovimientoInventario.ENTRADA,
                20,
                LocalDateTime.now(),
                100,
                120,
                " Compra de mercancía "
        );

        assertEquals("Compra de mercancía", movimiento.getObservacion());
    }

    @Test
    void aceptarDiferentesTiposDeMovimiento() {
        LocalDateTime fecha = LocalDateTime.now();

        for (TipoMovimientoInventario tipo :
                TipoMovimientoInventario.values()) {

            MovimientoInventario movimiento = new MovimientoInventario(
                    "M001",
                    tipo,
                    10,
                    fecha,
                    100,
                    110,
                    "Prueba"
            );

            assertEquals(tipo, movimiento.getTipo());
        }
    }
}