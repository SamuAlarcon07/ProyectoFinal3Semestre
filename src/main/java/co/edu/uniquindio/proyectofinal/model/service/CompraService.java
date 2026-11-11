package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.interfaces.CompraEventListener;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;
import co.edu.uniquindio.proyectofinal.model.principal.TarjetaVirtual;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.HashSet;
import java.util.Set;

/**
 * Coordina la confirmación de compras, pagos, inventario, puntos y eventos.
 * Las validaciones previas reducen el riesgo de dejar operaciones incompletas.
 */
public class CompraService {

    private final PagoService pagoService;
    private final InventarioService inventarioService;
    private final PuntosService puntosService;

    private final Map<String, Compra> compras = new LinkedHashMap<>();
    private final List<CompraEventListener> observadores = new ArrayList<>();
    private final Set<String> comprasConfirmadas = new HashSet<>();
    private final List<String> erroresNotificacion = new ArrayList<>();

    public CompraService(PagoService pagoService,
                         InventarioService inventarioService,
                         PuntosService puntosService) {
        this.pagoService = Objects.requireNonNull(pagoService,
                "El servicio de pago es obligatorio.");
        this.inventarioService = Objects.requireNonNull(inventarioService,
                "El servicio de inventario es obligatorio.");
        this.puntosService = Objects.requireNonNull(puntosService,
                "El servicio de puntos es obligatorio.");
    }

    /** Registra una compra pendiente. No permite reutilizar códigos. */
    public synchronized void registrarCompra(Compra compra) {
        Objects.requireNonNull(compra, "La compra es obligatoria.");

        if (compras.putIfAbsent(compra.getCodigo(), compra) != null) {
            throw new IllegalArgumentException(
                    "Ya existe una compra con ese código.");
        }
    }

    /**
     * Confirma una compra pendiente.
     *
     * El orden de validación evita cobrar cuando faltan artículos,
     * cuando el total no es válido o cuando la acumulación de puntos
     * ya fue procesada. Los bloqueos coordinan las operaciones realizadas
     * a través de estas instancias de servicios en memoria.
     */
    public synchronized Compra confirmarCompra(Compra compra,
                                               TarjetaVirtual tarjeta) {
        Objects.requireNonNull(compra, "La compra es obligatoria.");
        Objects.requireNonNull(tarjeta, "La tarjeta es obligatoria.");

        String codigoCompra = compra.getCodigo();

        if (comprasConfirmadas.contains(codigoCompra)) {
            throw new IllegalStateException("La compra ya fue confirmada.");
        }

        Compra compraRegistrada = compras.get(codigoCompra);
        if (compraRegistrada != null && compraRegistrada != compra) {
            throw new IllegalArgumentException(
                    "Ya existe otra instancia de compra con ese código.");
        }

        synchronized (compra) {
            if (!"PENDIENTE".equals(compra.consultarNombreEstado())) {
                throw new IllegalStateException(
                        "Solo se pueden confirmar compras en estado PENDIENTE.");
            }

            if (compra.getDetalles().isEmpty()) {
                throw new IllegalStateException(
                        "La compra no contiene elementos.");
            }

            BigDecimal total = compra.calcularTotal();
            if (total.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalStateException(
                        "El total de la compra debe ser mayor que cero.");
            }

            // Se mantienen estos bloqueos durante la validación y el uso de
            // inventario/puntos para evitar carreras entre operaciones del servicio.
            synchronized (inventarioService) {
                synchronized (puntosService) {
                    synchronized (tarjeta) {
                        if (inventarioService.fueDescontada(codigoCompra)) {
                            throw new IllegalStateException(
                                    "El inventario de esta compra ya fue descontado.");
                        }
                        if (!inventarioService.validarDisponibilidadCompra(compra)) {
                            throw new IllegalStateException(
                                    "No hay existencias suficientes para completar la compra.");
                        }

                        // Valida antes de cobrar que el acumulado no sea duplicado
                        // y que el cálculo de puntos sea representable.
                        puntosService.validarPuedeAcumularPorCompra(compra);

                        // El pago se intenta únicamente después de validar todo lo anterior.
                        pagoService.procesarPago(compra, tarjeta);

                        // La transición y los movimientos posteriores se ejecutan
                        // después de que la tarjeta confirma el cobro.
                        compra.confirmarPago();
                        inventarioService.registrarVentaCompra(compra);
                        puntosService.acumularPorCompra(compra);

                        compras.putIfAbsent(codigoCompra, compra);
                        comprasConfirmadas.add(codigoCompra);
                    }
                }
            }
        }

        // Los errores de un observador no deben hacer que el llamador crea
        // que el pago falló cuando la compra ya quedó confirmada.
        notificarObservadores(compra);
        return compra;
    }

    public synchronized void agregarObservador(CompraEventListener observador) {
        Objects.requireNonNull(observador, "El observador es obligatorio.");
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    public synchronized void eliminarObservador(CompraEventListener observador) {
        observadores.remove(observador);
    }

    public synchronized List<Compra> consultarCompras() {
        return List.copyOf(compras.values());
    }

    public synchronized List<Compra> consultarComprasCliente(String idCliente) {
        if (idCliente == null || idCliente.isBlank()) {
            throw new IllegalArgumentException("El identificador del cliente es obligatorio.");
        }
        return compras.values().stream()
                .filter(compra -> compra.getCliente().getId().equals(idCliente.trim()))
                .toList();
    }

    /** Devuelve los errores de observadores sin revertir compras confirmadas. */
    public synchronized List<String> consultarErroresNotificacion() {
        return List.copyOf(erroresNotificacion);
    }

    private void notificarObservadores(Compra compra) {
        List<CompraEventListener> copia;
        synchronized (this) {
            copia = List.copyOf(observadores);
        }

        for (CompraEventListener observador : copia) {
            try {
                observador.actualizar(compra);
            } catch (RuntimeException ex) {
                synchronized (this) {
                    erroresNotificacion.add(
                            observador.getClass().getSimpleName()
                                    + ": " + ex.getMessage());
                }
            }
        }
    }
}
