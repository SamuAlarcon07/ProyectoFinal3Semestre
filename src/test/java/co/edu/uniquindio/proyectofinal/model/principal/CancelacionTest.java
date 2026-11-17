package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoCancelacion;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state.CompraPendiente;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CancelacionTest {

    private Compra compra;
    private Cancelacion cancelacion;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente(
                "CLI01",
                "Juan Perez",
                "juan@gmail.com",
                "hash123",
                LocalDate.now()
        );

        compra = new Compra(
                "COM01",
                cliente,
                LocalDateTime.now(),
                new CompraPendiente()
        );

        // Crear un detalle de compra por valor de $100.000
        ElementoVendible elemento = new ElementoVendible() {
            @Override
            public BigDecimal consultarPrecio() {
                return new BigDecimal("100000");
            }

            @Override
            public String consultarDescripcion() {
                return "Entrada de cine";
            }
        };

        DetalleCompra detalle = new DetalleCompra(
                "DET01",
                elemento,
                1
        );

        compra.agregarDetalle(detalle);

        cancelacion = new Cancelacion(
                "CAN01",
                compra,
                "Problemas personales"
        );
    }

    // 1. Crear cancelación correctamente
    @Test
    void testCrearCancelacionCorrectamente() {
        assertNotNull(cancelacion);
        assertEquals("CAN01", cancelacion.getCodigo());
        assertEquals(compra, cancelacion.getCompra());
        assertEquals("Problemas personales", cancelacion.getMotivo());
        assertEquals(EstadoCancelacion.SOLICITADA,
                cancelacion.getEstado());
        assertEquals(0,
                cancelacion.getPorcentajeReembolso().compareTo(BigDecimal.ZERO));
        assertNotNull(cancelacion.getFecha());
    }

    // 2. Validar código nulo
    @Test
    void testCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cancelacion(null, compra, "Motivo")
        );
    }

    // 3. Validar código vacío
    @Test
    void testCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cancelacion(" ", compra, "Motivo")
        );
    }

    // 4. Validar espacios en el código
    @Test
    void testEliminarEspaciosCodigo() {
        Cancelacion nueva = new Cancelacion(
                " CAN02 ",
                compra,
                "Motivo"
        );

        assertEquals("CAN02", nueva.getCodigo());
    }

    // 5. Validar compra nula
    @Test
    void testCompraNula() {
        assertThrows(NullPointerException.class, () ->
                new Cancelacion("CAN01", null, "Motivo")
        );
    }

    // 6. Validar motivo nulo
    @Test
    void testMotivoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cancelacion("CAN01", compra, null)
        );
    }

    // 7. Validar motivo vacío
    @Test
    void testMotivoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cancelacion("CAN01", compra, " ")
        );
    }

    // 8. Validar espacios en el motivo
    @Test
    void testEliminarEspaciosMotivo() {
        Cancelacion nueva = new Cancelacion(
                "CAN02",
                compra,
                "  Cancelación solicitada  "
        );

        assertEquals("Cancelación solicitada", nueva.getMotivo());
    }

    // 9. Aprobar cancelación correctamente
    @Test
    void testAprobarCancelacion() {
        cancelacion.aprobar(new BigDecimal("80"));

        assertEquals(EstadoCancelacion.APROBADA,
                cancelacion.getEstado());

        assertEquals(0,
                cancelacion.getPorcentajeReembolso()
                        .compareTo(new BigDecimal("80")));
    }

    // 10. Aprobar con porcentaje cero
    @Test
    void testAprobarConCeroPorCiento() {
        cancelacion.aprobar(BigDecimal.ZERO);

        assertEquals(EstadoCancelacion.APROBADA,
                cancelacion.getEstado());

        assertEquals(0,
                cancelacion.getPorcentajeReembolso()
                        .compareTo(BigDecimal.ZERO));
    }

    // 11. Aprobar con porcentaje del 100%
    @Test
    void testAprobarConCienPorCiento() {
        cancelacion.aprobar(new BigDecimal("100"));

        assertEquals(EstadoCancelacion.APROBADA,
                cancelacion.getEstado());

        assertEquals(0,
                cancelacion.getPorcentajeReembolso()
                        .compareTo(new BigDecimal("100")));
    }

    // 12. Rechazar porcentaje negativo
    @Test
    void testAprobarConPorcentajeNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                cancelacion.aprobar(new BigDecimal("-10"))
        );

        assertEquals(EstadoCancelacion.SOLICITADA,
                cancelacion.getEstado());
    }

    // 13. Rechazar porcentaje superior a 100
    @Test
    void testAprobarConPorcentajeSuperiorACien() {
        assertThrows(IllegalArgumentException.class, () ->
                cancelacion.aprobar(new BigDecimal("110"))
        );

        assertEquals(EstadoCancelacion.SOLICITADA,
                cancelacion.getEstado());
    }

    // 14. Rechazar porcentaje nulo
    @Test
    void testAprobarConPorcentajeNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                cancelacion.aprobar(null)
        );

        assertEquals(EstadoCancelacion.SOLICITADA,
                cancelacion.getEstado());
    }

    // 15. No aprobar una cancelación dos veces
    @Test
    void testAprobarDosVeces() {
        cancelacion.aprobar(new BigDecimal("50"));

        assertThrows(IllegalStateException.class, () ->
                cancelacion.aprobar(new BigDecimal("80"))
        );
    }

    // 16. Rechazar cancelación correctamente
    @Test
    void testRechazarCancelacion() {
        cancelacion.rechazar();

        assertEquals(EstadoCancelacion.RECHAZADA,
                cancelacion.getEstado());
    }

    // 17. No rechazar una cancelación dos veces
    @Test
    void testRechazarDosVeces() {
        cancelacion.rechazar();

        assertThrows(IllegalStateException.class, () ->
                cancelacion.rechazar()
        );
    }

    // 18. No aprobar una cancelación rechazada
    @Test
    void testAprobarCancelacionRechazada() {
        cancelacion.rechazar();

        assertThrows(IllegalStateException.class, () ->
                cancelacion.aprobar(new BigDecimal("50"))
        );
    }

    // 19. Calcular reembolso del 50%
    @Test
    void testCalcularReembolsoCincuentaPorCiento() {
        cancelacion.aprobar(new BigDecimal("50"));

        BigDecimal resultado =
                cancelacion.calcularValorReembolso();

        assertEquals(0,
                resultado.compareTo(new BigDecimal("50000.00")));
    }

    // 20. Calcular reembolso del 100%
    @Test
    void testCalcularReembolsoCompleto() {
        cancelacion.aprobar(new BigDecimal("100"));

        BigDecimal resultado =
                cancelacion.calcularValorReembolso();

        assertEquals(0,
                resultado.compareTo(new BigDecimal("100000.00")));
    }

    // 21. Calcular reembolso del 0%
    @Test
    void testCalcularReembolsoCero() {
        cancelacion.aprobar(BigDecimal.ZERO);

        BigDecimal resultado =
                cancelacion.calcularValorReembolso();

        assertEquals(0,
                resultado.compareTo(new BigDecimal("0.00")));
    }

    // 22. No calcular reembolso sin aprobar
    @Test
    void testCalcularReembolsoSinAprobacion() {
        assertThrows(IllegalStateException.class, () ->
                cancelacion.calcularValorReembolso()
        );
    }

    // 23. No calcular reembolso después de rechazar
    @Test
    void testCalcularReembolsoCancelacionRechazada() {
        cancelacion.rechazar();

        assertThrows(IllegalStateException.class, () ->
                cancelacion.calcularValorReembolso()
        );
    }

    // 24. Comprobar redondeo del reembolso
    @Test
    void testRedondeoReembolso() {
        ElementoVendible elemento = new ElementoVendible() {
            @Override
            public BigDecimal consultarPrecio() {
                return new BigDecimal("100.01");
            }

            @Override
            public String consultarDescripcion() {
                return "Producto de prueba";
            }
        };

        Compra compraRedondeo = new Compra(
                "COM02",
                compra.getCliente(),
                LocalDateTime.now(),
                new CompraPendiente()
        );

        compraRedondeo.agregarDetalle(
                new DetalleCompra("DET02", elemento, 1)
        );

        Cancelacion nueva = new Cancelacion(
                "CAN02",
                compraRedondeo,
                "Prueba de redondeo"
        );

        nueva.aprobar(new BigDecimal("50"));

        assertEquals(
                0,
                nueva.calcularValorReembolso()
                        .compareTo(new BigDecimal("50.01"))
        );
    }

    // 25. Marcar cancelación aprobada como procesada
    @Test
    void testMarcarProcesada() {
        cancelacion.aprobar(new BigDecimal("80"));

        cancelacion.marcarProcesada();

        assertEquals(EstadoCancelacion.PROCESADA,
                cancelacion.getEstado());
    }

    // 26. No procesar una cancelación solicitada
    @Test
    void testProcesarSinAprobacion() {
        assertThrows(IllegalStateException.class, () ->
                cancelacion.marcarProcesada()
        );
    }

    // 27. No procesar una cancelación rechazada
    @Test
    void testProcesarCancelacionRechazada() {
        cancelacion.rechazar();

        assertThrows(IllegalStateException.class, () ->
                cancelacion.marcarProcesada()
        );
    }

    // 28. No procesar una cancelación dos veces
    @Test
    void testProcesarDosVeces() {
        cancelacion.aprobar(new BigDecimal("80"));
        cancelacion.marcarProcesada();

        assertThrows(IllegalStateException.class, () ->
                cancelacion.marcarProcesada()
        );
    }

    // 29. Calcular reembolso después de procesar
    @Test
    void testCalcularReembolsoProcesado() {
        cancelacion.aprobar(new BigDecimal("50"));
        cancelacion.marcarProcesada();

        BigDecimal resultado =
                cancelacion.calcularValorReembolso();

        assertEquals(0,
                resultado.compareTo(new BigDecimal("50000.00")));
    }

    // 30. Comprobar que los getters devuelvan los datos
    @Test
    void testGetters() {
        assertEquals("CAN01", cancelacion.getCodigo());
        assertEquals(compra, cancelacion.getCompra());
        assertEquals("Problemas personales",
                cancelacion.getMotivo());

        assertNotNull(cancelacion.getFecha());

        assertEquals(EstadoCancelacion.SOLICITADA,
                cancelacion.getEstado());

        assertEquals(0,
                cancelacion.getPorcentajeReembolso()
                        .compareTo(BigDecimal.ZERO));
    }
}