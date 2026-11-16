
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoUsuario;
import co.edu.uniquindio.proyectofinal.model.util.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new UsuarioPrueba(
                "U001",
                "Juan Pérez",
                "juan@correo.com",
                "hash123"
        );
    }

    // Subclase para poder probar la clase abstracta Usuario.
    private static class UsuarioPrueba extends Usuario {
        UsuarioPrueba(
                String id,
                String nombre,
                String correo,
                String contrasenaHash
        ) {
            super(id, nombre, correo, contrasenaHash);
        }
    }

    @Test
    void constructorDebeCrearUsuarioCorrectamente() {
        assertEquals("U001", usuario.getId());
        assertEquals("Juan Pérez", usuario.getNombre());
        assertEquals("juan@correo.com", usuario.getCorreo());
        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
    }

    @Test
    void constructorDebeEliminarEspaciosDeLosTextos() {
        Usuario resultado = new UsuarioPrueba(
                " U002 ",
                " Ana López ",
                " ana@correo.com ",
                " hash456 "
        );

        assertEquals("U002", resultado.getId());
        assertEquals("Ana López", resultado.getNombre());
        assertEquals("ana@correo.com", resultado.getCorreo());
    }

    @Test
    void constructorDebeRechazarIdNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba(null, "Juan", "juan@correo.com", "hash"));
    }

    @Test
    void constructorDebeRechazarIdVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba(" ", "Juan", "juan@correo.com", "hash"));
    }

    @Test
    void constructorDebeRechazarNombreNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba("U001", null, "juan@correo.com", "hash"));
    }

    @Test
    void constructorDebeRechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba("U001", " ", "juan@correo.com", "hash"));
    }

    @Test
    void constructorDebeRechazarCorreoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba("U001", "Juan", null, "hash"));
    }

    @Test
    void constructorDebeRechazarCorreoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba("U001", "Juan", " ", "hash"));
    }

    @Test
    void constructorDebeRechazarCorreoSinArroba() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba(
                        "U001", "Juan", "juan.correo.com", "hash"
                ));
    }

    @Test
    void constructorDebeRechazarCorreoQueEmpiezaConArroba() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba(
                        "U001", "Juan", "@correo.com", "hash"
                ));
    }

    @Test
    void constructorDebeRechazarCorreoQueTerminaConArroba() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba(
                        "U001", "Juan", "juan@", "hash"
                ));
    }

    @Test
    void constructorDebeRechazarHashNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba(
                        "U001", "Juan", "juan@correo.com", null
                ));
    }

    @Test
    void constructorDebeRechazarHashVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new UsuarioPrueba(
                        "U001", "Juan", "juan@correo.com", " "
                ));
    }

    @Test
    void actualizarDatosDebeCambiarNombreYCorreo() {
        usuario.actualizarDatos("Ana López", "ana@correo.com");

        assertEquals("Ana López", usuario.getNombre());
        assertEquals("ana@correo.com", usuario.getCorreo());
    }

    @Test
    void actualizarDatosDebeRechazarNombreNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                usuario.actualizarDatos(null, "ana@correo.com"));
    }

    @Test
    void actualizarDatosDebeRechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                usuario.actualizarDatos(" ", "ana@correo.com"));
    }

    @Test
    void actualizarDatosDebeRechazarCorreoInvalido() {
        assertThrows(IllegalArgumentException.class, () ->
                usuario.actualizarDatos("Ana", "correo-invalido"));
    }



    @Test
    void actualizarContrasenaHashDebeAceptarNuevoHash() {
        usuario.actualizarContrasenaHash("nuevoHash");

        assertTrue(usuario.coincideContrasenaHash("nuevoHash"));
    }

    @Test
    void actualizarContrasenaHashDebeRechazarHashNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                usuario.actualizarContrasenaHash(null));
    }

    @Test
    void actualizarContrasenaHashDebeRechazarHashVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                usuario.actualizarContrasenaHash(" "));
    }

    @Test
    void coincideContrasenaHashDebeRetornarTrueCuandoCoincide() {
        assertTrue(usuario.coincideContrasenaHash("hash123"));
    }

    @Test
    void coincideContrasenaHashDebeRetornarFalseCuandoNoCoincide() {
        assertFalse(usuario.coincideContrasenaHash("otroHash"));
    }

    @Test
    void coincideContrasenaHashDebeRetornarFalseConValorNulo() {
        assertFalse(usuario.coincideContrasenaHash(null));
    }

    @Test
    void verificarContrasenaDebeRetornarTrueConContrasenaCorrecta() {
        PasswordHasher hasher = new PasswordHasher();
        String hash = hasher.hash("claveSegura123");

        usuario.actualizarContrasenaHash(hash);

        assertTrue(usuario.verificarContrasena("claveSegura123", hasher));
    }

    @Test
    void verificarContrasenaDebeRetornarFalseConContrasenaIncorrecta() {
        PasswordHasher hasher = new PasswordHasher();
        String hash = hasher.hash("claveSegura123");

        usuario.actualizarContrasenaHash(hash);

        assertFalse(usuario.verificarContrasena("claveIncorrecta", hasher));
    }

    @Test
    void verificarContrasenaDebeRetornarFalseConHasherNulo() {
        assertFalse(usuario.verificarContrasena("claveSegura123", null));
    }

    @Test
    void verificarContrasenaDebeRetornarFalseConContrasenaNula() {
        PasswordHasher hasher = new PasswordHasher();

        assertFalse(usuario.verificarContrasena(null, hasher));
    }

    @Test
    void activarDebeCambiarEstadoAActivo() {
        usuario.desactivar();

        usuario.activar();

        assertEquals(EstadoUsuario.ACTIVO, usuario.getEstado());
    }

    @Test
    void desactivarDebeCambiarEstadoAInactivo() {
        usuario.desactivar();

        assertEquals(EstadoUsuario.INACTIVO, usuario.getEstado());
    }

    @Test
    void bloquearDebeCambiarEstadoABloqueado() {
        usuario.bloquear();

        assertEquals(EstadoUsuario.BLOQUEADO, usuario.getEstado());
    }

    @Test
    void puedeIngresarDebeRetornarTrueCuandoEstaActivo() {
        assertTrue(usuario.puedeIngresar());
    }

    @Test
    void puedeIngresarDebeRetornarFalseCuandoEstaInactivo() {
        usuario.desactivar();

        assertFalse(usuario.puedeIngresar());
    }

    @Test
    void puedeIngresarDebeRetornarFalseCuandoEstaBloqueado() {
        usuario.bloquear();

        assertFalse(usuario.puedeIngresar());
    }
}
