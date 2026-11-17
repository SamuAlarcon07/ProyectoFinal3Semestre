package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import co.edu.uniquindio.proyectofinal.model.enums.ResultadoAcceso;
import co.edu.uniquindio.proyectofinal.model.enums.TipoAsiento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegistroAccesoTest {

    private RegistroAcceso registro;
    private Entrada entrada;
    private LocalDateTime fechaHora;

    @BeforeEach
    void setUp() {
        TipoSala tipoSala = new TipoSala(
                "TS01",
                "Sala estándar",
                "Sala de proyección estándar",
                Set.of(FormatoProyeccion.DOS_D),
                false,
                false,
                false
        );

        Sala sala = new Sala("S01", "Sala 1", tipoSala);

        Asiento asiento = new Asiento("A", 1, TipoAsiento.GENERAL);
        sala.agregarAsiento(asiento);

        Pelicula pelicula = new Pelicula(
                "P01",
                "Interestelar",
                "Película de ciencia ficción",
                169,
                "PG-13",
                "Ciencia ficción"
        );

        Funcion funcion = new Funcion(
                "F01",
                pelicula,
                sala,
                LocalDateTime.now().plusDays(1),
                new BigDecimal("15000"),
                FormatoProyeccion.DOS_D
        );

        entrada = new EntradaGeneral(
                "E01",
                "QR001",
                funcion,
                asiento,
                new BigDecimal("15000"),
                LocalDateTime.now()
        );

        fechaHora = LocalDateTime.now();

        registro = new RegistroAcceso(
                "RA01",
                entrada,
                fechaHora,
                ResultadoAcceso.ACEPTADO,
                "Acceso autorizado"
        );
    }

    @Test
    void constructorDebeCrearRegistroCorrectamente() {
        assertEquals("RA01", registro.getCodigo());
        assertEquals(entrada, registro.getEntrada());
        assertEquals(fechaHora, registro.getFechaHora());
        assertEquals(ResultadoAcceso.ACEPTADO, registro.getResultado());
        assertEquals("Acceso autorizado", registro.getObservacion());
    }

    @Test
    void constructorDebeEliminarEspaciosDelCodigo() {
        RegistroAcceso resultado = new RegistroAcceso(
                " RA02 ",
                entrada,
                fechaHora,
                ResultadoAcceso.ACEPTADO,
                "Observación"
        );

        assertEquals("RA02", resultado.getCodigo());
    }

    @Test
    void constructorDebeRechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new RegistroAcceso(
                        null,
                        entrada,
                        fechaHora,
                        ResultadoAcceso.ACEPTADO,
                        "Observación"
                ));
    }

    @Test
    void constructorDebeRechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new RegistroAcceso(
                        " ",
                        entrada,
                        fechaHora,
                        ResultadoAcceso.ACEPTADO,
                        "Observación"
                ));
    }

    @Test
    void constructorDebeRechazarEntradaNula() {
        assertThrows(NullPointerException.class, () ->
                new RegistroAcceso(
                        "RA02",
                        null,
                        fechaHora,
                        ResultadoAcceso.ACEPTADO,
                        "Observación"
                ));
    }

    @Test
    void constructorDebeRechazarFechaHoraNula() {
        assertThrows(NullPointerException.class, () ->
                new RegistroAcceso(
                        "RA02",
                        entrada,
                        null,
                        ResultadoAcceso.ACEPTADO,
                        "Observación"
                ));
    }

    @Test
    void constructorDebeRechazarResultadoNulo() {
        assertThrows(NullPointerException.class, () ->
                new RegistroAcceso(
                        "RA02",
                        entrada,
                        fechaHora,
                        null,
                        "Observación"
                ));
    }

    @Test
    void constructorDebeAceptarObservacionNula() {
        RegistroAcceso resultado = new RegistroAcceso(
                "RA02",
                entrada,
                fechaHora,
                ResultadoAcceso.ACEPTADO,
                null
        );

        assertEquals("", resultado.getObservacion());
    }

    @Test
    void constructorDebeEliminarEspaciosDeLaObservacion() {
        RegistroAcceso resultado = new RegistroAcceso(
                "RA02",
                entrada,
                fechaHora,
                ResultadoAcceso.RECHAZADO,
                "  Entrada inválida  "
        );

        assertEquals("Entrada inválida", resultado.getObservacion());
    }

    @Test
    void constructorDebeAceptarObservacionVacia() {
        RegistroAcceso resultado = new RegistroAcceso(
                "RA02",
                entrada,
                fechaHora,
                ResultadoAcceso.RECHAZADO,
                ""
        );

        assertEquals("", resultado.getObservacion());
    }

    @Test
    void debeRegistrarResultadoAceptado() {
        RegistroAcceso resultado = new RegistroAcceso(
                "RA03",
                entrada,
                fechaHora,
                ResultadoAcceso.ACEPTADO,
                "Validación correcta"
        );

        assertEquals(ResultadoAcceso.ACEPTADO, resultado.getResultado());
    }

    @Test
    void debeRegistrarResultadoRechazado() {
        RegistroAcceso resultado = new RegistroAcceso(
                "RA04",
                entrada,
                fechaHora,
                ResultadoAcceso.RECHAZADO,
                "Entrada no válida"
        );

        assertEquals(ResultadoAcceso.RECHAZADO, resultado.getResultado());
    }

    @Test
    void debeConservarLaFechaHoraExacta() {
        LocalDateTime fechaEspecifica =
                LocalDateTime.of(2026, 5, 20, 18, 30);

        RegistroAcceso resultado = new RegistroAcceso(
                "RA05",
                entrada,
                fechaEspecifica,
                ResultadoAcceso.ACEPTADO,
                "Correcto"
        );

        assertEquals(fechaEspecifica, resultado.getFechaHora());
    }

    @Test
    void gettersDebenDevolverLosMismosObjetosRecibidos() {
        assertSame(entrada, registro.getEntrada());
        assertSame(fechaHora, registro.getFechaHora());
    }
}