
package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.model.principal.Cliente;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;

import javafx.fxml.FXML;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javafx.util.Duration;

import java.io.IOException;

/**
 * Controlador de la pantalla de registro de CinemaUQ.
 *
 * Gestiona el registro de clientes, las validaciones
 * y las animaciones cinematográficas de la interfaz.
 */
public class RegistroViewController {

    // =====================================================
    // CAMPOS DEL FORMULARIO
    // =====================================================

    @FXML private TextField identificacionField;
    @FXML private TextField nombreField;
    @FXML private TextField correoField;

    @FXML private PasswordField contrasenaField;
    @FXML private PasswordField confirmarContrasenaField;

    @FXML private Label mensajeLabel;

    // =====================================================
    // ELEMENTOS VISUALES Y ANIMACIONES
    // =====================================================

    @FXML private VBox brandPanel;
    @FXML private VBox formCard;

    @FXML private ImageView logoView;
    @FXML private Label heroStar;
    @FXML private Pane particleLayer;

    // Botón de registro para la animación Pop.
    @FXML private Button registerButton;

    // Control de las animaciones del botón.
    private Animation animacionBotonRegistro;

    // Aplicación principal.
    private App aplicacion;

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    private void initialize() {

        animarEntrada(
                brandPanel,
                25,
                Duration.ZERO
        );

        animarEntrada(
                formCard,
                22,
                Duration.millis(180)
        );

        animarLogo();
        animarEstrella();
        crearFondoAnimado();

        // Animación Pop del botón Crear mi cuenta.
        configurarAnimacionPop();
    }

    // =====================================================
    // ANIMACIONES DE ENTRADA
    // =====================================================

    private void animarEntrada(
            Node nodo,
            double desplazamiento,
            Duration retraso) {

        if (nodo == null) return;

        nodo.setOpacity(0);

        FadeTransition fade =
                new FadeTransition(
                        Duration.millis(900),
                        nodo
                );

        fade.setFromValue(0);
        fade.setToValue(1);
        fade.setDelay(retraso);
        fade.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition movimiento =
                new TranslateTransition(
                        Duration.millis(900),
                        nodo
                );

        movimiento.setFromY(desplazamiento);
        movimiento.setToY(0);
        movimiento.setDelay(retraso);
        movimiento.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(
                fade,
                movimiento
        ).play();
    }

    // =====================================================
    // LOGO FLOTANTE
    // =====================================================

    private void animarLogo() {

        if (logoView == null) return;

        TranslateTransition flotar =
                new TranslateTransition(
                        Duration.seconds(4.2),
                        logoView
                );

        flotar.setFromY(0);
        flotar.setToY(-6);

        flotar.setAutoReverse(true);
        flotar.setCycleCount(Animation.INDEFINITE);
        flotar.setInterpolator(Interpolator.EASE_BOTH);
        flotar.setDelay(Duration.millis(900));

        flotar.play();
    }

    // =====================================================
    // ESTRELLA ANIMADA
    // =====================================================

    private void animarEstrella() {

        if (heroStar == null) return;

        ScaleTransition pulso =
                new ScaleTransition(
                        Duration.seconds(1.8),
                        heroStar
                );

        pulso.setFromX(1);
        pulso.setFromY(1);

        pulso.setToX(1.22);
        pulso.setToY(1.22);

        pulso.setAutoReverse(true);
        pulso.setCycleCount(Animation.INDEFINITE);
        pulso.setInterpolator(Interpolator.EASE_BOTH);

        pulso.play();
    }

    // =====================================================
    // ANIMACIÓN POP DEL BOTÓN CREAR MI CUENTA
    // =====================================================

