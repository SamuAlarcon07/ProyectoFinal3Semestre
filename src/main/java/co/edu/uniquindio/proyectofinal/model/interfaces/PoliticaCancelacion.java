package co.edu.uniquindio.proyectofinal.model.interfaces;



import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import java.math.BigDecimal;

import java.time.LocalDateTime;



/** Contrato Strategy para las políticas de cancelación. */

public interface PoliticaCancelacion {

    boolean permiteCancelar(Funcion funcion, LocalDateTime fechaSolicitud);

    BigDecimal calcularPorcentajeReembolso(Funcion funcion, LocalDateTime fechaSolicitud);

}