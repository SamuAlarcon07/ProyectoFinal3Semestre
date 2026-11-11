package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;

import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import co.edu.uniquindio.proyectofinal.model.principal.Sala;

import co.edu.uniquindio.proyectofinal.model.principal.TipoSala;



import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;



/** Administra tipos de sala, registra salas y valida cambios de formato. */

public class GestionSalasService {



    private final CineService cineService;

    private final Map<String, TipoSala> tiposSala = new LinkedHashMap<>();



    public GestionSalasService(CineService cineService) {

        this.cineService = java.util.Objects.requireNonNull(cineService);

    }



    public void registrarTipoSala(TipoSala tipoSala) {

        if (tipoSala == null) throw new IllegalArgumentException("El tipo de sala es obligatorio.");

        if (tiposSala.putIfAbsent(tipoSala.getCodigo(), tipoSala) != null) {

            throw new IllegalArgumentException("Ya existe un tipo de sala con ese código.");

        }

    }



    public void registrarSala(Sala sala) {

        if (sala == null) throw new IllegalArgumentException("La sala es obligatoria.");

        TipoSala tipoRegistrado = tiposSala.get(sala.getTipoSala().getCodigo());

        if (tipoRegistrado == null || !tipoRegistrado.isActivo()) {

            throw new IllegalArgumentException("El tipo de sala no está registrado o está inactivo.");

        }

        cineService.registrarSala(sala);

    }



    public void cambiarTipoSala(Sala sala, TipoSala nuevoTipo, List<Funcion> funciones) {

        if (sala == null || nuevoTipo == null) throw new IllegalArgumentException("La sala y el tipo son obligatorios.");

        if (!nuevoTipo.isActivo() || !tiposSala.containsKey(nuevoTipo.getCodigo())) {

            throw new IllegalArgumentException("El nuevo tipo de sala debe estar registrado y activo.");

        }

        boolean incompatible = funciones.stream()

                .filter(f -> f.getSala().getCodigo().equals(sala.getCodigo()))

                .filter(f -> f.getEstado() == EstadoFuncion.PROGRAMADA)

                .anyMatch(f -> !nuevoTipo.permiteFormato(f.getFormatoProyeccion()));

        if (incompatible) throw new IllegalStateException("El nuevo tipo no es compatible con alguna función programada.");

        sala.cambiarTipoSala(nuevoTipo);

    }



    public List<TipoSala> consultarTiposSala() {

        return List.copyOf(tiposSala.values());

    }

}