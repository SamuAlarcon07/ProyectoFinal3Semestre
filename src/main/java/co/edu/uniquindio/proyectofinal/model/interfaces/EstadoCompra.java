package co.edu.uniquindio.proyectofinal.model.interfaces;

import co.edu.uniquindio.proyectofinal.model.principal.Compra;

/** Contrato del patrón State para las operaciones permitidas en una compra. */
public interface EstadoCompra {
    void pagar(Compra compra);
    void cancelar(Compra compra);
    void reembolsar(Compra compra);
    String obtenerNombre();
}