
package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.model.principal.Usuario;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;

/**
 * Controlador visual de inicio de sesión de CinemaUQ.
 * Gestiona la autenticación y la navegación al registro.
 */
public class LoginViewController {

    @FXML
    private TextField correoField;

    @FXML
    private PasswordField contrasenaField;

    @FXML
    private Label mensajeLabel;

    private App aplicacion;

    /**
     * Configura la aplicación y el mensaje inicial de la vista.
     */
    public void configurar(App aplicacion, String mensajeInicial) {
        this.aplicacion = aplicacion;
        mostrarMensaje(mensajeInicial, false);
    }

    /**
     * Valida las credenciales e inicia la sesión del usuario.
     */
    @FXML
    private void iniciarSesion() {

        if (aplicacion == null) {
            mostrarMensaje(
                    "No se pudo inicializar el sistema. Reinicia la aplicación.",
                    true
            );
            return;
        }

        String correo = correoField.getText() == null
                ? ""
                : correoField.getText().trim();

        String contrasena = contrasenaField.getText();

        if (correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            mostrarMensaje(
                    "Ingresa tu correo y contraseña para continuar.",
                    true
            );
            return;
        }

        Optional<Usuario> resultado = aplicacion
                .getAutenticacionController()
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
                    "No fue posible abrir el panel principal: " + ex.getMessage(),
                    true
            );
        }
    }

    /**
     * Abre el formulario de creación de cuenta.
     */
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
                    "No fue posible abrir el formulario de registro.",
                    true
            );
        }
    }

    /**
     * Actualiza el mensaje visual de la pantalla.
     */
    private void mostrarMensaje(String mensaje, boolean esError) {

        if (mensajeLabel == null) {
            return;
        }

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
