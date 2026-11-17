
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoAsiento;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AsientoTest {

    // 1. Prueba de creación correcta de un asiento
    @Test
    void testCrearAsientoCorrectamente() {
        Asiento asiento = new Asiento("A", 5, TipoAsiento.GENERAL);

        assertEquals("A", asiento.getFila());
        assertEquals(5, asiento.getNumero());
        assertEquals(TipoAsiento.GENERAL, asiento.getTipo());
        assertTrue(asiento.isActivo());
    }

    // 2. Prueba de normalización de la fila
    @Test
    void testNormalizarFila() {
        Asiento asiento = new Asiento("  a  ", 3, TipoAsiento.GENERAL);

        assertEquals("A", asiento.getFila());
    }

    // 3. Prueba de fila nula
    @Test
    void testFilaNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Asiento(null, 1, TipoAsiento.GENERAL)
        );
    }

    // 4. Prueba de fila vacía
    @Test
    void testFilaVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                new Asiento("", 1, TipoAsiento.GENERAL)
        );
    }

    // 5. Prueba de fila con espacios
    @Test
    void testFilaConEspacios() {
        assertThrows(IllegalArgumentException.class, () ->
                new Asiento("   ", 1, TipoAsiento.GENERAL)
        );
    }

    // 6. Prueba de número igual a cero
    @Test
    void testNumeroCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new Asiento("A", 0, TipoAsiento.GENERAL)
        );
    }

    // 7. Prueba de número negativo
    @Test
    void testNumeroNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Asiento("A", -5, TipoAsiento.GENERAL)
        );
    }

    // 8. Prueba de tipo nulo
    @Test
    void testTipoNulo() {
        assertThrows(NullPointerException.class, () ->
                new Asiento("A", 1, null)
        );
    }

    // 9. Prueba de generación del identificador
    @Test
    void testObtenerIdentificador() {
        Asiento asiento = new Asiento("B", 12, TipoAsiento.GENERAL);

        assertEquals("B12", asiento.obtenerIdentificador());
    }

    // 10. Prueba de asiento preferencial
    @Test
    void testEsPreferencial() {
        Asiento asiento = new Asiento("C", 4, TipoAsiento.PREFERENCIAL);

        assertTrue(asiento.esPreferencial());
    }

    // 11. Prueba de asiento no preferencial
    @Test
    void testNoEsPreferencial() {
        Asiento asiento = new Asiento("A", 2, TipoAsiento.GENERAL);

        assertFalse(asiento.esPreferencial());
    }

    // 12. Prueba de asiento accesible
    @Test
    void testAsientoAccesibleNoEsPreferencial() {
        Asiento asiento = new Asiento("D", 1, TipoAsiento.ACCESIBLE);

        assertFalse(asiento.esPreferencial());
    }

    // 13. Prueba de activación
    @Test
    void testActivarAsiento() {
        Asiento asiento = new Asiento("A", 1, TipoAsiento.GENERAL);

        asiento.desactivar();
        asiento.activar();

        assertTrue(asiento.isActivo());
    }

    // 14. Prueba de desactivación
    @Test
    void testDesactivarAsiento() {
        Asiento asiento = new Asiento("A", 1, TipoAsiento.GENERAL);

        asiento.desactivar();

        assertFalse(asiento.isActivo());
    }

    // 15. Prueba de activación de un asiento ya activo
    @Test
    void testActivarAsientoYaActivo() {
        Asiento asiento = new Asiento("A", 1, TipoAsiento.GENERAL);

        asiento.activar();

        assertTrue(asiento.isActivo());
    }

    // 16. Prueba del getter de fila
    @Test
    void testGetFila() {
        Asiento asiento = new Asiento("E", 8, TipoAsiento.GENERAL);

        assertEquals("E", asiento.getFila());
    }

    // 17. Prueba del getter de número
    @Test
    void testGetNumero() {
        Asiento asiento = new Asiento("E", 8, TipoAsiento.GENERAL);

        assertEquals(8, asiento.getNumero());
    }

    // 18. Prueba del getter de tipo
    @Test
    void testGetTipo() {
        Asiento asiento = new Asiento("E", 8, TipoAsiento.ACCESIBLE);

        assertEquals(TipoAsiento.ACCESIBLE, asiento.getTipo());
    }

    // 19. Prueba del getter de estado
    @Test
    void testIsActivo() {
        Asiento asiento = new Asiento("A", 1, TipoAsiento.GENERAL);

        assertTrue(asiento.isActivo());

        asiento.desactivar();

        assertFalse(asiento.isActivo());
    }
}