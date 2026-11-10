package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.principal.Pelicula;

import co.edu.uniquindio.proyectofinal.model.principal.Producto;



import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Optional;



/** Administra el catálogo de películas y productos de confitería. */

public class AdministracionCatalogoService {



    private final Map<String, Pelicula> peliculas = new LinkedHashMap<>();

    private final Map<String, Producto> productos = new LinkedHashMap<>();



    public void registrarPelicula(Pelicula pelicula) {

        if (pelicula == null) throw new IllegalArgumentException("La película es obligatoria.");

        if (peliculas.putIfAbsent(pelicula.getCodigo(), pelicula) != null) {

            throw new IllegalArgumentException("Ya existe una película con ese código.");

        }

    }



    public void registrarProducto(Producto producto) {

        if (producto == null) throw new IllegalArgumentException("El producto es obligatorio.");

        if (productos.putIfAbsent(producto.getCodigo(), producto) != null) {

            throw new IllegalArgumentException("Ya existe un producto con ese código.");

        }

    }



    public Optional<Pelicula> buscarPelicula(String codigo) {

        return Optional.ofNullable(peliculas.get(codigo));

    }



    public Optional<Producto> buscarProducto(String codigo) {

        return Optional.ofNullable(productos.get(codigo));

    }



    public List<Pelicula> consultarPeliculas() {

        return List.copyOf(peliculas.values());

    }



    public List<Pelicula> consultarPeliculasActivas() {

        return peliculas.values().stream().filter(Pelicula::estaActiva).toList();

    }



    public List<Producto> consultarProductos() {

        return List.copyOf(productos.values());

    }



    public void desactivarPelicula(String codigo) {

        buscarPelicula(codigo).orElseThrow(() -> new IllegalArgumentException("Película no encontrada.")).desactivar();

    }



    public void desactivarProducto(String codigo) {

        buscarProducto(codigo).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado.")).desactivar();

    }

}