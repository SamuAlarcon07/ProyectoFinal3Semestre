
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoTarjeta;
import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoTarjeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TarjetaVirtualTest {

    private Cliente titular;
    private Cliente otroCliente;
    private Administrador administrador;
    private TarjetaVirtual tarjeta;

    @BeforeEach
    void setUp() {
        titular = new Cliente(
                "C001",
                "Juan Pérez",
                "juan@correo.com",
                "hash"
        );

        otroCliente = new Cliente(
                "C002",
                "Ana López",
                "ana@correo.com",
                "hash"
        );

        administrador = new Administrador(
                "A001",
                "Administrador",
                "admin@correo.com",
                "hash",
                "EMP001"
        );

        tarjeta = new TarjetaVirtual("123456789", titular);
    }

    @Test
    void constructorDebeCrearTarjetaCorrectamente() {
        assertEquals("123456789", tarjeta.getNumero());
        assertSame(titular, tarjeta.getTitular());
        assertEquals(new BigDecimal("0.00"), tarjeta.getSaldo());
        assertEquals(EstadoTarjeta.ACTIVA, tarjeta.getEstado());
        assertTrue(tarjeta.getMovimientos().isEmpty());
    }

    @Test
    void constructorDebeEliminarEspaciosDelNumero() {
        TarjetaVirtual resultado = new TarjetaVirtual(" 123456 ", titular);

        assertEquals("123456", resultado.getNumero());
    }

    @Test
    void constructorDebeRechazarNumeroNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new TarjetaVirtual(null, titular));
    }

    @Test
    void constructorDebeRechazarNumeroVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new TarjetaVirtual(" ", titular));
    }

    @Test
    void constructorDebeRechazarTitularNulo() {
        assertThrows(NullPointerException.class, () ->
                new TarjetaVirtual("123456", null));
    }

    @Test
    void recargarDebeAumentarElSaldo() {
        tarjeta.recargar(new BigDecimal("50000"), administrador);

        assertEquals(new BigDecimal("50000.00"), tarjeta.getSaldo());
    }

    @Test
    void recargarDebeRegistrarMovimiento() {
        tarjeta.recargar(new BigDecimal("30000"), administrador);

        List<MovimientoTarjeta> movimientos = tarjeta.getMovimientos();

        assertEquals(1, movimientos.size());
        assertEquals(TipoMovimientoTarjeta.RECARGA,
                movimientos.get(0).getTipo());
        assertEquals(new BigDecimal("30000"), movimientos.get(0).getValor());
    }

    @Test
    void recargarDebeRechazarAdministradorNulo() {
        assertThrows(NullPointerException.class, () ->
                tarjeta.recargar(new BigDecimal("10000"), null));
    }

    @Test
    void recargarDebeRechazarValorNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                tarjeta.recargar(null, administrador));
    }

    @Test
    void recargarDebeRechazarValorCero() {
        assertThrows(IllegalArgumentException.class, () ->
                tarjeta.recargar(BigDecimal.ZERO, administrador));
    }

    @Test
    void recargarDebeRechazarValorNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                tarjeta.recargar(new BigDecimal("-1000"), administrador));
    }

    @Test
    void recargarDesdeClienteDebeAumentarSaldo() {
        tarjeta.recargarDesdeCliente(new BigDecimal("25000"), titular);

        assertEquals(new BigDecimal("25000.00"), tarjeta.getSaldo());
    }

    @Test
    void recargarDesdeClienteDebeRechazarSolicitanteNulo() {
        assertThrows(SecurityException.class, () ->
                tarjeta.recargarDesdeCliente(
                        new BigDecimal("10000"), null));
    }

    @Test
    void recargarDesdeClienteDebeRechazarOtroCliente() {
        assertThrows(SecurityException.class, () ->
                tarjeta.recargarDesdeCliente(
                        new BigDecimal("10000"), otroCliente));
    }

    @Test
    void recargarDesdeClienteDebeRechazarValorNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                tarjeta.recargarDesdeCliente(null, titular));
    }

    @Test
    void pagarDebeDescontarSaldo() {
        tarjeta.recargar(new BigDecimal("50000"), administrador);

        tarjeta.pagar(new BigDecimal("15000"), "COMP001");

        assertEquals(new BigDecimal("35000.00"), tarjeta.getSaldo());
    }

    @Test
    void pagarDebeRegistrarMovimiento() {
        tarjeta.recargar(new BigDecimal("50000"), administrador);

        tarjeta.pagar(new BigDecimal("15000"), "COMP001");

        List<MovimientoTarjeta> movimientos = tarjeta.getMovimientos();

        assertEquals(2, movimientos.size());
        assertEquals(TipoMovimientoTarjeta.PAGO,
                movimientos.get(1).getTipo());
        assertEquals(new BigDecimal("15000"), movimientos.get(1).getValor());
    }

    @Test
    void pagarDebeRechazarSaldoInsuficiente() {
        assertThrows(IllegalStateException.class, () ->
                tarjeta.pagar(new BigDecimal("1000"), "COMP001"));
    }

    @Test
    void pagarDebeRechazarValorNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                tarjeta.pagar(null, "COMP001"));
    }

    @Test
    void pagarDebeRechazarValorCero() {
        assertThrows(IllegalArgumentException.class, () ->
                tarjeta.pagar(BigDecimal.ZERO, "COMP001"));
    }

    @Test
    void pagarDebeRechazarValorNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                tarjeta.pagar(new BigDecimal("-100"), "COMP001"));
    }

    @Test
    void pagarDebeRechazarTarjetaBloqueada() {
        tarjeta.recargar(new BigDecimal("50000"), administrador);
        tarjeta.bloquear();

        assertThrows(IllegalStateException.class, () ->
                tarjeta.pagar(new BigDecimal("10000"), "COMP001"));
    }

    @Test
    void reembolsarDebeAumentarSaldo() {
        tarjeta.reembolsar(new BigDecimal("20000"), "REB001");

        assertEquals(new BigDecimal("20000.00"), tarjeta.getSaldo());
    }

    @Test
    void reembolsarDebeRegistrarMovimiento() {
        tarjeta.reembolsar(new BigDecimal("20000"), "REB001");

        assertEquals(1, tarjeta.getMovimientos().size());
        assertEquals(TipoMovimientoTarjeta.REEMBOLSO,
                tarjeta.getMovimientos().get(0).getTipo());
    }

    @Test
    void reembolsarDebePermitirTarjetaBloqueada() {
        tarjeta.bloquear();

        tarjeta.reembolsar(new BigDecimal("20000"), "REB001");

        assertEquals(new BigDecimal("20000.00"), tarjeta.getSaldo());
    }

    @Test
    void reembolsarDebeRechazarValorNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                tarjeta.reembolsar(null, "REB001"));
    }

    @Test
    void reembolsarDebeRechazarValorCero() {
        assertThrows(IllegalArgumentException.class, () ->
                tarjeta.reembolsar(BigDecimal.ZERO, "REB001"));
    }

    @Test
    void reembolsarDebeRechazarValorNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                tarjeta.reembolsar(new BigDecimal("-100"), "REB001"));
    }

    @Test
    void bloquearDebeCambiarEstado() {
        tarjeta.bloquear();

        assertEquals(EstadoTarjeta.BLOQUEADA, tarjeta.getEstado());
    }

    @Test
    void activarDebeCambiarEstado() {
        tarjeta.bloquear();

        tarjeta.activar();

        assertEquals(EstadoTarjeta.ACTIVA, tarjeta.getEstado());
    }

    @Test
    void tarjetaBloqueadaNoDebePermitirRecargas() {
        tarjeta.bloquear();

        assertThrows(IllegalStateException.class, () ->
                tarjeta.recargar(new BigDecimal("10000"), administrador));
    }

    @Test
    void tarjetaBloqueadaNoDebePermitirRecargasDesdeCliente() {
        tarjeta.bloquear();

        assertThrows(IllegalStateException.class, () ->
                tarjeta.recargarDesdeCliente(
                        new BigDecimal("10000"), titular));
    }

    @Test
    void getMovimientosDebeRetornarListaInmodificable() {
        tarjeta.recargar(new BigDecimal("10000"), administrador);

        List<MovimientoTarjeta> movimientos = tarjeta.getMovimientos();

        assertThrows(UnsupportedOperationException.class, () ->
                movimientos.clear());
    }

    @Test
    void saldoDebeConservarDosDecimales() {
        tarjeta.recargar(new BigDecimal("100.555"), administrador);

        assertEquals(new BigDecimal("100.56"), tarjeta.getSaldo());
    }

    @Test
    void pagarDebeActualizarSaldoConDosDecimales() {
        tarjeta.recargar(new BigDecimal("100.00"), administrador);

        tarjeta.pagar(new BigDecimal("25.555"), "COMP002");

        assertEquals(new BigDecimal("74.45"), tarjeta.getSaldo());
    }
}
