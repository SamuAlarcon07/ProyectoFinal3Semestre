
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;
import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import co.edu.uniquindio.proyectofinal.model.enums.TipoAsiento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntradaGeneralTest {

    private Pelicula crearPelicula() {
        return new Pelicula(
                "P01",
                "Interestelar",
                "Una aventura espacial",
                169,
                "12+",
                "Ciencia ficción"
        );
    }

    private Asiento crearAsiento() {
        return new Asiento("A", 1, TipoAsiento.GENERAL);
    }

    private Funcion crearFuncion() {
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
        sala.agregarAsiento(crearAsiento());

        return new Funcion(
                "F01",
                crearPelicula(),
                sala,
                LocalDateTime.now().plusDays(1),
                new BigDecimal("15000"),
                FormatoProyeccion.DOS_D
        );
    }

    private EntradaGeneral crearEntrada() {
        return new EntradaGeneral(
                "E01",
                "QR001",
                crearFuncion(),
                crearAsiento(),
                new BigDecimal("15000"),
                LocalDateTime.now()
        );
    }

    @Test
    void crearEntradaGeneralCorrectamente() {
        EntradaGeneral entrada = crearEntrada();

        assertEquals("E01", entrada.getCodigo());
        assertEquals("QR001", entrada.getCodigoQR());
        assertNotNull(entrada.getFuncion());
        assertNotNull(entrada.getAsiento());
        assertNotNull(entrada.getFechaEmision());
        assertEquals(EstadoEntrada.EMITIDA, entrada.getEstado());
        assertEquals(0, new BigDecimal("15000")
                .compareTo(entrada.consultarPrecio()));
    }

    @Test
    void consultarDescripcionGeneral() {
        EntradaGeneral entrada = crearEntrada();

        assertEquals(
                "Entrada general para Interestelar",
                entrada.consultarDescripcion()
        );
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new EntradaGeneral(
                        null, "QR001", crearFuncion(), crearAsiento(),
                        new BigDecimal("15000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarCodigoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new EntradaGeneral(
                        " ", "QR001", crearFuncion(), crearAsiento(),
                        new BigDecimal("15000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarCodigoQRVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new EntradaGeneral(
                        "E01", " ", crearFuncion(), crearAsiento(),
                        new BigDecimal("15000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarFuncionNula() {
        assertThrows(NullPointerException.class, () ->
                new EntradaGeneral(
                        "E01", "QR001", null, crearAsiento(),
                        new BigDecimal("15000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarAsientoNulo() {
        assertThrows(NullPointerException.class, () ->
                new EntradaGeneral(
                        "E01", "QR001", crearFuncion(), null,
                        new BigDecimal("15000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarAsientoQueNoPerteneceALaSala() {
        Asiento asientoAjeno = new Asiento(
                "B", 5, TipoAsiento.GENERAL
        );

        assertThrows(IllegalArgumentException.class, () ->
                new EntradaGeneral(
                        "E01", "QR001", crearFuncion(), asientoAjeno,
                        new BigDecimal("15000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarPrecioNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new EntradaGeneral(
                        "E01", "QR001", crearFuncion(), crearAsiento(),
                        null, LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarPrecioCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new EntradaGeneral(
                        "E01", "QR001", crearFuncion(), crearAsiento(),
                        BigDecimal.ZERO, LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarFechaEmisionNula() {
        assertThrows(NullPointerException.class, () ->
                new EntradaGeneral(
                        "E01", "QR001", crearFuncion(), crearAsiento(),
                        new BigDecimal("15000"), null
                )
        );
    }

    @Test
    void marcarEntradaComoUtilizadaYAnularNoPermitido() {
        EntradaGeneral entrada = crearEntrada();

        entrada.marcarComoUtilizada();

        assertEquals(EstadoEntrada.UTILIZADA, entrada.getEstado());

        assertThrows(
                IllegalStateException.class,
                entrada::anular
        );
    }
}