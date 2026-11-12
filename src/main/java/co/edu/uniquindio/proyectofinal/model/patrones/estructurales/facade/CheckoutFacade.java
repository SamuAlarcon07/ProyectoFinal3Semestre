
package co.edu.uniquindio.proyectofinal.model.patrones.estructurales.facade;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;
import co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator.ElementoVendibleDecorador;
import co.edu.uniquindio.proyectofinal.model.principal.*;
import co.edu.uniquindio.proyectofinal.model.service.CompraService;
import co.edu.uniquindio.proyectofinal.model.service.DisponibilidadAsientosService;
import co.edu.uniquindio.proyectofinal.model.service.ReservaService;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Fachada que simplifica la consulta de asientos
 * y la finalización de compras de CinemaUQ.
 */
public class CheckoutFacade {

    private final DisponibilidadAsientosService disponibilidadService;
    private final CompraService compraService;
    private final ReservaService reservaService;

    public CheckoutFacade(
            DisponibilidadAsientosService disponibilidadService,
            CompraService compraService
    ) {
        this(disponibilidadService, compraService, null);
    }

    /** Constructor usado cuando el proceso de compra parte de una reserva temporal. */
    public CheckoutFacade(
            DisponibilidadAsientosService disponibilidadService,
            CompraService compraService,
            ReservaService reservaService
    ) {
        this.disponibilidadService = Objects.requireNonNull(
                disponibilidadService,
                "El servicio de disponibilidad es obligatorio."
        );
        this.compraService = Objects.requireNonNull(
                compraService,
                "El servicio de compras es obligatorio."
        );
        this.reservaService = reservaService;
    }

    /**
     * Consulta los asientos disponibles de una función.
     */
    public List<Asiento> consultarAsientosDisponibles(
            Funcion funcion,
            LocalDateTime instante
    ) {
        return disponibilidadService.consultarAsientosDisponibles(
                funcion,
                instante
        );
    }

    /**
     * Valida las entradas y finaliza la compra.
     *
     * Los asientos se registran en bloque antes del cobro.
     * Si la compra no llega a confirmarse, se liberan.
     */
    public synchronized Compra finalizarCompra(
            Compra compra,
            TarjetaVirtual tarjeta,
            Collection<Entrada> entradas,
            LocalDateTime instante
    ) {
        Objects.requireNonNull(
                compra,
                "La compra es obligatoria."
        );

        Objects.requireNonNull(
                tarjeta,
                "La tarjeta es obligatoria."
        );

        Objects.requireNonNull(
                entradas,
                "La colección de entradas es obligatoria."
        );

        Objects.requireNonNull(
                instante,
                "El instante es obligatorio."
        );

        if (!"PENDIENTE".equals(compra.consultarNombreEstado())) {
            throw new IllegalStateException(
                    "Solo se puede finalizar una compra pendiente."
            );
        }

        List<Entrada> entradasValidadas =
                validarCorrespondenciaYDisponibilidad(
                        compra,
                        entradas,
                        instante
                );

        // Registrar los asientos antes de realizar el cobro.
        disponibilidadService.registrarEntradas(
                entradasValidadas,
                instante
        );

        try {

            return compraService.confirmarCompra(
                    compra,
                    tarjeta
            );

        } catch (RuntimeException ex) {

            // Si la compra no quedó pagada, liberar los asientos.
            if (!"PAGADA".equals(compra.consultarNombreEstado())) {

                disponibilidadService.eliminarEntradasRegistradas(
                        entradasValidadas
                );
            }

            throw ex;
        }
    }

