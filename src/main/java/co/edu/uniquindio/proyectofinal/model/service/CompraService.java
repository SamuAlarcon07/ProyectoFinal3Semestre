package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.interfaces.CompraEventListener;

import co.edu.uniquindio.proyectofinal.model.principal.Compra;

import co.edu.uniquindio.proyectofinal.model.principal.TarjetaVirtual;



import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Objects;

import java.util.HashSet;

import java.util.Set;



/** Coordina el flujo de compra, pago, inventario, puntos y notificaciones. */

public class CompraService {



    private final PagoService pagoService;

    private final InventarioService inventarioService;

    private final PuntosService puntosService;

    private final Map<String, Compra> compras = new LinkedHashMap<>();

    private final List<CompraEventListener> observadores = new ArrayList<>();

    private final Set<String> comprasConfirmadas = new HashSet<>();



    public CompraService(PagoService pagoService, InventarioService inventarioService,

                         PuntosService puntosService) {

        this.pagoService = Objects.requireNonNull(pagoService);

        this.inventarioService = Objects.requireNonNull(inventarioService);

        this.puntosService = Objects.requireNonNull(puntosService);

    }



    public void registrarCompra(Compra compra) {

        Objects.requireNonNull(compra);

        if (compras.putIfAbsent(compra.getCodigo(), compra) != null) {

            throw new IllegalArgumentException("Ya existe una compra con ese código.");

        }

    }



    public Compra confirmarCompra(Compra compra, TarjetaVirtual tarjeta) {

        Objects.requireNonNull(compra);

        if (comprasConfirmadas.contains(compra.getCodigo())) {

            throw new IllegalStateException("La compra ya fue confirmada.");

        }

        if (compra.getDetalles().isEmpty()) throw new IllegalStateException("La compra no contiene elementos.");

        if (!inventarioService.validarDisponibilidadCompra(compra)) {

            throw new IllegalStateException("No hay existencias suficientes para completar la compra.");

        }

        pagoService.procesarPago(compra, tarjeta);

        compra.confirmarPago();

        inventarioService.registrarVentaCompra(compra);

        puntosService.acumularPorCompra(compra);

        compras.putIfAbsent(compra.getCodigo(), compra);

        comprasConfirmadas.add(compra.getCodigo());

        notificarObservadores(compra);

        return compra;

    }



    public void agregarObservador(CompraEventListener observador) {

        Objects.requireNonNull(observador);

        if (!observadores.contains(observador)) observadores.add(observador);

    }



    public void eliminarObservador(CompraEventListener observador) {

        observadores.remove(observador);

    }



    public List<Compra> consultarCompras() {

        return List.copyOf(compras.values());

    }



    public List<Compra> consultarComprasCliente(String idCliente) {

        return compras.values().stream()

                .filter(c -> c.getCliente().getId().equals(idCliente)).toList();

    }



    private void notificarObservadores(Compra compra) {

        for (CompraEventListener observador : List.copyOf(observadores)) {

            observador.actualizar(compra);

        }

    }

}