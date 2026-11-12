package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.interfaces.CompraEventListener;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;
import co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator.ElementoVendibleDecorador;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;
import co.edu.uniquindio.proyectofinal.model.principal.DetalleCompra;
import co.edu.uniquindio.proyectofinal.model.principal.Entrada;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

/**
 * Observador que registra automáticamente en ControlAccesoService
 * las entradas emitidas al confirmar una compra.
 */
public class ControlAccesoCompraObserver implements CompraEventListener {

    private final ControlAccesoService controlAccesoService;

    public ControlAccesoCompraObserver(ControlAccesoService controlAccesoService) {
        this.controlAccesoService = Objects.requireNonNull(controlAccesoService);
    }

    @Override
    public void actualizar(Compra compra) {
        Objects.requireNonNull(compra, "La compra es obligatoria.");

        for (DetalleCompra detalle : compra.getDetalles()) {
            ElementoVendible elemento = desenvolver(detalle.getElemento());
            if (elemento instanceof Entrada entrada) {
                controlAccesoService.registrarEntrada(entrada);
            }
        }
    }

    private ElementoVendible desenvolver(ElementoVendible elemento) {
        ElementoVendible actual = Objects.requireNonNull(elemento);
        Set<ElementoVendible> visitados =
                Collections.newSetFromMap(new IdentityHashMap<>());
        while (actual instanceof ElementoVendibleDecorador decorador) {
            if (!visitados.add(actual)) {
                throw new IllegalStateException("Se detectó un ciclo en los decoradores.");
            }
            actual = decorador.obtenerElementoOriginal();
        }
        return actual;
    }
}