    /**
     * Comprueba que las entradas recibidas correspondan
     * exactamente con las entradas de los detalles de compra.
     */
    /**
     * Finaliza una compra cuyos asientos ya están retenidos mediante reservas temporales.
     * Si el pago falla, las reservas permanecen activas hasta que se cancelen o expiren.
     */
    public synchronized Compra finalizarCompraConReserva(
            Compra compra,
            TarjetaVirtual tarjeta,
            Collection<Entrada> entradas,
            Collection<ReservaAsiento> reservas,
            Cliente cliente,
            LocalDateTime instante
    ) {
        Objects.requireNonNull(compra, "La compra es obligatoria.");
        Objects.requireNonNull(tarjeta, "La tarjeta es obligatoria.");
        Objects.requireNonNull(entradas, "Las entradas son obligatorias.");
        Objects.requireNonNull(reservas, "Las reservas son obligatorias.");
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");

        if (reservaService == null) {
            throw new IllegalStateException(
                    "CheckoutFacade no fue configurada con ReservaService.");
        }
        if (!"PENDIENTE".equals(compra.consultarNombreEstado())) {
            throw new IllegalStateException(
                    "Solo se puede finalizar una compra pendiente.");
        }
        if (!tarjeta.getTitular().getId().equals(cliente.getId())) {
            throw new SecurityException(
                    "La tarjeta no pertenece al cliente autenticado.");
        }

        // El orden de bloqueo siempre es ReservaService -> DisponibilidadAsientosService.
        // Así se evita que otra reserva cambie la ocupación entre la validación y la conversión.
        synchronized (reservaService) {
            synchronized (disponibilidadService) {
                // El controlador entrega un instante actual justo antes de pagar;
                // se conserva para facilitar pruebas reproducibles.
                LocalDateTime momentoConfirmacion = instante;

                reservaService.validarReservasActivas(
                        reservas, cliente, momentoConfirmacion);

                List<Entrada> entradasValidadas = validarEntradasDeReserva(
                        compra, entradas, reservas, momentoConfirmacion);

                // El pago se procesa solo después de verificar la correspondencia
                // exacta entre compra, entradas y reservas.
                Compra confirmada = compraService.confirmarCompra(compra, tarjeta);

                // Se mantiene el bloqueo de disponibilidad hasta sustituir las
                // reservas por las entradas emitidas que ocuparán esos asientos.
                reservaService.convertirReservasEnEntradas(
                        reservas, entradasValidadas, cliente, momentoConfirmacion);

                return confirmada;
            }
        }
    }

    private List<Entrada> validarEntradasDeReserva(
            Compra compra,
            Collection<Entrada> entradasRecibidas,
            Collection<ReservaAsiento> reservas,
            LocalDateTime instante
    ) {
        List<Entrada> entradasCopia = List.copyOf(entradasRecibidas);
        List<ReservaAsiento> reservasCopia = List.copyOf(reservas);
        if (entradasCopia.isEmpty() || entradasCopia.size() != reservasCopia.size()) {
            throw new IllegalArgumentException(
                    "Debe existir una entrada por cada asiento reservado.");
        }

        Map<String, Entrada> entradasEsperadas = new HashMap<>();
        for (DetalleCompra detalle : compra.getDetalles()) {
            ElementoVendible elemento = desenvolver(detalle.getElemento());
            if (elemento instanceof Entrada entrada) {
                if (entradasEsperadas.putIfAbsent(entrada.getCodigo(), entrada) != null) {
                    throw new IllegalArgumentException(
                            "La compra contiene códigos de entrada duplicados.");
                }
            }
        }

        Map<String, ReservaAsiento> reservasPorAsiento = new HashMap<>();
        for (ReservaAsiento reserva : reservasCopia) {
            String clave = claveAsiento(reserva.getFuncion(), reserva.getAsiento());
            if (reservasPorAsiento.putIfAbsent(clave, reserva) != null) {
                throw new IllegalArgumentException(
                        "Hay reservas duplicadas para el mismo asiento.");
            }
        }

        Set<String> codigosRecibidos = new HashSet<>();
        List<Entrada> validadas = new ArrayList<>();
        for (Entrada entrada : entradasCopia) {
            Objects.requireNonNull(entrada, "No se permiten entradas nulas.");
            if (!codigosRecibidos.add(entrada.getCodigo())) {
                throw new IllegalArgumentException(
                        "La compra contiene entradas repetidas.");
            }
            Entrada esperada = entradasEsperadas.remove(entrada.getCodigo());
            if (esperada != entrada) {
                throw new IllegalArgumentException(
                        "Las entradas no coinciden con los detalles de la compra.");
            }
            if (entrada.getEstado() != EstadoEntrada.EMITIDA) {
                throw new IllegalStateException(
                        "Solo se pueden comprar entradas emitidas.");
            }
            if (!entrada.getFuncion().puedeVenderEntradas(instante)) {
                throw new IllegalStateException(
                        "La función ya no está disponible para la venta.");
            }
            String clave = claveAsiento(entrada.getFuncion(), entrada.getAsiento());
            ReservaAsiento reserva = reservasPorAsiento.remove(clave);
            if (reserva == null
                    || reserva.getFuncion() != entrada.getFuncion()
                    || reserva.getAsiento() != entrada.getAsiento()) {
                throw new IllegalArgumentException(
                        "Una entrada no corresponde a los asientos reservados.");
            }
            validadas.add(entrada);
        }

        if (!entradasEsperadas.isEmpty() || !reservasPorAsiento.isEmpty()) {
            throw new IllegalArgumentException(
                    "La compra debe contener exactamente las entradas reservadas.");
        }
        return List.copyOf(validadas);
    }

