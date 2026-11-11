package co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.strategy;

import co.edu.uniquindio.proyectofinal.model.interfaces.PoliticaCancelacion;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Política para cancelaciones solicitadas con más de 24 horas
 * de anticipación a la función. Autoriza el reembolso del 100 %.
 */
public class CancelacionMas24Horas implements PoliticaCancelacion {

    private static final Duration LIMITE = Duration.ofHours(24);
    private static final BigDecimal REEMBOLSO = new BigDecimal("100");
    private static final BigDecimal SIN_REEMBOLSO = BigDecimal.ZERO;

    @Override
    public boolean permiteCancelar(
            Funcion funcion,
            LocalDateTime fechaSolicitud
    ) {
        validarParametros(funcion, fechaSolicitud);

        Duration tiempoRestante = Duration.between(
                fechaSolicitud,
                funcion.getFechaHora()
        );

        return tiempoRestante.compareTo(LIMITE) > 0;
    }

    @Override
    public BigDecimal calcularPorcentajeReembolso(
            Funcion funcion,
            LocalDateTime fechaSolicitud
    ) {
        return permiteCancelar(funcion, fechaSolicitud)
                ? REEMBOLSO
                : SIN_REEMBOLSO;
    }

    private static void validarParametros(
            Funcion funcion,
            LocalDateTime fechaSolicitud
    ) {
        Objects.requireNonNull(
                funcion,
                "La función es obligatoria."
        );

        Objects.requireNonNull(
                fechaSolicitud,
                "La fecha de solicitud es obligatoria."
        );
    }
}
