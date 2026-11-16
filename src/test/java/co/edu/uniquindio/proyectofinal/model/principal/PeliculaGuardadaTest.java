
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoListaPelicula;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PeliculaGuardadaTest {

    private Cliente crearCliente() {
        return new Cliente(
                "C001",
                "Juan Pérez",
                "juan@gmail.com",
                "hash123"
        );
    }

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

    private PeliculaGuardada crearPeliculaGuardada() {
        return new PeliculaGuardada(
                "PG001",
                crearCliente(),
                crearPelicula(),
                TipoListaPelicula.FAVORITA
        );
    }

    @Test
    void crearPeliculaGuardadaCorrectamente() {
        Cliente cliente = crearCliente();
        Pelicula pelicula = crearPelicula();

        PeliculaGuardada guardada = new PeliculaGuardada(
                "PG001",
                cliente,
                pelicula,
                TipoListaPelicula.FAVORITA
        );

        assertEquals("PG001", guardada.getCodigo());
        assertSame(cliente, guardada.getCliente());
        assertSame(pelicula, guardada.getPelicula());
        assertEquals(TipoListaPelicula.FAVORITA, guardada.getTipoLista());
        assertNotNull(guardada.getFechaAgregada());
    }

    @Test
    void eliminarEspaciosDelCodigo() {
        PeliculaGuardada guardada = new PeliculaGuardada(
                " PG001 ",
                crearCliente(),
                crearPelicula(),
                TipoListaPelicula.FAVORITA
        );

        assertEquals("PG001", guardada.getCodigo());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new PeliculaGuardada(
                        null,
                        crearCliente(),
                        crearPelicula(),
                        TipoListaPelicula.FAVORITA
                )
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new PeliculaGuardada(
                        " ",
                        crearCliente(),
                        crearPelicula(),
                        TipoListaPelicula.FAVORITA
                )
        );
    }

    @Test
    void rechazarClienteNulo() {
        assertThrows(NullPointerException.class, () ->
                new PeliculaGuardada(
                        "PG001",
                        null,
                        crearPelicula(),
                        TipoListaPelicula.FAVORITA
                )
        );
    }

    @Test
    void rechazarPeliculaNula() {
        assertThrows(NullPointerException.class, () ->
                new PeliculaGuardada(
                        "PG001",
                        crearCliente(),
                        null,
                        TipoListaPelicula.FAVORITA
                )
        );
    }

    @Test
    void rechazarTipoListaNulo() {
        assertThrows(NullPointerException.class, () ->
                new PeliculaGuardada(
                        "PG001",
                        crearCliente(),
                        crearPelicula(),
                        null
                )
        );
    }

    @Test
    void cambiarTipoListaAFavorita() {
        PeliculaGuardada guardada = crearPeliculaGuardada();

        guardada.cambiarTipoLista(TipoListaPelicula.POR_VER);

        assertEquals(TipoListaPelicula.POR_VER, guardada.getTipoLista());
    }

    @Test
    void cambiarTipoListaAPorVer() {
        PeliculaGuardada guardada = crearPeliculaGuardada();

        guardada.cambiarTipoLista(TipoListaPelicula.POR_VER);

        guardada.cambiarTipoLista(TipoListaPelicula.FAVORITA);

        assertEquals(TipoListaPelicula.FAVORITA, guardada.getTipoLista());
    }

    @Test
    void rechazarCambioATipoListaNulo() {
        PeliculaGuardada guardada = crearPeliculaGuardada();

        assertThrows(NullPointerException.class, () ->
                guardada.cambiarTipoLista(null)
        );

        assertEquals(TipoListaPelicula.FAVORITA, guardada.getTipoLista());
    }

    @Test
    void fechaAgregadaNoCambiaAlCambiarLista() {
        PeliculaGuardada guardada = crearPeliculaGuardada();
        LocalDateTime fechaOriginal = guardada.getFechaAgregada();

        guardada.cambiarTipoLista(TipoListaPelicula.POR_VER);

        assertEquals(fechaOriginal, guardada.getFechaAgregada());
    }
}
