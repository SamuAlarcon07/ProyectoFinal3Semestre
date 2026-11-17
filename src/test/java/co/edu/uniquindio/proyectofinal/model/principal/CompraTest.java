package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.interfaces.EstadoCompra;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state.CompraCancelada;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state.CompraPagada;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state.CompraPendiente;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state.CompraReembolsada;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CompraTest {

    private Cliente crearCliente() {
        return new Cliente(
                "123",
                "Juan Pérez",
                "juan@gmail.com",
                "hash123"
        );
    }

    private Compra crearCompra() {
        return new Compra(
                "C001",
                crearCliente(),
                LocalDateTime.now(),
                new CompraPendiente()
        );
    }

    private Producto crearProducto(String codigo, String nombre, String precio) {
        return new Producto(
                codigo,
                nombre,
                "Producto de prueba",
                new BigDecimal(precio),
                50,
                5,
                "unidad"
        );
    }

    private DetalleCompra crearDetalle(String codigo, String precio, int cantidad) {
        Producto producto = crearProducto("P01", "Gaseosa", precio);
        return new DetalleCompra(codigo, producto, cantidad);
    }

    private void assertDecimalEquals(String esperado, BigDecimal actual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(actual));
    }

    @Test
    void crearCompraCorrectamente() {
        Compra compra = crearCompra();

        assertEquals("C001", compra.getCodigo());
        assertNotNull(compra.getCliente());
        assertNotNull(compra.getFecha());
        assertInstanceOf(CompraPendiente.class, compra.getEstado());
        assertTrue(compra.getDetalles().isEmpty());
        assertDecimalEquals("0.00", compra.getSubtotal());
        assertDecimalEquals("0.00", compra.getDescuentos());
        assertDecimalEquals("0.00", compra.getValorPuntosRedimidos());
        assertDecimalEquals("0.00", compra.getTotal());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Compra(null, crearCliente(), LocalDateTime.now(),
                        new CompraPendiente())
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Compra(" ", crearCliente(), LocalDateTime.now(),
                        new CompraPendiente())
        );
    }

    @Test
    void eliminarEspaciosDelCodigo() {
        Compra compra = new Compra(
                " C001 ",
                crearCliente(),
                LocalDateTime.now(),
                new CompraPendiente()
        );

        assertEquals("C001", compra.getCodigo());
    }

    @Test
    void rechazarClienteNulo() {
        assertThrows(NullPointerException.class, () ->
                new Compra("C001", null, LocalDateTime.now(),
                        new CompraPendiente())
        );
    }

    @Test
    void rechazarFechaNula() {
        assertThrows(NullPointerException.class, () ->
                new Compra("C001", crearCliente(), null,
                        new CompraPendiente())
        );
    }

    @Test
    void rechazarEstadoInicialNulo() {
        assertThrows(NullPointerException.class, () ->
                new Compra("C001", crearCliente(), LocalDateTime.now(), null)
        );
    }

    @Test
    void agregarDetalleActualizaSubtotalYTotal() {
        Compra compra = crearCompra();

        compra.agregarDetalle(crearDetalle("D01", "5000", 2));

        assertEquals(1, compra.getDetalles().size());
        assertDecimalEquals("10000.00", compra.getSubtotal());
        assertDecimalEquals("10000.00", compra.getTotal());
    }

    @Test
    void rechazarDetalleNulo() {
        Compra compra = crearCompra();

        assertThrows(NullPointerException.class, () ->
                compra.agregarDetalle(null)
        );
    }

    @Test
    void calcularSubtotalConVariosDetalles() {
        Compra compra = crearCompra();

        compra.agregarDetalle(crearDetalle("D01", "5000", 2));
        compra.agregarDetalle(crearDetalle("D02", "3000", 1));

        assertDecimalEquals("13000.00", compra.calcularSubtotal());
    }

    @Test
    void eliminarDetalleActualizaTotal() {
        Compra compra = crearCompra();

        compra.agregarDetalle(crearDetalle("D01", "5000", 2));
        compra.agregarDetalle(crearDetalle("D02", "3000", 1));

        compra.eliminarDetalle("D01");

        assertEquals(1, compra.getDetalles().size());
        assertDecimalEquals("3000.00", compra.getSubtotal());
        assertDecimalEquals("3000.00", compra.getTotal());
    }

    @Test
    void rechazarEliminarDetalleInexistente() {
        Compra compra = crearCompra();

        assertThrows(IllegalArgumentException.class, () ->
                compra.eliminarDetalle("NO_EXISTE")
        );
    }

    @Test
    void aplicarDescuentoCorrectamente() {
        Compra compra = crearCompra();
        compra.agregarDetalle(crearDetalle("D01", "10000", 2));

        compra.aplicarDescuentos(new BigDecimal("5000"));

        assertDecimalEquals("5000", compra.getDescuentos());
        assertDecimalEquals("15000", compra.getTotal());
    }

    @Test
    void rechazarDescuentoNulo() {
        Compra compra = crearCompra();

        assertThrows(IllegalArgumentException.class, () ->
                compra.aplicarDescuentos(null)
        );
    }

    @Test
    void rechazarDescuentoNegativo() {
        Compra compra = crearCompra();

        assertThrows(IllegalArgumentException.class, () ->
                compra.aplicarDescuentos(new BigDecimal("-100"))
        );
    }

    @Test
    void restaurarDescuentoAnteriorSiSuperaSubtotal() {
        Compra compra = crearCompra();
        compra.agregarDetalle(crearDetalle("D01", "10000", 1));
        compra.aplicarDescuentos(new BigDecimal("2000"));

        assertThrows(IllegalStateException.class, () ->
                compra.aplicarDescuentos(new BigDecimal("12000"))
        );

        assertDecimalEquals("2000", compra.getDescuentos());
        assertDecimalEquals("8000", compra.getTotal());
    }

    @Test
    void aplicarValorPuntosRedimidosCorrectamente() {
        Compra compra = crearCompra();
        compra.agregarDetalle(crearDetalle("D01", "10000", 2));

        compra.aplicarValorPuntosRedimidos(new BigDecimal("3000"));

        assertDecimalEquals("3000", compra.getValorPuntosRedimidos());
        assertDecimalEquals("17000", compra.getTotal());
    }

    @Test
    void rechazarValorPuntosNegativo() {
        Compra compra = crearCompra();

        assertThrows(IllegalArgumentException.class, () ->
                compra.aplicarValorPuntosRedimidos(new BigDecimal("-500"))
        );
    }

    @Test
    void restaurarPuntosAnterioresSiSuperanSubtotal() {
        Compra compra = crearCompra();
        compra.agregarDetalle(crearDetalle("D01", "10000", 1));
        compra.aplicarValorPuntosRedimidos(new BigDecimal("1000"));

        assertThrows(IllegalStateException.class, () ->
                compra.aplicarValorPuntosRedimidos(new BigDecimal("12000"))
        );

        assertDecimalEquals("1000", compra.getValorPuntosRedimidos());
        assertDecimalEquals("9000", compra.getTotal());
    }

    @Test
    void rechazarDescuentosYPuntosQueSuperanSubtotal() {
        Compra compra = crearCompra();
        compra.agregarDetalle(crearDetalle("D01", "10000", 1));
        compra.aplicarDescuentos(new BigDecimal("6000"));

        assertThrows(IllegalStateException.class, () ->
                compra.aplicarValorPuntosRedimidos(new BigDecimal("5000"))
        );

        assertDecimalEquals("6000", compra.getDescuentos());
        assertDecimalEquals("0.00", compra.getValorPuntosRedimidos());
        assertDecimalEquals("4000", compra.getTotal());
    }

    @Test
    void confirmarPagoCambiaEstado() {
        Compra compra = crearCompra();

        compra.confirmarPago();

        assertInstanceOf(CompraPagada.class, compra.getEstado());
        assertEquals("PAGADA", compra.consultarNombreEstado());
    }

    @Test
    void solicitarCancelacionCambiaEstado() {
        Compra compra = crearCompra();

        compra.solicitarCancelacion();

        assertInstanceOf(CompraCancelada.class, compra.getEstado());
        assertEquals("CANCELADA", compra.consultarNombreEstado());
    }

    @Test
    void solicitarReembolsoDeCompraPendienteNoEsPermitido() {
        Compra compra = crearCompra();

        assertThrows(IllegalStateException.class, compra::solicitarReembolso);
    }

    @Test
    void cancelarCompraPagada() {
        Compra compra = crearCompra();

        compra.confirmarPago();
        compra.solicitarCancelacion();

        assertInstanceOf(CompraCancelada.class, compra.getEstado());
    }

    @Test
    void reembolsarCompraCancelada() {
        Compra compra = crearCompra();

        compra.solicitarCancelacion();
        compra.solicitarReembolso();

        assertInstanceOf(CompraReembolsada.class, compra.getEstado());
        assertEquals("REEMBOLSADA", compra.consultarNombreEstado());
    }

    @Test
    void rechazarPagarCompraPagadaDosVeces() {
        Compra compra = crearCompra();
        compra.confirmarPago();

        assertThrows(IllegalStateException.class, compra::confirmarPago);
    }

    @Test
    void cambiarEstadoManualmente() {
        Compra compra = crearCompra();
        EstadoCompra nuevoEstado = new CompraCancelada();

        compra.cambiarEstado(nuevoEstado);

        assertSame(nuevoEstado, compra.getEstado());
    }

    @Test
    void rechazarCambioDeEstadoNulo() {
        Compra compra = crearCompra();

        assertThrows(NullPointerException.class, () ->
                compra.cambiarEstado(null)
        );
    }

    @Test
    void listaDeDetallesNoPuedeModificarseDesdeFuera() {
        Compra compra = crearCompra();
        compra.agregarDetalle(crearDetalle("D01", "5000", 1));

        assertThrows(UnsupportedOperationException.class, () ->
                compra.getDetalles().clear()
        );
    }
}