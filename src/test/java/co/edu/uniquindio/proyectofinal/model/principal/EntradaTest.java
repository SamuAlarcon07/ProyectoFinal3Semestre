
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import co.edu.uniquindio.proyectofinal.model.enums.TipoAsiento;
import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntradaTest {

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

    private Sala crearSala() {
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
        sala.agregarAsiento(
                new Asiento("A", 1, TipoAsiento.GENERAL)
        );

        return sala;
    }

    private Asiento crearAsiento() {
        return new Asiento("A", 1, TipoAsiento.GENERAL);
    }

    private Funcion crearFuncion() {
        return new Funcion(
                "F01",
                crearPelicula(),
                crearSala(),
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
    void crearEntradaCorrectamente() {
        EntradaGeneral entrada = crearEntrada();

        assertEquals("E01", entrada.getCodigo());
        assertEquals("QR001", entrada.getCodigoQR());
        assertNotNull(entrada.getFuncion());
        assertNotNull(entrada.getAsiento());
        assertEquals(LocalDateTime.now().getDayOfYear(),
                entrada.getFechaEmision().getDayOfYear());
        assertEquals(EstadoEntrada.EMITIDA, entrada.getEstado());
        assertEquals(0, new BigDecimal("15000")
                .compareTo(entrada.consultarPrecio()));
    }

    @Test
    void eliminarEspaciosDeCodigoYCodigoQR() {
        EntradaGeneral entrada = new EntradaGeneral(
                " E01 ",
                " QR001 ",
                crearFuncion(),
                crearAsiento(),
                new BigDecimal("15000"),
                LocalDateTime.now()
        );

        assertEquals("E01", entrada.getCodigo());
        assertEquals("QR001", entrada.getCodigoQR());
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
    void rechazarCodigoQRNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new EntradaGeneral(
                        "E01", null, crearFuncion(), crearAsiento(),
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
        Asiento asiento = new Asiento(
                "B", 5, TipoAsiento.GENERAL
        );

        assertThrows(IllegalArgumentException.class, () ->
                new EntradaGeneral(
                        "E01", "QR001", crearFuncion(), asiento,
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
    void rechazarPrecioNegativo() {
        assertThrows(IllegalArgumentException.class, () ->
                new EntradaGeneral(
                        "E01", "QR001", crearFuncion(), crearAsiento(),
                        new BigDecimal("-1000"), LocalDateTime.now()
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
    void marcarEntradaComoUtilizada() {
        EntradaGeneral entrada = crearEntrada();

        entrada.marcarComoUtilizada();

        assertEquals(EstadoEntrada.UTILIZADA, entrada.getEstado());
    }

    @Test
    void rechazarUsoDeEntradaYaUtilizada() {
        EntradaGeneral entrada = crearEntrada();

        entrada.marcarComoUtilizada();

        assertThrows(IllegalStateException.class,
                entrada::marcarComoUtilizada);
    }

    @Test
    void anularEntradaEmitida() {
        EntradaGeneral entrada = crearEntrada();

        entrada.anular();

        assertEquals(EstadoEntrada.ANULADA, entrada.getEstado());
    }

    @Test
    void rechazarAnulacionDeEntradaUtilizada() {
        EntradaGeneral entrada = crearEntrada();

        entrada.marcarComoUtilizada();

        assertThrows(IllegalStateException.class, entrada::anular);
    }
}