package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;
import co.edu.uniquindio.proyectofinal.model.principal.Pelicula;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Servicio encargado de preparar las consultas
 * de cartelera que se muestran a los clientes.
 */
public class CarteleraService {

    private final AdministracionCatalogoService catalogo;

    private final FuncionService funcionService;

    /**
     * Constructor compatible con el uso anterior.
     */
    public CarteleraService(
            AdministracionCatalogoService catalogo
    ) {
        this(catalogo, new FuncionService());
    }

    /**
     * Constructor recomendado para compartir las funciones
     * registradas por la administración.
     */
    public CarteleraService(
            AdministracionCatalogoService catalogo,
            FuncionService funcionService
    ) {

        this.catalogo = Objects.requireNonNull(
                catalogo,
                "El catálogo es obligatorio."
        );

        this.funcionService = Objects.requireNonNull(
                funcionService,
                "El servicio de funciones es obligatorio."
        );
    }

    /**
     * Registra una función si su película existe en el catálogo.
     */
    public void registrarFuncion(Funcion funcion) {

        Objects.requireNonNull(
                funcion,
                "La función es obligatoria."
        );

        if (catalogo.buscarPelicula(
                funcion.getPelicula().getCodigo()
        ).isEmpty()) {

            throw new IllegalArgumentException(
                    "La película debe estar registrada en el catálogo."
            );
        }

        funcionService.registrarFuncion(funcion);
    }

    /**
     * Consulta las películas activas.
     */
    public List<Pelicula> consultarPeliculas() {
        return catalogo.consultarPeliculasActivas();
    }

    /**
     * Busca una función por su código.
     */
    public Optional<Funcion> buscarFuncion(String codigo) {
        return funcionService.buscarFuncion(codigo);
    }

    /**
     * Consulta las funciones programadas de una película activa.
     */
    public List<Funcion> consultarFuncionesPorPelicula(
            String codigoPelicula
    ) {

        if (codigoPelicula == null || codigoPelicula.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de película es obligatorio."
            );
        }

        return funcionService.consultarFunciones()
                .stream()
                .filter(funcion ->
                        funcion.getPelicula().getCodigo()
                                .equals(codigoPelicula.trim())
                )
                .filter(funcion ->
                        funcion.getEstado() == EstadoFuncion.PROGRAMADA
                )
                .filter(funcion ->
                        funcion.getPelicula().estaActiva()
                )
                .sorted(
                        Comparator.comparing(Funcion::getFechaHora)
                )
                .toList();
    }

    /**
     * Consulta funciones programadas en un intervalo.
     */
    public List<Funcion> consultarFuncionesEntre(
            LocalDateTime desde,
            LocalDateTime hasta
    ) {

        Objects.requireNonNull(
                desde,
                "La fecha inicial es obligatoria."
        );

        Objects.requireNonNull(
                hasta,
                "La fecha final es obligatoria."
        );

        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException(
                    "El intervalo de fechas no es válido."
            );
        }

        return funcionService.consultarFunciones()
                .stream()
                .filter(funcion ->
                        funcion.getEstado() == EstadoFuncion.PROGRAMADA
                )
                .filter(funcion ->
                        funcion.getPelicula().estaActiva()
                )
                .filter(funcion ->
                        !funcion.getFechaHora().isBefore(desde)
                                && !funcion.getFechaHora().isAfter(hasta)
                )
                .sorted(
                        Comparator.comparing(Funcion::getFechaHora)
                )
                .toList();
    }
}
