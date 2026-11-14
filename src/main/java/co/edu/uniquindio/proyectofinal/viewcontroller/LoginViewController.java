
package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.model.principal.Usuario;

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
import java.util.Optional;

/**
 * Controlador visual del inicio de sesión de CinemaUQ.
 * Gestiona la autenticación, navegación y animaciones cinematográficas.
 */
public class LoginViewController {

    @FXML private TextField correoField;
    @FXML private PasswordField contrasenaField;
    @FXML private Label mensajeLabel;

    @FXML private VBox brandPanel;
    @FXML private VBox formCard;

    @FXML private ImageView logoView;
    @FXML private Label heroStar;
    @FXML private Pane particleLayer;

    // Botón principal para aplicar el efecto Pop.
    @FXML private Button loginButton;

    private App aplicacion;

    // Control de las transiciones del botón.
    private Animation animacionBoton;

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    private void initialize() {

        animarEntrada(brandPanel, 28, Duration.ZERO);

        animarEntrada(
                formCard,
                22,
                Duration.millis(180)
        );

        animarMovimientoLogo();
        animarDestello();
        crearFondoAnimado();

        // Nueva animación Pop del botón.
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

        FadeTransition aparecer =
                new FadeTransition(Duration.millis(900), nodo);

        aparecer.setFromValue(0);
        aparecer.setToValue(1);
        aparecer.setInterpolator(Interpolator.EASE_OUT);
        aparecer.setDelay(retraso);

        TranslateTransition deslizar =
                new TranslateTransition(Duration.millis(900), nodo);

        deslizar.setFromY(desplazamiento);
        deslizar.setToY(0);
        deslizar.setInterpolator(Interpolator.EASE_OUT);
        deslizar.setDelay(retraso);

        new ParallelTransition(aparecer, deslizar).play();
    }

    // =====================================================
    // ANIMACIÓN POP DEL BOTÓN INICIAR SESIÓN
    // =====================================================

