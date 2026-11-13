package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.controller.CarteleraController;
import co.edu.uniquindio.proyectofinal.controller.CinemaOperationsController;
import co.edu.uniquindio.proyectofinal.controller.ReservaController;
import co.edu.uniquindio.proyectofinal.model.principal.Asiento;
import co.edu.uniquindio.proyectofinal.model.principal.Cliente;
import co.edu.uniquindio.proyectofinal.model.principal.Compra;
import co.edu.uniquindio.proyectofinal.model.principal.ReservaAsiento;
import co.edu.uniquindio.proyectofinal.model.principal.TarjetaVirtual;
import co.edu.uniquindio.proyectofinal.model.principal.Funcion;
import co.edu.uniquindio.proyectofinal.model.principal.Pelicula;
import co.edu.uniquindio.proyectofinal.model.principal.PeliculaGuardada;
import co.edu.uniquindio.proyectofinal.model.enums.TipoListaPelicula;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.text.NumberFormat;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.net.URL;
import java.util.Map;
import java.util.Optional;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.TreeMap;

/**
 * Construye la pantalla de cartelera sin añadir otro FXML.
 * La vista se inserta dentro del contenido central del Dashboard.
 */
public class CarteleraViewController {

    private static final Locale LOCALE = new Locale("es", "CO");
    private static final String RUTA_PORTADAS =
            "/co/edu/uniquindio/proyectofinal/images/";
    private static final Map<String, String> PORTADAS_POR_CODIGO = Map.of(
            "PEL-001", "Interstellar.jpg",
            "PEL-002", "Inception.jpg",
            "PEL-003", "Batman.jpg",
            "PEL-004", "Coco.jpg",
            "PEL-005", "SpiderVerse.jpg",
            "PEL-006", "Duna.jpg",
            "PEL-007", "TheMartian.jpg",
            "PEL-008", "Parasite.jpg",
            "PEL-009", "JurassicPark.jpg",
            "PEL-010", "Whiplash.jpg"
    );
    /** Archivos de póster para estrenos futuros, ubicados en resources/images. */
    private static final String[] PORTADAS_PROXIMAMENTE = {
            "HungerGamesSunrise.jpg",
            "AvengersDoomsday.jpg",
            "DunePartThree.jpg",
            "JumanjiOpenWorld.jpg",
            "Zelda.jpg",
            "SpiderVerseBeyond.jpg"
    };

    private static final DateTimeFormatter FORMATO_FUNCION =
            DateTimeFormatter.ofPattern("EEEE d 'de' MMMM · HH:mm", LOCALE);
    private static final NumberFormat FORMATO_MONEDA =
            NumberFormat.getCurrencyInstance(LOCALE);

    private final CarteleraController carteleraController;
    private final ReservaController reservaController;
    private final CinemaOperationsController operaciones;
    private final Cliente cliente;
    private final Window ventanaPropietaria;
    private final TextField campoBusqueda;
    private final String filtroInicial;

    private FlowPane contenedorPeliculas;
    private Label etiquetaResultados;
    private ComboBox<String> filtroGenero;

    public CarteleraViewController(
            CarteleraController carteleraController,
            ReservaController reservaController,
            CinemaOperationsController operaciones,
            Cliente cliente,
            Window ventanaPropietaria,
            TextField campoBusqueda,
            String filtroInicial
    ) {
        this.carteleraController = carteleraController;
        this.reservaController = reservaController;
        this.operaciones = operaciones;
        this.cliente = cliente;
        this.ventanaPropietaria = ventanaPropietaria;
        this.campoBusqueda = campoBusqueda;
        this.filtroInicial = filtroInicial == null ? "" : filtroInicial.trim();
    }

    /** Construye el contenido de la sección Cartelera. */
    public Node construirVista() {
        VBox principal = new VBox(18);
        principal.getStyleClass().add("dashboard-main");

        VBox encabezado = new VBox(6);
        Label titulo = new Label("Cartelera");
        titulo.getStyleClass().add("page-title");
        Label subtitulo = new Label(
                "Explora las películas disponibles, consulta sus horarios y elige tus asientos.");
        subtitulo.getStyleClass().add("page-subtitle");
        subtitulo.setWrapText(true);
        encabezado.getChildren().addAll(titulo, subtitulo);

        HBox filtros = construirFiltros();
        etiquetaResultados = new Label();
        etiquetaResultados.getStyleClass().add("catalog-result-count");

        contenedorPeliculas = new FlowPane();
        contenedorPeliculas.setHgap(16);
        contenedorPeliculas.setVgap(18);
        contenedorPeliculas.getStyleClass().add("catalog-movies-flow");

        principal.getChildren().addAll(encabezado, filtros, etiquetaResultados,
                new Separator(), contenedorPeliculas,
                construirSeccionProximamente());
        refrescarPeliculas();
        return principal;
    }

    private HBox construirFiltros() {
        HBox filtros = new HBox(12);
        filtros.setAlignment(Pos.CENTER_LEFT);
        filtros.getStyleClass().add("catalog-filter-bar");

        Label etiqueta = new Label("Género");
        etiqueta.getStyleClass().add("field-label");

        filtroGenero = new ComboBox<>();
        filtroGenero.getItems().add("Todos");
        filtroGenero.getItems().addAll(carteleraController.consultarGeneros());
        filtroGenero.setValue("Todos");
        filtroGenero.setPromptText("Selecciona un género");
        filtroGenero.getStyleClass().add("catalog-genre-combo");
        filtroGenero.setOnAction(event -> refrescarPeliculas());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Button limpiar = new Button("Limpiar filtros");
        limpiar.getStyleClass().add("button-small-outline");
        limpiar.setOnAction(event -> {
            filtroGenero.setValue("Todos");
            if (campoBusqueda != null) {
                campoBusqueda.clear();
            }
            refrescarPeliculas();
        });

        filtros.getChildren().addAll(etiqueta, filtroGenero, espacio, limpiar);
        return filtros;
    }

    private void refrescarPeliculas() {
        if (contenedorPeliculas == null || etiquetaResultados == null) {
            return;
        }

        String genero = filtroGenero == null ? "Todos" : filtroGenero.getValue();
        String texto = campoBusqueda == null
                ? filtroInicial : campoBusqueda.getText();
        List<Pelicula> peliculas = carteleraController.buscarPeliculas(
                texto, genero);

        contenedorPeliculas.getChildren().clear();
        etiquetaResultados.setText(peliculas.size()
                + (peliculas.size() == 1 ? " película encontrada" : " películas encontradas"));

        if (peliculas.isEmpty()) {
            contenedorPeliculas.getChildren().add(estadoVacio(
                    "No encontramos películas",
                    "Prueba otro género o término de búsqueda. Las películas activas registradas aparecerán aquí."
            ));
            return;
        }

        int indice = 0;
        for (Pelicula pelicula : peliculas) {
            contenedorPeliculas.getChildren().add(crearTarjetaPelicula(pelicula, indice++));
        }
    }

    private VBox crearTarjetaPelicula(Pelicula pelicula, int indice) {
        VBox tarjeta = new VBox(9);
        tarjeta.setPrefWidth(190);
        tarjeta.setMinWidth(175);
        tarjeta.setMaxWidth(210);
        tarjeta.getStyleClass().add("catalog-movie-card");

        StackPane poster = new StackPane();
        poster.setMinWidth(150);
        poster.setPrefWidth(150);
        poster.setMaxWidth(150);
        poster.setMinHeight(220);
        poster.setPrefHeight(220);
        poster.setMaxHeight(220);
        poster.getStyleClass().add("catalog-poster");

        ImageView imagenPortada = crearImagenPortada(pelicula);
        if (imagenPortada != null) {
            poster.getStyleClass().add("catalog-poster-image");
            poster.getChildren().add(imagenPortada);
        } else {
            // Las películas nuevas que no tengan una portada configurada
            // conservan el diseño genérico de CinemaUQ.
            poster.getStyleClass().add("poster-tone-" + (indice % 5));

            VBox posterText = new VBox(9);
            posterText.setAlignment(Pos.CENTER);
            posterText.setPadding(new Insets(12));

            Label sello = new Label("CINEMA UQ");
            sello.getStyleClass().add("catalog-poster-brand");
            Label simbolo = new Label("✦");
            simbolo.getStyleClass().add("catalog-poster-symbol");
            Label tituloPoster = new Label(pelicula.getTitulo());
            tituloPoster.getStyleClass().add("catalog-poster-title");
            tituloPoster.setWrapText(true);
            tituloPoster.setAlignment(Pos.CENTER);
            Label generoPoster = new Label(pelicula.getGenero().toUpperCase(LOCALE));
            generoPoster.getStyleClass().add("catalog-poster-genre");
            posterText.getChildren().addAll(sello, simbolo, tituloPoster, generoPoster);
            poster.getChildren().add(posterText);
        }

        // Acceso rápido a Favoritas desde la esquina superior derecha del póster.
        Button corazon = new Button();
        corazon.setFocusTraversable(false);
        corazon.setMinSize(34, 34);
        corazon.setPrefSize(34, 34);
        corazon.setMaxSize(34, 34);
        actualizarEstiloCorazon(corazon, pelicula);
        corazon.setOnAction(event -> alternarFavorita(pelicula, corazon));
        StackPane.setAlignment(corazon, Pos.TOP_RIGHT);
        StackPane.setMargin(corazon, new Insets(9, 9, 0, 0));
        poster.getChildren().add(corazon);

        Label titulo = new Label(pelicula.getTitulo());
        titulo.getStyleClass().add("movie-title");
        titulo.setWrapText(true);

        Label datos = new Label(pelicula.getDuracionMinutos() + " min · "
                + pelicula.getClasificacion());
        datos.getStyleClass().add("muted-small");

        Button detalles = new Button("Ver detalles y funciones  →");
        detalles.getStyleClass().add("button-small-outline");
        detalles.setMaxWidth(Double.MAX_VALUE);
        detalles.setOnAction(event -> mostrarDetallePelicula(pelicula));

        tarjeta.getChildren().addAll(poster, titulo, datos, detalles);
        return tarjeta;
    }

