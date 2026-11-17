package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoPuntos;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CuentaPuntosTest {

    private Cliente crearCliente() {
        return new Cliente(
                "123",
                "Juan Pérez",
                "juan@gmail.com",
                "hash123"
        );
    }

    private CuentaPuntos crearCuenta() {
        return new CuentaPuntos(crearCliente());
    }

    @Test
    void crearCuentaCorrectamente() {
        CuentaPuntos cuenta = crearCuenta();

        assertNotNull(cuenta.getTitular());
        assertTrue(cuenta.getLotes().isEmpty());
        assertTrue(cuenta.getMovimientos().isEmpty());
    }

    @Test
    void rechazarTitularNulo() {
        assertThrows(NullPointerException.class, () ->
                new CuentaPuntos(null)
        );
    }

    @Test
    void acumularPuntosCorrectamente() {
        CuentaPuntos cuenta = crearCuenta();
        LocalDate hoy = LocalDate.now();

        LotePuntos lote = cuenta.acumularPuntos(
                "L001", 100, hoy, "COMPRA01"
        );

        assertEquals("L001", lote.getCodigo());
        assertEquals(100, lote.getCantidadOriginal());
        assertEquals(100, lote.getCantidadDisponible());
        assertEquals(hoy, lote.getFechaObtencion());
        assertEquals(hoy.plusYears(1), lote.getFechaVencimiento());
    }

    @Test
    void acumularPuntosRegistraMovimiento() {
        CuentaPuntos cuenta = crearCuenta();

        cuenta.acumularPuntos(
                "L001", 100, LocalDate.now(), "COMPRA01"
        );

        assertEquals(1, cuenta.getMovimientos().size());

        MovimientoPuntos movimiento = cuenta.getMovimientos().get(0);

        assertEquals(TipoMovimientoPuntos.ACUMULACION, movimiento.getTipo());
        assertEquals(100, movimiento.getCantidad());
        assertEquals("COMPRA01", movimiento.getCodigoReferencia());
    }

    @Test
    void rechazarFechaObtencionNula() {
        CuentaPuntos cuenta = crearCuenta();

        assertThrows(NullPointerException.class, () ->
                cuenta.acumularPuntos("L001", 100, null, "COMPRA01")
        );
    }

    @Test
    void rechazarFechaObtencionFutura() {
        CuentaPuntos cuenta = crearCuenta();

        assertThrows(IllegalArgumentException.class, () ->
                cuenta.acumularPuntos(
                        "L001", 100, LocalDate.now().plusDays(1), "COMPRA01"
                )
        );
    }

    @Test
    void consultarSaldoSinLotes() {
        CuentaPuntos cuenta = crearCuenta();

        assertEquals(0, cuenta.consultarSaldo(LocalDate.now()));
    }

    @Test
    void consultarSaldoConPuntosAcumulados() {
        CuentaPuntos cuenta = crearCuenta();
        LocalDate hoy = LocalDate.now();

        cuenta.acumularPuntos("L001", 100, hoy, "COMPRA01");
        cuenta.acumularPuntos("L002", 50, hoy, "COMPRA02");

        assertEquals(150, cuenta.consultarSaldo(hoy));
    }

    @Test
    void rechazarFechaConsultaNula() {
        CuentaPuntos cuenta = crearCuenta();

        assertThrows(NullPointerException.class, () ->
                cuenta.consultarSaldo(null)
        );
    }

    @Test
    void redimirPuntosCorrectamente() {
        CuentaPuntos cuenta = crearCuenta();
        LocalDate hoy = LocalDate.now();

        cuenta.acumularPuntos("L001", 100, hoy, "COMPRA01");
        cuenta.redimirPuntos(30, hoy, "VENTA01");

        assertEquals(70, cuenta.consultarSaldo(hoy));
    }

    @Test
    void redimirPuntosRegistraMovimiento() {
        CuentaPuntos cuenta = crearCuenta();
        LocalDate hoy = LocalDate.now();

        cuenta.acumularPuntos("L001", 100, hoy, "COMPRA01");
        cuenta.redimirPuntos(40, hoy, "VENTA01");

        assertEquals(2, cuenta.getMovimientos().size());

        MovimientoPuntos movimiento = cuenta.getMovimientos().get(1);

        assertEquals(TipoMovimientoPuntos.REDENCION, movimiento.getTipo());
        assertEquals(40, movimiento.getCantidad());
        assertEquals("VENTA01", movimiento.getCodigoReferencia());
    }

    @Test
    void rechazarRedencionDeCantidadCero() {
        CuentaPuntos cuenta = crearCuenta();

        assertThrows(IllegalArgumentException.class, () ->
                cuenta.redimirPuntos(0, LocalDate.now(), "VENTA01")
        );
    }

    @Test
    void rechazarRedencionDeCantidadNegativa() {
        CuentaPuntos cuenta = crearCuenta();

        assertThrows(IllegalArgumentException.class, () ->
                cuenta.redimirPuntos(-10, LocalDate.now(), "VENTA01")
        );
    }

    @Test
    void rechazarRedencionSinPuntosSuficientes() {
        CuentaPuntos cuenta = crearCuenta();
        LocalDate hoy = LocalDate.now();

        cuenta.acumularPuntos("L001", 50, hoy, "COMPRA01");

        assertThrows(IllegalStateException.class, () ->
                cuenta.redimirPuntos(100, hoy, "VENTA01")
        );
    }

    @Test
    void procesarVencimientosDeLotes() {
        CuentaPuntos cuenta = crearCuenta();
        LocalDate obtencion = LocalDate.now().minusYears(2);
        LocalDate fechaConsulta = LocalDate.now();

        cuenta.acumularPuntos("L001", 100, obtencion, "COMPRA01");

        int vencidos = cuenta.procesarVencimientos(fechaConsulta);

        assertEquals(100, vencidos);
        assertEquals(0, cuenta.consultarSaldo(fechaConsulta));
    }

    @Test
    void procesarVencimientosRegistraMovimiento() {
        CuentaPuntos cuenta = crearCuenta();
        LocalDate obtencion = LocalDate.now().minusYears(2);
        LocalDate hoy = LocalDate.now();

        cuenta.acumularPuntos("L001", 80, obtencion, "COMPRA01");
        cuenta.procesarVencimientos(hoy);

        MovimientoPuntos movimiento =
                cuenta.getMovimientos().get(1);

        assertEquals(TipoMovimientoPuntos.VENCIMIENTO, movimiento.getTipo());
        assertEquals(80, movimiento.getCantidad());
        assertEquals("L001", movimiento.getCodigoReferencia());
    }

    @Test
    void rechazarProcesamientoConFechaNula() {
        CuentaPuntos cuenta = crearCuenta();

        assertThrows(NullPointerException.class, () ->
                cuenta.procesarVencimientos(null)
        );
    }

    @Test
    void listasNoPuedenModificarseDesdeFuera() {
        CuentaPuntos cuenta = crearCuenta();

        cuenta.acumularPuntos(
                "L001", 100, LocalDate.now(), "COMPRA01"
        );

        assertThrows(UnsupportedOperationException.class, () ->
                cuenta.getLotes().clear()
        );

        assertThrows(UnsupportedOperationException.class, () ->
                cuenta.getMovimientos().clear()
        );
    }
}