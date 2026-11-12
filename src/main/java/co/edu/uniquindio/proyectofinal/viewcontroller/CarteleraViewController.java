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
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
                new Separator(), contenedorPeliculas);
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
        TextInputDialog cantidad = new TextInputDialog("1");
        cantidad.setTitle("Lista de espera");
        cantidad.setHeaderText(funcion.getPelicula().getTitulo()
                + " · " + FORMATO_FUNCION.format(funcion.getFechaHora()));
        cantidad.setContentText("¿Cuántos asientos necesitas?");
        if (ventanaPropietaria != null) cantidad.initOwner(ventanaPropietaria);
        cantidad.showAndWait().ifPresent(valor -> {
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
        Dialog<List<Asiento>> dialogo = new Dialog<>();
        prepararDialogo(dialogo, "Seleccionar asientos",
                funcion.getPelicula().getTitulo() + " · "
                        + FORMATO_FUNCION.format(funcion.getFechaHora()),
                820, 680);

        ButtonType continuar = new ButtonType(
                "Confirmar selección", ButtonBar.ButtonData.OK_DONE);
        dialogo.getDialogPane().getButtonTypes().addAll(continuar, ButtonType.CANCEL);

        Map<String, Asiento> seleccionados = new LinkedHashMap<>();
        Map<String, StackPane> controlesPorId = new LinkedHashMap<>();

        Button botonContinuar =
                (Button) dialogo.getDialogPane().lookupButton(continuar);
        botonContinuar.setDisable(true);

        Label resumen = new Label("No has seleccionado asientos.");
        resumen.getStyleClass().add("seat-selection-summary");
        resumen.setWrapText(true);

        VBox contenido = new VBox(16);
        contenido.setPadding(new Insets(20));
        contenido.setAlignment(Pos.TOP_CENTER);
        contenido.setStyle("-fx-background-color: #08192b;");

        Label pantalla = new Label("P A N T A L L A");
        pantalla.setMaxWidth(Double.MAX_VALUE);
        pantalla.setAlignment(Pos.CENTER);
        pantalla.setPadding(new Insets(13, 20, 13, 20));
        pantalla.setStyle(
                "-fx-background-color: linear-gradient(to right, #263e59, #f2bd39, #263e59);"
                        + "-fx-background-radius: 8 8 28 28;"
                        + "-fx-text-fill: #101827;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
        );

        HBox leyenda = new HBox(20,
                crearLeyendaAsiento("Disponible", "seat-legend-available"),
                crearLeyendaAsiento("Seleccionado", "seat-legend-selected"),
                crearLeyendaAsiento("Ocupado", "seat-legend-occupied"));
        leyenda.setAlignment(Pos.CENTER);

        VBox filas = new VBox(11);
        filas.setAlignment(Pos.CENTER);
        filas.setPadding(new Insets(12, 6, 12, 6));

        Map<String, List<Asiento>> asientosPorFila =
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Asiento asiento : carteleraController.consultarAsientos(funcion)) {
            asientosPorFila.computeIfAbsent(
                    asiento.getFila(), clave -> new ArrayList<>()).add(asiento);
        }

        long disponibles = 0;

        for (Map.Entry<String, List<Asiento>> entrada : asientosPorFila.entrySet()) {
            HBox filaVisual = new HBox(9);
            filaVisual.setAlignment(Pos.CENTER);

            Label nombreFila = new Label(entrada.getKey());
            nombreFila.setMinWidth(24);
            nombreFila.setAlignment(Pos.CENTER);
            nombreFila.setStyle(
                    "-fx-text-fill: #f4c34e;"
                            + "-fx-font-size: 13px;"
                            + "-fx-font-weight: bold;"
            );
            filaVisual.getChildren().add(nombreFila);

            entrada.getValue().sort(
                    java.util.Comparator.comparingInt(Asiento::getNumero));

            for (Asiento asiento : entrada.getValue()) {
                String id = asiento.obtenerIdentificador();
                boolean disponible = carteleraController.estaDisponible(funcion, asiento);

                Label numero = new Label(String.valueOf(asiento.getNumero()));
                numero.setMouseTransparent(true);
                numero.setStyle("-fx-text-fill: white; -fx-font-size: 12px;"
                        + "-fx-font-weight: bold;");

                StackPane asientoVisual = new StackPane(numero);
                asientoVisual.setMinSize(43, 38);
                asientoVisual.setPrefSize(43, 38);
                asientoVisual.setMaxSize(43, 38);
                asientoVisual.setPickOnBounds(true);
                asientoVisual.setFocusTraversable(disponible);
                asientoVisual.setAccessibleText("Asiento " + id
                        + (disponible ? " disponible" : " ocupado"));

                if (disponible) {
                    disponibles++;
                    asientoVisual.setStyle(estiloAsientoTile(false));
                    asientoVisual.setCursor(javafx.scene.Cursor.HAND);
                    controlesPorId.put(id, asientoVisual);

                    // Control personalizado: el área completa del asiento recibe el clic.
                    asientoVisual.setOnMouseClicked(event -> {
                        alternarAsientoVisual(
                                id, asiento, asientoVisual, seleccionados,
                                resumen, botonContinuar);
                        event.consume();
                    });

                    // También permite seleccionar con Enter o barra espaciadora.
                    asientoVisual.setOnKeyPressed(event -> {
                        if (event.getCode() == javafx.scene.input.KeyCode.ENTER
                                || event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                            alternarAsientoVisual(
                                    id, asiento, asientoVisual, seleccionados,
                                    resumen, botonContinuar);
                            event.consume();
                        }
                    });

                    asientoVisual.setOnMouseEntered(event -> {
                        if (!seleccionados.containsKey(id)) {
                            asientoVisual.setStyle(estiloAsientoTileHover());
                        }
                    });
                    asientoVisual.setOnMouseExited(event -> {
                        asientoVisual.setStyle(estiloAsientoTile(
                                seleccionados.containsKey(id)));
                    });
                } else {
                    asientoVisual.setStyle(estiloAsientoTileOcupado());
                    asientoVisual.setOpacity(0.68);
                }

                filaVisual.getChildren().add(asientoVisual);
            }

            filas.getChildren().add(filaVisual);
        }

        Label disponibilidad = new Label(
                "Hay " + disponibles + " asientos disponibles. "
                        + "Haz clic directamente sobre un asiento para marcarlo.");
        disponibilidad.setWrapText(true);
        disponibilidad.setAlignment(Pos.CENTER);
        disponibilidad.setStyle("-fx-text-fill: #aebed0; -fx-font-size: 12px;");

        Button limpiar = new Button("Limpiar selección");
        limpiar.setStyle(
                "-fx-background-color: #172d45;"
                        + "-fx-text-fill: #eaf0f7;"
                        + "-fx-border-color: #49627d;"
                        + "-fx-border-radius: 8;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 8 16;"
                        + "-fx-cursor: hand;"
        );
        limpiar.setOnAction(event -> {
            seleccionados.clear();
            controlesPorId.forEach((id, control) -> {
                control.setStyle(estiloAsientoTile(false));
                control.setAccessibleText("Asiento " + id + " disponible");
            });
            actualizarResumenAsientos(seleccionados, resumen);
            botonContinuar.setDisable(true);
        });

        HBox barraAcciones = new HBox(limpiar);
        barraAcciones.setAlignment(Pos.CENTER_RIGHT);

        contenido.getChildren().addAll(
                pantalla, leyenda, disponibilidad, filas,
                new Separator(), resumen, barraAcciones);

        ScrollPane scroll = new ScrollPane(contenido);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background: #08192b; -fx-background-color: #08192b;");
        scroll.setPrefViewportHeight(470);
        dialogo.getDialogPane().setContent(scroll);

        dialogo.setResultConverter(tipo -> tipo == continuar
                ? List.copyOf(seleccionados.values()) : null);

        Optional<List<Asiento>> resultado = dialogo.showAndWait();
        resultado.ifPresent(asientos ->
                mostrarResumenSeleccion(funcion, asientos));
    }

    private void alternarAsientoVisual(
            String id,
            Asiento asiento,
            StackPane control,
            Map<String, Asiento> seleccionados,
            Label resumen,
            Button botonContinuar) {

        boolean marcado;
        if (seleccionados.containsKey(id)) {
            seleccionados.remove(id);
            marcado = false;
        } else {
            seleccionados.put(id, asiento);
            marcado = true;
        }

        control.setStyle(estiloAsientoTile(marcado));
        control.setAccessibleText("Asiento " + id
                + (marcado ? " seleccionado" : " disponible"));
        actualizarResumenAsientos(seleccionados, resumen);
        botonContinuar.setDisable(seleccionados.isEmpty());
    }

    private String estiloAsientoTile(boolean seleccionado) {
        if (seleccionado) {
            return "-fx-background-color: #f2bd39;"
                    + "-fx-background-radius: 9;"
                    + "-fx-border-color: #ffe08a;"
                    + "-fx-border-width: 2;"
                    + "-fx-border-radius: 9;";
        }
        return "-fx-background-color: #183b50;"
                + "-fx-background-radius: 9;"
                + "-fx-border-color: #3f947d;"
                + "-fx-border-width: 1.5;"
                + "-fx-border-radius: 9;";
    }

    private String estiloAsientoTileHover() {
        return "-fx-background-color: #245d58;"
                + "-fx-background-radius: 9;"
                + "-fx-border-color: #8bd5ad;"
                + "-fx-border-width: 2;"
                + "-fx-border-radius: 9;";
    }

    private String estiloAsientoTileOcupado() {
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

    /** Muestra el tiempo de retención y permite pagar o liberar los asientos. */
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
        String total = FORMATO_MONEDA.format(
                reservaController.calcularTotalReserva(reservas));
        int minutos = reservaController.consultarDuracionReservaMinutos();
        java.math.BigDecimal totalNumerico =
                reservaController.calcularTotalReserva(reservas);

        Dialog<ButtonType> dialogo = new Dialog<>();
        prepararDialogo(
                dialogo,
                "Reserva temporal",
                "Tus asientos están retenidos temporalmente",
                590,
                430
        );

        ButtonType continuar = new ButtonType(
                "Continuar al pago", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType(
                "Cancelar reserva", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(continuar, cancelar);

        VBox contenido = new VBox(12);
        contenido.setPadding(new Insets(16));
        contenido.getStyleClass().add("reservation-summary");

        Label pelicula = new Label(funcion.getPelicula().getTitulo());
        pelicula.getStyleClass().add("section-title");

        Label fecha = new Label(
                FORMATO_FUNCION.format(funcion.getFechaHora()));
        fecha.getStyleClass().add("function-meta");

        Label asientosLabel = new Label("Asientos seleccionados: " + seleccion);
        asientosLabel.getStyleClass().add("catalog-synopsis");
        asientosLabel.setWrapText(true);

        Label precio = new Label("Total de entradas: " + total);
        precio.getStyleClass().add("function-meta");

        Label cuentaRegresiva = new Label();
        cuentaRegresiva.getStyleClass().add("reservation-countdown");

        Label aviso = new Label(
                "Los asientos quedan retenidos durante " + minutos
                        + " minutos. Si no completas el pago, se liberarán automáticamente "
                        + "al vencer el tiempo.");
        aviso.setWrapText(true);
        aviso.getStyleClass().add("reservation-expiration-note");

        Optional<TarjetaVirtual> tarjeta = reservaController.consultarTarjeta(cliente);
        String informacionTarjeta;
        boolean tarjetaLista = false;

        if (tarjeta.isEmpty()) {
            informacionTarjeta =
                    "No tienes una tarjeta virtual asociada. Contacta al administrador.";
        } else {
            TarjetaVirtual tarjetaCliente = tarjeta.get();
            boolean activa = tarjetaCliente.getEstado()
                    == co.edu.uniquindio.proyectofinal.model.enums.EstadoTarjeta.ACTIVA;
            informacionTarjeta = "Tarjeta virtual · Saldo disponible: "
                    + FORMATO_MONEDA.format(tarjetaCliente.getSaldo());

            if (!activa) {
                informacionTarjeta += " · Tarjeta bloqueada.";
            }

            tarjetaLista = activa;
        }

        Label pago = new Label(informacionTarjeta
                + "\nPuedes aplicar un código promocional o redimir puntos antes del pago.");
        pago.setWrapText(true);
        pago.getStyleClass().add("muted-small");

        TextField codigoPromocion = new TextField();
        codigoPromocion.setPromptText("Código promocional (opcional)");
        TextField puntosRedimir = new TextField("0");
        puntosRedimir.setPromptText("Puntos a redimir");

        HBox descuentos = new HBox(10, codigoPromocion, puntosRedimir);
        descuentos.setAlignment(Pos.CENTER_LEFT);

        contenido.getChildren().addAll(
                pelicula, fecha, new Separator(), asientosLabel,
                precio, cuentaRegresiva, aviso, descuentos, pago);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.setResultConverter(tipo -> tipo);

        Button botonContinuar = (Button)
                dialogo.getDialogPane().lookupButton(continuar);
        botonContinuar.setDisable(!tarjetaLista);

        // Cuenta regresiva visual. La expiración real también se valida
        // en ReservaService antes de reservar, cancelar o confirmar el pago.
        AtomicBoolean reservaExpirada = new AtomicBoolean(false);
        Runnable actualizarTiempo = () -> {
            long segundos = Math.max(0,
                    Duration.between(LocalDateTime.now(), fechaExpiracion).getSeconds());
            long minutosRestantes = segundos / 60;
            long segundosRestantes = segundos % 60;
            cuentaRegresiva.setText(String.format(
                    "Tiempo restante: %02d:%02d",
                    minutosRestantes,
                    segundosRestantes));
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
        }
        ));
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
                Compra compra = reservaController.confirmarCompra(
                        reservas, cliente, codigoPromocion.getText(), puntos);
                mostrarAlerta(
                        Alert.AlertType.INFORMATION,
                        "¡Compra confirmada!",
                        "Tu pago fue aprobado correctamente.",
                        "Código de compra: " + compra.getCodigo()
                                + "\nPelícula: " + funcion.getPelicula().getTitulo()
                                + "\nAsientos: " + seleccion
                                + "\nTotal pagado: "
                                + FORMATO_MONEDA.format(compra.getTotal())
                                + "\n\nTus entradas ya quedaron registradas."
                );
            } catch (RuntimeException ex) {
                // Si el pago no se confirma, cancelar libera los asientos.
                // Si el tiempo venció durante el proceso, se procesará la expiración.
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

    private void mostrarAlerta(
            Alert.AlertType tipo,
            String titulo,
            String encabezado,
            String contenido
    ) {
        Alert alerta = new Alert(tipo);
        if (ventanaPropietaria != null) {
            alerta.initOwner(ventanaPropietaria);
        }
        alerta.setTitle("CinemaUQ");
        alerta.setHeaderText(encabezado);
        alerta.setContentText(contenido == null ? "" : contenido);
        alerta.showAndWait();
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
        URL css = CarteleraViewController.class.getResource(
                "/co/edu/uniquindio/proyectofinal/css/styles.css");
        if (css != null) {
            pane.getStylesheets().add(css.toExternalForm());
        }
    }
}
