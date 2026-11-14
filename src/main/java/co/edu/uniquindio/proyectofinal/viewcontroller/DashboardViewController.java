package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.controller.CarteleraController;
import co.edu.uniquindio.proyectofinal.controller.CinemaOperationsController;
import co.edu.uniquindio.proyectofinal.controller.DashboardController;
import co.edu.uniquindio.proyectofinal.controller.ReservaController;
import co.edu.uniquindio.proyectofinal.model.principal.*;
import co.edu.uniquindio.proyectofinal.model.util.FechaHoraUtil;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import java.util.WeakHashMap;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador visual del Dashboard compartido.
 * Delegada los módulos de negocio a sus viewcontrollers y mantiene
 * separada la navegación del contenido de cada sección.
 */
public class DashboardViewController {

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

    @FXML private VBox menuContainer;
    @FXML private TextField searchField;
    @FXML private StackPane contentHost;
    @FXML private VBox rightPanel;
    @FXML private ScrollPane contentScroll;
    @FXML private Pane dashboardParticleLayer;

    // Reloj en vivo del panel lateral derecho.
    private Label relojHoraLabel;
    private Label relojFechaLabel;
    private Timeline relojTimeline;

    private static final ZoneId ZONA_CINEMA = ZoneId.of("America/Bogota");
    private static final Locale LOCALE_CINEMA = new Locale("es", "CO");
    private static final DateTimeFormatter FORMATO_HORA_RELOJ =
            DateTimeFormatter.ofPattern("hh:mm:ss a", LOCALE_CINEMA);
    private static final DateTimeFormatter FORMATO_FECHA_RELOJ =
            DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", LOCALE_CINEMA);

    private App aplicacion;
    private Usuario usuario;
    private DashboardController dashboardController;
    private CarteleraController carteleraController;
    private ReservaController reservaController;
    private CinemaOperationsController operaciones;
    private boolean administrador;

    private GestionAdminViewController adminView;
    private ClienteViewController clienteView;

    // Evita acumular transiciones si el cursor entra y sale rápidamente.
    private final Map<Node, ParallelTransition> transicionesHover = new WeakHashMap<>();
    private String seccionActual = "";

    @FXML
    private void initialize() {
        searchField.setOnAction(event -> {
            if (administrador) {
                mostrarSeccion("Gestión de películas", searchField.getText());
            } else {
                mostrarSeccion("Cartelera", searchField.getText());
            }
        });

        iniciarFondoAnimadoDashboard();
    }

    public void configurar(
            App aplicacion,
            Usuario usuario,
            DashboardController dashboardController,
            CarteleraController carteleraController,
            ReservaController reservaController,
            CinemaOperationsController operaciones
    ) {
        this.aplicacion = aplicacion;
        this.usuario = usuario;
        this.dashboardController = dashboardController;
        this.carteleraController = carteleraController;
        this.reservaController = reservaController;
        this.operaciones = operaciones;
        this.administrador = usuario instanceof Administrador;

        if (administrador) {
            adminView = new GestionAdminViewController(
                    operaciones, (Administrador) usuario,
                    aplicacion.getEscenarioPrincipal());
        } else {
            clienteView = new ClienteViewController(
                    operaciones, (Cliente) usuario,
                    aplicacion.getEscenarioPrincipal());
            clienteView.setAlActualizarPerfil(() -> {
                if (!rightPanel.getChildren().isEmpty()) {
                    rightPanel.getChildren().set(0, crearTarjetaPerfil());
                }
            });
            clienteView.setAlActualizarResumen(this::actualizarResumenCliente);
        }

        construirMenu();
        construirPanelLateral();
        mostrarSeccion("Inicio", "");
    }

    private void construirMenu() {
        menuContainer.getChildren().clear();

        List<String[]> opciones = administrador
                ? List.of(
                new String[]{"home", "Inicio"},
                new String[]{"film", "Gestión de películas"},
                new String[]{"clock", "Funciones y salas"},
                new String[]{"user", "Gestión de usuarios"},
                new String[]{"box", "Inventario"},
                new String[]{"popcorn", "Confitería"},
                new String[]{"tag", "Promociones"},
                new String[]{"scan", "Control de acceso"},
                new String[]{"hourglass", "Lista de espera"},
                new String[]{"chart", "Reportes"},
                new String[]{"wallet", "Tarjetas virtuales"}
        )
                : List.of(
                new String[]{"home", "Inicio"},
                new String[]{"film", "Cartelera"},
                new String[]{"popcorn", "Confitería"},
                new String[]{"clock", "Lista de espera"},
                new String[]{"ticket", "Mis entradas"},
                new String[]{"receipt", "Mis compras"},
                new String[]{"wallet", "Mi tarjeta virtual"},
                new String[]{"star", "Mis puntos"},
                new String[]{"heart", "Películas guardadas"},
                new String[]{"edit", "Reseñas"}
        );

        for (String[] opcion : opciones) {
            StackPane icono = crearIconoNavegacion(opcion[0]);
            icono.getStyleClass().add("nav-icon");

            Label texto = new Label(opcion[1]);
            texto.getStyleClass().add("nav-label");

            // Indicador vertical dorado: solo se ilumina en la sección activa.
            Region indicador = new Region();
            indicador.getStyleClass().add("nav-indicator");
            indicador.setMinWidth(4);
            indicador.setPrefWidth(4);
            indicador.setMaxWidth(4);
            indicador.setMinHeight(34);
            indicador.setPrefHeight(34);
            indicador.setMaxHeight(34);

            HBox contenido = new HBox(11, indicador, icono, texto);
            contenido.setAlignment(Pos.CENTER_LEFT);
            contenido.setMouseTransparent(true);

            Button boton = new Button();
            boton.setGraphic(contenido);
            boton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            boton.getStyleClass().add("nav-button");
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setAlignment(Pos.CENTER_LEFT);
            boton.setUserData(opcion[1]);
            boton.setOnAction(event -> mostrarSeccion(opcion[1], ""));
            instalarAnimacionBoton(boton);

            menuContainer.getChildren().add(boton);
        }

        actualizarMenuActivo("Inicio");
        animarEntradaEscalonada(menuContainer.getChildren());
        instalarAnimacionesRecursivas(menuContainer);
    }

