package co.edu.uniquindio.proyectofinal.model.patrones.estructurales.adapter.externo;



import java.io.IOException;

import java.nio.charset.StandardCharsets;

import java.nio.file.Files;

import java.nio.file.Path;

import java.nio.file.StandardOpenOption;



/**

 * Representa una API heredada de escritura de archivos.

 * Recibe primero la ruta como String y después el contenido.

 */

public class BibliotecaTextoLegacy {



    /**

     * Guarda el contenido indicado en la ruta recibida como texto UTF-8.

     *

     * @param rutaDestino ruta del archivo como texto

     * @param contenido contenido que se escribirá

     * @throws IOException si ocurre un error al crear o escribir el archivo

     */

    public void guardarArchivo(String rutaDestino, String contenido)

            throws IOException {



        Path ruta = Path.of(rutaDestino);

        Path directorio = ruta.toAbsolutePath().getParent();



        if (directorio != null) {

            Files.createDirectories(directorio);

        }



        Files.writeString(

                ruta,

                contenido,

                StandardCharsets.UTF_8,

                StandardOpenOption.CREATE,

                StandardOpenOption.TRUNCATE_EXISTING,

                StandardOpenOption.WRITE

        );

    }

}