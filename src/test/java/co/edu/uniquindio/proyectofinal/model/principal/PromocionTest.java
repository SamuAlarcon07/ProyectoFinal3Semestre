package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoPromocion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PromocionTest {

    private Promocion promocion;
    private LocalDate hoy;

    @BeforeEach
    void setUp() {
        hoy = LocalDate.now();

        promocion = new Promocion(
                "PROMO01",
                "Descuento especial",
                "Promoción de prueba",
                new BigDecimal("20"),
                hoy.minusDays(5),
                hoy.plusDays(5)
        );
    }

    @Test
    void constructorDebeCrearPromocionCorrectamente() {
        assertEquals("PROMO01", promocion.getCodigo());
        assertEquals("Descuento especial", promocion.getNombre());
        assertEquals("Promoción de prueba", promocion.getDescripcion());
        assertEquals(new BigDecimal("20"), promocion.getPorcentajeDescuento());
        assertEquals(hoy.minusDays(5), promocion.getFechaInicio());
        assertEquals(hoy.plusDays(5), promocion.getFechaFin());
        assertEquals(EstadoPromocion.ACTIVA, promocion.getEstado());
    }

    @Test
    void constructorDebeEliminarEspaciosDelCodigo() {
        Promocion resultado = new Promocion(
                " PROMO02 ",
                "Nombre",
                "Descripción",
                new BigDecimal("10"),
                hoy,
                hoy.plusDays(2)
        );

        assertEquals("PROMO02", resultado.getCodigo());
    }

    @Test
    void constructorDebeRechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        null, "Nombre", "Descripción",
                        new BigDecimal("10"), hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeRechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        " ", "Nombre", "Descripción",
                        new BigDecimal("10"), hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeRechazarNombreNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        "P01", null, "Descripción",
                        new BigDecimal("10"), hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeRechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        "P01", " ", "Descripción",
                        new BigDecimal("10"), hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeRechazarDescripcionNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        "P01", "Nombre", null,
                        new BigDecimal("10"), hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeRechazarDescripcionVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        "P01", "Nombre", " ",
                        new BigDecimal("10"), hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeRechazarDescuentoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        "P01", "Nombre", "Descripción",
                        null, hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeRechazarDescuentoCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        "P01", "Nombre", "Descripción",
                        BigDecimal.ZERO, hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeRechazarDescuentoNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        "P01", "Nombre", "Descripción",
                        new BigDecimal("-5"), hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeRechazarDescuentoMayorA100() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        "P01", "Nombre", "Descripción",
                        new BigDecimal("101"), hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeAceptarDescuentoDe100() {
        Promocion resultado = new Promocion(
                "P01", "Nombre", "Descripción",
                new BigDecimal("100"), hoy, hoy.plusDays(2)
        );

        assertEquals(new BigDecimal("100"), resultado.getPorcentajeDescuento());
    }

    @Test
    void constructorDebeRechazarFechaInicioNula() {
        assertThrows(NullPointerException.class, () ->
                new Promocion(
                        "P01", "Nombre", "Descripción",
                        new BigDecimal("10"), null, hoy.plusDays(2)
                ));
    }

    @Test
    void constructorDebeRechazarFechaFinNula() {
        assertThrows(NullPointerException.class, () ->
                new Promocion(
                        "P01", "Nombre", "Descripción",
                        new BigDecimal("10"), hoy, null
                ));
    }

    @Test
    void constructorDebeRechazarFechaFinAnteriorAInicio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Promocion(
                        "P01", "Nombre", "Descripción",
                        new BigDecimal("10"), hoy.plusDays(3), hoy
                ));
    }

    @Test
    void promocionFuturaDebeQuedarProgramada() {
        Promocion resultado = new Promocion(
                "P01", "Nombre", "Descripción",
                new BigDecimal("10"), hoy.plusDays(2), hoy.plusDays(5)
        );

        assertEquals(EstadoPromocion.PROGRAMADA, resultado.getEstado());
    }

    @Test
    void promocionVencidaDebeQuedarFinalizada() {
        Promocion resultado = new Promocion(
                "P01", "Nombre", "Descripción",
                new BigDecimal("10"), hoy.minusDays(5), hoy.minusDays(1)
        );

        assertEquals(EstadoPromocion.FINALIZADA, resultado.getEstado());
    }

    @Test
    void promocionDeHoyDebeQuedarActiva() {
        Promocion resultado = new Promocion(
                "P01", "Nombre", "Descripción",
                new BigDecimal("10"), hoy, hoy
        );

        assertEquals(EstadoPromocion.ACTIVA, resultado.getEstado());
    }

    @Test
    void actualizarInformacionDebeCambiarLosDatos() {
        promocion.actualizarInformacion(
                "Nueva promoción",
                "Nueva descripción",
                new BigDecimal("30"),
                hoy.minusDays(2),
                hoy.plusDays(10)
        );

        assertEquals("Nueva promoción", promocion.getNombre());
        assertEquals("Nueva descripción", promocion.getDescripcion());
        assertEquals(new BigDecimal("30"), promocion.getPorcentajeDescuento());
        assertEquals(hoy.minusDays(2), promocion.getFechaInicio());
        assertEquals(hoy.plusDays(10), promocion.getFechaFin());
    }

    @Test
    void actualizarInformacionDebeRechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                promocion.actualizarInformacion(
                        " ", "Descripción", new BigDecimal("10"),
                        hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void actualizarInformacionDebeRechazarDescripcionVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                promocion.actualizarInformacion(
                        "Nombre", " ", new BigDecimal("10"),
                        hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void actualizarInformacionDebeRechazarDescuentoFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () ->
                promocion.actualizarInformacion(
                        "Nombre", "Descripción", new BigDecimal("150"),
                        hoy, hoy.plusDays(2)
                ));
    }

    @Test
    void actualizarInformacionDebeRechazarFechaFinAnterior() {
        assertThrows(IllegalArgumentException.class, () ->
                promocion.actualizarInformacion(
                        "Nombre", "Descripción", new BigDecimal("10"),
                        hoy.plusDays(3), hoy
                ));
    }

    @Test
    void estaVigenteDebeRetornarTrueDentroDelPeriodo() {
        assertTrue(promocion.estaVigente(hoy));
    }

    @Test
    void estaVigenteDebeAceptarFechaInicial() {
        assertTrue(promocion.estaVigente(hoy.minusDays(5)));
    }

    @Test
    void estaVigenteDebeAceptarFechaFinal() {
        assertTrue(promocion.estaVigente(hoy.plusDays(5)));
    }

    @Test
    void estaVigenteDebeRetornarFalseAntesDelPeriodo() {
        assertFalse(promocion.estaVigente(hoy.minusDays(6)));
    }

    @Test
    void estaVigenteDebeRetornarFalseDespuesDelPeriodo() {
        assertFalse(promocion.estaVigente(hoy.plusDays(6)));
    }

    @Test
    void estaVigenteDebeRetornarFalseConFechaNula() {
        assertFalse(promocion.estaVigente(null));
    }

    @Test
    void calcularDescuentoDebeCalcularElPorcentaje() {
        BigDecimal resultado = promocion.calcularDescuento(
                new BigDecimal("100000"), hoy
        );

        assertEquals(new BigDecimal("20000.00"), resultado);
    }

    @Test
    void calcularDescuentoDebeRedondearA2Decimales() {
        BigDecimal resultado = promocion.calcularDescuento(
                new BigDecimal("100"), hoy
        );

        assertEquals(new BigDecimal("20.00"), resultado);
    }

    @Test
    void calcularDescuentoDebeRetornarCeroSiNoEstaVigente() {
        BigDecimal resultado = promocion.calcularDescuento(
                new BigDecimal("100000"), hoy.plusDays(10)
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void calcularDescuentoDebeRetornarCeroSiEstaDesactivada() {
        promocion.desactivar();

        BigDecimal resultado = promocion.calcularDescuento(
                new BigDecimal("100000"), hoy
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void calcularDescuentoDebeRechazarSubtotalNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                promocion.calcularDescuento(null, hoy));
    }

    @Test
    void calcularDescuentoDebeRechazarSubtotalNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                promocion.calcularDescuento(new BigDecimal("-1"), hoy));
    }

    @Test
    void calcularDescuentoDebeAceptarSubtotalCero() {
        BigDecimal resultado = promocion.calcularDescuento(
                BigDecimal.ZERO, hoy
        );

        assertEquals(new BigDecimal("0.00"), resultado);
    }

    @Test
    void activarDebeCambiarEstado() {
        promocion.desactivar();

        promocion.activar();

        assertEquals(EstadoPromocion.ACTIVA, promocion.getEstado());
    }

    @Test
    void desactivarDebeCambiarEstado() {
        promocion.desactivar();

        assertEquals(EstadoPromocion.INACTIVA, promocion.getEstado());
    }

    @Test
    void finalizarDebeCambiarEstado() {
        promocion.finalizar();

        assertEquals(EstadoPromocion.FINALIZADA, promocion.getEstado());
    }
}