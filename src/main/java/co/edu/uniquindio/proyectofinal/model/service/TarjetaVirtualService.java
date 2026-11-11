package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.principal.Administrador;

import co.edu.uniquindio.proyectofinal.model.principal.Cliente;

import co.edu.uniquindio.proyectofinal.model.principal.TarjetaVirtual;



import java.math.BigDecimal;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Optional;



/** Administra el registro y las operaciones de las tarjetas virtuales. */

public class TarjetaVirtualService {



    private final Map<String, TarjetaVirtual> tarjetasPorNumero = new LinkedHashMap<>();



    public void registrarTarjeta(TarjetaVirtual tarjeta) {

        if (tarjeta == null) throw new IllegalArgumentException("La tarjeta es obligatoria.");

        if (tarjetasPorNumero.containsKey(tarjeta.getNumero())) {

            throw new IllegalArgumentException("Ya existe una tarjeta con ese número.");

        }

        boolean titularConTarjeta = tarjetasPorNumero.values().stream()

                .anyMatch(actual -> actual.getTitular().getId().equals(tarjeta.getTitular().getId()));

        if (titularConTarjeta) {

            throw new IllegalArgumentException("El cliente ya tiene una tarjeta virtual registrada.");

        }

        tarjetasPorNumero.put(tarjeta.getNumero(), tarjeta);

    }



    public Optional<TarjetaVirtual> buscarTarjeta(String numero) {

        return Optional.ofNullable(tarjetasPorNumero.get(numero));

    }



    public Optional<TarjetaVirtual> buscarTarjetaPorCliente(Cliente cliente) {

        if (cliente == null) return Optional.empty();

        return tarjetasPorNumero.values().stream()

                .filter(t -> t.getTitular().getId().equals(cliente.getId()))

                .findFirst();

    }



    public void recargar(String numero, BigDecimal valor, Administrador administrador) {

        obtenerTarjeta(numero).recargar(valor, administrador);

    }



    public void bloquear(String numero) {

        obtenerTarjeta(numero).bloquear();

    }



    public void activar(String numero) {

        obtenerTarjeta(numero).activar();

    }



    public List<TarjetaVirtual> consultarTarjetas() {

        return List.copyOf(tarjetasPorNumero.values());

    }



    private TarjetaVirtual obtenerTarjeta(String numero) {

        TarjetaVirtual tarjeta = tarjetasPorNumero.get(numero);

        if (tarjeta == null) throw new IllegalArgumentException("No existe la tarjeta indicada.");

        return tarjeta;

    }

}