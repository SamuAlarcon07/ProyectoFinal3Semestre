package co.edu.uniquindio.proyectofinal;

import co.edu.uniquindio.proyectofinal.controller.AutenticacionController;
import co.edu.uniquindio.proyectofinal.controller.CarteleraController;
import co.edu.uniquindio.proyectofinal.controller.CinemaOperationsController;
import co.edu.uniquindio.proyectofinal.controller.DashboardController;
import co.edu.uniquindio.proyectofinal.controller.ReservaController;
import co.edu.uniquindio.proyectofinal.model.enums.FormatoProyeccion;
import co.edu.uniquindio.proyectofinal.model.principal.Administrador;
import co.edu.uniquindio.proyectofinal.model.principal.Cine;
import co.edu.uniquindio.proyectofinal.model.principal.Cliente;
import co.edu.uniquindio.proyectofinal.model.principal.TipoSala;
import co.edu.uniquindio.proyectofinal.model.principal.TarjetaVirtual;
import co.edu.uniquindio.proyectofinal.model.principal.Usuario;
import co.edu.uniquindio.proyectofinal.model.service.*;
import co.edu.uniquindio.proyectofinal.model.util.DatosInicialesCinemaUQ;
import co.edu.uniquindio.proyectofinal.model.util.DatosInicialesFuncionesCinemaUQ;
import co.edu.uniquindio.proyectofinal.model.patrones.estructurales.facade.CheckoutFacade;
import co.edu.uniquindio.proyectofinal.viewcontroller.DashboardViewController;
import co.edu.uniquindio.proyectofinal.viewcontroller.LoginViewController;
import co.edu.uniquindio.proyectofinal.viewcontroller.RegistroViewController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Punto de entrada y composición de dependencias de CinemaUQ.
 * Todos los servicios comparten una única instancia durante la sesión.
 */
public class App extends Application {

    private static final String RUTA_VISTAS =
            "/co/edu/uniquindio/proyectofinal/view/";
    private static final String RUTA_CSS =
            "/co/edu/uniquindio/proyectofinal/css/styles.css";

    // Servicios principales.
    private final AutenticacionService autenticacionService =
            new AutenticacionService();
    private final AdministracionCatalogoService catalogoService =
            new AdministracionCatalogoService();
    private final FuncionService funcionService =
            new FuncionService();
    private final CarteleraService carteleraService =
            new CarteleraService(catalogoService, funcionService);
    private final DisponibilidadAsientosService disponibilidadAsientosService =
            new DisponibilidadAsientosService();
    private final ReservaService reservaService =
            new ReservaService(disponibilidadAsientosService);

    private final InventarioService inventarioService =
            new InventarioService();
    private final ConfiteriaService confiteriaService =
            new ConfiteriaService();
    private final PagoService pagoService =
            new PagoService();
    private final PuntosService puntosService =
            new PuntosService();
    private final TarjetaVirtualService tarjetaVirtualService =
            new TarjetaVirtualService();
    private final CompraService compraService =
            new CompraService(pagoService, inventarioService, puntosService);

    private final Cine cine = new Cine(
            "NIT-POR-CONFIGURAR",
            "Razón social por configurar",
            "CinemaUQ",
            "Armenia, Quindío",
            "0000000000",
            "contacto@cinemauq.local",
            ""
    );
    private final CineService cineService = new CineService(cine);
    private final GestionSalasService gestionSalasService =
            new GestionSalasService(cineService);
    private final BloqueoSalaService bloqueoSalaService =
            new BloqueoSalaService();
    private final PromocionService promocionService =
            new PromocionService();
    private final ControlAccesoService controlAccesoService =
            new ControlAccesoService();
    private final ListaEsperaService listaEsperaService =
            new ListaEsperaService();
    private final ReporteService reporteService =
            new ReporteService();
    private final ListaPersonalService listaPersonalService =
            new ListaPersonalService();
    private final ResenaService resenaService =
            new ResenaService();
    private final CancelacionService cancelacionService =
            new CancelacionService();
    private final NotificacionService notificacionService =
            new NotificacionService();

