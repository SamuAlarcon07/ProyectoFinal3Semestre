package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TipoSalaTest {

    private TipoSala tipoSala;

    @BeforeEach
    void setUp() {
        tipoSala = new TipoSala(
                "TS01",
                "Sala 3D",
                "Sala con proyección tridimensional",
                EnumSet.of(
                        FormatoProyeccion.DOS_D,
                        FormatoProyeccion.TRES_D
                ),
                true,
                false,
                true
        );
    }

    @Test
    void constructorDebeCrearTipoSalaCorrectamente() {
        assertEquals("TS01", tipoSala.getCodigo());
        assertEquals("Sala 3D", tipoSala.getNombre());
        assertEquals(
                "Sala con proyección tridimensional",
                tipoSala.getDescripcion()
        );
        assertTrue(tipoSala.isActivo());
    }

    @Test
    void constructorDebeGuardarLasCaracteristicas() {
        assertTrue(tipoSala.isRequiereGafas3D());
        assertFalse(tipoSala.isTieneMovimiento());
        assertTrue(tipoSala.isTieneEfectos());
    }

    @Test
    void constructorDebeEliminarEspaciosDeLosTextos() {
        TipoSala resultado = new TipoSala(
                " TS02 ",
                " Sala especial ",
                " Descripción especial ",
                EnumSet.of(FormatoProyeccion.DOS_D),
                false,
                false,
                false
        );

        assertEquals("TS02", resultado.getCodigo());
        assertEquals("Sala especial", resultado.getNombre());
        assertEquals("Descripción especial", resultado.getDescripcion());
    }

    @Test
    void constructorDebeRechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new TipoSala(
                        null,
                        "Sala",
                        "Descripción",
                        EnumSet.of(FormatoProyeccion.DOS_D),
                        false, false, false
                ));
    }

    @Test
    void constructorDebeRechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new TipoSala(
                        " ",
                        "Sala",
                        "Descripción",
                        EnumSet.of(FormatoProyeccion.DOS_D),
                        false, false, false
                ));
    }

    @Test
    void constructorDebeRechazarNombreNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new TipoSala(
                        "TS01",
                        null,
                        "Descripción",
                        EnumSet.of(FormatoProyeccion.DOS_D),
                        false, false, false
                ));
    }

    @Test
    void constructorDebeRechazarNombreVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new TipoSala(
                        "TS01",
                        " ",
                        "Descripción",
                        EnumSet.of(FormatoProyeccion.DOS_D),
                        false, false, false
                ));
    }

    @Test
    void constructorDebeRechazarDescripcionNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new TipoSala(
                        "TS01",
                        "Sala",
                        null,
                        EnumSet.of(FormatoProyeccion.DOS_D),
                        false, false, false
                ));
    }

    @Test
    void constructorDebeRechazarDescripcionVacia() {
        assertThrows(IllegalArgumentException.class, () ->
                new TipoSala(
                        "TS01",
                        "Sala",
                        " ",
                        EnumSet.of(FormatoProyeccion.DOS_D),
                        false, false, false
                ));
    }

    @Test
    void constructorDebeRechazarFormatosNulos() {
        assertThrows(NullPointerException.class, () ->
                new TipoSala(
                        "TS01",
                        "Sala",
                        "Descripción",
                        null,
                        false, false, false
                ));
    }

    @Test
    void constructorDebeRechazarConjuntoDeFormatosVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new TipoSala(
                        "TS01",
                        "Sala",
                        "Descripción",
                        EnumSet.noneOf(FormatoProyeccion.class),
                        false, false, false
                ));
    }

    @Test
    void constructorDebeIniciarActivo() {
        assertTrue(tipoSala.isActivo());
    }

    @Test
    void permiteFormatoDebeRetornarTrueParaFormatoCompatible() {
        assertTrue(tipoSala.permiteFormato(FormatoProyeccion.DOS_D));
        assertTrue(tipoSala.permiteFormato(FormatoProyeccion.TRES_D));
    }

    @Test
    void permiteFormatoDebeRetornarFalseParaFormatoNoCompatible() {
        assertFalse(tipoSala.permiteFormato(FormatoProyeccion.CUATRO_D));
    }

    @Test
    void permiteFormatoDebeRetornarFalseParaFormatoNulo() {
        assertFalse(tipoSala.permiteFormato(null));
    }

    @Test
    void permiteFormatoDebeRetornarFalseCuandoEstaInactivo() {
        tipoSala.desactivar();

        assertFalse(tipoSala.permiteFormato(FormatoProyeccion.DOS_D));
    }

    @Test
    void actualizarFormatosCompatiblesDebeCambiarLosFormatos() {
        tipoSala.actualizarFormatosCompatibles(
                EnumSet.of(FormatoProyeccion.CUATRO_D)
        );

        assertEquals(
                EnumSet.of(FormatoProyeccion.CUATRO_D),
                tipoSala.getFormatosCompatibles()
        );
    }

    @Test
    void actualizarFormatosCompatiblesDebeAceptarVariosFormatos() {
        Set<FormatoProyeccion> formatos = EnumSet.of(
                FormatoProyeccion.DOS_D,
                FormatoProyeccion.TRES_D,
                FormatoProyeccion.CUATRO_D
        );

        tipoSala.actualizarFormatosCompatibles(formatos);

        assertEquals(formatos, tipoSala.getFormatosCompatibles());
    }

    @Test
    void actualizarFormatosCompatiblesDebeRechazarConjuntoNulo() {
        assertThrows(NullPointerException.class, () ->
                tipoSala.actualizarFormatosCompatibles(null));
    }

    @Test
    void actualizarFormatosCompatiblesDebeRechazarConjuntoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                tipoSala.actualizarFormatosCompatibles(
                        EnumSet.noneOf(FormatoProyeccion.class)
                ));
    }

    @Test
    void actualizarFormatosCompatiblesDebeConservarLosFormatosAnterioresSiFalla() {
        Set<FormatoProyeccion> anteriores =
                tipoSala.getFormatosCompatibles();

        assertThrows(IllegalArgumentException.class, () ->
                tipoSala.actualizarFormatosCompatibles(
                        EnumSet.noneOf(FormatoProyeccion.class)
                ));

        assertEquals(anteriores, tipoSala.getFormatosCompatibles());
    }

    @Test
    void activarDebeCambiarEstadoAActivo() {
        tipoSala.desactivar();

        tipoSala.activar();

        assertTrue(tipoSala.isActivo());
    }

    @Test
    void desactivarDebeCambiarEstadoAInactivo() {
        tipoSala.desactivar();

        assertFalse(tipoSala.isActivo());
    }

    @Test
    void activarDosVecesDebeMantenerEstadoActivo() {
        tipoSala.activar();
        tipoSala.activar();

        assertTrue(tipoSala.isActivo());
    }

    @Test
    void desactivarDosVecesDebeMantenerEstadoInactivo() {
        tipoSala.desactivar();
        tipoSala.desactivar();

        assertFalse(tipoSala.isActivo());
    }

    @Test
    void getFormatosCompatiblesDebeRetornarConjuntoInmodificable() {
        Set<FormatoProyeccion> formatos =
                tipoSala.getFormatosCompatibles();

        assertThrows(UnsupportedOperationException.class, () ->
                formatos.add(FormatoProyeccion.CUATRO_D));
    }

    @Test
    void modificarConjuntoOriginalNoDebeCambiarLosFormatosInternos() {
        Set<FormatoProyeccion> formatos =
                EnumSet.of(FormatoProyeccion.DOS_D);

        TipoSala resultado = new TipoSala(
                "TS03",
                "Sala estándar",
                "Descripción",
                formatos,
                false,
                false,
                false
        );

        formatos.add(FormatoProyeccion.TRES_D);

        assertEquals(
                EnumSet.of(FormatoProyeccion.DOS_D),
                resultado.getFormatosCompatibles()
        );
    }
}