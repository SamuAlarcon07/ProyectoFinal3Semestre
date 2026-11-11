package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.enums.TipoListaPelicula;

import co.edu.uniquindio.proyectofinal.model.principal.Cliente;

import co.edu.uniquindio.proyectofinal.model.principal.Pelicula;

import co.edu.uniquindio.proyectofinal.model.principal.PeliculaGuardada;



import java.util.ArrayList;

import java.util.List;

import java.util.Optional;

import java.util.UUID;



/** Gestiona las listas personales de películas favoritas y pendientes por ver. */

public class ListaPersonalService {



    private final List<PeliculaGuardada> peliculasGuardadas = new ArrayList<>();



    public PeliculaGuardada agregar(Cliente cliente, Pelicula pelicula, TipoListaPelicula tipo) {

        if (cliente == null || pelicula == null || tipo == null) {

            throw new IllegalArgumentException("El cliente, la película y el tipo de lista son obligatorios.");

        }

        boolean existe = peliculasGuardadas.stream().anyMatch(p ->

                p.getCliente().getId().equals(cliente.getId())

                        && p.getPelicula().getCodigo().equals(pelicula.getCodigo())

                        && p.getTipoLista() == tipo);

        if (existe) throw new IllegalArgumentException("La película ya está en esa lista.");

        PeliculaGuardada guardada = new PeliculaGuardada(UUID.randomUUID().toString(), cliente, pelicula, tipo);

        peliculasGuardadas.add(guardada);

        return guardada;

    }



    public void cambiarLista(Cliente cliente, Pelicula pelicula, TipoListaPelicula nuevoTipo) {

        if (cliente == null || pelicula == null || nuevoTipo == null) {

            throw new IllegalArgumentException("El cliente, la película y la lista destino son obligatorios.");

        }

        PeliculaGuardada guardada = buscar(cliente, pelicula, nuevoTipo == TipoListaPelicula.FAVORITA

                ? TipoListaPelicula.POR_VER : TipoListaPelicula.FAVORITA)

                .orElseThrow(() -> new IllegalArgumentException("La película no está en la otra lista."));

        guardada.cambiarTipoLista(nuevoTipo);

    }



    public void eliminar(Cliente cliente, Pelicula pelicula, TipoListaPelicula tipo) {

        if (cliente == null || pelicula == null || tipo == null) {

            throw new IllegalArgumentException("El cliente, la película y el tipo de lista son obligatorios.");

        }

        boolean eliminada = peliculasGuardadas.removeIf(p ->

                p.getCliente().getId().equals(cliente.getId())

                        && p.getPelicula().getCodigo().equals(pelicula.getCodigo())

                        && p.getTipoLista() == tipo);

        if (!eliminada) throw new IllegalArgumentException("No se encontró la película en esa lista.");

    }



    public List<PeliculaGuardada> consultarLista(Cliente cliente, TipoListaPelicula tipo) {

        if (cliente == null || tipo == null) return List.of();

        return peliculasGuardadas.stream().filter(p ->

                p.getCliente().getId().equals(cliente.getId()) && p.getTipoLista() == tipo).toList();

    }



    private Optional<PeliculaGuardada> buscar(Cliente cliente, Pelicula pelicula, TipoListaPelicula tipo) {

        return peliculasGuardadas.stream().filter(p ->

                p.getCliente().getId().equals(cliente.getId())

                        && p.getPelicula().getCodigo().equals(pelicula.getCodigo())

                        && p.getTipoLista() == tipo).findFirst();

    }

}

