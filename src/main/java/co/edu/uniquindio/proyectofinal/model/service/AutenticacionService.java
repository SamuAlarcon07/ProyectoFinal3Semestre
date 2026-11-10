package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.principal.Administrador;

import co.edu.uniquindio.proyectofinal.model.principal.Cliente;

import co.edu.uniquindio.proyectofinal.model.principal.Usuario;

import co.edu.uniquindio.proyectofinal.model.util.PasswordHasher;



import java.util.Collection;

import java.util.LinkedHashMap;

import java.util.Locale;

import java.util.Map;

import java.util.Objects;

import java.util.Optional;



/** Gestiona el registro de usuarios y la autenticación de CinemaUQ. */

public class AutenticacionService {



    private final Map<String, Usuario> usuariosPorId = new LinkedHashMap<>();

    private final PasswordHasher passwordHasher;



    public AutenticacionService() {

        this(new PasswordHasher());

    }



    public AutenticacionService(PasswordHasher passwordHasher) {

        this.passwordHasher = Objects.requireNonNull(passwordHasher);

    }



    public Cliente registrarCliente(String id, String nombre, String correo,

                                    String contrasena) {

        validarCredenciales(correo, contrasena);

        Cliente cliente = new Cliente(id, nombre, correo,

                passwordHasher.hash(contrasena));

        registrarUsuario(cliente);

        return cliente;

    }



    /** El registro de administradores debe invocarse desde un flujo autorizado. */

    public Administrador registrarAdministrador(String id, String nombre,

                                                String correo, String contrasena, String codigoEmpleado) {

        validarCredenciales(correo, contrasena);

        Administrador administrador = new Administrador(id, nombre, correo,

                passwordHasher.hash(contrasena), codigoEmpleado);

        registrarUsuario(administrador);

        return administrador;

    }



    public Optional<Usuario> iniciarSesion(String correo, String contrasena) {

        if (correo == null || contrasena == null) return Optional.empty();

        String correoNormalizado = correo.trim().toLowerCase(Locale.ROOT);

        return usuariosPorId.values().stream()

                .filter(usuario -> usuario.getCorreo().toLowerCase(Locale.ROOT)

                        .equals(correoNormalizado))

                .filter(Usuario::puedeIngresar)

                .filter(usuario -> usuario.verificarContrasena(contrasena, passwordHasher))

                .findFirst();

    }



    public Optional<Usuario> buscarUsuario(String id) {

        return Optional.ofNullable(usuariosPorId.get(id));

    }



    public Collection<Usuario> consultarUsuarios() {

        return java.util.List.copyOf(usuariosPorId.values());

    }



    public void bloquearUsuario(String id) {

        obtenerUsuario(id).bloquear();

    }



    public void desactivarUsuario(String id) {

        obtenerUsuario(id).desactivar();

    }



    public void activarUsuario(String id) {

        obtenerUsuario(id).activar();

    }



    private void registrarUsuario(Usuario usuario) {

        Objects.requireNonNull(usuario, "El usuario es obligatorio.");

        boolean idDuplicado = usuariosPorId.containsKey(usuario.getId());

        boolean correoDuplicado = usuariosPorId.values().stream()

                .anyMatch(actual -> actual.getCorreo().equalsIgnoreCase(usuario.getCorreo()));

        if (idDuplicado || correoDuplicado) {

            throw new IllegalArgumentException("Ya existe un usuario con esa identificación o correo.");

        }

        usuariosPorId.put(usuario.getId(), usuario);

    }



    private Usuario obtenerUsuario(String id) {

        Usuario usuario = usuariosPorId.get(id);

        if (usuario == null) throw new IllegalArgumentException("No existe el usuario indicado.");

        return usuario;

    }



    private void validarCredenciales(String correo, String contrasena) {

        if (correo == null || correo.isBlank()) throw new IllegalArgumentException("El correo es obligatorio.");

        if (contrasena == null || contrasena.length() < 8) {

            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");

        }

    }

}