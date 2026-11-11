package co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.strategy;

import co.edu.uniquindio.proyectofinal.model.interfaces.PoliticaCancelacion;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Política que rechaza la cancelación cuando faltan menos de 2 horas.
 */
public class CancelacionNoPermitida implements PoliticaCancelacion {

    @Override
    public boolean permiteCancelar(
            Funcion funcion,
            LocalDateTime fechaSolicitud
    ) {
        validarParametros(funcion, fechaSolicitud);
        return false;
    }

    @Override
    public BigDecimal calcularPorcentajeReembolso(
            Funcion funcion,
            LocalDateTime fechaSolicitud
    ) {
        validarParametros(funcion, fechaSolicitud);
        return BigDecimal.ZERO;
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
