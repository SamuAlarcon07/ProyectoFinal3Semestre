
package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;
import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import co.edu.uniquindio.proyectofinal.model.enums.TipoAsiento;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntradaVIPTest {

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
                "Sala VIP",
                "Sala de proyección VIP",
                Set.of(FormatoProyeccion.DOS_D),
                false,
                false,
                false
        );

        Sala sala = new Sala("S01", "Sala VIP 1", tipoSala);
        sala.agregarAsiento(crearAsiento());

        return new Funcion(
                "F01",
                crearPelicula(),
                sala,
                LocalDateTime.now().plusDays(1),
                new BigDecimal("20000"),
                FormatoProyeccion.DOS_D
        );
    }

    private EntradaVIP crearEntrada() {
        return new EntradaVIP(
                "E01",
                "QR001",
                crearFuncion(),
                crearAsiento(),
                new BigDecimal("30000"),
                LocalDateTime.now()
        );
    }

    @Test
    void crearEntradaVIPCorrectamente() {
        EntradaVIP entrada = crearEntrada();

        assertEquals("E01", entrada.getCodigo());
        assertEquals("QR001", entrada.getCodigoQR());
        assertNotNull(entrada.getFuncion());
        assertNotNull(entrada.getAsiento());
        assertEquals(EstadoEntrada.EMITIDA, entrada.getEstado());
        assertEquals(0, new BigDecimal("30000")
                .compareTo(entrada.consultarPrecio()));
    }

    @Test
    void consultarDescripcionVIP() {
        EntradaVIP entrada = crearEntrada();

        assertEquals(
                "Entrada VIP para Interestelar",
                entrada.consultarDescripcion()
        );
    }

    @Test
    void rechazarCodigoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new EntradaVIP(
                        null, "QR001", crearFuncion(), crearAsiento(),
                        new BigDecimal("30000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarCodigoQRVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new EntradaVIP(
                        "E01", " ", crearFuncion(), crearAsiento(),
                        new BigDecimal("30000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarFuncionNula() {
        assertThrows(NullPointerException.class, () ->
                new EntradaVIP(
                        "E01", "QR001", null, crearAsiento(),
                        new BigDecimal("30000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarAsientoNulo() {
        assertThrows(NullPointerException.class, () ->
                new EntradaVIP(
                        "E01", "QR001", crearFuncion(), null,
                        new BigDecimal("30000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarAsientoQueNoPerteneceALaSala() {
        Asiento asientoAjeno = new Asiento(
                "B", 5, TipoAsiento.GENERAL
        );

        assertThrows(IllegalArgumentException.class, () ->
                new EntradaVIP(
                        "E01", "QR001", crearFuncion(), asientoAjeno,
                        new BigDecimal("30000"), LocalDateTime.now()
                )
        );
    }

    @Test
    void rechazarPrecioCero() {
        assertThrows(IllegalArgumentException.class, () ->
                new EntradaVIP(
                        "E01", "QR001", crearFuncion(), crearAsiento(),
                        BigDecimal.ZERO, LocalDateTime.now()
                )
        );
    }

    @Test
    void marcarEntradaVIPComoUtilizada() {
        EntradaVIP entrada = crearEntrada();

        entrada.marcarComoUtilizada();

        assertEquals(EstadoEntrada.UTILIZADA, entrada.getEstado());
    }

    @Test
    void anularEntradaVIPEmitida() {
        EntradaVIP entrada = crearEntrada();

        entrada.anular();

        assertEquals(EstadoEntrada.ANULADA, entrada.getEstado());
    }

    @Test
    void rechazarAnulacionDeEntradaVIPUtilizada() {
        EntradaVIP entrada = crearEntrada();

        entrada.marcarComoUtilizada();

        assertThrows(
                IllegalStateException.class,
                entrada::anular
        );
    }
}
