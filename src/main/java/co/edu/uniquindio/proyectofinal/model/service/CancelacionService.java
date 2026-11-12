package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoCancelacion;
import co.edu.uniquindio.proyectofinal.model.interfaces.PoliticaCancelacion;
import co.edu.uniquindio.proyectofinal.model.principal.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** Aplica políticas de cancelación y coordina los reembolsos. */
public class CancelacionService {

    private final Map<String, Cancelacion> cancelaciones = new LinkedHashMap<>();
    private final Map<String, Reembolso> reembolsosPorCancelacion = new LinkedHashMap<>();

    /** Registra y evalúa una solicitud antes de modificar el estado de la compra. */
    public synchronized Cancelacion solicitarCancelacion(
            String codigo,
            Compra compra,
            Funcion funcion,
            String motivo,
            PoliticaCancelacion politica,
            LocalDateTime fechaSolicitud) {

        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de cancelación es obligatorio.");
        }
        Objects.requireNonNull(compra, "La compra es obligatoria.");
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(politica, "La política de cancelación es obligatoria.");
        Objects.requireNonNull(fechaSolicitud, "La fecha de solicitud es obligatoria.");
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("El motivo es obligatorio.");
        }

        String codigoNormalizado = codigo.trim();
        if (cancelaciones.containsKey(codigoNormalizado)) {
            throw new IllegalArgumentException(
                    "Ya existe una cancelación con ese código.");
        }

        synchronized (compra) {
            if (!"PAGADA".equals(compra.consultarNombreEstado())) {
                throw new IllegalStateException(
                        "Solo se pueden solicitar cancelaciones de compras pagadas.");
            }

            Cancelacion cancelacion = new Cancelacion(
                    codigoNormalizado, compra, motivo);

            if (politica.permiteCancelar(funcion, fechaSolicitud)) {
                BigDecimal porcentaje = politica.calcularPorcentajeReembolso(
                        funcion, fechaSolicitud);

                // aprobar() valida que el porcentaje esté entre 0 y 100.
                cancelacion.aprobar(porcentaje);

                // La transición solo se ejecuta después de validar la solicitud.
                compra.solicitarCancelacion();
            } else {
                cancelacion.rechazar();
            }

            cancelaciones.put(codigoNormalizado, cancelacion);
            return cancelacion;
        }
    }

    /**
     * Procesa una devolución aprobada. La tarjeta debe pertenecer al cliente
     * de la compra y el reembolso solo puede registrarse una vez.
     */
    public synchronized Optional<Reembolso> procesarReembolso(
            String codigoReembolso,
            Cancelacion cancelacion,
            TarjetaVirtual tarjeta) {

        if (codigoReembolso == null || codigoReembolso.isBlank()) {
            throw new IllegalArgumentException(
                    "El código de reembolso es obligatorio.");
        }
        Objects.requireNonNull(cancelacion, "La cancelación es obligatoria.");
        Objects.requireNonNull(tarjeta, "La tarjeta es obligatoria.");

        String codigoNormalizado = codigoReembolso.trim();
        Cancelacion cancelacionRegistrada = cancelaciones.get(cancelacion.getCodigo());
        if (cancelacionRegistrada != cancelacion) {
            throw new IllegalArgumentException(
                    "La cancelación no pertenece a este servicio.");
        }

        if (!cancelacion.getCompra().getCliente().getId()
                .equals(tarjeta.getTitular().getId())) {
            throw new IllegalArgumentException(
                    "La tarjeta no pertenece al cliente de la compra.");
        }

        if (reembolsosPorCancelacion.containsKey(cancelacion.getCodigo())) {
            throw new IllegalStateException(
                    "Esta cancelación ya tiene un reembolso registrado.");
        }

        boolean codigoReembolsoUsado = reembolsosPorCancelacion.values().stream()
                .anyMatch(reembolso -> reembolso.getCodigo().equals(codigoNormalizado));
        if (codigoReembolsoUsado) {
            throw new IllegalArgumentException(
                    "Ya existe un reembolso con ese código.");
        }

        if (cancelacion.getEstado() != EstadoCancelacion.APROBADA) {
            return Optional.empty();
        }

        Compra compra = cancelacion.getCompra();
        synchronized (compra) {
            if (!"CANCELADA".equals(compra.consultarNombreEstado())) {
                throw new IllegalStateException(
                        "La compra debe estar cancelada antes de procesar el reembolso.");
            }

            BigDecimal valor = cancelacion.calcularValorReembolso();

            // Una cancelación aprobada con porcentaje 0 se marca procesada,
            // pero no se crea movimiento de dinero ni estado REEMBOLSADA.
            if (valor.compareTo(BigDecimal.ZERO) == 0) {
                cancelacion.marcarProcesada();
                return Optional.empty();
            }

            Reembolso reembolso = new Reembolso(
                    codigoNormalizado, cancelacion, valor);

            synchronized (tarjeta) {
                tarjeta.reembolsar(valor, codigoNormalizado);
                MovimientoTarjeta movimiento = tarjeta.getMovimientos()
                        .get(tarjeta.getMovimientos().size() - 1);

                reembolso.marcarProcesado(movimiento);
                compra.solicitarReembolso();
            }

            reembolsosPorCancelacion.put(cancelacion.getCodigo(), reembolso);
            return Optional.of(reembolso);
        }
    }

    public synchronized Optional<Cancelacion> buscarCancelacion(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(cancelaciones.get(codigo.trim()));
    }

    public synchronized java.util.List<Cancelacion> consultarCancelaciones() {
        return java.util.List.copyOf(cancelaciones.values());
    }

    public synchronized java.util.List<Reembolso> consultarReembolsos() {
        return java.util.List.copyOf(reembolsosPorCancelacion.values());
    }
}
