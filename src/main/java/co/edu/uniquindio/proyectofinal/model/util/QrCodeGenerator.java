
package co.edu.uniquindio.proyectofinal.model.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.util.EnumMap;
import java.util.Map;

/**
 * Genera imágenes QR reales para las entradas de CinemaUQ.
 */
public final class QrCodeGenerator {

    private QrCodeGenerator() {
    }

    public static WritableImage generar(String contenido, int dimension) {

        if (contenido == null || contenido.isBlank()) {
            throw new IllegalArgumentException(
                    "El contenido del QR es obligatorio."
            );
        }

        if (dimension < 120) {
            throw new IllegalArgumentException(
                    "La dimensión debe ser de al menos 120 píxeles."
            );
        }

        try {
            Map<EncodeHintType, Object> configuracion =
                    new EnumMap<>(EncodeHintType.class);

            configuracion.put(EncodeHintType.MARGIN, 2);
            configuracion.put(
                    EncodeHintType.ERROR_CORRECTION,
                    ErrorCorrectionLevel.M
            );

            BitMatrix matriz = new MultiFormatWriter().encode(
                    contenido.trim(),
                    BarcodeFormat.QR_CODE,
                    dimension,
                    dimension,
                    configuracion
            );

            WritableImage imagen =
                    new WritableImage(dimension, dimension);

            PixelWriter escritor = imagen.getPixelWriter();

            for (int y = 0; y < dimension; y++) {
                for (int x = 0; x < dimension; x++) {

                    int color = matriz.get(x, y)
                            ? 0xFF000000
                            : 0xFFFFFFFF;

                    escritor.setArgb(x, y, color);
                }
            }

            return imagen;

        } catch (Exception e) {
            throw new IllegalStateException(
                    "No se pudo generar el código QR.",
                    e
            );
        }
    }
}
