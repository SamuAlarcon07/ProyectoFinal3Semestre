
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoResena;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ResenaPeliculaTest {

    private Cliente cliente;
    private Pelicula pelicula;
    private ResenaPelicula resena;

    @BeforeEach
    void setUp() {
        cliente = new Cliente(
                "C001",
                "Juan Pérez",
                "juan@correo.com",
                "hash"
        );

        pelicula = new Pelicula(
                "P001",
                "Interestelar",
                "Película de ciencia ficción",
                169,
                "PG-13",
                "Ciencia ficción"
        );

        resena = new ResenaPelicula(
                "R001",
                cliente,
                pelicula,
                5,
                "Excelente película"
        );
    }

    @Test
    void constructorDebeCrearResenaCorrectamente() {
        assertEquals("R001", resena.getCodigo());
        assertSame(cliente, resena.getCliente());
        assertSame(pelicula, resena.getPelicula());
        assertEquals(5, resena.getCalificacion());
        assertEquals("Excelente película", resena.getComentario());
        assertEquals(EstadoResena.PUBLICADA, resena.getEstado());
    }

    @Test
    void constructorDebeAsignarFechaDePublicacion() {
        assertNotNull(resena.getFechaPublicacion());
        assertFalse(resena.getFechaPublicacion().isAfter(LocalDateTime.now()));
    }

    @Test
    void constructorDebeEliminarEspaciosDelCodigo() {
        ResenaPelicula resultado = new ResenaPelicula(
                " R002 ", cliente, pelicula, 4, "Buena película"
        );

        assertEquals("R002", resultado.getCodigo());
    }

    @Test
    void constructorDebeRechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new ResenaPelicula(
                        null, cliente, pelicula, 5, "Comentario"
                ));
    }

    @Test
    void constructorDebeRechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new ResenaPelicula(
                        " ", cliente, pelicula, 5, "Comentario"
                ));
    }

    @Test
    void constructorDebeRechazarClienteNulo() {
        assertThrows(NullPointerException.class, () ->
                new ResenaPelicula(
                        "R002", null, pelicula, 5, "Comentario"
                ));
    }

    @Test
    void constructorDebeRechazarPeliculaNula() {
        assertThrows(NullPointerException.class, () ->
                new ResenaPelicula(
                        "R002", cliente, null, 5, "Comentario"
                ));
    }

    @Test
    void constructorDebeRechazarCalificacionMenorAUno() {
        assertThrows(IllegalArgumentException.class, () ->
                new ResenaPelicula(
                        "R002", cliente, pelicula, 0, "Comentario"
                ));
    }

    @Test
    void constructorDebeRechazarCalificacionMayorACinco() {
        assertThrows(IllegalArgumentException.class, () ->
                new ResenaPelicula(
                        "R002", cliente, pelicula, 6, "Comentario"
                ));
    }

    @Test
    void constructorDebeAceptarCalificacionUno() {
        ResenaPelicula resultado = new ResenaPelicula(
                "R002", cliente, pelicula, 1, "Comentario"
        );

        assertEquals(1, resultado.getCalificacion());
    }

    @Test
    void constructorDebeAceptarCalificacionCinco() {
        ResenaPelicula resultado = new ResenaPelicula(
                "R002", cliente, pelicula, 5, "Comentario"
        );

        assertEquals(5, resultado.getCalificacion());
    }

    @Test
    void constructorDebeRechazarComentarioNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new ResenaPelicula(
                        "R002", cliente, pelicula, 4, null
                ));
    }

    @Test
    void constructorDebeRechazarComentarioVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new ResenaPelicula(
                        "R002", cliente, pelicula, 4, " "
                ));
    }

    @Test
    void constructorDebeEliminarEspaciosDelComentario() {
        ResenaPelicula resultado = new ResenaPelicula(
                "R002", cliente, pelicula, 4, "  Muy buena  "
        );

        assertEquals("Muy buena", resultado.getComentario());
    }

    @Test
    void editarResenaDebeActualizarCalificacionYComentario() {
        resena.editarResena(3, "Está bien");

        assertEquals(3, resena.getCalificacion());
        assertEquals("Está bien", resena.getComentario());
    }

    @Test
    void editarResenaDebeActualizarFechaPublicacion() throws InterruptedException {
        LocalDateTime fechaAnterior = resena.getFechaPublicacion();

        Thread.sleep(10);
        resena.editarResena(4, "Nueva opinión");

        assertFalse(resena.getFechaPublicacion().isBefore(fechaAnterior));
    }

    @Test
    void editarResenaDebeRechazarCalificacionMenorAUno() {
        assertThrows(IllegalArgumentException.class, () ->
                resena.editarResena(0, "Comentario nuevo"));
    }

    @Test
    void editarResenaDebeRechazarCalificacionMayorACinco() {
        assertThrows(IllegalArgumentException.class, () ->
                resena.editarResena(6, "Comentario nuevo"));
    }

    @Test
    void editarResenaDebeRechazarComentarioNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                resena.editarResena(4, null));
    }

    @Test
    void editarResenaDebeRechazarComentarioVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                resena.editarResena(4, " "));
    }

    @Test
    void publicarDebeCambiarEstadoApublicada() {
        resena.ocultar();

        resena.publicar();

        assertEquals(EstadoResena.PUBLICADA, resena.getEstado());
        assertTrue(resena.estaVisible());
    }

    @Test
    void ocultarDebeCambiarEstadoAOcultada() {
        resena.ocultar();

        assertEquals(EstadoResena.OCULTA, resena.getEstado());
        assertFalse(resena.estaVisible());
    }

    @Test
    void resenaNuevaDebeEstarVisible() {
        assertTrue(resena.estaVisible());
    }

    @Test
    void publicarUnaResenaYaPublicadaDebeMantenerlaVisible() {
        resena.publicar();

        assertTrue(resena.estaVisible());
        assertEquals(EstadoResena.PUBLICADA, resena.getEstado());
    }

    @Test
    void ocultarUnaResenaYaOcultaDebeMantenerlaOculta() {
        resena.ocultar();
        resena.ocultar();

        assertFalse(resena.estaVisible());
        assertEquals(EstadoResena.OCULTA, resena.getEstado());
    }

    @Test
    void gettersDebenDevolverLosObjetosOriginales() {
        assertSame(cliente, resena.getCliente());
        assertSame(pelicula, resena.getPelicula());
    }
}