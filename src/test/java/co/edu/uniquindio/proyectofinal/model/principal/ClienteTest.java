
package co.edu.uniquindio.proyectofinal.model.principal;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ClienteTest {

    @Test
    void crearClienteConFechaActual() {
        Cliente cliente = new Cliente(
                "123", "Juan Pérez", "juan@gmail.com", "hash123"
        );

        assertEquals("123", cliente.getId());
        assertEquals("Juan Pérez", cliente.getNombre());
        assertEquals("juan@gmail.com", cliente.getCorreo());
        assertEquals(LocalDate.now(), cliente.getFechaRegistro());
        assertTrue(cliente.puedeIngresar());
    }

    @Test
    void crearClienteConFechaEspecifica() {
        LocalDate fecha = LocalDate.of(2024, 5, 10);

        Cliente cliente = new Cliente(
                "123", "Juan Pérez", "juan@gmail.com", "hash123", fecha
        );

        assertEquals(fecha, cliente.getFechaRegistro());
    }

    @Test
    void rechazarFechaRegistroNula() {
        assertThrows(NullPointerException.class, () ->
                new Cliente(
                        "123", "Juan Pérez", "juan@gmail.com",
                        "hash123", null
                )
        );
    }

    @Test
    void rechazarFechaRegistroFutura() {
        LocalDate fechaFutura = LocalDate.now().plusDays(1);

        assertThrows(IllegalArgumentException.class, () ->
                new Cliente(
                        "123", "Juan Pérez", "juan@gmail.com",
                        "hash123", fechaFutura
                )
        );
    }

    @Test
    void aceptarFechaRegistroActual() {
        LocalDate hoy = LocalDate.now();

        Cliente cliente = new Cliente(
                "123", "Juan", "juan@gmail.com", "hash", hoy
        );

        assertEquals(hoy, cliente.getFechaRegistro());
    }

    @Test
    void aceptarFechaRegistroPasada() {
        LocalDate fecha = LocalDate.now().minusYears(2);

        Cliente cliente = new Cliente(
                "123", "Juan", "juan@gmail.com", "hash", fecha
        );

        assertEquals(fecha, cliente.getFechaRegistro());
    }

    @Test
    void rechazarIdentificacionNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente(null, "Juan", "juan@gmail.com", "hash")
        );
    }

    @Test
    void rechazarIdentificacionVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente(" ", "Juan", "juan@gmail.com", "hash")
        );
    }

    @Test
    void rechazarNombreNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente("123", null, "juan@gmail.com", "hash")
        );
    }

    @Test
    void rechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente("123", " ", "juan@gmail.com", "hash")
        );
    }

    @Test
    void rechazarCorreoInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente("123", "Juan", "correo-invalido", "hash")
        );
    }

    @Test
    void rechazarCorreoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente("123", "Juan", null, "hash")
        );
    }

    @Test
    void rechazarHashContrasenaVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cliente("123", "Juan", "juan@gmail.com", " ")
        );
    }

    @Test
    void actualizarDatosHeredados() {
        Cliente cliente = new Cliente(
                "123", "Juan", "juan@gmail.com", "hash"
        );

        cliente.actualizarDatos("Carlos", "carlos@gmail.com");

        assertEquals("Carlos", cliente.getNombre());
        assertEquals("carlos@gmail.com", cliente.getCorreo());
    }

    @Test
    void desactivarClienteImpideIngreso() {
        Cliente cliente = new Cliente(
                "123", "Juan", "juan@gmail.com", "hash"
        );

        cliente.desactivar();

        assertFalse(cliente.puedeIngresar());
    }
}