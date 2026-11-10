package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;

import co.edu.uniquindio.proyectofinal.model.principal.Cliente;

import co.edu.uniquindio.proyectofinal.model.principal.Compra;

import co.edu.uniquindio.proyectofinal.model.principal.Entrada;

import co.edu.uniquindio.proyectofinal.model.principal.Pelicula;

import co.edu.uniquindio.proyectofinal.model.principal.ResenaPelicula;



import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Optional;

import java.util.UUID;



/** Valida y administra las reseñas de películas realizadas por clientes. */

public class ResenaService {



    private final Map<String, ResenaPelicula> resenas = new LinkedHashMap<>();



    public ResenaPelicula publicarResena(Cliente cliente, Pelicula pelicula,

                                         int calificacion, String comentario, List<Compra> comprasCliente) {

        if (cliente == null || pelicula == null || comprasCliente == null) {

            throw new IllegalArgumentException("El cliente, la película y el historial de compras son obligatorios.");

        }

        boolean puedeResenar = comprasCliente.stream()

                .filter(c -> c.getCliente().getId().equals(cliente.getId()))

                .flatMap(c -> c.getDetalles().stream())

                .map(d -> d.getElemento())

                .filter(Entrada.class::isInstance)

                .map(Entrada.class::cast)

                .anyMatch(e -> e.getFuncion().getPelicula().getCodigo().equals(pelicula.getCodigo())

                        && e.getFuncion().getEstado() == EstadoFuncion.FINALIZADA

                        && e.getEstado() != EstadoEntrada.ANULADA);

        if (!puedeResenar) throw new IllegalStateException("El cliente no tiene una entrada válida para una función finalizada de esta película.");

        String clave = clave(cliente, pelicula);

        if (resenas.containsKey(clave)) throw new IllegalStateException("El cliente ya publicó una reseña para esta película.");

        ResenaPelicula resena = new ResenaPelicula(UUID.randomUUID().toString(),

                cliente, pelicula, calificacion, comentario);

        resenas.put(clave, resena);

        return resena;

    }



    public void editarResena(Cliente cliente, Pelicula pelicula,

                             int calificacion, String comentario) {

        ResenaPelicula resena = obtener(cliente, pelicula);

        resena.editarResena(calificacion, comentario);

    }



    public void ocultarResena(Cliente cliente, Pelicula pelicula) {

        obtener(cliente, pelicula).ocultar();

    }



    public List<ResenaPelicula> consultarResenas(Pelicula pelicula) {

        return resenas.values().stream().filter(r -> r.getPelicula().getCodigo().equals(pelicula.getCodigo())

                && r.estaVisible()).toList();

    }



    public double calcularPromedio(Pelicula pelicula) {

        List<ResenaPelicula> visibles = consultarResenas(pelicula);

        return visibles.stream().mapToInt(ResenaPelicula::getCalificacion).average().orElse(0.0);

    }



    private ResenaPelicula obtener(Cliente cliente, Pelicula pelicula) {

        return Optional.ofNullable(resenas.get(clave(cliente, pelicula)))

                .orElseThrow(() -> new IllegalArgumentException("No se encontró la reseña del cliente para esta película."));

    }



    private String clave(Cliente cliente, Pelicula pelicula) {

        return cliente.getId() + "::" + pelicula.getCodigo();

    }

}