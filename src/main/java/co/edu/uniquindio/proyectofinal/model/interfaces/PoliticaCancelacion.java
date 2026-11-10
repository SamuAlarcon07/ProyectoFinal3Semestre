package co.edu.uniquindio.proyectofinal.model.interfaces;

import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Define el contrato para las diferentes políticas de cancelación
 * y cálculo de reembolsos de CinemaUQ.
 *
 * Cada implementación establece las condiciones para permitir
 * una cancelación y el porcentaje de dinero que debe devolverse.
 *
 * @author Equipo CinemaUQ
 */
public interface PoliticaCancelacion {

    /**
     * Determina si una compra puede cancelarse según la política.
     *
     * @param funcion función asociada a la entrada.
     * @param fechaSolicitud fecha y hora en que se solicita
     *                       la cancelación.
     * @return true si la cancelación está permitida.
     */
    boolean permiteCancelar(
            Funcion funcion,
            LocalDateTime fechaSolicitud
    );

    /**
     * Calcula el porcentaje de reembolso correspondiente.
     *
     * @param funcion función asociada a la entrada.
     * @param fechaSolicitud fecha y hora de la solicitud.
     * @return porcentaje de reembolso entre 0 y 100.
     */
    BigDecimal calcularPorcentajeReembolso(
            Funcion funcion,
            LocalDateTime fechaSolicitud
    );

}
