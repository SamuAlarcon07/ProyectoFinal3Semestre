package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;
import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;
import co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator.ElementoVendibleDecorador;
import co.edu.uniquindio.proyectofinal.model.principal.*;

import java.util.*;

/** Valida y administra las reseñas de películas realizadas por clientes. */
public class ResenaService {

    private final Map<String, ResenaPelicula> resenas = new LinkedHashMap<>();

    /**
     * Publica una reseña únicamente si el cliente tiene una compra PAGADA
     * que incluya una entrada no anulada para una función finalizada de la película.
     */
    public synchronized ResenaPelicula publicarResena(
            Cliente cliente, Pelicula pelicula, int calificacion,
            String comentario, List<Compra> comprasCliente) {

        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        Objects.requireNonNull(pelicula, "La película es obligatoria.");
        Objects.requireNonNull(comprasCliente, "El historial de compras es obligatorio.");

        boolean puedeResenar = comprasCliente.stream()
                .filter(Objects::nonNull)
                .filter(compra -> compra.getCliente() != null
                        && Objects.equals(compra.getCliente().getId(), cliente.getId()))
                .filter(compra -> "PAGADA".equals(compra.consultarNombreEstado()))
                .flatMap(compra -> compra.getDetalles().stream())
                .map(detalle -> desenvolver(detalle.getElemento()))
                .filter(Entrada.class::isInstance)
                .map(Entrada.class::cast)
                .anyMatch(entrada -> entrada.getFuncion() != null
                        && entrada.getFuncion().getPelicula() != null
                        && Objects.equals(
                        entrada.getFuncion().getPelicula().getCodigo(),
                        pelicula.getCodigo())
                        && entrada.getFuncion().getEstado() == EstadoFuncion.FINALIZADA
                        && entrada.getEstado() != EstadoEntrada.ANULADA);

        if (!puedeResenar) {
            throw new IllegalStateException(
                    "El cliente debe tener una compra pagada con una entrada válida "
                            + "para una función finalizada de esta película.");
        }

        String clave = clave(cliente, pelicula);
        if (resenas.containsKey(clave)) {
            throw new IllegalStateException(
                    "El cliente ya publicó una reseña para esta película.");
        }

        ResenaPelicula resena = new ResenaPelicula(
                UUID.randomUUID().toString(), cliente, pelicula,
                calificacion, comentario);
        resenas.put(clave, resena);
        return resena;
    }

    public synchronized void editarResena(
            Cliente cliente, Pelicula pelicula,
            int calificacion, String comentario) {
        obtener(cliente, pelicula).editarResena(calificacion, comentario);
    }

    public synchronized void ocultarResena(Cliente cliente, Pelicula pelicula) {
        obtener(cliente, pelicula).ocultar();
    }

    public synchronized List<ResenaPelicula> consultarResenas(Pelicula pelicula) {
        Objects.requireNonNull(pelicula, "La película es obligatoria.");
        return resenas.values().stream()
                .filter(resena -> Objects.equals(
                        resena.getPelicula().getCodigo(), pelicula.getCodigo()))
                .filter(ResenaPelicula::estaVisible)
                .toList();
    }

    public synchronized double calcularPromedio(Pelicula pelicula) {
        List<ResenaPelicula> visibles = consultarResenas(pelicula);
        return visibles.stream()
                .mapToInt(ResenaPelicula::getCalificacion)
                .average()
                .orElse(0.0);
    }

    private ResenaPelicula obtener(Cliente cliente, Pelicula pelicula) {
        String clave = clave(cliente, pelicula);
        return Optional.ofNullable(resenas.get(clave))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la reseña del cliente para esta película."));
    }

    private String clave(Cliente cliente, Pelicula pelicula) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        Objects.requireNonNull(pelicula, "La película es obligatoria.");
        return cliente.getId() + "::" + pelicula.getCodigo();
    }

    /** Retira uno o varios decoradores sin perder el tipo real del producto. */
    private ElementoVendible desenvolver(ElementoVendible elemento) {
        ElementoVendible actual = Objects.requireNonNull(
                elemento, "El elemento de compra es obligatorio.");
        Set<ElementoVendible> visitados = Collections.newSetFromMap(
                new IdentityHashMap<>());

        while (actual instanceof ElementoVendibleDecorador decorador) {
            if (!visitados.add(actual)) {
                throw new IllegalStateException(
                        "Se detectó un ciclo en los decoradores del elemento vendible.");
            }
            actual = decorador.obtenerElementoOriginal();
        }
        return actual;
    }
}
