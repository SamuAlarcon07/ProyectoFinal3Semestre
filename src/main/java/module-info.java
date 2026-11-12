module co.edu.uniquindio.proyectofinal {
    requires javafx.controls;
    requires javafx.fxml;

    opens co.edu.uniquindio.proyectofinal to javafx.fxml;
    opens co.edu.uniquindio.proyectofinal.viewcontroller to javafx.fxml;
    opens co.edu.uniquindio.proyectofinal.model.principal to javafx.base;

    exports co.edu.uniquindio.proyectofinal;
    exports co.edu.uniquindio.proyectofinal.controller;
}
