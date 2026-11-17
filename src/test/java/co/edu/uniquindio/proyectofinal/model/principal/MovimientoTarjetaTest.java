
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoTarjeta;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MovimientoTarjetaTest {

    private MovimientoTarjeta crearMovimiento() {
        return new MovimientoTarjeta(
                "MT001",
                TipoMovimientoTarjeta.RECARGA,
                new BigDecimal("50000"),
                LocalDateTime.of(2026, 5, 10, 10, 30),
                new BigDecimal("100000"),
                "REC001",
                "USR001"
        );
    }

    @Test
    void crearMovimientoCorrectamente() {
        MovimientoTarjeta movimiento = crearMovimiento();

        assertEquals("MT001", movimiento.getCodigo());
        assertEquals(TipoMovimientoTarjeta.RECARGA, movimiento.getTipo());
        assertEquals(new BigDecimal("50000"), movimiento.getValor());
        assertEquals(
                LocalDateTime.of(2026, 5, 10, 10, 30),
                movimiento.getFecha()
        );
        assertEquals(new BigDecimal("100000"), movimiento.getSaldoResultante());
        assertEquals("REC001", movimiento.getReferenciaOperacion());
        assertEquals("USR001", movimiento.getResponsableId());
    }

    @Test
    void eliminarEspaciosDelCodigo() {
        MovimientoTarjeta movimiento = new MovimientoTarjeta(
                " MT001 ",
                TipoMovimientoTarjeta.RECARGA,
                new BigDecimal("50000"),
                LocalDateTime.now(),
                new BigDecimal("100000"),
                "REC001",
                "USR001"
        );

        assertEquals("MT001", movimiento.getCodigo());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoTarjeta(
                        null,
                        TipoMovimientoTarjeta.RECARGA,
                        new BigDecimal("50000"),
                        LocalDateTime.now(),
                        new BigDecimal("100000"),
                        "REC001",
                        "USR001"
                )
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoTarjeta(
                        " ",
                        TipoMovimientoTarjeta.RECARGA,
                        new BigDecimal("50000"),
                        LocalDateTime.now(),
                        new BigDecimal("100000"),
                        "REC001",
                        "USR001"
                )
        );
    }

    @Test
    void rechazarTipoNulo() {
        assertThrows(NullPointerException.class, () ->
                new MovimientoTarjeta(
                        "MT001",
                        null,
                        new BigDecimal("50000"),
                        LocalDateTime.now(),
                        new BigDecimal("100000"),
                        "REC001",
                        "USR001"
                )
        );
    }

    @Test
    void rechazarValorNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoTarjeta(
                        "MT001",
                        TipoMovimientoTarjeta.RECARGA,
                        null,
                        LocalDateTime.now(),
                        new BigDecimal("100000"),
                        "REC001",
                        "USR001"
                )
        );
    }

    @Test
    void rechazarValorCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoTarjeta(
                        "MT001",
                        TipoMovimientoTarjeta.RECARGA,
                        BigDecimal.ZERO,
                        LocalDateTime.now(),
                        new BigDecimal("100000"),
                        "REC001",
                        "USR001"
                )
        );
    }

    @Test
    void rechazarValorNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoTarjeta(
                        "MT001",
                        TipoMovimientoTarjeta.PAGO,
                        new BigDecimal("-1000"),
                        LocalDateTime.now(),
                        new BigDecimal("100000"),
                        "PAG001",
                        "USR001"
                )
        );
    }

    @Test
    void rechazarSaldoResultanteNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoTarjeta(
                        "MT001",
                        TipoMovimientoTarjeta.RECARGA,
                        new BigDecimal("50000"),
                        LocalDateTime.now(),
                        null,
                        "REC001",
                        "USR001"
                )
        );
    }

    @Test
    void rechazarSaldoResultanteNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new MovimientoTarjeta(
                        "MT001",
                        TipoMovimientoTarjeta.PAGO,
                        new BigDecimal("50000"),
                        LocalDateTime.now(),
                        new BigDecimal("-1000"),
                        "PAG001",
                        "USR001"
                )
        );
    }

    @Test
    void aceptarSaldoResultanteCero() {
        MovimientoTarjeta movimiento = new MovimientoTarjeta(
                "MT001",
                TipoMovimientoTarjeta.PAGO,
                new BigDecimal("50000"),
                LocalDateTime.now(),
                BigDecimal.ZERO,
                "PAG001",
                "USR001"
        );

        assertEquals(0, BigDecimal.ZERO.compareTo(
                movimiento.getSaldoResultante()
        ));
    }

    @Test
    void consultarValorConSignoDePago() {
        MovimientoTarjeta movimiento = new MovimientoTarjeta(
                "MT001",
                TipoMovimientoTarjeta.PAGO,
                new BigDecimal("20000"),
                LocalDateTime.now(),
                new BigDecimal("30000"),
                "PAG001",
                "USR001"
        );

        assertEquals(
                0,
                new BigDecimal("-20000").compareTo(
                        movimiento.consultarValorConSigno()
                )
        );
    }

    @Test
    void consultarValorConSignoDeRecargaYReembolso() {
        MovimientoTarjeta recarga = new MovimientoTarjeta(
                "MT001",
                TipoMovimientoTarjeta.RECARGA,
                new BigDecimal("50000"),
                LocalDateTime.now(),
                new BigDecimal("100000"),
                "REC001",
                "USR001"
        );

        MovimientoTarjeta reembolso = new MovimientoTarjeta(
                "MT002",
                TipoMovimientoTarjeta.REEMBOLSO,
                new BigDecimal("10000"),
                LocalDateTime.now(),
                new BigDecimal("110000"),
                "REE001",
                "USR001"
        );

        assertEquals(
                0,
                new BigDecimal("50000").compareTo(
                        recarga.consultarValorConSigno()
                )
        );

        assertEquals(
                0,
                new BigDecimal("10000").compareTo(
                        reembolso.consultarValorConSigno()
                )
        );
    }

    @Test
    void aceptarReferenciasNulasComoTextoVacio() {
        MovimientoTarjeta movimiento = new MovimientoTarjeta(
                "MT001",
                TipoMovimientoTarjeta.RECARGA,
                new BigDecimal("50000"),
                LocalDateTime.now(),
                new BigDecimal("50000"),
                null,
                null
        );

        assertEquals("", movimiento.getReferenciaOperacion());
        assertEquals("", movimiento.getResponsableId());
    }

    @Test
    void rechazarFechaNula() {
        assertThrows(NullPointerException.class, () ->
                new MovimientoTarjeta(
                        "MT001",
                        TipoMovimientoTarjeta.RECARGA,
                        new BigDecimal("50000"),
                        null,
                        new BigDecimal("50000"),
                        "REC001",
                        "USR001"
                )
        );
    }
}