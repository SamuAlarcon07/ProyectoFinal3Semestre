package co.edu.uniquindio.proyectofinal.model.principal;



import co.edu.uniquindio.proyectofinal.model.enums.EstadoUsuario;

import co.edu.uniquindio.proyectofinal.model.util.PasswordHasher;



/** Clase base abstracta para los usuarios de CinemaUQ. */

public abstract class Usuario {



    private final String id;

    private String nombre;

    private String correo;

    private String contrasenaHash;

    private EstadoUsuario estado;



    protected Usuario(String id, String nombre, String correo, String contrasenaHash) {

        this.id = validarTexto(id, "La identificación");

        this.nombre = validarTexto(nombre, "El nombre");

        this.correo = validarCorreo(correo);

        this.contrasenaHash = validarTexto(contrasenaHash, "El hash de contraseña");

        this.estado = EstadoUsuario.ACTIVO;

    }



    public void actualizarDatos(String nombre, String correo) {

        this.nombre = validarTexto(nombre, "El nombre");

        this.correo = validarCorreo(correo);

    }



    public void actualizarContrasenaHash(String nuevoHash) {

        this.contrasenaHash = validarTexto(nuevoHash, "El hash de contraseña");

    }



    /** Compara un hash ya calculado con el almacenado; se conserva por compatibilidad. */

    public boolean coincideContrasenaHash(String hashPropuesto) {

        return hashPropuesto != null && contrasenaHash.equals(hashPropuesto);

    }



    /** Verifica una contraseña en texto plano sin exponer el hash almacenado. */

    public boolean verificarContrasena(String contrasena, PasswordHasher hasher) {

        return hasher != null && hasher.verificar(contrasena, contrasenaHash);

    }



    public void activar() { estado = EstadoUsuario.ACTIVO; }

    public void desactivar() { estado = EstadoUsuario.INACTIVO; }

    public void bloquear() { estado = EstadoUsuario.BLOQUEADO; }

    public boolean puedeIngresar() { return estado == EstadoUsuario.ACTIVO; }



    private static String validarTexto(String valor, String campo) {

        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " no puede estar vacío.");

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

