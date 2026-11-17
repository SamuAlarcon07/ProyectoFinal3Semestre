package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CineTest {

    private Cine crearCine() {
        return new Cine(
                "900123456",
                "CinemaUQ S.A.S.",
                "CinemaUQ",
                "Calle 12 # 15-20",
                "3001234567",
                "contacto@cinemauq.com",
                "www.cinemauq.com"
        );
    }

    private Sala crearSala(String codigo) {
        TipoSala tipoSala = new TipoSala(
                "TS01",
                "Sala estándar",
                "Sala de proyección estándar",
                Set.of(FormatoProyeccion.DOS_D),
                false,
                false,
                false
        );

        return new Sala(codigo, "Sala " + codigo, tipoSala);
    }

    @Test
    void crearCineCorrectamente() {
        Cine cine = crearCine();

        assertEquals("900123456", cine.getNit());
        assertEquals("CinemaUQ S.A.S.", cine.getRazonSocial());
        assertEquals("CinemaUQ", cine.getNombreComercial());
        assertEquals("Calle 12 # 15-20", cine.getDireccion());
        assertEquals("3001234567", cine.getTelefono());
        assertEquals("contacto@cinemauq.com", cine.getCorreoElectronico());
        assertEquals("www.cinemauq.com", cine.getPaginaWeb());
        assertTrue(cine.isActivo());
    }

    @Test
    void crearCineConPaginaWebNula() {
        Cine cine = new Cine(
                "900123456",
                "CinemaUQ S.A.S.",
                "CinemaUQ",
                "Calle 12",
                "3001234567",
                "contacto@cinemauq.com",
                null
        );

        assertEquals("", cine.getPaginaWeb());
    }

    @Test
    void crearCineEliminaEspaciosDeLosTextos() {
        Cine cine = new Cine(
                " 900123456 ",
                " CinemaUQ S.A.S. ",
                " CinemaUQ ",
                " Calle 12 ",
                " 3001234567 ",
                " contacto@cinemauq.com ",
                " www.cinemauq.com "
        );

        assertEquals("900123456", cine.getNit());
        assertEquals("CinemaUQ S.A.S.", cine.getRazonSocial());
        assertEquals("CinemaUQ", cine.getNombreComercial());
        assertEquals("Calle 12", cine.getDireccion());
        assertEquals("3001234567", cine.getTelefono());
        assertEquals("contacto@cinemauq.com", cine.getCorreoElectronico());
        assertEquals("www.cinemauq.com", cine.getPaginaWeb());
    }

    @Test
    void rechazarNitNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cine(null, "Razón social", "Nombre",
                        "Dirección", "Teléfono", "correo", "web")
        );
    }

    @Test
    void rechazarNitVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cine(" ", "Razón social", "Nombre",
                        "Dirección", "Teléfono", "correo", "web")
        );
    }

    @Test
    void rechazarRazonSocialNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cine("123", null, "Nombre",
                        "Dirección", "Teléfono", "correo", "web")
        );
    }

    @Test
    void rechazarNombreComercialVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cine("123", "Razón social", " ",
                        "Dirección", "Teléfono", "correo", "web")
        );
    }

    @Test
    void rechazarDireccionNula() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cine("123", "Razón social", "Nombre",
                        null, "Teléfono", "correo", "web")
        );
    }

    @Test
    void rechazarTelefonoVacio() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cine("123", "Razón social", "Nombre",
                        "Dirección", " ", "correo", "web")
        );
    }

    @Test
    void rechazarCorreoElectronicoNulo() {
        assertThrows(IllegalArgumentException.class, () ->
                new Cine("123", "Razón social", "Nombre",
                        "Dirección", "Teléfono", null, "web")
        );
    }

    @Test
    void actualizarInformacionCorrectamente() {
        Cine cine = crearCine();

        cine.actualizarInformacion(
                "Nueva razón social",
                "Nuevo nombre",
                "Nueva dirección",
                "3111111111",
                "nuevo@cinemauq.com",
                "www.nuevocine.com"
        );

        assertEquals("Nueva razón social", cine.getRazonSocial());
        assertEquals("Nuevo nombre", cine.getNombreComercial());
        assertEquals("Nueva dirección", cine.getDireccion());
        assertEquals("3111111111", cine.getTelefono());
        assertEquals("nuevo@cinemauq.com", cine.getCorreoElectronico());
        assertEquals("www.nuevocine.com", cine.getPaginaWeb());
    }

    @Test
    void actualizarInformacionConPaginaWebNula() {
        Cine cine = crearCine();

        cine.actualizarInformacion(
                "Nueva razón",
                "Nuevo nombre",
                "Nueva dirección",
                "3111111111",
                "nuevo@cinemauq.com",
                null
        );

        assertEquals("", cine.getPaginaWeb());
    }

    @Test
    void rechazarActualizacionConRazonSocialVacia() {
        Cine cine = crearCine();

        assertThrows(IllegalArgumentException.class, () ->
                cine.actualizarInformacion(
                        " ",
                        "Nuevo nombre",
                        "Nueva dirección",
                        "3111111111",
                        "nuevo@cinemauq.com",
                        "web"
                )
        );
    }

    @Test
    void activarCine() {
        Cine cine = crearCine();

        cine.desactivar();
        cine.activar();

        assertTrue(cine.isActivo());
    }

    @Test
    void desactivarCine() {
        Cine cine = crearCine();

        cine.desactivar();

        assertFalse(cine.isActivo());
    }

    @Test
    void registrarSalaCorrectamente() {
        Cine cine = crearCine();
        Sala sala = crearSala("S01");

        cine.registrarSala(sala);

        assertEquals(1, cine.getSalas().size());
        assertTrue(cine.getSalas().contains(sala));
    }

    @Test
    void rechazarRegistroDeSalaNula() {
        Cine cine = crearCine();

        assertThrows(NullPointerException.class, () ->
                cine.registrarSala(null)
        );
    }

    @Test
    void rechazarDosSalasConElMismoCodigo() {
        Cine cine = crearCine();

        cine.registrarSala(crearSala("S01"));

        assertThrows(IllegalArgumentException.class, () ->
                cine.registrarSala(crearSala("S01"))
        );
    }

    @Test
    void buscarSalaExistente() {
        Cine cine = crearCine();
        Sala sala = crearSala("S01");

        cine.registrarSala(sala);

        assertTrue(cine.buscarSala("S01").isPresent());
        assertEquals(sala, cine.buscarSala("S01").get());
    }

    @Test
    void buscarSalaInexistente() {
        Cine cine = crearCine();

        assertTrue(cine.buscarSala("S99").isEmpty());
    }

    @Test
    void buscarSalaConCodigoNulo() {
        Cine cine = crearCine();

        assertTrue(cine.buscarSala(null).isEmpty());
    }

    @Test
    void buscarSalaConCodigoVacio() {
        Cine cine = crearCine();

        assertTrue(cine.buscarSala(" ").isEmpty());
    }

    @Test
    void buscarSalaEliminandoEspaciosDelCodigo() {
        Cine cine = crearCine();
        Sala sala = crearSala("S01");

        cine.registrarSala(sala);

        assertEquals(sala, cine.buscarSala(" S01 ").orElse(null));
    }

    @Test
    void listaDeSalasInicialmenteVacia() {
        Cine cine = crearCine();

        assertTrue(cine.getSalas().isEmpty());
    }

    @Test
    void listaDeSalasNoPuedeModificarseDesdeFuera() {
        Cine cine = crearCine();
        cine.registrarSala(crearSala("S01"));

        assertThrows(UnsupportedOperationException.class, () ->
                cine.getSalas().add(crearSala("S02"))
        );
    }
}