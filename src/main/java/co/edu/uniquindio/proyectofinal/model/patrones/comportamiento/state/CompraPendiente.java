package co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state;

import co.edu.uniquindio.proyectofinal.model.interfaces.EstadoCompra;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;

import java.util.Objects;

/** Estado inicial de una compra sin pagar. */
public class CompraPendiente implements EstadoCompra {

    @Override
    public void pagar(Compra compra) {
        Objects.requireNonNull(compra);
        compra.cambiarEstado(new CompraPagada());
    }

    @Override
    public void cancelar(Compra compra) {
        Objects.requireNonNull(compra);
        compra.cambiarEstado(new CompraCancelada());
    }

    @Override
    public void reembolsar(Compra compra) {
        throw new IllegalStateException(
                "No se puede reembolsar una compra que todavía no ha sido pagada."
        );
    }

    @Override
    public String obtenerNombre() {
        return "PENDIENTE";
    }
}
