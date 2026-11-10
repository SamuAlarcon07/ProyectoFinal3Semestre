package co.edu.uniquindio.proyectofinal.model.principal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;

/** Representa una sala física perteneciente a CinemaUQ. */
public class Sala {

    private final String codigo;
    private String nombre;
    private TipoSala tipoSala;
    private final List<Asiento> asientos;

    public Sala(String codigo, String nombre, TipoSala tipoSala) {
        this.codigo = validarTexto(codigo, "El código");
        this.nombre = validarTexto(nombre, "El nombre");
        this.tipoSala = Objects.requireNonNull(tipoSala, "El tipo de sala es obligatorio.");
        if (!tipoSala.isActivo()) {
            throw new IllegalArgumentException("No se puede asignar un tipo de sala inactivo.");
        }
        this.asientos = new ArrayList<>();
    }

    /** Registra un asiento sin duplicar la fila y el número dentro de la sala. */
    public void agregarAsiento(Asiento asiento) {
        Objects.requireNonNull(asiento, "El asiento no puede ser nulo.");
        if (contieneAsiento(asiento)) {
            throw new IllegalArgumentException("Ya existe ese asiento en la sala.");
        }
        asientos.add(asiento);
    }

    public boolean contieneAsiento(Asiento asiento) {
        if (asiento == null) return false;
        return asientos.stream().anyMatch(actual ->
                actual.getFila().equalsIgnoreCase(asiento.getFila())
                        && actual.getNumero() == asiento.getNumero());
    }

    /** Calcula la capacidad contando solamente los asientos activos. */
    public int calcularCapacidad() {
        return (int) asientos.stream().filter(Asiento::isActivo).count();
    }

    public boolean permiteFormato(FormatoProyeccion formato) {
        return tipoSala.permiteFormato(formato);
    }

    public void cambiarTipoSala(TipoSala nuevoTipo) {
        Objects.requireNonNull(nuevoTipo, "El tipo de sala no puede ser nulo.");
        if (!nuevoTipo.isActivo()) {
            throw new IllegalArgumentException("El tipo de sala debe estar activo.");
        }
        this.tipoSala = nuevoTipo;
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public TipoSala getTipoSala() { return tipoSala; }
    public List<Asiento> getAsientos() { return List.copyOf(asientos); }
}
