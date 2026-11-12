package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada;
import co.edu.uniquindio.proyectofinal.model.enums.EstadoFuncion;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;
import co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator.ElementoVendibleDecorador;
import co.edu.uniquindio.proyectofinal.model.principal.Cliente;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;
import co.edu.uniquindio.proyectofinal.model.principal.Entrada;
import co.edu.uniquindio.proyectofinal.model.principal.Pelicula;
import co.edu.uniquindio.proyectofinal.model.principal.ResenaPelicula;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Valida y administra las reseñas de películas realizadas por clientes.
 * También carga opiniones de demostración para que el catálogo no aparezca vacío.
 */
public class ResenaService {

    private final Map<String, ResenaPelicula> resenas = new LinkedHashMap<>();

    /**
     * Publica una reseña real únicamente si el cliente tiene una compra PAGADA
     * que incluya una entrada no anulada para una función finalizada de la película.
     */
    public synchronized ResenaPelicula publicarResena(
            Cliente cliente, Pelicula pelicula, int calificacion,
            String comentario, List<Compra> comprasCliente) {

        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        Objects.requireNonNull(pelicula, "La película es obligatoria.");
        Objects.requireNonNull(comprasCliente, "El historial de compras es obligatorio.");

        boolean puedeResenar = comprasCliente.stream()
                .filter(Objects::nonNull)
                .filter(compra -> compra.getCliente() != null
                        && Objects.equals(compra.getCliente().getId(), cliente.getId()))
                .filter(compra -> "PAGADA".equals(compra.consultarNombreEstado()))
                .flatMap(compra -> compra.getDetalles().stream())
                .map(detalle -> desenvolver(detalle.getElemento()))
                .filter(Entrada.class::isInstance)
                .map(Entrada.class::cast)
                .anyMatch(entrada -> entrada.getFuncion() != null
                        && entrada.getFuncion().getPelicula() != null
                        && Objects.equals(
                        entrada.getFuncion().getPelicula().getCodigo(),
                        pelicula.getCodigo())
                        && entrada.getFuncion().getEstado() == EstadoFuncion.FINALIZADA
                        && entrada.getEstado() != EstadoEntrada.ANULADA);

        if (!puedeResenar) {
            throw new IllegalStateException(
                    "El cliente debe tener una compra pagada con una entrada válida "
                            + "para una función finalizada de esta película.");
        }

        String clave = clave(cliente, pelicula);
        if (resenas.containsKey(clave)) {
            throw new IllegalStateException(
                    "El cliente ya publicó una reseña para esta película.");
        }

        ResenaPelicula resena = new ResenaPelicula(
                UUID.randomUUID().toString(), cliente, pelicula,
                calificacion, comentario);
        resenas.put(clave, resena);
        return resena;
    }

