package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.controller.CarteleraController;
import co.edu.uniquindio.proyectofinal.controller.CinemaOperationsController;
import co.edu.uniquindio.proyectofinal.controller.DashboardController;
import co.edu.uniquindio.proyectofinal.controller.ReservaController;
import co.edu.uniquindio.proyectofinal.model.principal.*;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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

    private App aplicacion;
    private Usuario usuario;
    private DashboardController dashboardController;
    private CarteleraController carteleraController;
    private ReservaController reservaController;
    private CinemaOperationsController operaciones;
    private boolean administrador;

    private GestionAdminViewController adminView;
    private ClienteViewController clienteView;

    @FXML
    private void initialize() {
        searchField.setOnAction(event -> {
            if (administrador) {
                mostrarSeccion("Gestión de películas", searchField.getText());
            } else {
                mostrarSeccion("Cartelera", searchField.getText());
            }
        });
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
                new String[]{"⌂", "Inicio"},
                new String[]{"▣", "Gestión de películas"},
                new String[]{"▦", "Funciones y salas"},
                new String[]{"♙", "Gestión de usuarios"},
                new String[]{"▤", "Inventario"},
                new String[]{"🍿", "Confitería"},
                new String[]{"◇", "Promociones"},
                new String[]{"▥", "Control de acceso"},
                new String[]{"≋", "Lista de espera"},
                new String[]{"▧", "Reportes"},
                new String[]{"▣", "Tarjetas virtuales"},
                new String[]{"◎", "Mi perfil"}
        )
                : List.of(
                new String[]{"⌂", "Inicio"},
                new String[]{"▣", "Cartelera"},
                new String[]{"🍿", "Confitería"},
                new String[]{"≋", "Lista de espera"},
                new String[]{"♢", "Mis entradas"},
                new String[]{"▤", "Mis compras"},
                new String[]{"▣", "Mi tarjeta virtual"},
                new String[]{"☆", "Mis puntos"},
                new String[]{"♡", "Películas guardadas"},
                new String[]{"☏", "Reseñas"},
                new String[]{"♙", "Mi perfil"},
                new String[]{"♧", "Notificaciones"}
        );

        for (String[] opcion : opciones) {
            Button boton = new Button(opcion[0] + "    " + opcion[1]);
            boton.getStyleClass().add("nav-button");
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setAlignment(Pos.CENTER_LEFT);
            boton.setOnAction(event -> mostrarSeccion(opcion[1], ""));
            menuContainer.getChildren().add(boton);
        }
        actualizarMenuActivo("Inicio");
    }

    private void actualizarMenuActivo(String seleccion) {
        for (Node node : menuContainer.getChildren()) {
            if (node instanceof Button boton) {
                boton.getStyleClass().remove("nav-button-active");
                if (boton.getText().endsWith(seleccion)) {
                    boton.getStyleClass().add("nav-button-active");
                }
            }
        }
    }

    private void construirPanelLateral() {
        rightPanel.getChildren().clear();
        rightPanel.getChildren().add(crearTarjetaPerfil());

        if (administrador) {
            rightPanel.getChildren().add(crearPanelResumenAdmin());
            rightPanel.getChildren().add(panelInfo(
                    "Centro de administración",
                    "Gestiona el catálogo, las salas, la programación, los usuarios y la operación del cine."));
        } else {
            rightPanel.getChildren().add(crearPanelPuntos());
            rightPanel.getChildren().add(crearPanelTarjetaVirtual());
            rightPanel.getChildren().add(panelInfo(
                    "Tu experiencia",
                    "Consulta tus entradas, guarda películas y administra tus reseñas desde el menú."));
        }
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
        contentScroll.setVvalue(0);
        // También sincroniza los indicadores al volver de Cartelera u otra sección.
        actualizarResumenCliente();
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
            Label when = new Label(f.getFechaHora().toString().replace('T', ' ')
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
        try {
            aplicacion.mostrarLogin("Has cerrado sesión correctamente.");
        } catch (IOException ex) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("CinemaUQ");
            alerta.setHeaderText("No fue posible cerrar sesión");
            alerta.setContentText(ex.getMessage());
            alerta.showAndWait();
        }
    }

    private String iniciales(String nombre) {
        if (nombre == null || nombre.isBlank()) return "CU";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, 1).toUpperCase(Locale.ROOT);
        return (partes[0].substring(0, 1)
                + partes[partes.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }
}