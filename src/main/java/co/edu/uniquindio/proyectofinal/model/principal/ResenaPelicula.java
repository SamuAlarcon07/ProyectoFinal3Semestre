package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoResena;
import java.time.LocalDateTime;
import java.util.Objects;

/** Representa la calificación y el comentario de un cliente sobre una película. */
public class ResenaPelicula {

    private final String codigo;
    private final Cliente cliente;
    private final Pelicula pelicula;
    private int calificacion;
    private String comentario;
    private LocalDateTime fechaPublicacion;
    private EstadoResena estado;

    public ResenaPelicula(String codigo, Cliente cliente, Pelicula pelicula,
                          int calificacion, String comentario) {
        this.codigo = validarTexto(codigo, "El código");
        this.cliente = Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        this.pelicula = Objects.requireNonNull(pelicula, "La película es obligatoria.");
        actualizarContenido(calificacion, comentario);
        this.fechaPublicacion = LocalDateTime.now();
        this.estado = EstadoResena.PUBLICADA;
    }

    /** Actualiza la calificación y el comentario de la reseña. */
    public void editarResena(int nuevaCalificacion, String nuevoComentario) {
        actualizarContenido(nuevaCalificacion, nuevoComentario);
        this.fechaPublicacion = LocalDateTime.now();
    }

    private void actualizarContenido(int calificacion, String comentario) {
        if (calificacion < 1 || calificacion > 5) {
            throw new IllegalArgumentException("La calificación debe estar entre 1 y 5.");
        }
        this.calificacion = calificacion;
        this.comentario = validarTexto(comentario, "El comentario");
    }

    public void publicar() { this.estado = EstadoResena.PUBLICADA; }
    public void ocultar() { this.estado = EstadoResena.OCULTA; }
    public boolean estaVisible() { return estado == EstadoResena.PUBLICADA; }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " no puede estar vacío.");
        return valor.trim();
    }

    public String getCodigo() { return codigo; }
    public Cliente getCliente() { return cliente; }
    public Pelicula getPelicula() { return pelicula; }
    public int getCalificacion() { return calificacion; }
    public String getComentario() { return comentario; }
    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
    public EstadoResena getEstado() { return estado; }
}