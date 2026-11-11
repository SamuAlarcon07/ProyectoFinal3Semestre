package co.edu.uniquindio.proyectofinal.model.principal;

import java.time.LocalDate;
import java.util.Objects;

/** Conserva una acumulación de puntos con su propia fecha de vencimiento. */
public class LotePuntos {

    private final String codigo;
    private final int cantidadOriginal;
    private int cantidadDisponible;
    private final LocalDate fechaObtencion;
    private final LocalDate fechaVencimiento;

    public LotePuntos(String codigo, int cantidad, LocalDate fechaObtencion,
                      LocalDate fechaVencimiento) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad de puntos debe ser positiva.");
        this.codigo = codigo.trim();
        this.cantidadOriginal = cantidad;
        this.cantidadDisponible = cantidad;
        this.fechaObtencion = Objects.requireNonNull(fechaObtencion, "La fecha de obtención es obligatoria.");
        this.fechaVencimiento = Objects.requireNonNull(fechaVencimiento, "La fecha de vencimiento es obligatoria.");
        if (!fechaVencimiento.isAfter(fechaObtencion)) throw new IllegalArgumentException("El vencimiento debe ser posterior a la obtención.");
    }

    public boolean estaVencido(LocalDate fechaConsulta) {
        return fechaConsulta != null && fechaConsulta.isAfter(fechaVencimiento);
    }

    /** Descuenta puntos disponibles de este lote. */
    public void utilizarPuntos(int cantidad, LocalDate fechaConsulta) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        if (estaVencido(fechaConsulta)) throw new IllegalStateException("El lote de puntos está vencido.");
        if (cantidad > cantidadDisponible) throw new IllegalArgumentException("El lote no tiene suficientes puntos.");
        cantidadDisponible -= cantidad;
    }

    /** Vence los puntos restantes cuando ya pasó la fecha de vencimiento. */
    public int procesarVencimiento(LocalDate fechaConsulta) {
        if (!estaVencido(fechaConsulta)) return 0;
        int vencidos = cantidadDisponible;
        cantidadDisponible = 0;
        return vencidos;
    }

    public String getCodigo() { return codigo; }
    public int getCantidadOriginal() { return cantidadOriginal; }
    public int getCantidadDisponible() { return cantidadDisponible; }
    public LocalDate getFechaObtencion() { return fechaObtencion; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
}