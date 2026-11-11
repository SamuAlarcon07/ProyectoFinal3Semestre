package co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state;

import co.edu.uniquindio.proyectofinal.model.interfaces.EstadoCompra;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;

import java.util.Objects;

/** Estado de una compra pagada. */
public class CompraPagada implements EstadoCompra {

    @Override
    public void pagar(Compra compra) {
        throw new IllegalStateException(
                "La compra ya se encuentra pagada."
        );
    }

    @Override
    public void cancelar(Compra compra) {
        Objects.requireNonNull(compra);
        compra.cambiarEstado(new CompraCancelada());
    }

    @Override
    public void reembolsar(Compra compra) {
        throw new IllegalStateException(
                "La compra debe pasar por el proceso de cancelación antes del reembolso."
        );
    }

    @Override
    public String obtenerNombre() {
        return "PAGADA";
    }
}
