
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoSolicitudEspera;
import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ListaEsperaTest {

    private Cliente crearCliente(String id) {
        return new Cliente(
                id,
                "Cliente " + id,
                id + "@gmail.com",
                "hash123"
        );
    }

    private Funcion crearFuncion() {
        Pelicula pelicula = new Pelicula(
                "P01",
                "Interestelar",
                "Una aventura espacial",
                169,
                "12+",
                "Ciencia ficción"
        );

        TipoSala tipoSala = new TipoSala(
                "TS01",
                "Sala estándar",
                "Sala convencional",
                Set.of(FormatoProyeccion.DOS_D),
                false,
                false,
                false
        );

        Sala sala = new Sala("S01", "Sala 1", tipoSala);

        return new Funcion(
                "F01",
                pelicula,
                sala,
                LocalDateTime.now().plusDays(1),
                new BigDecimal("15000"),
                FormatoProyeccion.DOS_D
        );
    }

    private ListaEspera crearLista() {
        return new ListaEspera(
                "LE01",
                crearFuncion(),
                LocalDateTime.now()
        );
    }

    private SolicitudEspera crearSolicitud(
            String codigo,
            Cliente cliente,
            LocalDateTime fecha
    ) {
        return new SolicitudEspera(codigo, cliente, fecha, 2);
    }

    @Test
    void crearListaEsperaCorrectamente() {
        Funcion funcion = crearFuncion();
        LocalDateTime fecha = LocalDateTime.now();

        ListaEspera lista = new ListaEspera("LE01", funcion, fecha);

        assertEquals("LE01", lista.getCodigo());
        assertSame(funcion, lista.getFuncion());
        assertEquals(fecha, lista.getFechaCreacion());
        assertTrue(lista.consultarSolicitudes().isEmpty());
    }

    @Test
    void eliminarEspaciosDelCodigo() {
        ListaEspera lista = new ListaEspera(
                " LE01 ",
                crearFuncion(),
                LocalDateTime.now()
        );

        assertEquals("LE01", lista.getCodigo());
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new ListaEspera(null, crearFuncion(), LocalDateTime.now())
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new ListaEspera(" ", crearFuncion(), LocalDateTime.now())
        );
    }

    @Test
    void rechazarFuncionNula() {
        assertThrows(NullPointerException.class, () ->
                new ListaEspera("LE01", null, LocalDateTime.now())
        );
    }

    @Test
    void rechazarFechaCreacionNula() {
        assertThrows(NullPointerException.class, () ->
                new ListaEspera("LE01", crearFuncion(), null)
        );
    }

    @Test
    void agregarSolicitudCorrectamente() {
        ListaEspera lista = crearLista();
        SolicitudEspera solicitud = crearSolicitud(
                "SE01",
                crearCliente("C01"),
                LocalDateTime.now()
        );

        lista.agregarSolicitud(solicitud);

        assertEquals(1, lista.consultarSolicitudes().size());
        assertSame(solicitud, lista.consultarSolicitudes().get(0));
    }

    @Test
    void rechazarSolicitudNula() {
        ListaEspera lista = crearLista();

        assertThrows(NullPointerException.class, () ->
                lista.agregarSolicitud(null)
        );
    }

    @Test
    void rechazarSolicitudActivaDuplicadaDelMismoCliente() {
        ListaEspera lista = crearLista();
        Cliente cliente = crearCliente("C01");

        lista.agregarSolicitud(
                crearSolicitud("SE01", cliente, LocalDateTime.now())
        );

        assertThrows(IllegalArgumentException.class, () ->
                lista.agregarSolicitud(
                        crearSolicitud(
                                "SE02",
                                cliente,
                                LocalDateTime.now().plusMinutes(1)
                        )
                )
        );
    }

    @Test
    void rechazarSolicitudDuplicadaCuandoLaAnteriorFueNotificada() {
        ListaEspera lista = crearLista();
        Cliente cliente = crearCliente("C01");

        SolicitudEspera primera = crearSolicitud(
                "SE01", cliente, LocalDateTime.now()
        );

        lista.agregarSolicitud(primera);
        primera.notificarDisponibilidad(LocalDateTime.now().plusHours(1));

        assertThrows(IllegalArgumentException.class, () ->
                lista.agregarSolicitud(
                        crearSolicitud(
                                "SE02",
                                cliente,
                                LocalDateTime.now().plusMinutes(1)
                        )
                )
        );
    }

    @Test
    void permitirNuevaSolicitudCuandoLaAnteriorFueCancelada() {
        ListaEspera lista = crearLista();
        Cliente cliente = crearCliente("C01");

        SolicitudEspera primera = crearSolicitud(
                "SE01", cliente, LocalDateTime.now()
        );

        lista.agregarSolicitud(primera);
        primera.cancelar();

        SolicitudEspera segunda = crearSolicitud(
                "SE02", cliente, LocalDateTime.now().plusMinutes(1)
        );

        assertDoesNotThrow(() -> lista.agregarSolicitud(segunda));
        assertEquals(2, lista.consultarSolicitudes().size());
    }

    @Test
    void obtenerSiguienteSolicitudActivaMasAntigua() {
        ListaEspera lista = crearLista();

        SolicitudEspera segunda = crearSolicitud(
                "SE02",
                crearCliente("C02"),
                LocalDateTime.now().plusMinutes(5)
        );

        SolicitudEspera primera = crearSolicitud(
                "SE01",
                crearCliente("C01"),
                LocalDateTime.now()
        );

        lista.agregarSolicitud(segunda);
        lista.agregarSolicitud(primera);

        assertSame(primera, lista.obtenerSiguienteSolicitud().orElseThrow());
    }

    @Test
    void noObtenerSolicitudesNotificadasComoSiguientes() {
        ListaEspera lista = crearLista();

        SolicitudEspera solicitud = crearSolicitud(
                "SE01",
                crearCliente("C01"),
                LocalDateTime.now()
        );

        lista.agregarSolicitud(solicitud);
        solicitud.notificarDisponibilidad(LocalDateTime.now().plusHours(1));

        assertTrue(lista.obtenerSiguienteSolicitud().isEmpty());
    }

    @Test
    void devolverListaInmodificableDeSolicitudes() {
        ListaEspera lista = crearLista();

        lista.agregarSolicitud(
                crearSolicitud(
                        "SE01",
                        crearCliente("C01"),
                        LocalDateTime.now()
                )
        );

        assertThrows(UnsupportedOperationException.class, () ->
                lista.consultarSolicitudes().clear()
        );
    }
}
