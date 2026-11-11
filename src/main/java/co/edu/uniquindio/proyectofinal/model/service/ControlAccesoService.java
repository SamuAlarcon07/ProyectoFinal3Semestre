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
import java.util.Objects;
import java.util.UUID;

/** Valida entradas digitales y registra los intentos de ingreso. */
public class ControlAccesoService {

    private final Map<String, Entrada> entradasPorQR = new LinkedHashMap<>();
    private final List<RegistroAcceso> registros = new ArrayList<>();

    /** Registra únicamente entradas emitidas y evita duplicar códigos QR. */
    public synchronized void registrarEntrada(Entrada entrada) {
        Objects.requireNonNull(entrada, "La entrada es obligatoria.");

        String codigoQR = entrada.getCodigoQR();
        if (codigoQR == null || codigoQR.isBlank()) {
            throw new IllegalArgumentException("El código QR es obligatorio.");
        }
        if (entrada.getEstado() != EstadoEntrada.EMITIDA) {
            throw new IllegalStateException(
                    "Solo se pueden registrar entradas que estén emitidas.");
        }
        if (entradasPorQR.putIfAbsent(codigoQR.trim(), entrada) != null) {
            throw new IllegalArgumentException("El código QR ya está registrado.");
        }
    }

    /**
     * Valida el QR y marca la entrada como utilizada en una única sección sincronizada,
     * evitando que dos lectores acepten simultáneamente el mismo código.
     *
     * El enunciado del proyecto no define una ventana horaria de acceso. Por ello,
     * este servicio valida estado de entrada, estado de función y uso único,
     * sin inventar un margen de minutos antes/después de la función.
     */
    public synchronized RegistroAcceso validarAcceso(
            String codigoQR, LocalDateTime instante) {
        if (codigoQR == null || codigoQR.isBlank()) {
            throw new IllegalArgumentException("El código QR es obligatorio.");
        }
        Objects.requireNonNull(instante, "El instante de validación es obligatorio.");

        Entrada entrada = entradasPorQR.get(codigoQR.trim());
        if (entrada == null) {
            throw new IllegalArgumentException(
                    "El código QR no corresponde a una entrada registrada.");
        }

        boolean valido = entrada.getEstado() == EstadoEntrada.EMITIDA
                && entrada.getFuncion().getEstado() == EstadoFuncion.PROGRAMADA;

        ResultadoAcceso resultado = valido
                ? ResultadoAcceso.ACEPTADO
                : ResultadoAcceso.RECHAZADO;

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

        RegistroAcceso registro = new RegistroAcceso(
                UUID.randomUUID().toString(), entrada, instante, resultado, observacion);
        registros.add(registro);
        return registro;
    }

    public synchronized List<RegistroAcceso> consultarRegistros() {
        return List.copyOf(registros);
    }

    public synchronized int consultarCantidadEntradasRegistradas() {
        return entradasPorQR.size();
    }
}
