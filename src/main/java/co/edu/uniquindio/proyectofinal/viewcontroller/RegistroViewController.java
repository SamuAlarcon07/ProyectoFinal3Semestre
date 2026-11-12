package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.App;
import co.edu.uniquindio.proyectofinal.model.principal.Cliente;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;

/** Controlador visual del registro público de clientes. */
public class RegistroViewController {

    @FXML private TextField identificacionField;
    @FXML private TextField nombreField;
    @FXML private TextField correoField;
    @FXML private PasswordField contrasenaField;
    @FXML private PasswordField confirmarContrasenaField;
    @FXML private Label mensajeLabel;

    private App aplicacion;

    public void configurar(App aplicacion) {
        this.aplicacion = aplicacion;
        mostrarMensaje("Crea tu cuenta para disfrutar de CinemaUQ.", false);
    }

    @FXML
    private void registrarCliente() {
        String identificacion = texto(identificacionField);
        String nombre = texto(nombreField);
        String correo = texto(correoField);
        String contrasena = contrasenaField.getText();
        String confirmacion = confirmarContrasenaField.getText();

        if (identificacion.isBlank() || nombre.isBlank() || correo.isBlank()
                || contrasena == null || contrasena.isBlank()
                || confirmacion == null || confirmacion.isBlank()) {
            mostrarMensaje("Completa todos los campos para crear tu cuenta.", true);
            return;
        }

        if (!contrasena.equals(confirmacion)) {
            mostrarMensaje("Las contraseñas no coinciden.", true);
            confirmarContrasenaField.clear();
            return;
        }

        try {
            Cliente cliente = aplicacion.getAutenticacionController()
                    .registrarCliente(identificacion, nombre, correo, contrasena);
            aplicacion.mostrarLogin(
                    "¡Cuenta creada, " + cliente.getNombre()
                            + "! Ya puedes iniciar sesión."
            );
        } catch (IllegalArgumentException ex) {
            mostrarMensaje(ex.getMessage(), true);
        } catch (IOException ex) {
            mostrarMensaje("La cuenta se creó, pero no fue posible volver al inicio de sesión.", true);
        }
    }

    @FXML
    private void volverAlLogin() {
        try {
            aplicacion.mostrarLogin(null);
        } catch (IOException ex) {
            mostrarMensaje("No fue posible volver al inicio de sesión.", true);
        }
    }

    private String texto(TextField campo) {
        return campo.getText() == null ? "" : campo.getText().trim();
    }

    private void mostrarMensaje(String mensaje, boolean esError) {
        mensajeLabel.setText(mensaje == null ? " " : mensaje);
        mensajeLabel.getStyleClass().removeAll("message-success", "message-error");
        mensajeLabel.getStyleClass().add(esError ? "message-error" : "message-success");
    }
}
