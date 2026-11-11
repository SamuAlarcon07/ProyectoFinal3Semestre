package co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state;

import co.edu.uniquindio.proyectofinal.model.interfaces.EstadoCompra;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;

/** Estado final de una compra reembolsada. */
public class CompraReembolsada implements EstadoCompra {

    @Override
    public void pagar(Compra compra) {
        throw new IllegalStateException(
                "Una compra reembolsada no puede pagarse nuevamente."
        );
    }

    @Override
    public void cancelar(Compra compra) {
        throw new IllegalStateException(
                "Una compra reembolsada no puede cancelarse nuevamente."
        );
    }

    @Override
    public void reembolsar(Compra compra) {
        throw new IllegalStateException(
                "La compra ya fue reembolsada."
        );
    }

    @Override
    public String obtenerNombre() {
        return "REEMBOLSADA";
    }
}