    /**
     * Configura la animación Pop del botón de registro.
     * Incluye entrada con rebote, efecto hover y compresión al pulsar.
     */
    private void configurarAnimacionPop() {

        if (registerButton == null) return;

        // ---------------------------------------------
        // ANIMACIÓN INICIAL DE ENTRADA
        // ---------------------------------------------

        ScaleTransition entrada =
                new ScaleTransition(
                        Duration.millis(240),
                        registerButton
                );

        entrada.setFromX(0.86);
        entrada.setFromY(0.86);

        entrada.setToX(1.08);
        entrada.setToY(1.08);

        entrada.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition asentamiento =
                new ScaleTransition(
                        Duration.millis(150),
                        registerButton
                );

        asentamiento.setFromX(1.08);
        asentamiento.setFromY(1.08);

        asentamiento.setToX(1.0);
        asentamiento.setToY(1.0);

        asentamiento.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition popInicial =
                new SequentialTransition(
                        entrada,
                        asentamiento
                );

        popInicial.setDelay(Duration.millis(750));
        popInicial.play();

        // ---------------------------------------------
        // AL PASAR EL CURSOR
        // ---------------------------------------------

        registerButton.addEventHandler(
                MouseEvent.MOUSE_ENTERED,
                event -> animarEscalaRegistro(
                        1.04,
                        140
                )
        );

        // ---------------------------------------------
        // AL RETIRAR EL CURSOR
        // ---------------------------------------------

        registerButton.addEventHandler(
                MouseEvent.MOUSE_EXITED,
                event -> animarEscalaRegistro(
                        1.0,
                        150
                )
        );

        // ---------------------------------------------
        // AL PRESIONAR
        // ---------------------------------------------

        registerButton.addEventHandler(
                MouseEvent.MOUSE_PRESSED,
                event -> animarEscalaRegistro(
                        0.95,
                        75
                )
        );

        // ---------------------------------------------
        // AL SOLTAR
        // ---------------------------------------------

        registerButton.addEventHandler(
                MouseEvent.MOUSE_RELEASED,
                event -> ejecutarPopRegistro()
        );
    }

    /**
     * Modifica suavemente la escala del botón.
     * Detiene la transición anterior para evitar acumulaciones.
     */
    private void animarEscalaRegistro(
            double escala,
            int duracion) {

        if (registerButton == null) return;

        if (animacionBotonRegistro != null) {
            animacionBotonRegistro.stop();
        }

        ScaleTransition transicion =
                new ScaleTransition(
                        Duration.millis(duracion),
                        registerButton
                );

        transicion.setToX(escala);
        transicion.setToY(escala);

        transicion.setInterpolator(Interpolator.EASE_OUT);

        animacionBotonRegistro = transicion;

        transicion.play();
    }

    /**
     * Ejecuta el pequeño rebote al soltar el botón.
     */
    private void ejecutarPopRegistro() {

        if (registerButton == null) return;

        if (animacionBotonRegistro != null) {
            animacionBotonRegistro.stop();
        }

        double escalaFinal =
                registerButton.isHover() ? 1.04 : 1.0;

        ScaleTransition rebote =
                new ScaleTransition(
                        Duration.millis(115),
                        registerButton
                );

        rebote.setToX(1.075);
        rebote.setToY(1.075);

        rebote.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition regreso =
                new ScaleTransition(
                        Duration.millis(125),
                        registerButton
                );

        regreso.setFromX(1.075);
        regreso.setFromY(1.075);

        regreso.setToX(escalaFinal);
        regreso.setToY(escalaFinal);

        regreso.setInterpolator(Interpolator.EASE_BOTH);

        SequentialTransition pop =
                new SequentialTransition(
                        rebote,
                        regreso
                );

        animacionBotonRegistro = pop;

        pop.play();
    }

    // =====================================================
    // FONDO CINEMATOGRÁFICO ANIMADO
    // =====================================================

