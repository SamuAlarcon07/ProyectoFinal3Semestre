
package co.edu.uniquindio.proyectofinal.model.patrones.estructurales.facade;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;
import co.edu.uniquindio.proyectofinal.model.principal.Asiento;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;
import co.edu.uniquindio.proyectofinal.model.principal.DetalleCompra;
import co.edu.uniquindio.proyectofinal.model.principal.Entrada;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;
import co.edu.uniquindio.proyectofinal.model.principal.TarjetaVirtual;
import co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator.ElementoVendibleDecorador;
import co.edu.uniquindio.proyectofinal.model.service.CompraService;
import co.edu.uniquindio.proyectofinal.model.service.DisponibilidadAsientosService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Fachada que simplifica la consulta de asientos
 * y la finalización de compras de CinemaUQ.
 */
public class CheckoutFacade {

    private final DisponibilidadAsientosService disponibilidadService;
    private final CompraService compraService;

    public CheckoutFacade(
            DisponibilidadAsientosService disponibilidadService,
            CompraService compraService
    ) {
        this.disponibilidadService = Objects.requireNonNull(
                disponibilidadService,
                "El servicio de disponibilidad es obligatorio."
        );

        this.compraService = Objects.requireNonNull(
                compraService,
                "El servicio de compras es obligatorio."
        );
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
