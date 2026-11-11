package co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.observer;

import co.edu.uniquindio.proyectofinal.model.interfaces.CompraEventListener;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Observador que conserva un historial de las compras confirmadas.
 */
public class HistorialCompraObserver implements CompraEventListener {

    private final List<String> eventos = new ArrayList<>();

    @Override
    public synchronized void actualizar(Compra compra) {
        Objects.requireNonNull(compra, "La compra no puede ser nula.");

        String evento = LocalDateTime.now()
                + " | Compra: " + compra.getCodigo()
                + " | Cliente: " + compra.getCliente().getId()
                + " | Estado: " + compra.consultarNombreEstado()
                + " | Total: $" + compra.getTotal();

        eventos.add(evento);
    }

    public synchronized List<String> consultarHistorial() {
        return List.copyOf(eventos);
    }

    public synchronized void limpiarHistorial() {
        eventos.clear();
    }
}
