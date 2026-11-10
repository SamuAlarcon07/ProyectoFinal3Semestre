package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/** Define las capacidades de proyección y experiencia de una sala. */
public class TipoSala {

    private final String codigo;
    private String nombre;
    private String descripcion;
    private Set<FormatoProyeccion> formatosCompatibles;
    private boolean requiereGafas3D;
    private boolean tieneMovimiento;
    private boolean tieneEfectos;
    private boolean activo;

    public TipoSala(String codigo, String nombre, String descripcion,
                    Set<FormatoProyeccion> formatosCompatibles,
                    boolean requiereGafas3D, boolean tieneMovimiento,
                    boolean tieneEfectos) {
        this.codigo = validarTexto(codigo, "El código");
        this.nombre = validarTexto(nombre, "El nombre");
        this.descripcion = validarTexto(descripcion, "La descripción");
        actualizarFormatosCompatibles(formatosCompatibles);
        this.requiereGafas3D = requiereGafas3D;
        this.tieneMovimiento = tieneMovimiento;
        this.tieneEfectos = tieneEfectos;
        this.activo = true;
    }

    /** Determina si la sala admite el formato indicado. */
    public boolean permiteFormato(FormatoProyeccion formato) {
        return formato != null && activo && formatosCompatibles.contains(formato);
    }

    /** Actualiza los formatos admitidos por este tipo de sala. */
    public final void actualizarFormatosCompatibles(Set<FormatoProyeccion> formatos) {
        Objects.requireNonNull(formatos, "Los formatos no pueden ser nulos.");
        if (formatos.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos un formato compatible.");
        }
        this.formatosCompatibles = EnumSet.copyOf(formatos);
    }

    public void activar() { activo = true; }
    public void desactivar() { activo = false; }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public Set<FormatoProyeccion> getFormatosCompatibles() {
        return Collections.unmodifiableSet(EnumSet.copyOf(formatosCompatibles));
    }
    public boolean isRequiereGafas3D() { return requiereGafas3D; }
    public boolean isTieneMovimiento() { return tieneMovimiento; }
    public boolean isTieneEfectos() { return tieneEfectos; }
    public boolean isActivo() { return activo; }
}