    private void crearFondoAnimado() {

        if (particleLayer == null) return;

        // Halo azul.
        Circle haloAzul = new Circle(190);

        haloAzul.setFill(
                Color.web("#1b5790", 0.13)
        );

        haloAzul.setManaged(false);

        // Halo dorado.
        Circle haloDorado = new Circle(125);

        haloDorado.setFill(
                Color.web("#e9a927", 0.075)
        );

        haloDorado.setManaged(false);

        particleLayer.getChildren().addAll(
                haloAzul,
                haloDorado
        );

        particleLayer.widthProperty().addListener(
                (obs, oldValue, newValue) ->
                        posicionarHalos(
                                haloAzul,
                                haloDorado
                        )
        );

        particleLayer.heightProperty().addListener(
                (obs, oldValue, newValue) ->
                        posicionarHalos(
                                haloAzul,
                                haloDorado
                        )
        );

        posicionarHalos(
                haloAzul,
                haloDorado
        );

        animarHalo(
                haloAzul,
                1.045,
                17
        );

        animarHalo(
                haloDorado,
                1.12,
                -13
        );

        // Partículas flotantes.
        for (int i = 0; i < 34; i++) {

            double radio =
                    1.1 + (i % 4) * 0.65;

            Circle particula =
                    new Circle(radio);

            particula.setFill(
                    i % 5 == 0
                            ? Color.web("#ffd36a", 0.9)
                            : Color.web("#8bb9e8", 0.58)
            );

            particula.setManaged(false);

            double xRatio =
                    ((i * 37) % 97 + 2) / 100.0;

            double yRatio =
                    ((i * 61) % 91 + 4) / 100.0;

            particula.layoutXProperty().bind(
                    particleLayer.widthProperty().multiply(xRatio)
            );

            particula.layoutYProperty().bind(
                    particleLayer.heightProperty().multiply(yRatio)
            );

            particleLayer.getChildren().add(particula);

            // Animación de brillo.
            FadeTransition brillo =
                    new FadeTransition(
                            Duration.seconds(
                                    2.2 + (i % 5) * 0.75
                            ),
                            particula
                    );

            brillo.setFromValue(0.12);

            brillo.setToValue(
                    i % 3 == 0 ? 0.95 : 0.62
            );

            brillo.setAutoReverse(true);
            brillo.setCycleCount(Animation.INDEFINITE);

            brillo.setDelay(
                    Duration.millis(i * 95L)
            );

            brillo.setInterpolator(
                    Interpolator.EASE_BOTH
            );

            // Movimiento flotante.
            TranslateTransition deriva =
                    new TranslateTransition(
                            Duration.seconds(
                                    5.5 + (i % 6) * 1.15
                            ),
                            particula
                    );

            deriva.setFromY(0);

            deriva.setToY(
                    -18 - (i % 7) * 5
            );

            deriva.setFromX(0);

            deriva.setToX(
                    (i % 2 == 0 ? 1 : -1)
                            * (8 + (i % 5) * 4)
            );

            deriva.setAutoReverse(true);
            deriva.setCycleCount(Animation.INDEFINITE);

            deriva.setDelay(
                    Duration.millis(i * 110L)
            );

            deriva.setInterpolator(
                    Interpolator.EASE_BOTH
            );

            new ParallelTransition(
                    brillo,
                    deriva
            ).play();
        }
    }

    // =====================================================
    // POSICIONAMIENTO DE HALOS
    // =====================================================

    private void posicionarHalos(
            Circle azul,
            Circle dorado) {

        if (particleLayer == null) return;

        double ancho = particleLayer.getWidth();
        double alto = particleLayer.getHeight();

        azul.setCenterX(ancho * 0.73);
        azul.setCenterY(alto * 0.18);

        dorado.setCenterX(ancho * 0.91);
        dorado.setCenterY(alto * 0.82);
    }

    // =====================================================
    // ANIMACIÓN DE HALOS
    // =====================================================

