package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.controller.CinemaOperationsController;
import co.edu.uniquindio.proyectofinal.model.enums.*;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;
import co.edu.uniquindio.proyectofinal.model.principal.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.StringConverter;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;

/**
 * Construye los módulos administrativos dentro del Dashboard existente.
 * No requiere archivos FXML adicionales.
 */
public class GestionAdminViewController {

    private final CinemaOperationsController operaciones;
    private final Administrador administrador;
    private final Window owner;

    public GestionAdminViewController(
            CinemaOperationsController operaciones,
            Administrador administrador,
            Window owner
    ) {
        this.operaciones = Objects.requireNonNull(operaciones);
        this.administrador = Objects.requireNonNull(administrador);
        this.owner = owner;
    }

    public Node construir(String seccion) {
        return switch (seccion) {
            case "Gestión de películas" -> vistaPeliculas();
            case "Funciones y salas" -> vistaFuncionesSalas();
            case "Gestión de usuarios" -> vistaUsuarios();
            case "Inventario" -> vistaInventario();
            case "Confitería" -> vistaConfiteria();
            case "Promociones" -> vistaPromociones();
            case "Control de acceso" -> vistaAcceso();
            case "Lista de espera" -> vistaListaEspera();
            case "Reportes" -> vistaReportes();
            case "Tarjetas virtuales" -> vistaTarjetas();
            default -> vistaNoDisponible(seccion);
        };
    }

    // ------------------------------------------------------------------
    // PELÍCULAS
    // ------------------------------------------------------------------

    private Node vistaPeliculas() {
        VBox root = pagina("Gestión de películas",
                "Registra, actualiza y administra el catálogo que verán los clientes.");

        TableView<Pelicula> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", Pelicula::getCodigo),
                columna("Título", Pelicula::getTitulo),
                columna("Género", Pelicula::getGenero),
                columna("Duración", p -> p.getDuracionMinutos() + " min"),
                columna("Clasificación", Pelicula::getClasificacion),
                columna("Estado", p -> p.getEstado().name())
        );

        Button nueva = boton("＋ Nueva película", "button-gold");
        Button editar = boton("Editar", "button-small-outline");
        Button estado = boton("Activar / desactivar", "button-small-outline");
        Button actualizar = boton("Actualizar", "button-small-outline");

        nueva.setOnAction(e -> formularioPelicula(null, tabla));
        editar.setOnAction(e -> {
            Pelicula seleccion = tabla.getSelectionModel().getSelectedItem();
            if (seleccion == null) { aviso("Selecciona una película."); return; }
            formularioPelicula(seleccion, tabla);
        });
        estado.setOnAction(e -> {
            Pelicula seleccion = tabla.getSelectionModel().getSelectedItem();
            if (seleccion == null) {
                aviso("Selecciona una película de la tabla.");
                return;
            }

            // Captura el estado antes de modificarlo para evitar invertirlo dos veces.
            boolean activar = !seleccion.estaActiva();
            String mensaje = activar
                    ? "La película " + seleccion.getTitulo()
                      + " quedó ACTIVA y podrá aparecer en la cartelera."
                    : "La película " + seleccion.getTitulo()
                      + " quedó INACTIVA y dejará de aparecer en la cartelera.";

            // Actualiza la entidad y la tabla antes de mostrar la confirmación modal.
            // Así el cambio ya se ve detrás del diálogo y no parece que el botón
            // haya hecho nada mientras el usuario lee la alerta.
            ejecutar(() -> {
                operaciones.cambiarEstadoPelicula(seleccion.getCodigo(), activar);
                refrescar(tabla, operaciones.consultarPeliculas());
                tabla.refresh();

                int indice = tabla.getItems().indexOf(seleccion);
                if (indice >= 0) {
                    tabla.getSelectionModel().select(indice);
                }
            }, mensaje);
        });
        actualizar.setOnAction(e -> refrescar(tabla, operaciones.consultarPeliculas()));

