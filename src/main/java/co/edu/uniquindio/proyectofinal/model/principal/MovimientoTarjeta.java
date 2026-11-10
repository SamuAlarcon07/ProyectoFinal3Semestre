package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoTarjeta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/** Registra una recarga, pago o reembolso realizado sobre una tarjeta virtual. */
public class MovimientoTarjeta {

    private final String codigo;
    private final TipoMovimientoTarjeta tipo;
    private final BigDecimal valor;
    private final LocalDateTime fecha;
    private final BigDecimal saldoResultante;
    private final String referenciaOperacion;
    private final String responsableId;

    public MovimientoTarjeta(String codigo, TipoMovimientoTarjeta tipo,
                             BigDecimal valor, LocalDateTime fecha,
                             BigDecimal saldoResultante,
                             String referenciaOperacion, String responsableId) {
        if (codigo == null || codigo.isBlank()) throw new IllegalArgumentException("El código es obligatorio.");
        this.codigo = codigo.trim();
        this.tipo = Objects.requireNonNull(tipo, "El tipo de movimiento es obligatorio.");
        validarMonto(valor, "El valor");
        validarMonto(saldoResultante, "El saldo resultante", true);
        this.valor = valor;
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria.");
        this.saldoResultante = saldoResultante;
        this.referenciaOperacion = referenciaOperacion == null ? "" : referenciaOperacion.trim();
        this.responsableId = responsableId == null ? "" : responsableId.trim();
    }

    private static void validarMonto(BigDecimal valor, String campo) { validarMonto(valor, campo, false); }
    private static void validarMonto(BigDecimal valor, String campo, boolean permiteCero) {
        if (valor == null || (permiteCero ? valor.compareTo(BigDecimal.ZERO) < 0 : valor.compareTo(BigDecimal.ZERO) <= 0))
            throw new IllegalArgumentException(campo + " no es válido.");
    }

    /** Devuelve el importe con signo según el tipo de movimiento. */
    public BigDecimal consultarValorConSigno() {
        return tipo == TipoMovimientoTarjeta.PAGO ? valor.negate() : valor;
    }

    public String getCodigo() { return codigo; }
    public TipoMovimientoTarjeta getTipo() { return tipo; }
    public BigDecimal getValor() { return valor; }
    public LocalDateTime getFecha() { return fecha; }
    public BigDecimal getSaldoResultante() { return saldoResultante; }
    public String getReferenciaOperacion() { return referenciaOperacion; }
    public String getResponsableId() { return responsableId; }
}