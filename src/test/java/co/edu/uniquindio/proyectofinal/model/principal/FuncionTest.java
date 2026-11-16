
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;
import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FuncionTest {

    private Pelicula crearPelicula() {
        return new Pelicula(
                "P01",
                "Interestelar",
                "Una aventura espacial",
                169,
                "12+",
                "Ciencia ficción"
        );
    }

    private TipoSala crearTipoSala() {
        return new TipoSala(
                "TS01",
                "Sala estándar",
                "Sala convencional",
                Set.of(
                        FormatoProyeccion.DOS_D,
                        FormatoProyeccion.TRES_D
                ),
                false,
                false,
                false
        );
    }

    private Sala crearSala() {
        return new Sala("S01", "Sala 1", crearTipoSala());
    }

    private Funcion crearFuncion() {
        return new Funcion(
                "F01",
                crearPelicula(),
                crearSala(),
                LocalDateTime.now().plusDays(1),
                new BigDecimal("15000"),
                FormatoProyeccion.DOS_D
        );
    }

    @Test
    void crearFuncionCorrectamente() {
        Funcion funcion = crearFuncion();

        assertEquals("F01", funcion.getCodigo());
        assertEquals("Interestelar", funcion.getPelicula().getTitulo());
        assertEquals("S01", funcion.getSala().getCodigo());
        assertEquals(new BigDecimal("15000"), funcion.getPrecioBase());
        assertEquals(FormatoProyeccion.DOS_D, funcion.getFormatoProyeccion());
        assertEquals(EstadoFuncion.PROGRAMADA, funcion.getEstado());
    }

    @Test
    void eliminarEspaciosDelCodigo() {
        Funcion funcion = new Funcion(
                " F01 ",
                crearPelicula(),
                crearSala(),
                LocalDateTime.now().plusDays(1),
                new BigDecimal("15000"),
                FormatoProyeccion.DOS_D
        );

        assertEquals("F01", funcion.getCodigo());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Funcion(
                        null, crearPelicula(), crearSala(),
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("15000"),
                        FormatoProyeccion.DOS_D
                )
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Funcion(
                        " ", crearPelicula(), crearSala(),
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("15000"),
                        FormatoProyeccion.DOS_D
                )
        );
    }

    @Test
    void rechazarPeliculaNula() {
        assertThrows(NullPointerException.class, () ->
                new Funcion(
                        "F01", null, crearSala(),
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("15000"),
                        FormatoProyeccion.DOS_D
                )
        );
    }

    @Test
    void rechazarSalaNula() {
        assertThrows(NullPointerException.class, () ->
                new Funcion(
                        "F01", crearPelicula(), null,
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("15000"),
                        FormatoProyeccion.DOS_D
                )
        );
    }

    @Test
    void rechazarFechaHoraNula() {
        assertThrows(NullPointerException.class, () ->
                new Funcion(
                        "F01", crearPelicula(), crearSala(),
                        null,
                        new BigDecimal("15000"),
                        FormatoProyeccion.DOS_D
                )
        );
    }

    @Test
    void rechazarFormatoNulo() {
        assertThrows(NullPointerException.class, () ->
                new Funcion(
                        "F01", crearPelicula(), crearSala(),
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("15000"),
                        null
                )
        );
    }

    @Test
    void rechazarPrecioNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Funcion(
                        "F01", crearPelicula(), crearSala(),
                        LocalDateTime.now().plusDays(1),
                        null,
                        FormatoProyeccion.DOS_D
                )
        );
    }

    @Test
    void rechazarPrecioCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new Funcion(
                        "F01", crearPelicula(), crearSala(),
                        LocalDateTime.now().plusDays(1),
                        BigDecimal.ZERO,
                        FormatoProyeccion.DOS_D
                )
        );
    }

    @Test
    void rechazarPrecioNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Funcion(
                        "F01", crearPelicula(), crearSala(),
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("-1000"),
                        FormatoProyeccion.DOS_D
                )
        );
    }

    @Test
    void rechazarFormatoNoCompatibleConSala() {
        assertThrows(IllegalArgumentException.class, () ->
                new Funcion(
                        "F01",
                        crearPelicula(),
                        crearSala(),
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("15000"),
                        FormatoProyeccion.CUATRO_D
                )
        );
    }

    @Test
    void rechazarPeliculaInactiva() {
        Pelicula pelicula = crearPelicula();
        pelicula.desactivar();

        assertThrows(IllegalArgumentException.class, () ->
                new Funcion(
                        "F01",
                        pelicula,
                        crearSala(),
                        LocalDateTime.now().plusDays(1),
                        new BigDecimal("15000"),
                        FormatoProyeccion.DOS_D
                )
        );
    }

    @Test
    void validarCompatibilidadCuandoElFormatoEsAdmitido() {
        Funcion funcion = crearFuncion();

        assertDoesNotThrow(funcion::validarCompatibilidad);
    }

    @Test
    void validarQuePuedeVenderEntradasAntesDeLaFuncion() {
        Funcion funcion = crearFuncion();

        assertTrue(funcion.puedeVenderEntradas(LocalDateTime.now()));
    }

    @Test
    void noPermitirVentaCuandoLaFuncionYaComenzo() {
        Funcion funcion = crearFuncion();

        LocalDateTime instante = funcion.getFechaHora().plusMinutes(1);

        assertFalse(funcion.puedeVenderEntradas(instante));
    }

    @Test
    void noPermitirVentaConInstanteNulo() {
        Funcion funcion = crearFuncion();

        assertFalse(funcion.puedeVenderEntradas(null));
    }

    @Test
    void actualizarHorarioCorrectamente() {
        Funcion funcion = crearFuncion();
        LocalDateTime nuevaFecha = LocalDateTime.now().plusDays(3);

        funcion.actualizarHorario(nuevaFecha);

        assertEquals(nuevaFecha, funcion.getFechaHora());
    }

    @Test
    void rechazarActualizacionConHorarioNulo() {
        Funcion funcion = crearFuncion();

        assertThrows(NullPointerException.class, () ->
                funcion.actualizarHorario(null)
        );
    }

    @Test
    void actualizarPrecioBaseCorrectamente() {
        Funcion funcion = crearFuncion();
        BigDecimal nuevoPrecio = new BigDecimal("20000");

        funcion.actualizarPrecioBase(nuevoPrecio);

        assertEquals(nuevoPrecio, funcion.getPrecioBase());
    }

    @Test
    void rechazarActualizacionConPrecioNulo() {
        Funcion funcion = crearFuncion();

        assertThrows(IllegalArgumentException.class, () ->
                funcion.actualizarPrecioBase(null)
        );
    }

    @Test
    void rechazarActualizacionConPrecioCero() {
        Funcion funcion = crearFuncion();

        assertThrows(IllegalArgumentException.class, () ->
                funcion.actualizarPrecioBase(BigDecimal.ZERO)
        );
    }

    @Test
    void cancelarFuncion() {
        Funcion funcion = crearFuncion();

        funcion.cancelar();

        assertEquals(EstadoFuncion.CANCELADA, funcion.getEstado());
    }

    @Test
    void noPermitirVentaDespuesDeCancelar() {
        Funcion funcion = crearFuncion();

        funcion.cancelar();

        assertFalse(funcion.puedeVenderEntradas(LocalDateTime.now()));
    }

    @Test
    void finalizarFuncionProgramada() {
        Funcion funcion = crearFuncion();

        funcion.finalizar();

        assertEquals(EstadoFuncion.FINALIZADA, funcion.getEstado());
    }

    @Test
    void noPermitirFinalizarFuncionCancelada() {
        Funcion funcion = crearFuncion();
        funcion.cancelar();

        assertThrows(IllegalStateException.class, funcion::finalizar);
    }

    @Test
    void noPermitirFinalizarFuncionDosVeces() {
        Funcion funcion = crearFuncion();
        funcion.finalizar();

        assertThrows(IllegalStateException.class, funcion::finalizar);
    }

    @Test
    void noPermitirCancelarFuncionFinalizada() {
        Funcion funcion = crearFuncion();
        funcion.finalizar();

        assertThrows(IllegalStateException.class, funcion::cancelar);
    }

    @Test
    void permitirCancelarFuncionYaCancelada() {
        Funcion funcion = crearFuncion();

        funcion.cancelar();
        funcion.cancelar();

        assertEquals(EstadoFuncion.CANCELADA, funcion.getEstado());
    }
}
