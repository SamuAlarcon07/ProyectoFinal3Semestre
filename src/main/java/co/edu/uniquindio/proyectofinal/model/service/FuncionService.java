package co.edu.uniquindio.proyectofinal.model.service;


import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;
import co.edu.uniquindio.proyectofinal.model.principal.BloqueoSala;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import java.time.LocalDateTime;
import java.util.*;


/** Programa y administra las funciones cinematográficas. */

public class FuncionService {



    private static final int MINUTOS_LIMPIEZA = 20;

    private final Map<String, Funcion> funciones = new LinkedHashMap<>();

    private final List<BloqueoSala> bloqueos = new ArrayList<>();



    /**
     * Valida que un bloqueo no interrumpa una función que ya está programada.
     * Se ejecuta antes de persistir el bloqueo en los servicios relacionados.
     */
    public void validarBloqueo(BloqueoSala bloqueo) {
        if (bloqueo == null) {
            throw new IllegalArgumentException("El bloqueo es obligatorio.");
        }

        LocalDateTime inicioBloqueo = bloqueo.getFechaInicio();
        LocalDateTime finBloqueo = bloqueo.getFechaFin();

        Optional<Funcion> funcionEnConflicto = funciones.values().stream()
                .filter(funcion -> funcion.getEstado() == EstadoFuncion.PROGRAMADA)
                .filter(funcion -> funcion.getSala().getCodigo()
                        .equals(bloqueo.getSala().getCodigo()))
                .filter(funcion -> {
                    LocalDateTime inicioFuncion = funcion.getFechaHora();
                    LocalDateTime finFuncion = inicioFuncion.plusMinutes(
                            funcion.getPelicula().getDuracionMinutos() + MINUTOS_LIMPIEZA);
                    return inicioBloqueo.isBefore(finFuncion)
                            && finBloqueo.isAfter(inicioFuncion);
                })
                .findFirst();

        if (funcionEnConflicto.isPresent()) {
            Funcion funcion = funcionEnConflicto.get();
            throw new IllegalStateException(
                    "No se puede bloquear la sala: el intervalo se cruza con la función "
                            + funcion.getCodigo() + " (" + funcion.getPelicula().getTitulo() + ").");
        }
    }

    public void registrarBloqueo(BloqueoSala bloqueo) {
        validarBloqueo(bloqueo);
        bloqueos.add(bloqueo);
    }



    public void registrarFuncion(Funcion funcion) {

        if (funcion == null) throw new IllegalArgumentException("La función es obligatoria.");

        LocalDateTime inicio = funcion.getFechaHora();

        if (!inicio.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException("La función debe programarse para una fecha y hora futuras.");

        }

        LocalDateTime fin = inicio.plusMinutes(

                funcion.getPelicula().getDuracionMinutos() + MINUTOS_LIMPIEZA);

        boolean bloqueada = bloqueos.stream().anyMatch(b ->

                b.getSala().getCodigo().equals(funcion.getSala().getCodigo())

                        && b.seSuperponeCon(inicio, fin));

        if (bloqueada) throw new IllegalStateException("La sala tiene un bloqueo en ese intervalo.");



        boolean conflicto = funciones.values().stream()

                .filter(f -> f.getSala().getCodigo().equals(funcion.getSala().getCodigo()))

                .filter(f -> f.getEstado() == EstadoFuncion.PROGRAMADA)

                .anyMatch(f -> {

                    LocalDateTime otroInicio = f.getFechaHora();

                    LocalDateTime otroFin = otroInicio.plusMinutes(

                            f.getPelicula().getDuracionMinutos() + MINUTOS_LIMPIEZA);

                    return inicio.isBefore(otroFin) && fin.isAfter(otroInicio);

                });

        if (conflicto) throw new IllegalStateException("La sala ya tiene otra función en ese horario.");

        if (funciones.putIfAbsent(funcion.getCodigo(), funcion) != null) {

            throw new IllegalArgumentException("Ya existe una función con ese código.");

        }

    }



    public Optional<Funcion> buscarFuncion(String codigo) {

        return Optional.ofNullable(funciones.get(codigo));

    }



    public void cancelarFuncion(String codigo) {

        obtenerFuncion(codigo).cancelar();

    }



    public void finalizarFuncion(String codigo) {

        obtenerFuncion(codigo).finalizar();

    }



    public List<Funcion> consultarFunciones() {

        return List.copyOf(funciones.values());

    }



    private Funcion obtenerFuncion(String codigo) {

        Funcion funcion = funciones.get(codigo);

        if (funcion == null) throw new IllegalArgumentException("No existe la función indicada.");

        return funcion;

    }

}