    private void animarHalo(
            Circle halo,
            double escala,
            double desplazamiento) {

        ScaleTransition pulso =
                new ScaleTransition(
                        Duration.seconds(8.5),
                        halo
                );

        pulso.setFromX(0.92);
        pulso.setFromY(0.92);

        pulso.setToX(escala);
        pulso.setToY(escala);

        pulso.setAutoReverse(true);
        pulso.setCycleCount(Animation.INDEFINITE);
        pulso.setInterpolator(Interpolator.EASE_BOTH);

        TranslateTransition movimiento =
                new TranslateTransition(
                        Duration.seconds(10),
                        halo
                );

        movimiento.setFromY(0);
        movimiento.setToY(desplazamiento);

        movimiento.setAutoReverse(true);
        movimiento.setCycleCount(Animation.INDEFINITE);
        movimiento.setInterpolator(Interpolator.EASE_BOTH);

        new ParallelTransition(
                pulso,
                movimiento
        ).play();
    }

    // =====================================================
    // CONFIGURACIÓN DE LA APLICACIÓN
    // =====================================================

    public void configurar(App aplicacion) {

        this.aplicacion = aplicacion;

        mostrarMensaje(
                "Crea tu cuenta para disfrutar de CinemaUQ.",
                false
        );
    }

    // =====================================================
    // REGISTRAR CLIENTE
    // =====================================================

    @FXML
    private void registrarCliente() {

        if (aplicacion == null) {

            mostrarMensaje(
                    "No se pudo inicializar el sistema.",
                    true
            );

            return;
        }

        String identificacion =
                texto(identificacionField);

        String nombre =
                texto(nombreField);

        String correo =
                texto(correoField);

        String contrasena =
                contrasenaField.getText();

        String confirmacion =
                confirmarContrasenaField.getText();

        // Validar campos vacíos.
        if (identificacion.isBlank()
                || nombre.isBlank()
                || correo.isBlank()
                || contrasena == null
                || contrasena.isBlank()
                || confirmacion == null
                || confirmacion.isBlank()) {

            mostrarMensaje(
                    "Completa todos los campos para crear tu cuenta.",
                    true
            );

            return;
        }

        // Validar coincidencia de contraseñas.
        if (!contrasena.equals(confirmacion)) {

            mostrarMensaje(
                    "Las contraseñas no coinciden.",
                    true
            );

            confirmarContrasenaField.clear();
            confirmarContrasenaField.requestFocus();

            return;
        }

        try {

            Cliente cliente =
                    aplicacion.getAutenticacionController()
                            .registrarCliente(
                                    identificacion,
                                    nombre,
                                    correo,
                                    contrasena
                            );

            // Volver al inicio después del registro exitoso.
            aplicacion.mostrarLogin(
                    "¡Cuenta creada, "
                            + cliente.getNombre()
                            + "! Ya puedes iniciar sesión."
            );

        } catch (IllegalArgumentException ex) {

            mostrarMensaje(
                    ex.getMessage(),
                    true
            );

        } catch (IOException ex) {

            mostrarMensaje(
                    "La cuenta se creó, pero no fue posible volver al inicio de sesión.",
                    true
            );

            ex.printStackTrace();
        }
    }

    // =====================================================
    // VOLVER AL INICIO DE SESIÓN
    // =====================================================

    @FXML
    private void volverAlLogin() {

        if (aplicacion == null) return;

        try {

            aplicacion.mostrarLogin(null);

        } catch (IOException ex) {

            mostrarMensaje(
                    "No fue posible volver al inicio de sesión.",
                    true
            );

            ex.printStackTrace();
        }
    }

    // =====================================================
    // UTILIDADES
    // =====================================================

    private String texto(TextField campo) {

        return campo.getText() == null
                ? ""
                : campo.getText().trim();
    }

    private void mostrarMensaje(
            String mensaje,
            boolean esError) {

        if (mensajeLabel == null) return;

        mensajeLabel.setText(
                mensaje == null
                        ? " "
                        : mensaje
        );

        mensajeLabel.getStyleClass().removeAll(
                "message-success",
                "message-error"
        );

        mensajeLabel.getStyleClass().add(
                esError
                        ? "message-error"
                        : "message-success"
        );
    }
}
