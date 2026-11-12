package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.principal.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Gestiona reservas temporales de uno o varios asientos.
 * La duración predeterminada de una reserva es de diez minutos.
 */
public class ReservaService {

    private static final Duration VIGENCIA_PREDETERMINADA = Duration.ofMinutes(10);

    private final DisponibilidadAsientosService disponibilidadService;
    private final Duration vigencia;
    private final Map<String, List<ReservaAsiento>> reservasPorGrupo =
            new LinkedHashMap<>();
    private final Map<String, String> grupoPorCodigoReserva = new HashMap<>();

    public ReservaService(DisponibilidadAsientosService disponibilidadService) {
        this(disponibilidadService, VIGENCIA_PREDETERMINADA);
    }

    /** Constructor configurable, útil también para pruebas. */
    public ReservaService(DisponibilidadAsientosService disponibilidadService,
                          Duration vigencia) {
        this.disponibilidadService = Objects.requireNonNull(
                disponibilidadService, "El servicio de disponibilidad es obligatorio.");
        this.vigencia = Objects.requireNonNull(vigencia,
                "La duración de la reserva es obligatoria.");
        if (vigencia.isZero() || vigencia.isNegative()) {
            throw new IllegalArgumentException(
                    "La duración de la reserva debe ser positiva.");
        }
    }

    /**
     * Reserva de forma temporal todos los asientos seleccionados.
     * Si alguno ya no está disponible, no se registra ninguno.
     */
    public synchronized List<ReservaAsiento> reservarAsientos(
            Funcion funcion,
            Collection<Asiento> asientos,
            Cliente cliente,
            LocalDateTime instante) {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(asientos, "La selección de asientos es obligatoria.");
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        Objects.requireNonNull(instante, "La fecha de reserva es obligatoria.");

        procesarExpiraciones(instante);

        if (!funcion.puedeVenderEntradas(instante)) {
            throw new IllegalStateException(
                    "La función ya no está disponible para reservar asientos.");
        }

        List<Asiento> seleccion = List.copyOf(asientos);
        if (seleccion.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar al menos un asiento.");
        }

        Set<String> identificadores = new HashSet<>();
        for (Asiento asiento : seleccion) {
            Objects.requireNonNull(asiento, "No se permiten asientos nulos.");
            if (!funcion.getSala().contieneAsiento(asiento) || !asiento.isActivo()) {
                throw new IllegalArgumentException(
                        "Uno de los asientos no pertenece a la sala o está inactivo.");
            }
            if (!identificadores.add(asiento.obtenerIdentificador())) {
                throw new IllegalArgumentException(
                        "La selección contiene asientos repetidos.");
            }
        }

        String codigoGrupo = "RES-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase();
        LocalDateTime expiracion = instante.plus(vigencia);
        List<ReservaAsiento> nuevasReservas = new ArrayList<>();

        for (Asiento asiento : seleccion) {
            String codigoReserva = codigoGrupo + "-" + asiento.obtenerIdentificador();
            nuevasReservas.add(new ReservaAsiento(
                    codigoReserva,
                    funcion,
                    asiento,
                    cliente,
                    instante,
                    expiracion
            ));
        }

        disponibilidadService.registrarReservasAtomicas(nuevasReservas, instante);

        List<ReservaAsiento> grupo = List.copyOf(nuevasReservas);
        reservasPorGrupo.put(codigoGrupo, grupo);
        for (ReservaAsiento reserva : grupo) {
            grupoPorCodigoReserva.put(reserva.getCodigo(), codigoGrupo);
        }

        return grupo;
    }

    /** Verifica que todas las reservas pertenezcan al cliente y sigan vigentes. */
    public synchronized void validarReservasActivas(
            Collection<ReservaAsiento> reservas,
            Cliente cliente,
            LocalDateTime instante) {
        Objects.requireNonNull(reservas, "La colección de reservas es obligatoria.");
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");

        procesarExpiraciones(instante);
        List<ReservaAsiento> copia = List.copyOf(reservas);
        List<ReservaAsiento> grupo = obtenerGrupoRegistrado(copia);
        if (grupo.size() != copia.size()) {
            throw new IllegalArgumentException(
                    "La selección no corresponde al grupo completo de reservas.");
        }
        for (ReservaAsiento reserva : grupo) {
            if (!copia.contains(reserva)) {
                throw new IllegalArgumentException(
                        "La selección no corresponde al grupo original de reservas.");
            }
        }
        disponibilidadService.validarReservasActivas(copia, cliente, instante);
    }

