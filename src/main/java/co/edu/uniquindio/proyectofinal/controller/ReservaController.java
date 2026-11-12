package co.edu.uniquindio.proyectofinal.controller;

import co.edu.uniquindio.proyectofinal.model.enums.TipoAsiento;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state.CompraPendiente;
import co.edu.uniquindio.proyectofinal.model.patrones.creacionales.builder.CompraBuilder;
import co.edu.uniquindio.proyectofinal.model.patrones.creacionales.factory.FactoryEntrada;
import co.edu.uniquindio.proyectofinal.model.patrones.creacionales.factory.FactoryEntradaGeneral;
import co.edu.uniquindio.proyectofinal.model.patrones.creacionales.factory.FactoryEntradaPreferencial;
import co.edu.uniquindio.proyectofinal.model.patrones.estructurales.facade.CheckoutFacade;
import co.edu.uniquindio.proyectofinal.model.principal.*;
import co.edu.uniquindio.proyectofinal.model.service.PromocionService;
import co.edu.uniquindio.proyectofinal.model.service.PuntosService;
import co.edu.uniquindio.proyectofinal.model.service.ReservaService;
import co.edu.uniquindio.proyectofinal.model.service.TarjetaVirtualService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador de aplicación para las reservas temporales y la compra
 * de entradas seleccionadas desde la cartelera.
 */
public class ReservaController {

    private final ReservaService reservaService;
    private final CheckoutFacade checkoutFacade;
    private final TarjetaVirtualService tarjetaVirtualService;
    private final PuntosService puntosService;
    private final PromocionService promocionService;

    public ReservaController(
            ReservaService reservaService,
            CheckoutFacade checkoutFacade,
            TarjetaVirtualService tarjetaVirtualService
    ) {
        this(reservaService, checkoutFacade, tarjetaVirtualService, null, null);
    }

    public ReservaController(
            ReservaService reservaService,
            CheckoutFacade checkoutFacade,
            TarjetaVirtualService tarjetaVirtualService,
            PuntosService puntosService,
            PromocionService promocionService
    ) {
        this.reservaService = Objects.requireNonNull(
                reservaService, "El servicio de reservas es obligatorio.");
        this.checkoutFacade = Objects.requireNonNull(
                checkoutFacade, "La fachada de compra es obligatoria.");
        this.tarjetaVirtualService = Objects.requireNonNull(
                tarjetaVirtualService, "El servicio de tarjeta es obligatorio.");
        this.puntosService = puntosService;
        this.promocionService = promocionService;
    }

    /** Crea una reserva temporal para uno o varios asientos. */
    public List<ReservaAsiento> reservarAsientos(
            Funcion funcion,
            Collection<Asiento> asientos,
            Cliente cliente
    ) {
        return reservaService.reservarAsientos(
                funcion,
                asientos,
                cliente,
                LocalDateTime.now()
        );
    }

    /**
     * Construye las entradas y la compra pendiente, cobra la tarjeta virtual
     * y convierte las reservas temporales en entradas ocupadas.
     */
    public Compra confirmarCompra(
            Collection<ReservaAsiento> reservas,
            Cliente cliente
    ) {
        return confirmarCompra(reservas, cliente, "", 0);
    }