    private String claveAsiento(Funcion funcion, Asiento asiento) {
        return funcion.getCodigo() + "|"
                + asiento.getFila().toUpperCase(java.util.Locale.ROOT)
                + "|" + asiento.getNumero();
    }

    private List<Entrada> validarCorrespondenciaYDisponibilidad(
            Compra compra,
            Collection<Entrada> entradasRecibidas,
            LocalDateTime instante
    ) {

        Map<String, Entrada> entradasEsperadas = new HashMap<>();

        for (DetalleCompra detalle : compra.getDetalles()) {

            ElementoVendible elemento = desenvolver(
                    detalle.getElemento()
            );

            if (elemento instanceof Entrada entrada) {

                if (entradasEsperadas.putIfAbsent(
                        entrada.getCodigo(),
                        entrada
                ) != null) {

                    throw new IllegalStateException(
                            "La compra contiene códigos de entrada duplicados."
                    );
                }
            }
        }

        List<Entrada> resultado = new ArrayList<>();

        Set<String> codigosRecibidos = new HashSet<>();
        Set<String> asientosRecibidos = new HashSet<>();

        for (Entrada entrada : entradasRecibidas) {

            Objects.requireNonNull(
                    entrada,
                    "No se permiten entradas nulas."
            );

            if (!codigosRecibidos.add(entrada.getCodigo())) {
                throw new IllegalArgumentException(
                        "La colección contiene códigos de entrada duplicados."
                );
            }

            Entrada esperada = entradasEsperadas.remove(
                    entrada.getCodigo()
            );

            if (esperada == null || esperada != entrada) {
                throw new IllegalArgumentException(
                        "Las entradas proporcionadas no coinciden "
                                + "con los detalles de la compra."
                );
            }

            if (entrada.getEstado() != EstadoEntrada.EMITIDA) {
                throw new IllegalStateException(
                        "Solo se pueden comprar entradas en estado EMITIDA."
                );
            }

            Funcion funcion = Objects.requireNonNull(
                    entrada.getFuncion(),
                    "La función de la entrada es obligatoria."
            );

            Asiento asiento = Objects.requireNonNull(
                    entrada.getAsiento(),
                    "El asiento de la entrada es obligatorio."
            );

            if (!funcion.puedeVenderEntradas(instante)) {
                throw new IllegalStateException(
                        "La función no está disponible para la venta."
                );
            }

            String codigoAsiento =
                    funcion.getCodigo()
                            + "|"
                            + asiento.getFila().toUpperCase()
                            + "|"
                            + asiento.getNumero();

            if (!asientosRecibidos.add(codigoAsiento)) {
                throw new IllegalArgumentException(
                        "Se intenta utilizar dos veces el mismo asiento."
                );
            }

            if (!disponibilidadService.estaDisponible(
                    funcion,
                    asiento,
                    instante
            )) {
                throw new IllegalStateException(
                        "El asiento ya no está disponible."
                );
            }

            resultado.add(entrada);
        }

        if (!entradasEsperadas.isEmpty()) {
            throw new IllegalArgumentException(
                    "Faltan entradas de los detalles de la compra."
            );
        }

        return List.copyOf(resultado);
    }

    /**
     * Desenvolver decoradores para obtener el elemento original.
     */
    private ElementoVendible desenvolver(
            ElementoVendible elemento
    ) {

        ElementoVendible actual = Objects.requireNonNull(elemento);

        Set<ElementoVendible> visitados =
                java.util.Collections.newSetFromMap(
                        new java.util.IdentityHashMap<>()
                );

        while (actual instanceof ElementoVendibleDecorador decorador) {

            if (!visitados.add(actual)) {
                throw new IllegalStateException(
                        "Se detectó una composición circular de decoradores."
                );
            }

            actual = decorador.obtenerElementoOriginal();
        }

        return actual;
    }
}
