package co.edu.uniquindio.proyectofinal.model.principal;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class LotePuntosTest {

    private LotePuntos crearLote() {
        return new LotePuntos(
                "L001",
                100,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1)
        );
    }

    @Test
    void crearLoteCorrectamente() {
        LotePuntos lote = crearLote();

        assertEquals("L001", lote.getCodigo());
        assertEquals(100, lote.getCantidadOriginal());
        assertEquals(100, lote.getCantidadDisponible());
        assertEquals(LocalDate.of(2026, 1, 1), lote.getFechaObtencion());
        assertEquals(LocalDate.of(2027, 1, 1), lote.getFechaVencimiento());
    }

    @Test
    void eliminarEspaciosDelCodigo() {
        LotePuntos lote = new LotePuntos(
                " L001 ",
                100,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2027, 1, 1)
        );

        assertEquals("L001", lote.getCodigo());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new LotePuntos(
                        null, 100,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2027, 1, 1)
                )
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new LotePuntos(
                        " ", 100,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2027, 1, 1)
                )
        );
    }

    @Test
    void rechazarCantidadCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new LotePuntos(
                        "L001", 0,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2027, 1, 1)
                )
        );
    }

    @Test
    void rechazarCantidadNegativa() {
        assertThrows(IllegalArgumentException.class, () ->
                new LotePuntos(
                        "L001", -10,
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2027, 1, 1)
                )
        );
    }

    @Test
    void rechazarFechaObtencionNula() {
        assertThrows(NullPointerException.class, () ->
                new LotePuntos(
                        "L001", 100, null,
                        LocalDate.of(2027, 1, 1)
                )
        );
    }

    @Test
    void rechazarFechaVencimientoNula() {
        assertThrows(NullPointerException.class, () ->
                new LotePuntos(
                        "L001", 100,
                        LocalDate.of(2026, 1, 1), null
                )
        );
    }

    @Test
    void rechazarVencimientoIgualALaObtencion() {
        LocalDate fecha = LocalDate.of(2026, 1, 1);

        assertThrows(IllegalArgumentException.class, () ->
                new LotePuntos("L001", 100, fecha, fecha)
        );
    }

    @Test
    void rechazarVencimientoAnteriorALaObtencion() {
        assertThrows(IllegalArgumentException.class, () ->
                new LotePuntos(
                        "L001",
                        100,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 1)
                )
        );
    }

    @Test
    void loteNoEstaVencidoAntesDeLaFechaDeVencimiento() {
        LotePuntos lote = crearLote();

        assertFalse(lote.estaVencido(LocalDate.of(2026, 12, 31)));
    }

    @Test
    void loteNoEstaVencidoEnLaFechaExactaDeVencimiento() {
        LotePuntos lote = crearLote();

        assertFalse(lote.estaVencido(LocalDate.of(2027, 1, 1)));
    }

    @Test
    void loteEstaVencidoDespuesDeLaFechaDeVencimiento() {
        LotePuntos lote = crearLote();

        assertTrue(lote.estaVencido(LocalDate.of(2027, 1, 2)));
    }

    @Test
    void fechaConsultaNulaNoMarcaLoteComoVencido() {
        LotePuntos lote = crearLote();

        assertFalse(lote.estaVencido(null));
    }

    @Test
    void utilizarPuntosCorrectamente() {
        LotePuntos lote = crearLote();

        lote.utilizarPuntos(30, LocalDate.of(2026, 6, 1));

        assertEquals(70, lote.getCantidadDisponible());
        assertEquals(100, lote.getCantidadOriginal());
    }

    @Test
    void rechazarUtilizarCantidadCero() {
        LotePuntos lote = crearLote();

        assertThrows(IllegalArgumentException.class, () ->
                lote.utilizarPuntos(0, LocalDate.of(2026, 6, 1))
        );
    }

    @Test
    void rechazarUtilizarCantidadNegativa() {
        LotePuntos lote = crearLote();

        assertThrows(IllegalArgumentException.class, () ->
                lote.utilizarPuntos(-5, LocalDate.of(2026, 6, 1))
        );
    }

    @Test
    void rechazarUtilizarMasPuntosDeLosDisponibles() {
        LotePuntos lote = crearLote();

        assertThrows(IllegalArgumentException.class, () ->
                lote.utilizarPuntos(150, LocalDate.of(2026, 6, 1))
        );

        assertEquals(100, lote.getCantidadDisponible());
    }

    @Test
    void rechazarUtilizarPuntosDeLoteVencido() {
        LotePuntos lote = crearLote();

        assertThrows(IllegalStateException.class, () ->
                lote.utilizarPuntos(10, LocalDate.of(2027, 1, 2))
        );
    }

    @Test
    void procesarVencimientoDeLote() {
        LotePuntos lote = crearLote();

        int vencidos = lote.procesarVencimiento(
                LocalDate.of(2027, 1, 2)
        );

        assertEquals(100, vencidos);
        assertEquals(0, lote.getCantidadDisponible());
    }

    @Test
    void noProcesarVencimientoAntesDeLaFecha() {
        LotePuntos lote = crearLote();

        int vencidos = lote.procesarVencimiento(
                LocalDate.of(2026, 12, 31)
        );

        assertEquals(0, vencidos);
        assertEquals(100, lote.getCantidadDisponible());
    }

    @Test
    void procesarVencimientoSoloUnaVez() {
        LotePuntos lote = crearLote();
        LocalDate fecha = LocalDate.of(2027, 1, 2);

        assertEquals(100, lote.procesarVencimiento(fecha));
        assertEquals(0, lote.procesarVencimiento(fecha));
        assertEquals(0, lote.getCantidadDisponible());
    }

    @Test
    void vencerSoloLosPuntosRestantes() {
        LotePuntos lote = crearLote();

        lote.utilizarPuntos(40, LocalDate.of(2026, 6, 1));

        int vencidos = lote.procesarVencimiento(
                LocalDate.of(2027, 1, 2)
        );

        assertEquals(60, vencidos);
        assertEquals(0, lote.getCantidadDisponible());
        assertEquals(100, lote.getCantidadOriginal());
    }
}