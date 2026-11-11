package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.TipoMovimientoPuntos;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Administra los lotes y movimientos de puntos de un cliente. */
public class CuentaPuntos {

    private final Cliente titular;
    private final List<LotePuntos> lotes;
    private final List<MovimientoPuntos> movimientos;

    public CuentaPuntos(Cliente titular) {
        this.titular = Objects.requireNonNull(titular, "El titular de la cuenta es obligatorio.");
        this.lotes = new ArrayList<>();
        this.movimientos = new ArrayList<>();
    }

    /** Acumula puntos y crea un lote cuya vigencia es de un año. */
    public LotePuntos acumularPuntos(String codigoLote, int cantidad,
                                     LocalDate fechaObtencion, String referencia) {
        Objects.requireNonNull(fechaObtencion, "La fecha de obtención es obligatoria.");
        if (fechaObtencion.isAfter(LocalDate.now())) throw new IllegalArgumentException("La fecha de obtención no puede ser futura.");
        LotePuntos lote = new LotePuntos(codigoLote, cantidad, fechaObtencion, fechaObtencion.plusYears(1));
        lotes.add(lote);
        registrarMovimiento(TipoMovimientoPuntos.ACUMULACION, cantidad, "Puntos acumulados", referencia, fechaObtencion);
        return lote;
    }

    /** Consulta el saldo de puntos que todavía no han vencido. */
    public int consultarSaldo(LocalDate fechaConsulta) {
        Objects.requireNonNull(fechaConsulta, "La fecha de consulta es obligatoria.");
        return lotes.stream().filter(lote -> !lote.estaVencido(fechaConsulta))
                .mapToInt(LotePuntos::getCantidadDisponible).sum();
    }

    /** Redime puntos consumiendo primero los lotes que vencen más pronto. */
    public void redimirPuntos(int cantidad, LocalDate fecha, String referencia) {
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        Objects.requireNonNull(fecha, "La fecha de redención es obligatoria.");
        procesarVencimientos(fecha);
        if (consultarSaldo(fecha) < cantidad) throw new IllegalStateException("El cliente no tiene puntos suficientes.");
        int restante = cantidad;
        List<LotePuntos> ordenados = lotes.stream()
                .filter(lote -> !lote.estaVencido(fecha) && lote.getCantidadDisponible() > 0)
                .sorted(Comparator.comparing(LotePuntos::getFechaVencimiento))
                .toList();
        for (LotePuntos lote : ordenados) {
            int usar = Math.min(restante, lote.getCantidadDisponible());
            lote.utilizarPuntos(usar, fecha);
            restante -= usar;
            if (restante == 0) break;
        }
        registrarMovimiento(TipoMovimientoPuntos.REDENCION, cantidad, "Puntos redimidos", referencia, fecha);
    }

    /** Procesa el vencimiento de los lotes cuya fecha de vencimiento ya pasó. */
    public int procesarVencimientos(LocalDate fecha) {
        Objects.requireNonNull(fecha, "La fecha de consulta es obligatoria.");
        int totalVencido = 0;
        for (LotePuntos lote : lotes) {
            int vencidos = lote.procesarVencimiento(fecha);
            if (vencidos > 0) {
                totalVencido += vencidos;
                registrarMovimiento(TipoMovimientoPuntos.VENCIMIENTO, vencidos,
                        "Puntos vencidos del lote " + lote.getCodigo(), lote.getCodigo(), fecha);
            }
        }
        return totalVencido;
    }

    private void registrarMovimiento(TipoMovimientoPuntos tipo, int cantidad,
                                     String descripcion, String referencia, LocalDate fecha) {
        movimientos.add(new MovimientoPuntos(UUID.randomUUID().toString(), tipo,
                cantidad, fecha.atStartOfDay(), descripcion, referencia));
    }

    public Cliente getTitular() { return titular; }
    public List<LotePuntos> getLotes() { return List.copyOf(lotes); }
    public List<MovimientoPuntos> getMovimientos() { return List.copyOf(movimientos); }
}