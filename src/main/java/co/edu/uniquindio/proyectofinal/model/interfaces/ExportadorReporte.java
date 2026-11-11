package co.edu.uniquindio.proyectofinal.model.interfaces;



import java.io.IOException;

import java.nio.file.Path;



/** Contrato común para exportar reportes a distintos formatos. */

public interface ExportadorReporte {

    void exportar(String contenido, Path rutaDestino) throws IOException;

}