    /** Cancela y libera todos los asientos del grupo. */
    public synchronized void cancelarReserva(
            Collection<ReservaAsiento> reservas,
            Cliente cliente,
            LocalDateTime instante) {
        validarReservasActivas(reservas, cliente, instante);
        List<ReservaAsiento> copia = List.copyOf(reservas);
        disponibilidadService.cancelarReservasAtomicas(copia, cliente, instante);
        eliminarGrupo(copia);
    }

    /**
     * Después de un pago aprobado, sustituye las reservas por las entradas
     * emitidas. Las entradas pasan a ser las que mantienen ocupados los asientos.
     */
    public synchronized void convertirReservasEnEntradas(
            Collection<ReservaAsiento> reservas,
            Collection<Entrada> entradas,
            Cliente cliente,
            LocalDateTime instante) {
        validarReservasActivas(reservas, cliente, instante);
        List<ReservaAsiento> copia = List.copyOf(reservas);
        disponibilidadService.convertirReservasEnEntradas(
                copia, entradas, cliente, instante);
        eliminarGrupo(copia);
    }

    /** Procesa vencimientos; las reservas expiradas dejan de bloquear asientos. */
    public synchronized int procesarExpiraciones(LocalDateTime instante) {
        Objects.requireNonNull(instante, "El instante es obligatorio.");
        int cantidad = disponibilidadService.expirarReservas(instante);

        List<String> gruposTerminados = reservasPorGrupo.entrySet().stream()
                .filter(entry -> entry.getValue().stream().allMatch(reserva ->
                        !reserva.estaVigente(instante)))
                .map(Map.Entry::getKey)
                .toList();

        for (String codigoGrupo : gruposTerminados) {
            List<ReservaAsiento> grupo = reservasPorGrupo.remove(codigoGrupo);
            if (grupo != null) {
                grupo.forEach(reserva ->
                        grupoPorCodigoReserva.remove(reserva.getCodigo()));
            }
        }
        return cantidad;
    }

    public LocalDateTime consultarFechaExpiracion(
            Collection<ReservaAsiento> reservas) {
        List<ReservaAsiento> copia = List.copyOf(reservas);
        if (copia.isEmpty()) {
            throw new IllegalArgumentException("No hay reservas seleccionadas.");
        }
        return copia.get(0).getFechaExpiracion();
    }

    public long consultarMinutosRestantes(
            Collection<ReservaAsiento> reservas,
            LocalDateTime instante) {
        validarReservasDelGrupoSinExpirar(reservas);
        LocalDateTime expiracion = consultarFechaExpiracion(reservas);
        if (!expiracion.isAfter(instante)) {
            return 0;
        }
        return Math.max(1, Duration.between(instante, expiracion).toMinutes());
    }

    public int consultarDuracionMinutos() {
        return Math.toIntExact(vigencia.toMinutes());
    }

    private void validarReservasDelGrupoSinExpirar(
            Collection<ReservaAsiento> reservas) {
        List<ReservaAsiento> copia = List.copyOf(reservas);
        List<ReservaAsiento> grupo = obtenerGrupoRegistrado(copia);
        if (grupo.size() != copia.size() || !copia.containsAll(grupo)) {
            throw new IllegalArgumentException(
                    "La selección no corresponde al grupo original de reservas.");
        }
    }

    private List<ReservaAsiento> obtenerGrupoRegistrado(
            List<ReservaAsiento> seleccion) {
        if (seleccion.isEmpty()) {
            throw new IllegalArgumentException("No hay reservas seleccionadas.");
        }
        String codigoGrupo = grupoPorCodigoReserva.get(
                seleccion.get(0).getCodigo());
        if (codigoGrupo == null) {
            throw new IllegalStateException(
                    "La reserva ya no está activa o fue procesada.");
        }
        List<ReservaAsiento> grupo = reservasPorGrupo.get(codigoGrupo);
        if (grupo == null) {
            throw new IllegalStateException(
                    "No se encontró el grupo de reservas.");
        }
        return grupo;
    }

    private void eliminarGrupo(Collection<ReservaAsiento> reservas) {
        if (reservas.isEmpty()) {
            return;
        }
        String codigoGrupo = grupoPorCodigoReserva.remove(
                reservas.iterator().next().getCodigo());
        if (codigoGrupo != null) {
            List<ReservaAsiento> grupo = reservasPorGrupo.remove(codigoGrupo);
            if (grupo != null) {
                grupo.forEach(reserva ->
                        grupoPorCodigoReserva.remove(reserva.getCodigo()));
            }
        }
    }
}
