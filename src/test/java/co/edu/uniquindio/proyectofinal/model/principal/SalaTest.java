
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import co.edu.uniquindio.proyectofinal.model.enums.TipoAsiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SalaTest {

    private Sala sala;
    private TipoSala tipoSala;

    @BeforeEach
    void setUp() {
        tipoSala = new TipoSala(
                "TS01",
                "Sala estándar",
                "Sala de proyección estándar",
                Set.of(FormatoProyeccion.DOS_D),
                false,
                false,
                false
        );

        sala = new Sala("S01", "Sala principal", tipoSala);
    }

    @Test
    void constructorDebeCrearSalaCorrectamente() {
        assertEquals("S01", sala.getCodigo());
        assertEquals("Sala principal", sala.getNombre());
        assertSame(tipoSala, sala.getTipoSala());
        assertNotNull(sala.getAsientos());
        assertTrue(sala.getAsientos().isEmpty());
    }

    @Test
    void constructorDebeEliminarEspaciosDeLosTextos() {
        Sala resultado = new Sala(" S02 ", " Sala auxiliar ", tipoSala);

        assertEquals("S02", resultado.getCodigo());
        assertEquals("Sala auxiliar", resultado.getNombre());
    }

    @Test
    void constructorDebeRechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Sala(null, "Sala principal", tipoSala));
    }

    @Test
    void constructorDebeRechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Sala(" ", "Sala principal", tipoSala));
    }

    @Test
    void constructorDebeRechazarNombreNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Sala("S01", null, tipoSala));
    }

    @Test
    void constructorDebeRechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Sala("S01", " ", tipoSala));
    }

    @Test
    void constructorDebeRechazarTipoSalaNulo() {
        assertThrows(NullPointerException.class, () ->
                new Sala("S01", "Sala principal", null));
    }

    @Test
    void constructorDebeRechazarTipoSalaInactivo() {
        TipoSala inactivo = new TipoSala(
                "TS02",
                "Sala inactiva",
                "Tipo de sala desactivado",
                Set.of(FormatoProyeccion.DOS_D),
                false,
                false,
                false
        );
        inactivo.desactivar();

        assertThrows(IllegalArgumentException.class, () ->
                new Sala("S01", "Sala principal", inactivo));
    }

    @Test
    void agregarAsientoDebeRegistrarAsiento() {
        Asiento asiento = new Asiento("A", 1, TipoAsiento.GENERAL);

        sala.agregarAsiento(asiento);

        assertEquals(1, sala.getAsientos().size());
        assertSame(asiento, sala.getAsientos().get(0));
    }

    @Test
    void agregarAsientoDebeRechazarAsientoNulo() {
        assertThrows(NullPointerException.class, () ->
                sala.agregarAsiento(null));
    }

    @Test
    void agregarAsientoDebeRechazarAsientoDuplicado() {
        Asiento primero = new Asiento("A", 1, TipoAsiento.GENERAL);
        Asiento duplicado = new Asiento("A", 1, TipoAsiento.PREFERENCIAL);

        sala.agregarAsiento(primero);

        assertThrows(IllegalArgumentException.class, () ->
                sala.agregarAsiento(duplicado));
    }

    @Test
    void contieneAsientoDebeRetornarTrueCuandoExiste() {
        Asiento asiento = new Asiento("A", 1, TipoAsiento.GENERAL);
        sala.agregarAsiento(asiento);

        assertTrue(sala.contieneAsiento(asiento));
    }

    @Test
    void contieneAsientoDebeCompararFilaSinDistinguirMayusculas() {
        Asiento registrado = new Asiento("A", 1, TipoAsiento.GENERAL);
        Asiento consultado = new Asiento("a", 1, TipoAsiento.GENERAL);

        sala.agregarAsiento(registrado);

        assertTrue(sala.contieneAsiento(consultado));
    }

    @Test
    void contieneAsientoDebeRetornarFalseCuandoNoExiste() {
        Asiento asiento = new Asiento("A", 1, TipoAsiento.GENERAL);

        assertFalse(sala.contieneAsiento(asiento));
    }

    @Test
    void contieneAsientoDebeRetornarFalseConAsientoNulo() {
        assertFalse(sala.contieneAsiento(null));
    }

    @Test
    void calcularCapacidadDebeRetornarCeroSinAsientos() {
        assertEquals(0, sala.calcularCapacidad());
    }

    @Test
    void calcularCapacidadDebeContarAsientosActivos() {
        sala.agregarAsiento(new Asiento("A", 1, TipoAsiento.GENERAL));
        sala.agregarAsiento(new Asiento("A", 2, TipoAsiento.GENERAL));
        sala.agregarAsiento(new Asiento("A", 3, TipoAsiento.GENERAL));

        assertEquals(3, sala.calcularCapacidad());
    }

    @Test
    void calcularCapacidadNoDebeContarAsientosInactivos() {
        Asiento activo = new Asiento("A", 1, TipoAsiento.GENERAL);
        Asiento inactivo = new Asiento("A", 2, TipoAsiento.GENERAL);
        inactivo.desactivar();

        sala.agregarAsiento(activo);
        sala.agregarAsiento(inactivo);

        assertEquals(1, sala.calcularCapacidad());
    }

    @Test
    void permiteFormatoDebeRetornarTrueParaFormatoPermitido() {
        assertTrue(sala.permiteFormato(FormatoProyeccion.DOS_D));
    }

    @Test
    void permiteFormatoDebeRetornarFalseParaFormatoNoPermitido() {
        assertFalse(sala.permiteFormato(FormatoProyeccion.TRES_D));
    }

    @Test
    void cambiarTipoSalaDebeActualizarElTipo() {
        TipoSala nuevoTipo = new TipoSala(
                "TS03",
                "Sala 3D",
                "Sala con proyección tridimensional",
                Set.of(FormatoProyeccion.TRES_D),
                false,
                false,
                false
        );

        sala.cambiarTipoSala(nuevoTipo);

        assertSame(nuevoTipo, sala.getTipoSala());
    }

    @Test
    void cambiarTipoSalaDebeRechazarTipoNulo() {
        assertThrows(NullPointerException.class, () ->
                sala.cambiarTipoSala(null));
    }

    @Test
    void cambiarTipoSalaDebeRechazarTipoInactivo() {
        TipoSala inactivo = new TipoSala(
                "TS04",
                "Sala inactiva",
                "Tipo desactivado",
                Set.of(FormatoProyeccion.DOS_D),
                false,
                false,
                false
        );
        inactivo.desactivar();

        assertThrows(IllegalArgumentException.class, () ->
                sala.cambiarTipoSala(inactivo));
    }

    @Test
    void getAsientosDebeRetornarUnaListaInmodificable() {
        sala.agregarAsiento(
                new Asiento("A", 1, TipoAsiento.GENERAL)
        );

        assertThrows(UnsupportedOperationException.class, () ->
                sala.getAsientos().add(
                        new Asiento("A", 2, TipoAsiento.GENERAL)
                ));
    }
}
