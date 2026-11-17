package co.edu.uniquindio.proyectofinal.model.principal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdministradorTest {

    // Prueba 1: Crear un administrador correctamente
    @Test
    void crearAdministradorCorrectamente() {

        Administrador administrador = new Administrador(
                "123456",
                "Juan Perez",
                "juan@gmail.com",
                "hash123",
                "ADM001"
        );

        assertNotNull(administrador);
        assertEquals("123456", administrador.getId());
        assertEquals("Juan Perez", administrador.getNombre());
        assertEquals("juan@gmail.com", administrador.getCorreo());
        assertEquals("ADM001", administrador.getCodigoEmpleado());
    }

    // Prueba 2: El código de empleado no puede ser nulo
    @Test
    void crearAdministradorConCodigoNulo() {

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new Administrador(
                        "123456",
                        "Juan Perez",
                        "juan@gmail.com",
                        "hash123",
                        null
                )
        );

        assertEquals(
                "El código de empleado es obligatorio.",
                excepcion.getMessage()
        );
    }

    // Prueba 3: El código de empleado no puede estar vacío
    @Test
    void crearAdministradorConCodigoVacio() {

        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new Administrador(
                        "123456",
                        "Juan Perez",
                        "juan@gmail.com",
                        "hash123",
                        ""
                )
        );

        assertEquals(
                "El código de empleado es obligatorio.",
                excepcion.getMessage()
        );
    }

    // Prueba 4: El código no puede contener solamente espacios
    @Test
    void crearAdministradorConCodigoSoloEspacios() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new Administrador(
                        "123456",
                        "Juan Perez",
                        "juan@gmail.com",
                        "hash123",
                        "   "
                )
        );
    }

    // Prueba 5: El código debe eliminar los espacios exteriores
    @Test
    void crearAdministradorConCodigoConEspacios() {

        Administrador administrador = new Administrador(
                "123456",
                "Juan Perez",
                "juan@gmail.com",
                "hash123",
                "  ADM001  "
        );

        assertEquals("ADM001", administrador.getCodigoEmpleado());
    }

    // Prueba 6: Verificar los datos heredados de Usuario
    @Test
    void verificarDatosHeredadosDeUsuario() {

        Administrador administrador = new Administrador(
                "123456",
                "Juan Perez",
                "juan@gmail.com",
                "hash123",
                "ADM001"
        );

        assertEquals("123456", administrador.getId());
        assertEquals("Juan Perez", administrador.getNombre());
        assertEquals("juan@gmail.com", administrador.getCorreo());
        assertTrue(administrador.puedeIngresar());
    }
}