        refrescar(tabla, operaciones.consultarPeliculas());
        root.getChildren().addAll(barra(nueva, editar, estado, actualizar), tabla);
        return root;
    }

    private void formularioPelicula(Pelicula existente, TableView<Pelicula> tabla) {
        TextField codigo = campo(existente == null ? "" : existente.getCodigo(),
                "Código único (ej. PEL-001)");
        codigo.setDisable(existente != null);
        TextField titulo = campo(existente == null ? "" : existente.getTitulo(), "Título");
        TextArea sinopsis = new TextArea(existente == null ? "" : existente.getSinopsis());
        sinopsis.setPromptText("Sinopsis");
        sinopsis.setPrefRowCount(4);
        configurarAreaTexto(sinopsis);
        TextField duracion = campo(existente == null ? "" :
                String.valueOf(existente.getDuracionMinutos()), "Duración en minutos");
        TextField clasificacion = campo(existente == null ? "" :
                existente.getClasificacion(), "Clasificación (TP, 12+, 15+, 18+)");
        TextField genero = campo(existente == null ? "" : existente.getGenero(), "Género");

        GridPane grid = formulario();
        fila(grid, 0, "Código", codigo);
        fila(grid, 1, "Título", titulo);
        fila(grid, 2, "Sinopsis", sinopsis);
        fila(grid, 3, "Duración", duracion);
        fila(grid, 4, "Clasificación", clasificacion);
        fila(grid, 5, "Género", genero);

        if (!confirmarFormulario(
                existente == null ? "Registrar película" : "Editar película",
                grid)) return;

        ejecutar(() -> {
            int minutos = Integer.parseInt(duracion.getText().trim());
            if (existente == null) {
                operaciones.registrarPelicula(codigo.getText(), titulo.getText(),
                        sinopsis.getText(), minutos, clasificacion.getText(), genero.getText());
            } else {
                operaciones.actualizarPelicula(existente.getCodigo(), titulo.getText(),
                        sinopsis.getText(), minutos, clasificacion.getText(), genero.getText());
            }
            refrescar(tabla, operaciones.consultarPeliculas());
        }, "La película se guardó correctamente.");
    }

    // ------------------------------------------------------------------
    // SALAS Y FUNCIONES
    // ------------------------------------------------------------------

    private Node vistaFuncionesSalas() {
        VBox root = pagina("Funciones y salas",
                "Administra la capacidad de las salas y programa las funciones de CinemaUQ.");

        TabPane tabs = new TabPane();
        Tab tabSalas = new Tab("Salas", construirTabSalas());
        Tab tabFunciones = new Tab("Funciones", construirTabFunciones());
        Tab tabBloqueos = new Tab("Bloqueos", construirTabBloqueos());
        tabs.getTabs().addAll(tabSalas, tabFunciones, tabBloqueos);
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        root.getChildren().add(tabs);
        return root;
    }

    private Node construirTabSalas() {
        VBox box = new VBox(12);
        TableView<Sala> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", Sala::getCodigo),
                columna("Nombre", Sala::getNombre),
                columna("Tipo", s -> s.getTipoSala().getNombre()),
                columna("Capacidad", s -> String.valueOf(s.calcularCapacidad())),
                columna("Formatos", s -> s.getTipoSala().getFormatosCompatibles().toString())
        );

        Button nuevaSala = boton("＋ Crear sala", "button-gold");
        Button nuevoTipo = boton("＋ Tipo de sala", "button-small-outline");
        Button refrescar = boton("Actualizar", "button-small-outline");

        nuevaSala.setOnAction(e -> formularioSala(tabla));
        nuevoTipo.setOnAction(e -> formularioTipoSala());
        refrescar.setOnAction(e -> refrescar(tabla, operaciones.consultarSalas()));

        refrescar(tabla, operaciones.consultarSalas());
        box.getChildren().addAll(barra(nuevaSala, nuevoTipo, refrescar), tabla);
        return box;
    }

    private void formularioTipoSala() {
        TextField codigo = campo("", "Código (ej. TS-IMAX)");
        TextField nombre = campo("", "Nombre del tipo");
        TextArea descripcion = new TextArea();
        descripcion.setPromptText("Descripción");
        descripcion.setPrefRowCount(3);
        configurarAreaTexto(descripcion);
        CheckBox dosD = new CheckBox("2D");
        configurarCasilla(dosD);
        CheckBox tresD = new CheckBox("3D");
        configurarCasilla(tresD);
        CheckBox cuatroD = new CheckBox("4D");
        configurarCasilla(cuatroD);
        dosD.setSelected(true);
        CheckBox gafas = new CheckBox("Requiere gafas 3D");
        configurarCasilla(gafas);
        CheckBox movimiento = new CheckBox("Movimiento");
        configurarCasilla(movimiento);
        CheckBox efectos = new CheckBox("Efectos especiales");
        configurarCasilla(efectos);

        GridPane grid = formulario();
        fila(grid, 0, "Código", codigo);
        fila(grid, 1, "Nombre", nombre);
        fila(grid, 2, "Descripción", descripcion);
        HBox formatos = new HBox(12, dosD, tresD, cuatroD);
        fila(grid, 3, "Formatos", formatos);
        VBox caracteristicas = new VBox(7, gafas, movimiento, efectos);
        fila(grid, 4, "Características", caracteristicas);

        if (!confirmarFormulario("Crear tipo de sala", grid)) return;
        ejecutar(() -> {
            Set<FormatoProyeccion> compatibles = EnumSet.noneOf(FormatoProyeccion.class);
            if (dosD.isSelected()) compatibles.add(FormatoProyeccion.DOS_D);
            if (tresD.isSelected()) compatibles.add(FormatoProyeccion.TRES_D);
            if (cuatroD.isSelected()) compatibles.add(FormatoProyeccion.CUATRO_D);
            operaciones.registrarTipoSala(codigo.getText(), nombre.getText(),
                    descripcion.getText(), compatibles, gafas.isSelected(),
                    movimiento.isSelected(), efectos.isSelected());
        }, "Tipo de sala registrado.");
    }

    private void formularioSala(TableView<Sala> tabla) {
        TextField codigo = campo("", "Código de sala (ej. SAL-01)");
        TextField nombre = campo("", "Nombre de sala");
        ComboBox<TipoSala> tipo = new ComboBox<>(
                FXCollections.observableArrayList(operaciones.consultarTiposSala()));
        configurarCombo(tipo, TipoSala::getNombre);
        TextField filas = campo("5", "Cantidad de filas (1 a 26)");
        TextField columnas = campo("8", "Asientos por fila (1 a 30)");
        ComboBox<TipoAsiento> tipoAsiento = new ComboBox<>(
                FXCollections.observableArrayList(TipoAsiento.values()));
        tipoAsiento.setValue(TipoAsiento.GENERAL);

        GridPane grid = formulario();
        fila(grid, 0, "Código", codigo);
        fila(grid, 1, "Nombre", nombre);
        fila(grid, 2, "Tipo", tipo);
        fila(grid, 3, "Filas", filas);
        fila(grid, 4, "Asientos por fila", columnas);
        fila(grid, 5, "Tipo de asiento", tipoAsiento);

        if (!confirmarFormulario("Crear sala y generar asientos", grid)) return;
        ejecutar(() -> {
            if (tipo.getValue() == null || tipoAsiento.getValue() == null) {
                throw new IllegalArgumentException("Selecciona tipo de sala y tipo de asiento.");
            }
            operaciones.registrarSala(codigo.getText(), nombre.getText(),
                    tipo.getValue(), Integer.parseInt(filas.getText()),
                    Integer.parseInt(columnas.getText()), tipoAsiento.getValue());
            refrescar(tabla, operaciones.consultarSalas());
        }, "Sala registrada con sus asientos.");
    }

    private Node construirTabFunciones() {
        VBox box = new VBox(12);
        TableView<Funcion> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", Funcion::getCodigo),
                columna("Película", f -> f.getPelicula().getTitulo()),
                columna("Sala", f -> f.getSala().getNombre()),
                columna("Fecha", f -> f.getFechaHora().toString().replace('T', ' ')),
                columna("Formato", f -> f.getFormatoProyeccion().name()),
                columna("Precio", f -> moneda(f.getPrecioBase())),
                columna("Estado", f -> f.getEstado().name())
        );

        Button nueva = boton("＋ Programar función", "button-gold");
        Button cancelar = boton("Cancelar función", "button-small-outline");
        Button finalizar = boton("Finalizar función", "button-small-outline");
        Button refrescar = boton("Actualizar", "button-small-outline");

        nueva.setOnAction(e -> formularioFuncion(tabla));
        cancelar.setOnAction(e -> {
            Funcion f = tabla.getSelectionModel().getSelectedItem();
            if (f == null) { aviso("Selecciona una función."); return; }
            if (f.getEstado() == EstadoFuncion.FINALIZADA) {
                aviso("Una función finalizada no se puede cancelar.");
                return;
            }
            if (ejecutar(() -> operaciones.cancelarFuncion(f.getCodigo()),
                    "Función cancelada.")) {
                refrescar(tabla, operaciones.consultarFunciones());
            }
        });
        finalizar.setOnAction(e -> {
            Funcion f = tabla.getSelectionModel().getSelectedItem();
            if (f == null) { aviso("Selecciona una función."); return; }
            if (f.getEstado() != EstadoFuncion.PROGRAMADA) {
                aviso("Solo puedes finalizar funciones que estén programadas.");
                return;
            }
            LocalDateTime finFuncion = f.getFechaHora()
                    .plusMinutes(f.getPelicula().getDuracionMinutos());
            if (LocalDateTime.now().isBefore(finFuncion)) {
                aviso("La función todavía no ha terminado según su duración programada.");
                return;
            }
            if (ejecutar(() -> operaciones.finalizarFuncion(f.getCodigo()),
                    "Función finalizada.")) {
                refrescar(tabla, operaciones.consultarFunciones());
            }
        });
        refrescar.setOnAction(e -> refrescar(tabla, operaciones.consultarFunciones()));

        refrescar(tabla, operaciones.consultarFunciones());
        box.getChildren().addAll(barra(nueva, cancelar, finalizar, refrescar), tabla);
        return box;
    }

    private void formularioFuncion(TableView<Funcion> tabla) {
        TextField codigo = campo("", "Código único de función");
        ComboBox<Pelicula> pelicula = new ComboBox<>(
                FXCollections.observableArrayList(operaciones.consultarPeliculasActivas()));
        configurarCombo(pelicula, Pelicula::getTitulo);
        ComboBox<Sala> sala = new ComboBox<>(
                FXCollections.observableArrayList(operaciones.consultarSalas()));
        configurarCombo(sala, s -> s.getNombre() + " (" + s.getCodigo() + ")");
        DatePicker fecha = new DatePicker(LocalDate.now().plusDays(1));
        configurarFecha(fecha);
        TextField hora = campo("19:00", "Hora HH:mm");
        TextField precio = campo("15000", "Precio en COP");
        ComboBox<FormatoProyeccion> formato = new ComboBox<>(
                FXCollections.observableArrayList(FormatoProyeccion.values()));
        formato.setValue(FormatoProyeccion.DOS_D);

        // Al cambiar la sala, limita los formatos a los que admite ese tipo de sala.
        sala.setOnAction(evento -> {
            if (sala.getValue() == null) return;
            List<FormatoProyeccion> compatibles = new ArrayList<>(
                    sala.getValue().getTipoSala().getFormatosCompatibles());
            formato.getItems().setAll(compatibles);
            if (!compatibles.contains(formato.getValue())) {
                formato.setValue(compatibles.isEmpty() ? null : compatibles.get(0));
            }
        });
        GridPane grid = formulario();
        fila(grid, 0, "Código", codigo);
        fila(grid, 1, "Película", pelicula);
        fila(grid, 2, "Sala", sala);
        fila(grid, 3, "Fecha", fecha);
        fila(grid, 4, "Hora", hora);
        fila(grid, 5, "Precio", precio);
        fila(grid, 6, "Formato", formato);

        if (!confirmarFormulario("Programar función", grid)) return;
        ejecutar(() -> {
            if (pelicula.getValue() == null || sala.getValue() == null
                    || fecha.getValue() == null || formato.getValue() == null) {
                throw new IllegalArgumentException("Completa todos los datos.");
            }
            LocalTime tiempo = LocalTime.parse(hora.getText().trim());
            LocalDateTime fechaHora = LocalDateTime.of(fecha.getValue(), tiempo);
            if (!fechaHora.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("La función debe programarse para una fecha y hora futuras.");
            }
            if (!sala.getValue().permiteFormato(formato.getValue())) {
                throw new IllegalArgumentException("La sala seleccionada no admite ese formato de proyección.");
            }
            operaciones.programarFuncion(codigo.getText(), pelicula.getValue(),
                    sala.getValue(), fechaHora,
                    new BigDecimal(precio.getText().trim()), formato.getValue());
            refrescar(tabla, operaciones.consultarFunciones());
        }, "Función programada correctamente.");
    }

    private Node construirTabBloqueos() {
        VBox box = new VBox(12);
        TableView<BloqueoSala> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", BloqueoSala::getCodigo),
                columna("Sala", b -> b.getSala().getNombre()),
                columna("Tipo", b -> b.getTipoBloqueo().name()),
                columna("Desde", b -> b.getFechaInicio().toString()),
                columna("Hasta", b -> b.getFechaFin().toString()),
                columna("Estado", b -> b.getEstado().name())
        );
        Button nuevo = boton("＋ Nuevo bloqueo", "button-gold");
        Button cancelar = boton("Cancelar bloqueo", "button-small-outline");
        nuevo.setOnAction(e -> formularioBloqueo(tabla));
        cancelar.setOnAction(e -> {
            BloqueoSala b = tabla.getSelectionModel().getSelectedItem();
            if (b == null) { aviso("Selecciona un bloqueo."); return; }
            if (b.getEstado() == EstadoBloqueoSala.FINALIZADO
                    || b.getEstado() == EstadoBloqueoSala.CANCELADO) {
                aviso("Ese bloqueo ya está finalizado o cancelado.");
                return;
            }
            if (ejecutar(() -> operaciones.cancelarBloqueo(b.getCodigo()),
                    "Bloqueo cancelado.")) {
                refrescar(tabla, operaciones.consultarBloqueos());
            }
        });
        refrescar(tabla, operaciones.consultarBloqueos());
        box.getChildren().addAll(barra(nuevo, cancelar), tabla);
        return box;
    }

    private void formularioBloqueo(TableView<BloqueoSala> tabla) {
        TextField codigo = campo("", "Código del bloqueo");
        ComboBox<Sala> sala = new ComboBox<>(
                FXCollections.observableArrayList(operaciones.consultarSalas()));
        configurarCombo(sala, Sala::getNombre);
        ComboBox<TipoBloqueoSala> tipo = new ComboBox<>(
                FXCollections.observableArrayList(TipoBloqueoSala.values()));
        tipo.setValue(TipoBloqueoSala.MANTENIMIENTO);
        DatePicker fechaDesde = new DatePicker(LocalDate.now());
        configurarFecha(fechaDesde);
        TextField horaDesde = campo("08:00", "HH:mm");
        DatePicker fechaHasta = new DatePicker(LocalDate.now());
        configurarFecha(fechaHasta);
        TextField horaHasta = campo("12:00", "HH:mm");
        TextField motivo = campo("", "Motivo");
        GridPane grid = formulario();
        fila(grid,0,"Código",codigo); fila(grid,1,"Sala",sala);
        fila(grid,2,"Tipo",tipo); fila(grid,3,"Fecha inicial",fechaDesde);
        fila(grid,4,"Hora inicial",horaDesde); fila(grid,5,"Fecha final",fechaHasta);
        fila(grid,6,"Hora final",horaHasta); fila(grid,7,"Motivo",motivo);
        if (!confirmarFormulario("Registrar bloqueo de sala", grid)) return;
        if (sala.getValue() == null || tipo.getValue() == null
                || fechaDesde.getValue() == null || fechaHasta.getValue() == null) {
            aviso("Completa la sala, el tipo y ambas fechas del bloqueo.");
            return;
        }
        if (!ejecutar(() -> {
            LocalDateTime inicio = LocalDateTime.of(fechaDesde.getValue(),
                    LocalTime.parse(horaDesde.getText().trim()));
            LocalDateTime fin = LocalDateTime.of(fechaHasta.getValue(),
                    LocalTime.parse(horaHasta.getText().trim()));
            if (!fin.isAfter(inicio)) {
                throw new IllegalArgumentException("La fecha y hora final deben ser posteriores al inicio.");
            }
            operaciones.registrarBloqueo(codigo.getText(), sala.getValue(),
                    tipo.getValue(), inicio, fin, motivo.getText(), administrador);
        }, "Bloqueo registrado.")) {
            return;
        }
        refrescar(tabla, operaciones.consultarBloqueos());
    }

    // ------------------------------------------------------------------
    // USUARIOS
    // ------------------------------------------------------------------

    private Node vistaUsuarios() {
        VBox root = pagina("Gestión de usuarios",
                "Consulta cuentas y administra su estado de acceso.");

        TableView<Usuario> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Identificación", Usuario::getId),
                columna("Nombre", Usuario::getNombre),
                columna("Correo", Usuario::getCorreo),
                columna("Rol", u -> u instanceof Administrador ? "Administrador" : "Cliente"),
                columna("Estado", u -> u.getEstado().name())
        );

        Button cambiar = boton("Bloquear / activar", "button-small-outline");
        Button recargar = boton("Recargar tarjeta seleccionada", "button-gold");
        Button refrescar = boton("Actualizar", "button-small-outline");

        cambiar.setOnAction(e -> {
            Usuario u = tabla.getSelectionModel().getSelectedItem();
            if (u == null) { aviso("Selecciona un usuario."); return; }
            if (u.getId().equals(administrador.getId())) {
                aviso("No puedes bloquear tu propia sesión.");
                return;
            }
            boolean activar = u.getEstado() != EstadoUsuario.ACTIVO;
            if (ejecutar(() -> operaciones.cambiarEstadoUsuario(u.getId(), activar),
                    activar ? "Usuario activado." : "Usuario bloqueado.")) {
                refrescar(tabla, operaciones.consultarUsuarios());
            }
        });

        recargar.setOnAction(e -> {
            Usuario u = tabla.getSelectionModel().getSelectedItem();
            if (!(u instanceof Cliente cliente)) {
                aviso("Selecciona un cliente que tenga tarjeta virtual.");
                return;
            }
            formularioRecarga(cliente);
        });

        refrescar.setOnAction(e -> refrescar(tabla, operaciones.consultarUsuarios()));
        refrescar(tabla, operaciones.consultarUsuarios());
        root.getChildren().addAll(barra(cambiar, recargar, refrescar), tabla);
        return root;
    }

    private void formularioRecarga(Cliente cliente) {
        Optional<TarjetaVirtual> tarjeta = operaciones.consultarTarjeta(cliente);
        if (tarjeta.isEmpty()) { aviso("El cliente no tiene tarjeta virtual."); return; }
        Optional<String> valor = solicitarTexto(
                "Recargar tarjeta",
                "Cliente: " + cliente.getNombre(),
                "Valor de la recarga (COP):",
                "50000"
        );

        valor.ifPresent(texto -> ejecutar(
                () -> operaciones.recargarTarjeta(
                        tarjeta.get(), new BigDecimal(texto.trim()), administrador),
                "Tarjeta recargada. Nuevo saldo: " + moneda(tarjeta.get().getSaldo())
        ));
    }

    // ------------------------------------------------------------------
    // INVENTARIO
    // ------------------------------------------------------------------

    private Node vistaInventario() {
        VBox root = pagina("Inventario de confitería",
                "Administra productos, existencias, entradas y mermas.");

        TableView<Producto> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", Producto::getCodigo),
                columna("Producto", Producto::getNombre),
                columna("Precio", p -> moneda(p.getPrecio())),
                columna("Stock", p -> String.valueOf(p.getInventario().getStockActual())),
                columna("Mínimo", p -> String.valueOf(p.getInventario().getStockMinimo())),
                columna("Unidad", p -> p.getInventario().getUnidadMedida()),
                columna("Estado", p -> p.getEstado().name())
        );

        Button nuevo = boton("＋ Nuevo producto", "button-gold");
        Button editar = boton("Editar producto", "button-small-outline");
        Button entrada = boton("Registrar entrada", "button-small-outline");
        Button merma = boton("Registrar merma", "button-small-outline");
        Button activar = boton("Activar / desactivar", "button-small-outline");
        Button reposicion = boton("Ver productos por reponer", "button-small-outline");
        Button todos = boton("Ver inventario completo", "button-small-outline");

        nuevo.setOnAction(e -> formularioProducto(tabla));
        editar.setOnAction(e -> {
            Producto seleccionado = tabla.getSelectionModel().getSelectedItem();
            if (seleccionado == null) {
                aviso("Selecciona un producto para editar.");
                return;
            }
            formularioEditarProducto(seleccionado, tabla);
        });
        entrada.setOnAction(e -> movimientoInventario(tabla, true));
        merma.setOnAction(e -> movimientoInventario(tabla, false));
        activar.setOnAction(e -> {
            Producto p = tabla.getSelectionModel().getSelectedItem();
            if (p == null) { aviso("Selecciona un producto."); return; }
            if (ejecutar(() -> operaciones.cambiarEstadoProducto(p, !p.estaActivo()),
                    "Estado del producto actualizado.")) {
                refrescar(tabla, operaciones.consultarProductos());
            }
        });
        reposicion.setOnAction(e -> {
            List<Producto> bajos = operaciones.consultarProductosPorReponer();
            refrescar(tabla, bajos);
            aviso(bajos.size() + " productos necesitan reposición.");
        });
        todos.setOnAction(e -> refrescar(tabla, operaciones.consultarProductos()));

        refrescar(tabla, operaciones.consultarProductos());
        root.getChildren().addAll(barra(nuevo, editar, entrada, merma, activar,
                reposicion, todos), tabla);
        return root;
    }

    private void formularioProducto(TableView<Producto> tabla) {
        TextField codigo = campo("", "Código");
        TextField nombre = campo("", "Nombre");
        TextField descripcion = campo("", "Descripción");
        TextField precio = campo("", "Precio COP");
        TextField stock = campo("0", "Stock inicial");
        TextField minimo = campo("5", "Stock mínimo");
        TextField unidad = campo("unidad", "Unidad (unidad, caja, botella...)");
        GridPane grid = formulario();
        fila(grid,0,"Código",codigo); fila(grid,1,"Nombre",nombre);
        fila(grid,2,"Descripción",descripcion); fila(grid,3,"Precio",precio);
        fila(grid,4,"Stock inicial",stock); fila(grid,5,"Stock mínimo",minimo);
        fila(grid,6,"Unidad",unidad);
        if (!confirmarFormulario("Registrar producto", grid)) return;
        if (ejecutar(() -> operaciones.registrarProducto(codigo.getText().trim(),
                nombre.getText().trim(), descripcion.getText().trim(),
                new BigDecimal(precio.getText().trim()),
                Integer.parseInt(stock.getText().trim()),
                Integer.parseInt(minimo.getText().trim()),
                unidad.getText().trim()), "Producto registrado.")) {
            refrescar(tabla, operaciones.consultarProductos());
        }
    }

    private void formularioEditarProducto(Producto producto, TableView<Producto> tabla) {
        TextField nombre = campo(producto.getNombre(), "Nombre");
        TextField descripcion = campo(producto.getDescripcion(), "Descripción");
        TextField precio = campo(producto.getPrecio().toPlainString(), "Precio COP");

        GridPane grid = formulario();
        fila(grid, 0, "Nombre", nombre);
        fila(grid, 1, "Descripción", descripcion);
        fila(grid, 2, "Precio", precio);

        if (!confirmarFormulario("Editar producto", grid)) return;

        if (ejecutar(() -> operaciones.actualizarProducto(
                producto,
                nombre.getText().trim(),
                descripcion.getText().trim(),
                new BigDecimal(precio.getText().trim())
        ), "Producto actualizado correctamente.")) {
            refrescar(tabla, operaciones.consultarProductos());
        }
    }

    private void movimientoInventario(TableView<Producto> tabla, boolean esEntrada) {
        Producto producto = tabla.getSelectionModel().getSelectedItem();
        if (producto == null) { aviso("Selecciona un producto."); return; }
        Optional<String> valor = solicitarTexto(
                esEntrada ? "Registrar entrada de inventario" : "Registrar merma",
                producto.getNombre(),
                "Cantidad de unidades:",
                "10"
        );
        if (valor.isEmpty()) return;

        Optional<String> observacion = solicitarTexto(
                "Detalle del movimiento",
                producto.getNombre(),
                "Escribe una observación:",
                esEntrada ? "Reposición de inventario" : "Merma registrada"
        );
        if (observacion.isEmpty()) return;

        String textoObservacion = observacion.get();
        valor.ifPresent(cantidad -> {
            boolean actualizado = ejecutar(() -> {
                int unidades = Integer.parseInt(cantidad.trim());
                if (unidades <= 0) {
                    throw new IllegalArgumentException("La cantidad debe ser un número entero mayor que cero.");
                }
                if (textoObservacion == null || textoObservacion.isBlank()) {
                    throw new IllegalArgumentException("Escribe una observación para el movimiento.");
                }
                if (esEntrada) {
                    operaciones.registrarEntradaInventario(producto, unidades, textoObservacion.trim());
                } else {
                    operaciones.registrarMermaInventario(producto, unidades, textoObservacion.trim());
                }
            }, "Movimiento de inventario registrado.");
            if (actualizado) refrescar(tabla, operaciones.consultarProductos());
        });
    }

    // ------------------------------------------------------------------
    // CONFITERÍA Y COMBOS (COMPOSITE)
    // ------------------------------------------------------------------

    private Node vistaConfiteria() {
        VBox root = pagina("Confitería y combos",
                "Administra combos compuestos por productos y otros combos. El inventario se descuenta desde sus componentes base.");

        TableView<Combo> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", Combo::getCodigo),
                columna("Combo", Combo::getNombre),
                columna("Precio", c -> moneda(c.getPrecio())),
                columna("Componentes", c -> String.valueOf(c.getComponentes().size())),
                columna("Estado", c -> c.getEstado().name())
        );

        Button nuevo = boton("＋ Crear combo", "button-gold");
        Button estado = boton("Activar / desactivar", "button-small-outline");
        Button refrescar = boton("Actualizar", "button-small-outline");

        nuevo.setOnAction(e -> formularioCombo(tabla));
        estado.setOnAction(e -> {
            Combo combo = tabla.getSelectionModel().getSelectedItem();
            if (combo == null) { aviso("Selecciona un combo."); return; }
            boolean activar = !combo.estaActivo();
            if (ejecutar(() -> operaciones.cambiarEstadoCombo(combo.getCodigo(), activar),
                    activar ? "Combo activado." : "Combo desactivado.")) {
                refrescar(tabla, operaciones.consultarCombos());
            }
        });
        refrescar.setOnAction(e -> refrescar(tabla, operaciones.consultarCombos()));
        refrescar(tabla, operaciones.consultarCombos());
        root.getChildren().addAll(barra(nuevo, estado, refrescar), tabla);
        return root;
    }

    private void formularioCombo(TableView<Combo> tabla) {
        TextField codigo = campo("", "Código único (ej. COM-001)");
        TextField nombre = campo("", "Nombre del combo");
        TextField precio = campo("", "Precio de venta COP");
        ComboBox<ElementoConfiteria> componente = new ComboBox<>();
        List<ElementoConfiteria> disponibles = new ArrayList<>();
        operaciones.consultarProductos().stream()
                .filter(Producto::estaActivo).forEach(disponibles::add);
        operaciones.consultarCombosActivos().forEach(disponibles::add);
        componente.getItems().setAll(disponibles);
        configurarCombo(componente, this::nombreElementoConfiteria);
        TextField cantidad = campo("1", "Cantidad");
        ListView<String> seleccionados = new ListView<>();
        seleccionados.getStyleClass().add("admin-combo-components");
        seleccionados.setPrefHeight(120);
        Label placeholderComponentes = new Label("Los componentes agregados aparecerán aquí.");
        placeholderComponentes.getStyleClass().add("admin-list-placeholder");
        seleccionados.setPlaceholder(placeholderComponentes);
        Map<String, Integer> componentes = new LinkedHashMap<>();

        Button agregar = boton("Agregar componente", "button-small-outline");
        agregar.setOnAction(e -> {
            ElementoConfiteria elemento = componente.getValue();
            if (elemento == null) { aviso("Selecciona un producto o combo."); return; }
            try {
                int unidades = Integer.parseInt(cantidad.getText().trim());
                if (unidades <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
                String codigoElemento = codigoElementoConfiteria(elemento);
                componentes.merge(codigoElemento, unidades, Integer::sum);
                seleccionados.getItems().setAll(componentes.entrySet().stream()
                        .map(entry -> nombreElementoPorCodigo(disponibles, entry.getKey())
                                + " × " + entry.getValue())
                        .toList());
            } catch (RuntimeException ex) { mostrarError(ex); }
        });

        GridPane grid = formulario();
        fila(grid, 0, "Código", codigo);
        fila(grid, 1, "Nombre", nombre);
        fila(grid, 2, "Precio", precio);
        fila(grid, 3, "Componente", componente);
        fila(grid, 4, "Cantidad", cantidad);
        fila(grid, 5, "Composición", new VBox(7, agregar, seleccionados));

        if (!confirmarFormulario("Crear combo", grid)) return;
        ejecutar(() -> {
            if (componentes.isEmpty()) {
                throw new IllegalArgumentException("Agrega al menos un componente.");
            }
            operaciones.registrarCombo(codigo.getText(), nombre.getText(),
                    new BigDecimal(precio.getText().trim()), componentes);
            refrescar(tabla, operaciones.consultarCombos());
        }, "Combo registrado correctamente.");
    }

    private String nombreElementoConfiteria(ElementoConfiteria elemento) {
        if (elemento instanceof Producto producto) return "Producto · " + producto.getNombre();
        if (elemento instanceof Combo combo) return "Combo · " + combo.getNombre();
        return elemento.consultarDescripcion();
    }

    private String codigoElementoConfiteria(ElementoConfiteria elemento) {
        if (elemento instanceof Producto producto) return producto.getCodigo();
        if (elemento instanceof Combo combo) return combo.getCodigo();
        throw new IllegalArgumentException("Tipo de componente no reconocido.");
    }

    private String nombreElementoPorCodigo(List<ElementoConfiteria> elementos, String codigo) {
        return elementos.stream()
                .filter(elemento -> codigoElementoConfiteria(elemento).equals(codigo))
                .findFirst().map(this::nombreElementoConfiteria).orElse(codigo);
    }

    // ------------------------------------------------------------------
    // PROMOCIONES
    // ------------------------------------------------------------------

    private Node vistaPromociones() {
        VBox root = pagina("Promociones",
                "Crea campañas, consulta su vigencia y cambia su estado.");

        TableView<Promocion> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", Promocion::getCodigo),
                columna("Nombre", Promocion::getNombre),
                columna("Descuento", p -> p.getPorcentajeDescuento() + "%"),
                columna("Desde", p -> p.getFechaInicio().toString()),
                columna("Hasta", p -> p.getFechaFin().toString()),
                columna("Estado", p -> p.getEstado().name())
        );

        Button nueva = boton("＋ Nueva promoción", "button-gold");
        Button estado = boton("Activar / desactivar", "button-small-outline");
        Button refrescar = boton("Actualizar", "button-small-outline");
        nueva.setOnAction(e -> formularioPromocion(tabla));
        estado.setOnAction(e -> {
            Promocion p = tabla.getSelectionModel().getSelectedItem();
            if (p == null) { aviso("Selecciona una promoción."); return; }
            boolean activar = p.getEstado() != EstadoPromocion.ACTIVA;
            if (activar && LocalDate.now().isAfter(p.getFechaFin())) {
                aviso("No puedes activar una promoción cuya vigencia ya terminó.");
                return;
            }
            if (ejecutar(() -> operaciones.cambiarEstadoPromocion(p, activar),
                    activar ? "Promoción activada." : "Promoción desactivada.")) {
                refrescar(tabla, operaciones.consultarPromociones());
            }
        });
        refrescar.setOnAction(e -> refrescar(tabla, operaciones.consultarPromociones()));
        refrescar(tabla, operaciones.consultarPromociones());
        root.getChildren().addAll(barra(nueva, estado, refrescar), tabla);
        return root;
    }

    private void formularioPromocion(TableView<Promocion> tabla) {
        TextField codigo = campo("", "Código promocional");
        TextField nombre = campo("", "Nombre");
        TextArea descripcion = new TextArea();
        descripcion.setPromptText("Descripción");
        descripcion.setPrefRowCount(3);
        configurarAreaTexto(descripcion);
        TextField porcentaje = campo("10", "Porcentaje (1 a 100)");
        DatePicker desde = new DatePicker(LocalDate.now());
        configurarFecha(desde);
        DatePicker hasta = new DatePicker(LocalDate.now().plusDays(30));
        configurarFecha(hasta);
        GridPane grid = formulario();
        fila(grid,0,"Código",codigo); fila(grid,1,"Nombre",nombre);
        fila(grid,2,"Descripción",descripcion); fila(grid,3,"Descuento",porcentaje);
        fila(grid,4,"Fecha inicial",desde); fila(grid,5,"Fecha final",hasta);
        if (!confirmarFormulario("Crear promoción", grid)) return;
        if (desde.getValue() == null || hasta.getValue() == null) {
            aviso("Selecciona las fechas de inicio y finalización de la promoción.");
            return;
        }
        if (hasta.getValue().isBefore(desde.getValue())) {
            aviso("La fecha final no puede ser anterior a la fecha inicial.");
            return;
        }
        if (ejecutar(() -> operaciones.registrarPromocion(
                codigo.getText().trim(), nombre.getText().trim(),
                descripcion.getText().trim(), new BigDecimal(porcentaje.getText().trim()),
                desde.getValue(), hasta.getValue()), "Promoción registrada.")) {
            refrescar(tabla, operaciones.consultarPromociones());
        }
    }

    // ------------------------------------------------------------------
    // CONTROL DE ACCESO
    // ------------------------------------------------------------------

    private Node vistaAcceso() {
        VBox root = pagina("Control de acceso",
                "Valida el código QR de una entrada. Cada entrada solo permite un ingreso.");

        TextField qr = campo("", "Escanea o escribe el código QR");
        qr.setPrefColumnCount(28);
        Button validar = boton("Validar entrada", "button-gold");
        Label resultado = new Label("Esperando un código QR...");
        resultado.getStyleClass().add("page-subtitle");

        TableView<RegistroAcceso> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Fecha", r -> r.getFechaHora().toString()),
                columna("Entrada", r -> r.getEntrada().getCodigo()),
                columna("QR", r -> r.getEntrada().getCodigoQR()),
                columna("Resultado", r -> r.getResultado().name()),
                columna("Observación", RegistroAcceso::getObservacion)
        );
        refrescar(tabla, operaciones.consultarRegistrosAcceso());

        validar.setOnAction(e -> {
            try {
                if (qr.getText() == null || qr.getText().isBlank()) {
                    aviso("Escanea o escribe el código QR antes de validar.");
                    qr.requestFocus();
                    return;
                }
                RegistroAcceso registro = operaciones.validarCodigoQR(qr.getText().trim());
                resultado.setText(registro.getResultado() + " · " + registro.getObservacion());
                resultado.getStyleClass().removeAll("access-success", "access-error");
                resultado.getStyleClass().add(
                        registro.getResultado() == ResultadoAcceso.ACEPTADO
                                ? "access-success" : "access-error");
                qr.clear();
                refrescar(tabla, operaciones.consultarRegistrosAcceso());
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });

        root.getChildren().addAll(barra(qr, validar), resultado,
                new Label("Historial de accesos"), tabla);
        return root;
    }

    // ------------------------------------------------------------------
    // LISTA DE ESPERA
    // ------------------------------------------------------------------

    private Node vistaListaEspera() {
        VBox root = pagina("Lista de espera",
                "Consulta solicitudes por función, libera ofertas vencidas y atiende solicitudes notificadas.");

        List<Funcion> funcionesDisponibles = operaciones.consultarFunciones().stream()
                .filter(f -> f.getEstado() == EstadoFuncion.PROGRAMADA)
                .filter(f -> f.getFechaHora().isAfter(LocalDateTime.now()))
                .sorted(Comparator.comparing(Funcion::getFechaHora))
                .toList();
        ComboBox<Funcion> funcion = new ComboBox<>(
                FXCollections.observableArrayList(funcionesDisponibles));
        configurarCombo(funcion, f -> f.getPelicula().getTitulo()
                + " · " + f.getFechaHora().toString().replace('T', ' ')
                + " · " + f.getSala().getNombre());
        if (!funcionesDisponibles.isEmpty()) {
            funcion.getSelectionModel().selectFirst();
        }

        TableView<SolicitudEspera> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", SolicitudEspera::getCodigo),
                columna("Cliente", s -> s.getCliente().getNombre()),
                columna("Identificación", s -> s.getCliente().getId()),
                columna("Asientos", s -> String.valueOf(s.getCantidadAsientos())),
                columna("Solicitud", s -> s.getFechaSolicitud().toString()),
                columna("Estado", s -> s.getEstado().name()),
                columna("Oferta expira", s -> s.getFechaExpiracionOferta() == null
                        ? "—" : s.getFechaExpiracionOferta().toString())
        );

        Button consultar = boton("Consultar", "button-small-outline");
        Button notificar = boton("Notificar siguiente", "button-gold");
        Button atender = boton("Atender solicitud", "button-small-outline");
        Button cancelar = boton("Cancelar solicitud", "button-small-outline");
        Button expirar = boton("Procesar vencidas", "button-small-outline");

        Runnable cargar = () -> {
            if (funcion.getValue() != null) {
                refrescar(tabla, operaciones.consultarSolicitudesEspera(funcion.getValue()));
            }
        };
        consultar.setOnAction(e -> cargar.run());
        notificar.setOnAction(e -> {
            Funcion f = funcion.getValue();
            if (f == null) { aviso("Selecciona una función."); return; }
            Optional<String> n = solicitarTexto(
                    "Notificar lista de espera",
                    f.getPelicula().getTitulo() + " · " + f.getFechaHora().toString().replace('T', ' '),
                    "Cantidad de asientos liberados:",
                    "2"
            );
            if (n.isEmpty()) return;
            if (ejecutar(() -> {
                int asientos = Integer.parseInt(n.get().trim());
                if (asientos <= 0) {
                    throw new IllegalArgumentException("La cantidad de asientos liberados debe ser mayor que cero.");
                }
                Optional<SolicitudEspera> oferta = operaciones.notificarSiguiente(f, asientos, 10);
                if (oferta.isPresent()) {
                    aviso("Oferta enviada a " + oferta.get().getCliente().getNombre()
                            + ". Tiene 10 minutos para responder.");
                } else {
                    aviso("No hay solicitudes activas compatibles con esa cantidad de asientos.");
                }
            }, "")) {
                cargar.run();
            }
        });
        atender.setOnAction(e -> {
            SolicitudEspera s = tabla.getSelectionModel().getSelectedItem();
            if (s == null) { aviso("Selecciona una solicitud."); return; }
            if (s.getEstado() != EstadoSolicitudEspera.NOTIFICADA) {
                aviso("Solo puedes atender solicitudes que tengan una oferta notificada.");
                return;
            }
            if (ejecutar(() -> operaciones.atenderSolicitud(s), "Solicitud atendida.")) {
                cargar.run();
            }
        });
        cancelar.setOnAction(e -> {
            SolicitudEspera s = tabla.getSelectionModel().getSelectedItem();
            if (s == null) { aviso("Selecciona una solicitud."); return; }
            if (s.getEstado() != EstadoSolicitudEspera.ACTIVA
                    && s.getEstado() != EstadoSolicitudEspera.NOTIFICADA) {
                aviso("La solicitud ya no está activa y no se puede cancelar.");
                return;
            }
            if (ejecutar(() -> operaciones.cancelarSolicitud(s), "Solicitud cancelada.")) {
                cargar.run();
            }
        });
        expirar.setOnAction(e -> {
            if (ejecutar(() -> aviso(
                    "Ofertas expiradas: " + operaciones.procesarOfertasVencidas()), "")) {
                cargar.run();
            }
        });
        funcion.setOnAction(e -> cargar.run());
        cargar.run();

        root.getChildren().addAll(barra(funcion, consultar, notificar, atender, cancelar, expirar), tabla);
        return root;
    }

    // ------------------------------------------------------------------
    // REPORTES
    // ------------------------------------------------------------------

    private Node vistaReportes() {
        VBox root = pagina("Reportes",
                "Genera informes de ventas, entradas, ocupación, productos, recargas, cancelaciones y puntos.");

        ComboBox<String> tipo = new ComboBox<>(FXCollections.observableArrayList(
                "Ventas", "Entradas por película", "Entradas por función",
                "Ocupación", "Productos", "Recargas",
                "Cancelaciones y reembolsos", "Puntos", "Reembolsos"));
        tipo.setValue("Ventas");
        DatePicker desde = new DatePicker(LocalDate.now().withDayOfMonth(1));
        configurarFecha(desde);
        DatePicker hasta = new DatePicker(LocalDate.now());
        configurarFecha(hasta);
        TextArea salida = new TextArea();
        salida.setEditable(false);
        salida.setWrapText(true);
        salida.setPrefRowCount(20);
        salida.getStyleClass().add("report-output");
        configurarAreaTexto(salida);

        Button generar = boton("Generar reporte", "button-gold");
        Button exportar = boton("Exportar TXT", "button-small-outline");

        generar.setOnAction(e -> {
            try {
                if (tipo.getValue() == null || desde.getValue() == null || hasta.getValue() == null) {
                    aviso("Selecciona el tipo de reporte y ambas fechas.");
                    return;
                }
                if (hasta.getValue().isBefore(desde.getValue())) {
                    aviso("La fecha final no puede ser anterior a la fecha inicial.");
                    return;
                }
                salida.setText(operaciones.generarReporte(
                        tipo.getValue(), desde.getValue(), hasta.getValue()));
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });

        exportar.setOnAction(e -> {
            if (salida.getText().isBlank()) {
                aviso("Primero genera un reporte.");
                return;
            }
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Guardar reporte");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Archivo de texto", "*.txt"));
            chooser.setInitialFileName("reporte-cinemauq.txt");
            File destino = chooser.showSaveDialog(owner);
            if (destino == null) return;
            try {
                Files.writeString(destino.toPath(), salida.getText());
                aviso("Reporte exportado correctamente.");
            } catch (Exception ex) {
                mostrarError(ex);
            }
        });

        GridPane filtros = formulario();
        fila(filtros,0,"Reporte",tipo);
        fila(filtros,1,"Desde",desde);
        fila(filtros,2,"Hasta",hasta);

        root.getChildren().addAll(filtros, barra(generar, exportar), salida);
        VBox.setVgrow(salida, Priority.ALWAYS);
        return root;
    }

    // ------------------------------------------------------------------
    // TARJETAS VIRTUALES PARA ADMINISTRACIÓN
    // ------------------------------------------------------------------

    private Node vistaTarjetas() {
        VBox root = pagina("Tarjetas virtuales",
                "Consulta saldos, recarga tarjetas y bloquea o activa medios de pago.");

        TableView<TarjetaVirtual> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Número", t -> mascara(t.getNumero())),
                columna("Cliente", t -> t.getTitular().getNombre()),
                columna("Identificación", t -> t.getTitular().getId()),
                columna("Saldo", t -> moneda(t.getSaldo())),
                columna("Estado", t -> t.getEstado().name())
        );

        Button recargar = boton("Recargar", "button-gold");
        Button cambiar = boton("Bloquear / activar", "button-small-outline");
        Button refrescar = boton("Actualizar", "button-small-outline");

        recargar.setOnAction(e -> {
            TarjetaVirtual t = tabla.getSelectionModel().getSelectedItem();
            if (t == null) { aviso("Selecciona una tarjeta."); return; }
            Optional<String> valor = solicitarTexto(
                    "Recarga de tarjeta",
                    t.getTitular().getNombre(),
                    "Valor de la recarga (COP):",
                    "50000"
            );

            valor.ifPresent(texto -> {
                if (ejecutar(
                        () -> operaciones.recargarTarjeta(
                                t, new BigDecimal(texto.trim()), administrador),
                        "Recarga aplicada. Saldo: " + moneda(t.getSaldo()))) {
                    refrescar(tabla, operaciones.consultarTarjetas());
                }
            });
        });
        cambiar.setOnAction(e -> {
            TarjetaVirtual t = tabla.getSelectionModel().getSelectedItem();
            if (t == null) { aviso("Selecciona una tarjeta."); return; }
            boolean activar = t.getEstado() != EstadoTarjeta.ACTIVA;
            if (ejecutar(() -> operaciones.cambiarEstadoTarjeta(t, activar),
                    activar ? "Tarjeta activada." : "Tarjeta bloqueada.")) {
                refrescar(tabla, operaciones.consultarTarjetas());
            }
        });
        refrescar.setOnAction(e -> refrescar(tabla, operaciones.consultarTarjetas()));
        refrescar(tabla, operaciones.consultarTarjetas());

        root.getChildren().addAll(barra(recargar, cambiar, refrescar), tabla);
        return root;
    }

    // ------------------------------------------------------------------
    // COMPONENTES REUTILIZABLES
    // ------------------------------------------------------------------

    private VBox pagina(String titulo, String descripcion) {
        VBox root = new VBox(16);
        root.getStyleClass().add("dashboard-main");
        Label t = new Label(titulo);
        t.getStyleClass().add("page-title");
        Label d = new Label(descripcion);
        d.getStyleClass().add("page-subtitle");
        d.setWrapText(true);
        root.getChildren().addAll(t, d);
        return root;
    }

    private VBox vistaNoDisponible(String seccion) {
        VBox box = pagina(seccion, "Esta sección no está habilitada para este rol.");
        return box;
    }

    private <T> TableView<T> tabla() {
        TableView<T> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No hay registros para mostrar."));
        table.setPrefHeight(560);
        table.setMinHeight(260);
        table.getStyleClass().add("cinemauq-table");
        return table;
    }

    private <T> TableColumn<T, String> columna(
            String titulo, Function<T, String> valor) {
        TableColumn<T, String> column = new TableColumn<>(titulo);
        column.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        Objects.toString(valor.apply(data.getValue()), "—")));
        return column;
    }

    private <T> void refrescar(TableView<T> tabla, Collection<T> datos) {
        tabla.setItems(FXCollections.observableArrayList(datos));
        // Las entidades del modelo son objetos mutables sin propiedades JavaFX.
        // refresh() obliga a recalcular las celdas que muestran su estado actual.
        tabla.refresh();
    }

    private HBox barra(Node... elementos) {
        HBox h = new HBox(8, elementos);
        h.setAlignment(Pos.CENTER_LEFT);
        h.setPadding(new Insets(4, 0, 8, 0));
        h.setMinHeight(42);
        return h;
    }

    private Button boton(String texto, String estilo) {
        Button b = new Button(texto);
        b.getStyleClass().add(estilo);
        return b;
    }

    private TextField campo(String valor, String prompt) {
        TextField f = new TextField(valor);
        f.setPromptText(prompt);
        f.getStyleClass().add("input-field");
        return f;
    }

    /**
     * Unifica el aspecto y el tamaño de los selectores de fecha del panel
     * administrativo. El CSS personalizado también estiliza el calendario popup.
     */
    private void configurarFecha(DatePicker selector) {
        selector.getStyleClass().add("admin-date-picker");
        selector.setPrefWidth(255);
        selector.setMinWidth(225);
        selector.setMaxWidth(280);
        selector.setPrefHeight(40);
        selector.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Aplica el tema azul oscuro a todos los campos multilínea del administrador.
     * El ajuste de envoltura evita barras horizontales en descripciones largas.
     */
    /**
     * Aplica el estilo CinemaUQ a las casillas de selección administrativas.
     */
    private void configurarCasilla(CheckBox casilla) {
        casilla.getStyleClass().add("admin-check-box");
        casilla.setMnemonicParsing(false);
    }

    private void configurarAreaTexto(TextArea area) {
        area.getStyleClass().add("admin-text-area");
        area.setWrapText(true);
        area.setMaxWidth(Double.MAX_VALUE);
    }

    private GridPane formulario() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));
        return grid;
    }

    private void fila(GridPane grid, int fila, String titulo, Node campo) {
        Label label = new Label(titulo);
        label.getStyleClass().add("field-label");
        grid.add(label, 0, fila);
        grid.add(campo, 1, fila);
        GridPane.setHgrow(campo, Priority.ALWAYS);
    }

    /**
     * Solicita un dato mediante un diálogo propio de CinemaUQ.
     * Reemplaza los TextInputDialog nativos para evitar cuadros blancos
     * y botones predeterminados en los flujos administrativos.
     */
    private Optional<String> solicitarTexto(
            String titulo,
            String contexto,
            String etiqueta,
            String valorInicial
    ) {
        Dialog<String> dialogo = new Dialog<>();

        if (owner != null) {
            dialogo.initOwner(owner);
        }

        dialogo.setTitle("CinemaUQ · " + titulo);
        dialogo.setHeaderText(null);

        ButtonType confirmar = new ButtonType("Confirmar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().addAll(confirmar, cancelar);
        panel.setMinWidth(430);
        panel.setPrefWidth(455);
        panel.setStyle(
                "-fx-background-color: #071629;"
                        + "-fx-border-color: #344c67;"
                        + "-fx-border-width: 1;"
        );

        VBox contenido = new VBox(12);
        contenido.setAlignment(Pos.CENTER_LEFT);
        contenido.setPadding(new Insets(20, 23, 18, 23));
        contenido.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #10243a, #09192b);"
                        + "-fx-background-radius: 10;"
        );

        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle(
                "-fx-text-fill: #f5f7fb;"
                        + "-fx-font-size: 17px;"
                        + "-fx-font-weight: bold;"
        );

        Label contextoLabel = new Label(
                contexto == null ? "" : contexto
        );
        contextoLabel.setWrapText(true);
        contextoLabel.setMaxWidth(Double.MAX_VALUE);
        contextoLabel.setStyle(
                "-fx-text-fill: #aebfd1;"
                        + "-fx-font-size: 11px;"
        );

        Label etiquetaLabel = new Label(etiqueta);
        etiquetaLabel.setStyle(
                "-fx-text-fill: #e9bb4e;"
                        + "-fx-font-size: 10px;"
                        + "-fx-font-weight: bold;"
        );

        TextField entrada = new TextField(valorInicial == null ? "" : valorInicial);
        entrada.setPromptText(etiqueta);
        entrada.getStyleClass().add("input-field");
        entrada.setStyle(
                "-fx-control-inner-background: #0b1b2e;"
                        + "-fx-background-color: #0b1b2e;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #405d7c;"
                        + "-fx-border-radius: 8;"
                        + "-fx-text-fill: #f3f6fb;"
                        + "-fx-prompt-text-fill: #8298b0;"
                        + "-fx-highlight-fill: #d9a52b;"
                        + "-fx-highlight-text-fill: #101827;"
                        + "-fx-font-size: 12px;"
                        + "-fx-padding: 9 12;"
        );
        entrada.setMaxWidth(Double.MAX_VALUE);
        entrada.setPrefHeight(40);

        contenido.getChildren().addAll(
                tituloLabel,
                contextoLabel,
                etiquetaLabel,
                entrada
        );

        panel.setContent(contenido);

        Button botonConfirmar = (Button) panel.lookupButton(confirmar);
        botonConfirmar.setDefaultButton(true);
        botonConfirmar.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffd34d, #eaa018);"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #101827;"
                        + "-fx-font-size: 11px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 9 20;"
                        + "-fx-cursor: hand;"
        );

        Button botonCancelar = (Button) panel.lookupButton(cancelar);
        botonCancelar.setCancelButton(true);
        botonCancelar.setStyle(
                "-fx-background-color: #152b43;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #3a5675;"
                        + "-fx-border-radius: 8;"
                        + "-fx-text-fill: #dce6f2;"
                        + "-fx-font-size: 11px;"
                        + "-fx-padding: 9 18;"
                        + "-fx-cursor: hand;"
        );

        dialogo.setResultConverter(tipo ->
                tipo == confirmar ? entrada.getText() : null
        );

        dialogo.setOnShown(evento -> {
            entrada.requestFocus();
            entrada.selectAll();
        });

        return dialogo.showAndWait()
                .filter(Objects::nonNull);
    }

    private boolean confirmarFormulario(String titulo, Node contenido) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("CinemaUQ · " + titulo);
        if (owner != null) dialog.initOwner(owner);
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setContent(contenido);
        pane.setPrefWidth(600);
        pane.setStyle("-fx-background-color: #0b1b2e; -fx-border-color: #294765;");
        Button aceptar = (Button) pane.lookupButton(ButtonType.OK);
        aceptar.setText("Guardar");
        aceptar.setStyle("-fx-background-color: linear-gradient(to right, #ffd34d, #eaa018);"
                + "-fx-text-fill: #101827; -fx-font-weight: bold; -fx-background-radius: 8;");
        Button cancelar = (Button) pane.lookupButton(ButtonType.CANCEL);
        cancelar.setText("Cancelar");
        cancelar.setStyle("-fx-background-color: #152b43; -fx-text-fill: #dce6f2;"
                + "-fx-border-color: #3a5675; -fx-background-radius: 8; -fx-border-radius: 8;");
        return dialog.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    private <T> void configurarCombo(ComboBox<T> combo, Function<T, String> texto) {
        StringConverter<T> converter = new StringConverter<>() {
            @Override public String toString(T object) {
                return object == null ? "" : texto.apply(object);
            }
            @Override public T fromString(String string) { return null; }
        };
        combo.setConverter(converter);
        combo.setCellFactory(list -> new ListCell<>() {
            {
                getStyleClass().add("cinemauq-combo-cell");
            }

            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : texto.apply(item));
            }
        });
        combo.setButtonCell(new ListCell<>() {
            {
                getStyleClass().add("cinemauq-combo-button-cell");
            }

            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : texto.apply(item));
            }
        });
        combo.setMaxWidth(Double.MAX_VALUE);
    }

    private boolean ejecutar(Runnable operacion, String mensajeExito) {
        try {
            operacion.run();
            if (mensajeExito != null && !mensajeExito.isBlank()) {
                aviso(mensajeExito);
            }
            return true;
        } catch (RuntimeException ex) {
            mostrarError(ex);
            return false;
        }
    }

    private void aviso(String mensaje) {
        String texto = mensaje == null || mensaje.isBlank()
                ? "La operación requiere tu atención."
                : mensaje.trim();
        String lower = texto.toLowerCase(Locale.ROOT);
        boolean exito = lower.contains("registrad")
                || lower.contains("correctamente")
                || lower.contains("actualizad")
                || lower.contains("activad")
                || lower.contains("cancelad")
                || lower.contains("finalizad")
                || lower.contains("exportad")
                || lower.contains("recargad")
                || lower.contains("atendida")
                || lower.contains("aplicada")
                || lower.contains("guardad");
        mostrarDialogoAdmin(
                exito ? "¡Operación completada!" : "Aviso de CinemaUQ",
                texto,
                exito,
                null
        );
    }

    private void mostrarError(Throwable ex) {
        String detalle = mensajeError(ex);
        String lower = detalle.toLowerCase(Locale.ROOT);
        String titulo = lower.contains("saldo")
                && (lower.contains("insuficiente") || lower.contains("suficiente"))
                ? "Saldo insuficiente"
                : "No se pudo completar la operación";

        String recomendacion = lower.contains("saldo")
                ? "Verifica el saldo disponible e inténtalo nuevamente."
                : "Revisa los datos ingresados. Si el problema continúa, verifica la información y vuelve a intentarlo.";

        mostrarDialogoAdmin(titulo, detalle, false, recomendacion);
    }

    private String mensajeError(Throwable ex) {
        Throwable actual = ex;
        while (actual != null) {
            if (actual.getMessage() != null && !actual.getMessage().isBlank()) {
                return actual.getMessage().trim();
            }
            actual = actual.getCause();
        }
        return "Se produjo un error inesperado. Inténtalo nuevamente.";
    }

    private void mostrarDialogoAdmin(
            String titulo,
            String mensaje,
            boolean exito,
            String recomendacion
    ) {
        Dialog<Void> dialogo = new Dialog<>();
        if (owner != null) {
            dialogo.initOwner(owner);
        }
        dialogo.setTitle("CinemaUQ · Administración");
        dialogo.setHeaderText(null);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.OK);

        DialogPane panel = dialogo.getDialogPane();
        panel.setMinWidth(430);
        panel.setPrefWidth(455);
        panel.setStyle(
                "-fx-background-color: #071629;"
                        + "-fx-border-color: #344c67;"
                        + "-fx-border-width: 1;"
        );

        Button aceptar = (Button) panel.lookupButton(ButtonType.OK);
        aceptar.setText("Entendido");
        aceptar.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffd34d, #eaa018);"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #101827;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 9 24;"
                        + "-fx-cursor: hand;"
        );

        VBox contenido = new VBox(12);
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new Insets(22, 25, 20, 25));
        contenido.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #10243a, #09192b);"
                        + "-fx-background-radius: 11;"
        );

        String color = exito ? "#78E0B5" : "#FFD04A";
        String fondo = exito
                ? "rgba(63, 190, 139, 0.15)"
                : "rgba(238, 177, 45, 0.13)";
        String borde = exito ? "#3A9B78" : "#A77A24";

        StackPane iconoFondo = new StackPane();
        iconoFondo.setMinSize(56, 56);
        iconoFondo.setPrefSize(56, 56);
        iconoFondo.setMaxSize(56, 56);
        iconoFondo.setStyle(
                "-fx-background-color: " + fondo + ";"
                        + "-fx-border-color: " + borde + ";"
                        + "-fx-background-radius: 40; -fx-border-radius: 40;"
        );

        Label icono = new Label(exito ? "✓" : "!");
        icono.setStyle(
                "-fx-text-fill: " + color + ";"
                        + "-fx-font-size: 27px; -fx-font-weight: bold;"
        );
        iconoFondo.getChildren().add(icono);

        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle(
                "-fx-text-fill: #F5F7FB;"
                        + "-fx-font-size: 19px; -fx-font-weight: bold;"
        );

        Label detalle = new Label(mensaje);
        detalle.setWrapText(true);
        detalle.setMaxWidth(350);
        detalle.setAlignment(Pos.CENTER);
        detalle.setStyle("-fx-text-fill: #C2CFDD; -fx-font-size: 12px;");

        contenido.getChildren().addAll(iconoFondo, tituloLabel, detalle);

        if (recomendacion != null && !recomendacion.isBlank()) {
            Label ayuda = new Label(recomendacion);
            ayuda.setWrapText(true);
            ayuda.setMaxWidth(350);
            ayuda.setAlignment(Pos.CENTER);
            ayuda.setStyle("-fx-text-fill: #8FA6BE; -fx-font-size: 10px;");
            contenido.getChildren().add(ayuda);
        }

        Label marca = new Label("CINEMAUQ  ·  PANEL DE ADMINISTRACIÓN");
        marca.setStyle("-fx-text-fill: #7189A2; -fx-font-size: 8px; -fx-font-weight: bold;");
        contenido.getChildren().add(marca);

        panel.setContent(contenido);
        dialogo.showAndWait();
    }

    private String moneda(BigDecimal valor) {
        return java.text.NumberFormat.getCurrencyInstance(new Locale("es", "CO"))
                .format(valor);
    }

    private String mascara(String numero) {
        if (numero == null || numero.length() <= 4) return numero;
        return "•••• •••• " + numero.substring(numero.length() - 4);
    }
}