    /**
     * Agrega reseñas ficticias de demostración para cada película activa.
     * Estas opiniones solo sirven para poblar la interfaz; no representan compras
     * ni habilitan a ningún cliente real para publicar una reseña.
     *
     * La clave independiente DEMO evita colisiones con las reseñas reales.
     * El método es idempotente: no duplica las opiniones si se invoca dos veces.
     */
    public synchronized int cargarResenasDemostracion(List<Pelicula> peliculas) {
        Objects.requireNonNull(peliculas, "La lista de películas es obligatoria.");

        String[][] opiniones = {
                {
                        "La historia me dejó pensando durante días. La música y la fotografía hacen que cada escena se sienta enorme.",
                        "Una película emocionante y visualmente impresionante. Vale muchísimo la pena verla en pantalla grande.",
                        "Tiene momentos muy emotivos y una historia que conecta la ciencia ficción con los sentimientos."
                },
                {
                        "Me encantó cómo juega con la realidad y los sueños. Cada detalle parece tener un propósito.",
                        "Es una película que exige atención y recompensa con una historia muy bien construida.",
                        "La banda sonora y el montaje mantienen la tensión de principio a fin."
                },
                {
                        "Una gran película de superhéroes, con personajes complejos y un villano inolvidable.",
                        "La ambientación de Ciudad Gótica y la tensión de la historia son excelentes.",
                        "No se queda solo en la acción: plantea decisiones difíciles y momentos memorables."
                },
                {
                        "Una historia muy bonita sobre la familia, la memoria y perseguir los sueños.",
                        "La música y los colores son maravillosos. Es divertida y también muy conmovedora.",
                        "Me hizo reír y llorar. Una película para disfrutar en familia."
                },
                {
                        "La animación tiene muchísimo estilo y la historia transmite un mensaje inspirador.",
                        "Es dinámica, divertida y visualmente diferente. Miles es un protagonista genial.",
                        "Una aventura llena de energía, humor y corazón."
                },
                {
                        "El universo visual es impresionante y la construcción del mundo es muy interesante.",
                        "Una historia de ciencia ficción que se toma su tiempo para construir tensión y personajes.",
                        "La fotografía, el sonido y la escala de Arrakis se disfrutan mucho en el cine."
                },
                {
                        "Una aventura intensa que combina ciencia, supervivencia y mucho humor.",
                        "El protagonista hace que una situación imposible resulte entretenida y emocionante.",
                        "Me gustó cómo muestra la perseverancia y la creatividad para resolver problemas."
                },
                {
                        "Una historia inesperada que mezcla humor negro, tensión y crítica social.",
                        "Cada escena aporta algo. Es una película que sorprende y deja mucho para conversar.",
                        "La dirección y las actuaciones son excelentes. Muy recomendable."
                },
                {
                        "Un clásico de aventuras que sigue funcionando. La música y los dinosaurios son inolvidables.",
                        "Tiene suspenso, aventura y escenas que se disfrutan incluso después de tantos años.",
                        "Una película entretenida de principio a fin, ideal para verla en pantalla grande."
                },
                {
                        "La música y el ritmo de la película transmiten una tensión impresionante.",
                        "Es intensa y tiene actuaciones muy buenas. El final se queda contigo.",
                        "Una historia sobre la exigencia, la disciplina y los límites personales."
                }
        };

        String[][] autores = {
                {"Laura Méndez", "Andrés Rojas", "Valentina Cruz"},
                {"Mateo Gómez", "Camila Torres", "Daniela Ruiz"},
                {"Santiago López", "Mariana Castro", "Felipe Vargas"},
                {"Isabella Ramírez", "Juan Esteban Mora", "Sara Cárdenas"},
                {"Nicolás Herrera", "Lucía Martínez", "David Salazar"},
                {"Gabriela Ortiz", "Tomás Restrepo", "Manuela Gil"},
                {"Samuel Pardo", "Juliana Ríos", "Esteban Molina"},
                {"Sofía Quintero", "Miguel Ángel Peña", "Paula Duque"},
                {"Alejandro Vélez", "Natalia Franco", "Emilio Arias"},
                {"Daniela Ospina", "Sebastián Marín", "Laura Valencia"}
        };

        int[][] calificaciones = {
                {5, 5, 4}, {5, 4, 5}, {5, 5, 4}, {5, 5, 5}, {5, 4, 5},
                {4, 5, 4}, {5, 4, 5}, {5, 5, 4}, {5, 4, 5}, {5, 5, 4}
        };

        Map<String, Integer> indicePorCodigo = Map.of(
                "PEL-001", 0, "PEL-002", 1, "PEL-003", 2, "PEL-004", 3,
                "PEL-005", 4, "PEL-006", 5, "PEL-007", 6, "PEL-008", 7,
                "PEL-009", 8, "PEL-010", 9
        );

        int agregadas = 0;
        for (Pelicula pelicula : peliculas) {
            if (pelicula == null) {
                continue;
            }

            Integer indice = indicePorCodigo.get(pelicula.getCodigo());
            if (indice == null) {
                continue;
            }

            for (int i = 0; i < 3; i++) {
                String idDemo = "DEMO-RESENA-" + pelicula.getCodigo() + "-" + (i + 1);
                String claveDemo = idDemo + "::" + pelicula.getCodigo();

                if (resenas.containsKey(claveDemo)) {
                    continue;
                }

                Cliente autor = new Cliente(
                        idDemo,
                        autores[indice][i] + " · Comunidad CinemaUQ",
                        "resena-" + pelicula.getCodigo().toLowerCase() + "-" + (i + 1)
                                + "@demo.cinemauq.local",
                        "HASH-DEMO-NO-UTILIZABLE"
                );

                ResenaPelicula resena = new ResenaPelicula(
                        "RESENA-DEMO-" + pelicula.getCodigo() + "-" + (i + 1),
                        autor,
                        pelicula,
                        calificaciones[indice][i],
                        opiniones[indice][i]
                );

                resenas.put(claveDemo, resena);
                agregadas++;
            }
        }

        return agregadas;
    }

    public synchronized void editarResena(
            Cliente cliente, Pelicula pelicula,
            int calificacion, String comentario) {
        obtener(cliente, pelicula).editarResena(calificacion, comentario);
    }

    public synchronized void ocultarResena(Cliente cliente, Pelicula pelicula) {
        obtener(cliente, pelicula).ocultar();
    }

    public synchronized List<ResenaPelicula> consultarResenas(Pelicula pelicula) {
        Objects.requireNonNull(pelicula, "La película es obligatoria.");
        return resenas.values().stream()
                .filter(resena -> Objects.equals(
                        resena.getPelicula().getCodigo(), pelicula.getCodigo()))
                .filter(ResenaPelicula::estaVisible)
                .toList();
    }

    public synchronized double calcularPromedio(Pelicula pelicula) {
        List<ResenaPelicula> visibles = consultarResenas(pelicula);
        return visibles.stream()
                .mapToInt(ResenaPelicula::getCalificacion)
                .average()
                .orElse(0.0);
    }

    private ResenaPelicula obtener(Cliente cliente, Pelicula pelicula) {
        String clave = clave(cliente, pelicula);
        return Optional.ofNullable(resenas.get(clave))
                .orElseThrow(() -> new IllegalArgumentException(
                        "No se encontró la reseña del cliente para esta película."));
    }

    private String clave(Cliente cliente, Pelicula pelicula) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        Objects.requireNonNull(pelicula, "La película es obligatoria.");
        return cliente.getId() + "::" + pelicula.getCodigo();
    }

    /** Retira uno o varios decoradores sin perder el tipo real del producto. */
    private ElementoVendible desenvolver(ElementoVendible elemento) {
        ElementoVendible actual = Objects.requireNonNull(
                elemento, "El elemento de compra es obligatorio.");
        Set<ElementoVendible> visitados = Collections.newSetFromMap(
                new IdentityHashMap<>());

        while (actual instanceof ElementoVendibleDecorador decorador) {
            if (!visitados.add(actual)) {
                throw new IllegalStateException(
                        "Se detectó un ciclo en los decoradores del elemento vendible.");
            }
            actual = decorador.obtenerElementoOriginal();
        }
        return actual;
    }
}