    private void configurarAnimacionPop() {

        if (loginButton == null) return;

        // Entrada con pequeño efecto de rebote.
        ScaleTransition entrada =
                new ScaleTransition(
                        Duration.millis(240),
                        loginButton
                );

        entrada.setFromX(0.86);
        entrada.setFromY(0.86);
        entrada.setToX(1.08);
        entrada.setToY(1.08);
        entrada.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition asentamiento =
                new ScaleTransition(
                        Duration.millis(150),
                        loginButton
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

        // Efecto al pasar el cursor.
        loginButton.addEventHandler(
                MouseEvent.MOUSE_ENTERED,
                event -> animarEscalaBoton(
                        1.04,
                        Duration.millis(140)
                )
        );

        // Regresa al tamaño original.
        loginButton.addEventHandler(
                MouseEvent.MOUSE_EXITED,
                event -> animarEscalaBoton(
                        1.0,
                        Duration.millis(150)
                )
        );

        // Se comprime al presionar.
        loginButton.addEventHandler(
                MouseEvent.MOUSE_PRESSED,
                event -> animarEscalaBoton(
                        0.95,
                        Duration.millis(75)
                )
        );

        // Rebote al soltar el botón.
        loginButton.addEventHandler(
                MouseEvent.MOUSE_RELEASED,
                event -> ejecutarPopBoton()
        );
    }

    /**
     * Aplica una transición de escala y detiene la anterior
     * para evitar que las animaciones se acumulen.
     */
    private void animarEscalaBoton(
            double escala,
            Duration duracion) {

        if (loginButton == null) return;

        if (animacionBoton != null) {
            animacionBoton.stop();
        }

        ScaleTransition transicion =
                new ScaleTransition(duracion, loginButton);

        transicion.setToX(escala);
        transicion.setToY(escala);
        transicion.setInterpolator(Interpolator.EASE_OUT);

        animacionBoton = transicion;
        transicion.play();
    }

    /**
     * Rebote de confirmación al soltar el botón.
     */
    private void ejecutarPopBoton() {

        if (loginButton == null) return;

        if (animacionBoton != null) {
            animacionBoton.stop();
        }

        double escalaFinal =
                loginButton.isHover() ? 1.04 : 1.0;

        ScaleTransition rebote =
                new ScaleTransition(
                        Duration.millis(115),
                        loginButton
                );

        rebote.setToX(1.075);
        rebote.setToY(1.075);
        rebote.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition regreso =
                new ScaleTransition(
                        Duration.millis(125),
                        loginButton
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

        animacionBoton = pop;
        pop.play();
    }

    // =====================================================
    // LOGO FLOTANTE
    // =====================================================

    private void animarMovimientoLogo() {

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
    // DESTELLO DORADO
    // =====================================================

    private void animarDestello() {

        if (heroStar == null) return;

        ScaleTransition pulso =
                new ScaleTransition(
                        Duration.seconds(1.8),
                        heroStar
                );

        pulso.setFromX(1.0);
        pulso.setFromY(1.0);

        pulso.setToX(1.22);
        pulso.setToY(1.22);

        pulso.setAutoReverse(true);
        pulso.setCycleCount(Animation.INDEFINITE);
        pulso.setInterpolator(Interpolator.EASE_BOTH);

        pulso.play();
    }

    // =====================================================
    // FONDO CINEMATOGRÁFICO ANIMADO
    // =====================================================

    private void crearFondoAnimado() {

        if (particleLayer == null) return;

        Circle haloAzul = new Circle(190);

        haloAzul.setFill(
                Color.web("#1b5790", 0.13)
        );

        haloAzul.setManaged(false);

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
                        posicionarHalos(haloAzul, haloDorado)
        );

        particleLayer.heightProperty().addListener(
                (obs, oldValue, newValue) ->
                        posicionarHalos(haloAzul, haloDorado)
        );

        posicionarHalos(haloAzul, haloDorado);

        animarHalo(haloAzul, 1.045, 17);
        animarHalo(haloDorado, 1.12, -13);

        // Partículas flotantes.
        for (int i = 0; i < 34; i++) {

            double radio = 1.1 + (i % 4) * 0.65;

            Circle particula = new Circle(radio);

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

            FadeTransition brillo =
                    new FadeTransition(
                            Duration.seconds(2.2 + (i % 5) * 0.75),
                            particula
                    );

            brillo.setFromValue(0.12);
            brillo.setToValue(i % 3 == 0 ? 0.95 : 0.62);
            brillo.setAutoReverse(true);
            brillo.setCycleCount(Animation.INDEFINITE);
            brillo.setDelay(Duration.millis(i * 95L));
            brillo.setInterpolator(Interpolator.EASE_BOTH);

            TranslateTransition deriva =
                    new TranslateTransition(
                            Duration.seconds(5.5 + (i % 6) * 1.15),
                            particula
                    );

            deriva.setFromY(0);
            deriva.setToY(-18 - (i % 7) * 5);
            deriva.setFromX(0);

            deriva.setToX(
                    (i % 2 == 0 ? 1 : -1)
                            * (8 + (i % 5) * 4)
            );

            deriva.setAutoReverse(true);
            deriva.setCycleCount(Animation.INDEFINITE);
            deriva.setDelay(Duration.millis(i * 110L));
            deriva.setInterpolator(Interpolator.EASE_BOTH);

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
    // CONFIGURACIÓN
    // =====================================================

    public void configurar(
            App aplicacion,
            String mensajeInicial) {

        this.aplicacion = aplicacion;

        mostrarMensaje(mensajeInicial, false);
    }

    // =====================================================
    // INICIAR SESIÓN
    // =====================================================

    @FXML
    private void iniciarSesion() {

        if (aplicacion == null) {

            mostrarMensaje(
                    "No se pudo inicializar el sistema. Reinicia la aplicación.",
                    true
            );

            return;
        }

        String correo =
                correoField.getText() == null
                        ? ""
                        : correoField.getText().trim();

        String contrasena = contrasenaField.getText();

        if (correo.isBlank()
                || contrasena == null
                || contrasena.isBlank()) {

            mostrarMensaje(
                    "Ingresa tu correo y contraseña para continuar.",
                    true
            );

            return;
        }

        Optional<Usuario> resultado =
                aplicacion.getAutenticacionController()
                        .iniciarSesion(correo, contrasena);

        if (resultado.isEmpty()) {

            mostrarMensaje(
                    "Credenciales incorrectas o cuenta inactiva. Verifica tus datos.",
                    true
            );

            contrasenaField.clear();
            contrasenaField.requestFocus();

            return;
        }

        try {

            aplicacion.mostrarDashboard(resultado.get());

        } catch (IOException ex) {

            mostrarMensaje(
                    "No fue posible abrir el panel principal: "
                            + ex.getMessage(),
                    true
            );

            ex.printStackTrace();
        }
    }

    // =====================================================
    // ABRIR REGISTRO
    // =====================================================

    @FXML
    private void abrirRegistro() {

        if (aplicacion == null) {

            mostrarMensaje(
                    "No se pudo inicializar el sistema.",
                    true
            );

            return;
        }

        try {

            aplicacion.mostrarRegistro();

        } catch (IOException ex) {

            mostrarMensaje(
                    "No fue posible abrir el formulario de registro: "
                            + ex.getMessage(),
                    true
            );

            ex.printStackTrace();
        }
    }

    // =====================================================
    // MENSAJES
    // =====================================================

    private void mostrarMensaje(
            String mensaje,
            boolean esError) {

        if (mensajeLabel == null) return;

        mensajeLabel.setText(
                mensaje == null || mensaje.isBlank()
                        ? " "
                        : mensaje
        );

        mensajeLabel.getStyleClass().removeAll(
                "message-success",
                "message-error"
        );

        mensajeLabel.getStyleClass().add(
                esError ? "message-error" : "message-success"
        );
    }
}
