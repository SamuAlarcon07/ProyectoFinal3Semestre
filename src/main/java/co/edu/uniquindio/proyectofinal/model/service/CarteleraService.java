package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;

import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import co.edu.uniquindio.proyectofinal.model.principal.Pelicula;



import java.time.LocalDateTime;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Objects;

import java.util.Optional;



/** Prepara las consultas de cartelera visibles para los clientes. */

public class CarteleraService {



    private final AdministracionCatalogoService catalogo;

    private final Map<String, Funcion> funciones = new LinkedHashMap<>();



    public CarteleraService(AdministracionCatalogoService catalogo) {

        this.catalogo = Objects.requireNonNull(catalogo);

    }



    public void registrarFuncion(Funcion funcion) {

        Objects.requireNonNull(funcion, "La función es obligatoria.");

        if (catalogo.buscarPelicula(funcion.getPelicula().getCodigo()).isEmpty()) {

            throw new IllegalArgumentException("La película debe estar registrada en el catálogo.");

        }

        if (funciones.putIfAbsent(funcion.getCodigo(), funcion) != null) {

            throw new IllegalArgumentException("Ya existe una función con ese código.");

        }

    }



    public List<Pelicula> consultarPeliculas() {

        return catalogo.consultarPeliculasActivas();

    }



    public Optional<Funcion> buscarFuncion(String codigo) {

        return Optional.ofNullable(funciones.get(codigo));

    }



    public List<Funcion> consultarFuncionesPorPelicula(String codigoPelicula) {

        return funciones.values().stream()

                .filter(funcion -> funcion.getPelicula().getCodigo().equals(codigoPelicula))

                .filter(funcion -> funcion.getEstado() == EstadoFuncion.PROGRAMADA)

                .filter(funcion -> funcion.getPelicula().estaActiva())

                .sorted(java.util.Comparator.comparing(Funcion::getFechaHora))

                .toList();

    }



    public List<Funcion> consultarFuncionesEntre(LocalDateTime desde, LocalDateTime hasta) {

        Objects.requireNonNull(desde, "La fecha inicial es obligatoria.");

        Objects.requireNonNull(hasta, "La fecha final es obligatoria.");

        if (hasta.isBefore(desde)) throw new IllegalArgumentException("El intervalo de fechas no es válido.");

        return funciones.values().stream()

                .filter(funcion -> funcion.getEstado() == EstadoFuncion.PROGRAMADA)

                .filter(funcion -> !funcion.getFechaHora().isBefore(desde)

                        && !funcion.getFechaHora().isAfter(hasta))

                .sorted(java.util.Comparator.comparing(Funcion::getFechaHora))

                .toList();

    }

}