    // Controladores de aplicación.
    private final AutenticacionController autenticacionController =
            new AutenticacionController(
                    autenticacionService, tarjetaVirtualService);
    private final DashboardController dashboardController =
            new DashboardController(
                    autenticacionService,
                    catalogoService,
                    carteleraService,
                    puntosService,
                    tarjetaVirtualService
            );
    private final CarteleraController carteleraController =
            new CarteleraController(
                    carteleraService,
                    disponibilidadAsientosService
            );
    private final CheckoutFacade checkoutFacade =
            new CheckoutFacade(
                    disponibilidadAsientosService,
                    compraService,
                    reservaService
            );
    private final ReservaController reservaController =
            new ReservaController(
                    reservaService,
                    checkoutFacade,
                    tarjetaVirtualService,
                    puntosService,
                    promocionService
            );
    private final CinemaOperationsController cinemaOperationsController =
            new CinemaOperationsController(
                    catalogoService,
                    funcionService,
                    cineService,
                    gestionSalasService,
                    bloqueoSalaService,
                    autenticacionService,
                    inventarioService,
                    promocionService,
                    controlAccesoService,
                    listaEsperaService,
                    reporteService,
                    compraService,
                    tarjetaVirtualService,
                    puntosService,
                    listaPersonalService,
                    resenaService,
                    cancelacionService,
                    notificacionService,
                    confiteriaService
            );

    private Stage escenarioPrincipal;

    public App() {
        configurarDatosIniciales();
    }

    /**
     * Crea una cuenta administrativa inicial cuando no hay ninguna.
     * Esta es una credencial de demostración local; debe cambiarse
     * cuando se implemente persistencia y gestión segura de credenciales.
     */
    private void configurarDatosIniciales() {
        boolean existeAdministrador = autenticacionService.consultarUsuarios()
                .stream().anyMatch(Administrador.class::isInstance);

        if (!existeAdministrador) {
            autenticacionService.registrarAdministrador(
                    "ADMIN001",
                    "Administrador CinemaUQ",
                    "admin@cinemauq.local",
                    "CinemaUQ2026*",
                    "ADM-001"
            );
        }

        // Cliente de demostración disponible desde cada inicio del proyecto.
        registrarClienteDemo();

        // Catálogo inicial: películas activas de demostración visibles en la cartelera.
        DatosInicialesCinemaUQ.cargarPeliculas(catalogoService);

        // Opiniones ficticias de demostración para todas las películas del catálogo.
        // No sustituyen ni alteran las validaciones de las reseñas de clientes reales.
        resenaService.cargarResenasDemostracion(
                catalogoService.consultarPeliculasActivas()
        );

        // Tipos de sala base; las salas físicas se registran desde Administración.
        registrarTipoSalaBase(
                "TS-2D", "Sala 2D", "Sala de proyección estándar.",
                EnumSet.of(FormatoProyeccion.DOS_D),
                false, false, false
        );
        registrarTipoSalaBase(
                "TS-3D", "Sala 3D", "Sala compatible con proyección 2D y 3D.",
                EnumSet.of(FormatoProyeccion.DOS_D, FormatoProyeccion.TRES_D),
                true, false, false
        );
        registrarTipoSalaBase(
                "TS-4D", "Sala 4D", "Sala compatible con efectos y movimiento.",
                EnumSet.of(FormatoProyeccion.DOS_D, FormatoProyeccion.TRES_D,
                        FormatoProyeccion.CUATRO_D),
                true, true, true
        );

        // Salas con asientos y funciones de demostración para las películas iniciales.
        DatosInicialesFuncionesCinemaUQ.cargarFunciones(
                catalogoService, funcionService, gestionSalasService, cineService
        );

        // Los observadores se notifican una sola vez por compra confirmada.
        compraService.agregarObservador(notificacionService);
        compraService.agregarObservador(
                new ControlAccesoCompraObserver(controlAccesoService)
        );
    }

    /**
     * Registra un cliente de demostración si todavía no existe.
     * También garantiza que tenga tarjeta virtual y cuenta de puntos.
     *
     * Credenciales de prueba:
     * Correo: cliente@cinemauq.local
     * Contraseña: ClienteUQ2026*
     */
    private void registrarClienteDemo() {
        final String id = "CLI-DEMO-001";
        final String nombre = "Cliente de prueba";
        final String correo = "cliente@cinemauq.local";
        final String contrasena = "ClienteUQ2026*";

        Usuario usuarioExistente = autenticacionService.buscarUsuario(id)
                .orElseGet(() -> autenticacionService.consultarUsuarios()
                        .stream()
                        .filter(usuario -> usuario.getCorreo()
                                .equalsIgnoreCase(correo))
                        .findFirst()
                        .orElse(null));

        Cliente cliente;

        if (usuarioExistente == null) {
            // Usa el controlador de aplicación para que también se emita
            // la tarjeta virtual automáticamente.
            cliente = autenticacionController.registrarCliente(
                    id, nombre, correo, contrasena
            );
        } else if (usuarioExistente instanceof Cliente clienteRegistrado) {
            cliente = clienteRegistrado;
        } else {
            throw new IllegalStateException(
                    "El correo reservado para el cliente de demostración "
                            + "ya pertenece a otro tipo de usuario."
            );
        }

        // Garantiza una tarjeta aunque la cuenta se haya creado previamente
        // por un mecanismo distinto al registro normal.
        if (tarjetaVirtualService.buscarTarjetaPorCliente(cliente).isEmpty()) {
            tarjetaVirtualService.registrarTarjeta(
                    new TarjetaVirtual("CU-" + cliente.getId(), cliente)
            );
        }

        // Deja creada la cuenta de puntos desde el primer ingreso.
        puntosService.obtenerOCrearCuenta(cliente);
    }

