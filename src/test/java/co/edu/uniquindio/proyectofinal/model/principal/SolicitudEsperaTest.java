package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoSolicitudEspera;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SolicitudEsperaTest {

    private Cliente cliente;
    private SolicitudEspera solicitud;
    private LocalDateTime fechaSolicitud;

    @BeforeEach
    void setUp() {
        cliente = new Cliente(
                "C001",
                "Juan Pérez",
                "juan@correo.com",
                "hash"
        );

        fechaSolicitud = LocalDateTime.now();

        solicitud = new SolicitudEspera(
                "SOL001",
                cliente,
                fechaSolicitud,
                2
        );
    }

    @Test
    void constructorDebeCrearSolicitudCorrectamente() {
        assertEquals("SOL001", solicitud.getCodigo());
        assertSame(cliente, solicitud.getCliente());
        assertEquals(fechaSolicitud, solicitud.getFechaSolicitud());
        assertEquals(2, solicitud.getCantidadAsientos());
        assertEquals(EstadoSolicitudEspera.ACTIVA, solicitud.getEstado());
        assertNull(solicitud.getFechaExpiracionOferta());
    }

    @Test
    void constructorDebeEliminarEspaciosDelCodigo() {
        SolicitudEspera resultado = new SolicitudEspera(
                " SOL002 ", cliente, fechaSolicitud, 1
        );

        assertEquals("SOL002", resultado.getCodigo());
    }

    @Test
    void constructorDebeRechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new SolicitudEspera(null, cliente, fechaSolicitud, 1));
    }

    @Test
    void constructorDebeRechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new SolicitudEspera(" ", cliente, fechaSolicitud, 1));
    }

    @Test
    void constructorDebeRechazarClienteNulo() {
        assertThrows(NullPointerException.class, () ->
                new SolicitudEspera("SOL002", null, fechaSolicitud, 1));
    }

    @Test
    void constructorDebeRechazarFechaSolicitudNula() {
        assertThrows(NullPointerException.class, () ->
                new SolicitudEspera("SOL002", cliente, null, 1));
    }

    @Test
    void constructorDebeRechazarCantidadDeAsientosCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new SolicitudEspera("SOL002", cliente, fechaSolicitud, 0));
    }

    @Test
    void constructorDebeRechazarCantidadDeAsientosNegativa() {
        assertThrows(IllegalArgumentException.class, () ->
                new SolicitudEspera("SOL002", cliente, fechaSolicitud, -1));
    }

    @Test
    void constructorDebeAceptarUnAsiento() {
        SolicitudEspera resultado = new SolicitudEspera(
                "SOL002", cliente, fechaSolicitud, 1
        );

        assertEquals(1, resultado.getCantidadAsientos());
    }

    @Test
    void constructorDebeAceptarVariosAsientos() {
        SolicitudEspera resultado = new SolicitudEspera(
                "SOL002", cliente, fechaSolicitud, 8
        );

        assertEquals(8, resultado.getCantidadAsientos());
    }

    @Test
    void notificarDisponibilidadDebeCambiarEstado() {
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(10);

        solicitud.notificarDisponibilidad(expiracion);

        assertEquals(EstadoSolicitudEspera.NOTIFICADA, solicitud.getEstado());
        assertEquals(expiracion, solicitud.getFechaExpiracionOferta());
    }

    @Test
    void notificarDisponibilidadDebeRechazarFechaNula() {
        assertThrows(NullPointerException.class, () ->
                solicitud.notificarDisponibilidad(null));

        assertEquals(EstadoSolicitudEspera.ACTIVA, solicitud.getEstado());
    }

    @Test
    void notificarDisponibilidadDebeRechazarFechaPasada() {
        assertThrows(IllegalArgumentException.class, () ->
                solicitud.notificarDisponibilidad(
                        LocalDateTime.now().minusMinutes(1)
                ));

        assertEquals(EstadoSolicitudEspera.ACTIVA, solicitud.getEstado());
    }

    @Test
    void notificarDisponibilidadDebeRechazarFechaActual() {
        assertThrows(IllegalArgumentException.class, () ->
                solicitud.notificarDisponibilidad(LocalDateTime.now()));
    }

    @Test
    void noDebeNotificarDosVecesLaMismaSolicitud() {
        solicitud.notificarDisponibilidad(
                LocalDateTime.now().plusMinutes(10)
        );

        assertThrows(IllegalStateException.class, () ->
                solicitud.notificarDisponibilidad(
                        LocalDateTime.now().plusMinutes(20)
                ));
    }

    @Test
    void atenderDebeCambiarEstadoAAtendida() {
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(10);
        solicitud.notificarDisponibilidad(expiracion);

        solicitud.atender(LocalDateTime.now().plusMinutes(1));

        assertEquals(EstadoSolicitudEspera.ATENDIDA, solicitud.getEstado());
    }

    @Test
    void atenderDebeRechazarSolicitudSinNotificar() {
        assertThrows(IllegalStateException.class, () ->
                solicitud.atender(LocalDateTime.now()));
    }



    @Test
    void atenderDebeExpirarOfertaCuandoSeAlcanzaElLimite() {
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(10);
        solicitud.notificarDisponibilidad(expiracion);

        assertThrows(IllegalStateException.class, () ->
                solicitud.atender(expiracion));

        assertEquals(EstadoSolicitudEspera.EXPIRADA, solicitud.getEstado());
    }

    @Test
    void atenderDebeExpirarOfertaCuandoSeSuperaElLimite() {
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(10);
        solicitud.notificarDisponibilidad(expiracion);

        assertThrows(IllegalStateException.class, () ->
                solicitud.atender(expiracion.plusSeconds(1)));

        assertEquals(EstadoSolicitudEspera.EXPIRADA, solicitud.getEstado());
    }

    @Test
    void expirarDebeCambiarEstadoCuandoSeAlcanzaElLimite() {
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(10);
        solicitud.notificarDisponibilidad(expiracion);

        solicitud.expirar(expiracion);

        assertEquals(EstadoSolicitudEspera.EXPIRADA, solicitud.getEstado());
    }

    @Test
    void expirarNoDebeCambiarEstadoAntesDelLimite() {
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(10);
        solicitud.notificarDisponibilidad(expiracion);

        solicitud.expirar(expiracion.minusMinutes(1));

        assertEquals(EstadoSolicitudEspera.NOTIFICADA, solicitud.getEstado());
    }

    @Test
    void expirarNoDebeCambiarEstadoConInstanteNulo() {
        solicitud.notificarDisponibilidad(
                LocalDateTime.now().plusMinutes(10)
        );

        solicitud.expirar(null);

        assertEquals(EstadoSolicitudEspera.NOTIFICADA, solicitud.getEstado());
    }

    @Test
    void expirarNoDebeCambiarSolicitudActiva() {
        solicitud.expirar(LocalDateTime.now().plusDays(1));

        assertEquals(EstadoSolicitudEspera.ACTIVA, solicitud.getEstado());
    }

    @Test
    void cancelarDebeCambiarSolicitudActivaACancelada() {
        solicitud.cancelar();

        assertEquals(EstadoSolicitudEspera.CANCELADA, solicitud.getEstado());
    }

    @Test
    void cancelarDebeCambiarSolicitudNotificadaACancelada() {
        solicitud.notificarDisponibilidad(
                LocalDateTime.now().plusMinutes(10)
        );

        solicitud.cancelar();

        assertEquals(EstadoSolicitudEspera.CANCELADA, solicitud.getEstado());
    }

    @Test
    void cancelarNoDebeCambiarSolicitudAtendida() {
        solicitud.notificarDisponibilidad(
                LocalDateTime.now().plusMinutes(10)
        );
        solicitud.atender(LocalDateTime.now().plusMinutes(1));

        solicitud.cancelar();

        assertEquals(EstadoSolicitudEspera.ATENDIDA, solicitud.getEstado());
    }

    @Test
    void cancelarNoDebeCambiarSolicitudExpirada() {
        LocalDateTime expiracion = LocalDateTime.now().plusMinutes(10);
        solicitud.notificarDisponibilidad(expiracion);
        solicitud.expirar(expiracion);

        solicitud.cancelar();

        assertEquals(EstadoSolicitudEspera.EXPIRADA, solicitud.getEstado());
    }

    @Test
    void cancelarDosVecesDebeConservarEstadoCancelado() {
        solicitud.cancelar();
        solicitud.cancelar();

        assertEquals(EstadoSolicitudEspera.CANCELADA, solicitud.getEstado());
    }
}