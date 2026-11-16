
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoCancelacion;
import co.edu.uniquindio.proyectofinal.model.enums.EstadoReembolso;
import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoTarjeta;
import co.edu.uniquindio.proyectofinal.model.interfaces.EstadoCompra;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ReembolsoTest {

    private Compra compra;
    private Cancelacion cancelacion;
    private Reembolso reembolso;
    private BigDecimal valorReembolso;

    @BeforeEach
    void setUp() {
        EstadoCompra estadoInicial = new EstadoCompra() {
            @Override
            public void pagar(Compra compra) {}

            @Override
            public void cancelar(Compra compra) {}

            @Override
            public void reembolsar(Compra compra) {}

            @Override
            public String obtenerNombre() {
                return "Estado de prueba";
            }
        };

        Cliente cliente = new Cliente(
                "C001", "Cliente de prueba",
                "cliente@correo.com", "hash"
        );

        compra = new Compra(
                "COMP001",
                cliente,
                LocalDateTime.now(),
                estadoInicial
        );

        compra.agregarDetalle(new DetalleCompra(
                "DET001",
                new Producto(
                        "PROD001",
                        "Producto de prueba",
                        "Descripción",
                        new BigDecimal("100000"),
                        10,
                        1,
                        "unidades"
                ),
                1
        ));

        cancelacion = new Cancelacion(
                "CAN001",
                compra,
                "Solicitud de devolución"
        );

        cancelacion.aprobar(new BigDecimal("50"));
        valorReembolso = cancelacion.calcularValorReembolso();

        reembolso = new Reembolso(
                "REB001",
                cancelacion,
                valorReembolso
        );
    }

    @Test
    void constructorDebeCrearReembolsoCorrectamente() {
        assertEquals("REB001", reembolso.getCodigo());
        assertEquals(cancelacion, reembolso.getCancelacion());
        assertEquals(valorReembolso, reembolso.getValor());
        assertEquals(EstadoReembolso.PENDIENTE, reembolso.getEstado());
        assertNull(reembolso.getMovimientoTarjeta());
        assertNotNull(reembolso.getFecha());
    }

    @Test
    void constructorDebeEliminarEspaciosDelCodigo() {
        Reembolso resultado = new Reembolso(
                " REB002 ", cancelacion, valorReembolso
        );

        assertEquals("REB002", resultado.getCodigo());
    }

    @Test
    void constructorDebeRechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reembolso(null, cancelacion, valorReembolso));
    }

    @Test
    void constructorDebeRechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reembolso(" ", cancelacion, valorReembolso));
    }

    @Test
    void constructorDebeRechazarCancelacionNula() {
        assertThrows(NullPointerException.class, () ->
                new Reembolso("REB002", null, BigDecimal.ONE));
    }

    @Test
    void constructorDebeRechazarCancelacionNoAprobada() {
        Cancelacion pendiente = new Cancelacion(
                "CAN002", compra, "Motivo"
        );

        assertThrows(IllegalArgumentException.class, () ->
                new Reembolso("REB002", pendiente, BigDecimal.ONE));
    }

    @Test
    void constructorDebeRechazarCancelacionRechazada() {
        Cancelacion rechazada = new Cancelacion(
                "CAN003", compra, "Motivo"
        );
        rechazada.rechazar();

        assertThrows(IllegalArgumentException.class, () ->
                new Reembolso("REB003", rechazada, BigDecimal.ONE));
    }

    @Test
    void constructorDebeRechazarValorNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reembolso("REB002", cancelacion, null));
    }

    @Test
    void constructorDebeRechazarValorCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reembolso("REB002", cancelacion, BigDecimal.ZERO));
    }

    @Test
    void constructorDebeRechazarValorNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reembolso(
                        "REB002", cancelacion, new BigDecimal("-1")
                ));
    }

    @Test
    void constructorDebeRechazarValorDiferenteAlAprobado() {
        assertThrows(IllegalArgumentException.class, () ->
                new Reembolso(
                        "REB002", cancelacion, new BigDecimal("1000")
                ));
    }

    @Test
    void marcarProcesadoDebeCambiarEstadoYGuardarMovimiento() {
        MovimientoTarjeta movimiento = crearMovimientoValido();

        reembolso.marcarProcesado(movimiento);

        assertEquals(EstadoReembolso.PROCESADO, reembolso.getEstado());
        assertEquals(movimiento, reembolso.getMovimientoTarjeta());
        assertEquals(EstadoCancelacion.PROCESADA, cancelacion.getEstado());
    }

    @Test
    void marcarProcesadoDebeRechazarMovimientoNulo() {
        assertThrows(NullPointerException.class, () ->
                reembolso.marcarProcesado(null));

        assertEquals(EstadoReembolso.PENDIENTE, reembolso.getEstado());
    }

    @Test
    void marcarProcesadoDebeRechazarMovimientoDeTipoDiferente() {
        MovimientoTarjeta movimiento = new MovimientoTarjeta(
                "MOV001",
                TipoMovimientoTarjeta.PAGO,
                valorReembolso,
                LocalDateTime.now(),
                BigDecimal.ZERO,
                "REB001",
                "ADMIN001"
        );

        assertThrows(IllegalArgumentException.class, () ->
                reembolso.marcarProcesado(movimiento));
    }

    @Test
    void marcarProcesadoDebeRechazarValorDiferente() {
        MovimientoTarjeta movimiento = new MovimientoTarjeta(
                "MOV002",
                TipoMovimientoTarjeta.REEMBOLSO,
                new BigDecimal("100"),
                LocalDateTime.now(),
                BigDecimal.ZERO,
                "REB001",
                "ADMIN001"
        );

        assertThrows(IllegalArgumentException.class, () ->
                reembolso.marcarProcesado(movimiento));
    }

    @Test
    void marcarProcesadoDebeRechazarReferenciaDiferente() {
        MovimientoTarjeta movimiento = new MovimientoTarjeta(
                "MOV003",
                TipoMovimientoTarjeta.REEMBOLSO,
                valorReembolso,
                LocalDateTime.now(),
                BigDecimal.ZERO,
                "OTRA-REFERENCIA",
                "ADMIN001"
        );

        assertThrows(IllegalArgumentException.class, () ->
                reembolso.marcarProcesado(movimiento));
    }

    @Test
    void marcarProcesadoNoDebePermitirProcesarDosVeces() {
        reembolso.marcarProcesado(crearMovimientoValido());

        assertThrows(IllegalStateException.class, () ->
                reembolso.marcarProcesado(crearMovimientoValido()));
    }

    @Test
    void marcarFallidoDebeCambiarElEstado() {
        reembolso.marcarFallido();

        assertEquals(EstadoReembolso.FALLIDO, reembolso.getEstado());
    }

    @Test
    void marcarFallidoNoDebePermitirRepetirse() {
        reembolso.marcarFallido();

        assertThrows(IllegalStateException.class, () ->
                reembolso.marcarFallido());
    }

    @Test
    void marcarFallidoNoDebePermitirProcesarDespues() {
        reembolso.marcarFallido();

        assertThrows(IllegalStateException.class, () ->
                reembolso.marcarProcesado(crearMovimientoValido()));
    }

    @Test
    void marcarProcesadoNoDebePermitirMarcarFallidoDespues() {
        reembolso.marcarProcesado(crearMovimientoValido());

        assertThrows(IllegalStateException.class, () ->
                reembolso.marcarFallido());
    }

    private MovimientoTarjeta crearMovimientoValido() {
        return new MovimientoTarjeta(
                "MOV004",
                TipoMovimientoTarjeta.REEMBOLSO,
                valorReembolso,
                LocalDateTime.now(),
                BigDecimal.ZERO,
                "REB001",
                "ADMIN001"
        );
    }
}