    /**
     * Sección editorial independiente del catálogo activo. Las fechas son
     * fechas de estreno anunciadas internacionalmente y pueden variar por país.
     */
    private VBox construirSeccionProximamente() {
        VBox seccion = new VBox(13);
        seccion.setPadding(new Insets(8, 0, 10, 0));

        HBox encabezado = new HBox(10);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        Label icono = new Label("✦");
        icono.setStyle("-fx-text-fill: #ffc52e; -fx-font-size: 20px; -fx-font-weight: bold;");

        VBox textos = new VBox(3);
        Label titulo = new Label("Próximamente");
        titulo.getStyleClass().add("section-title");
        Label subtitulo = new Label(
                "Los próximos estrenos que llegarán a la pantalla de CinemaUQ.");
        subtitulo.getStyleClass().add("page-subtitle");
        subtitulo.setWrapText(true);
        textos.getChildren().addAll(titulo, subtitulo);
        encabezado.getChildren().addAll(icono, textos);

        FlowPane tarjetas = new FlowPane();
        tarjetas.setHgap(16);
        tarjetas.setVgap(18);
        tarjetas.getStyleClass().add("catalog-movies-flow");

        List<EstrenoProximo> estrenos = List.of(
                new EstrenoProximo("The Hunger Games: Sunrise on the Reaping",
                        "20 NOV 2026", "Ciencia ficción · Acción",
                        "El regreso al universo de Panem con la historia del joven Haymitch."),
                new EstrenoProximo("Avengers: Doomsday",
                        "18 DIC 2026", "Acción · Ciencia ficción",
                        "Los héroes del universo Marvel se enfrentan a una nueva amenaza."),
                new EstrenoProximo("Dune: Part Three",
                        "18 DIC 2026", "Ciencia ficción · Aventura",
                        "La continuación de la historia de Paul Atreides."),
                new EstrenoProximo("Jumanji: Open World",
                        "25 DIC 2026", "Aventura · Fantasía",
                        "Una nueva aventura en el mundo de Jumanji."),
                new EstrenoProximo("The Legend of Zelda",
                        "30 ABR 2027", "Aventura · Fantasía",
                        "La adaptación cinematográfica del popular universo de videojuegos."),
                new EstrenoProximo("Spider-Man: Beyond the Spider-Verse",
                        "18 JUN 2027", "Animación · Acción",
                        "Miles Morales continúa su aventura a través del multiverso.")
        );

        int indice = 0;
        for (EstrenoProximo estreno : estrenos) {
            tarjetas.getChildren().add(crearTarjetaEstreno(estreno, indice++));
        }

        Label nota = new Label(
                "Fechas de estreno anunciadas internacionalmente. La programación local de CinemaUQ puede variar.");
        nota.getStyleClass().add("muted-small");
        nota.setWrapText(true);

        seccion.getChildren().addAll(new Separator(), encabezado, tarjetas, nota);
        return seccion;
    }

    /** Tarjeta visual de un estreno que todavía no está habilitado para compra. */
    private VBox crearTarjetaEstreno(EstrenoProximo estreno, int indice) {
        VBox tarjeta = new VBox(8);
        tarjeta.setPrefWidth(190);
        tarjeta.setMinWidth(175);
        tarjeta.setMaxWidth(210);
        tarjeta.setPadding(new Insets(8));
        tarjeta.setStyle(
                "-fx-background-color: #0e2034;"
                        + "-fx-background-radius: 13;"
                        + "-fx-border-color: #294760;"
                        + "-fx-border-radius: 13;"
                        + "-fx-border-width: 1;"
        );

        StackPane poster = new StackPane();
        poster.setMinSize(166, 220);
        poster.setPrefSize(166, 220);
        poster.setMaxSize(166, 220);
        poster.setStyle(
                "-fx-background-color: " + coloresEstreno(indice) + ";"
                        + "-fx-background-radius: 9;"
                        + "-fx-border-color: #52677d;"
                        + "-fx-border-radius: 9;"
        );

        VBox arte = new VBox(12);
        arte.setAlignment(Pos.CENTER);
        arte.setPadding(new Insets(14));

        Label marca = new Label("CINEMA UQ  ·  EXCLUSIVE");
        marca.setStyle(
                "-fx-text-fill: #ffd36a; -fx-font-size: 8px; -fx-font-weight: bold;"
                        + "-fx-letter-spacing: 1px;"
        );

        Label simbolo = new Label(indice == 1 ? "✦" : indice == 4 ? "⚔" : "✧");
        simbolo.setStyle(
                "-fx-text-fill: rgba(255,255,255,0.88);"
                        + "-fx-font-size: 42px; -fx-font-weight: bold;"
        );

        Label nombre = new Label(estreno.titulo);
        nombre.setWrapText(true);
        nombre.setAlignment(Pos.CENTER);
        nombre.setStyle(
                "-fx-text-fill: white; -fx-font-size: 15px;"
                        + "-fx-font-weight: bold; -fx-text-alignment: center;"
        );

        Region linea = new Region();
        linea.setPrefHeight(2);
        linea.setMaxWidth(55);
        linea.setStyle("-fx-background-color: #ffc52e;");

        arte.getChildren().addAll(marca, simbolo, nombre, linea);
        // Cargar la portada real desde los recursos del proyecto. Si aún no
        // existe, conservar el póster tipográfico para evitar tarjetas vacías.
        ImageView portadaEstreno = crearPortadaEstreno(indice);
        if (portadaEstreno != null) {
            poster.getChildren().add(portadaEstreno);
        } else {
            poster.getChildren().add(arte);
        }

        Label cinta = new Label("PRÓXIMAMENTE");
        cinta.setStyle(
                "-fx-background-color: #f2bd39;"
                        + "-fx-background-radius: 5;"
                        + "-fx-text-fill: #142235;"
                        + "-fx-font-size: 8px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 5 8;"
        );
        StackPane.setAlignment(cinta, Pos.TOP_LEFT);
        StackPane.setMargin(cinta, new Insets(8));
        poster.getChildren().add(cinta);

        Label fecha = new Label("ESTRENO · " + estreno.fecha);
        fecha.setStyle(
                "-fx-text-fill: #ffc52e; -fx-font-size: 9px; -fx-font-weight: bold;"
        );

        Label titulo = new Label(estreno.titulo);
        titulo.getStyleClass().add("movie-title");
        titulo.setWrapText(true);
        titulo.setMinHeight(Region.USE_PREF_SIZE);

        Label genero = new Label(estreno.genero);
        genero.getStyleClass().add("muted-small");
        genero.setWrapText(true);

        Label descripcion = new Label(estreno.descripcion);
        descripcion.setStyle("-fx-text-fill: #9fb4c8; -fx-font-size: 10px;");
        descripcion.setWrapText(true);
        descripcion.setMinHeight(42);

        tarjeta.getChildren().addAll(poster, fecha, titulo, genero, descripcion);
        return tarjeta;
    }

    /** Lee la portada local sin afectar la cartelera si falta un archivo. */
    private ImageView crearPortadaEstreno(int indice) {
        if (indice < 0 || indice >= PORTADAS_PROXIMAMENTE.length) {
            return null;
        }
        String ruta = RUTA_PORTADAS + PORTADAS_PROXIMAMENTE[indice];
        try (InputStream archivo = CarteleraViewController.class.getResourceAsStream(ruta)) {
            if (archivo == null) {
                return null;
            }
            Image imagen = new Image(archivo);
            if (imagen.isError()) {
                return null;
            }
            ImageView vista = new ImageView(imagen);
            vista.setFitWidth(164);
            vista.setFitHeight(218);
            vista.setPreserveRatio(false);
            vista.setSmooth(true);
            Rectangle recorte = new Rectangle(164, 218);
            recorte.setArcWidth(16);
            recorte.setArcHeight(16);
            vista.setClip(recorte);
            return vista;
        } catch (IOException | RuntimeException ex) {
            return null;
        }
    }

    private String coloresEstreno(int indice) {
        String[] fondos = {
                "linear-gradient(to bottom right, #203b50, #101827 72%)",
                "linear-gradient(to bottom right, #4b3420, #171c2c 72%)",
                "linear-gradient(to bottom right, #32435d, #101827 72%)",
                "linear-gradient(to bottom right, #285044, #101827 72%)",
                "linear-gradient(to bottom right, #4a3d67, #101827 72%)",
                "linear-gradient(to bottom right, #174b60, #101827 72%)"
        };
        return fondos[Math.floorMod(indice, fondos.length)];
    }

    private static final class EstrenoProximo {
        private final String titulo;
        private final String fecha;
        private final String genero;
        private final String descripcion;

        private EstrenoProximo(String titulo, String fecha,
                               String genero, String descripcion) {
            this.titulo = titulo;
            this.fecha = fecha;
            this.genero = genero;
            this.descripcion = descripcion;
        }
    }

    /** Comprueba si la película ya pertenece a la lista de Favoritas. */
    private boolean estaEnFavoritas(Pelicula pelicula) {
        if (operaciones == null || cliente == null) {
            return false;
        }
        return operaciones.consultarListaPersonal(cliente, TipoListaPelicula.FAVORITA)
                .stream()
                .anyMatch(guardada -> guardada.getPelicula().getCodigo()
                        .equals(pelicula.getCodigo()));
    }

