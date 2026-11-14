package co.edu.uniquindio.proyectofinal.model.util;

import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import co.edu.uniquindio.proyectofinal.model.enums.TipoAsiento;
import co.edu.uniquindio.proyectofinal.model.principal.Asiento;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;
import co.edu.uniquindio.proyectofinal.model.principal.Pelicula;
import co.edu.uniquindio.proyectofinal.model.principal.Sala;
import co.edu.uniquindio.proyectofinal.model.principal.TipoSala;
import co.edu.uniquindio.proyectofinal.model.service.AdministracionCatalogoService;
import co.edu.uniquindio.proyectofinal.model.service.CineService;
import co.edu.uniquindio.proyectofinal.model.service.FuncionService;
import co.edu.uniquindio.proyectofinal.model.service.GestionSalasService;
import co.edu.uniquindio.proyectofinal.model.service.DisponibilidadAsientosService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Prepara salas, asientos y funciones de demostración para CinemaUQ.
 *
 * La carga es idempotente: conserva las salas que ya existan, completa sus
 * asientos si hace falta y no registra dos veces una función de demostración.
 * Las funciones se programan desde la fecha local actual hasta los seis días
 * siguientes. En el día actual solo se agregan horarios que todavía no hayan
 * comenzado.
 */
public final class DatosInicialesFuncionesCinemaUQ {

    private static final int DIAS_PROGRAMACION = 7;
    private static final int MINUTOS_MARGEN = 15;
    private static final DateTimeFormatter FORMATO_CODIGO_FECHA =
            DateTimeFormatter.BASIC_ISO_DATE;

    /**
     * Ocupación visual inicial para las diez funciones diarias de demostración.
     * Los asientos se concentran en grupos contiguos de dos y tres plazas,
     * como una reserva de pareja o de pequeño grupo, nunca en diagonal.
     */
    /**
     * Patrones de ocupación coherentes para las funciones demo.
     * Cada función muestra tres grupos contiguos: un grupo de tres
     * asientos y dos parejas en otras filas. No se distribuyen en diagonal.
     * El patrón rota por día y horario para que no todas las funciones
     * tengan exactamente las mismas plazas ocupadas.
     */
    private static final List<List<String>> ASIENTOS_OCUPADOS_DEMO = List.of(
            List.of("B4", "B5", "B6", "E2", "E3", "G8", "G9"),
            List.of("A2", "A3", "A4", "D6", "D7", "H9", "H10"),
            List.of("C1", "C2", "C3", "F5", "F6", "G8", "G9"),
            List.of("B7", "B8", "B9", "D2", "D3", "F4", "F5"),
            List.of("A6", "A7", "A8", "C3", "C4", "H1", "H2"),
            List.of("B1", "B2", "B3", "E7", "E8", "G5", "G6"),
            List.of("C6", "C7", "C8", "E2", "E3", "H9", "H10"),
            List.of("A3", "A4", "A5", "D8", "D9", "F1", "F2"),
            List.of("B5", "B6", "B7", "E9", "E10", "G2", "G3"),
            List.of("C2", "C3", "C4", "F6", "F7", "H4", "H5")
    );

    private DatosInicialesFuncionesCinemaUQ() {
        // Clase de utilidad.
    }

    /**
     * Crea tres salas de demostración y programa las diez películas del
     * catálogo durante una semana.
     *
     * @return cantidad de funciones nuevas registradas
     */
    public static int cargarFunciones(AdministracionCatalogoService catalogo,
                                      FuncionService funcionService,
                                      GestionSalasService gestionSalasService,
                                      CineService cineService) {
        return cargarFunciones(
                catalogo, funcionService, gestionSalasService, cineService, null);
    }

    /**
     * Carga funciones y, cuando se proporciona el servicio de disponibilidad,
     * marca ocupaciones visuales de demostración para las funciones iniciales.
     */
    public static int cargarFunciones(AdministracionCatalogoService catalogo,
                                      FuncionService funcionService,
                                      GestionSalasService gestionSalasService,
                                      CineService cineService,
                                      DisponibilidadAsientosService disponibilidadService) {
        Objects.requireNonNull(catalogo, "El catálogo es obligatorio.");
        Objects.requireNonNull(funcionService, "El servicio de funciones es obligatorio.");
        Objects.requireNonNull(gestionSalasService, "La gestión de salas es obligatoria.");
        Objects.requireNonNull(cineService, "El servicio del cine es obligatorio.");

        Sala salaDosD = obtenerOCrearSala(
                "SALA-DEMO-2D", "Sala 1 · Tradicional", "TS-2D",
                gestionSalasService, cineService);
        Sala salaTresD = obtenerOCrearSala(
                "SALA-DEMO-3D", "Sala 2 · 3D", "TS-3D",
                gestionSalasService, cineService);
        Sala salaCuatroD = obtenerOCrearSala(
                "SALA-DEMO-4D", "Sala 3 · 4D", "TS-4D",
                gestionSalasService, cineService);

        List<Pelicula> peliculas = obtenerPeliculas(catalogo);
        List<HorarioDemo> horarios = crearHorarios(salaDosD, salaTresD, salaCuatroD);

        LocalDate hoy = LocalDate.now();
        LocalDateTime ahora = LocalDateTime.now();
        int registradas = 0;

        for (int dia = 0; dia < DIAS_PROGRAMACION; dia++) {
            LocalDate fecha = hoy.plusDays(dia);

            for (int indice = 0; indice < horarios.size(); indice++) {
                HorarioDemo horario = horarios.get(indice);
                LocalDateTime fechaHora = LocalDateTime.of(fecha, horario.hora);

                // No se programan funciones para horas que ya pasaron hoy.
                if (dia == 0 && !fechaHora.isAfter(ahora.plusMinutes(MINUTOS_MARGEN))) {
                    continue;
                }

                String codigo = "FUN-DEMO-" + fecha.format(FORMATO_CODIGO_FECHA)
                        + "-" + String.format("%02d", indice + 1);

                Funcion funcion = funcionService.buscarFuncion(codigo).orElse(null);
                if (funcion == null) {
                    Pelicula pelicula = peliculas.get(indice);
                    funcion = new Funcion(
                            codigo,
                            pelicula,
                            horario.sala,
                            fechaHora,
                            horario.precio,
                            horario.formato
                    );
                    funcionService.registrarFuncion(funcion);
                    registradas++;
                }

                // Idempotente: si el cargador se invoca nuevamente, las mismas
                // plazas permanecen ocupadas sin duplicar registros.
                if (disponibilidadService != null) {
                    int patronOcupacion = (indice + dia) % ASIENTOS_OCUPADOS_DEMO.size();
                    disponibilidadService.marcarAsientosOcupadosDemostracion(
                            funcion, ASIENTOS_OCUPADOS_DEMO.get(patronOcupacion));
                }
            }
        }

        return registradas;
    }

