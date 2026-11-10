package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoTarjeta;
import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoTarjeta;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Representa la tarjeta virtual asociada a un cliente de CinemaUQ. */
public class TarjetaVirtual {

    private final String numero;
    private final Cliente titular;
    private BigDecimal saldo;
    private EstadoTarjeta estado;
    private final List<MovimientoTarjeta> movimientos;

    public TarjetaVirtual(String numero, Cliente titular) {
        if (numero == null || numero.isBlank()) throw new IllegalArgumentException("El número de tarjeta es obligatorio.");
        this.numero = numero.trim();
        this.titular = Objects.requireNonNull(titular, "El titular es obligatorio.");
        this.saldo = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        this.estado = EstadoTarjeta.ACTIVA;
        this.movimientos = new ArrayList<>();
    }

    /** Recarga la tarjeta. La operación debe ser solicitada por un administrador. */
    public void recargar(BigDecimal valor, Administrador administrador) {
        Objects.requireNonNull(administrador, "El administrador responsable es obligatorio.");
        verificarMonto(valor);
        if (estado != EstadoTarjeta.ACTIVA) throw new IllegalStateException("No se puede recargar una tarjeta bloqueada.");
        registrarMovimiento(TipoMovimientoTarjeta.RECARGA, valor, "Recarga autorizada", administrador.getId());
    }

    /** Descuenta el importe de una compra si la tarjeta está activa y tiene saldo suficiente. */
    public void pagar(BigDecimal valor, String codigoCompra) {
        verificarMonto(valor);
        if (estado != EstadoTarjeta.ACTIVA) throw new IllegalStateException("La tarjeta está bloqueada.");
        if (saldo.compareTo(valor) < 0) throw new IllegalStateException("La tarjeta no tiene saldo suficiente.");
        saldo = saldo.subtract(valor).setScale(2, RoundingMode.HALF_UP);
        agregarMovimiento(TipoMovimientoTarjeta.PAGO, valor, codigoCompra, "");
    }

    /** Acredita un reembolso incluso si la tarjeta está bloqueada. */
    public void reembolsar(BigDecimal valor, String codigoReembolso) {
        verificarMonto(valor);
        saldo = saldo.add(valor).setScale(2, RoundingMode.HALF_UP);
        agregarMovimiento(TipoMovimientoTarjeta.REEMBOLSO, valor, codigoReembolso, "");
    }

    private void registrarMovimiento(TipoMovimientoTarjeta tipo, BigDecimal valor,
                                     String referencia, String responsableId) {
        saldo = saldo.add(valor).setScale(2, RoundingMode.HALF_UP);
        agregarMovimiento(tipo, valor, referencia, responsableId);
    }

    private void agregarMovimiento(TipoMovimientoTarjeta tipo, BigDecimal valor,
                                   String referencia, String responsableId) {
        movimientos.add(new MovimientoTarjeta(UUID.randomUUID().toString(), tipo, valor,
                LocalDateTime.now(), saldo, referencia, responsableId));
    }

    public void bloquear() { estado = EstadoTarjeta.BLOQUEADA; }
    public void activar() { estado = EstadoTarjeta.ACTIVA; }

    private static void verificarMonto(BigDecimal valor) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("El valor debe ser mayor que cero.");
    }

    public String getNumero() { return numero; }
    public Cliente getTitular() { return titular; }
    public BigDecimal getSaldo() { return saldo; }
    public EstadoTarjeta getEstado() { return estado; }
    public List<MovimientoTarjeta> getMovimientos() { return List.copyOf(movimientos); }
}