    /**
     * Construye iconos vectoriales SVG para que todo el menú use el mismo
     * lenguaje visual, sin emojis ni caracteres dependientes de la fuente.
     */
    private StackPane crearIconoNavegacion(String tipo) {
        String path = switch (tipo) {
            case "home" -> "M3 10.8 L12 3 L21 10.8 V20 H14.5 V14.5 H9.5 V20 H3 Z";
            case "film" -> "M4 3 H20 V21 H4 Z M4 8 H20 M4 16 H20 M8 3 V8 M16 3 V8 M8 16 V21 M16 16 V21";
            case "clock" -> "M12 3 A9 9 0 1 0 12.01 3 M12 7 V12 L16 14";
            case "user" -> "M12 12 A4 4 0 1 0 12 4 A4 4 0 0 0 12 12 M4 21 C4 16 8 14 12 14 C16 14 20 16 20 21";
            case "box" -> "M3 7 L12 3 L21 7 V17 L12 21 L3 17 Z M3 7 L12 11 L21 7 M12 11 V21";
            case "ticket" -> "M3 6 H21 V10 A2 2 0 0 0 21 14 V18 H3 V14 A2 2 0 0 0 3 10 Z M12 7 V9 M12 11 V13 M12 15 V17";
            case "receipt" -> "M5 3 H19 V21 L16 19 L13 21 L10 19 L7 21 L5 19 Z M8 8 H16 M8 12 H16 M8 16 H13";
            case "popcorn" -> "M6 8 H18 L16 21 H8 Z M6 8 L4 4 L8 3 L10 7 L12 3 L15 7 L18 3 L20 8 M9 11 V17 M13 11 V17 M16 11 V17";
            case "tag" -> "M3 12 L12 3 H21 V12 L12 21 Z M16 8 A1 1 0 1 0 16.01 8";
            case "scan" -> "M4 9 V4 H9 M15 4 H20 V9 M20 15 V20 H15 M9 20 H4 V15 M7 12 H17";
            case "hourglass" -> "M6 3 H18 M6 21 H18 M7 3 C7 8 10 9 12 12 C14 9 17 8 17 3 M7 21 C7 16 10 15 12 12 C14 15 17 16 17 21";
            case "chart" -> "M4 20 V11 H8 V20 M10 20 V5 H14 V20 M16 20 V9 H20 V20";
            case "star" -> "M12 2.8 L14.8 8.7 L21.2 9.5 L16.5 14 L17.6 20.5 L12 17.4 L6.4 20.5 L7.5 14 L2.8 9.5 L9.2 8.7 Z";
            case "wallet" -> "M3 6 H20 V20 H3 Z M3 6 L17 3 V6 M15 12 H21 V17 H15 A2.5 2.5 0 0 1 15 12";
            case "heart" -> "M12 20 C10 18 3 13 3 8 A4.5 4.5 0 0 1 12 6 A4.5 4.5 0 0 1 21 8 C21 13 14 18 12 20 Z";
            case "edit" -> "M4 16.5 V20 H7.5 L19 8.5 L15.5 5 Z M13.8 6.7 L17.3 10.2 M4 4 H11";
            case "bell" -> "M18 9 A6 6 0 0 0 6 9 C6 16 3.5 16 3.5 18 H20.5 C20.5 16 18 16 18 9 M10 21 H14";
            default -> "M12 3 V21 M3 12 H21";
        };
        SVGPath vector = new SVGPath();
        vector.setContent(path);
        vector.setFill(javafx.scene.paint.Color.TRANSPARENT);
        vector.setStroke(javafx.scene.paint.Color.web("#AAB6C5"));
        vector.getStyleClass().add("nav-icon-vector");
        vector.setStrokeWidth(1.8);
        vector.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        vector.setStrokeLineJoin(javafx.scene.shape.StrokeLineJoin.ROUND);
        vector.setScaleX(0.82);
        vector.setScaleY(0.82);
        StackPane contenedor = new StackPane(vector);
        contenedor.setMinSize(36, 36);
        contenedor.setPrefSize(36, 36);
        contenedor.setMaxSize(36, 36);
        return contenedor;
    }

