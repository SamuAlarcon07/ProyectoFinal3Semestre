package co.edu.uniquindio.proyectofinal.model.interfaces;



import co.edu.uniquindio.proyectofinal.model.principal.Compra;



/** Contrato Observer para recibir eventos relacionados con compras. */

public interface CompraEventListener {

    void actualizar(Compra compra);

}