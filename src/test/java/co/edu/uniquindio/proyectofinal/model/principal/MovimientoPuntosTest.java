package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoPuntos;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MovimientoPuntosTest {

    private MovimientoPuntos crearMovimiento() {
        return new MovimientoPuntos(
                "M001",
                TipoMovimientoPuntos.ACUMULACION,
                100,
                LocalDateTime.of(2026, 5, 10, 10, 30),
                "Acumulación por compra",
                "COMPRA01"
        );
    }

    @Test
    void crearMovimientoCorrectamente() {
        MovimientoPuntos movimiento = crearMovimiento();

        assertEquals("M001", movimiento.getCodigo());
        assertEquals(TipoMovimientoPuntos.ACUMULACION, movimiento.getTipo());
        assertEquals(100, movimiento.getCantidad());
        assertEquals(
                LocalDateTime.of(2026, 5, 10, 10, 30),
                movimiento.getFecha()
        );
        assertEquals("Acumulación por compra", movimiento.getDescripcion());
        assertEquals("COMPRA01", movimiento.getCodigoReferencia());
    }

    @Test
    void eliminarEspaciosDelCodigo() {
        MovimientoPuntos movimiento = new MovimientoPuntos(
                " M001 ",
                TipoMovimientoPuntos.ACUMULACION,
                100,
                LocalDateTime.now(),
                "Acumulación",
                "COMPRA01"
        );

        assertEquals("M001", movimiento.getCodigo());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoPuntos(
                        null,
                        TipoMovimientoPuntos.ACUMULACION,
                        100,
                        LocalDateTime.now(),
                        "Acumulación",
                        "COMPRA01"
                )
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoPuntos(
                        " ",
                        TipoMovimientoPuntos.ACUMULACION,
                        100,
                        LocalDateTime.now(),
                        "Acumulación",
                        "COMPRA01"
                )
        );
    }

    @Test
    void rechazarTipoNulo() {
        assertThrows(NullPointerException.class, () ->
                new MovimientoPuntos(
                        "M001",
                        null,
                        100,
                        LocalDateTime.now(),
                        "Acumulación",
                        "COMPRA01"
                )
        );
    }

    @Test
    void rechazarCantidadCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoPuntos(
                        "M001",
                        TipoMovimientoPuntos.ACUMULACION,
                        0,
                        LocalDateTime.now(),
                        "Acumulación",
                        "COMPRA01"
                )
        );
    }

    @Test
    void rechazarCantidadNegativa() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoPuntos(
                        "M001",
                        TipoMovimientoPuntos.REDENCION,
                        -10,
                        LocalDateTime.now(),
                        "Redención",
                        "COMPRA01"
                )
        );
    }

    @Test
    void rechazarFechaNula() {
        assertThrows(NullPointerException.class, () ->
                new MovimientoPuntos(
                        "M001",
                        TipoMovimientoPuntos.ACUMULACION,
                        100,
                        null,
                        "Acumulación",
                        "COMPRA01"
                )
        );
    }

    @Test
    void aceptarDescripcionNulaComoTextoVacio() {
        MovimientoPuntos movimiento = new MovimientoPuntos(
                "M001",
                TipoMovimientoPuntos.ACUMULACION,
                100,
                LocalDateTime.now(),
                null,
                "COMPRA01"
        );

        assertEquals("", movimiento.getDescripcion());
    }

    @Test
    void eliminarEspaciosDeLaDescripcion() {
        MovimientoPuntos movimiento = new MovimientoPuntos(
                "M001",
                TipoMovimientoPuntos.ACUMULACION,
                100,
                LocalDateTime.now(),
                " Acumulación por compra ",
                "COMPRA01"
        );

        assertEquals("Acumulación por compra", movimiento.getDescripcion());
    }

    @Test
    void aceptarReferenciaNulaComoTextoVacio() {
        MovimientoPuntos movimiento = new MovimientoPuntos(
                "M001",
                TipoMovimientoPuntos.AJUSTE,
                50,
                LocalDateTime.now(),
                "Ajuste de puntos",
                null
        );

        assertEquals("", movimiento.getCodigoReferencia());
    }

    @Test
    void eliminarEspaciosDelCodigoReferencia() {
        MovimientoPuntos movimiento = new MovimientoPuntos(
                "M001",
                TipoMovimientoPuntos.REDENCION,
                30,
                LocalDateTime.now(),
                "Redención",
                " COMPRA01 "
        );

        assertEquals("COMPRA01", movimiento.getCodigoReferencia());
    }

    @Test
    void aceptarTodosLosTiposDeMovimiento() {
        for (TipoMovimientoPuntos tipo : TipoMovimientoPuntos.values()) {
            MovimientoPuntos movimiento = new MovimientoPuntos(
                    "M001",
                    tipo,
                    10,
                    LocalDateTime.now(),
                    "Movimiento de prueba",
                    "REF01"
            );

            assertEquals(tipo, movimiento.getTipo());
        }
    }
}