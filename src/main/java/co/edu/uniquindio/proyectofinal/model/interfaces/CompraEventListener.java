package co.edu.uniquindio.proyectofinal.model.interfaces;

import co.edu.uniquindio.proyectofinal.model.principal.Compra;

/**
 * Define el contrato para los componentes que desean
 * recibir notificaciones sobre eventos de compra
 * dentro del sistema CinemaUQ.
 *
 * @author Equipo CinemaUQ
 */
public interface CompraEventListener {

    /**
     * Se ejecuta cuando se notifica un evento relacionado
     * con la confirmación de una compra.
     *
     * @param compra compra que generó el evento.
     */
    void actualizar(Compra compra);

}