    /** Confirma entradas aplicando, de forma opcional, promoción y redención de puntos. */
    public Compra confirmarCompra(
            Collection<ReservaAsiento> reservas,
            Cliente cliente,
            String codigoPromocion,
            int puntosARedimir
    ) {
        Objects.requireNonNull(reservas, "Las reservas son obligatorias.");
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");

        LocalDateTime ahora = LocalDateTime.now();
        List<ReservaAsiento> reservasCopia = List.copyOf(reservas);
        reservaService.validarReservasActivas(reservasCopia, cliente, ahora);

        TarjetaVirtual tarjeta = tarjetaVirtualService
                .buscarTarjetaPorCliente(cliente)
                .orElseThrow(() -> new IllegalStateException(
                        "El cliente no tiene una tarjeta virtual registrada."));

        List<Entrada> entradas = new ArrayList<>();
        String codigoCompra = "COMP-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();

        CompraBuilder builder = new CompraBuilder(
                codigoCompra,
                cliente,
                new CompraPendiente()
        ).conFecha(ahora);

        for (ReservaAsiento reserva : reservasCopia) {
            Entrada entrada = crearEntrada(reserva, ahora);
            entradas.add(entrada);

            String codigoDetalle = "DET-" + UUID.randomUUID()
                    .toString().substring(0, 8).toUpperCase();
            builder.agregarDetalle(
                    new DetalleCompra(codigoDetalle, entrada, 1)
            );
        }

        Compra compra = builder.build();

        if (codigoPromocion != null && !codigoPromocion.isBlank()) {
            if (promocionService == null) {
                throw new IllegalStateException("El servicio de promociones no está configurado.");
            }
            promocionService.aplicarPromocion(
                    codigoPromocion.trim(), compra, java.time.LocalDate.now());
        }

        BigDecimal valorPuntos = BigDecimal.ZERO;
        if (puntosARedimir > 0) {
            if (puntosService == null) {
                throw new IllegalStateException("El servicio de puntos no está configurado.");
            }
            valorPuntos = puntosService.redimirParaCompra(
                    cliente, compra, puntosARedimir);
        }

        try {
            // Se obtiene una hora nueva justo antes de confirmar para no aceptar
            // una reserva que haya vencido mientras se construía la compra.
            return checkoutFacade.finalizarCompraConReserva(
                    compra,
                    tarjeta,
                    entradas,
                    reservasCopia,
                    cliente,
                    LocalDateTime.now()
            );
        } catch (RuntimeException ex) {
            if (puntosARedimir > 0 && valorPuntos.compareTo(BigDecimal.ZERO) > 0) {
                puntosService.acumularPuntos(cliente, puntosARedimir,
                        java.time.LocalDate.now(),
                        "Reintegro por compra de entradas fallida " + codigoCompra);
            }
            throw ex;
        }
    }


    /** Consulta la tarjeta virtual asociada al cliente autenticado. */
    public Optional<TarjetaVirtual> consultarTarjeta(Cliente cliente) {
        return tarjetaVirtualService.buscarTarjetaPorCliente(
                Objects.requireNonNull(cliente, "El cliente es obligatorio."));
    }

    /** Cancela la reserva temporal y libera sus asientos. */
    public void cancelarReserva(
            Collection<ReservaAsiento> reservas,
            Cliente cliente
    ) {
        reservaService.cancelarReserva(
                reservas,
                cliente,
                LocalDateTime.now()
        );
    }

    /** Procesa las reservas vencidas y devuelve cuántas expiraron. */
    public int procesarReservasExpiradas() {
        return reservaService.procesarExpiraciones(LocalDateTime.now());
    }

    public LocalDateTime consultarFechaExpiracion(
            Collection<ReservaAsiento> reservas) {
        return reservaService.consultarFechaExpiracion(reservas);
    }

    public int consultarDuracionReservaMinutos() {
        return reservaService.consultarDuracionMinutos();
    }

    public BigDecimal calcularTotalReserva(
            Collection<ReservaAsiento> reservas) {
        List<ReservaAsiento> copia = List.copyOf(reservas);
        if (copia.isEmpty()) {
            throw new IllegalArgumentException("No hay asientos reservados.");
        }
        BigDecimal precioUnitario = copia.get(0).getFuncion().getPrecioBase();
        return precioUnitario.multiply(BigDecimal.valueOf(copia.size()));
    }

    private Entrada crearEntrada(
            ReservaAsiento reserva,
            LocalDateTime fechaEmision
    ) {
        Funcion funcion = reserva.getFuncion();
        Asiento asiento = reserva.getAsiento();
        BigDecimal precio = funcion.getPrecioBase();

        FactoryEntrada factory;
        if (asiento.getTipo() == TipoAsiento.PREFERENCIAL) {
            factory = new FactoryEntradaPreferencial();
        } else {
            // Los asientos accesibles conservan el precio base general:
            // el modelo no define una tarifa diferenciada para ACCESIBLE.
            factory = new FactoryEntradaGeneral();
        }

        String codigo = "ENT-" + UUID.randomUUID()
                .toString().substring(0, 12).toUpperCase();
        String codigoQR = "QR-" + UUID.randomUUID();

        return factory.crearEntrada(
                codigo,
                codigoQR,
                funcion,
                asiento,
                precio,
                fechaEmision
        );
    }
}