    private static Sala obtenerOCrearSala(String codigo, String nombre,
                                          String codigoTipoSala,
                                          GestionSalasService gestionSalasService,
                                          CineService cineService) {
        TipoSala tipoSala = gestionSalasService.consultarTiposSala().stream()
                .filter(tipo -> tipo.getCodigo().equals(codigoTipoSala))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No está registrado el tipo de sala " + codigoTipoSala + "."));

        Sala sala = cineService.buscarSala(codigo).orElseGet(() -> {
            Sala nueva = new Sala(codigo, nombre, tipoSala);
            gestionSalasService.registrarSala(nueva);
            return nueva;
        });

        completarAsientos(sala);
        return sala;
    }

    /** Completa una distribución de 80 asientos sin duplicar los existentes. */
    private static void completarAsientos(Sala sala) {
        for (int fila = 0; fila < 8; fila++) {
            String letra = String.valueOf((char) ('A' + fila));
            for (int numero = 1; numero <= 10; numero++) {
                if (sala.contieneAsiento(new Asiento(letra, numero, TipoAsiento.GENERAL))) {
                    continue;
                }

                TipoAsiento tipo = fila == 0
                        ? TipoAsiento.PREFERENCIAL
                        : fila == 7 && numero <= 2
                          ? TipoAsiento.ACCESIBLE
                          : TipoAsiento.GENERAL;
                sala.agregarAsiento(new Asiento(letra, numero, tipo));
            }
        }
    }

    private static List<Pelicula> obtenerPeliculas(AdministracionCatalogoService catalogo) {
        List<String> codigos = List.of(
                "PEL-001", "PEL-002", "PEL-003", "PEL-004", "PEL-005",
                "PEL-006", "PEL-007", "PEL-008", "PEL-009", "PEL-010"
        );
        List<Pelicula> peliculas = new ArrayList<>();
        for (String codigo : codigos) {
            Pelicula pelicula = catalogo.buscarPelicula(codigo)
                    .orElseThrow(() -> new IllegalStateException(
                            "Falta registrar la película " + codigo + " antes de programar funciones."));
            peliculas.add(pelicula);
        }
        return List.copyOf(peliculas);
    }

    /**
     * Diez horarios al día. Cada sala vuelve a utilizarse después de más de
     * tres horas y media, suficiente para la película más larga y la limpieza.
     */
    private static List<HorarioDemo> crearHorarios(Sala salaDosD,
                                                   Sala salaTresD,
                                                   Sala salaCuatroD) {
        return List.of(
                new HorarioDemo(salaDosD, LocalTime.of(10, 0),
                        FormatoProyeccion.DOS_D, 15000),
                new HorarioDemo(salaTresD, LocalTime.of(10, 45),
                        FormatoProyeccion.TRES_D, 19000),
                new HorarioDemo(salaCuatroD, LocalTime.of(11, 30),
                        FormatoProyeccion.CUATRO_D, 25000),
                new HorarioDemo(salaDosD, LocalTime.of(13, 30),
                        FormatoProyeccion.DOS_D, 15000),
                new HorarioDemo(salaTresD, LocalTime.of(14, 15),
                        FormatoProyeccion.TRES_D, 19000),
                new HorarioDemo(salaCuatroD, LocalTime.of(15, 0),
                        FormatoProyeccion.CUATRO_D, 25000),
                new HorarioDemo(salaDosD, LocalTime.of(17, 0),
                        FormatoProyeccion.DOS_D, 15000),
                new HorarioDemo(salaTresD, LocalTime.of(17, 45),
                        FormatoProyeccion.TRES_D, 19000),
                new HorarioDemo(salaCuatroD, LocalTime.of(18, 30),
                        FormatoProyeccion.CUATRO_D, 25000),
                new HorarioDemo(salaDosD, LocalTime.of(20, 30),
                        FormatoProyeccion.DOS_D, 15000)
        );
    }

    private static final class HorarioDemo {
        private final Sala sala;
        private final LocalTime hora;
        private final FormatoProyeccion formato;
        private final BigDecimal precio;

        private HorarioDemo(Sala sala, LocalTime hora,
                            FormatoProyeccion formato, long precio) {
            this.sala = sala;
            this.hora = hora;
            this.formato = formato;
            this.precio = BigDecimal.valueOf(precio);
        }
    }
}