    private void actualizarMenuActivo(String seleccion) {
        boolean cambioSeccion = !seleccion.equals(seccionActual);
        seccionActual = seleccion;

        for (Node node : menuContainer.getChildren()) {
            if (node instanceof Button boton) {
                boolean activo = seleccion.equals(boton.getUserData());
                boton.getStyleClass().remove("nav-button-active");
                if (activo) {
                    boton.getStyleClass().add("nav-button-active");
                    if (cambioSeccion) {
                        ScaleTransition pulso = new ScaleTransition(Duration.millis(170), boton);
                        pulso.setFromX(0.985);
                        pulso.setFromY(0.985);
                        pulso.setToX(1.0);
                        pulso.setToY(1.0);
                        pulso.play();
                    }
                }

                // El icono inactivo se muestra gris; el activo recupera el dorado.
                if (boton.getGraphic() instanceof HBox contenido) {
                    for (Node elemento : contenido.getChildren()) {
                        if (elemento instanceof StackPane icono
                                && icono.getStyleClass().contains("nav-icon")) {
                            for (Node forma : icono.getChildren()) {
                                if (forma instanceof SVGPath vector) {
                                    vector.setStroke(activo
                                            ? javafx.scene.paint.Color.web("#F5B82E")
                                            : javafx.scene.paint.Color.web("#AAB6C5"));
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void construirPanelLateral() {
        rightPanel.getChildren().clear();
        rightPanel.getChildren().add(crearTarjetaPerfil());

        if (administrador) {
            rightPanel.getChildren().add(crearPanelResumenAdmin());

            VBox informacion = panelInfo(
                    "Centro de administración",
                    "Gestiona el catálogo, las salas, la programación, los usuarios y la operación del cine.");
            informacion.getStyleClass().add("side-card-bottom");
            informacion.setMaxHeight(Double.MAX_VALUE);
            informacion.setAlignment(Pos.CENTER_LEFT);
            rightPanel.getChildren().add(informacion);
            VBox.setVgrow(informacion, Priority.ALWAYS);
        } else {
            rightPanel.getChildren().add(crearPanelPuntos());
            rightPanel.getChildren().add(crearPanelTarjetaVirtual());

            // Actividad reciente del cliente.
            rightPanel.getChildren().add(crearPanelActividadReciente());

            // El espacio flexible empuja el reloj hacia la parte inferior
            // del panel derecho cuando hay altura disponible.
            Region espacioFlexible = new Region();
            VBox.setVgrow(espacioFlexible, Priority.ALWAYS);
            rightPanel.getChildren().add(espacioFlexible);

            // Reloj digital en vivo con la hora local de Colombia.
            rightPanel.getChildren().add(crearPanelReloj());
        }

        animarEntradaEscalonada(rightPanel.getChildren());
        instalarAnimacionesRecursivas(rightPanel);
    }

    /**
     * Construye una tarjeta de reloj digital para ocupar el espacio libre
     * de la parte inferior del panel derecho.
     */
    private VBox crearPanelReloj() {
        VBox card = new VBox(9);
        card.getStyleClass().addAll("side-card", "dashboard-clock-card");

        HBox encabezado = new HBox(8);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        Label icono = new Label("◷");
        icono.getStyleClass().add("dashboard-clock-icon");

        Label titulo = new Label("Hora actual");
        titulo.getStyleClass().add("side-card-title");

        Region separador = new Region();
        HBox.setHgrow(separador, Priority.ALWAYS);

        Label enVivo = new Label("EN VIVO");
        enVivo.getStyleClass().add("dashboard-clock-live");

        encabezado.getChildren().addAll(icono, titulo, separador, enVivo);

        relojHoraLabel = new Label("--:--:--");
        relojHoraLabel.getStyleClass().add("dashboard-clock-time");

        relojFechaLabel = new Label("Cargando fecha...");
        relojFechaLabel.getStyleClass().add("dashboard-clock-date");

        HBox pie = new HBox(7);
        pie.setAlignment(Pos.CENTER_LEFT);

        Label punto = new Label("●");
        punto.getStyleClass().add("dashboard-clock-dot");

        Label zona = new Label("HORA LOCAL · COLOMBIA");
        zona.getStyleClass().add("dashboard-clock-zone");

        pie.getChildren().addAll(punto, zona);
        card.getChildren().addAll(encabezado, relojHoraLabel, relojFechaLabel, pie);

        // Primera actualización inmediata y luego una actualización por segundo.
        actualizarReloj();
        if (relojTimeline != null) {
            relojTimeline.stop();
        }
        relojTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), evento -> actualizarReloj())
        );
        relojTimeline.setCycleCount(Animation.INDEFINITE);
        relojTimeline.play();

        return card;
    }

    /** Actualiza hora y fecha usando la zona horaria de Colombia. */
    private void actualizarReloj() {
        if (relojHoraLabel == null || relojFechaLabel == null) {
            return;
        }

        ZonedDateTime ahora = ZonedDateTime.now(ZONA_CINEMA);
        relojHoraLabel.setText(
                FORMATO_HORA_RELOJ.format(ahora).toLowerCase(LOCALE_CINEMA)
        );
        relojFechaLabel.setText(
                FORMATO_FECHA_RELOJ.format(ahora).toLowerCase(LOCALE_CINEMA)
        );
    }

    /** Muestra la siguiente entrada comprada que todavía corresponde a una función futura. */
    private VBox crearPanelProximaFuncion() {
        VBox card = tarjetaLateral("Tu próxima función", "🎟");
        if (!(usuario instanceof Cliente cliente)) return card;

        LocalDateTime ahora = LocalDateTime.now();
        Optional<Entrada> proxima = operaciones.consultarEntradas(cliente).stream()
                .filter(e -> e.getEstado() != null && e.getEstado().name().equals("EMITIDA"))
                .filter(e -> e.getFuncion().getFechaHora().isAfter(ahora))
                .min(Comparator.comparing(e -> e.getFuncion().getFechaHora()));

        if (proxima.isEmpty()) {
            Label vacio = new Label("Aún no tienes funciones futuras compradas.");
            vacio.getStyleClass().add("muted-small");
            vacio.setWrapText(true);
            Button ir = botonLateral("Explorar cartelera", () -> mostrarSeccion("Cartelera", ""));
            card.getChildren().addAll(vacio, ir);
            return card;
        }

        Entrada entrada = proxima.get();
        Funcion funcion = entrada.getFuncion();
        Label pelicula = new Label(funcion.getPelicula().getTitulo());
        pelicula.getStyleClass().add("side-feature-title");
        Label fecha = new Label(funcion.getFechaHora().format(
                DateTimeFormatter.ofPattern("EEE d MMM · h:mm a", new Locale("es", "CO"))));
        fecha.getStyleClass().add("muted-small");
        Label datos = new Label(funcion.getSala().getNombre() + " · Asiento "
                + entrada.getAsiento().obtenerIdentificador());
        datos.getStyleClass().add("muted-small");
        datos.setWrapText(true);
        card.getChildren().addAll(pelicula, fecha, datos,
                botonLateral("Ver mis entradas", () -> mostrarSeccion("Mis entradas", "")));
        return card;
    }

    /** Resume la compra más reciente registrada para el cliente. */
    private VBox crearPanelActividadReciente() {
        VBox card = tarjetaLateral("Actividad reciente", "↻");
        if (!(usuario instanceof Cliente cliente)) return card;

        Optional<Compra> reciente = operaciones.consultarCompras(cliente).stream()
                .max(Comparator.comparing(Compra::getFecha));
        if (reciente.isEmpty()) {
            Label vacio = new Label("Tus compras aparecerán aquí cuando realices la primera.");
            vacio.getStyleClass().add("muted-small");
            vacio.setWrapText(true);
            card.getChildren().addAll(vacio,
                    botonLateral("Ir a confitería", () -> mostrarSeccion("Confitería", "")));
            return card;
        }

        Compra compra = reciente.get();
        Label codigo = new Label("Compra " + compra.getCodigo());
        codigo.getStyleClass().add("side-feature-title");
        Label fecha = new Label(compra.getFecha().format(
                DateTimeFormatter.ofPattern("d MMM yyyy · h:mm a", new Locale("es", "CO"))));
        fecha.getStyleClass().add("muted-small");
        Label total = new Label("Total: " + java.text.NumberFormat
                .getCurrencyInstance(new Locale("es", "CO")).format(compra.getTotal()));
        total.getStyleClass().add("side-feature-value");
        Label estado = new Label("Estado: " + compra.consultarNombreEstado());
        estado.getStyleClass().add("muted-small");
        card.getChildren().addAll(codigo, fecha, total, estado,
                botonLateral("Ver mis compras", () -> mostrarSeccion("Mis compras", "")));
        return card;
    }

    /** Recomienda una película activa que no aparezca en las entradas ya adquiridas. */
    private VBox crearPanelRecomendacion() {
        VBox card = tarjetaLateral("Recomendación", "✦");
        if (!(usuario instanceof Cliente cliente)) return card;

        Set<String> peliculasCompradas = operaciones.consultarEntradas(cliente).stream()
                .map(e -> e.getFuncion().getPelicula().getCodigo())
                .collect(Collectors.toSet());
        Optional<Pelicula> recomendada = dashboardController.consultarPeliculasDestacadas()
                .stream()
                .filter(Pelicula::estaActiva)
                .filter(p -> !peliculasCompradas.contains(p.getCodigo()))
                .findFirst();

        if (recomendada.isEmpty()) {
            Label texto = new Label("Ya has explorado nuestras películas destacadas. ¡Revisa la cartelera!");
            texto.getStyleClass().add("muted-small");
            texto.setWrapText(true);
            card.getChildren().addAll(texto,
                    botonLateral("Ver cartelera", () -> mostrarSeccion("Cartelera", "")));
            return card;
        }

        Pelicula pelicula = recomendada.get();
        Label titulo = new Label(pelicula.getTitulo());
        titulo.getStyleClass().add("side-feature-title");
        Label detalle = new Label(pelicula.getGenero() + " · "
                + pelicula.getDuracionMinutos() + " min");
        detalle.getStyleClass().add("muted-small");
        detalle.setWrapText(true);
        card.getChildren().addAll(titulo, detalle,
                botonLateral("Descubrir película", () -> mostrarSeccion("Cartelera", pelicula.getTitulo())));
        return card;
    }

    /** Alertas reales de inventario para el perfil administrativo. */
    private VBox crearPanelAlertasAdmin() {
        VBox card = tarjetaLateral("Alertas operativas", "⚠");
        List<Producto> porReponer = operaciones.consultarProductosPorReponer();
        List<Funcion> proximas = dashboardController.consultarProximasFunciones();

        Label inventario = new Label(porReponer.isEmpty()
                ? "Inventario: sin productos por reponer."
                : "Inventario: " + porReponer.size() + " producto(s) requieren reposición.");
        inventario.getStyleClass().add(porReponer.isEmpty()
                ? "muted-small" : "side-alert-text");
        inventario.setWrapText(true);

        Label funciones = new Label("Funciones próximas: " + proximas.size()
                + " en los próximos días.");
        funciones.getStyleClass().add("muted-small");
        funciones.setWrapText(true);

        card.getChildren().addAll(inventario, funciones,
                botonLateral("Revisar inventario", () -> mostrarSeccion("Inventario", "")));
        if (!porReponer.isEmpty()) {
            Label nombres = new Label(porReponer.stream().limit(3)
                    .map(Producto::getNombre).collect(Collectors.joining(" · ")));
            nombres.getStyleClass().add("muted-small");
            nombres.setWrapText(true);
            card.getChildren().add(2, nombres);
        }
        return card;
    }

    private VBox tarjetaLateral(String titulo, String icono) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("side-card", "side-live-card");
        HBox encabezado = new HBox(8);
        encabezado.setAlignment(Pos.CENTER_LEFT);
        Label simbolo = new Label(icono);
        simbolo.getStyleClass().add("side-metric-icon");
        Label nombre = new Label(titulo);
        nombre.getStyleClass().add("side-card-title");
        encabezado.getChildren().addAll(simbolo, nombre);
        card.getChildren().add(encabezado);
        return card;
    }

    private Button botonLateral(String texto, Runnable accion) {
        Button boton = new Button(texto);
        boton.getStyleClass().add("side-action-button");
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setOnAction(e -> accion.run());
        return boton;
    }

    private VBox crearTarjetaPerfil() {
        VBox card = new VBox(8);
        card.getStyleClass().add("side-card");
        HBox fila = new HBox(12);
        fila.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(iniciales(usuario.getNombre()));
        avatar.getStyleClass().add("avatar-circle");
        VBox datos = new VBox(4);
        Label nombre = new Label(usuario.getNombre());
        nombre.getStyleClass().add("profile-name");
        Label rol = new Label(administrador ? "Administrador" : "Cliente");
        rol.getStyleClass().add("profile-role");
        datos.getChildren().addAll(nombre, rol);
        fila.getChildren().addAll(avatar, datos);

        Label correo = new Label(usuario.getCorreo());
        correo.getStyleClass().add("muted-small");
        correo.setWrapText(true);
        card.getChildren().addAll(fila, correo);
        card.setCursor(javafx.scene.Cursor.HAND);
        card.setFocusTraversable(true);
        card.setOnMouseClicked(event -> mostrarSeccion("Mi perfil", ""));
        card.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER
                    || event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                mostrarSeccion("Mi perfil", "");
                event.consume();
            }
        });
        card.setAccessibleText("Abrir perfil de " + usuario.getNombre());
        Tooltip.install(card, new Tooltip("Ver y editar mi perfil"));
        instalarAnimacionTarjeta(card);
        return card;
    }

    private VBox crearPanelPuntos() {
        VBox card = new VBox(9);
        card.getStyleClass().addAll("side-card", "side-metric-card", "side-points-card");

        HBox encabezado = new HBox(8);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        Label icono = new Label("★");
        icono.getStyleClass().add("side-metric-icon");

        Label title = new Label("Mis puntos");
        title.getStyleClass().add("side-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label chip = new Label("RECOMPENSAS");
        chip.getStyleClass().add("side-card-chip");
        encabezado.getChildren().addAll(icono, title, spacer, chip);

        int saldo = Math.max(0, dashboardController.consultarPuntos(usuario));

        Label value = new Label(String.format("%,d", saldo));
        value.getStyleClass().addAll("points-value", "side-metric-value");

        Label unidad = new Label(saldo == 1 ? "punto disponible" : "puntos disponibles");
        unidad.getStyleClass().add("muted-small");

        Region separador = new Region();
        separador.getStyleClass().add("side-card-separator");

        Label descripcion = new Label("Acumula puntos con tus compras y canjéalos por beneficios.");
        descripcion.getStyleClass().add("side-metric-description");
        descripcion.setWrapText(true);

        card.getChildren().addAll(encabezado, value, unidad, separador, descripcion);
        return card;
    }

    private VBox crearPanelTarjetaVirtual() {
        VBox card = new VBox(9);
        card.getStyleClass().addAll("side-card", "side-metric-card", "side-credit-card");

        HBox encabezado = new HBox(8);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        Label icono = new Label("▣");
        icono.getStyleClass().add("side-metric-icon");

        Label title = new Label("Mi tarjeta virtual");
        title.getStyleClass().add("side-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label chip = new Label("CINEMA UQ");
        chip.getStyleClass().add("side-card-chip");
        encabezado.getChildren().addAll(icono, title, spacer, chip);

        Optional<TarjetaVirtual> tarjetaOpt = operaciones.consultarTarjeta((Cliente) usuario);
        if (tarjetaOpt.isEmpty()) {
            Label sinTarjeta = new Label("No hay una tarjeta registrada.");
            sinTarjeta.getStyleClass().add("muted-small");
            sinTarjeta.setWrapText(true);
            card.getChildren().addAll(encabezado, sinTarjeta);
            return card;
        }

        TarjetaVirtual tarjeta = tarjetaOpt.get();
        String numero = tarjeta.getNumero() == null ? "••••" :
                "•••• " + tarjeta.getNumero().substring(
                        Math.max(0, tarjeta.getNumero().length() - 4));

        Label numeroLabel = new Label(numero);
        numeroLabel.getStyleClass().add("side-card-number");

        Label saldoTitulo = new Label("SALDO DISPONIBLE");
        saldoTitulo.getStyleClass().add("side-card-overline");

        Label saldo = new Label(java.text.NumberFormat
                .getCurrencyInstance(new Locale("es", "CO"))
                .format(tarjeta.getSaldo()));
        saldo.getStyleClass().add("side-credit-value");

        HBox estadoFila = new HBox(6);
        estadoFila.setAlignment(Pos.CENTER_LEFT);
        Label estadoPunto = new Label("●");
        estadoPunto.getStyleClass().add("side-status-dot");
        Label estado = new Label(tarjeta.getEstado() == null
                ? "Estado no disponible" : tarjeta.getEstado().name());
        estado.getStyleClass().add("side-card-status");
        estadoFila.getChildren().addAll(estadoPunto, estado);

        card.getChildren().addAll(encabezado, numeroLabel, saldoTitulo, saldo, estadoFila);
        return card;
    }

    /**
     * Reconstruye los indicadores laterales desde las fuentes reales del cliente.
     * No mantiene saldos ni puntos duplicados en variables locales.
     */
    private void actualizarResumenCliente() {
        if (administrador || rightPanel.getChildren().size() < 3) {
            return;
        }
        rightPanel.getChildren().set(1, crearPanelPuntos());
        rightPanel.getChildren().set(2, crearPanelTarjetaVirtual());
        if (rightPanel.getChildren().size() > 3) {
            rightPanel.getChildren().set(3, crearPanelActividadReciente());
        }
    }

    private VBox crearPanelResumenAdmin() {
        VBox card = new VBox(10);
        card.getStyleClass().add("side-card");
        Label titulo = new Label("Resumen del sistema");
        titulo.getStyleClass().add("side-card-title");
        card.getChildren().addAll(
                titulo,
                lineaResumen("Películas", dashboardController.consultarTotalPeliculas()),
                lineaResumen("Usuarios", dashboardController.consultarTotalUsuarios()),
                lineaResumen("Productos", dashboardController.consultarTotalProductos()),
                lineaResumen("Funciones próximas", dashboardController.consultarTotalFuncionesProximas())
        );
        return card;
    }

    private HBox lineaResumen(String texto, int valor) {
        HBox row = new HBox(8);
        Label t = new Label(texto);
        t.getStyleClass().add("muted-small");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label v = new Label(String.valueOf(valor));
        v.getStyleClass().add("summary-value");
        row.getChildren().addAll(t, spacer, v);
        return row;
    }

    private VBox panelInfo(String titulo, String descripcion) {
        VBox card = new VBox(7);
        card.getStyleClass().add("side-card");
        Label t = new Label(titulo);
        t.getStyleClass().add("side-card-title");
        Label d = new Label(descripcion);
        d.setWrapText(true);
        d.getStyleClass().add("muted-small");
        card.getChildren().addAll(t, d);
        return card;
    }

    private void mostrarSeccion(String seccion, String filtro) {
        actualizarMenuActivo(seccion);
        Node contenido;

        if (seccion.equals("Inicio")) {
            contenido = administrador ? construirInicioAdmin() : construirInicioCliente();
        } else if (seccion.equals("Mi perfil")) {
            contenido = administrador
                    ? construirPerfilAdmin()
                    : clienteView.construir("Mi perfil");
        } else if (!administrador && seccion.equals("Cartelera")) {
            contenido = construirCartelera(filtro);
        } else if (administrador) {
            contenido = adminView.construir(seccion);
        } else {
            contenido = clienteView.construir(seccion);
        }

        contentHost.getChildren().setAll(contenido);
        animarEntrada(contenido, 0, false);
        instalarAnimacionesRecursivas(contenido);
        animarTarjetasEscalonadas(contenido);
        contentScroll.setVvalue(0);
        // También sincroniza los indicadores al volver de Cartelera u otra sección.
        actualizarResumenCliente();
    }


    // ------------------------------------------------------------------
    // FONDO AMBIENTAL ANIMADO DEL DASHBOARD
    // ------------------------------------------------------------------

    /**
     * Añade halos y partículas ambientales detrás de la interfaz.
     * La capa es transparente a los eventos de ratón, por lo que no
     * interfiere con botones, campos, cartelera ni navegación.
     */
    private void iniciarFondoAnimadoDashboard() {
        if (dashboardParticleLayer == null) {
            return;
        }

        // Halos luminosos grandes y de baja opacidad.
        Circle haloAzul = new Circle(215);
        haloAzul.setManaged(false);
        haloAzul.setFill(Color.web("#2368a4", 0.15));

        Circle haloDorado = new Circle(150);
        haloDorado.setManaged(false);
        haloDorado.setFill(Color.web("#d99b24", 0.105));

        Circle haloAzulSecundario = new Circle(105);
        haloAzulSecundario.setManaged(false);
        haloAzulSecundario.setFill(Color.web("#3b83bd", 0.095));

        dashboardParticleLayer.getChildren().addAll(
                haloAzul, haloDorado, haloAzulSecundario
        );

        Runnable posicionar = () -> {
            double ancho = dashboardParticleLayer.getWidth();
            double alto = dashboardParticleLayer.getHeight();

            haloAzul.setCenterX(ancho * 0.69);
            haloAzul.setCenterY(alto * 0.22);

            haloDorado.setCenterX(ancho * 0.91);
            haloDorado.setCenterY(alto * 0.78);

            haloAzulSecundario.setCenterX(ancho * 0.43);
            haloAzulSecundario.setCenterY(alto * 0.87);
        };

        dashboardParticleLayer.widthProperty().addListener(
                (obs, anterior, nuevo) -> posicionar.run()
        );
        dashboardParticleLayer.heightProperty().addListener(
                (obs, anterior, nuevo) -> posicionar.run()
        );
        posicionar.run();

        animarHaloDashboard(haloAzul, 1.10, -18, 9.5);
        animarHaloDashboard(haloDorado, 1.14, 16, 11.0);
        animarHaloDashboard(haloAzulSecundario, 1.18, -12, 8.0);

        // Partículas pequeñas con trayectorias y tiempos desfasados.
        for (int i = 0; i < 30; i++) {
            double radio = 1.15 + (i % 4) * 0.62;
            Circle particula = new Circle(radio);
            particula.setManaged(false);
            particula.setFill(i % 5 == 0
                    ? Color.web("#ffd36a", 0.88)
                    : Color.web("#91bde7", 0.58));

            double proporcionX = 0.24 + ((i * 37) % 73) / 100.0;
            double proporcionY = 0.035 + ((i * 53) % 91) / 100.0;
            particula.layoutXProperty().bind(
                    dashboardParticleLayer.widthProperty().multiply(proporcionX)
            );
            particula.layoutYProperty().bind(
                    dashboardParticleLayer.heightProperty().multiply(proporcionY)
            );
            dashboardParticleLayer.getChildren().add(particula);

            FadeTransition brillo = new FadeTransition(
                    Duration.seconds(2.6 + (i % 5) * 0.72), particula
            );
            brillo.setFromValue(0.12);
            brillo.setToValue(i % 3 == 0 ? 0.92 : 0.62);
            brillo.setAutoReverse(true);
            brillo.setCycleCount(Animation.INDEFINITE);
            brillo.setDelay(Duration.millis(i * 115L));
            brillo.setInterpolator(Interpolator.EASE_BOTH);

            TranslateTransition deriva = new TranslateTransition(
                    Duration.seconds(6.0 + (i % 6) * 1.1), particula
            );
            deriva.setFromY(0);
            deriva.setToY(-13 - (i % 6) * 4.2);
            deriva.setFromX(0);
            deriva.setToX((i % 2 == 0 ? 1 : -1) * (7 + (i % 5) * 3));
            deriva.setAutoReverse(true);
            deriva.setCycleCount(Animation.INDEFINITE);
            deriva.setDelay(Duration.millis(i * 90L));
            deriva.setInterpolator(Interpolator.EASE_BOTH);

            new ParallelTransition(brillo, deriva).play();
        }
    }

    private void animarHaloDashboard(
            Circle halo,
            double escalaFinal,
            double desplazamientoY,
            double duracionSegundos
    ) {
        ScaleTransition pulso = new ScaleTransition(
                Duration.seconds(duracionSegundos), halo
        );
        pulso.setFromX(0.94);
        pulso.setFromY(0.94);
        pulso.setToX(escalaFinal);
        pulso.setToY(escalaFinal);
        pulso.setAutoReverse(true);
        pulso.setCycleCount(Animation.INDEFINITE);
        pulso.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition movimiento = new TranslateTransition(
                Duration.seconds(duracionSegundos + 1.8), halo
        );
        movimiento.setFromY(0);
        movimiento.setToY(desplazamientoY);
        movimiento.setAutoReverse(true);
        movimiento.setCycleCount(Animation.INDEFINITE);
        movimiento.setInterpolator(Interpolator.EASE_BOTH);

        new ParallelTransition(pulso, movimiento).play();
    }

    // ------------------------------------------------------------------
    // ANIMACIONES DEL DASHBOARD
    // ------------------------------------------------------------------

    /**
     * Entrada ligera para el contenido de cada sección. La animación se ejecuta
     * al navegar, sin bloquear eventos ni operaciones del usuario.
     */
    private void animarEntrada(Node nodo, long retrasoMs, boolean esTarjeta) {
        if (nodo == null) return;

        nodo.setOpacity(0);
        nodo.setTranslateY(esTarjeta ? 11 : 8);
        if (esTarjeta) {
            nodo.setScaleX(0.985);
            nodo.setScaleY(0.985);
        }

        Runnable reproducir = () -> {
            FadeTransition aparecer = new FadeTransition(
                    Duration.millis(esTarjeta ? 260 : 300), nodo);
            aparecer.setFromValue(0);
            aparecer.setToValue(1);
            aparecer.setInterpolator(Interpolator.EASE_OUT);

            TranslateTransition subir = new TranslateTransition(
                    Duration.millis(esTarjeta ? 310 : 340), nodo);
            subir.setFromY(esTarjeta ? 11 : 8);
            subir.setToY(0);
            subir.setInterpolator(Interpolator.EASE_OUT);

            ParallelTransition entrada = new ParallelTransition(aparecer, subir);
            if (esTarjeta) {
                ScaleTransition escala = new ScaleTransition(Duration.millis(300), nodo);
                escala.setFromX(0.985);
                escala.setFromY(0.985);
                escala.setToX(1);
                escala.setToY(1);
                escala.setInterpolator(Interpolator.EASE_OUT);
                entrada.getChildren().add(escala);
            }
            entrada.play();
        };

        if (retrasoMs <= 0) {
            reproducir.run();
        } else {
            PauseTransition pausa = new PauseTransition(Duration.millis(retrasoMs));
            pausa.setOnFinished(evento -> reproducir.run());
            pausa.play();
        }
    }

    /** Da entrada escalonada a las tarjetas visibles del panel derecho. */
    private void animarEntradaEscalonada(List<Node> nodos) {
        int indice = 0;
        for (Node nodo : nodos) {
            animarEntrada(nodo, indice * 65L, true);
            indice++;
        }
    }

    /**
     * Busca tarjetas dentro de cualquier vista: Inicio, Cartelera, Confitería,
     * compras, entradas, perfil, panel administrativo y módulos auxiliares.
     */
    private void animarTarjetasEscalonadas(Node raiz) {
        List<Node> tarjetas = new java.util.ArrayList<>();
        recolectarTarjetas(raiz, tarjetas);

        int indice = 0;
        for (Node tarjeta : tarjetas) {
            // Limita el retraso total para no hacer esperar al usuario en listas largas.
            long retraso = Math.min(indice * 42L, 420L);
            animarEntrada(tarjeta, retraso, true);
            indice++;
        }
    }

    private void recolectarTarjetas(Node nodo, List<Node> resultado) {
        if (nodo == null) return;

        if (esTarjetaAnimable(nodo)) {
            resultado.add(nodo);
        }
        if (nodo instanceof Parent padre) {
            for (Node hijo : padre.getChildrenUnmodifiable()) {
                recolectarTarjetas(hijo, resultado);
            }
        }
    }

    private boolean esTarjetaAnimable(Node nodo) {
        if (nodo.getStyleClass() == null) return false;

        List<String> clases = nodo.getStyleClass();
        String[] candidatas = {
                "hero-banner", "movie-card", "function-card", "quick-access-button",
                "stat-card", "side-card", "side-metric-card", "side-live-card",
                "ticket-card", "tickets-summary", "tickets-empty", "catalog-movie-card",
                "content-card", "empty-state", "reservation-summary",
                "catalog-metadata-card", "catalog-function-row", "tickets-summary",
                "profile-identity-card", "profile-data-card", "profile-extra-card",
                "interactive-card"
        };
        for (String clase : candidatas) {
            if (clases.contains(clase)) return true;
        }
        return false;
    }

    /** Instala efectos sin reemplazar los controladores onAction existentes. */
    private void instalarAnimacionesRecursivas(Node raiz) {
        if (raiz == null) return;

        if (raiz instanceof Button boton) {
            instalarAnimacionBoton(boton);
        } else if (esTarjetaAnimable(raiz)) {
            instalarAnimacionTarjeta(raiz);
        }

        if (raiz instanceof Parent padre) {
            for (Node hijo : padre.getChildrenUnmodifiable()) {
                instalarAnimacionesRecursivas(hijo);
            }
        }
    }

    private void instalarAnimacionBoton(Button boton) {
        if (Boolean.TRUE.equals(boton.getProperties().get("cinemauq-button-animation"))) return;
        boton.getProperties().put("cinemauq-button-animation", Boolean.TRUE);

        boton.addEventHandler(MouseEvent.MOUSE_ENTERED,
                evento -> transicionarNodo(boton, 1.025, -1.0, 135));
        boton.addEventHandler(MouseEvent.MOUSE_EXITED,
                evento -> transicionarNodo(boton, 1.0, 0, 145));
        boton.addEventHandler(MouseEvent.MOUSE_PRESSED,
                evento -> transicionarNodo(boton, 0.975, 1.0, 75));
        boton.addEventHandler(MouseEvent.MOUSE_RELEASED,
                evento -> transicionarNodo(boton, boton.isHover() ? 1.025 : 1.0,
                        boton.isHover() ? -1.0 : 0, 110));
    }

    private void instalarAnimacionTarjeta(Node tarjeta) {
        if (Boolean.TRUE.equals(tarjeta.getProperties().get("cinemauq-card-animation"))) return;
        tarjeta.getProperties().put("cinemauq-card-animation", Boolean.TRUE);

        tarjeta.addEventHandler(MouseEvent.MOUSE_ENTERED,
                evento -> transicionarNodo(tarjeta, 1.012, -2.2, 170));
        tarjeta.addEventHandler(MouseEvent.MOUSE_EXITED,
                evento -> transicionarNodo(tarjeta, 1.0, 0, 190));
    }

    /** Interrumpe la animación anterior del mismo nodo para evitar vibraciones. */
    private void transicionarNodo(Node nodo, double escala, double desplazamientoY, int duracionMs) {
        ParallelTransition anterior = transicionesHover.remove(nodo);
        if (anterior != null) anterior.stop();

        ScaleTransition cambioEscala = new ScaleTransition(Duration.millis(duracionMs), nodo);
        cambioEscala.setToX(escala);
        cambioEscala.setToY(escala);

        TranslateTransition movimiento = new TranslateTransition(
                Duration.millis(duracionMs), nodo);
        movimiento.setToY(desplazamientoY);

        ParallelTransition transicion = new ParallelTransition(cambioEscala, movimiento);
        transicionesHover.put(nodo, transicion);
        transicion.play();
    }

    private Node construirCartelera(String filtro) {
        CarteleraViewController vista = new CarteleraViewController(
                carteleraController,
                reservaController,
                operaciones,
                (Cliente) usuario,
                aplicacion.getEscenarioPrincipal(),
                searchField,
                filtro
        );

        // Mantiene sincronizados los puntos, la tarjeta virtual y la actividad
        // reciente cuando el cliente confirma una compra de entradas.
        vista.setAlActualizarResumen(this::actualizarResumenCliente);

        return vista.construirVista();
    }

    private VBox construirInicioCliente() {
        VBox root = new VBox(20);
        root.getStyleClass().add("dashboard-main");

        HBox hero = new HBox(20);
        hero.getStyleClass().add("hero-banner");
        hero.setAlignment(Pos.CENTER_LEFT);

        VBox copy = new VBox(10);
        Label eyebrow = new Label("CINEMA EXPERIENCE");
        eyebrow.getStyleClass().add("hero-eyebrow");
        Label title = new Label("¡Bienvenido a\nCinemaUQ!");
        title.getStyleClass().add("hero-heading");
        Label description = new Label(
                "Disfruta los estrenos, consulta funciones y vive la experiencia del cine.");
        description.setWrapText(true);
        description.getStyleClass().add("hero-description-dashboard");
        Button cartelera = new Button("▶  Ver cartelera");
        cartelera.getStyleClass().add("button-gold");
        cartelera.setOnAction(e -> mostrarSeccion("Cartelera", ""));
        copy.getChildren().addAll(eyebrow, title, description, cartelera);
        HBox.setHgrow(copy, Priority.ALWAYS);

        Label icon = new Label("🎬");
        icon.getStyleClass().add("hero-illustration");
        hero.getChildren().addAll(copy, icon);

        HBox accesos = new HBox(10);
        accesos.getStyleClass().add("quick-access-row");
        agregarAcceso(accesos, "🎞", "Cartelera");
        agregarAcceso(accesos, "🎟", "Mis entradas");
        agregarAcceso(accesos, "🛍", "Mis compras");
        agregarAcceso(accesos, "⭐", "Mis puntos");

        Label tituloPeliculas = new Label("Cartelera destacada");
        tituloPeliculas.getStyleClass().add("section-title");
        HBox peliculas = new HBox(12);
        List<Pelicula> destacadas = dashboardController.consultarPeliculasDestacadas();
        if (destacadas.isEmpty()) {
            peliculas.getChildren().add(estadoVacio(
                    "Todavía no hay películas",
                    "El administrador debe registrar películas y programar funciones."));
        } else {
            for (int i = 0; i < destacadas.size(); i++) {
                Pelicula p = destacadas.get(i);
                VBox tarjeta = new VBox(8);
                tarjeta.getStyleClass().add("movie-card");
                StackPane poster = new StackPane();
                poster.getStyleClass().add("movie-poster");
                poster.setMinSize(150, 210);
                poster.setPrefSize(150, 210);
                poster.setMaxSize(150, 210);

                ImageView imagenPortada = crearImagenPortada(p);
                if (imagenPortada != null) {
                    poster.getChildren().add(imagenPortada);
                } else {
                    // Si una película nueva no tiene portada asignada,
                    // conserva el diseño genérico de CinemaUQ.
                    poster.getStyleClass().add("poster-tone-" + (i % 5));
                    Label nombre = new Label(p.getTitulo());
                    nombre.setWrapText(true);
                    nombre.getStyleClass().add("poster-title");
                    poster.getChildren().add(nombre);
                }

                Label info = new Label(p.getGenero() + " · " + p.getDuracionMinutos() + " min");
                info.getStyleClass().add("muted-small");
                tarjeta.getChildren().addAll(poster, info);
                tarjeta.setPrefWidth(150);
                peliculas.getChildren().add(tarjeta);
            }
        }

        VBox proximas = new VBox(9);
        Label tituloFunciones = new Label("Próximas funciones");
        tituloFunciones.getStyleClass().add("section-title");
        dashboardController.consultarProximasFunciones().forEach(f -> {
            HBox row = new HBox(12);
            row.getStyleClass().add("function-card");
            Label movie = new Label(f.getPelicula().getTitulo());
            movie.getStyleClass().add("function-title");
            Label when = new Label(FechaHoraUtil.formatear(f.getFechaHora())
                    + " · " + f.getSala().getNombre());
            when.getStyleClass().add("muted-small");
            row.getChildren().addAll(movie, new Region(), when);
            HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
            proximas.getChildren().add(row);
        });
        if (proximas.getChildren().isEmpty()) {
            proximas.getChildren().add(new Label("No hay funciones próximas programadas."));
        }

        root.getChildren().addAll(hero, accesos, tituloPeliculas, peliculas,
                new Separator(), tituloFunciones, proximas);
        return root;
    }

    /**
     * Carga la misma portada local que utiliza la sección Cartelera.
     * Devuelve null para mantener el póster genérico si no hay imagen.
     */
    private ImageView crearImagenPortada(Pelicula pelicula) {
        String nombreArchivo = PORTADAS_POR_CODIGO.get(pelicula.getCodigo());
        if (nombreArchivo == null) {
            return null;
        }

        try (InputStream recurso = DashboardViewController.class.getResourceAsStream(
                RUTA_PORTADAS + nombreArchivo)) {
            if (recurso == null) {
                return null;
            }

            Image imagen = new Image(recurso);
            if (imagen.isError()) {
                return null;
            }

            // El área interna del póster mide 130 x 190 por el padding
            // de 10 px definido en la clase CSS movie-poster.
            ImageView vista = new ImageView(imagen);
            vista.setFitWidth(130);
            vista.setFitHeight(190);
            vista.setPreserveRatio(false);
            vista.setSmooth(true);

            Rectangle recorte = new Rectangle(130, 190);
            recorte.setArcWidth(10);
            recorte.setArcHeight(10);
            vista.setClip(recorte);
            return vista;
        } catch (IOException | RuntimeException ex) {
            return null;
        }
    }

    private VBox construirInicioAdmin() {
        VBox root = new VBox(20);
        root.getStyleClass().add("dashboard-main");
        HBox hero = new HBox(18);
        hero.getStyleClass().add("admin-hero");
        hero.setAlignment(Pos.CENTER_LEFT);
        VBox copy = new VBox(8);
        Label tag = new Label("CINEMA UQ · ADMINISTRACIÓN");
        tag.getStyleClass().add("hero-eyebrow");
        Label title = new Label("Panel de administración");
        title.getStyleClass().add("hero-heading-admin");
        Label subtitle = new Label(
                "Administra el catálogo, las funciones, los usuarios y la operación del cine.");
        subtitle.setWrapText(true);
        subtitle.getStyleClass().add("hero-description-dashboard");
        copy.getChildren().addAll(tag, title, subtitle);
        HBox.setHgrow(copy, Priority.ALWAYS);
        hero.getChildren().addAll(copy, new Label("🎬"));

        HBox stats = new HBox(12);
        stats.getChildren().addAll(
                indicador("Películas activas", dashboardController.consultarTotalPeliculas()),
                indicador("Usuarios", dashboardController.consultarTotalUsuarios()),
                indicador("Funciones próximas", dashboardController.consultarTotalFuncionesProximas()),
                indicador("Productos", dashboardController.consultarTotalProductos())
        );

        Label quickTitle = new Label("Accesos de administración");
        quickTitle.getStyleClass().add("section-title");
        HBox quick = new HBox(10);
        agregarAccesoAdmin(quick, "🎬", "Películas", "Gestión de películas");
        agregarAccesoAdmin(quick, "🗓", "Funciones y salas", "Funciones y salas");
        agregarAccesoAdmin(quick, "👥", "Usuarios", "Gestión de usuarios");
        agregarAccesoAdmin(quick, "📦", "Inventario", "Inventario");

        root.getChildren().addAll(hero, stats, quickTitle, quick);
        return root;
    }

    private VBox indicador(String titulo, int valor) {
        VBox card = new VBox(8);
        card.getStyleClass().add("stat-card");
        Label number = new Label(String.valueOf(valor));
        number.getStyleClass().add("stat-value");
        Label caption = new Label(titulo);
        caption.getStyleClass().add("muted-small");
        card.getChildren().addAll(number, caption);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }

    private void agregarAcceso(HBox fila, String icono, String texto) {
        Button b = crearBotonAcceso(icono, texto);
        b.setOnAction(e -> mostrarSeccion(texto, ""));
        fila.getChildren().add(b);
    }

    private void agregarAccesoAdmin(HBox fila, String icono, String texto, String seccion) {
        Button b = crearBotonAcceso(icono, texto);
        b.setOnAction(e -> mostrarSeccion(seccion, ""));
        fila.getChildren().add(b);
    }

    /** Construye el acceso con nodos gráficos reales; evita que graphic-only oculte el texto. */
    private Button crearBotonAcceso(String icono, String texto) {
        Label simbolo = new Label(icono);
        simbolo.getStyleClass().add("quick-access-icon");
        Label nombre = new Label(texto);
        nombre.getStyleClass().add("quick-access-label");
        VBox contenido = new VBox(7, simbolo, nombre);
        contenido.setAlignment(Pos.CENTER);
        contenido.setMouseTransparent(true);

        Button boton = new Button();
        boton.getStyleClass().add("quick-access-button");
        boton.setGraphic(contenido);
        boton.setContentDisplay(javafx.scene.control.ContentDisplay.GRAPHIC_ONLY);
        boton.setAlignment(Pos.CENTER);
        boton.setMaxWidth(Double.MAX_VALUE);
        boton.setMinHeight(86);
        HBox.setHgrow(boton, Priority.ALWAYS);
        return boton;
    }

    private VBox construirPerfilAdmin() {
        VBox root = new VBox(12);
        root.getStyleClass().add("dashboard-main");
        Label title = new Label("Mi perfil");
        title.getStyleClass().add("page-title");
        Label name = new Label("Nombre: " + usuario.getNombre());
        Label id = new Label("Identificación: " + usuario.getId());
        Label correo = new Label("Correo: " + usuario.getCorreo());
        Label rol = new Label("Rol: Administrador");
        root.getChildren().addAll(title, name, id, correo, rol);
        return root;
    }

    private VBox estadoVacio(String titulo, String descripcion) {
        VBox box = new VBox(8);
        box.getStyleClass().add("empty-state");
        Label t = new Label(titulo);
        t.getStyleClass().add("empty-title");
        Label d = new Label(descripcion);
        d.setWrapText(true);
        d.getStyleClass().add("muted-small");
        box.getChildren().addAll(t, d);
        return box;
    }

    @FXML
    private void mostrarNotificaciones() {
        mostrarSeccion(administrador ? "Control de acceso" : "Notificaciones", "");
    }

    @FXML
    private void cerrarSesion() {
        // Detiene el reloj cuando se abandona el Dashboard.
        if (relojTimeline != null) {
            relojTimeline.stop();
        }

        try {
            aplicacion.mostrarLogin("Has cerrado sesión correctamente.");
        } catch (IOException ex) {
            mostrarErrorSesion(ex);
        }
    }

    /**
     * Alerta de error del Dashboard con el estilo azul oscuro y dorado de CinemaUQ.
     */
    private void mostrarErrorSesion(IOException ex) {
        Dialog<Void> dialogo = new Dialog<>();
        dialogo.setTitle("CinemaUQ · Error de sesión");
        dialogo.setHeaderText(null);

        if (contentHost != null && contentHost.getScene() != null) {
            dialogo.initOwner(contentHost.getScene().getWindow());
        }

        dialogo.getDialogPane().getButtonTypes().add(ButtonType.OK);

        DialogPane panel = dialogo.getDialogPane();
        panel.setMinWidth(430);
        panel.setPrefWidth(455);
        panel.setStyle(
                "-fx-background-color: #071629;"
                        + "-fx-control-inner-background: #0b1b2d;"
                        + "-fx-border-color: #344c67;"
                        + "-fx-border-width: 1;"
                        + "-fx-padding: 0;"
        );
        panel.setBackground(new javafx.scene.layout.Background(
                new javafx.scene.layout.BackgroundFill(
                        javafx.scene.paint.Color.web("#071629"),
                        javafx.scene.layout.CornerRadii.EMPTY,
                        javafx.geometry.Insets.EMPTY
                )
        ));

        Button aceptar = (Button) panel.lookupButton(ButtonType.OK);
        aceptar.setText("Entendido");
        aceptar.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffd34d, #eaa018);"
                        + "-fx-text-fill: #101827;"
                        + "-fx-font-weight: bold;"
                        + "-fx-background-radius: 8;"
                        + "-fx-padding: 9 24;"
        );

        VBox contenido = new VBox(12);
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new javafx.geometry.Insets(23, 25, 20, 25));
        contenido.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #10243a, #09192b);"
        );

        Label icono = new Label("!");
        icono.setMinSize(56, 56);
        icono.setAlignment(Pos.CENTER);
        icono.setStyle(
                "-fx-background-color: #482b36;"
                        + "-fx-background-radius: 40;"
                        + "-fx-border-color: #ff8b86;"
                        + "-fx-border-radius: 40;"
                        + "-fx-text-fill: #ff8b86;"
                        + "-fx-font-size: 27px;"
                        + "-fx-font-weight: bold;"
        );

        Label titulo = new Label("No fue posible cerrar sesión");
        titulo.setStyle(
                "-fx-text-fill: #f5f7fb;"
                        + "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
        );

        Label detalle = new Label(
                ex.getMessage() == null || ex.getMessage().isBlank()
                        ? "Ocurrió un problema al regresar al inicio de sesión."
                        : ex.getMessage()
        );
        detalle.setWrapText(true);
        detalle.setMaxWidth(350);
        detalle.setAlignment(Pos.CENTER);
        detalle.setStyle("-fx-text-fill: #b8c9d9; -fx-font-size: 11px;");

        Label marca = new Label("CINEMAUQ  ·  EXPERIENCIA CINEMATOGRÁFICA");
        marca.setStyle("-fx-text-fill: #7189a2; -fx-font-size: 8px;");

        contenido.getChildren().addAll(icono, titulo, detalle, marca);
        panel.setContent(contenido);

        panel.sceneProperty().addListener((observable, anterior, escena) -> {
            if (escena == null) return;
            javafx.application.Platform.runLater(() -> {
                if (escena.getRoot() instanceof Region region) {
                    region.setStyle("-fx-background-color: #071629;");
                }
                panel.applyCss();
                Node barra = panel.lookup(".button-bar");
                if (barra instanceof Region regionBarra) {
                    regionBarra.setStyle("-fx-background-color: #071629;");
                }
                panel.layout();
            });
        });

        dialogo.showAndWait();
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "CU";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, 1).toUpperCase(Locale.ROOT);
        return (partes[0].substring(0, 1)
                + partes[partes.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }
}
