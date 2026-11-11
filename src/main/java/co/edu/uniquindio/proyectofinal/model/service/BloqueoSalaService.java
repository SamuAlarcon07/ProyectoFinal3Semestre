package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.principal.BloqueoSala;



import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.List;



/** Programa y consulta los bloqueos por mantenimiento, limpieza o reparación. */

public class BloqueoSalaService {



    private final List<BloqueoSala> bloqueos = new ArrayList<>();



    public void registrarBloqueo(BloqueoSala nuevo) {

        if (nuevo == null) throw new IllegalArgumentException("El bloqueo es obligatorio.");

        boolean superpuesto = bloqueos.stream().anyMatch(actual ->

                actual.getSala().getCodigo().equals(nuevo.getSala().getCodigo())

                        && actual.seSuperponeCon(nuevo.getFechaInicio(), nuevo.getFechaFin()));

        if (superpuesto) throw new IllegalStateException("Ya existe un bloqueo que se superpone en esa sala.");

        bloqueos.add(nuevo);

    }



    public boolean estaBloqueadaEn(String codigoSala, LocalDateTime instante) {

        return bloqueos.stream().anyMatch(b -> b.getSala().getCodigo().equals(codigoSala)

                && b.estaVigente(instante));

    }



    public List<BloqueoSala> consultarBloqueosSala(String codigoSala) {

        return bloqueos.stream().filter(b -> b.getSala().getCodigo().equals(codigoSala)).toList();

    }



    public void cancelarBloqueo(String codigo) {

        bloqueos.stream().filter(b -> b.getCodigo().equals(codigo)).findFirst()

                .orElseThrow(() -> new IllegalArgumentException("Bloqueo no encontrado."))

                .cancelar();

    }



    public List<BloqueoSala> consultarTodos() {

        return List.copyOf(bloqueos);

    }

}