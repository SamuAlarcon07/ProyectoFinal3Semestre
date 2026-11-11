package co.edu.uniquindio.proyectofinal.model.principal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Representa la empresa CinemaUQ y sus datos generales.
 * También mantiene las salas que pertenecen al establecimiento.
 *
 * @author Equipo CinemaUQ
 */
public class Cine {

    private final String nit;
    private String razonSocial;
    private String nombreComercial;
    private String direccion;
    private String telefono;
    private String correoElectronico;
    private String paginaWeb;
    private boolean activo;
    private final List<Sala> salas;

    /**
     * Construye la información principal del cine.
     */
    public Cine(String nit, String razonSocial, String nombreComercial,
                String direccion, String telefono,
                String correoElectronico, String paginaWeb) {
        this.nit = validarTexto(nit, "El NIT");
        this.razonSocial = validarTexto(razonSocial, "La razón social");
        this.nombreComercial = validarTexto(nombreComercial, "El nombre comercial");
        this.direccion = validarTexto(direccion, "La dirección");
        this.telefono = validarTexto(telefono, "El teléfono");
        this.correoElectronico = validarTexto(correoElectronico, "El correo electrónico");
        this.paginaWeb = paginaWeb == null ? "" : paginaWeb.trim();
        this.activo = true;
        this.salas = new ArrayList<>();
    }

    /** Actualiza los datos de contacto y presentación de la empresa. */
    public void actualizarInformacion(String razonSocial, String nombreComercial,
                                      String direccion, String telefono,
                                      String correoElectronico, String paginaWeb) {
        this.razonSocial = validarTexto(razonSocial, "La razón social");
        this.nombreComercial = validarTexto(nombreComercial, "El nombre comercial");
        this.direccion = validarTexto(direccion, "La dirección");
        this.telefono = validarTexto(telefono, "El teléfono");
        this.correoElectronico = validarTexto(correoElectronico, "El correo electrónico");
        this.paginaWeb = paginaWeb == null ? "" : paginaWeb.trim();
    }

    /** Registra una sala si todavía no existe otra con el mismo código. */
    public void registrarSala(Sala sala) {
        Objects.requireNonNull(sala, "La sala no puede ser nula.");
        boolean existe = salas.stream()
                .anyMatch(registrada -> registrada.getCodigo().equals(sala.getCodigo()));
        if (existe) {
            throw new IllegalArgumentException("Ya existe una sala con ese código.");
        }
        salas.add(sala);
    }

    /** Busca una sala por su código. */
    public Optional<Sala> buscarSala(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return salas.stream()
                .filter(sala -> sala.getCodigo().equals(codigo.trim()))
                .findFirst();
    }

    public void activar() { this.activo = true; }
    public void desactivar() { this.activo = false; }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    public String getNit() { return nit; }
    public String getRazonSocial() { return razonSocial; }
    public String getNombreComercial() { return nombreComercial; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getCorreoElectronico() { return correoElectronico; }
    public String getPaginaWeb() { return paginaWeb; }
    public boolean isActivo() { return activo; }
    public List<Sala> getSalas() { return List.copyOf(salas); }
}
