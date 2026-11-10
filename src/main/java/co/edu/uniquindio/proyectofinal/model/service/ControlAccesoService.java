package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;

import co.edu.uniquindio.proyectofinal.model.enums.ResultadoAcceso;

import co.edu.uniquindio.proyectofinal.model.principal.Entrada;

import co.edu.uniquindio.proyectofinal.model.principal.RegistroAcceso;



import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.UUID;



/** Valida entradas digitales y registra los intentos de ingreso. */

public class ControlAccesoService {



    private final Map<String, Entrada> entradasPorQR = new LinkedHashMap<>();

    private final List<RegistroAcceso> registros = new ArrayList<>();



    public void registrarEntrada(Entrada entrada) {

        if (entrada == null) throw new IllegalArgumentException("La entrada es obligatoria.");

        if (entradasPorQR.putIfAbsent(entrada.getCodigoQR(), entrada) != null) {

            throw new IllegalArgumentException("El código QR ya está registrado.");

        }

    }



    public RegistroAcceso validarAcceso(String codigoQR, LocalDateTime instante) {

        if (codigoQR == null || codigoQR.isBlank()) throw new IllegalArgumentException("El código QR es obligatorio.");

        if (instante == null) throw new IllegalArgumentException("El instante de validación es obligatorio.");

        Entrada entrada = entradasPorQR.get(codigoQR);

        if (entrada == null) throw new IllegalArgumentException("El código QR no corresponde a una entrada registrada.");



        boolean valido = entrada.getEstado() == EstadoEntrada.EMITIDA

                && entrada.getFuncion().getEstado() == EstadoFuncion.PROGRAMADA;

        ResultadoAcceso resultado = valido ? ResultadoAcceso.ACEPTADO : ResultadoAcceso.RECHAZADO;

        String observacion;

        if (valido) {

            entrada.marcarComoUtilizada();

            observacion = "Ingreso autorizado.";

        } else if (entrada.getEstado() == EstadoEntrada.UTILIZADA) {

            observacion = "La entrada ya fue utilizada.";

        } else if (entrada.getEstado() == EstadoEntrada.ANULADA) {

            observacion = "La entrada está anulada.";

        } else {

            observacion = "La función no está disponible.";

        }



        RegistroAcceso registro = new RegistroAcceso(UUID.randomUUID().toString(),

                entrada, instante, resultado, observacion);

        registros.add(registro);

        return registro;

    }



    public List<RegistroAcceso> consultarRegistros() {

        return List.copyOf(registros);

    }

}