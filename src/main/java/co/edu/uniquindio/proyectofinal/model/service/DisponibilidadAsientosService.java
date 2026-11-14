package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;
import co.edu.uniquindio.proyectofinal.model.enums.EstadoReserva;
import co.edu.uniquindio.proyectofinal.model.principal.*;

import java.time.LocalDateTime;
import java.util.*;

/** Consulta y controla la disponibilidad de asientos por función. */
public class DisponibilidadAsientosService {

    private final List<ReservaAsiento> reservas = new ArrayList<>();
    private final List<Entrada> entradas = new ArrayList<>();

    /**
     * Asientos ocupados de demostración, identificados por función y asiento.
     * No representan compras reales ni generan registros contables; sirven para
     * mostrar disponibilidad realista en los datos iniciales de la aplicación.
     */
    private final Set<String> asientosOcupadosDemostracion = new HashSet<>();

    public synchronized List<Asiento> consultarAsientosDisponibles(
            Funcion funcion, LocalDateTime instante) {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");
        expirarReservas(instante);

        return funcion.getSala().getAsientos().stream()
                .filter(Asiento::isActivo)
                .filter(asiento -> estaDisponible(funcion, asiento, instante))
                .toList();
    }

    public synchronized boolean estaDisponible(
            Funcion funcion, Asiento asiento, LocalDateTime instante) {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(asiento, "El asiento es obligatorio.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");
        expirarReservas(instante);

        if (!funcion.getSala().contieneAsiento(asiento) || !asiento.isActivo()) {
            return false;
        }

        if (asientosOcupadosDemostracion.contains(claveAsiento(funcion, asiento))) {
            return false;
        }

        boolean tieneReserva = reservas.stream().anyMatch(reserva ->
                reserva.getFuncion().getCodigo().equals(funcion.getCodigo())
                        && mismoAsiento(reserva.getAsiento(), asiento)
                        && (reserva.getEstado() == EstadoReserva.CONFIRMADA
                        || reserva.estaVigente(instante)));

        boolean tieneEntrada = entradas.stream().anyMatch(entrada ->
                entrada.getFuncion().getCodigo().equals(funcion.getCodigo())
                        && mismoAsiento(entrada.getAsiento(), asiento)
                        && entrada.getEstado() != EstadoEntrada.ANULADA);

        return !tieneReserva && !tieneEntrada;
    }

    /**
     * Marca asientos como ocupados en una función de demostración.
     *
     * La ocupación es independiente por función: el asiento B4 puede estar
     * ocupado en una función y disponible en otra sesión de la misma sala.
     * La operación es idempotente y no crea compras ni entradas ficticias.
     *
     * @param funcion función en la que se desea simular ocupación
     * @param identificadores identificadores como A1, B4 o H10
     * @return cantidad de ocupaciones nuevas registradas
     */
    public synchronized int marcarAsientosOcupadosDemostracion(
            Funcion funcion,
            Collection<String> identificadores) {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(identificadores,
                "La lista de asientos ocupados es obligatoria.");

        if (identificadores.isEmpty()) {
            return 0;
        }

        Map<String, Asiento> asientosPorMarcar = new LinkedHashMap<>();
        for (String identificador : identificadores) {
            if (identificador == null || identificador.isBlank()) {
                throw new IllegalArgumentException(
                        "Los identificadores de asiento no pueden estar vacíos.");
            }

            String normalizado = identificador.trim().toUpperCase(Locale.ROOT);
            Asiento asiento = funcion.getSala().getAsientos().stream()
                    .filter(Asiento::isActivo)
                    .filter(actual -> actual.obtenerIdentificador()
                            .equalsIgnoreCase(normalizado))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No existe el asiento " + normalizado
                                    + " en la sala de la función " + funcion.getCodigo() + "."));

