package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.principal.Compra;

import co.edu.uniquindio.proyectofinal.model.principal.Promocion;



import java.math.BigDecimal;

import java.time.LocalDate;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Optional;



/** Registra promociones y calcula descuentos sobre las compras. */

public class PromocionService {



    private final Map<String, Promocion> promociones = new LinkedHashMap<>();



    public void registrarPromocion(Promocion promocion) {

        if (promocion == null) throw new IllegalArgumentException("La promoción es obligatoria.");

        if (promociones.putIfAbsent(promocion.getCodigo(), promocion) != null) {

            throw new IllegalArgumentException("Ya existe una promoción con ese código.");

        }

    }



    public Optional<Promocion> buscarPromocion(String codigo) {

        return Optional.ofNullable(promociones.get(codigo));

    }



    public BigDecimal calcularDescuento(String codigo, BigDecimal subtotal, LocalDate fecha) {

        Promocion promocion = promociones.get(codigo);

        if (promocion == null) throw new IllegalArgumentException("Promoción no encontrada.");

        return promocion.calcularDescuento(subtotal, fecha);

    }



    /** Aplica una única promoción al total de la compra; reemplaza el descuento anterior. */

    public BigDecimal aplicarPromocion(String codigo, Compra compra, LocalDate fecha) {

        if (compra == null) throw new IllegalArgumentException("La compra es obligatoria.");

        if (compra.getDescuentos().compareTo(BigDecimal.ZERO) > 0) {

            throw new IllegalStateException("La compra ya tiene un descuento aplicado.");

        }

        BigDecimal descuento = calcularDescuento(codigo, compra.calcularSubtotal(), fecha);

        compra.aplicarDescuentos(descuento);

        return descuento;

    }



    public List<Promocion> consultarPromociones() {

        return List.copyOf(promociones.values());

    }

}