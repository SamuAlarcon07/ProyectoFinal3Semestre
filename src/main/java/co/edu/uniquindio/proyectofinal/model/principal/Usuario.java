package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoUsuario;

/**
 * Clase base abstracta para los usuarios de CinemaUQ.
 * Centraliza los datos comunes de clientes y administradores.
 *
 * @author Equipo CinemaUQ
 */
public abstract class Usuario {

    private final String id;
    private String nombre;
    private String correo;
    private String contrasenaHash;
    private EstadoUsuario estado;

    /** Constructor protegido para las clases que heredan de Usuario. */
    protected Usuario(String id, String nombre, String correo,
                      String contrasenaHash) {
        this.id = validarTexto(id, "La identificación");
        this.nombre = validarTexto(nombre, "El nombre");
        this.correo = validarCorreo(correo);
        this.contrasenaHash = validarTexto(contrasenaHash, "El hash de contraseña");
        this.estado = EstadoUsuario.ACTIVO;
    }

    /** Actualiza el nombre y el correo del usuario. */
    public void actualizarDatos(String nombre, String correo) {
        this.nombre = validarTexto(nombre, "El nombre");
        this.correo = validarCorreo(correo);
    }

    /** Actualiza el hash de contraseña generado por el servicio de autenticación. */
    public void actualizarContrasenaHash(String nuevoHash) {
        this.contrasenaHash = validarTexto(nuevoHash, "El hash de contraseña");
    }

    /** Comprueba si un hash calculado por el servicio coincide con el almacenado. */
    public boolean coincideContrasenaHash(String hashPropuesto) {
        return hashPropuesto != null && contrasenaHash.equals(hashPropuesto);
    }

    public void activar() { estado = EstadoUsuario.ACTIVO; }
    public void desactivar() { estado = EstadoUsuario.INACTIVO; }
    public void bloquear() { estado = EstadoUsuario.BLOQUEADO; }
    public boolean puedeIngresar() { return estado == EstadoUsuario.ACTIVO; }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    private static String validarCorreo(String correo) {
        String valor = validarTexto(correo, "El correo");
        if (!valor.contains("@") || valor.startsWith("@") || valor.endsWith("@")) {
            throw new IllegalArgumentException("El correo electrónico no tiene un formato válido.");
        }
        return valor;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public EstadoUsuario getEstado() { return estado; }
}
