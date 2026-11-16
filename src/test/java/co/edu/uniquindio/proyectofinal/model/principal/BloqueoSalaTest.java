
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoBloqueoSala;
import co.edu.uniquindio.proyectofinal.model.enums.TipoBloqueoSala;
import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BloqueoSalaTest {

    private BloqueoSala bloqueo;
    private Sala sala;
    private Administrador administrador;

    private LocalDateTime inicio;
    private LocalDateTime fin;

    @BeforeEach
    void setUp() {

        TipoSala tipoSala = new TipoSala(
                "TS01",
                "Sala tradicional",
                "Sala de cine tradicional",
                Set.of(FormatoProyeccion.DOS_D),
                false,
                false,
                false
        );

        sala = new Sala("S01", "Sala 1", tipoSala);

        administrador = new Administrador(
                "ADM01",
                "Administrador",
                "admin@cine.com",
                "hash123",
                "EMP01"
        );

        inicio = LocalDateTime.of(2026, 11, 16, 10, 0);
        fin = LocalDateTime.of(2026, 11, 16, 12, 0);

        bloqueo = new BloqueoSala(
                "BLQ01",
                sala,
                TipoBloqueoSala.MANTENIMIENTO,
                inicio,
                fin,
                "Mantenimiento de equipos",
                administrador
        );
    }

    // 1. Creación correcta del bloqueo
    @Test
    void testCrearBloqueoCorrectamente() {

        assertNotNull(bloqueo);
        assertEquals("BLQ01", bloqueo.getCodigo());
        assertEquals(sala, bloqueo.getSala());
        assertEquals(TipoBloqueoSala.MANTENIMIENTO,
                bloqueo.getTipoBloqueo());
        assertEquals(inicio, bloqueo.getFechaInicio());
        assertEquals(fin, bloqueo.getFechaFin());
        assertEquals("Mantenimiento de equipos", bloqueo.getMotivo());
        assertEquals(administrador, bloqueo.getAdministrador());
        assertEquals(EstadoBloqueoSala.PROGRAMADO,
                bloqueo.getEstado());
    }

    // 2. Validación del código vacío
    @Test
    void testCodigoVacio() {

        assertThrows(IllegalArgumentException.class, () ->
                new BloqueoSala(
                        " ",
                        sala,
                        TipoBloqueoSala.MANTENIMIENTO,
                        inicio,
                        fin,
                        "Mantenimiento",
                        administrador
                )
        );
    }

    // 3. Validación del código nulo
    @Test
    void testCodigoNulo() {

        assertThrows(IllegalArgumentException.class, () ->
                new BloqueoSala(
                        null,
                        sala,
                        TipoBloqueoSala.MANTENIMIENTO,
                        inicio,
                        fin,
                        "Mantenimiento",
                        administrador
                )
        );
    }

    // 4. Validación de sala nula
    @Test
    void testSalaNula() {

        assertThrows(NullPointerException.class, () ->
                new BloqueoSala(
                        "BLQ01",
                        null,
                        TipoBloqueoSala.MANTENIMIENTO,
                        inicio,
                        fin,
                        "Mantenimiento",
                        administrador
                )
        );
    }

    // 5. Validación del tipo de bloqueo nulo
    @Test
    void testTipoBloqueoNulo() {

        assertThrows(NullPointerException.class, () ->
                new BloqueoSala(
                        "BLQ01",
                        sala,
                        null,
                        inicio,
                        fin,
                        "Mantenimiento",
                        administrador
                )
        );
    }

    // 6. Validación de fecha inicial nula
    @Test
    void testFechaInicioNula() {

        assertThrows(NullPointerException.class, () ->
                new BloqueoSala(
                        "BLQ01",
                        sala,
                        TipoBloqueoSala.MANTENIMIENTO,
                        null,
                        fin,
                        "Mantenimiento",
                        administrador
                )
        );
    }

    // 7. Validación de fecha final nula
    @Test
    void testFechaFinNula() {

        assertThrows(NullPointerException.class, () ->
                new BloqueoSala(
                        "BLQ01",
                        sala,
                        TipoBloqueoSala.MANTENIMIENTO,
                        inicio,
                        null,
                        "Mantenimiento",
                        administrador
                )
        );
    }

    // 8. Validación de fechas iguales
    @Test
    void testFechasIguales() {

        assertThrows(IllegalArgumentException.class, () ->
                new BloqueoSala(
                        "BLQ01",
                        sala,
                        TipoBloqueoSala.MANTENIMIENTO,
                        inicio,
                        inicio,
                        "Mantenimiento",
                        administrador
                )
        );
    }

    // 9. Validación de fecha final anterior a la inicial
    @Test
    void testFechaFinalAnterior() {

        assertThrows(IllegalArgumentException.class, () ->
                new BloqueoSala(
                        "BLQ01",
                        sala,
                        TipoBloqueoSala.MANTENIMIENTO,
                        fin,
                        inicio,
                        "Mantenimiento",
                        administrador
                )
        );
    }

    // 10. Validación de motivo vacío
    @Test
    void testMotivoVacio() {

        assertThrows(IllegalArgumentException.class, () ->
                new BloqueoSala(
                        "BLQ01",
                        sala,
                        TipoBloqueoSala.MANTENIMIENTO,
                        inicio,
                        fin,
                        " ",
                        administrador
                )
        );
    }

    // 11. Validación de motivo nulo
    @Test
    void testMotivoNulo() {

        assertThrows(IllegalArgumentException.class, () ->
                new BloqueoSala(
                        "BLQ01",
                        sala,
                        TipoBloqueoSala.MANTENIMIENTO,
                        inicio,
                        fin,
                        null,
                        administrador
                )
        );
    }

    // 12. Validación de administrador nulo
    @Test
    void testAdministradorNulo() {

        assertThrows(NullPointerException.class, () ->
                new BloqueoSala(
                        "BLQ01",
                        sala,
                        TipoBloqueoSala.MANTENIMIENTO,
                        inicio,
                        fin,
                        "Mantenimiento",
                        null
                )
        );
    }

    // 13. Comprobar que los textos eliminan espacios externos
    @Test
    void testEliminarEspaciosExternos() {

        BloqueoSala nuevo = new BloqueoSala(
                " BLQ02 ",
                sala,
                TipoBloqueoSala.LIMPIEZA,
                inicio,
                fin,
                " Limpieza general ",
                administrador
        );

        assertEquals("BLQ02", nuevo.getCodigo());
        assertEquals("Limpieza general", nuevo.getMotivo());
    }

    // 14. Comprobar bloqueo vigente
    @Test
    void testEstaVigente() {

        LocalDateTime instante = inicio.plusMinutes(30);

        assertTrue(bloqueo.estaVigente(instante));
    }

    // 15. Comprobar bloqueo no vigente antes del inicio
    @Test
    void testNoEstaVigenteAntesDelInicio() {

        LocalDateTime instante = inicio.minusMinutes(1);

        assertFalse(bloqueo.estaVigente(instante));
    }

    // 16. Comprobar bloqueo no vigente después del final
    @Test
    void testNoEstaVigenteDespuesDelFinal() {

        LocalDateTime instante = fin;

        assertFalse(bloqueo.estaVigente(instante));
    }

    // 17. Comprobar instante nulo
    @Test
    void testEstaVigenteConInstanteNulo() {

        assertFalse(bloqueo.estaVigente(null));
    }

    // 18. Comprobar superposición de intervalos
    @Test
    void testSeSuperponeCon() {

        LocalDateTime otroInicio = inicio.plusMinutes(30);
        LocalDateTime otroFin = fin.plusMinutes(30);

        assertTrue(bloqueo.seSuperponeCon(otroInicio, otroFin));
    }

    // 19. Comprobar intervalo sin superposición
    @Test
    void testNoSeSuperponeCon() {

        LocalDateTime otroInicio = fin.plusHours(1);
        LocalDateTime otroFin = fin.plusHours(2);

        assertFalse(bloqueo.seSuperponeCon(otroInicio, otroFin));
    }

    // 20. Comprobar intervalo completamente anterior
    @Test
    void testIntervaloAnterior() {

        LocalDateTime otroInicio = inicio.minusHours(2);
        LocalDateTime otroFin = inicio;

        assertFalse(bloqueo.seSuperponeCon(otroInicio, otroFin));
    }

    // 21. Comprobar inicio nulo en superposición
    @Test
    void testSuperposicionInicioNulo() {

        assertThrows(NullPointerException.class, () ->
                bloqueo.seSuperponeCon(null, fin)
        );
    }

    // 22. Comprobar final nulo en superposición
    @Test
    void testSuperposicionFinNulo() {

        assertThrows(NullPointerException.class, () ->
                bloqueo.seSuperponeCon(inicio, null)
        );
    }

    // 23. Comprobar intervalo inválido
    @Test
    void testIntervaloInvalido() {

        assertThrows(IllegalArgumentException.class, () ->
                bloqueo.seSuperponeCon(fin, inicio)
        );
    }

    // 24. Iniciar bloqueo
    @Test
    void testIniciarBloqueo() {

        bloqueo.iniciar();

        assertEquals(EstadoBloqueoSala.EN_CURSO,
                bloqueo.getEstado());
    }

    // 25. Finalizar bloqueo
    @Test
    void testFinalizarBloqueo() {

        bloqueo.iniciar();
        bloqueo.finalizar();

        assertEquals(EstadoBloqueoSala.FINALIZADO,
                bloqueo.getEstado());
    }

    // 26. Cancelar bloqueo programado
    @Test
    void testCancelarBloqueoProgramado() {

        bloqueo.cancelar();

        assertEquals(EstadoBloqueoSala.CANCELADO,
                bloqueo.getEstado());
    }

    // 27. Cancelar bloqueo en curso
    @Test
    void testCancelarBloqueoEnCurso() {

        bloqueo.iniciar();
        bloqueo.cancelar();

        assertEquals(EstadoBloqueoSala.CANCELADO,
                bloqueo.getEstado());
    }

    // 28. No iniciar un bloqueo que ya está en curso
    @Test
    void testIniciarBloqueoEnCurso() {

        bloqueo.iniciar();

        assertThrows(IllegalStateException.class, () ->
                bloqueo.iniciar()
        );
    }

    // 29. No finalizar un bloqueo programado
    @Test
    void testFinalizarBloqueoProgramado() {

        assertThrows(IllegalStateException.class, () ->
                bloqueo.finalizar()
        );
    }

    // 30. No cancelar un bloqueo finalizado
    @Test
    void testCancelarBloqueoFinalizado() {

        bloqueo.iniciar();
        bloqueo.finalizar();

        assertThrows(IllegalStateException.class, () ->
                bloqueo.cancelar()
        );
    }

    // 31. Un bloqueo cancelado no puede estar vigente
    @Test
    void testBloqueoCanceladoNoEstaVigente() {

        bloqueo.cancelar();

        assertFalse(bloqueo.estaVigente(inicio.plusMinutes(30)));
    }

    // 32. Un bloqueo finalizado no puede estar vigente
    @Test
    void testBloqueoFinalizadoNoEstaVigente() {

        bloqueo.iniciar();
        bloqueo.finalizar();

        assertFalse(bloqueo.estaVigente(inicio.plusMinutes(30)));
    }

    // 33. Un bloqueo cancelado no debe generar superposición
    @Test
    void testBloqueoCanceladoNoSeSuperpone() {

        bloqueo.cancelar();

        assertFalse(bloqueo.seSuperponeCon(
                inicio.plusMinutes(10),
                fin.minusMinutes(10)
        ));
    }

    // 34. Un bloqueo finalizado no debe generar superposición
    @Test
    void testBloqueoFinalizadoNoSeSuperpone() {

        bloqueo.iniciar();
        bloqueo.finalizar();

        assertFalse(bloqueo.seSuperponeCon(
                inicio.plusMinutes(10),
                fin.minusMinutes(10)
        ));
    }
}
