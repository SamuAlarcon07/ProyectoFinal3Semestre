package co.edu.uniquindio.proyectofinal.model.principal;

import java.time.LocalDate;
import java.util.Objects;

/** Representa a un cliente registrado en CinemaUQ. */
public class Cliente extends Usuario {

    private final LocalDate fechaRegistro;

    /** Crea un cliente usando la fecha actual como fecha de registro. */
    public Cliente(String id, String nombre, String correo, String contrasenaHash) {
        this(id, nombre, correo, contrasenaHash, LocalDate.now());
    }

    /** Crea un cliente con una fecha de registro específica. */
    public Cliente(String id, String nombre, String correo,
                   String contrasenaHash, LocalDate fechaRegistro) {
        super(id, nombre, correo, contrasenaHash);
        this.fechaRegistro = Objects.requireNonNull(fechaRegistro,
                "La fecha de registro no puede ser nula.");
        if (fechaRegistro.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La fecha de registro no puede ser futura.");
        }
    }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
}
