package co.edu.uniquindio.proyectofinal.model.interfaces;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Define el contrato para exportar los reportes
 * generados por el sistema CinemaUQ.
 *
 * Las implementaciones de esta interfaz permitirán
 * utilizar diferentes formatos de exportación.
 *
 * @author Equipo CinemaUQ
 */
public interface ExportadorReporte {

    /**
     * Exporta el contenido de un reporte a un archivo.
     *
     * @param contenido información que contiene el reporte.
     * @param rutaDestino ubicación donde se guardará el archivo.
     * @throws IOException si ocurre un error durante la exportación.
     */
    void exportar(String contenido, Path rutaDestino)
            throws IOException;

}
