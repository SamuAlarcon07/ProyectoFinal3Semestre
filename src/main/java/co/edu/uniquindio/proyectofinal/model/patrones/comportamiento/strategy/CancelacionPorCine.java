package co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.strategy;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;
import co.edu.uniquindio.proyectofinal.model.interfaces.PoliticaCancelacion;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Política aplicada cuando el cine cancela la función.
 * Autoriza el reembolso del 100 %.
 */
public class CancelacionPorCine implements PoliticaCancelacion {

    private static final BigDecimal REEMBOLSO =
            new BigDecimal("100");

    @Override
    public boolean permiteCancelar(
            Funcion funcion,
            LocalDateTime fechaSolicitud
    ) {
        validarParametros(funcion, fechaSolicitud);

        return funcion.getEstado() == EstadoFuncion.CANCELADA;
    }

    @Override
    public BigDecimal calcularPorcentajeReembolso(
            Funcion funcion,
            LocalDateTime fechaSolicitud
    ) {
        return permiteCancelar(funcion, fechaSolicitud)
                ? REEMBOLSO
                : BigDecimal.ZERO;
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
