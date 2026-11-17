package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoPelicula;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PeliculaTest {

    private Pelicula crearPelicula() {
        return new Pelicula(
                "P001",
                "Interestelar",
                "Una aventura espacial",
                169,
                "12+",
                "Ciencia ficción"
        );
    }

    @Test
    void crearPeliculaCorrectamente() {
        Pelicula pelicula = crearPelicula();

        assertEquals("P001", pelicula.getCodigo());
        assertEquals("Interestelar", pelicula.getTitulo());
        assertEquals("Una aventura espacial", pelicula.getSinopsis());
        assertEquals(169, pelicula.getDuracionMinutos());
        assertEquals("12+", pelicula.getClasificacion());
        assertEquals("Ciencia ficción", pelicula.getGenero());
        assertEquals(EstadoPelicula.ACTIVA, pelicula.getEstado());
        assertTrue(pelicula.estaActiva());
    }

    @Test
    void eliminarEspaciosDeLosDatos() {
        Pelicula pelicula = new Pelicula(
                " P001 ",
                " Interestelar ",
                " Una aventura espacial ",
                169,
                " 12+ ",
                " Ciencia ficción "
        );

        assertEquals("P001", pelicula.getCodigo());
        assertEquals("Interestelar", pelicula.getTitulo());
        assertEquals("Una aventura espacial", pelicula.getSinopsis());
        assertEquals("12+", pelicula.getClasificacion());
        assertEquals("Ciencia ficción", pelicula.getGenero());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        null, "Interestelar", "Sinopsis",
                        169, "12+", "Ciencia ficción"
                )
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        " ", "Interestelar", "Sinopsis",
                        169, "12+", "Ciencia ficción"
                )
        );
    }

    @Test
    void rechazarTituloNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        "P001", null, "Sinopsis",
                        169, "12+", "Ciencia ficción"
                )
        );
    }

    @Test
    void rechazarTituloVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        "P001", " ", "Sinopsis",
                        169, "12+", "Ciencia ficción"
                )
        );
    }

    @Test
    void rechazarSinopsisNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        "P001", "Interestelar", null,
                        169, "12+", "Ciencia ficción"
                )
        );
    }

    @Test
    void rechazarSinopsisVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        "P001", "Interestelar", " ",
                        169, "12+", "Ciencia ficción"
                )
        );
    }

    @Test
    void rechazarDuracionCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        "P001", "Interestelar", "Sinopsis",
                        0, "12+", "Ciencia ficción"
                )
        );
    }

    @Test
    void rechazarDuracionNegativa() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        "P001", "Interestelar", "Sinopsis",
                        -10, "12+", "Ciencia ficción"
                )
        );
    }

    @Test
    void rechazarClasificacionNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        "P001", "Interestelar", "Sinopsis",
                        169, null, "Ciencia ficción"
                )
        );
    }

    @Test
    void rechazarClasificacionVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        "P001", "Interestelar", "Sinopsis",
                        169, " ", "Ciencia ficción"
                )
        );
    }

    @Test
    void rechazarGeneroNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        "P001", "Interestelar", "Sinopsis",
                        169, "12+", null
                )
        );
    }

    @Test
    void rechazarGeneroVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Pelicula(
                        "P001", "Interestelar", "Sinopsis",
                        169, "12+", " "
                )
        );
    }

    @Test
    void aceptarDuracionMinimaPositiva() {
        Pelicula pelicula = new Pelicula(
                "P001",
                "Cortometraje",
                "Una historia corta",
                1,
                "TP",
                "Drama"
        );

        assertEquals(1, pelicula.getDuracionMinutos());
    }

    @Test
    void actualizarInformacionCorrectamente() {
        Pelicula pelicula = crearPelicula();

        pelicula.actualizarInformacion(
                "El origen",
                "Una historia sobre los sueños",
                148,
                "15+",
                "Acción"
        );

        assertEquals("El origen", pelicula.getTitulo());
        assertEquals(
                "Una historia sobre los sueños",
                pelicula.getSinopsis()
        );
        assertEquals(148, pelicula.getDuracionMinutos());
        assertEquals("15+", pelicula.getClasificacion());
        assertEquals("Acción", pelicula.getGenero());
    }

    @Test
    void eliminarEspaciosAlActualizarInformacion() {
        Pelicula pelicula = crearPelicula();

        pelicula.actualizarInformacion(
                " El origen ",
                " Una historia sobre los sueños ",
                148,
                " 15+ ",
                " Acción "
        );

        assertEquals("El origen", pelicula.getTitulo());
        assertEquals(
                "Una historia sobre los sueños",
                pelicula.getSinopsis()
        );
        assertEquals("15+", pelicula.getClasificacion());
        assertEquals("Acción", pelicula.getGenero());
    }

    @Test
    void rechazarActualizacionConTituloNulo() {
        Pelicula pelicula = crearPelicula();

        assertThrows(IllegalArgumentException.class, () ->
                pelicula.actualizarInformacion(
                        null,
                        "Nueva sinopsis",
                        120,
                        "12+",
                        "Drama"
                )
        );
    }

    @Test
    void rechazarActualizacionConSinopsisVacia() {
        Pelicula pelicula = crearPelicula();

        assertThrows(IllegalArgumentException.class, () ->
                pelicula.actualizarInformacion(
                        "Nuevo título",
                        " ",
                        120,
                        "12+",
                        "Drama"
                )
        );
    }

    @Test
    void rechazarActualizacionConDuracionCero() {
        Pelicula pelicula = crearPelicula();

        assertThrows(IllegalArgumentException.class, () ->
                pelicula.actualizarInformacion(
                        "Nuevo título",
                        "Nueva sinopsis",
                        0,
                        "12+",
                        "Drama"
                )
        );
    }

    @Test
    void rechazarActualizacionConClasificacionNula() {
        Pelicula pelicula = crearPelicula();

        assertThrows(IllegalArgumentException.class, () ->
                pelicula.actualizarInformacion(
                        "Nuevo título",
                        "Nueva sinopsis",
                        120,
                        null,
                        "Drama"
                )
        );
    }

    @Test
    void rechazarActualizacionConGeneroVacio() {
        Pelicula pelicula = crearPelicula();

        assertThrows(IllegalArgumentException.class, () ->
                pelicula.actualizarInformacion(
                        "Nuevo título",
                        "Nueva sinopsis",
                        120,
                        "12+",
                        " "
                )
        );
    }

    @Test
    void desactivarPeliculaCorrectamente() {
        Pelicula pelicula = crearPelicula();

        pelicula.desactivar();

        assertEquals(EstadoPelicula.INACTIVA, pelicula.getEstado());
        assertFalse(pelicula.estaActiva());
    }

    @Test
    void activarPeliculaCorrectamente() {
        Pelicula pelicula = crearPelicula();

        pelicula.desactivar();
        pelicula.activar();

        assertEquals(EstadoPelicula.ACTIVA, pelicula.getEstado());
        assertTrue(pelicula.estaActiva());
    }

    @Test
    void actualizarInformacionNoCambiaCodigo() {
        Pelicula pelicula = crearPelicula();

        pelicula.actualizarInformacion(
                "Nuevo título",
                "Nueva sinopsis",
                120,
                "15+",
                "Drama"
        );

        assertEquals("P001", pelicula.getCodigo());
    }

    @Test
    void actualizarInformacionNoCambiaEstado() {
        Pelicula pelicula = crearPelicula();
        pelicula.desactivar();

        pelicula.actualizarInformacion(
                "Nuevo título",
                "Nueva sinopsis",
                120,
                "15+",
                "Drama"
        );

        assertEquals(EstadoPelicula.INACTIVA, pelicula.getEstado());
        assertFalse(pelicula.estaActiva());
    }
}