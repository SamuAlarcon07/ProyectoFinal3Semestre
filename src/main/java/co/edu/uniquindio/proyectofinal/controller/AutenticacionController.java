package co.edu.uniquindio.proyectofinal.controller;

import co.edu.uniquindio.proyectofinal.model.principal.Cliente;
import co.edu.uniquindio.proyectofinal.model.principal.TarjetaVirtual;
import co.edu.uniquindio.proyectofinal.model.principal.Usuario;
import co.edu.uniquindio.proyectofinal.model.service.AutenticacionService;
import co.edu.uniquindio.proyectofinal.model.service.TarjetaVirtualService;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Controlador de aplicación para los casos de uso de autenticación.
 * No contiene referencias a componentes JavaFX.
 */
public class AutenticacionController {

    private final AutenticacionService autenticacionService;
    private final TarjetaVirtualService tarjetaVirtualService;

    public AutenticacionController(AutenticacionService autenticacionService) {
        this(autenticacionService, null);
    }

    /** Constructor que permite emitir la tarjeta virtual al registrar un cliente. */
    public AutenticacionController(AutenticacionService autenticacionService,
                                   TarjetaVirtualService tarjetaVirtualService) {
        this.autenticacionService = Objects.requireNonNull(
                autenticacionService,
                "El servicio de autenticación es obligatorio."
        );
        this.tarjetaVirtualService = tarjetaVirtualService;
    }

    public Optional<Usuario> iniciarSesion(String correo, String contrasena) {
        return autenticacionService.iniciarSesion(correo, contrasena);
    }

    public Cliente registrarCliente(String identificacion,
                                    String nombre,
                                    String correo,
                                    String contrasena) {
        Cliente cliente = autenticacionService.registrarCliente(
                identificacion, nombre, correo, contrasena
        );

        // El enunciado establece que cada cliente cuenta con una tarjeta virtual.
        // Cuando el controlador recibe el servicio, la tarjeta se emite al registrarlo.
        if (tarjetaVirtualService != null) {
            String numeroTarjeta = "CU-" + UUID.randomUUID()
                    .toString().replace("-", "").substring(0, 12).toUpperCase();
            tarjetaVirtualService.registrarTarjeta(
                    new TarjetaVirtual(numeroTarjeta, cliente)
            );
        }

        return cliente;
    }
}
