package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoPromocion;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/** Representa una promoción comercial que puede aplicarse a una compra. */
public class Promocion {

    private final String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal porcentajeDescuento;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private EstadoPromocion estado;

    public Promocion(String codigo, String nombre, String descripcion,
                     BigDecimal porcentajeDescuento,
                     LocalDate fechaInicio, LocalDate fechaFin) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        this.codigo = codigo.trim();
        actualizarInformacion(nombre, descripcion, porcentajeDescuento, fechaInicio, fechaFin);
        LocalDate hoy = LocalDate.now();
        if (hoy.isBefore(fechaInicio)) {
            this.estado = EstadoPromocion.PROGRAMADA;
        } else if (hoy.isAfter(fechaFin)) {
            this.estado = EstadoPromocion.FINALIZADA;
        } else {
            this.estado = EstadoPromocion.ACTIVA;
        }
    }

    public void actualizarInformacion(String nombre, String descripcion,
                                      BigDecimal porcentajeDescuento,
                                      LocalDate fechaInicio, LocalDate fechaFin) {
        if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("El nombre es obligatorio.");
        if (descripcion == null || descripcion.isBlank()) throw new IllegalArgumentException("La descripción es obligatoria.");
        if (porcentajeDescuento == null || porcentajeDescuento.compareTo(BigDecimal.ZERO) <= 0
                || porcentajeDescuento.compareTo(new BigDecimal("100")) > 0) throw new IllegalArgumentException("El descuento debe estar entre 0 y 100.");
        Objects.requireNonNull(fechaInicio, "La fecha inicial es obligatoria.");
        Objects.requireNonNull(fechaFin, "La fecha final es obligatoria.");
        if (fechaFin.isBefore(fechaInicio)) throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial.");
        this.nombre = nombre.trim();
        this.descripcion = descripcion.trim();
        this.porcentajeDescuento = porcentajeDescuento;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public boolean estaVigente(LocalDate fecha) {
        return estado == EstadoPromocion.ACTIVA && fecha != null
                && !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }

    public BigDecimal calcularDescuento(BigDecimal subtotal, LocalDate fecha) {
        if (subtotal == null || subtotal.compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("El subtotal no es válido.");
        if (!estaVigente(fecha)) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return subtotal.multiply(porcentajeDescuento).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public void activar() { estado = EstadoPromocion.ACTIVA; }
    public void desactivar() { estado = EstadoPromocion.INACTIVA; }
    public void finalizar() { estado = EstadoPromocion.FINALIZADA; }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPorcentajeDescuento() { return porcentajeDescuento; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public EstadoPromocion getEstado() { return estado; }
}