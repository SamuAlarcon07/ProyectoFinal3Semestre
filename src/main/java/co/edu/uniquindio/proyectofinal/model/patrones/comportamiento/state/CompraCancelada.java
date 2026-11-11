package co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state;

import co.edu.uniquindio.proyectofinal.model.interfaces.EstadoCompra;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;

import java.util.Objects;

/** Estado de una compra cancelada. */
public class CompraCancelada implements EstadoCompra {

    @Override
    public void pagar(Compra compra) {
        throw new IllegalStateException(
                "No se puede pagar una compra cancelada."
        );
    }

    @Override
    public void cancelar(Compra compra) {
        throw new IllegalStateException(
                "La compra ya se encuentra cancelada."
        );
    }

    @Override
    public void reembolsar(Compra compra) {
        Objects.requireNonNull(compra);
        compra.cambiarEstado(new CompraReembolsada());
    }

    @Override
    public String obtenerNombre() {
        return "CANCELADA";
    }
}
