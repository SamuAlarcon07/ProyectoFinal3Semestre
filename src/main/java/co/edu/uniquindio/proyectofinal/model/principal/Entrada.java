package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Clase base abstracta para los tipos de entrada de CinemaUQ. */
public abstract class Entrada implements ElementoVendible {

    private final String codigo;
    private final String codigoQR;
    private final Funcion funcion;
    private final Asiento asiento;
    private final BigDecimal precio;
    private final LocalDateTime fechaEmision;
    private EstadoEntrada estado;

    protected Entrada(String codigo, String codigoQR, Funcion funcion,
                      Asiento asiento, BigDecimal precio,
                      LocalDateTime fechaEmision) {
        this.codigo = validarTexto(codigo, "El código de entrada");
        this.codigoQR = validarTexto(codigoQR, "El código QR");
        this.funcion = Objects.requireNonNull(funcion, "La función es obligatoria.");
        this.asiento = Objects.requireNonNull(asiento, "El asiento es obligatorio.");
        if (!funcion.getSala().contieneAsiento(asiento)) {
            throw new IllegalArgumentException("El asiento no pertenece a la sala de la función.");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio de la entrada debe ser mayor que cero.");
        }
        this.precio = precio;
        this.fechaEmision = Objects.requireNonNull(fechaEmision,
                "La fecha de emisión es obligatoria.");
        this.estado = EstadoEntrada.EMITIDA;
    }

    @Override
    public final BigDecimal consultarPrecio() { return precio; }

    /** Marca la entrada como utilizada después de validar el acceso. */
    public void marcarComoUtilizada() {
        if (estado != EstadoEntrada.EMITIDA) {
            throw new IllegalStateException("Solo una entrada emitida puede utilizarse.");
        }
        estado = EstadoEntrada.UTILIZADA;
    }

    public void anular() {
        if (estado == EstadoEntrada.UTILIZADA) {
            throw new IllegalStateException("Una entrada utilizada no puede anularse.");
        }
        estado = EstadoEntrada.ANULADA;
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " no puede estar vacío.");
        }
        return valor.trim();
    }

    public String getCodigo() { return codigo; }
    public String getCodigoQR() { return codigoQR; }
    public Funcion getFuncion() { return funcion; }
    public Asiento getAsiento() { return asiento; }
    public LocalDateTime getFechaEmision() { return fechaEmision; }
    public EstadoEntrada getEstado() { return estado; }
}
