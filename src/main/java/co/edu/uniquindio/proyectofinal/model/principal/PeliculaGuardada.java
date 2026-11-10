package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoListaPelicula;
import java.time.LocalDateTime;
import java.util.Objects;

/** Representa una película guardada por un cliente en una de sus listas personales. */
public class PeliculaGuardada {

    private final String codigo;
    private final Cliente cliente;
    private final Pelicula pelicula;
    private TipoListaPelicula tipoLista;
    private final LocalDateTime fechaAgregada;

    public PeliculaGuardada(String codigo, Cliente cliente, Pelicula pelicula,
                            TipoListaPelicula tipoLista) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        this.codigo = codigo.trim();
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        this.pelicula = Objects.requireNonNull(pelicula, "La película es obligatoria.");
        this.tipoLista = Objects.requireNonNull(tipoLista, "El tipo de lista es obligatorio.");
        this.fechaAgregada = LocalDateTime.now();
    }

    /** Cambia la película de una lista personal a otra. */
    public void cambiarTipoLista(TipoListaPelicula nuevoTipo) {
        this.tipoLista = Objects.requireNonNull(nuevoTipo, "El tipo de lista es obligatorio.");
    }

    public String getCodigo() { return codigo; }
    public Cliente getCliente() { return cliente; }
    public Pelicula getPelicula() { return pelicula; }
    public TipoListaPelicula getTipoLista() { return tipoLista; }
    public LocalDateTime getFechaAgregada() { return fechaAgregada; }
}