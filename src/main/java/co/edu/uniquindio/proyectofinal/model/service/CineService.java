package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.principal.Cine;

import co.edu.uniquindio.proyectofinal.model.principal.Sala;



import java.util.List;

import java.util.Objects;

import java.util.Optional;



/** Coordina las operaciones generales de la empresa CinemaUQ. */

public class CineService {



    private final Cine cine;



    public CineService(Cine cine) {

        this.cine = Objects.requireNonNull(cine, "El cine es obligatorio.");

    }



    public Cine consultarInformacion() {

        return cine;

    }



    public void actualizarInformacion(String razonSocial, String nombreComercial,

                                      String direccion, String telefono,

                                      String correoElectronico, String paginaWeb) {

        cine.actualizarInformacion(razonSocial, nombreComercial, direccion,

                telefono, correoElectronico, paginaWeb);

    }



    public void registrarSala(Sala sala) {

        verificarCineActivo();

        cine.registrarSala(sala);

    }



    public Optional<Sala> buscarSala(String codigo) {

        return cine.buscarSala(codigo);

    }



    public List<Sala> consultarSalas() {

        return cine.getSalas();

    }



    public void activarCine() {

        cine.activar();

    }



    public void desactivarCine() {

        cine.desactivar();

    }



    private void verificarCineActivo() {

        if (!cine.isActivo()) {

            throw new IllegalStateException("CinemaUQ está inactivo.");

        }

    }

}