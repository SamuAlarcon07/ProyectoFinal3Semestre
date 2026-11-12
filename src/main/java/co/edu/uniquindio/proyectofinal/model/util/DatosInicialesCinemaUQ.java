package co.edu.uniquindio.proyectofinal.model.util;

import co.edu.uniquindio.proyectofinal.model.principal.Pelicula;
import co.edu.uniquindio.proyectofinal.model.service.AdministracionCatalogoService;

import java.util.List;
import java.util.Objects;

/**
 * Carga un catálogo inicial de películas de demostración para CinemaUQ.
 * La carga es idempotente: no registra dos veces los mismos códigos.
 */
public final class DatosInicialesCinemaUQ {

    private DatosInicialesCinemaUQ() {
        // Clase de utilidad: no requiere instancias.
    }

    /**
     * Registra películas iniciales cuando aún no existen en el catálogo.
     * Las películas quedan activas al construirse mediante Pelicula.
     *
     * @param catalogo servicio compartido de administración del catálogo
     * @return cantidad de películas nuevas que fueron registradas
     */
    public static int cargarPeliculas(AdministracionCatalogoService catalogo) {
        Objects.requireNonNull(catalogo, "El catálogo es obligatorio.");

        List<Pelicula> peliculasIniciales = List.of(
                new Pelicula(
                        "PEL-001",
                        "Interestelar",
                        "Un grupo de exploradores viaja más allá de los límites conocidos para buscar un futuro para la humanidad, mientras una familia intenta mantener su vínculo a través del tiempo y la distancia.",
                        169,
                        "12+",
                        "Ciencia ficción"
                ),
                new Pelicula(
                        "PEL-002",
                        "El origen",
                        "Un especialista en infiltrarse en los sueños recibe una misión extraordinaria: implantar una idea en la mente de una persona. Para lograrlo deberá construir varios niveles de sueños y distinguir la realidad de la ilusión.",
                        148,
                        "12+",
                        "Ciencia ficción"
                ),
                new Pelicula(
                        "PEL-003",
                        "El caballero de la noche",
                        "Batman se enfrenta a un criminal que busca sumir Ciudad Gótica en el caos y poner a prueba los límites morales de sus habitantes y de quienes intentan protegerlos.",
                        152,
                        "13+",
                        "Acción"
                ),
                new Pelicula(
                        "PEL-004",
                        "Coco",
                        "Miguel sueña con convertirse en músico y, durante una celebración familiar, llega a la Tierra de los Muertos. Allí descubre secretos sobre su familia y el valor de recordar a quienes amamos.",
                        105,
                        "7+",
                        "Animación"
                ),
                new Pelicula(
                        "PEL-005",
                        "Spider-Man: Un nuevo universo",
                        "Miles Morales descubre sus habilidades arácnidas y conoce a otros Spider-Man de diferentes universos. Juntos deberán aprender a trabajar en equipo para proteger sus realidades.",
                        117,
                        "7+",
                        "Animación"
                ),
                new Pelicula(
                        "PEL-006",
                        "Duna: Parte uno",
                        "Paul Atreides llega al planeta desértico Arrakis, un territorio decisivo para el futuro de las grandes casas. Allí deberá enfrentarse a intrigas políticas y descubrir el alcance de su destino.",
                        155,
                        "12+",
                        "Ciencia ficción"
                ),
                new Pelicula(
                        "PEL-007",
                        "Misión rescate",
                        "Tras quedar aislado en Marte, un astronauta debe utilizar sus conocimientos científicos y todos los recursos disponibles para sobrevivir mientras un equipo busca la manera de traerlo de regreso.",
                        144,
                        "12+",
                        "Aventura"
                ),
                new Pelicula(
                        "PEL-008",
                        "Parásitos",
                        "Dos familias de realidades económicas muy distintas comienzan a relacionarse de una manera inesperada. Una serie de decisiones revela tensiones sociales y secretos que cambian sus vidas.",
                        132,
                        "15+",
                        "Suspenso"
                ),
                new Pelicula(
                        "PEL-009",
                        "Jurassic Park",
                        "Un parque temático poblado por dinosaurios recreados mediante ingeniería genética recibe a sus primeros visitantes. Un fallo de seguridad convierte la visita en una lucha por sobrevivir.",
                        127,
                        "12+",
                        "Aventura"
                ),
                new Pelicula(
                        "PEL-010",
                        "Whiplash: Música y obsesión",
                        "Un joven baterista ingresa a un exigente conservatorio y queda bajo la dirección de un profesor cuya búsqueda de la excelencia pone a prueba los límites físicos y emocionales de sus estudiantes.",
                        107,
                        "15+",
                        "Drama"
                )
        );

        int registradas = 0;
        for (Pelicula pelicula : peliculasIniciales) {
            if (catalogo.buscarPelicula(pelicula.getCodigo()).isEmpty()) {
                catalogo.registrarPelicula(pelicula);
                registradas++;
            }
        }
        return registradas;
    }
}
