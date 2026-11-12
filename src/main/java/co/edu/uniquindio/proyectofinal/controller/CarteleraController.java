package co.edu.uniquindio.proyectofinal.controller;

import co.edu.uniquindio.proyectofinal.model.principal.Asiento;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;
import co.edu.uniquindio.proyectofinal.model.principal.Pelicula;
import co.edu.uniquindio.proyectofinal.model.service.CarteleraService;
import co.edu.uniquindio.proyectofinal.model.service.DisponibilidadAsientosService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Controlador de aplicación del módulo de cartelera.
 * Coordina las consultas del catálogo y la disponibilidad.
 */
public class CarteleraController {

    private final CarteleraService carteleraService;

    private final DisponibilidadAsientosService disponibilidadService;

    public CarteleraController(
            CarteleraService carteleraService,
            DisponibilidadAsientosService disponibilidadService
    ) {
        this.carteleraService = Objects.requireNonNull(
                carteleraService,
                "El servicio de cartelera es obligatorio."
        );

        this.disponibilidadService = Objects.requireNonNull(
                disponibilidadService,
                "El servicio de disponibilidad es obligatorio."
        );
    }

    /**
     * Consulta las películas activas ordenadas por título.
     */
    public List<Pelicula> consultarPeliculas() {

        return carteleraService.consultarPeliculas()
                .stream()
                .sorted(
                        Comparator.comparing(
                                Pelicula::getTitulo,
                                String.CASE_INSENSITIVE_ORDER
                        )
                )
                .toList();
    }

    /**
     * Busca películas por título, sinopsis o género.
     * También permite aplicar un filtro de género.
     */
    public List<Pelicula> buscarPeliculas(
            String texto,
            String genero
    ) {

        String criterio = normalizar(texto);
        String generoSeleccionado = normalizar(genero);

        return consultarPeliculas()
                .stream()
                .filter(pelicula ->
                        criterio.isEmpty()
                                || normalizar(pelicula.getTitulo()).contains(criterio)
                                || normalizar(pelicula.getSinopsis()).contains(criterio)
                                || normalizar(pelicula.getGenero()).contains(criterio)
                )
                .filter(pelicula ->
                        generoSeleccionado.isEmpty()
                                || generoSeleccionado.equals("todos")
                                || normalizar(pelicula.getGenero())
                                .equals(generoSeleccionado)
                )
                .toList();
    }

    /**
     * Consulta los géneros disponibles en el catálogo.
     */
    public List<String> consultarGeneros() {

        return consultarPeliculas()
                .stream()
                .map(Pelicula::getGenero)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /**
     * Consulta las funciones futuras de una película.
     */
    public List<Funcion> consultarFunciones(String codigoPelicula) {

        if (codigoPelicula == null || codigoPelicula.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de película es obligatorio."
            );
        }

        LocalDateTime ahora = LocalDateTime.now();

        return carteleraService
                .consultarFuncionesPorPelicula(codigoPelicula.trim())
                .stream()
                .filter(funcion ->
                        funcion.getFechaHora().isAfter(ahora)
                )
                .sorted(
                        Comparator.comparing(Funcion::getFechaHora)
                )
                .toList();
    }

    /**
     * Consulta los asientos activos de la sala de una función.
     */
    public List<Asiento> consultarAsientos(Funcion funcion) {

        Objects.requireNonNull(
                funcion,
                "La función es obligatoria."
        );

        return funcion.getSala()
                .getAsientos()
                .stream()
                .filter(Asiento::isActivo)
                .sorted(
                        Comparator.comparing(
                                Asiento::getFila,
                                String.CASE_INSENSITIVE_ORDER
                        ).thenComparingInt(Asiento::getNumero)
                )
                .toList();
    }

    /**
     * Comprueba si un asiento está disponible.
     */
    public boolean estaDisponible(
            Funcion funcion,
            Asiento asiento
    ) {

        return disponibilidadService.estaDisponible(
                Objects.requireNonNull(funcion),
                Objects.requireNonNull(asiento),
                LocalDateTime.now()
        );
    }

    private String normalizar(String valor) {
        return valor == null
                ? ""
                : valor.trim().toLowerCase(java.util.Locale.ROOT);
    }
}
