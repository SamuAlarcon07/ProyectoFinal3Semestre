package co.edu.uniquindio.proyectofinal.model.principal;

/** Representa a un administrador de CinemaUQ. */
public class Administrador extends Usuario {

    private final String codigoEmpleado;

    public Administrador(String id, String nombre, String correo,
                         String contrasenaHash, String codigoEmpleado) {
        super(id, nombre, correo, contrasenaHash);
        if (codigoEmpleado == null || codigoEmpleado.isBlank()) {
            throw new IllegalArgumentException("El código de empleado es obligatorio.");
        }
        this.codigoEmpleado = codigoEmpleado.trim();
    }

    public String getCodigoEmpleado() { return codigoEmpleado; }
}
