package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.interfaces.CompraEventListener;

import co.edu.uniquindio.proyectofinal.model.principal.Cliente;

import co.edu.uniquindio.proyectofinal.model.principal.Compra;



import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;



/** Recibe eventos de compra y conserva notificaciones para cada cliente. */

public class NotificacionService implements CompraEventListener {



    private final Map<String, List<String>> notificacionesPorCliente = new LinkedHashMap<>();



    @Override

    public void actualizar(Compra compra) {

        if (compra == null) return;

        registrarNotificacion(compra.getCliente(),

                "Tu compra " + compra.getCodigo() + " fue confirmada por $" + compra.getTotal());

    }



    public void registrarNotificacion(Cliente cliente, String mensaje) {

        if (cliente == null || mensaje == null || mensaje.isBlank()) {

            throw new IllegalArgumentException("El cliente y el mensaje son obligatorios.");

        }

        String contenido = LocalDateTime.now() + " - " + mensaje.trim();

        notificacionesPorCliente.computeIfAbsent(cliente.getId(), id -> new ArrayList<>()).add(contenido);

    }



    public List<String> consultarNotificaciones(Cliente cliente) {

        if (cliente == null) return List.of();

        return List.copyOf(notificacionesPorCliente.getOrDefault(cliente.getId(), List.of()));

    }



    public void limpiarNotificaciones(Cliente cliente) {

        if (cliente != null) notificacionesPorCliente.remove(cliente.getId());

    }

}