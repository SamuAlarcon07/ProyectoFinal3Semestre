package co.edu.uniquindio.proyectofinal.model.patrones.estructurales.adapter;



import co.edu.uniquindio.proyectofinal.model.interfaces.ExportadorReporte;

import co.edu.uniquindio.proyectofinal.model.patrones.estructurales.adapter.externo.BibliotecaTextoLegacy;



import java.io.IOException;

import java.nio.file.Path;

import java.util.Objects;



/**

 * Adaptador que permite utilizar BibliotecaTextoLegacy mediante el contrato

 * ExportadorReporte que conoce ReporteService.

 */

public class ExportadorTextoAdapter implements ExportadorReporte {



    private final BibliotecaTextoLegacy biblioteca;



    /** Crea el adaptador con la implementación heredada. */

    public ExportadorTextoAdapter() {

        this(new BibliotecaTextoLegacy());

    }



    /**

     * Permite inyectar la implementación de la API heredada.

     * @param biblioteca biblioteca que realizará la escritura

     */

    public ExportadorTextoAdapter(BibliotecaTextoLegacy biblioteca) {

        this.biblioteca = Objects.requireNonNull(

                biblioteca,

                "La biblioteca de texto es obligatoria."

        );

    }



    /**

     * Adapta (contenido, Path) a (ruta String, contenido).

     */

    @Override

    public void exportar(String contenido, Path rutaDestino)

            throws IOException {



        Objects.requireNonNull(contenido, "El contenido no puede ser nulo.");

        Objects.requireNonNull(rutaDestino, "La ruta de destino es obligatoria.");



        biblioteca.guardarArchivo(rutaDestino.toString(), contenido);

    }

}