    /** Actualiza el corazón según el estado persistido de la película. */
    private void actualizarEstiloCorazon(Button corazon, Pelicula pelicula) {
        boolean guardada = estaEnFavoritas(pelicula);
        corazon.setText(guardada ? "♥" : "♡");
        // El propio glifo tiene forma de corazón: no se dibuja un botón circular.
        // Se fuerza Segoe UI Symbol para evitar que el carácter desaparezca
        // por la fuente personalizada utilizada en el resto de la interfaz.
        corazon.setStyle(guardada
                ? "-fx-background-color: transparent;"
                  + "-fx-text-fill: #ffc52e; -fx-font-family: 'Segoe UI Symbol';"
                  + "-fx-font-size: 29px; -fx-font-weight: bold;"
                  + "-fx-padding: 0; -fx-cursor: hand;"
                  + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 5, 0.3, 0, 1);"
                : "-fx-background-color: transparent;"
                  + "-fx-text-fill: white; -fx-font-family: 'Segoe UI Symbol';"
                  + "-fx-font-size: 29px; -fx-font-weight: bold;"
                  + "-fx-padding: 0; -fx-cursor: hand;"
                  + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.85), 5, 0.3, 0, 1);");
        corazon.setAccessibleText(guardada
                ? "Quitar de películas favoritas" : "Agregar a películas favoritas");
    }

    /** Ejecuta un pequeño pulso al cambiar el estado de Favoritas. */
    private void animarCorazon(Button corazon, Runnable alTerminar) {
        // Reinicia la escala para que cada clic produzca la animación completa.
        corazon.setScaleX(1.0);
        corazon.setScaleY(1.0);

        ScaleTransition pulso = new ScaleTransition(
                javafx.util.Duration.millis(125), corazon);
        pulso.setToX(1.38);
        pulso.setToY(1.38);
        pulso.setAutoReverse(true);
        pulso.setCycleCount(2);
        pulso.setOnFinished(event -> {
            corazon.setScaleX(1.0);
            corazon.setScaleY(1.0);
            if (alTerminar != null) {
                // Ejecuta la alerta después de finalizar el pulso de animación.
                // Así showAndWait() no se invoca durante el procesamiento del pulso.
                Platform.runLater(alTerminar);
            }
        });
        pulso.playFromStart();
    }

    /** Agrega o quita la película de Favoritas sin duplicar registros. */
    private void alternarFavorita(Pelicula pelicula, Button corazon) {
        if (operaciones == null || cliente == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Favoritas",
                    "No se pudo acceder a tu lista", "Inicia sesión como cliente para guardar películas.");
            return;
        }
        try {
            if (estaEnFavoritas(pelicula)) {
                operaciones.quitarPeliculaGuardada(
                        cliente, pelicula, TipoListaPelicula.FAVORITA);
                actualizarEstiloCorazon(corazon, pelicula);
                animarCorazon(corazon, () -> mostrarAlertaFavorita(
                        pelicula, false));
            } else {
                operaciones.guardarPelicula(
                        cliente, pelicula, TipoListaPelicula.FAVORITA);
                actualizarEstiloCorazon(corazon, pelicula);
                animarCorazon(corazon, () -> mostrarAlertaFavorita(
                        pelicula, true));
            }
        } catch (RuntimeException ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Favoritas",
                    "No se pudo actualizar tu lista",
                    ex.getMessage() == null ? "Inténtalo nuevamente." : ex.getMessage());
            actualizarEstiloCorazon(corazon, pelicula);
        }
    }

    /**
     * Carga la portada local de una película de demostración.
     * Devuelve null cuando no existe una imagen asignada, para conservar
     * el diseño genérico como respaldo.
     */
    private ImageView crearImagenPortada(Pelicula pelicula) {
        String nombreArchivo = PORTADAS_POR_CODIGO.get(pelicula.getCodigo());
        if (nombreArchivo == null) {
            return null;
        }

        try (InputStream recurso = CarteleraViewController.class.getResourceAsStream(
                RUTA_PORTADAS + nombreArchivo)) {
            if (recurso == null) {
                return null;
            }

            Image imagen = new Image(recurso);
            if (imagen.isError()) {
                return null;
            }

            ImageView vista = new ImageView(imagen);
            vista.setFitWidth(146);
            vista.setFitHeight(216);
            vista.setPreserveRatio(false);
            vista.setSmooth(true);

            Rectangle recorte = new Rectangle(146, 216);
            recorte.setArcWidth(12);
            recorte.setArcHeight(12);
            vista.setClip(recorte);
            return vista;
        } catch (IOException | RuntimeException ex) {
            return null;
        }
    }

    private void mostrarDetallePelicula(Pelicula pelicula) {
        Dialog<Void> dialogo = new Dialog<>();
        prepararDialogo(dialogo, "Detalles de la película", pelicula.getTitulo(), 710, 640);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox contenido = new VBox(13);
        contenido.setPadding(new Insets(16));
        contenido.getStyleClass().add("catalog-dialog-content");

        Label genero = etiquetaDato("GÉNERO", pelicula.getGenero());
        Label duracion = etiquetaDato("DURACIÓN", pelicula.getDuracionMinutos() + " minutos");
        Label clasificacion = etiquetaDato("CLASIFICACIÓN", pelicula.getClasificacion());
        Label codigo = etiquetaDato("CÓDIGO", pelicula.getCodigo());

        HBox metadatos = new HBox(10,
                tarjetaDato(genero), tarjetaDato(duracion),
                tarjetaDato(clasificacion), tarjetaDato(codigo));

        Label tituloSinopsis = new Label("Sinopsis");
        tituloSinopsis.getStyleClass().add("section-title");
        Label sinopsis = new Label(pelicula.getSinopsis());
        sinopsis.setWrapText(true);
        sinopsis.getStyleClass().add("catalog-synopsis");

        Label tituloFunciones = new Label("Próximas funciones");
        tituloFunciones.getStyleClass().add("section-title");
        VBox funciones = new VBox(9);
        List<Funcion> funcionesDisponibles =
                carteleraController.consultarFunciones(pelicula.getCodigo());

        if (funcionesDisponibles.isEmpty()) {
            funciones.getChildren().add(estadoVacio(
                    "No hay funciones programadas",
                    "Esta película está activa, pero todavía no tiene horarios futuros registrados."
            ));
        } else {
            for (Funcion funcion : funcionesDisponibles) {
                funciones.getChildren().add(crearFilaFuncion(funcion, dialogo));
            }
        }

        contenido.getChildren().addAll(metadatos, tituloSinopsis, sinopsis,
                new Separator(), tituloFunciones, funciones);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(480);
        scroll.getStyleClass().add("catalog-dialog-scroll");
        dialogo.getDialogPane().setContent(scroll);
        dialogo.showAndWait();
    }

    private HBox crearFilaFuncion(Funcion funcion, Dialog<?> dialogoDetalles) {
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("catalog-function-row");

        VBox informacion = new VBox(5);
        Label fecha = new Label(FORMATO_FUNCION.format(funcion.getFechaHora()));
        fecha.getStyleClass().add("function-meta");
        Label sala = new Label("Sala " + funcion.getSala().getNombre()
                + " · " + formatoProyeccion(funcion));
        sala.getStyleClass().add("muted-small");
        informacion.getChildren().addAll(fecha, sala);
        HBox.setHgrow(informacion, Priority.ALWAYS);

        VBox precio = new VBox(4);
        precio.setAlignment(Pos.CENTER_RIGHT);
        Label precioLabel = new Label(FORMATO_MONEDA.format(funcion.getPrecioBase()));
        precioLabel.getStyleClass().add("function-meta");
        precio.getChildren().add(precioLabel);

        Button asientos = new Button("Elegir asientos");
        asientos.getStyleClass().add("button-gold");
        asientos.setOnAction(event -> {
            dialogoDetalles.close();
            mostrarSelectorAsientos(funcion);
        });

        Button listaEspera = new Button("Lista de espera");
        listaEspera.getStyleClass().add("button-small-outline");
        listaEspera.setOnAction(event -> solicitarListaEspera(funcion));

        fila.getChildren().addAll(informacion, precio, asientos, listaEspera);
        return fila;
    }

    private void solicitarListaEspera(Funcion funcion) {
        if (cliente == null || operaciones == null) {
            mostrarAlerta(Alert.AlertType.ERROR, "Lista de espera",
                    "Inicia sesión como cliente.",
                    "La lista de espera está disponible para clientes autenticados.");
            return;
        }
        Optional<String> cantidad = solicitarTextoCinemaUQ(
                "Lista de espera",
                funcion.getPelicula().getTitulo()
                        + " · " + FORMATO_FUNCION.format(funcion.getFechaHora()),
                "¿Cuántos asientos necesitas?",
                "1",
                "Cantidad de asientos"
        );
        cantidad.ifPresent(valor -> {
            try {
                int unidades = Integer.parseInt(valor.trim());
                operaciones.solicitarEspera(funcion, cliente, unidades);
                mostrarAlerta(Alert.AlertType.INFORMATION, "Lista de espera",
                        "Solicitud registrada.",
                        "Te avisaremos cuando haya disponibilidad para la cantidad solicitada.");
            } catch (RuntimeException ex) {
                mostrarAlerta(Alert.AlertType.WARNING, "Lista de espera",
                        "No se pudo registrar la solicitud.", ex.getMessage());
            }
        });
    }

    private void mostrarSelectorAsientos(Funcion funcion) {
        /*
         * Se utiliza un Stage propio en lugar de Dialog/ DialogPane.
         * El selector tenía un problema persistente con el fondo interno de
         * DialogPane en algunos equipos Windows: la skin de JavaFX volvía a
         * pintar el contenido de blanco. Una Scene independiente evita esa capa.
         */
        Stage ventanaAsientos = new Stage();
        ventanaAsientos.setTitle("CinemaUQ · Seleccionar asientos");
        if (ventanaPropietaria != null) {
            ventanaAsientos.initOwner(ventanaPropietaria);
        }
        ventanaAsientos.initModality(Modality.APPLICATION_MODAL);
        ventanaAsientos.setResizable(false);

        Color colorFondo = Color.web("#09192b");
        Color colorPanel = Color.web("#0b1b2d");

        Background fondoVentana = new Background(
                new BackgroundFill(colorFondo, CornerRadii.EMPTY, Insets.EMPTY)
        );
        Background fondoPanel = new Background(
                new BackgroundFill(colorPanel, CornerRadii.EMPTY, Insets.EMPTY)
        );

        Map<String, Asiento> seleccionados = new LinkedHashMap<>();
        List<Asiento> resultadoSeleccion = new ArrayList<>();

        VBox raiz = new VBox(0);
        raiz.setFillWidth(true);
        raiz.setBackground(fondoVentana);
        raiz.setStyle("-fx-background-color: #09192b;");

        // Encabezado de la ventana.
        VBox encabezado = new VBox(5);
        encabezado.setPadding(new Insets(17, 22, 14, 22));
        encabezado.setBackground(fondoVentana);
        encabezado.setStyle("-fx-background-color: #09192b;");

        Label titulo = new Label("Selecciona tus asientos");
        titulo.setStyle(
                "-fx-text-fill: #f5f7fb;"
                        + "-fx-font-family: 'Segoe UI';"
                        + "-fx-font-size: 19px;"
                        + "-fx-font-weight: bold;"
        );

        Label detalleFuncion = new Label(
                funcion.getPelicula().getTitulo()
                        + " · "
                        + FORMATO_FUNCION.format(funcion.getFechaHora())
        );
        detalleFuncion.setStyle(
                "-fx-text-fill: #aebfd1;"
                        + "-fx-font-family: 'Segoe UI';"
                        + "-fx-font-size: 11px;"
        );

        encabezado.getChildren().addAll(titulo, detalleFuncion);

        // Cuerpo desplazable.
        VBox contenido = new VBox(15);
        contenido.setPadding(new Insets(17, 22, 17, 22));
        contenido.setFillWidth(true);
        contenido.setBackground(fondoPanel);
        contenido.setStyle("-fx-background-color: #0b1b2d;");

        Label pantalla = new Label("PANTALLA");
        pantalla.setMaxWidth(Double.MAX_VALUE);
        pantalla.setAlignment(Pos.CENTER);
        pantalla.setPadding(new Insets(10, 12, 10, 12));
        pantalla.setStyle(
                "-fx-background-color: linear-gradient(to right, #314b61, #d2a83f, #314b61);"
                        + "-fx-background-radius: 18;"
                        + "-fx-text-fill: #111c2b;"
                        + "-fx-font-family: 'Segoe UI';"
                        + "-fx-font-weight: bold;"
                        + "-fx-font-size: 11px;"
        );

        HBox leyenda = new HBox(
                22,
                crearLeyendaAsiento("Disponible", "seat-legend-available"),
                crearLeyendaAsiento("Seleccionado", "seat-legend-selected"),
                crearLeyendaAsiento("Ocupado", "seat-legend-occupied")
        );
        leyenda.setAlignment(Pos.CENTER);
        leyenda.setPadding(new Insets(2, 0, 2, 0));

        List<Asiento> asientosFuncion =
                carteleraController.consultarAsientos(funcion);

        Map<String, List<Asiento>> porFila =
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Asiento asiento : asientosFuncion) {
            porFila.computeIfAbsent(
                    asiento.getFila(), clave -> new ArrayList<>()
            ).add(asiento);
        }

        Label estadoDisponibilidad = new Label();
        estadoDisponibilidad.setWrapText(true);
        estadoDisponibilidad.setStyle(
                "-fx-text-fill: #aebfd1;"
                        + "-fx-font-family: 'Segoe UI';"
                        + "-fx-font-size: 10px;"
        );

        GridPane mapa = new GridPane();
        mapa.setHgap(5);
        mapa.setVgap(9);
        mapa.setAlignment(Pos.CENTER);
        mapa.setPadding(new Insets(5, 0, 8, 0));
        mapa.setBackground(fondoPanel);
        mapa.setStyle("-fx-background-color: #0b1b2d;");

        final java.util.concurrent.atomic.AtomicReference<Label> resumenAsientosRef =
                new java.util.concurrent.atomic.AtomicReference<>();
        final java.util.concurrent.atomic.AtomicReference<Button> botonConfirmarRef =
                new java.util.concurrent.atomic.AtomicReference<>();

        long cantidadDisponibles = 0;
        long cantidadOcupados = 0;
        int indiceFila = 0;

        for (Map.Entry<String, List<Asiento>> entrada : porFila.entrySet()) {
            Label nombreFila = new Label(entrada.getKey());
            nombreFila.setMinWidth(22);
            nombreFila.setAlignment(Pos.CENTER);
            nombreFila.setStyle(
                    "-fx-text-fill: #aebfd1;"
                            + "-fx-font-family: 'Segoe UI';"
                            + "-fx-font-size: 11px;"
                            + "-fx-font-weight: bold;"
            );
            mapa.add(nombreFila, 0, indiceFila);

            entrada.getValue().sort(
                    java.util.Comparator.comparingInt(Asiento::getNumero)
            );

            int columna = 1;

            for (int indiceAsiento = 0;
                 indiceAsiento < entrada.getValue().size();
                 indiceAsiento++) {

                Asiento asiento = entrada.getValue().get(indiceAsiento);
                String idAsiento = asiento.obtenerIdentificador();
                boolean disponible =
                        carteleraController.estaDisponible(funcion, asiento);

                if (disponible) {
                    cantidadDisponibles++;
                } else {
                    cantidadOcupados++;
                }

                Button botonAsiento = new Button(
                        String.valueOf(asiento.getNumero())
                );
                botonAsiento.getStyleClass().add("seat-button");
                botonAsiento.setMinSize(43, 38);
                botonAsiento.setPrefSize(43, 38);
                botonAsiento.setMaxSize(43, 38);
                botonAsiento.setFocusTraversable(disponible);
                botonAsiento.setAccessibleText(
                        "Asiento " + idAsiento
                                + (disponible ? ", disponible" : ", ocupado")
                );

                if (!disponible) {
                    botonAsiento.getStyleClass().add("seat-button-occupied");
                    botonAsiento.setStyle(estiloAsientoOcupado());
                    botonAsiento.setDisable(true);
                    botonAsiento.setCursor(javafx.scene.Cursor.DEFAULT);
                    botonAsiento.setTooltip(new Tooltip(
                            "Asiento " + idAsiento + " · Ocupado"
                    ));
                } else {
                    botonAsiento.setStyle(estiloAsientoDisponible());
                    botonAsiento.setCursor(javafx.scene.Cursor.HAND);
                    botonAsiento.setTooltip(new Tooltip(
                            "Asiento " + idAsiento
                                    + " · Disponible. Pulsa para seleccionar."
                    ));

                    botonAsiento.setOnAction(evento -> {
                        if (seleccionados.containsKey(idAsiento)) {
                            seleccionados.remove(idAsiento);
                            botonAsiento.getStyleClass()
                                    .remove("seat-button-selected");
                            botonAsiento.setStyle(estiloAsientoDisponible());
                            botonAsiento.setAccessibleText(
                                    "Asiento " + idAsiento + ", disponible"
                            );
                            botonAsiento.setTooltip(new Tooltip(
                                    "Asiento " + idAsiento
                                            + " · Disponible. Pulsa para seleccionar."
                            ));
                        } else {
                            seleccionados.put(idAsiento, asiento);
                            if (!botonAsiento.getStyleClass()
                                    .contains("seat-button-selected")) {
                                botonAsiento.getStyleClass()
                                        .add("seat-button-selected");
                            }
                            botonAsiento.setStyle(estiloAsientoSeleccionado());
                            botonAsiento.setAccessibleText(
                                    "Asiento " + idAsiento + ", seleccionado"
                            );
                            botonAsiento.setTooltip(new Tooltip(
                                    "Asiento " + idAsiento
                                            + " · Seleccionado. Pulsa para quitarlo."
                            ));
                        }

                        // El resumen y el estado del botón cambian en el mismo clic.
                        actualizarResumenAsientos(seleccionados, resumenAsientosRef.get());
                        actualizarEstadoConfirmacion(
                                botonConfirmarRef.get(), seleccionados
                        );

                        ScaleTransition pulso = new ScaleTransition(
                                javafx.util.Duration.millis(105), botonAsiento);
                        pulso.setFromX(1.0);
                        pulso.setFromY(1.0);
                        pulso.setToX(1.08);
                        pulso.setToY(1.08);
                        pulso.setCycleCount(2);
                        pulso.setAutoReverse(true);
                        pulso.play();
                    });
                }

                mapa.add(botonAsiento, columna++, indiceFila);

                // Solo se separan los dos asientos de los extremos laterales.
                // Los seis asientos del centro permanecen completamente juntos.
                int numeroAsiento = asiento.getNumero();
                if ((numeroAsiento == 2 || numeroAsiento == 8)
                        && indiceAsiento < entrada.getValue().size() - 1) {
                    Region pasilloLateral = new Region();
                    pasilloLateral.setMinWidth(12);
                    pasilloLateral.setPrefWidth(14);
                    pasilloLateral.setMaxWidth(14);
                    mapa.add(pasilloLateral, columna++, indiceFila);
                }
            }

            indiceFila++;
        }

        estadoDisponibilidad.setText(
                "Disponibles: " + cantidadDisponibles
                        + "     ·     Ocupados: " + cantidadOcupados
        );

        Label resumenAsientos = new Label("No has seleccionado asientos.");
        resumenAsientos.setWrapText(true);
        resumenAsientos.setStyle(
                "-fx-text-fill: #f4c34e;"
                        + "-fx-font-family: 'Segoe UI';"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
        );

        // Conecta el resumen y el botón inferior con los eventos de cada asiento.
        resumenAsientosRef.set(resumenAsientos);

        contenido.getChildren().addAll(
                pantalla,
                leyenda,
                estadoDisponibilidad,
                mapa,
                new Separator(),
                resumenAsientos
        );

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(false);
        scroll.setPannable(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefViewportHeight(445);
        scroll.setMinViewportHeight(320);
        scroll.setBackground(fondoPanel);
        scroll.setStyle(
                "-fx-background-color: #0b1b2d;"
                        + "-fx-background: #0b1b2d;"
                        + "-fx-border-color: transparent;"
                        + "-fx-padding: 0;"
        );

        // La escena es propia: no existe DialogPane que pueda volver a pintar blanco.
        Scene escena = new Scene(raiz, 760, 680, colorFondo);
        URL cssAsientos = CarteleraViewController.class.getResource(
                "/co/edu/uniquindio/proyectofinal/css/styles.css"
        );
        if (cssAsientos != null) {
            escena.getStylesheets().add(cssAsientos.toExternalForm());
        }

        HBox pie = new HBox(10);
        pie.setAlignment(Pos.CENTER_RIGHT);
        pie.setPadding(new Insets(13, 20, 15, 20));
        pie.setBackground(fondoVentana);
        pie.setStyle("-fx-background-color: #09192b;");

        Button botonConfirmar = new Button("Confirmar selección");
        botonConfirmar.setDisable(true);
        botonConfirmar.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffd34d, #eaa018);"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #101827;"
                        + "-fx-font-family: 'Segoe UI';"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 10px 18px;"
                        + "-fx-cursor: hand;"
        );

        Button botonCancelar = new Button("Cancelar");
        botonCancelar.setStyle(
                "-fx-background-color: #172d45;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #3b5876;"
                        + "-fx-border-radius: 8;"
                        + "-fx-text-fill: #e5edf6;"
                        + "-fx-font-family: 'Segoe UI';"
                        + "-fx-font-size: 11px;"
                        + "-fx-padding: 10px 18px;"
                        + "-fx-cursor: hand;"
        );

        botonConfirmarRef.set(botonConfirmar);

        botonConfirmar.setOnAction(evento -> {
            if (seleccionados.isEmpty()) {
                return;
            }
            resultadoSeleccion.clear();
            resultadoSeleccion.addAll(seleccionados.values());
            ventanaAsientos.close();
        });

        botonCancelar.setOnAction(evento -> ventanaAsientos.close());

        pie.getChildren().addAll(botonCancelar, botonConfirmar);

        VBox.setVgrow(scroll, Priority.ALWAYS);
        raiz.getChildren().addAll(encabezado, scroll, pie);

        // Control de rueda explícito, además del desplazamiento nativo y arrastre.
        scroll.addEventFilter(ScrollEvent.SCROLL, evento -> {
            double recorrido = contenido.getBoundsInLocal().getHeight()
                    - scroll.getViewportBounds().getHeight();
            if (recorrido > 0.5) {
                double nuevoValor = scroll.getVvalue()
                        - evento.getDeltaY() / recorrido;
                scroll.setVvalue(Math.max(0, Math.min(1, nuevoValor)));
                evento.consume();
            }
        });

        ventanaAsientos.setScene(escena);
        ventanaAsientos.setMinWidth(700);
        ventanaAsientos.setMinHeight(600);
        ventanaAsientos.setOnShown(evento -> Platform.runLater(() -> {
            escena.getRoot().applyCss();
            escena.getRoot().layout();

            Node viewport = scroll.lookup(".viewport");
            if (viewport instanceof Region regionViewport) {
                regionViewport.setBackground(fondoPanel);
                regionViewport.setStyle(
                        "-fx-background-color: #0b1b2d;"
                                + "-fx-background: #0b1b2d;"
                );
            }

            Node corner = scroll.lookup(".corner");
            if (corner instanceof Region regionCorner) {
                regionCorner.setBackground(fondoPanel);
                regionCorner.setStyle("-fx-background-color: #0b1b2d;");
            }

            scroll.setBackground(fondoPanel);
            contenido.setBackground(fondoPanel);
            raiz.setBackground(fondoVentana);
            escena.setFill(colorFondo);
            scroll.setVvalue(0);
        }));

        ventanaAsientos.showAndWait();

        if (!resultadoSeleccion.isEmpty()) {
            mostrarResumenSeleccion(funcion, List.copyOf(resultadoSeleccion));
        }
    }

    private void actualizarEstadoConfirmacion(
            Button boton,
            Map<String, Asiento> seleccionados
    ) {
        boolean habilitado = !seleccionados.isEmpty();
        boton.setDisable(!habilitado);
        boton.setOpacity(habilitado ? 1.0 : 0.48);
    }

    private void aplicarTemaDialogoAsientos(
            Dialog<?> dialogo,
            DialogPane panel,
            ScrollPane scroll,
            VBox contenido
    ) {
        javafx.scene.paint.Color fondoPrincipal =
                javafx.scene.paint.Color.web("#09192b");
        javafx.scene.paint.Color fondoContenido =
                javafx.scene.paint.Color.web("#0b1b2d");

        javafx.scene.layout.Background backgroundPrincipal =
                new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundFill(
                                fondoPrincipal,
                                javafx.scene.layout.CornerRadii.EMPTY,
                                Insets.EMPTY
                        )
                );

        javafx.scene.layout.Background backgroundContenido =
                new javafx.scene.layout.Background(
                        new javafx.scene.layout.BackgroundFill(
                                fondoContenido,
                                javafx.scene.layout.CornerRadii.EMPTY,
                                Insets.EMPTY
                        )
                );

        panel.setBackground(backgroundPrincipal);
        panel.setStyle(
                "-fx-background-color: #09192b;"
                        + "-fx-control-inner-background: #09192b;"
                        + "-fx-padding: 0;"
                        + "-fx-border-color: #294760;"
                        + "-fx-border-width: 1px;"
        );

        contenido.setBackground(backgroundContenido);
        contenido.setStyle(
                "-fx-background-color: #0b1b2d;"
                        + "-fx-background: #0b1b2d;"
        );

        scroll.setBackground(backgroundContenido);
        scroll.setStyle(
                "-fx-background-color: #0b1b2d;"
                        + "-fx-background: #0b1b2d;"
                        + "-fx-border-color: transparent;"
                        + "-fx-padding: 0;"
        );

        // La escena del Dialog es separada de la Scene del Dashboard.
        if (panel.getScene() != null) {
            Node raizEscena = panel.getScene().getRoot();
            if (raizEscena instanceof Region regionRaiz) {
                regionRaiz.setBackground(backgroundPrincipal);
                regionRaiz.setStyle("-fx-background-color: #09192b;");
            }
        }

        // Fuerza el estilo de las regiones internas creadas por DialogPane.
        for (String selector : List.of(
                ".content",
                ".header-panel",
                ".button-bar",
                ".graphic-container"
        )) {
            Node nodo = panel.lookup(selector);
            if (nodo instanceof Region region) {
                region.setBackground(backgroundPrincipal);
                region.setStyle(
                        "-fx-background-color: #09192b;"
                                + "-fx-background: #09192b;"
                );
            }
        }

        // El viewport es la superficie que suele quedarse blanca en Windows.
        for (String selector : List.of(
                ".viewport",
                ".corner",
                ".scroll-pane"
        )) {
            Node nodo = scroll.lookup(selector);
            if (nodo instanceof Region region) {
                region.setBackground(backgroundContenido);
                region.setStyle(
                        "-fx-background-color: #0b1b2d;"
                                + "-fx-background: #0b1b2d;"
                );
            }
        }

        // También se pinta el nodo raíz del contenido dentro del ScrollPane.
        if (scroll.getContent() instanceof Region regionContenido) {
            regionContenido.setBackground(backgroundContenido);
            regionContenido.setStyle(
                    "-fx-background-color: #0b1b2d;"
                            + "-fx-background: #0b1b2d;"
            );
        }

        panel.applyCss();
        panel.layout();
        scroll.applyCss();
        scroll.layout();
        scroll.setVvalue(0);
        panel.requestLayout();

        // Evita advertencias por parámetro y mantiene el método autocontenido.
        if (dialogo.getDialogPane() != panel) {
            throw new IllegalStateException(
                    "El panel del diálogo de asientos cambió inesperadamente."
            );
        }
    }

    private String estiloAsientoDisponible() {
        return "-fx-background-color: #183b50;"
                + "-fx-background-radius: 9;"
                + "-fx-border-color: #3f947d;"
                + "-fx-border-width: 1.5;"
                + "-fx-border-radius: 9;"
                + "-fx-cursor: hand;";
    }

    private String estiloAsientoHover() {
        return "-fx-background-color: #245d58;"
                + "-fx-background-radius: 9;"
                + "-fx-border-color: #8bd5ad;"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 9;"
                + "-fx-effect: dropshadow(gaussian, rgba(139,213,173,0.45), 8, 0.25, 0, 0);"
                + "-fx-cursor: hand;";
    }

    private String estiloAsientoSeleccionado() {
        return "-fx-background-color: #f2bd39;"
                + "-fx-background-radius: 9;"
                + "-fx-border-color: #ffe08a;"
                + "-fx-border-width: 2.5;"
                + "-fx-border-radius: 9;"
                + "-fx-effect: dropshadow(gaussian, rgba(242,189,57,0.75), 12, 0.35, 0, 0);"
                + "-fx-cursor: hand;";
    }

    private String estiloAsientoOcupado() {
        return "-fx-background-color: #3a4655;"
                + "-fx-background-radius: 9;"
                + "-fx-border-color: #667386;"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 9;";
    }

    private void actualizarResumenAsientos(Map<String, Asiento> seleccionados, Label etiqueta) {
        if (seleccionados.isEmpty()) {
            etiqueta.setText("No has seleccionado asientos.");
            return;
        }
        String codigos = seleccionados.keySet().stream()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        etiqueta.setText("Seleccionados: " + codigos
                + " · Total: " + seleccionados.size());
    }

    private HBox crearLeyendaAsiento(String texto, String estilo) {
        HBox leyenda = new HBox(6);
        leyenda.setAlignment(Pos.CENTER_LEFT);
        Region muestra = new Region();
        muestra.setMinSize(13, 13);
        muestra.setPrefSize(13, 13);
        muestra.getStyleClass().add(estilo);
        Label etiqueta = new Label(texto);
        etiqueta.getStyleClass().add("muted-small");
        leyenda.getChildren().addAll(muestra, etiqueta);
        return leyenda;
    }

    private void mostrarResumenSeleccion(Funcion funcion, List<Asiento> asientos) {
        if (cliente == null || reservaController == null) {
            mostrarAlerta(
                    Alert.AlertType.ERROR,
                    "No se pudo crear la reserva",
                    "La sesión actual no corresponde a un cliente autenticado.",
                    "Inicia sesión con una cuenta de cliente para reservar asientos."
            );
            return;
        }

        final List<ReservaAsiento> reservas;
        try {
            reservas = reservaController.reservarAsientos(
                    funcion, asientos, cliente);
        } catch (RuntimeException ex) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "No fue posible reservar los asientos",
                    "La disponibilidad pudo cambiar mientras elegías.",
                    ex.getMessage()
            );
            return;
        }

        mostrarDialogoReserva(funcion, reservas);
    }

    /** Muestra la reserva temporal con un resumen de pago más claro. */
    private void mostrarDialogoReserva(
            Funcion funcion,
            List<ReservaAsiento> reservas
    ) {
        String seleccion = reservas.stream()
                .map(reserva -> reserva.getAsiento().obtenerIdentificador())
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        LocalDateTime fechaExpiracion =
                reservaController.consultarFechaExpiracion(reservas);
        java.math.BigDecimal subtotal =
                reservaController.calcularTotalReserva(reservas);
        String total = FORMATO_MONEDA.format(subtotal);
        int minutos = reservaController.consultarDuracionReservaMinutos();

        Dialog<ButtonType> dialogo = new Dialog<>();
        prepararDialogo(
                dialogo,
                "Reserva temporal",
                "Revisa tu reserva y completa el pago",
                650,
                680
        );

        ButtonType continuar = new ButtonType(
                "Continuar al pago", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(
                "Cancelar reserva", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(continuar, cancelar);

        VBox contenido = new VBox(14);
        contenido.setPadding(new Insets(18));
        contenido.setStyle("-fx-background-color: #0b1b2d;");

        // Encabezado de la reserva.
        VBox encabezado = new VBox(5);
        Label etiquetaReserva = new Label("RESERVA TEMPORAL");
        etiquetaReserva.setStyle(
                "-fx-text-fill: #f2bd39; -fx-font-size: 10px; "
                        + "-fx-font-weight: bold; -fx-letter-spacing: 1px;");
        Label pelicula = new Label(funcion.getPelicula().getTitulo());
        pelicula.setStyle(
                "-fx-text-fill: white; -fx-font-size: 23px; -fx-font-weight: bold;");
        Label fecha = new Label(FORMATO_FUNCION.format(funcion.getFechaHora()));
        fecha.setStyle("-fx-text-fill: #b8c9d9; -fx-font-size: 12px;");
        encabezado.getChildren().addAll(etiquetaReserva, pelicula, fecha);

        // Tarjeta con los asientos y el subtotal.
        VBox tarjetaReserva = new VBox(10);
        tarjetaReserva.setPadding(new Insets(15));
        tarjetaReserva.setStyle(
                "-fx-background-color: #142b42; -fx-background-radius: 12; "
                        + "-fx-border-color: #294760; -fx-border-radius: 12;");

        Label tituloAsientos = new Label("DETALLES DE TU RESERVA");
        tituloAsientos.setStyle(
                "-fx-text-fill: #8faec8; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label asientosLabel = new Label(seleccion);
        asientosLabel.setWrapText(true);
        asientosLabel.setStyle(
                "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label textoAsientos = new Label("Asientos seleccionados");
        textoAsientos.setStyle("-fx-text-fill: #a9bfd2; -fx-font-size: 11px;");
        Separator separadorReserva = new Separator();
        separadorReserva.setStyle("-fx-opacity: 0.45;");

        HBox filaSubtotal = new HBox();
        filaSubtotal.setAlignment(Pos.CENTER_LEFT);
        Label textoSubtotal = new Label("Subtotal de entradas");
        textoSubtotal.setStyle("-fx-text-fill: #c4d3e0; -fx-font-size: 13px;");
        Region espacioSubtotal = new Region();
        HBox.setHgrow(espacioSubtotal, Priority.ALWAYS);
        Label valorSubtotal = new Label(total);
        valorSubtotal.setStyle(
                "-fx-text-fill: #f2bd39; -fx-font-size: 18px; -fx-font-weight: bold;");
        filaSubtotal.getChildren().addAll(textoSubtotal, espacioSubtotal, valorSubtotal);
        tarjetaReserva.getChildren().addAll(
                tituloAsientos, asientosLabel, textoAsientos,
                separadorReserva, filaSubtotal);

        // Temporizador y progreso.
        VBox tarjetaTiempo = new VBox(9);
        tarjetaTiempo.setPadding(new Insets(14));
        tarjetaTiempo.setStyle(
                "-fx-background-color: #142b42; -fx-background-radius: 12; "
                        + "-fx-border-color: #294760; -fx-border-radius: 12;");

        HBox filaTiempo = new HBox();
        filaTiempo.setAlignment(Pos.CENTER_LEFT);
        Label tituloTiempo = new Label("TIEMPO PARA COMPLETAR EL PAGO");
        tituloTiempo.setStyle(
                "-fx-text-fill: #a9bfd2; -fx-font-size: 10px; -fx-font-weight: bold;");
        Region espacioTiempo = new Region();
        HBox.setHgrow(espacioTiempo, Priority.ALWAYS);
        Label cuentaRegresiva = new Label();
        cuentaRegresiva.setStyle(
                "-fx-text-fill: #ffc247; -fx-font-size: 21px; -fx-font-weight: bold;");
        filaTiempo.getChildren().addAll(tituloTiempo, espacioTiempo, cuentaRegresiva);

        javafx.scene.control.ProgressBar progreso =
                new javafx.scene.control.ProgressBar(1);
        progreso.setMaxWidth(Double.MAX_VALUE);
        progreso.setPrefHeight(9);
        progreso.setStyle(
                "-fx-accent: #f2bd39; -fx-control-inner-background: #294760;");
        Label aviso = new Label(
                "Tus asientos estarán retenidos durante " + minutos
                        + " minutos. Si el tiempo termina, se liberarán automáticamente.");
        aviso.setWrapText(true);
        aviso.setStyle("-fx-text-fill: #b8c9d9; -fx-font-size: 11px;");
        tarjetaTiempo.getChildren().addAll(filaTiempo, progreso, aviso);

        // Beneficios: el código se valida en el momento de confirmar la compra.
        VBox tarjetaBeneficios = new VBox(10);
        tarjetaBeneficios.setPadding(new Insets(14));
        tarjetaBeneficios.setStyle(
                "-fx-background-color: #142b42; -fx-background-radius: 12; "
                        + "-fx-border-color: #294760; -fx-border-radius: 12;");

        HBox tituloBeneficios = new HBox(8);
        tituloBeneficios.setAlignment(Pos.CENTER_LEFT);
        Label iconoBeneficios = new Label("✦");
        iconoBeneficios.setStyle("-fx-text-fill: #ffc247; -fx-font-size: 17px;");
        Label textoBeneficios = new Label("BENEFICIOS Y DESCUENTOS");
        textoBeneficios.setStyle(
                "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;");
        tituloBeneficios.getChildren().addAll(iconoBeneficios, textoBeneficios);

        Label textoCodigo = new Label("¿Tienes un código promocional?");
        textoCodigo.setStyle("-fx-text-fill: #c4d3e0; -fx-font-size: 12px;");
        TextField codigoPromocion = new TextField();
        codigoPromocion.setPromptText("Ej. CINEMA10");
        codigoPromocion.setPrefHeight(36);
        codigoPromocion.setStyle(
                "-fx-background-color: #0b1b2d; -fx-text-fill: white; "
                        + "-fx-prompt-text-fill: #7891a8; -fx-background-radius: 7; "
                        + "-fx-border-color: #34536d; -fx-border-radius: 7;");

        Label textoPuntos = new Label("Puntos que deseas redimir");
        textoPuntos.setStyle("-fx-text-fill: #c4d3e0; -fx-font-size: 12px;");
        TextField puntosRedimir = new TextField("0");
        puntosRedimir.setPromptText("0");
        puntosRedimir.setPrefWidth(110);
        puntosRedimir.setMaxWidth(140);
        puntosRedimir.setStyle(
                "-fx-background-color: #0b1b2d; -fx-text-fill: white; "
                        + "-fx-prompt-text-fill: #7891a8; -fx-background-radius: 7; "
                        + "-fx-border-color: #34536d; -fx-border-radius: 7;");

        Label notaPromocion = new Label(
                "El código y los puntos se validarán al continuar al pago.");
        notaPromocion.setWrapText(true);
        notaPromocion.setStyle("-fx-text-fill: #8faec8; -fx-font-size: 10px;");
        tarjetaBeneficios.getChildren().addAll(
                tituloBeneficios, textoCodigo, codigoPromocion,
                textoPuntos, puntosRedimir, notaPromocion);

        // Información de la tarjeta virtual.
        Optional<TarjetaVirtual> tarjeta = reservaController.consultarTarjeta(cliente);
        boolean tarjetaLista = false;
        String informacionTarjeta;
        if (tarjeta.isEmpty()) {
            informacionTarjeta =
                    "No tienes una tarjeta virtual asociada. Contacta al administrador.";
        } else {
            TarjetaVirtual tarjetaCliente = tarjeta.get();
            boolean activa = tarjetaCliente.getEstado()
                    == co.edu.uniquindio.proyectofinal.model.enums.EstadoTarjeta.ACTIVA;
            informacionTarjeta = "Saldo disponible: "
                    + FORMATO_MONEDA.format(tarjetaCliente.getSaldo());
            if (!activa) {
                informacionTarjeta += " · Tarjeta bloqueada";
            }
            tarjetaLista = activa;
        }

        Label pago = new Label("TARJETA VIRTUAL\n" + informacionTarjeta);
        pago.setWrapText(true);
        pago.setMaxWidth(Double.MAX_VALUE);
        pago.setPadding(new Insets(12));
        pago.setStyle(
                "-fx-background-color: #10243a; -fx-background-radius: 9; "
                        + "-fx-border-color: #294760; -fx-border-radius: 9; "
                        + "-fx-text-fill: #c4d3e0; -fx-font-size: 11px;");

        contenido.getChildren().addAll(
                encabezado, tarjetaReserva, tarjetaTiempo,
                tarjetaBeneficios, pago);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(570);
        scroll.setStyle("-fx-background: #0b1b2d; -fx-background-color: #0b1b2d;");
        dialogo.getDialogPane().setContent(scroll);
        dialogo.setResultConverter(tipo -> tipo);

        Button botonContinuar = (Button)
                dialogo.getDialogPane().lookupButton(continuar);
        botonContinuar.setDisable(!tarjetaLista);
        botonContinuar.setStyle(
                "-fx-background-color: #f2bd39; -fx-text-fill: #142235; "
                        + "-fx-font-weight: bold; -fx-background-radius: 7; -fx-padding: 9 16;");
        Button botonCancelar = (Button)
                dialogo.getDialogPane().lookupButton(cancelar);
        botonCancelar.setStyle(
                "-fx-background-color: #20364b; -fx-text-fill: white; "
                        + "-fx-background-radius: 7; -fx-padding: 9 16;");

        // La cuenta regresiva visual se mantiene sincronizada con la expiración real.
        AtomicBoolean reservaExpirada = new AtomicBoolean(false);
        Runnable actualizarTiempo = () -> {
            long segundos = Math.max(0,
                    Duration.between(LocalDateTime.now(), fechaExpiracion).getSeconds());
            long minutosRestantes = segundos / 60;
            long segundosRestantes = segundos % 60;
            cuentaRegresiva.setText(String.format(
                    "%02d:%02d", minutosRestantes, segundosRestantes));
            double fraccion = Math.max(0.0,
                    Math.min(1.0, segundos / (double) (minutos * 60L)));
            progreso.setProgress(fraccion);
            if (segundos <= 60) {
                cuentaRegresiva.setStyle(
                        "-fx-text-fill: #ff8b70; -fx-font-size: 21px; -fx-font-weight: bold;");
            }
        };
        actualizarTiempo.run();

        Timeline cronometro = new Timeline(new KeyFrame(
                javafx.util.Duration.seconds(1), evento -> {
            actualizarTiempo.run();
            if (!LocalDateTime.now().isBefore(fechaExpiracion)) {
                reservaExpirada.set(true);
                reservaController.procesarReservasExpiradas();
                dialogo.setResult(cancelar);
                dialogo.close();
            }
        }));
        cronometro.setCycleCount(Animation.INDEFINITE);
        cronometro.play();

        Optional<ButtonType> respuesta;
        try {
            respuesta = dialogo.showAndWait();
        } finally {
            cronometro.stop();
        }

        if (reservaExpirada.get()) {
            mostrarAlerta(
                    Alert.AlertType.WARNING,
                    "Reserva vencida",
                    "El tiempo para completar la compra terminó.",
                    "Los asientos fueron liberados. Puedes seleccionarlos nuevamente si siguen disponibles."
            );
            return;
        }

        if (respuesta.isPresent() && respuesta.get() == continuar) {
            try {
                int puntos = Integer.parseInt(puntosRedimir.getText().trim());
                if (puntos < 0) {
                    throw new IllegalArgumentException("Los puntos no pueden ser negativos.");
                }
                Compra compra = reservaController.confirmarCompra(
                        reservas, cliente, codigoPromocion.getText(), puntos);
                mostrarAlertaCompraExitosa(compra, funcion, seleccion);
            } catch (RuntimeException ex) {
                try {
                    reservaController.cancelarReserva(reservas, cliente);
                } catch (RuntimeException ignored) {
                    reservaController.procesarReservasExpiradas();
                }
                mostrarAlerta(
                        Alert.AlertType.ERROR,
                        "No se pudo completar la compra",
                        "No fue posible confirmar el pago.",
                        ex.getMessage()
                );
            }
        } else {
            try {
                reservaController.cancelarReserva(reservas, cliente);
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "Reserva cancelada",
                        "Los asientos fueron liberados.",
                        "Puedes volver a seleccionarlos mientras sigan disponibles."
                );
            } catch (RuntimeException ex) {
                reservaController.procesarReservasExpiradas();
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "La reserva ya no estaba activa",
                        "Los asientos ya fueron liberados.",
                        "Puedes volver a seleccionarlos si continúan disponibles."
                );
            }
        }
    }

    /**
     * Muestra una confirmación personalizada para agregar o quitar favoritas.
     * Se utiliza un Dialog propio para mantener la identidad visual de CinemaUQ.
     */
    private void mostrarAlertaFavorita(Pelicula pelicula, boolean guardada) {
        Dialog<Void> dialogo = new Dialog<>();
        prepararDialogo(
                dialogo,
                guardada ? "Película guardada" : "Película eliminada",
                null,
                430,
                330
        );
        dialogo.setHeaderText(null);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Button botonAceptar = (Button) dialogo.getDialogPane()
                .lookupButton(ButtonType.CLOSE);
        botonAceptar.setText("¡Genial!");
        botonAceptar.setStyle(
                "-fx-background-color: #f2bd39; "
                        + "-fx-text-fill: #142235; "
                        + "-fx-font-weight: bold; "
                        + "-fx-background-radius: 8; "
                        + "-fx-padding: 10 30; "
                        + "-fx-cursor: hand;"
        );

        VBox contenido = new VBox(13);
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new Insets(25, 28, 22, 28));
        contenido.setStyle("-fx-background-color: #0b1b2d;");

        StackPane circulo = new StackPane();
        circulo.setMinSize(76, 76);
        circulo.setPrefSize(76, 76);
        circulo.setMaxSize(76, 76);
        circulo.setStyle(
                "-fx-background-color: " + (guardada ? "#3b321b" : "#26364a") + "; "
                        + "-fx-background-radius: 100; "
                        + "-fx-border-color: " + (guardada ? "#d9a928" : "#52677d") + "; "
                        + "-fx-border-radius: 100; -fx-border-width: 1.5;"
        );

        Label corazon = new Label(guardada ? "♥" : "♡");
        corazon.setStyle(
                "-fx-font-family: 'Segoe UI Symbol'; "
                        + "-fx-font-size: 43px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-text-fill: " + (guardada ? "#ffc52e" : "#a9bfd2") + ";"
        );
        circulo.getChildren().add(corazon);

        Label titulo = new Label(guardada ? "¡Película guardada!" : "Película eliminada");
        titulo.setStyle(
                "-fx-text-fill: white; -fx-font-size: 23px; -fx-font-weight: bold;"
        );
        titulo.setWrapText(true);
        titulo.setAlignment(Pos.CENTER);

        Label mensaje = new Label(guardada
                ? "«" + pelicula.getTitulo() + "» se agregó a tus películas favoritas."
                : "«" + pelicula.getTitulo() + "» se quitó de tus películas favoritas.");
        mensaje.setWrapText(true);
        mensaje.setMaxWidth(340);
        mensaje.setAlignment(Pos.CENTER);
        mensaje.setStyle("-fx-text-fill: #b8c9d9; -fx-font-size: 13px;");

        VBox tarjeta = new VBox(6);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        tarjeta.setPadding(new Insets(13, 16, 13, 16));
        tarjeta.setStyle(
                "-fx-background-color: #142b42; "
                        + "-fx-background-radius: 11; "
                        + "-fx-border-color: #294760; "
                        + "-fx-border-radius: 11;"
        );
        Label marca = new Label("CINEMA UQ  ·  MI COLECCIÓN");
        marca.setStyle(
                "-fx-text-fill: #f2bd39; -fx-font-size: 10px; -fx-font-weight: bold;"
        );
        Label detalle = new Label(guardada
                ? "Encuéntrala cuando quieras en Películas guardadas."
                : "Puedes volver a agregarla desde la cartelera.");
        detalle.setWrapText(true);
        detalle.setAlignment(Pos.CENTER);
        detalle.setStyle("-fx-text-fill: #91a9bf; -fx-font-size: 11px;");
        tarjeta.getChildren().addAll(marca, detalle);

        contenido.getChildren().addAll(circulo, titulo, mensaje, tarjeta);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setStyle(
                "-fx-background-color: #0b1b2d; -fx-border-color: #294760;"
        );

        // La animación se ejecuta al mostrarse la ventana, no durante el pulso
        // que finaliza la animación del corazón de la cartelera.
        dialogo.setOnShown(event -> {
            ScaleTransition entrada = new ScaleTransition(
                    javafx.util.Duration.millis(220), circulo);
            entrada.setFromX(0.72);
            entrada.setFromY(0.72);
            entrada.setToX(1.0);
            entrada.setToY(1.0);
            entrada.playFromStart();
        });

        dialogo.showAndWait();
    }

    /**
     * Reemplaza Alert nativos por un diálogo CinemaUQ. Mantiene la firma
     * existente para que todos los puntos de aviso/error sigan funcionando.
     */
    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String encabezado,
            String contenido
    ) {
        Dialog<Void> dialogo = new Dialog<>();
        prepararDialogo(dialogo, titulo, null, 465, 330);
        dialogo.setHeaderText(null);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.OK);

        boolean error = tipo == Alert.AlertType.ERROR;
        boolean advertencia = tipo == Alert.AlertType.WARNING
                || tipo == Alert.AlertType.CONFIRMATION;
        String color = error ? "#ff8b86" : advertencia ? "#ffd04a" : "#78e0b5";
        String fondoIcono = error ? "#482b36" : advertencia ? "#47391f" : "#173d38";
        String simbolo = error ? "!" : advertencia ? "!" : "✓";

        Button aceptar = (Button) dialogo.getDialogPane().lookupButton(ButtonType.OK);
        aceptar.setText("Entendido");
        aceptar.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffd34d, #eaa018);"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #101827;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 9 24;"
                        + "-fx-cursor: hand;"
        );

        VBox tarjeta = new VBox(12);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(24, 27, 22, 27));
        tarjeta.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #10243a, #09192b);"
                        + "-fx-background-radius: 11;"
        );

        StackPane iconoFondo = new StackPane();
        iconoFondo.setMinSize(58, 58);
        iconoFondo.setPrefSize(58, 58);
        iconoFondo.setMaxSize(58, 58);
        iconoFondo.setStyle(
                "-fx-background-color: " + fondoIcono + ";"
                        + "-fx-background-radius: 40;"
                        + "-fx-border-color: " + color + ";"
                        + "-fx-border-radius: 40;"
        );

        Label icono = new Label(simbolo);
        icono.setStyle(
                "-fx-text-fill: " + color + ";"
                        + "-fx-font-size: 28px;"
                        + "-fx-font-weight: bold;"
        );
        iconoFondo.getChildren().add(icono);

        Label tituloLabel = new Label(titulo == null ? "Aviso de CinemaUQ" : titulo);
        tituloLabel.setWrapText(true);
        tituloLabel.setAlignment(Pos.CENTER);
        tituloLabel.setStyle(
                "-fx-text-fill: #f5f7fb;"
                        + "-fx-font-size: 19px;"
                        + "-fx-font-weight: bold;"
        );

        Label encabezadoLabel = new Label(encabezado == null ? "" : encabezado);
        encabezadoLabel.setWrapText(true);
        encabezadoLabel.setAlignment(Pos.CENTER);
        encabezadoLabel.setMaxWidth(365);
        encabezadoLabel.setStyle(
                "-fx-text-fill: #dce6f2;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );

        Label detalleLabel = new Label(contenido == null ? "" : contenido);
        detalleLabel.setWrapText(true);
        detalleLabel.setAlignment(Pos.CENTER);
        detalleLabel.setMaxWidth(365);
        detalleLabel.setStyle(
                "-fx-text-fill: #9fb3c9;"
                        + "-fx-font-size: 11px;"
        );

        Label marca = new Label("CINEMAUQ  ·  EXPERIENCIA CINEMATOGRÁFICA");
        marca.setStyle(
                "-fx-text-fill: #7189a2;"
                        + "-fx-font-size: 8px;"
                        + "-fx-font-weight: bold;"
        );

        tarjeta.getChildren().addAll(
                iconoFondo, tituloLabel, encabezadoLabel, detalleLabel, marca
        );
        dialogo.getDialogPane().setContent(tarjeta);
        dialogo.showAndWait();
    }

    /**
     * Formulario de entrada de texto con campos y botones CinemaUQ.
     */
    private Optional<String> solicitarTextoCinemaUQ(
            String titulo,
            String encabezado,
            String etiqueta,
            String valorInicial,
            String prompt
    ) {
        Dialog<String> dialogo = new Dialog<>();
        prepararDialogo(dialogo, titulo, null, 455, 285);
        dialogo.setHeaderText(null);

        ButtonType aceptar = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(aceptar, cancelar);

        VBox contenido = new VBox(11);
        contenido.setPadding(new Insets(21, 24, 18, 24));
        contenido.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #10243a, #09192b);"
        );

        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle(
                "-fx-text-fill: #f5f7fb; -fx-font-size: 18px; -fx-font-weight: bold;"
        );

        Label descripcion = new Label(encabezado == null ? "" : encabezado);
        descripcion.setWrapText(true);
        descripcion.setStyle("-fx-text-fill: #b8c9d9; -fx-font-size: 11px;");

        Label etiquetaLabel = new Label(etiqueta);
        etiquetaLabel.setStyle(
                "-fx-text-fill: #e9bb4e; -fx-font-size: 10px; -fx-font-weight: bold;"
        );

        TextField campo = new TextField(valorInicial == null ? "" : valorInicial);
        campo.setPromptText(prompt);
        campo.setMaxWidth(Double.MAX_VALUE);
        campo.setStyle(
                "-fx-background-color: #0b1b2d;"
                        + "-fx-control-inner-background: #0b1b2d;"
                        + "-fx-text-fill: #f5f7fb;"
                        + "-fx-prompt-text-fill: #8198af;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #35516e;"
                        + "-fx-border-radius: 8;"
                        + "-fx-padding: 10 12;"
        );

        contenido.getChildren().addAll(
                tituloLabel, descripcion, etiquetaLabel, campo
        );
        dialogo.getDialogPane().setContent(contenido);

        Button botonAceptar = (Button) dialogo.getDialogPane().lookupButton(aceptar);
        botonAceptar.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffd34d, #eaa018);"
                        + "-fx-text-fill: #101827; -fx-font-weight: bold;"
                        + "-fx-background-radius: 8; -fx-padding: 9 20;"
        );

        Button botonCancelar = (Button) dialogo.getDialogPane().lookupButton(cancelar);
        botonCancelar.setStyle(
                "-fx-background-color: #172d45; -fx-text-fill: #e5edf6;"
                        + "-fx-border-color: #3b5876; -fx-border-radius: 8;"
                        + "-fx-background-radius: 8; -fx-padding: 9 20;"
        );

        dialogo.setResultConverter(tipo ->
                tipo == aceptar ? campo.getText().trim() : null
        );
        dialogo.setOnShown(evento -> campo.requestFocus());
        return dialogo.showAndWait();
    }


    /**
     * Muestra una confirmación visual de la compra realizada.
     * Presenta el código, la película, los asientos y el total pagado.
     */
    private void mostrarAlertaCompraExitosa(
            Compra compra,
            Funcion funcion,
            String asientos
    ) {
        Dialog<Void> dialogo = new Dialog<>();
        prepararDialogo(
                dialogo,
                "Compra confirmada",
                null,
                480,
                440
        );

        dialogo.setHeaderText(null);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Button botonEntendido = (Button) dialogo.getDialogPane()
                .lookupButton(ButtonType.CLOSE);
        botonEntendido.setText("Entendido");
        botonEntendido.setStyle(
                "-fx-background-color: #f2bd39; "
                        + "-fx-text-fill: #142235; "
                        + "-fx-font-weight: bold; "
                        + "-fx-background-radius: 8; "
                        + "-fx-padding: 10 26;"
        );

        VBox contenido = new VBox(16);
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new Insets(24));
        contenido.setStyle("-fx-background-color: #0b1b2d;");

        StackPane iconoContenedor = new StackPane();
        iconoContenedor.setMinSize(72, 72);
        iconoContenedor.setPrefSize(72, 72);
        iconoContenedor.setMaxSize(72, 72);
        iconoContenedor.setStyle(
                "-fx-background-color: #123e36; "
                        + "-fx-background-radius: 100;"
        );

        Label icono = new Label("✓");
        icono.setStyle(
                "-fx-text-fill: #55d6a4; "
                        + "-fx-font-size: 38px; "
                        + "-fx-font-weight: bold;"
        );
        iconoContenedor.getChildren().add(icono);

        Label titulo = new Label("¡Compra exitosa!");
        titulo.setStyle(
                "-fx-text-fill: white; "
                        + "-fx-font-size: 25px; "
                        + "-fx-font-weight: bold;"
        );

        Label subtitulo = new Label(
                "Tu pago se ha procesado correctamente."
        );
        subtitulo.setWrapText(true);
        subtitulo.setAlignment(Pos.CENTER);
        subtitulo.setStyle(
                "-fx-text-fill: #a9bed2; "
                        + "-fx-font-size: 13px;"
        );

        Label estado = new Label("PAGO APROBADO");
        estado.setStyle(
                "-fx-background-color: #153d36; "
                        + "-fx-background-radius: 20; "
                        + "-fx-text-fill: #67ddb0; "
                        + "-fx-font-size: 10px; "
                        + "-fx-font-weight: bold; "
                        + "-fx-padding: 7 14;"
        );

        VBox tarjeta = new VBox(12);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        tarjeta.setPadding(new Insets(17));
        tarjeta.setStyle(
                "-fx-background-color: #142a40; "
                        + "-fx-background-radius: 12; "
                        + "-fx-border-color: #294760; "
                        + "-fx-border-radius: 12;"
        );

        Label etiquetaCodigo = new Label("CÓDIGO DE COMPRA");
        etiquetaCodigo.setStyle(
                "-fx-text-fill: #8faec8; "
                        + "-fx-font-size: 10px; "
                        + "-fx-font-weight: bold;"
        );

        Label codigo = new Label(compra.getCodigo());
        codigo.setWrapText(true);
        codigo.setStyle(
                "-fx-text-fill: white; "
                        + "-fx-font-size: 15px; "
                        + "-fx-font-weight: bold;"
        );

        Separator separador = new Separator();
        separador.setStyle("-fx-opacity: 0.45;");

        HBox filaPelicula = crearFilaConfirmacion(
                "Película",
                funcion.getPelicula().getTitulo()
        );
        HBox filaAsientos = crearFilaConfirmacion(
                "Asientos",
                asientos
        );

        HBox filaTotal = new HBox();
        filaTotal.setAlignment(Pos.CENTER_LEFT);

        Label textoTotal = new Label("Total pagado");
        textoTotal.setStyle(
                "-fx-text-fill: #c4d3e0; -fx-font-size: 13px;"
        );

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label valorTotal = new Label(
                FORMATO_MONEDA.format(compra.getTotal())
        );
        valorTotal.setStyle(
                "-fx-text-fill: #f2bd39; "
                        + "-fx-font-size: 20px; "
                        + "-fx-font-weight: bold;"
        );

        filaTotal.getChildren().addAll(textoTotal, espacio, valorTotal);
        tarjeta.getChildren().addAll(
                etiquetaCodigo,
                codigo,
                separador,
                filaPelicula,
                filaAsientos,
                filaTotal
        );

        HBox mensajeEntradas = new HBox(10);
        mensajeEntradas.setAlignment(Pos.CENTER_LEFT);
        mensajeEntradas.setPadding(new Insets(4, 2, 2, 2));

        Label iconoEntrada = new Label("▣");
        iconoEntrada.setStyle(
                "-fx-text-fill: #55d6a4; -fx-font-size: 22px;"
        );

        VBox textoEntradas = new VBox(3);
        Label tituloEntradas = new Label("Tus entradas están listas");
        tituloEntradas.setStyle(
                "-fx-text-fill: white; "
                        + "-fx-font-size: 12px; "
                        + "-fx-font-weight: bold;"
        );

        Label detalleEntradas = new Label(
                "Ya quedaron registradas en tu cuenta."
        );
        detalleEntradas.setWrapText(true);
        detalleEntradas.setStyle(
                "-fx-text-fill: #a9bed2; -fx-font-size: 11px;"
        );

        textoEntradas.getChildren().addAll(tituloEntradas, detalleEntradas);
        mensajeEntradas.getChildren().addAll(iconoEntrada, textoEntradas);

        contenido.getChildren().addAll(
                iconoContenedor,
                titulo,
                subtitulo,
                estado,
                tarjeta,
                mensajeEntradas
        );

        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setStyle(
                "-fx-background-color: #0b1b2d; "
                        + "-fx-border-color: #294760;"
        );
        dialogo.showAndWait();
    }

    /** Construye una fila de dato para el resumen de compra. */
    private HBox crearFilaConfirmacion(String etiqueta, String valor) {
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label nombre = new Label(etiqueta);
        nombre.setStyle(
                "-fx-text-fill: #a9bfd2; -fx-font-size: 12px;"
        );

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label dato = new Label(valor == null ? "—" : valor);
        dato.setWrapText(true);
        dato.setMaxWidth(230);
        dato.setStyle(
                "-fx-text-fill: white; "
                        + "-fx-font-size: 12px; "
                        + "-fx-font-weight: bold;"
        );

        fila.getChildren().addAll(nombre, espacio, dato);
        return fila;
    }

    private VBox tarjetaDato(Label contenido) {
        VBox tarjeta = new VBox(5, contenido);
        tarjeta.getStyleClass().add("catalog-metadata-card");
        HBox.setHgrow(tarjeta, Priority.ALWAYS);
        return tarjeta;
    }

    private Label etiquetaDato(String titulo, String valor) {
        Label etiqueta = new Label(titulo + "\n" + valor);
        etiqueta.setWrapText(true);
        etiqueta.getStyleClass().add("catalog-metadata-label");
        return etiqueta;
    }

    private String formatoProyeccion(Funcion funcion) {
        switch (funcion.getFormatoProyeccion()) {
            case DOS_D: return "2D";
            case TRES_D: return "3D";
            case CUATRO_D: return "4D";
            default: return funcion.getFormatoProyeccion().name();
        }
    }

    private VBox estadoVacio(String titulo, String descripcion) {
        VBox caja = new VBox(8);
        caja.setPadding(new Insets(22));
        caja.getStyleClass().add("empty-state");
        Label icono = new Label("✦");
        icono.getStyleClass().add("section-icon");
        Label encabezado = new Label(titulo);
        encabezado.getStyleClass().add("empty-title");
        Label texto = new Label(descripcion);
        texto.setWrapText(true);
        texto.getStyleClass().add("muted-small");
        caja.getChildren().addAll(icono, encabezado, texto);
        return caja;
    }

    private void prepararDialogo(Dialog<?> dialogo, String titulo,
                                 String encabezado, double ancho, double alto) {
        dialogo.setTitle("CinemaUQ · " + titulo);
        dialogo.setHeaderText(encabezado);
        if (ventanaPropietaria != null) {
            dialogo.initOwner(ventanaPropietaria);
        }
        dialogo.initModality(Modality.APPLICATION_MODAL);
        DialogPane pane = dialogo.getDialogPane();
        pane.setPrefSize(ancho, alto);
        pane.getStyleClass().add("cinemauq-dialog");
        pane.setStyle(
                "-fx-background-color: #071629;"
                        + "-fx-control-inner-background: #0b1b2d;"
                        + "-fx-border-color: #344c67;"
                        + "-fx-border-width: 1;"
                        + "-fx-padding: 0;"
        );
        pane.setBackground(new Background(
                new BackgroundFill(
                        Color.web("#071629"),
                        CornerRadii.EMPTY,
                        Insets.EMPTY
                )
        ));
        URL css = CarteleraViewController.class.getResource(
                "/co/edu/uniquindio/proyectofinal/css/styles.css");
        if (css != null && !pane.getStylesheets().contains(css.toExternalForm())) {
            pane.getStylesheets().add(css.toExternalForm());
        }

        pane.sceneProperty().addListener((observable, anterior, escena) -> {
            if (escena == null) return;
            Platform.runLater(() -> {
                Node raizEscena = escena.getRoot();
                if (raizEscena instanceof Region regionRaiz) {
                    regionRaiz.setBackground(new Background(
                            new BackgroundFill(
                                    Color.web("#071629"),
                                    CornerRadii.EMPTY,
                                    Insets.EMPTY
                            )
                    ));
                    regionRaiz.setStyle("-fx-background-color: #071629;");
                }
                pane.applyCss();
                for (String selector : List.of(
                        ".content", ".header-panel", ".button-bar",
                        ".container", ".graphic-container"
                )) {
                    Node nodo = pane.lookup(selector);
                    if (nodo instanceof Region region) {
                        String color = selector.equals(".button-bar")
                                ? "#071629" : "#0b1b2d";
                        region.setBackground(new Background(
                                new BackgroundFill(
                                        Color.web(color),
                                        CornerRadii.EMPTY,
                                        Insets.EMPTY
                                )
                        ));
                        region.setStyle("-fx-background-color: " + color + ";");
                    }
                }
                pane.layout();
            });
        });
    }
}