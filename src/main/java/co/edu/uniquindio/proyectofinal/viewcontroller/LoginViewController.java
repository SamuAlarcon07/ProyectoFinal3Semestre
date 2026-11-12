package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.model.principal.Usuario;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.Optional;

/** Controlador visual de inicio de sesión. */
public class LoginViewController {

    @FXML private TextField correoField;
    @FXML private PasswordField contrasenaField;
    @FXML private Label mensajeLabel;

    private App aplicacion;

    public void configurar(App aplicacion, String mensajeInicial) {
        this.aplicacion = aplicacion;
        mostrarMensaje(mensajeInicial, false);
    }

    @FXML
    private void iniciarSesion() {
        String correo = correoField.getText() == null
                ? "" : correoField.getText().trim();
        String contrasena = contrasenaField.getText();

        if (correo.isBlank() || contrasena == null || contrasena.isBlank()) {
            mostrarMensaje("Ingresa tu correo y contraseña para continuar.", true);
            return;
        }

        Optional<Usuario> resultado = aplicacion.getAutenticacionController()
                .iniciarSesion(correo, contrasena);

        if (resultado.isEmpty()) {
            mostrarMensaje("Credenciales incorrectas o cuenta inactiva. Verifica tus datos.", true);
            contrasenaField.clear();
            return;
        }

        try {
            aplicacion.mostrarDashboard(resultado.get());
        } catch (IOException ex) {
            mostrarMensaje("No fue posible abrir el panel principal: " + ex.getMessage(), true);
        }
    }

    @FXML
    private void abrirRegistro() {
        try {
            aplicacion.mostrarRegistro();
        } catch (IOException ex) {
            mostrarMensaje("No fue posible abrir el formulario de registro.", true);
        }
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        if (mensajeLabel == null) return;
        mensajeLabel.setText(mensaje == null ? " " : mensaje);
        mensajeLabel.getStyleClass().removeAll("message-success", "message-error");
        mensajeLabel.getStyleClass().add(esError ? "message-error" : "message-success");
    }
}