    private void registrarTipoSalaBase(
            String codigo, String nombre, String descripcion,
            java.util.Set<FormatoProyeccion> formatos,
            boolean gafas, boolean movimiento, boolean efectos) {
        if (gestionSalasService.consultarTiposSala().stream()
                .noneMatch(tipo -> tipo.getCodigo().equals(codigo))) {
            gestionSalasService.registrarTipoSala(
                    new TipoSala(codigo, nombre, descripcion,
                            formatos, gafas, movimiento, efectos)
            );
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        escenarioPrincipal = stage;
        mostrarLogin(null);
        escenarioPrincipal.show();
    }

    public AutenticacionController getAutenticacionController() {
        return autenticacionController;
    }

    public AutenticacionService getAutenticacionService() {
        return autenticacionService;
    }

    public DashboardController getDashboardController() {
        return dashboardController;
    }

    public CarteleraController getCarteleraController() {
        return carteleraController;
    }

    public ReservaController getReservaController() {
        return reservaController;
    }

    public CinemaOperationsController getCinemaOperationsController() {
        return cinemaOperationsController;
    }

    public FuncionService getFuncionService() {
        return funcionService;
    }

    public AdministracionCatalogoService getCatalogoService() {
        return catalogoService;
    }

    public TarjetaVirtualService getTarjetaVirtualService() {
        return tarjetaVirtualService;
    }

    public Stage getEscenarioPrincipal() {
        return escenarioPrincipal;
    }

    public void mostrarLogin(String mensaje) throws IOException {
        FXMLLoader loader = crearLoader("login-view.fxml");
        Parent root = loader.load();
        LoginViewController controller = loader.getController();
        controller.configurar(this, mensaje);
        mostrarEscena(root, "CinemaUQ | Iniciar sesión", 1050, 690);
    }

    public void mostrarRegistro() throws IOException {
        FXMLLoader loader = crearLoader("registro-view.fxml");
        Parent root = loader.load();
        RegistroViewController controller = loader.getController();
        controller.configurar(this);
        mostrarEscena(root, "CinemaUQ | Crear cuenta", 1050, 740);
    }

    public void mostrarDashboard(Usuario usuario) throws IOException {
        Objects.requireNonNull(usuario, "El usuario autenticado es obligatorio.");
        FXMLLoader loader = crearLoader("dashboard-view.fxml");
        Parent root = loader.load();
        DashboardViewController controller = loader.getController();
        controller.configurar(
                this,
                usuario,
                dashboardController,
                carteleraController,
                reservaController,
                cinemaOperationsController
        );
        mostrarEscena(root, "CinemaUQ | Inicio", 1500, 920);
    }

    private FXMLLoader crearLoader(String nombreVista) throws IOException {
        String ruta = RUTA_VISTAS + nombreVista;
        URL recurso = App.class.getResource(ruta);
        if (recurso == null) {
            throw new IOException("No se encontró la vista: " + ruta);
        }
        return new FXMLLoader(recurso);
    }

    private void mostrarEscena(
            Parent contenido, String titulo,
            double ancho, double alto) {
        Scene escena = new Scene(contenido, ancho, alto);
        URL css = App.class.getResource(RUTA_CSS);
        if (css != null) {
            escena.getStylesheets().add(css.toExternalForm());
        }
        escenarioPrincipal.setTitle(titulo);
        escenarioPrincipal.setMinWidth(1120);
        escenarioPrincipal.setMinHeight(720);
        escenarioPrincipal.setScene(escena);
        escenarioPrincipal.centerOnScreen();
    }
}
