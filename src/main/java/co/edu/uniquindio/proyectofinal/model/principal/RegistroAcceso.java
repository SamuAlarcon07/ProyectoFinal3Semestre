package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.ResultadoAcceso;

import java.time.LocalDateTime;
import java.util.Objects;

/** Registra un intento de validación de acceso mediante una entrada. */
public class RegistroAcceso {

    private final String codigo;
    private final Entrada entrada;
    private final LocalDateTime fechaHora;
    private final ResultadoAcceso resultado;
    private final String observacion;

    public RegistroAcceso(String codigo, Entrada entrada,
                          LocalDateTime fechaHora, ResultadoAcceso resultado,
                          String observacion) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código del registro es obligatorio.");
        }
        this.codigo = codigo.trim();
        this.entrada = Objects.requireNonNull(entrada, "La entrada es obligatoria.");
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora son obligatorias.");
        this.resultado = Objects.requireNonNull(resultado, "El resultado es obligatorio.");
        this.observacion = observacion == null ? "" : observacion.trim();
    }

    public String getCodigo() { return codigo; }
    public Entrada getEntrada() { return entrada; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public ResultadoAcceso getResultado() { return resultado; }
    public String getObservacion() { return observacion; }
}
