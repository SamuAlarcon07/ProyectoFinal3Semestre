
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoReserva;
import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import co.edu.uniquindio.proyectofinal.model.enums.TipoAsiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReservaAsientoTest {

    private ReservaAsiento reserva;
    private Funcion funcion;
    private Asiento asiento;
    private Cliente cliente;
    private LocalDateTime fechaReserva;
    private LocalDateTime fechaExpiracion;

    @BeforeEach
    void setUp() {
        TipoSala tipoSala = new TipoSala(
                "TS01",
                "Sala estándar",
                "Sala de proyección estándar",
                Set.of(FormatoProyeccion.DOS_D),
                false,
                false,
                false
        );

        Sala sala = new Sala("S01", "Sala 1", tipoSala);

        asiento = new Asiento("A", 1, TipoAsiento.GENERAL);
        sala.agregarAsiento(asiento);

        Pelicula pelicula = new Pelicula(
                "P01",
                "Interestelar",
                "Película de ciencia ficción",
                169,
                "PG-13",
                "Ciencia ficción"
        );

        funcion = new Funcion(
                "F01",
                pelicula,
                sala,
                LocalDateTime.now().plusDays(1),
                new BigDecimal("15000"),
                FormatoProyeccion.DOS_D
        );

        cliente = new Cliente(
                "C001",
                "Juan Pérez",
                "juan@correo.com",
                "hash"
        );

        fechaReserva = LocalDateTime.of(2026, 11, 16, 10, 0);
        fechaExpiracion = fechaReserva.plusMinutes(15);

        reserva = new ReservaAsiento(
                "RES001",
                funcion,
                asiento,
                cliente,
                fechaReserva,
                fechaExpiracion
        );
    }

    @Test
    void constructorDebeCrearReservaCorrectamente() {
        assertEquals("RES001", reserva.getCodigo());
        assertSame(funcion, reserva.getFuncion());
        assertSame(asiento, reserva.getAsiento());
        assertSame(cliente, reserva.getCliente());
        assertEquals(fechaReserva, reserva.getFechaReserva());
        assertEquals(fechaExpiracion, reserva.getFechaExpiracion());
        assertEquals(EstadoReserva.ACTIVA, reserva.getEstado());
    }

    @Test
    void constructorDebeEliminarEspaciosDelCodigo() {
        ReservaAsiento resultado = new ReservaAsiento(
                " RES002 ",
                funcion,
                asiento,
                cliente,
                fechaReserva,
                fechaExpiracion
        );

        assertEquals("RES002", resultado.getCodigo());
    }

    @Test
    void constructorDebeRechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReservaAsiento(
                        null, funcion, asiento, cliente,
                        fechaReserva, fechaExpiracion
                ));
    }

    @Test
    void constructorDebeRechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReservaAsiento(
                        " ", funcion, asiento, cliente,
                        fechaReserva, fechaExpiracion
                ));
    }

    @Test
    void constructorDebeRechazarFuncionNula() {
        assertThrows(NullPointerException.class, () ->
                new ReservaAsiento(
                        "RES002", null, asiento, cliente,
                        fechaReserva, fechaExpiracion
                ));
    }

    @Test
    void constructorDebeRechazarAsientoNulo() {
        assertThrows(NullPointerException.class, () ->
                new ReservaAsiento(
                        "RES002", funcion, null, cliente,
                        fechaReserva, fechaExpiracion
                ));
    }

    @Test
    void constructorDebeRechazarClienteNulo() {
        assertThrows(NullPointerException.class, () ->
                new ReservaAsiento(
                        "RES002", funcion, asiento, null,
                        fechaReserva, fechaExpiracion
                ));
    }

    @Test
    void constructorDebeRechazarFechaReservaNula() {
        assertThrows(NullPointerException.class, () ->
                new ReservaAsiento(
                        "RES002", funcion, asiento, cliente,
                        null, fechaExpiracion
                ));
    }

    @Test
    void constructorDebeRechazarFechaExpiracionNula() {
        assertThrows(NullPointerException.class, () ->
                new ReservaAsiento(
                        "RES002", funcion, asiento, cliente,
                        fechaReserva, null
                ));
    }

    @Test
    void constructorDebeRechazarExpiracionAnteriorAReserva() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReservaAsiento(
                        "RES002", funcion, asiento, cliente,
                        fechaReserva, fechaReserva.minusMinutes(1)
                ));
    }

    @Test
    void constructorDebeRechazarExpiracionIgualAReserva() {
        assertThrows(IllegalArgumentException.class, () ->
                new ReservaAsiento(
                        "RES002", funcion, asiento, cliente,
                        fechaReserva, fechaReserva
                ));
    }

    @Test
    void constructorDebeRechazarAsientoQueNoPerteneceALaSala() {
        Asiento otroAsiento = new Asiento(
                "B", 2, TipoAsiento.GENERAL
        );

        assertThrows(IllegalArgumentException.class, () ->
                new ReservaAsiento(
                        "RES002", funcion, otroAsiento, cliente,
                        fechaReserva, fechaExpiracion
                ));
    }

    @Test
    void constructorDebeRechazarAsientoInactivo() {
        asiento.desactivar();

        assertThrows(IllegalArgumentException.class, () ->
                new ReservaAsiento(
                        "RES002", funcion, asiento, cliente,
                        fechaReserva, fechaExpiracion
                ));
    }

    @Test
    void reservaNuevaDebeEstarVigenteAntesDeExpirar() {
        assertTrue(reserva.estaVigente(
                fechaExpiracion.minusMinutes(1)
        ));
    }

    @Test
    void reservaDebeEstarVigenteEnLaFechaDeReserva() {
        assertTrue(reserva.estaVigente(fechaReserva));
    }

    @Test
    void reservaNoDebeEstarVigenteEnLaFechaDeExpiracion() {
        assertFalse(reserva.estaVigente(fechaExpiracion));
    }

    @Test
    void reservaNoDebeEstarVigenteDespuesDeExpirar() {
        assertFalse(reserva.estaVigente(
                fechaExpiracion.plusSeconds(1)
        ));
    }

    @Test
    void estaVigenteDebeRetornarFalseConInstanteNulo() {
        assertFalse(reserva.estaVigente(null));
    }

    @Test
    void confirmarDebeCambiarEstadoAConfirmada() {
        reserva.confirmar(fechaReserva.plusMinutes(5));

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void confirmarDebeRechazarInstanteNulo() {
        assertThrows(IllegalStateException.class, () ->
                reserva.confirmar(null));
    }

    @Test
    void confirmarDebeRechazarReservaExpirada() {
        assertThrows(IllegalStateException.class, () ->
                reserva.confirmar(fechaExpiracion));
    }

    @Test
    void confirmarDebeRechazarReservaYaConfirmada() {
        reserva.confirmar(fechaReserva.plusMinutes(1));

        assertThrows(IllegalStateException.class, () ->
                reserva.confirmar(fechaReserva.plusMinutes(2)));
    }

    @Test
    void cancelarDebeCambiarEstadoACancelada() {
        reserva.cancelar();

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }

    @Test
    void cancelarDebeRechazarReservaConfirmada() {
        reserva.confirmar(fechaReserva.plusMinutes(1));

        assertThrows(IllegalStateException.class, reserva::cancelar);
    }

    @Test
    void cancelarReservaCanceladaDebeConservarEstado() {
        reserva.cancelar();
        reserva.cancelar();

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }

    @Test
    void expirarDebeCambiarEstadoCuandoSeAlcanzaLaFechaLimite() {
        reserva.expirar(fechaExpiracion);

        assertEquals(EstadoReserva.EXPIRADA, reserva.getEstado());
    }

    @Test
    void expirarDebeCambiarEstadoDespuesDeLaFechaLimite() {
        reserva.expirar(fechaExpiracion.plusMinutes(1));

        assertEquals(EstadoReserva.EXPIRADA, reserva.getEstado());
    }

    @Test
    void expirarNoDebeCambiarEstadoAntesDeLaFechaLimite() {
        reserva.expirar(fechaExpiracion.minusMinutes(1));

        assertEquals(EstadoReserva.ACTIVA, reserva.getEstado());
    }

    @Test
    void expirarNoDebeCambiarEstadoConInstanteNulo() {
        reserva.expirar(null);

        assertEquals(EstadoReserva.ACTIVA, reserva.getEstado());
    }

    @Test
    void expirarNoDebeCambiarUnaReservaConfirmada() {
        reserva.confirmar(fechaReserva.plusMinutes(1));

        reserva.expirar(fechaExpiracion.plusMinutes(1));

        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void expirarNoDebeCambiarUnaReservaCancelada() {
        reserva.cancelar();

        reserva.expirar(fechaExpiracion.plusMinutes(1));

        assertEquals(EstadoReserva.CANCELADA, reserva.getEstado());
    }

    @Test
    void gettersDebenDevolverLosObjetosOriginales() {
        assertSame(funcion, reserva.getFuncion());
        assertSame(asiento, reserva.getAsiento());
        assertSame(cliente, reserva.getCliente());
    }
}