            String clave = claveAsiento(funcion, asiento);
            if (asientosPorMarcar.putIfAbsent(clave, asiento) != null) {
                throw new IllegalArgumentException(
                        "El asiento " + normalizado + " está repetido en la lista.");
            }
        }

        int nuevas = 0;
        LocalDateTime ahora = LocalDateTime.now();
        for (Map.Entry<String, Asiento> entrada : asientosPorMarcar.entrySet()) {
            String clave = entrada.getKey();

            if (asientosOcupadosDemostracion.contains(clave)) {
                continue;
            }

            // Si una compra o reserva real ya ocupa el asiento, se respeta
            // ese estado y no se duplica como ocupación de demostración.
            if (!estaDisponible(funcion, entrada.getValue(), ahora)) {
                continue;
            }

            asientosOcupadosDemostracion.add(clave);
            nuevas++;
        }

        return nuevas;
    }

    /** Indica si la ocupación proviene específicamente de los datos demo. */
    public synchronized boolean estaOcupadoPorDemostracion(
            Funcion funcion, Asiento asiento) {
        Objects.requireNonNull(funcion, "La función es obligatoria.");
        Objects.requireNonNull(asiento, "El asiento es obligatorio.");
        return asientosOcupadosDemostracion.contains(claveAsiento(funcion, asiento));
    }

    /** Registra una reserva individual. */
    public synchronized void registrarReserva(ReservaAsiento reserva,
                                              LocalDateTime instante) {
        registrarReservasAtomicas(List.of(Objects.requireNonNull(reserva)), instante);
    }

    /**
     * Registra todas las reservas de una selección de asientos como una sola
     * operación. Si un asiento no está disponible, no se registra ninguno.
     */
    public synchronized void registrarReservasAtomicas(
            Collection<ReservaAsiento> nuevasReservas,
            LocalDateTime instante) {
        Objects.requireNonNull(nuevasReservas,
                "La colección de reservas es obligatoria.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");
        expirarReservas(instante);

        List<ReservaAsiento> copia = List.copyOf(nuevasReservas);
        if (copia.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debes seleccionar al menos un asiento.");
        }

        Set<String> codigos = new HashSet<>();
        Set<String> asientosDelGrupo = new HashSet<>();

        for (ReservaAsiento reserva : copia) {
            Objects.requireNonNull(reserva, "No se permiten reservas nulas.");

            if (reserva.getEstado() != EstadoReserva.ACTIVA
                    || !reserva.estaVigente(instante)) {
                throw new IllegalStateException(
                        "La reserva nueva no está vigente.");
            }

            if (!codigos.add(reserva.getCodigo())
                    || reservas.stream().anyMatch(actual ->
                    actual.getCodigo().equals(reserva.getCodigo()))) {
                throw new IllegalArgumentException(
                        "Ya existe una reserva con ese código.");
            }

            String identificador = claveAsiento(
                    reserva.getFuncion(), reserva.getAsiento());
            if (!asientosDelGrupo.add(identificador)) {
                throw new IllegalArgumentException(
                        "La selección contiene el mismo asiento más de una vez.");
            }

            if (!reserva.getFuncion().puedeVenderEntradas(instante)) {
                throw new IllegalStateException(
                        "La función ya no está disponible para la venta.");
            }

            if (!estaDisponible(reserva.getFuncion(), reserva.getAsiento(), instante)) {
                throw new IllegalStateException(
                        "El asiento " + reserva.getAsiento().obtenerIdentificador()
                                + " ya no está disponible.");
            }
        }

        reservas.addAll(copia);
    }

    /** Comprueba que todas las reservas siguen activas y pertenecen al cliente. */
    public synchronized void validarReservasActivas(
            Collection<ReservaAsiento> reservasAValidar,
            Cliente cliente,
            LocalDateTime instante) {
        Objects.requireNonNull(reservasAValidar,
                "La colección de reservas es obligatoria.");
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");
        expirarReservas(instante);

        List<ReservaAsiento> copia = List.copyOf(reservasAValidar);
        if (copia.isEmpty()) {
            throw new IllegalArgumentException("No hay reservas para validar.");
        }

        Set<String> codigos = new HashSet<>();
        for (ReservaAsiento reserva : copia) {
            Objects.requireNonNull(reserva, "No se permiten reservas nulas.");

            if (!codigos.add(reserva.getCodigo())) {
                throw new IllegalArgumentException(
                        "La reserva contiene elementos duplicados.");
            }
            boolean registrada = reservas.stream().anyMatch(actual -> actual == reserva);
            if (!registrada) {
                throw new IllegalStateException(
                        "Una de las reservas ya no está registrada.");
            }
            if (!reserva.getCliente().getId().equals(cliente.getId())) {
                throw new SecurityException(
                        "La reserva no pertenece al cliente autenticado.");
            }
            if (reserva.getEstado() != EstadoReserva.ACTIVA
                    || !reserva.estaVigente(instante)) {
                throw new IllegalStateException(
                        "La reserva del asiento "
                                + reserva.getAsiento().obtenerIdentificador()
                                + " expiró o dejó de estar activa.");
            }
        }
    }

    /** Cancela un grupo completo de reservas activas. */
    public synchronized void cancelarReservasAtomicas(
            Collection<ReservaAsiento> reservasACancelar,
            Cliente cliente,
            LocalDateTime instante) {
        validarReservasActivas(reservasACancelar, cliente, instante);
        for (ReservaAsiento reserva : reservasACancelar) {
            reserva.cancelar();
        }
    }

    /**
     * Convierte las reservas activas en entradas emitidas después de que
     * CompraService confirma el pago. La conversión es atómica para el inventario
     * de asientos: primero valida todo y luego sustituye reservas por entradas.
     */
    public synchronized void convertirReservasEnEntradas(
            Collection<ReservaAsiento> reservasAConvertir,
            Collection<Entrada> nuevasEntradas,
            Cliente cliente,
            LocalDateTime instante) {
        Objects.requireNonNull(nuevasEntradas,
                "La colección de entradas es obligatoria.");
        validarReservasActivas(reservasAConvertir, cliente, instante);

        List<ReservaAsiento> reservasCopia = List.copyOf(reservasAConvertir);
        List<Entrada> entradasCopia = List.copyOf(nuevasEntradas);

        if (reservasCopia.size() != entradasCopia.size()) {
            throw new IllegalArgumentException(
                    "Debe existir una entrada por cada asiento reservado.");
        }

        Map<String, ReservaAsiento> reservasPorAsiento = new HashMap<>();
        for (ReservaAsiento reserva : reservasCopia) {
            String clave = claveAsiento(reserva.getFuncion(), reserva.getAsiento());
            if (reservasPorAsiento.putIfAbsent(clave, reserva) != null) {
                throw new IllegalArgumentException(
                        "Hay reservas duplicadas para el mismo asiento.");
            }
        }

        Set<String> codigosEntrada = new HashSet<>();
        for (Entrada entrada : entradasCopia) {
            Objects.requireNonNull(entrada, "No se permiten entradas nulas.");

            if (entrada.getEstado() != EstadoEntrada.EMITIDA) {
                throw new IllegalStateException(
                        "Solo se pueden registrar entradas emitidas.");
            }
            if (!codigosEntrada.add(entrada.getCodigo())
                    || entradas.stream().anyMatch(actual ->
                    actual.getCodigo().equals(entrada.getCodigo()))) {
                throw new IllegalArgumentException(
                        "El código de entrada ya está registrado.");
            }
            if (!entrada.getFuncion().puedeVenderEntradas(instante)) {
                throw new IllegalStateException(
                        "La función ya no está disponible para la venta.");
            }

            String clave = claveAsiento(entrada.getFuncion(), entrada.getAsiento());
            ReservaAsiento reserva = reservasPorAsiento.remove(clave);
            if (reserva == null
                    || reserva.getFuncion() != entrada.getFuncion()
                    || reserva.getAsiento() != entrada.getAsiento()) {
                throw new IllegalArgumentException(
                        "La entrada no corresponde a uno de los asientos reservados.");
            }
        }

        if (!reservasPorAsiento.isEmpty()) {
            throw new IllegalArgumentException(
                    "Faltan entradas para algunos asientos reservados.");
        }

        // Todas las validaciones terminaron. Se ejecutan las transiciones.
        for (ReservaAsiento reserva : reservasCopia) {
            reserva.confirmar(instante);
        }
        reservas.removeAll(reservasCopia);
        entradas.addAll(entradasCopia);
    }

    /** Compatibilidad con el registro individual anterior. */
    public synchronized void registrarEntrada(Entrada entrada) {
        registrarEntradas(List.of(Objects.requireNonNull(entrada)), LocalDateTime.now());
    }

    /** Valida un grupo completo de entradas y lo registra de forma conjunta. */
    public synchronized void registrarEntradas(Collection<Entrada> nuevasEntradas,
                                               LocalDateTime instante) {
        Objects.requireNonNull(nuevasEntradas,
                "La colección de entradas es obligatoria.");
        Objects.requireNonNull(instante, "El instante es obligatorio.");
        List<Entrada> copia = List.copyOf(nuevasEntradas);
        Set<String> codigos = new HashSet<>();
        Set<String> asientosDelGrupo = new HashSet<>();

        for (Entrada entrada : copia) {
            Objects.requireNonNull(entrada, "No se permiten entradas nulas.");
            if (!codigos.add(entrada.getCodigo())) {
                throw new IllegalArgumentException(
                        "El grupo contiene códigos de entrada duplicados.");
            }
            if (entradas.stream().anyMatch(actual ->
                    actual.getCodigo().equals(entrada.getCodigo()))) {
                throw new IllegalArgumentException("La entrada ya está registrada.");
            }
            if (entrada.getEstado() != EstadoEntrada.EMITIDA) {
                throw new IllegalStateException(
                        "Solo se pueden registrar entradas emitidas.");
            }
            if (!entrada.getFuncion().puedeVenderEntradas(instante)) {
                throw new IllegalStateException(
                        "La función no está disponible para la venta.");
            }
            String identificadorAsiento = claveAsiento(
                    entrada.getFuncion(), entrada.getAsiento());
            if (!asientosDelGrupo.add(identificadorAsiento)) {
                throw new IllegalArgumentException(
                        "El grupo intenta registrar dos entradas para el mismo asiento.");
            }
            if (!estaDisponible(entrada.getFuncion(), entrada.getAsiento(), instante)) {
                throw new IllegalStateException(
                        "El asiento ya no está disponible para esta función.");
            }
        }
        entradas.addAll(copia);
    }

    /** Elimina solo las instancias de entrada indicadas, como compensación. */
    public synchronized void eliminarEntradasRegistradas(
            Collection<Entrada> entradasAEliminar) {
        Objects.requireNonNull(entradasAEliminar,
                "La colección de entradas es obligatoria.");
        Set<Entrada> referencias = java.util.Collections.newSetFromMap(
                new java.util.IdentityHashMap<>());
        referencias.addAll(entradasAEliminar);
        entradas.removeIf(referencias::contains);
    }

    /** Marca como expiradas las reservas cuyo tiempo terminó. */
    public synchronized int expirarReservas(LocalDateTime instante) {
        Objects.requireNonNull(instante, "El instante es obligatorio.");
        int expiradas = 0;
        for (ReservaAsiento reserva : reservas) {
            if (reserva.getEstado() == EstadoReserva.ACTIVA
                    && !reserva.estaVigente(instante)) {
                reserva.expirar(instante);
                expiradas++;
            }
        }
        return expiradas;
    }

    private String claveAsiento(Funcion funcion, Asiento asiento) {
        return funcion.getCodigo() + "|"
                + asiento.getFila().toUpperCase(java.util.Locale.ROOT)
                + "|" + asiento.getNumero();
    }

    private boolean mismoAsiento(Asiento primero, Asiento segundo) {
        return primero.getFila().equalsIgnoreCase(segundo.getFila())
                && primero.getNumero() == segundo.getNumero();
    }
}
