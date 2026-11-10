package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoPelicula;

/** Representa una película del catálogo de CinemaUQ. */
public class Pelicula {

    private final String codigo;
    private String titulo;
    private String sinopsis;
    private int duracionMinutos;
    private String clasificacion;
    private String genero;
    private EstadoPelicula estado;

    public Pelicula(String codigo, String titulo, String sinopsis,
                    int duracionMinutos, String clasificacion, String genero) {
        this.codigo = validarTexto(codigo, "El código");
        actualizarInformacion(titulo, sinopsis, duracionMinutos, clasificacion, genero);
        this.estado = EstadoPelicula.ACTIVA;
    }

    /** Actualiza los datos descriptivos de la película. */
    public final void actualizarInformacion(String titulo, String sinopsis,
                                            int duracionMinutos,
                                            String clasificacion, String genero) {
        this.titulo = validarTexto(titulo, "El título");
        this.sinopsis = validarTexto(sinopsis, "La sinopsis");
        if (duracionMinutos <= 0) {
            throw new IllegalArgumentException("La duración debe ser mayor que cero.");
        }
        this.duracionMinutos = duracionMinutos;
        this.clasificacion = validarTexto(clasificacion, "La clasificación");
        this.genero = validarTexto(genero, "El género");
    }

    public void activar() { estado = EstadoPelicula.ACTIVA; }
    public void desactivar() { estado = EstadoPelicula.INACTIVA; }
    public boolean estaActiva() { return estado == EstadoPelicula.ACTIVA; }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    public String getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public String getSinopsis() { return sinopsis; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public String getClasificacion() { return clasificacion; }
    public String getGenero() { return genero; }
    public EstadoPelicula getEstado() { return estado; }
}
