package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.enums.EstadoCancelacion;

import co.edu.uniquindio.proyectofinal.model.interfaces.PoliticaCancelacion;

import co.edu.uniquindio.proyectofinal.model.principal.Cancelacion;

import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import co.edu.uniquindio.proyectofinal.model.principal.MovimientoTarjeta;

import co.edu.uniquindio.proyectofinal.model.principal.Reembolso;

import co.edu.uniquindio.proyectofinal.model.principal.TarjetaVirtual;



import java.math.BigDecimal;

import java.time.LocalDateTime;

import java.util.LinkedHashMap;

import java.util.Map;

import java.util.Optional;



/** Aplica políticas de cancelación y coordina los reembolsos. */

public class CancelacionService {



    private final Map<String, Cancelacion> cancelaciones = new LinkedHashMap<>();

    private final Map<String, Reembolso> reembolsosPorCancelacion = new LinkedHashMap<>();



    public Cancelacion solicitarCancelacion(String codigo, co.edu.uniquindio.proyectofinal.model.principal.Compra compra,

                                            Funcion funcion, String motivo, PoliticaCancelacion politica, LocalDateTime fechaSolicitud) {

        if (politica == null || funcion == null) throw new IllegalArgumentException("La función y la política son obligatorias.");

        Cancelacion cancelacion = new Cancelacion(codigo, compra, motivo);

        if (politica.permiteCancelar(funcion, fechaSolicitud)) {

            BigDecimal porcentaje = politica.calcularPorcentajeReembolso(funcion, fechaSolicitud);

            cancelacion.aprobar(porcentaje);

            compra.solicitarCancelacion();

        } else {

            cancelacion.rechazar();

        }

        if (cancelaciones.putIfAbsent(codigo, cancelacion) != null) {

            throw new IllegalArgumentException("Ya existe una cancelación con ese código.");

        }

        return cancelacion;

    }



    public Optional<Reembolso> procesarReembolso(String codigoReembolso,

                                                 Cancelacion cancelacion, TarjetaVirtual tarjeta) {

        if (cancelacion == null || tarjeta == null) throw new IllegalArgumentException("La cancelación y la tarjeta son obligatorias.");

        if (!cancelacion.getCompra().getCliente().getId().equals(tarjeta.getTitular().getId())) {

            throw new IllegalArgumentException("La tarjeta no pertenece al cliente de la compra.");

        }

        if (cancelacion.getEstado() != EstadoCancelacion.APROBADA) return Optional.empty();

        if (reembolsosPorCancelacion.containsKey(cancelacion.getCodigo())) {

            throw new IllegalStateException("Esta cancelación ya tiene un reembolso registrado.");

        }

        BigDecimal valor = cancelacion.calcularValorReembolso();

        if (valor.compareTo(BigDecimal.ZERO) == 0) {

            cancelacion.marcarProcesada();

            return Optional.empty();

        }

        Reembolso reembolso = new Reembolso(codigoReembolso, cancelacion, valor);

        tarjeta.reembolsar(valor, codigoReembolso);

        MovimientoTarjeta movimiento = tarjeta.getMovimientos()

                .get(tarjeta.getMovimientos().size() - 1);

        reembolso.marcarProcesado(movimiento);

        cancelacion.getCompra().solicitarReembolso();

        reembolsosPorCancelacion.put(cancelacion.getCodigo(), reembolso);

        return Optional.of(reembolso);

    }



    public Optional<Cancelacion> buscarCancelacion(String codigo) {

        return Optional.ofNullable(cancelaciones.get(codigo));

    }



    public java.util.List<Cancelacion> consultarCancelaciones() {

        return java.util.List.copyOf(cancelaciones.values());

    }

}

