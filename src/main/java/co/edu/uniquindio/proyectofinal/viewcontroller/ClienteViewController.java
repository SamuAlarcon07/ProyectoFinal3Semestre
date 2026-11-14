
package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.controller.CinemaOperationsController;
import co.edu.uniquindio.proyectofinal.model.enums.EstadoCancelacion;
import co.edu.uniquindio.proyectofinal.model.enums.TipoListaPelicula;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;
import co.edu.uniquindio.proyectofinal.model.principal.*;
import co.edu.uniquindio.proyectofinal.model.util.QrCodeGenerator;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

/**
 * Construye los módulos del cliente dentro del Dashboard compartido.
 */
public class ClienteViewController {

    private final CinemaOperationsController operaciones;
    private final Cliente cliente;
    private final Window owner;
    private Runnable alActualizarPerfil = () -> { };
    private Runnable alActualizarResumen = () -> { };

    public ClienteViewController(
            CinemaOperationsController operaciones,
            Cliente cliente,
            Window owner) {
        this.operaciones = Objects.requireNonNull(operaciones);
        this.cliente = Objects.requireNonNull(cliente);
        this.owner = owner;
    }

    public void setAlActualizarPerfil(Runnable callback) {
        this.alActualizarPerfil = callback == null ? () -> { } : callback;
    }

    /** Notifica al Dashboard que debe sincronizar saldo y puntos del panel lateral. */
    public void setAlActualizarResumen(Runnable callback) {
        this.alActualizarResumen = callback == null ? () -> { } : callback;
    }

    public Node construir(String seccion) {
        return switch (seccion) {
            case "Mis entradas" -> vistaEntradas();
            case "Confitería" -> vistaConfiteria();
            case "Lista de espera" -> vistaListaEspera();
            case "Mis compras" -> vistaCompras();
            case "Mi tarjeta virtual" -> vistaTarjeta();
            case "Mis puntos" -> vistaPuntos();
            case "Películas guardadas" -> vistaPeliculasGuardadas();
            case "Reseñas" -> vistaResenas();
            case "Mi perfil" -> vistaPerfil();
            case "Notificaciones" -> vistaNotificaciones();
            default -> vistaInformacion(seccion);
        };
    }

    private Node vistaEntradas() {
        VBox root = pagina("Mis entradas",
                "Tus entradas digitales, listas para disfrutar la experiencia CinemaUQ.");

        List<Entrada> entradas = new ArrayList<>(operaciones.consultarEntradas(cliente));

        HBox resumen = new HBox(12);
        resumen.getStyleClass().add("tickets-summary");
        resumen.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox resumenTexto = new VBox(4);
        Label resumenTitulo = new Label("ENTRADAS ADQUIRIDAS");
        resumenTitulo.getStyleClass().add("tickets-summary-label");
        Label resumenCantidad = new Label(String.valueOf(entradas.size()));
        resumenCantidad.getStyleClass().add("tickets-summary-number");
        resumenTexto.getChildren().addAll(resumenTitulo, resumenCantidad);

        Region separador = new Region();
        HBox.setHgrow(separador, Priority.ALWAYS);
        // La lista se declara antes del evento para que pueda utilizarse dentro de la lambda.
        VBox lista = new VBox(13);
        Button actualizar = boton("↻  Actualizar", "button-small-outline");
        actualizar.setOnAction(e -> {
            entradas.clear();
            entradas.addAll(operaciones.consultarEntradas(cliente));
            actualizarListaEntradas(lista, entradas, resumenCantidad);
        });
        resumen.getChildren().addAll(resumenTexto, separador, actualizar);

        lista.setFillWidth(true);
        lista.setId("lista-entradas");
        root.getChildren().addAll(resumen, lista);
        actualizarListaEntradas(lista, entradas, resumenCantidad);
        return root;
    }

    private void actualizarListaEntradas(VBox lista, List<Entrada> entradas, Label contador) {
        contador.setText(String.valueOf(entradas.size()));
        lista.getChildren().clear();

        if (entradas.isEmpty()) {
            VBox vacio = new VBox(9);
            vacio.setAlignment(javafx.geometry.Pos.CENTER);
            vacio.getStyleClass().add("tickets-empty");
            Label icono = new Label("🎟");
            icono.getStyleClass().add("tickets-empty-icon");
            Label titulo = new Label("Aún no tienes entradas");
            titulo.getStyleClass().add("tickets-empty-title");
            Label texto = new Label("Cuando compres una entrada, aparecerá aquí con su código QR.");
            texto.getStyleClass().add("tickets-empty-text");
            texto.setWrapText(true);
            texto.setMaxWidth(360);
            texto.setAlignment(javafx.geometry.Pos.CENTER);
            vacio.getChildren().addAll(icono, titulo, texto);
            lista.getChildren().add(vacio);
            return;
        }

        for (Entrada entrada : entradas) {
            lista.getChildren().add(crearTarjetaEntrada(entrada));
        }
    }

    private Node crearTarjetaEntrada(Entrada entrada) {
        VBox tarjeta = new VBox(12);
        tarjeta.getStyleClass().add("ticket-card");

        HBox encabezado = new HBox(10);
        encabezado.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        VBox peliculaInfo = new VBox(4);
        Label pelicula = new Label(entrada.getFuncion().getPelicula().getTitulo());
        pelicula.getStyleClass().add("ticket-movie-title");
        Label codigo = new Label("CÓDIGO  ·  " + entrada.getCodigo());
        codigo.getStyleClass().add("ticket-code");
        peliculaInfo.getChildren().addAll(pelicula, codigo);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Label estado = new Label(entrada.getEstado().name().replace('_', ' '));
        estado.getStyleClass().add("ticket-status");
        if (entrada.getEstado().name().equals("EMITIDA")) {
            estado.getStyleClass().add("ticket-status-active");
        } else if (entrada.getEstado().name().equals("UTILIZADA")) {
            estado.getStyleClass().add("ticket-status-used");
        } else {
            estado.getStyleClass().add("ticket-status-cancelled");
        }
        encabezado.getChildren().addAll(peliculaInfo, espacio, estado);

        HBox detalles = new HBox(0);
        detalles.getStyleClass().add("ticket-details");
        detalles.getChildren().addAll(
                detalleEntrada("◷", "FECHA Y HORA",
                        entrada.getFuncion().getFechaHora().toString().replace('T', ' ')),
                detalleEntrada("▣", "SALA",
                        entrada.getFuncion().getSala().getNombre()),
                detalleEntrada("⌖", "ASIENTO",
                        entrada.getAsiento().obtenerIdentificador())
        );

        HBox pie = new HBox(10);
        pie.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label nota = new Label("Presenta el QR en el ingreso a la sala");
        nota.getStyleClass().add("ticket-hint");
        Region espacioPie = new Region();
        HBox.setHgrow(espacioPie, Priority.ALWAYS);
        Button qr = boton("▦  Ver código QR", "button-gold");
        qr.setOnAction(e -> mostrarCodigoQR(entrada));
        pie.getChildren().addAll(nota, espacioPie, qr);

        tarjeta.getChildren().addAll(encabezado, detalles, pie);
        return tarjeta;
    }

    private VBox detalleEntrada(String icono, String titulo, String valor) {
        VBox detalle = new VBox(5);
        detalle.getStyleClass().add("ticket-detail");
        Label simbolo = new Label(icono);
        simbolo.getStyleClass().add("ticket-detail-icon");
        Label etiqueta = new Label(titulo);
        etiqueta.getStyleClass().add("ticket-detail-label");
        Label contenido = new Label(valor);
        contenido.getStyleClass().add("ticket-detail-value");
        contenido.setWrapText(true);
        detalle.getChildren().addAll(simbolo, etiqueta, contenido);
        HBox.setHgrow(detalle, Priority.ALWAYS);
        return detalle;
    }

    /**
     * Presenta una imagen QR escaneable y los datos principales de la entrada.
     * El contenido del QR es el identificador que valida ControlAccesoService.
     */
    private void mostrarCodigoQR(Entrada entrada) {
        Dialog<Void> dialogo = new Dialog<>();
        configurarDialogoCinemaUQ(dialogo);
        dialogo.setTitle("Entrada digital · CinemaUQ");
        if (owner != null) dialogo.initOwner(owner);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Button cerrar = (Button) dialogo.getDialogPane().lookupButton(ButtonType.CLOSE);
        cerrar.setText("Cerrar");
        cerrar.setStyle("-fx-background-color: #f5b91b; -fx-text-fill: #101827;"
                + "-fx-font-weight: bold; -fx-background-radius: 8;");

        VBox contenido = new VBox(12);
        contenido.setAlignment(javafx.geometry.Pos.CENTER);
        contenido.setPadding(new Insets(22));
        contenido.setStyle("-fx-background-color: #0b1b2d;");

        Label titulo = new Label("Tu entrada CinemaUQ");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 21px; -fx-font-weight: bold;");
        Label subtitulo = new Label("Presenta este código en el control de acceso");
        subtitulo.setStyle("-fx-text-fill: #b7c8d9; -fx-font-size: 12px;");

        ImageView imagenQR = new ImageView(QrCodeGenerator.generar(entrada.getCodigoQR(), 300));
        imagenQR.setFitWidth(250);
        imagenQR.setFitHeight(250);
        imagenQR.setPreserveRatio(true);
        imagenQR.setSmooth(false);

        StackPane marcoQR = new StackPane(imagenQR);
        marcoQR.setPadding(new Insets(12));
        marcoQR.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        VBox datos = new VBox(6);
        datos.setMaxWidth(Double.MAX_VALUE);
        datos.setPadding(new Insets(13));
        datos.setStyle("-fx-background-color: #142a40; -fx-background-radius: 10;"
                + "-fx-border-color: #294760; -fx-border-radius: 10;");

        Label pelicula = new Label(entrada.getFuncion().getPelicula().getTitulo());
        pelicula.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label funcion = new Label("Función: "
                + entrada.getFuncion().getFechaHora().toString().replace('T', ' '));
        funcion.setStyle("-fx-text-fill: #c4d3e0; -fx-font-size: 11px;");
        Label asiento = new Label("Asiento: " + entrada.getAsiento().obtenerIdentificador());
        asiento.setStyle("-fx-text-fill: #c4d3e0; -fx-font-size: 11px;");
        Label codigo = new Label("Entrada: " + entrada.getCodigo());
        codigo.setWrapText(true);
        codigo.setStyle("-fx-text-fill: #f5c242; -fx-font-size: 10px;");
        Label estado = new Label("Estado: " + entrada.getEstado().name());
        estado.setStyle("-fx-text-fill: #72d6a8; -fx-font-size: 11px; -fx-font-weight: bold;");
        datos.getChildren().addAll(pelicula, funcion, asiento, codigo, estado);

        Label nota = new Label("Cada código es único y solo puede utilizarse una vez.");
        nota.setStyle("-fx-text-fill: #91a9bf; -fx-font-size: 10px;");
        nota.setWrapText(true);

        contenido.getChildren().addAll(titulo, subtitulo, marcoQR, datos, nota);
        dialogo.getDialogPane().setContent(contenido);
        dialogo.getDialogPane().setStyle("-fx-background-color: #0b1b2d;"
                + "-fx-border-color: #294760;");
        dialogo.getDialogPane().setPrefWidth(390);
        dialogo.showAndWait();
    }

    private Node vistaCompras() {
        VBox root = pagina("Mis compras",
                "Revisa tu historial de transacciones, consulta los detalles y gestiona solicitudes de cancelación.");
        root.setSpacing(15);

        List<Compra> compras = new ArrayList<>(operaciones.consultarCompras(cliente));

        // Indicadores rápidos del historial de compras.
        Label totalCompras = new Label();
        Label comprasPagadas = new Label();
        Label totalGastado = new Label();

        HBox resumen = new HBox(12,
                tarjetaResumenCompra("COMPRAS REGISTRADAS", totalCompras, "▤"),
                tarjetaResumenCompra("COMPRAS PAGADAS", comprasPagadas, "✓"),
                tarjetaResumenCompra("TOTAL PAGADO", totalGastado, "$"));
        resumen.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        resumen.setFillHeight(true);

        TableView<Compra> tabla = tabla();
        tabla.setPrefHeight(430);
        tabla.getColumns().addAll(
                columna("Código", Compra::getCodigo),
                columna("Fecha", c -> c.getFecha().toString().replace('T', ' ')),
                columna("Artículos", c -> String.valueOf(c.getDetalles().size())),
                columna("Subtotal", c -> moneda(c.calcularSubtotal())),
                columna("Descuentos", c -> moneda(c.getDescuentos())),
                columna("Total", c -> moneda(c.getTotal())),
                columna("Estado", Compra::consultarNombreEstado)
        );

        Runnable cargar = () -> {
            compras.clear();
            compras.addAll(operaciones.consultarCompras(cliente));
            refrescar(tabla, compras);

            long pagadas = compras.stream()
                    .filter(c -> "PAGADA".equals(c.consultarNombreEstado()))
                    .count();
            BigDecimal gastado = compras.stream()
                    .filter(c -> "PAGADA".equals(c.consultarNombreEstado()))
                    .map(Compra::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalCompras.setText(String.valueOf(compras.size()));
            comprasPagadas.setText(String.valueOf(pagadas));
            totalGastado.setText(moneda(gastado));
        };
        cargar.run();

        Button detalle = boton("▤  Ver detalle", "button-small-outline");
        Button cancelar = boton("↶  Solicitar cancelación", "button-gold");
        Button actualizar = boton("↻  Actualizar", "button-small-outline");

        detalle.setDisable(true);
        cancelar.setDisable(true);
        tabla.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionada) -> {
                    detalle.setDisable(seleccionada == null);
                    cancelar.setDisable(seleccionada == null);
                });

        detalle.setOnAction(e -> {
            Compra compra = tabla.getSelectionModel().getSelectedItem();
            if (compra == null) {
                aviso("Selecciona una compra para consultar sus detalles.");
                return;
            }

            Dialog<Void> dialogo = new Dialog<>();
            configurarDialogoCinemaUQ(dialogo);
            if (owner != null) dialogo.initOwner(owner);
            dialogo.setTitle("Detalle de compra · CinemaUQ");
            dialogo.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialogo.getDialogPane().setStyle(
                    "-fx-background-color: #081a2d; -fx-border-color: #294764;");
            dialogo.getDialogPane().setPrefWidth(430);

            Button cerrar = (Button) dialogo.getDialogPane().lookupButton(ButtonType.CLOSE);
            cerrar.setText("Cerrar");
            cerrar.setStyle("-fx-background-color: #f5b91b; -fx-text-fill: #101827;"
                    + "-fx-font-weight: bold; -fx-background-radius: 8;");

            VBox contenido = new VBox(12);
            contenido.setPadding(new Insets(20));
            contenido.setStyle("-fx-background-color: #081a2d;");

            Label titulo = new Label("Resumen de tu compra");
            titulo.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");
            Label codigo = new Label(compra.getCodigo());
            codigo.setStyle("-fx-text-fill: #f5c242; -fx-font-size: 12px; -fx-font-weight: bold;");

            VBox articulos = new VBox(8);
            articulos.setPadding(new Insets(13));
            articulos.setStyle("-fx-background-color: #102943; -fx-background-radius: 10;"
                    + "-fx-border-color: #294764; -fx-border-radius: 10;");
            Label etiquetaArticulos = new Label("ARTÍCULOS");
            etiquetaArticulos.setStyle("-fx-text-fill: #f5c242; -fx-font-size: 10px;"
                    + "-fx-font-weight: bold;");
            articulos.getChildren().add(etiquetaArticulos);

            for (DetalleCompra d : compra.getDetalles()) {
                Label linea = new Label(d.getElemento().consultarDescripcion()
                        + "  × " + d.getCantidad() + "     " + moneda(d.getSubtotal()));
                linea.setWrapText(true);
                linea.setMaxWidth(Double.MAX_VALUE);
                linea.setStyle("-fx-text-fill: #e7eef7; -fx-font-size: 12px;");
                articulos.getChildren().add(linea);
            }

            VBox totales = new VBox(7);
            totales.setPadding(new Insets(13));
            totales.setStyle("-fx-background-color: #10243a; -fx-background-radius: 10;"
                    + "-fx-border-color: #294764; -fx-border-radius: 10;");
            Label subtotal = new Label("Subtotal:  " + moneda(compra.calcularSubtotal()));
            Label descuentos = new Label("Descuentos:  " + moneda(compra.getDescuentos()));
            Label puntos = new Label("Puntos redimidos:  " + moneda(compra.getValorPuntosRedimidos()));
            Label total = new Label("TOTAL PAGADO:  " + moneda(compra.getTotal()));
            for (Label dato : List.of(subtotal, descuentos, puntos)) {
                dato.setStyle("-fx-text-fill: #c4d3e0; -fx-font-size: 12px;");
            }
            total.setStyle("-fx-text-fill: #f5c242; -fx-font-size: 15px; -fx-font-weight: bold;");
            totales.getChildren().addAll(subtotal, descuentos, puntos, new Separator(), total);

            contenido.getChildren().addAll(titulo, codigo, articulos, totales);
            dialogo.getDialogPane().setContent(contenido);
            dialogo.showAndWait();
        });

        cancelar.setOnAction(e -> {
            Compra compra = tabla.getSelectionModel().getSelectedItem();
            if (compra == null) {
                aviso("Selecciona una compra.");
                return;
            }
            if (!"PAGADA".equals(compra.consultarNombreEstado())) {
                aviso("Solo se pueden cancelar compras pagadas.");
                return;
            }

            Optional<String> motivo = solicitarTextoCinemaUQ(
                    "Solicitud de cancelación",
                    "Compra " + compra.getCodigo(),
                    "Cuéntanos el motivo de la cancelación.",
                    "No podré asistir a la función",
                    "Escribe tu motivo"
            );
            motivo.ifPresent(texto -> {
                try {
                    Cancelacion solicitud = operaciones.solicitarCancelacion(compra, texto);
                    if (solicitud.getEstado() == EstadoCancelacion.APROBADA) {
                        Optional<Reembolso> reembolso =
                                operaciones.procesarReembolso(solicitud, cliente);
                        aviso(reembolso.map(r -> "Cancelación aprobada.\nReembolso: "
                                        + moneda(r.getValor()))
                                .orElse("Cancelación aprobada sin movimiento de dinero."));
                    } else {
                        aviso("La política no permite la cancelación solicitada.");
                    }
                    cargar.run();
                } catch (RuntimeException ex) {
                    mostrarError(ex);
                }
            });
        });

        actualizar.setOnAction(e -> {
            cargar.run();
            aviso("Tu historial de compras se ha actualizado.");
        });

        HBox acciones = barra(detalle, cancelar, actualizar);
        VBox tablaContenedor = new VBox(10);
        tablaContenedor.setPadding(new Insets(13));
        tablaContenedor.setStyle("-fx-background-color: #0b1d31; -fx-background-radius: 14;"
                + "-fx-border-color: #294764; -fx-border-radius: 14;");
        Label tituloHistorial = new Label("Historial de transacciones");
        tituloHistorial.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label ayuda = new Label("Selecciona una compra para ver su detalle o solicitar una cancelación.");
        ayuda.setStyle("-fx-text-fill: #9fb3c9; -fx-font-size: 11px;");
        tablaContenedor.getChildren().addAll(tituloHistorial, ayuda, tabla);

        root.getChildren().addAll(resumen, acciones, tablaContenedor);
        return root;
    }

    private Node vistaTarjeta() {
        VBox root = pagina("Mi tarjeta virtual",
                "Administra tu saldo, realiza recargas y consulta cada movimiento de tu tarjeta.");
        root.setSpacing(18);

        Optional<TarjetaVirtual> opcional = operaciones.consultarTarjeta(cliente);
        if (opcional.isEmpty()) {
            root.getChildren().add(estadoVacio(
                    "No tienes tarjeta virtual",
                    "Contacta al administrador para verificar tu cuenta."));
            return root;
        }

        TarjetaVirtual tarjeta = opcional.get();

        // Tarjeta digital principal.
        VBox tarjetaVisual = new VBox(15);
        tarjetaVisual.getStyleClass().add("interactive-card");
        tarjetaVisual.setPadding(new Insets(22, 25, 22, 25));
        tarjetaVisual.setMinHeight(205);
        tarjetaVisual.setMaxWidth(Double.MAX_VALUE);
        tarjetaVisual.setStyle(
                "-fx-background-color: linear-gradient(to right, #10243d 0%, #142b46 52%, #49351c 100%);"
                        + "-fx-background-radius: 18; -fx-border-color: #d9a928;"
                        + "-fx-border-width: 1.2; -fx-border-radius: 18;");

        HBox marcaFila = new HBox(10);
        marcaFila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label marcaIcono = new Label("▣");
        marcaIcono.setStyle("-fx-text-fill: #ffd04a; -fx-font-size: 25px; -fx-font-weight: bold;");
        Label marca = new Label("CINEMA UQ");
        marca.setStyle("-fx-text-fill: #ffd04a; -fx-font-size: 17px; -fx-font-weight: bold;");
        Region espacioMarca = new Region();
        HBox.setHgrow(espacioMarca, Priority.ALWAYS);
        Label tipoTarjeta = new Label("TARJETA DIGITAL");
        tipoTarjeta.setStyle("-fx-text-fill: #d5dfeb; -fx-font-size: 10px; -fx-font-weight: bold;"
                + "-fx-background-color: #ffffff18; -fx-background-radius: 12; -fx-padding: 6 10;");
        marcaFila.getChildren().addAll(marcaIcono, marca, espacioMarca, tipoTarjeta);

        Label numero = new Label(mascara(tarjeta.getNumero()));
        numero.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;"
                + "-fx-padding: 7 0 2 0;");

        HBox datosFila = new HBox(20);
        datosFila.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        VBox titularBox = new VBox(5);
        Label titularEtiqueta = new Label("TITULAR");
        titularEtiqueta.setStyle("-fx-text-fill: #aebfd1; -fx-font-size: 9px; -fx-font-weight: bold;");
        Label titular = new Label(cliente.getNombre());
        titular.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        titularBox.getChildren().addAll(titularEtiqueta, titular);

        Region espacioDatos = new Region();
        HBox.setHgrow(espacioDatos, Priority.ALWAYS);
        VBox saldoBox = new VBox(4);
        saldoBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label saldoEtiqueta = new Label("SALDO DISPONIBLE");
        saldoEtiqueta.setStyle("-fx-text-fill: #f1d99a; -fx-font-size: 9px; -fx-font-weight: bold;");
        Label saldo = new Label(moneda(tarjeta.getSaldo()));
        saldo.setStyle("-fx-text-fill: #ffd04a; -fx-font-size: 23px; -fx-font-weight: bold;");
        saldoBox.getChildren().addAll(saldoEtiqueta, saldo);

        HBox pieTarjeta = new HBox(8);
        pieTarjeta.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label estadoPunto = new Label("●");
        estadoPunto.setStyle("-fx-text-fill: #70e0ae; -fx-font-size: 11px;");
        Label estado = new Label("TARJETA " + tarjeta.getEstado().name());
        estado.setStyle("-fx-text-fill: #dce8f3; -fx-font-size: 10px; -fx-font-weight: bold;");
        pieTarjeta.getChildren().addAll(estadoPunto, estado);

        tarjetaVisual.getChildren().addAll(marcaFila, numero, datosFila, pieTarjeta);
        datosFila.getChildren().addAll(titularBox, espacioDatos, saldoBox);

        // Indicadores rápidos.
        int cantidadMovimientos = tarjeta.getMovimientos().size();
        HBox indicadores = new HBox(12);
        indicadores.setFillHeight(true);

        VBox indicadorSaldo = new VBox(6);
        indicadorSaldo.setPadding(new Insets(14, 17, 14, 17));
        indicadorSaldo.setStyle("-fx-background-color: #0d2138; -fx-background-radius: 13;"
                + "-fx-border-color: #29445f; -fx-border-radius: 13;");
        Label saldoTitulo = new Label("SALDO ACTUAL");
        saldoTitulo.setStyle("-fx-text-fill: #9eb2c8; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label saldoIndicador = new Label(moneda(tarjeta.getSaldo()));
        saldoIndicador.setStyle("-fx-text-fill: #ffd04a; -fx-font-size: 17px; -fx-font-weight: bold;");
        indicadorSaldo.getChildren().addAll(saldoTitulo, saldoIndicador);

        VBox indicadorMovimientos = new VBox(6);
        indicadorMovimientos.setPadding(new Insets(14, 17, 14, 17));
        indicadorMovimientos.setStyle("-fx-background-color: #0d2138; -fx-background-radius: 13;"
                + "-fx-border-color: #29445f; -fx-border-radius: 13;");
        Label movimientosTitulo = new Label("MOVIMIENTOS");
        movimientosTitulo.setStyle("-fx-text-fill: #9eb2c8; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label movimientosValor = new Label(String.valueOf(cantidadMovimientos));
        movimientosValor.setStyle("-fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;");
        indicadorMovimientos.getChildren().addAll(movimientosTitulo, movimientosValor);

        VBox indicadorEstado = new VBox(6);
        indicadorEstado.setPadding(new Insets(14, 17, 14, 17));
        indicadorEstado.setStyle("-fx-background-color: #0d2138; -fx-background-radius: 13;"
                + "-fx-border-color: #29445f; -fx-border-radius: 13;");
        Label estadoTitulo = new Label("ESTADO");
        estadoTitulo.setStyle("-fx-text-fill: #9eb2c8; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label estadoValor = new Label(tarjeta.getEstado().name());
        estadoValor.setStyle("-fx-text-fill: #75d6ae; -fx-font-size: 17px; -fx-font-weight: bold;");
        indicadorEstado.getChildren().addAll(estadoTitulo, estadoValor);

        for (VBox indicador : List.of(indicadorSaldo, indicadorMovimientos, indicadorEstado)) {
            HBox.setHgrow(indicador, Priority.ALWAYS);
            indicador.setMaxWidth(Double.MAX_VALUE);
        }
        indicadores.getChildren().addAll(indicadorSaldo, indicadorMovimientos, indicadorEstado);

        // Panel de recargas.
        VBox panelRecarga = new VBox(11);
        panelRecarga.setPadding(new Insets(16, 18, 16, 18));
        panelRecarga.setStyle("-fx-background-color: #0d2035; -fx-background-radius: 14;"
                + "-fx-border-color: #29445f; -fx-border-radius: 14;");
        HBox encabezadoRecarga = new HBox(8);
        encabezadoRecarga.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label iconoRecarga = new Label("＋");
        iconoRecarga.setStyle("-fx-text-fill: #ffd04a; -fx-font-size: 19px; -fx-font-weight: bold;");
        Label tituloRecarga = new Label("Agregar saldo");
        tituloRecarga.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        encabezadoRecarga.getChildren().addAll(iconoRecarga, tituloRecarga);

        Label descripcionRecarga = new Label("Selecciona un monto para recargar tu tarjeta virtual.");
        descripcionRecarga.setStyle("-fx-text-fill: #aebfd1; -fx-font-size: 11px;");

        // Historial de movimientos.
        Label movimientosLabel = new Label("Historial de movimientos");
        movimientosLabel.getStyleClass().add("section-title");
        Label movimientosSubtitulo = new Label("Consulta las recargas, pagos y reembolsos realizados.");
        movimientosSubtitulo.getStyleClass().add("muted-small");

        TableView<MovimientoTarjeta> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Fecha", m -> m.getFecha().toString().replace('T', ' ')),
                columna("Tipo", m -> m.getTipo().name()),
                columna("Valor", m -> {
                    boolean esPago = m.getTipo().name().equalsIgnoreCase("PAGO");
                    String signo = esPago ? "- " : "+ ";
                    return signo + moneda(m.getValor().abs());
                }),
                columna("Saldo", m -> moneda(m.getSaldoResultante())),
                columna("Referencia", MovimientoTarjeta::getReferenciaOperacion)
        );
        refrescar(tabla, tarjeta.getMovimientos());

        // Recargas de demostración: se conserva el flujo de operaciones existente.
        java.util.function.Consumer<BigDecimal> ejecutarRecarga = valor -> {
            try {
                operaciones.recargarTarjetaCliente(cliente, valor);
                TarjetaVirtual actualizada = operaciones.consultarTarjeta(cliente).orElseThrow();
                saldo.setText(moneda(actualizada.getSaldo()));
                saldoIndicador.setText(moneda(actualizada.getSaldo()));
                movimientosValor.setText(String.valueOf(actualizada.getMovimientos().size()));
                refrescar(tabla, actualizada.getMovimientos());
                alActualizarResumen.run();
                mostrarConfirmacionRecarga(valor, actualizada);
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        };

        Button recarga10 = boton("+ $10.000", "button-small-outline");
        recarga10.setOnAction(e -> ejecutarRecarga.accept(new BigDecimal("10000")));
        Button recarga20 = boton("+ $20.000", "button-small-outline");
        recarga20.setOnAction(e -> ejecutarRecarga.accept(new BigDecimal("20000")));
        Button recarga50 = boton("+ $50.000", "button-gold");
        recarga50.setOnAction(e -> ejecutarRecarga.accept(new BigDecimal("50000")));
        Button otraRecarga = boton("Otro valor", "button-small-outline");
        otraRecarga.setOnAction(e -> {
            Optional<String> entrada = solicitarTextoCinemaUQ(
                    "Recargar tarjeta virtual",
                    "Ingresa el valor que deseas agregar.",
                    "Valor en pesos colombianos (COP):",
                    "30000",
                    "Ej. 25000"
            );
            entrada.ifPresent(valorTexto -> {
                try {
                    String limpio = valorTexto.replace("$", "").replace(".", "")
                            .replace(",", "").trim();
                    ejecutarRecarga.accept(new BigDecimal(limpio));
                } catch (NumberFormatException ex) {
                    aviso("Escribe un valor válido. Ejemplo: 25000");
                }
            });
        });

        HBox accionesRecarga = barra(recarga10, recarga20, recarga50, otraRecarga);
        Label notaRecarga = new Label(
                "ⓘ  Recargas de demostración: no se realiza un cobro bancario real.");
        notaRecarga.getStyleClass().add("muted-small");

        panelRecarga.getChildren().addAll(encabezadoRecarga, descripcionRecarga,
                accionesRecarga, notaRecarga);

        VBox panelHistorial = new VBox(9);
        panelHistorial.setPadding(new Insets(15));
        panelHistorial.setStyle("-fx-background-color: #0b1e33; -fx-background-radius: 14;"
                + "-fx-border-color: #29445f; -fx-border-radius: 14;");
        panelHistorial.getChildren().addAll(movimientosLabel, movimientosSubtitulo, tabla);
        VBox.setVgrow(tabla, Priority.ALWAYS);

        root.getChildren().addAll(tarjetaVisual, indicadores, panelRecarga, panelHistorial);
        return root;
    }

    /**
     * Catálogo de confitería y carrito de compra.
     * El pago se realiza con la tarjeta virtual del cliente.
     */
    /**
     * Confitería con catálogo visual, combos destacados y carrito lateral.
     * Conserva el flujo de compra y pago existente del dominio.
     */
    private Node vistaConfiteria() {
        VBox root = pagina("Confitería CinemaUQ",
                "Disfruta de nuestros snacks favoritos y aprovecha nuestros combos especiales.");
        root.setSpacing(16);
        root.setStyle("-fx-background-color: #061426;");

        List<ElementoConfiteria> productos = new ArrayList<>();
        operaciones.consultarProductos().stream()
                .filter(Producto::estaActivo)
                .forEach(productos::add);
        List<ElementoConfiteria> combos = new ArrayList<>(
                operaciones.consultarCombosActivos());

        if (productos.isEmpty() && combos.isEmpty()) {
            root.getChildren().add(estadoVacio("Confitería no disponible",
                    "Todavía no hay productos ni combos registrados."));
            return root;
        }

        Map<ElementoConfiteria, Integer> carrito = new LinkedHashMap<>();

        // Banner superior inspirado en la referencia visual de CinemaUQ.
        HBox banner = new HBox(18);
        banner.getStyleClass().add("interactive-card");
        banner.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        banner.setPadding(new Insets(20, 24, 20, 24));
        banner.setMinHeight(118);
        banner.setStyle("-fx-background-color: linear-gradient(to right, #101d39, #29162a, #10243b);"
                + "-fx-background-radius: 16; -fx-border-color: #344b6a;"
                + "-fx-border-radius: 16;");
        Label bannerIcon = new Label("🍿");
        bannerIcon.setStyle("-fx-font-size: 48px;");
        VBox bannerTexto = new VBox(5);
        Label bannerTitulo = new Label("Confitería");
        bannerTitulo.setStyle("-fx-font-size: 27px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label bannerSubtitulo = new Label(
                "Disfruta nuestra variedad de snacks, bebidas y combos especiales.");
        bannerSubtitulo.setStyle("-fx-font-size: 13px; -fx-text-fill: #d5dfec;");
        bannerTexto.getChildren().addAll(bannerTitulo, bannerSubtitulo);
        banner.getChildren().addAll(bannerIcon, bannerTexto);

        // Barra de navegación y búsqueda.
        ToggleButton tabProductos = new ToggleButton("▦  Productos");
        ToggleButton tabCombos = new ToggleButton("▱  Combos");
        ToggleGroup grupo = new ToggleGroup();
        tabProductos.setToggleGroup(grupo);
        tabCombos.setToggleGroup(grupo);
        tabProductos.setSelected(true);
        String estiloTab = "-fx-background-color: #142a43; -fx-text-fill: #eaf1f8;"
                + "-fx-font-weight: bold; -fx-background-radius: 10;"
                + "-fx-border-color: #34516f; -fx-border-radius: 10; -fx-padding: 10 18;";
        String estiloTabActivo = "-fx-background-color: #f5b91b; -fx-text-fill: #111827;"
                + "-fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 10 18;";
        tabProductos.setStyle(estiloTabActivo);
        tabCombos.setStyle(estiloTab);

        TextField buscar = new TextField();
        buscar.setPromptText("⌕  Buscar en confitería...");
        buscar.setPrefWidth(260);
        buscar.setStyle("-fx-background-color: #10243a; -fx-text-fill: white;"
                + "-fx-prompt-text-fill: #9aabc0; -fx-background-radius: 18;"
                + "-fx-border-color: #304a68; -fx-border-radius: 18; -fx-padding: 10 15;");
        HBox barraCatalogo = new HBox(10, tabProductos, tabCombos);
        barraCatalogo.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Region separador = new Region();
        HBox.setHgrow(separador, Priority.ALWAYS);
        HBox barraSuperior = new HBox(12, barraCatalogo, separador, buscar);
        barraSuperior.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Catálogo desplazable a la izquierda.
        TilePane mosaico = new TilePane();
        mosaico.setHgap(13);
        mosaico.setVgap(13);
        mosaico.setPrefColumns(3);
        mosaico.setTileAlignment(javafx.geometry.Pos.TOP_LEFT);
        mosaico.setPrefTileWidth(220);
        mosaico.setPrefTileHeight(278);

        Label tituloCatalogo = new Label("★  Productos");
        tituloCatalogo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        ScrollPane desplazamiento = new ScrollPane(mosaico);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);

        // Carrito lateral fijo.
        VBox panelCarrito = new VBox(12);
        panelCarrito.getStyleClass().add("interactive-card");
        panelCarrito.setPadding(new Insets(17));
        panelCarrito.setPrefWidth(330);
        panelCarrito.setMinWidth(310);
        panelCarrito.setStyle("-fx-background-color: linear-gradient(to bottom, #0d2138, #09192b);"
                + "-fx-background-radius: 16; -fx-border-color: #304a68;"
                + "-fx-border-radius: 16;");
        HBox encabezadoCarrito = new HBox(9);
        encabezadoCarrito.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label iconoCarrito = new Label("🛒");
        iconoCarrito.setStyle("-fx-font-size: 22px;");
        Label textoCarrito = new Label("Tu carrito");
        textoCarrito.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: white;");
        encabezadoCarrito.getChildren().addAll(iconoCarrito, textoCarrito);
        ListView<String> listaCarrito = new ListView<>();
        listaCarrito.setPrefHeight(330);
        listaCarrito.setStyle("-fx-background-color: transparent; -fx-control-inner-background: #0b1e33;"
                + "-fx-background-radius: 10; -fx-border-color: #243e5a; -fx-border-radius: 10;");
        Label subtotal = new Label("Subtotal                                      $0");
        subtotal.setMaxWidth(Double.MAX_VALUE);
        subtotal.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #f7c52b;");
        Label saldoTarjeta = new Label();
        operaciones.consultarTarjeta(cliente).ifPresentOrElse(
                tarjeta -> saldoTarjeta.setText("Saldo disponible: " + moneda(tarjeta.getSaldo())),
                () -> saldoTarjeta.setText("Tarjeta virtual no disponible"));
        saldoTarjeta.setStyle("-fx-text-fill: #a9bdd2; -fx-font-size: 11px;");
        Button quitar = boton("Quitar seleccionado", "button-small-outline");
        Button vaciar = boton("Vaciar carrito", "button-small-outline");
        Button pagar = boton("▣  Pagar con tarjeta virtual", "button-gold");
        pagar.setMaxWidth(Double.MAX_VALUE);

        Runnable refrescarCarrito = () -> {
            listaCarrito.getItems().clear();
            BigDecimal total = BigDecimal.ZERO;
            for (Map.Entry<ElementoConfiteria, Integer> item : carrito.entrySet()) {
                BigDecimal linea = item.getKey().consultarPrecio()
                        .multiply(BigDecimal.valueOf(item.getValue()));
                total = total.add(linea);
                listaCarrito.getItems().add(nombreElementoConfiteria(item.getKey())
                        + "  × " + item.getValue() + "    " + moneda(linea));
            }
            subtotal.setText("Subtotal                              " + moneda(total));
            pagar.setDisable(carrito.isEmpty());
            textoCarrito.setText("Tu carrito (" + carrito.values().stream()
                    .mapToInt(Integer::intValue).sum() + ")");
        };

        quitar.setOnAction(e -> {
            int indice = listaCarrito.getSelectionModel().getSelectedIndex();
            if (indice < 0 || indice >= carrito.size()) {
                aviso("Selecciona un producto del carrito.");
                return;
            }
            ElementoConfiteria elemento = new ArrayList<>(carrito.keySet()).get(indice);
            carrito.remove(elemento);
            refrescarCarrito.run();
        });
        vaciar.setOnAction(e -> {
            carrito.clear();
            refrescarCarrito.run();
        });

        pagar.setOnAction(event -> {
            if (carrito.isEmpty()) {
                aviso("Agrega productos o combos al carrito.");
                return;
            }
            try {
                Compra compra = operaciones.comprarConfiteria(
                        cliente, new LinkedHashMap<>(carrito), "", 0);
                carrito.clear();
                refrescarCarrito.run();
                operaciones.consultarTarjeta(cliente).ifPresent(tarjeta ->
                        saldoTarjeta.setText("Saldo disponible: " + moneda(tarjeta.getSaldo())));
                alActualizarResumen.run();
                mostrarConfirmacionPedido(compra);
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });

        panelCarrito.getChildren().addAll(encabezadoCarrito, listaCarrito,
                quitar, vaciar, new Separator(), subtotal, saldoTarjeta, pagar);

        // Renderizado de tarjetas. Se muestran imágenes tipo emoji hasta que
        // el proyecto incorpore fotografías propias de cada producto.
        Runnable renderizarCatalogo = () -> {
            mosaico.getChildren().clear();
            boolean verCombos = tabCombos.isSelected();
            tituloCatalogo.setText(verCombos ? "★  Combos especiales" : "★  Productos");
            List<ElementoConfiteria> fuente = verCombos ? combos : productos;
            String filtro = buscar.getText() == null
                    ? "" : buscar.getText().trim().toLowerCase(Locale.ROOT);

            for (ElementoConfiteria elemento : fuente) {
                String nombre = nombreElementoConfiteria(elemento);
                if (!nombre.toLowerCase(Locale.ROOT).contains(filtro)
                        && !elemento.consultarDescripcion().toLowerCase(Locale.ROOT).contains(filtro)) {
                    continue;
                }

                String archivoImagen = archivoImagenConfiteria(elemento);

                VBox tarjeta = new VBox(9);
                tarjeta.getStyleClass().add("interactive-card");
                tarjeta.setPadding(new Insets(12));
                tarjeta.setPrefWidth(220);
                tarjeta.setMinWidth(220);
                tarjeta.setMaxWidth(220);
                tarjeta.setStyle("-fx-background-color: linear-gradient(to bottom, #112b45, #091a2d);"
                        + "-fx-background-radius: 15; -fx-border-color: #31516f;"
                        + "-fx-border-radius: 15;");
                StackPane imagen = new StackPane();
                imagen.setMinHeight(130);
                imagen.setPrefHeight(130);
                imagen.setStyle("-fx-background-color: linear-gradient(to bottom right, #172d45, #071525);"
                        + "-fx-background-radius: 10; -fx-border-color: #294764;"
                        + "-fx-border-radius: 10;");
                ImageView fotografia = crearImagenConfiteria(archivoImagen);
                imagen.getChildren().add(fotografia);

                Label nombreLabel = new Label(nombre);
                nombreLabel.setWrapText(true);
                nombreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #f5f7fb;");
                Label descripcion = new Label(elemento.consultarDescripcion());
                descripcion.setWrapText(true);
                descripcion.setMaxHeight(32);
                descripcion.setStyle("-fx-font-size: 11px; -fx-text-fill: #b7c7d9;");
                Label precio = new Label(moneda(elemento.consultarPrecio()));
                precio.setStyle("-fx-font-size: 19px; -fx-font-weight: bold; -fx-text-fill: #ffc52e;");
                Button agregar = boton("🛒  Agregar al carrito", "button-gold");
                agregar.setMaxWidth(Double.MAX_VALUE);
                agregar.setPrefHeight(36);
                agregar.setOnAction(e -> {
                    carrito.merge(elemento, 1, Integer::sum);
                    refrescarCarrito.run();
                });
                tarjeta.getChildren().addAll(imagen, nombreLabel, descripcion, precio, agregar);
                mosaico.getChildren().add(tarjeta);
            }
        };

        tabProductos.setOnAction(e -> {
            tabProductos.setStyle(estiloTabActivo);
            tabCombos.setStyle(estiloTab);
            renderizarCatalogo.run();
        });
        tabCombos.setOnAction(e -> {
            tabCombos.setStyle(estiloTabActivo);
            tabProductos.setStyle(estiloTab);
            renderizarCatalogo.run();
        });
        buscar.textProperty().addListener((obs, anterior, nuevo) -> renderizarCatalogo.run());

        HBox zonaCentral = new HBox(15, new VBox(10, tituloCatalogo, desplazamiento), panelCarrito);
        HBox.setHgrow(zonaCentral.getChildren().get(0), Priority.ALWAYS);
        VBox.setVgrow(zonaCentral, Priority.ALWAYS);
        root.getChildren().addAll(banner, barraSuperior, zonaCentral);
        VBox.setVgrow(zonaCentral, Priority.ALWAYS);
        renderizarCatalogo.run();
        refrescarCarrito.run();
        return root;
    }

    /**
     * Devuelve el recurso PNG correspondiente al producto o combo.
     * Los nombres de archivo se mantienen independientes del texto visible.
     */
    /**
     * Muestra la confirmación de la compra y simula el envío del comprobante
     * al correo registrado por el cliente. No realiza un envío real.
     */
    private void mostrarConfirmacionPedido(Compra compra) {
        Dialog<Void> dialogo = new Dialog<>();
        configurarDialogoCinemaUQ(dialogo);
        if (owner != null) {
            dialogo.initOwner(owner);
        }
        dialogo.setTitle("CinemaUQ · Pedido confirmado");
        dialogo.setHeaderText(null);

        DialogPane panel = dialogo.getDialogPane();
        panel.getButtonTypes().add(ButtonType.OK);
        panel.setStyle("-fx-background-color: #081a2d;");

        VBox contenido = new VBox(13);
        contenido.setPadding(new Insets(22));
        contenido.setAlignment(javafx.geometry.Pos.CENTER);
        contenido.setPrefWidth(410);
        contenido.setStyle("-fx-background-color: #081a2d;");

        Label icono = new Label("✓");
        icono.setMinSize(62, 62);
        icono.setAlignment(javafx.geometry.Pos.CENTER);
        icono.setStyle("-fx-background-color: #174d42; -fx-background-radius: 40;"
                + "-fx-text-fill: #75e0b5; -fx-font-size: 34px; -fx-font-weight: bold;");

        Label titulo = new Label("¡Tu pedido está confirmado!");
        titulo.setStyle("-fx-text-fill: white; -fx-font-size: 21px; -fx-font-weight: bold;");

        Label subtitulo = new Label("Hemos preparado tu comprobante de compra.");
        subtitulo.setStyle("-fx-text-fill: #b7c7d9; -fx-font-size: 12px;");
        subtitulo.setWrapText(true);

        VBox correoBox = new VBox(5);
        correoBox.setPadding(new Insets(13));
        correoBox.setMaxWidth(Double.MAX_VALUE);
        correoBox.setStyle("-fx-background-color: #102943; -fx-background-radius: 10;"
                + "-fx-border-color: #294764; -fx-border-radius: 10;");

        Label estadoCorreo = new Label("✉  ENVÍO DE CORREO SIMULADO");
        estadoCorreo.setStyle("-fx-text-fill: #f7c52b; -fx-font-size: 11px;"
                + "-fx-font-weight: bold;");
        Label correoDestino = new Label("Comprobante enviado a:");
        correoDestino.setStyle("-fx-text-fill: #9fb3c9; -fx-font-size: 11px;");
        Label correo = new Label(cliente.getCorreo());
        correo.setWrapText(true);
        correo.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
        correoBox.getChildren().addAll(estadoCorreo, correoDestino, correo);

        VBox codigoBox = new VBox(7);
        codigoBox.setPadding(new Insets(15));
        codigoBox.setAlignment(javafx.geometry.Pos.CENTER);
        codigoBox.setMaxWidth(Double.MAX_VALUE);
        codigoBox.setStyle("-fx-background-color: #10243a; -fx-background-radius: 10;"
                + "-fx-border-color: #f0b51b; -fx-border-width: 1.2;"
                + "-fx-border-radius: 10;");

        Label etiquetaCodigo = new Label("CÓDIGO PARA RECLAMAR TU PEDIDO");
        etiquetaCodigo.setStyle("-fx-text-fill: #aebed0; -fx-font-size: 10px;"
                + "-fx-font-weight: bold;");
        Label codigo = new Label(compra.getCodigo());
        codigo.setWrapText(true);
        codigo.setStyle("-fx-text-fill: #ffd04a; -fx-font-size: 19px;"
                + "-fx-font-weight: bold;");

        Label total = new Label("Total pagado: " + moneda(compra.getTotal()));
        total.setStyle("-fx-text-fill: white; -fx-font-size: 13px;"
                + "-fx-font-weight: bold;");

        Label instrucciones = new Label(
                "Presenta este código en la confitería para reclamar tus productos. "
                        + "El personal podrá verificar el pedido en el sistema.");
        instrucciones.setWrapText(true);
        instrucciones.setMaxWidth(360);
        instrucciones.setStyle("-fx-text-fill: #aebed0; -fx-font-size: 11px;"
                + "-fx-text-alignment: center;");

        Label nota = new Label("Tu saldo y el inventario se actualizaron correctamente.");
        nota.setWrapText(true);
        nota.setStyle("-fx-text-fill: #75d6ae; -fx-font-size: 10px;");

        // El código y el total se agrupan en una tarjeta destacada.
        codigoBox.getChildren().addAll(etiquetaCodigo, codigo, total);
        contenido.getChildren().addAll(icono, titulo, subtitulo, correoBox,
                codigoBox, instrucciones, nota);

        panel.setContent(contenido);
        panel.lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color: #f5b91b; -fx-text-fill: #101827;"
                        + "-fx-font-weight: bold; -fx-background-radius: 8;"
                        + "-fx-padding: 8 25;");
        dialogo.showAndWait();
    }

    private String archivoImagenConfiteria(ElementoConfiteria elemento) {
        String nombre = nombreElementoConfiteria(elemento).toLowerCase(Locale.ROOT);

        if (elemento instanceof Combo) {
            if (nombre.contains("pareja")) return "combo_pareja.png";
            if (nombre.contains("familiar")) return "combo_familiar.png";
            if (nombre.contains("nacho")) return "combo_nachos.png";
            return "combo_clasico.png";
        }

        if (nombre.contains("palom") && nombre.contains("grande")) {
            return "palomitas_grandes.png";
        }
        if (nombre.contains("palom")) return "palomitas_medianas.png";
        if (nombre.contains("gaseosa") && nombre.contains("grande")) {
            return "gaseosa_grande.png";
        }
        if (nombre.contains("gaseosa")) return "gaseosa_mediana.png";
        if (nombre.contains("perro")) return "perro_caliente.png";
        if (nombre.contains("nacho")) return "nachos_con_queso.png";
        if (nombre.contains("agua")) return "agua.png";
        if (nombre.contains("chocol")) return "chocolatina.png";

        return "palomitas_grandes.png";
    }

    /**
     * Carga la imagen desde resources y ajusta su tamaño sin deformarla.
     * Si falta el archivo, muestra un aviso visual en vez de romper la pantalla.
     */
    private ImageView crearImagenConfiteria(String nombreArchivo) {
        String ruta = "/co/edu/uniquindio/proyectofinal/images/confiteria/" + nombreArchivo;
        var recurso = getClass().getResource(ruta);

        ImageView vista = new ImageView();
        vista.setFitWidth(196);
        vista.setFitHeight(118);
        vista.setPreserveRatio(false);
        vista.setSmooth(true);

        if (recurso != null) {
            vista.setImage(new Image(recurso.toExternalForm(), true));
        } else {
            Label avisoImagen = new Label("Imagen no encontrada");
            avisoImagen.setStyle("-fx-text-fill: #9aabc0; -fx-font-size: 10px;");
            // El nodo alternativo se añade en el contenedor de la tarjeta.
            // Se conserva ImageView vacío para que el resto de la tarjeta funcione.
        }
        return vista;
    }

    private String nombreElementoConfiteria(ElementoConfiteria elemento) {
        if (elemento instanceof Producto producto) return producto.getNombre();
        if (elemento instanceof Combo combo) return combo.getNombre();
        return elemento.consultarDescripcion();
    }

    private Node vistaListaEspera() {
        VBox root = pagina("Mi lista de espera",
                "Consulta y cancela tus solicitudes para funciones sin disponibilidad inmediata.");
        TableView<SolicitudEspera> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", SolicitudEspera::getCodigo),
                columna("Asientos", s -> String.valueOf(s.getCantidadAsientos())),
                columna("Estado", s -> s.getEstado().name()),
                columna("Vence oferta", s -> s.getFechaExpiracionOferta() == null ? "—" : s.getFechaExpiracionOferta().toString())
        );
        Runnable cargar = () -> refrescar(tabla, operaciones.consultarSolicitudesEspera(cliente));
        Button cancelar = boton("Cancelar solicitud", "button-small-outline");
        cancelar.setOnAction(event -> {
            SolicitudEspera solicitud = tabla.getSelectionModel().getSelectedItem();
            if (solicitud == null) { aviso("Selecciona una solicitud."); return; }
            try {
                operaciones.cancelarEsperaCliente(solicitud, cliente);
                cargar.run();
                aviso("Solicitud cancelada.");
            } catch (RuntimeException ex) { mostrarError(ex); }
        });
        Button actualizar = boton("Actualizar", "button-small-outline");
        actualizar.setOnAction(event -> cargar.run());
        cargar.run();
        root.getChildren().addAll(barra(cancelar, actualizar), tabla);
        return root;
    }

    private Node vistaPuntos() {
        VBox root = pagina("Mis puntos",
                "Acumula beneficios con tus compras y disfruta de recompensas en CinemaUQ.");
        root.setSpacing(16);

        int saldo = operaciones.consultarPuntos(cliente);
        List<MovimientoPuntos> movimientos =
                new ArrayList<>(operaciones.consultarMovimientosPuntos(cliente));

        // Tarjeta principal de puntos.
        HBox hero = new HBox(18);
        hero.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        hero.setPadding(new Insets(22, 25, 22, 25));
        hero.setMinHeight(150);
        hero.setStyle("-fx-background-color: linear-gradient(to right, #102b49, #172b43, #49351c);"
                + "-fx-background-radius: 16; -fx-border-color: #d99e18;"
                + "-fx-border-radius: 16;");

        VBox heroTexto = new VBox(7);
        Label marca = new Label("★  CINEMA UQ  ·  CLUB DE BENEFICIOS");
        marca.setStyle("-fx-text-fill: #ffd04a; -fx-font-size: 11px; -fx-font-weight: bold;");
        Label heroTitulo = new Label("Tus puntos, tus beneficios");
        heroTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 21px; -fx-font-weight: bold;");
        Label heroDescripcion = new Label("Sigue disfrutando del cine mientras acumulas recompensas.");
        heroDescripcion.setStyle("-fx-text-fill: #c3d2e1; -fx-font-size: 11px;");
        heroDescripcion.setWrapText(true);
        heroTexto.getChildren().addAll(marca, heroTitulo, heroDescripcion);

        Region espacioHero = new Region();
        HBox.setHgrow(espacioHero, Priority.ALWAYS);

        VBox puntosHero = new VBox(3);
        puntosHero.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        Label etiquetaPuntos = new Label("PUNTOS DISPONIBLES");
        etiquetaPuntos.setStyle("-fx-text-fill: #e7d5a4; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label numeroHero = new Label(String.format(Locale.ROOT, "%,d", saldo));
        numeroHero.setStyle("-fx-text-fill: #ffd04a; -fx-font-size: 38px; -fx-font-weight: bold;");
        Label unidadHero = new Label("puntos");
        unidadHero.setStyle("-fx-text-fill: #f5e7c5; -fx-font-size: 12px;");
        puntosHero.getChildren().addAll(etiquetaPuntos, numeroHero, unidadHero);
        hero.getChildren().addAll(heroTexto, espacioHero, puntosHero);

        // Indicadores secundarios.
        BigDecimal valorPuntos = BigDecimal.valueOf(saldo)
                .multiply(operaciones.consultarValorPunto());
        HBox indicadores = new HBox(10);
        indicadores.setAlignment(javafx.geometry.Pos.CENTER);
        indicadores.getChildren().addAll(
                crearIndicadorPuntos("SALDO DE PUNTOS",
                        String.format(Locale.ROOT, "%,d pts", saldo), "★", "#ffd04a"),
                crearIndicadorPuntos("VALOR EQUIVALENTE",
                        moneda(valorPuntos), "$", "#75d6ae"),
                crearIndicadorPuntos("MOVIMIENTOS REGISTRADOS",
                        String.valueOf(movimientos.size()), "↕", "#8fc8ff")
        );

        // Reglas del programa.
        VBox reglas = new VBox(9);
        reglas.setPadding(new Insets(16));
        reglas.setStyle("-fx-background-color: #0e2238; -fx-background-radius: 13;"
                + "-fx-border-color: #294760; -fx-border-radius: 13;");
        Label reglasTitulo = new Label("✦  ¿Cómo funciona tu programa de puntos?");
        reglasTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label reglaAcumulacion = new Label("•  Acumulas 100 puntos por cada $10.000 efectivamente pagados.");
        Label reglaValor = new Label("•  Cada 100 puntos equivalen a $1.000 de recompensa.");
        Label reglaRedencion = new Label("•  Puedes redimir puntos hasta por el 50 % del subtotal.");
        for (Label regla : List.of(reglaAcumulacion, reglaValor, reglaRedencion)) {
            regla.setStyle("-fx-text-fill: #b9cadd; -fx-font-size: 11px;");
            regla.setWrapText(true);
        }
        reglas.getChildren().addAll(reglasTitulo, reglaAcumulacion, reglaValor, reglaRedencion);

        // Historial de movimientos.
        VBox historial = new VBox(10);
        historial.setPadding(new Insets(14));
        historial.setStyle("-fx-background-color: #0b1e33; -fx-background-radius: 14;"
                + "-fx-border-color: #294760; -fx-border-radius: 14;");
        Label historialTitulo = new Label("Historial de puntos");
        historialTitulo.setStyle("-fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;");
        Label historialSubtitulo = new Label("Consulta las acumulaciones, redenciones y otros movimientos de tu cuenta.");
        historialSubtitulo.setStyle("-fx-text-fill: #9fb4c9; -fx-font-size: 10px;");
        historialSubtitulo.setWrapText(true);

        TableView<MovimientoPuntos> tabla = tabla();
        tabla.setPrefHeight(390);
        tabla.setPlaceholder(new Label("Todavía no tienes movimientos de puntos."));
        tabla.getColumns().addAll(
                columna("Fecha", m -> m.getFecha().toString().replace('T', ' ')),
                columna("Movimiento", m -> m.getTipo().name().replace('_', ' ')),
                columna("Cantidad", m -> {
                    String tipo = m.getTipo().name().toUpperCase(Locale.ROOT);
                    boolean salida = tipo.contains("REDENC") || tipo.contains("VENC");
                    String signo = salida ? "- " : "+ ";
                    return signo + Math.abs(m.getCantidad());
                }),
                columna("Descripción", MovimientoPuntos::getDescripcion),
                columna("Referencia", MovimientoPuntos::getCodigoReferencia)
        );
        refrescar(tabla, movimientos);

        historial.getChildren().addAll(historialTitulo, historialSubtitulo, tabla);
        root.getChildren().addAll(hero, indicadores, reglas, historial);
        return root;
    }

    private Node vistaPeliculasGuardadas() {
        VBox root = pagina("Películas guardadas",
                "Organiza las películas que te gustan y las que quieres ver más adelante.");

        ComboBox<TipoListaPelicula> tipo = new ComboBox<>(
                FXCollections.observableArrayList(TipoListaPelicula.values()));
        tipo.setValue(TipoListaPelicula.FAVORITA);
        configurarCombo(tipo, valor -> valor.name().replace('_', ' '));

        TableView<PeliculaGuardada> tabla = tabla();
        tabla.setFixedCellSize(76);

        // Columna visual: portada y título de la película.
        TableColumn<PeliculaGuardada, PeliculaGuardada> columnaPelicula =
                new TableColumn<>("Película");
        columnaPelicula.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(data.getValue()));
        columnaPelicula.setCellFactory(columna -> new TableCell<>() {
            @Override
            protected void updateItem(PeliculaGuardada guardada, boolean vacia) {
                super.updateItem(guardada, vacia);
                if (vacia || guardada == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Pelicula peliculaGuardada = guardada.getPelicula();
                HBox contenido = new HBox(12);
                contenido.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                ImageView portada = crearPortadaGuardada(peliculaGuardada);
                Label titulo = new Label(peliculaGuardada.getTitulo());
                titulo.setWrapText(true);
                titulo.setStyle("-fx-text-fill: #f5f7fb; -fx-font-size: 13px; "
                        + "-fx-font-weight: bold;");

                contenido.getChildren().addAll(portada, titulo);
                setGraphic(contenido);
                setText(null);
            }
        });
        columnaPelicula.setPrefWidth(280);
        columnaPelicula.setMinWidth(220);

        tabla.getColumns().addAll(
                columnaPelicula,
                columna("Género", p -> p.getPelicula().getGenero()),
                columna("Lista", p -> p.getTipoLista().name()),
                columna("Agregada", p -> p.getFechaAgregada().toString())
        );

        Runnable cargar = () -> refrescar(
                tabla, operaciones.consultarListaPersonal(cliente, tipo.getValue()));
        tipo.setOnAction(e -> cargar.run());

        ComboBox<Pelicula> pelicula = new ComboBox<>(
                FXCollections.observableArrayList(operaciones.consultarPeliculasActivas()));
        configurarCombo(pelicula, Pelicula::getTitulo);

        Button agregar = boton("＋ Guardar película", "button-gold");
        agregar.setOnAction(e -> {
            if (pelicula.getValue() == null) {
                aviso("Selecciona una película.");
                return;
            }
            try {
                operaciones.guardarPelicula(cliente, pelicula.getValue(), tipo.getValue());
                cargar.run();
                aviso("Película agregada a tu lista.");
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });

        Button eliminar = boton("Quitar de la lista", "button-small-outline");
        eliminar.setOnAction(e -> {
            PeliculaGuardada seleccionada = tabla.getSelectionModel().getSelectedItem();
            if (seleccionada == null) {
                aviso("Selecciona una película guardada.");
                return;
            }
            try {
                operaciones.quitarPeliculaGuardada(
                        cliente, seleccionada.getPelicula(), seleccionada.getTipoLista());
                cargar.run();
                aviso("La película se retiró de tu lista correctamente.");
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });

        cargar.run();
        root.getChildren().addAll(barra(tipo, pelicula, agregar, eliminar), tabla);
        return root;
    }

    /**
     * Carga la misma portada local utilizada en Cartelera.
     * Si no existe una imagen para el código, conserva un recuadro de respaldo.
     */
    private ImageView crearPortadaGuardada(Pelicula pelicula) {
        Map<String, String> portadas = Map.of(
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

        ImageView vista = new ImageView();
        vista.setFitWidth(43);
        vista.setFitHeight(62);
        vista.setPreserveRatio(false);
        vista.setSmooth(true);

        String archivo = portadas.get(pelicula.getCodigo());
        if (archivo != null) {
            String ruta = "/co/edu/uniquindio/proyectofinal/images/" + archivo;
            var recurso = getClass().getResource(ruta);
            if (recurso != null) {
                Image imagen = new Image(recurso.toExternalForm(), true);
                if (!imagen.isError()) {
                    vista.setImage(imagen);
                }
            }
        }
        return vista;
    }

    private Node vistaResenas() {
        VBox root = pagina("Reseñas",
                "Comparte tu experiencia y consulta las opiniones de otros espectadores.");

        // Selector de película: conserva las validaciones del servicio de operaciones.
        ComboBox<Pelicula> pelicula = new ComboBox<>(
                FXCollections.observableArrayList(operaciones.consultarPeliculas()));
        configurarCombo(pelicula, Pelicula::getTitulo);
        pelicula.setPromptText("Selecciona una película");
        pelicula.setPrefWidth(330);

        // Calificación con estrellas visuales.
        ComboBox<Integer> calificacion = new ComboBox<>(
                FXCollections.observableArrayList(1, 2, 3, 4, 5));
        calificacion.setValue(5);
        calificacion.setPrefWidth(105);
        configurarCombo(calificacion, valor -> "★".repeat(valor) + "  " + valor + "/5");

        Label promedio = new Label("Selecciona una película para consultar sus reseñas.");
        promedio.setStyle("-fx-text-fill: #a9bdd2; -fx-font-size: 12px;");

        TextArea comentario = new TextArea();
        comentario.setPromptText("Cuéntanos qué te pareció la película...");
        comentario.setWrapText(true);
        comentario.setPrefRowCount(4);
        comentario.setMinHeight(105);
        comentario.setStyle(
                "-fx-control-inner-background: #10243a;" +
                        "-fx-background-color: #10243a;" +
                        "-fx-text-fill: #f5f7fb;" +
                        "-fx-prompt-text-fill: #8fa6bd;" +
                        "-fx-highlight-fill: #f5b91b;" +
                        "-fx-highlight-text-fill: #101827;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #294765;" +
                        "-fx-border-radius: 10;" +
                        "-fx-font-size: 13px;");

        VBox formulario = new VBox(11);
        formulario.setPadding(new Insets(18));
        formulario.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #10243a, #0b1b2d);" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #294765;" +
                        "-fx-border-radius: 14;");

        Label tituloFormulario = new Label("✎  Escribe tu reseña");
        tituloFormulario.setStyle("-fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;");

        Label etiquetaPelicula = new Label("PELÍCULA");
        etiquetaPelicula.setStyle("-fx-text-fill: #a9bdd2; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label etiquetaCalificacion = new Label("TU CALIFICACIÓN");
        etiquetaCalificacion.setStyle("-fx-text-fill: #a9bdd2; -fx-font-size: 10px; -fx-font-weight: bold;");
        Label etiquetaComentario = new Label("COMENTARIO");
        etiquetaComentario.setStyle("-fx-text-fill: #a9bdd2; -fx-font-size: 10px; -fx-font-weight: bold;");

        VBox selectorPelicula = new VBox(5, etiquetaPelicula, pelicula);
        VBox selectorCalificacion = new VBox(5, etiquetaCalificacion, calificacion);
        HBox filaSelectores = new HBox(14, selectorPelicula, selectorCalificacion);
        filaSelectores.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        HBox.setHgrow(selectorPelicula, Priority.ALWAYS);

        Button publicar = boton("★  Publicar reseña", "button-gold");
        publicar.setPrefHeight(40);
        publicar.setMinWidth(175);

        formulario.getChildren().addAll(tituloFormulario, filaSelectores,
                etiquetaComentario, comentario, publicar);

        // Resumen y listado visual de opiniones.
        VBox panelResenas = new VBox(12);
        panelResenas.setPadding(new Insets(17));
        panelResenas.setStyle(
                "-fx-background-color: #0b1b2d;" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #294765;" +
                        "-fx-border-radius: 14;");

        HBox encabezadoResenas = new HBox(10);
        encabezadoResenas.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label tituloListado = new Label("Opiniones de la comunidad");
        tituloListado.setStyle("-fx-text-fill: white; -fx-font-size: 17px; -fx-font-weight: bold;");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        Button actualizar = boton("↻  Actualizar", "button-small-outline");
        encabezadoResenas.getChildren().addAll(tituloListado, espacio, actualizar);

        VBox listaResenas = new VBox(10);
        Label vacio = new Label("Selecciona una película para ver las opiniones.");
        vacio.setWrapText(true);
        vacio.setStyle("-fx-text-fill: #8fa6bd; -fx-font-size: 12px;");
        listaResenas.getChildren().add(vacio);

        Runnable cargar = () -> {
            listaResenas.getChildren().clear();

            Pelicula seleccionada = pelicula.getValue();
            if (seleccionada == null) {
                promedio.setText("Selecciona una película para consultar sus reseñas.");
                listaResenas.getChildren().add(estadoVacio(
                        "Elige una película",
                        "Cuando selecciones una película, aquí aparecerán las reseñas publicadas."));
                return;
            }

            List<ResenaPelicula> resenas =
                    operaciones.consultarResenas(seleccionada);
            double valorPromedio = operaciones.promedioResenas(seleccionada);

            promedio.setText(String.format(Locale.ROOT,
                    "★  Promedio de la película: %.1f / 5    ·    %d reseña(s)",
                    valorPromedio, resenas.size()));

            if (resenas.isEmpty()) {
                listaResenas.getChildren().add(estadoVacio(
                        "Todavía no hay reseñas",
                        "Sé el primero en compartir tu opinión sobre esta película."));
                return;
            }

            for (ResenaPelicula resena : resenas) {
                VBox tarjeta = new VBox(8);
                tarjeta.setPadding(new Insets(14));
                tarjeta.setStyle(
                        "-fx-background-color: #10243a;" +
                                "-fx-background-radius: 11;" +
                                "-fx-border-color: #294765;" +
                                "-fx-border-radius: 11;");

                HBox encabezado = new HBox(10);
                encabezado.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

                Label inicial = new Label(
                        resena.getCliente().getNombre().isBlank()
                                ? "C" : resena.getCliente().getNombre().substring(0, 1).toUpperCase(Locale.ROOT));
                inicial.setMinSize(34, 34);
                inicial.setAlignment(javafx.geometry.Pos.CENTER);
                inicial.setStyle(
                        "-fx-background-color: #f5b91b;" +
                                "-fx-background-radius: 20;" +
                                "-fx-text-fill: #101827;" +
                                "-fx-font-weight: bold;");

                VBox datosAutor = new VBox(3);
                Label autor = new Label(resena.getCliente().getNombre());
                autor.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                Label fecha = new Label(resena.getFechaPublicacion().toString().replace('T', ' '));
                fecha.setStyle("-fx-text-fill: #8fa6bd; -fx-font-size: 10px;");
                datosAutor.getChildren().addAll(autor, fecha);

                Region separador = new Region();
                HBox.setHgrow(separador, Priority.ALWAYS);
                Label estrellas = new Label(
                        "★".repeat(Math.max(0, Math.min(5, resena.getCalificacion())))
                                + "☆".repeat(Math.max(0, 5 - Math.min(5, resena.getCalificacion())))
                                + "  " + resena.getCalificacion() + "/5");
                estrellas.setStyle("-fx-text-fill: #f5c242; -fx-font-size: 13px; -fx-font-weight: bold;");

                encabezado.getChildren().addAll(inicial, datosAutor, separador, estrellas);

                Label texto = new Label(resena.getComentario());
                texto.setWrapText(true);
                texto.setStyle("-fx-text-fill: #d5dfec; -fx-font-size: 12px;");

                tarjeta.getChildren().addAll(encabezado, texto);
                listaResenas.getChildren().add(tarjeta);
            }
        };

        pelicula.setOnAction(e -> cargar.run());
        actualizar.setOnAction(e -> cargar.run());

        publicar.setOnAction(e -> {
            if (pelicula.getValue() == null) {
                aviso("Selecciona una película.");
                return;
            }
            if (comentario.getText() == null || comentario.getText().trim().isEmpty()) {
                aviso("Escribe un comentario antes de publicar.");
                comentario.requestFocus();
                return;
            }
            try {
                operaciones.publicarResena(cliente, pelicula.getValue(),
                        calificacion.getValue(), comentario.getText().trim());
                comentario.clear();
                cargar.run();
                aviso("¡Tu reseña se publicó correctamente!");
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });

        panelResenas.getChildren().addAll(encabezadoResenas, promedio, listaResenas);
        root.getChildren().addAll(formulario, panelResenas);
        cargar.run();
        return root;
    }

    private Node vistaPerfil() {
        VBox root = pagina("Mi perfil", "Administra tu información personal de CinemaUQ.");
        root.setSpacing(16);

        // Encabezado de identidad: conserva la paleta y los componentes del tema actual.
        HBox identidad = new HBox(16);
        identidad.getStyleClass().add("profile-identity-card");
        identidad.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        identidad.setPadding(new Insets(18));
        identidad.setStyle("-fx-background-color: linear-gradient(to right, #172b43, #102238);"
                + "-fx-border-color: #294765; -fx-border-radius: 14; -fx-background-radius: 14;");

        Label avatar = new Label(inicialesPerfil(cliente.getNombre()));
        avatar.setMinSize(66, 66);
        avatar.setPrefSize(66, 66);
        avatar.setAlignment(javafx.geometry.Pos.CENTER);
        avatar.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffd45a, #e99a08);"
                + "-fx-background-radius: 40; -fx-text-fill: #101827; -fx-font-size: 21px;"
                + "-fx-font-weight: bold;");

        VBox identidadTexto = new VBox(5);
        Label nombreVisible = new Label(cliente.getNombre());
        nombreVisible.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        Label correoVisible = new Label(cliente.getCorreo());
        correoVisible.setStyle("-fx-text-fill: #b8c9dc; -fx-font-size: 12px;");
        Label marca = new Label("CLIENTE CINEMAUQ");
        marca.setStyle("-fx-text-fill: #ffc43d; -fx-font-size: 10px; -fx-font-weight: bold;");
        identidadTexto.getChildren().addAll(nombreVisible, correoVisible, marca);
        identidad.getChildren().addAll(avatar, identidadTexto);

        VBox datosCard = new VBox(14);
        datosCard.getStyleClass().add("profile-data-card");
        datosCard.setPadding(new Insets(18));
        datosCard.setStyle("-fx-background-color: #0e2033; -fx-border-color: #294765;"
                + "-fx-border-radius: 14; -fx-background-radius: 14;");
        Label tituloDatos = new Label("Información personal");
        tituloDatos.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        Label ayuda = new Label("Consulta tus datos y activa la edición cuando quieras realizar cambios.");
        ayuda.setWrapText(true);
        ayuda.setStyle("-fx-text-fill: #9fb2c8; -fx-font-size: 11px;");

        TextField identificacion = new TextField(cliente.getId());
        identificacion.setEditable(false);
        identificacion.setFocusTraversable(false);
        TextField nombre = new TextField(cliente.getNombre());
        TextField correo = new TextField(cliente.getCorreo());
        configurarCampoPerfil(identificacion);
        configurarCampoPerfil(nombre);
        configurarCampoPerfil(correo);
        nombre.setEditable(false);
        correo.setEditable(false);
        nombre.setPromptText("Nombre completo");
        correo.setPromptText("correo@ejemplo.com");

        GridPane formulario = new GridPane();
        formulario.setHgap(14);
        formulario.setVgap(10);
        formulario.setPadding(new Insets(4, 0, 2, 0));
        ColumnConstraints colEtiqueta = new ColumnConstraints();
        colEtiqueta.setMinWidth(105);
        ColumnConstraints colCampo = new ColumnConstraints();
        colCampo.setHgrow(Priority.ALWAYS);
        formulario.getColumnConstraints().addAll(colEtiqueta, colCampo);
        agregarFilaPerfil(formulario, 0, "Identificación", identificacion);
        agregarFilaPerfil(formulario, 1, "Nombre completo", nombre);
        agregarFilaPerfil(formulario, 2, "Correo electrónico", correo);

        HBox acciones = new HBox(9);
        acciones.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Button editar = boton("✎  Editar perfil", "button-gold");
        Button guardar = boton("✓  Guardar cambios", "button-gold");
        Button cancelar = boton("Cancelar", "button-small-outline");
        guardar.setVisible(false);
        guardar.setManaged(false);
        cancelar.setVisible(false);
        cancelar.setManaged(false);
        Label estadoEdicion = new Label("Tus datos están protegidos. La identificación no se puede modificar.");
        estadoEdicion.setWrapText(true);
        estadoEdicion.setStyle("-fx-text-fill: #91a8c2; -fx-font-size: 10px;");

        Runnable modoEdicion = () -> {
            nombre.setEditable(true);
            correo.setEditable(true);
            // Los campos conservan el tema oscuro también durante la edición.
            editar.setVisible(false); editar.setManaged(false);
            guardar.setVisible(true); guardar.setManaged(true);
            cancelar.setVisible(true); cancelar.setManaged(true);
            estadoEdicion.setText("Edita tu nombre y correo. La identificación permanece protegida.");
            nombre.requestFocus();
        };
        editar.setOnAction(e -> modoEdicion.run());
        cancelar.setOnAction(e -> {
            nombre.setText(cliente.getNombre());
            correo.setText(cliente.getCorreo());
            nombre.setEditable(false); correo.setEditable(false);
            editar.setVisible(true); editar.setManaged(true);
            guardar.setVisible(false); guardar.setManaged(false);
            cancelar.setVisible(false); cancelar.setManaged(false);
            estadoEdicion.setText("No se realizaron cambios.");
        });
        guardar.setOnAction(e -> {
            String nuevoNombre = nombre.getText() == null ? "" : nombre.getText().trim();
            String nuevoCorreo = correo.getText() == null ? "" : correo.getText().trim();
            if (nuevoNombre.isBlank()) {
                aviso("Escribe tu nombre completo."); nombre.requestFocus(); return;
            }
            if (!nuevoCorreo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                aviso("Escribe un correo electrónico válido."); correo.requestFocus(); return;
            }
            try {
                operaciones.actualizarPerfil(cliente, nuevoNombre, nuevoCorreo);
                nombre.setText(cliente.getNombre());
                correo.setText(cliente.getCorreo());
                nombreVisible.setText(cliente.getNombre());
                correoVisible.setText(cliente.getCorreo());
                avatar.setText(inicialesPerfil(cliente.getNombre()));
                nombre.setEditable(false); correo.setEditable(false);
                editar.setVisible(true); editar.setManaged(true);
                guardar.setVisible(false); guardar.setManaged(false);
                cancelar.setVisible(false); cancelar.setManaged(false);
                estadoEdicion.setText("✓  Tus datos se actualizaron correctamente.");
                alActualizarPerfil.run();
                aviso("Perfil actualizado correctamente.");
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });
        acciones.getChildren().addAll(editar, guardar, cancelar);

        HBox metadatos = new HBox(10);
        metadatos.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label registro = new Label("▣  Cliente desde: " + cliente.getFechaRegistro());
        registro.setStyle("-fx-text-fill: #a9bdd2; -fx-font-size: 10px;");
        Label estado = new Label("●  " + (cliente.getEstado() == null ? "ACTIVO" : cliente.getEstado().name()));
        estado.setStyle("-fx-text-fill: #65d6a0; -fx-font-size: 10px; -fx-font-weight: bold;");
        metadatos.getChildren().addAll(registro, estado);

        datosCard.getChildren().addAll(tituloDatos, ayuda, formulario, acciones, estadoEdicion, new Separator(), metadatos);
        root.getChildren().addAll(identidad, datosCard);
        return root;
    }

    private void configurarCampoPerfil(TextField campo) {
        campo.setPrefHeight(38);
        campo.setMaxWidth(Double.MAX_VALUE);
        campo.getStyleClass().add("profile-input");
    }

    private void agregarFilaPerfil(GridPane grid, int fila, String etiqueta, TextField campo) {
        Label label = new Label(etiqueta);
        label.setStyle("-fx-text-fill: #c7d5e5; -fx-font-size: 11px; -fx-font-weight: bold;");
        grid.add(label, 0, fila);
        grid.add(campo, 1, fila);
        GridPane.setHgrow(campo, Priority.ALWAYS);
    }

    private String inicialesPerfil(String nombre) {
        if (nombre == null || nombre.isBlank()) return "CU";
        String[] partes = nombre.trim().split("\\s+");
        if (partes.length == 1) return partes[0].substring(0, 1).toUpperCase(Locale.ROOT);
        return (partes[0].substring(0, 1) + partes[partes.length - 1].substring(0, 1)).toUpperCase(Locale.ROOT);
    }

    private Node vistaNotificaciones() {
        VBox root = pagina("Notificaciones",
                "Mantente al tanto de las novedades y movimientos de tu cuenta.");
        root.setSpacing(14);

        List<String> mensajes = new ArrayList<>(
                operaciones.consultarNotificaciones(cliente));

        // Mensaje inicial de bienvenida mostrado siempre en la bandeja del cliente.
        VBox bienvenida = new VBox(9);
        bienvenida.setPadding(new Insets(17));
        bienvenida.setStyle(
                "-fx-background-color: linear-gradient(to right, #142f4b, #1b2940);"
                        + "-fx-background-radius: 14; -fx-border-color: #d9a928;"
                        + "-fx-border-radius: 14; -fx-border-width: 1.1;");

        HBox bienvenidaCabecera = new HBox(9);
        bienvenidaCabecera.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label iconoBienvenida = new Label("✦");
        iconoBienvenida.setStyle("-fx-text-fill: #ffd04a; -fx-font-size: 22px; -fx-font-weight: bold;");
        Label tituloBienvenida = new Label("¡Bienvenido a CinemaUQ!");
        tituloBienvenida.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        Region espacioBienvenida = new Region();
        HBox.setHgrow(espacioBienvenida, Priority.ALWAYS);
        Label etiquetaNueva = new Label("BIENVENIDA");
        etiquetaNueva.setStyle("-fx-text-fill: #172338; -fx-background-color: #f5bd32;"
                + "-fx-background-radius: 10; -fx-padding: 4 8; -fx-font-size: 9px; -fx-font-weight: bold;");
        bienvenidaCabecera.getChildren().addAll(
                iconoBienvenida, tituloBienvenida, espacioBienvenida, etiquetaNueva);

        Label textoBienvenida = new Label(
                "Nos alegra tenerte aquí. Explora nuestra cartelera, descubre los próximos estrenos "
                        + "y disfruta de una experiencia de cine hecha para ti.");
        textoBienvenida.setWrapText(true);
        textoBienvenida.setStyle("-fx-text-fill: #d1deeb; -fx-font-size: 12px;");

        Label fechaBienvenida = new Label(
                java.time.LocalDate.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        fechaBienvenida.setStyle("-fx-text-fill: #91a9bf; -fx-font-size: 10px;");
        bienvenida.getChildren().addAll(bienvenidaCabecera, textoBienvenida, fechaBienvenida);

        VBox lista = new VBox(10);
        lista.setFillWidth(true);

        if (mensajes.isEmpty()) {
            VBox vacio = new VBox(7);
            vacio.setPadding(new Insets(16));
            vacio.setStyle("-fx-background-color: #0d2035; -fx-background-radius: 12;"
                    + "-fx-border-color: #29445f; -fx-border-radius: 12;");
            Label tituloVacio = new Label("Todo está al día");
            tituloVacio.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");
            Label detalleVacio = new Label(
                    "Cuando realices compras o recibas avisos, aparecerán aquí.");
            detalleVacio.setWrapText(true);
            detalleVacio.setStyle("-fx-text-fill: #9fb3c9; -fx-font-size: 11px;");
            vacio.getChildren().addAll(tituloVacio, detalleVacio);
            lista.getChildren().add(vacio);
        } else {
            for (String mensaje : mensajes) {
                VBox tarjeta = new VBox(7);
                tarjeta.setPadding(new Insets(14, 16, 14, 16));
                tarjeta.setStyle("-fx-background-color: #0d2035; -fx-background-radius: 12;"
                        + "-fx-border-color: #29445f; -fx-border-radius: 12;");

                HBox encabezado = new HBox(8);
                encabezado.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                Label icono = new Label("●");
                icono.setStyle("-fx-text-fill: #f5bd32; -fx-font-size: 10px;");
                Label titulo = new Label("Aviso de CinemaUQ");
                titulo.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                Region espacio = new Region();
                HBox.setHgrow(espacio, Priority.ALWAYS);
                Label estado = new Label("NOTIFICACIÓN");
                estado.setStyle("-fx-text-fill: #f5c242; -fx-font-size: 9px; -fx-font-weight: bold;");
                encabezado.getChildren().addAll(icono, titulo, espacio, estado);

                Label contenido = new Label(mensaje);
                contenido.setWrapText(true);
                contenido.setStyle("-fx-text-fill: #c4d3e0; -fx-font-size: 11px;");
                tarjeta.getChildren().addAll(encabezado, contenido);
                lista.getChildren().add(tarjeta);
            }
        }

        Label subtitulo = new Label("CENTRO DE NOTIFICACIONES");
        subtitulo.setStyle("-fx-text-fill: #f5c242; -fx-font-size: 10px; -fx-font-weight: bold;");
        root.getChildren().addAll(bienvenida, subtitulo, lista);
        return root;
    }

    private Node vistaInformacion(String titulo) {
        VBox root = pagina(titulo, "Selecciona una opción del menú para consultar esta sección.");
        root.getChildren().add(estadoVacio(
                "Módulo disponible en el menú",
                "Su flujo se conecta con las operaciones del sistema."));
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

    private <T> TableView<T> tabla() {
        TableView<T> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(new Label("No hay registros para mostrar."));
        table.setPrefHeight(540);
        table.getStyleClass().add("cinemauq-table");
        return table;
    }

    private <T> TableColumn<T, String> columna(String titulo, Function<T, String> valor) {
        TableColumn<T, String> column = new TableColumn<>(titulo);
        column.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyStringWrapper(
                        Objects.toString(valor.apply(data.getValue()), "—")));
        return column;
    }

    private <T> void refrescar(TableView<T> tabla, Collection<T> datos) {
        tabla.setItems(FXCollections.observableArrayList(datos));
    }

    private HBox barra(Node... nodos) {
        HBox h = new HBox(8, nodos);
        h.setPadding(new Insets(4, 0, 8, 0));
        h.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return h;
    }

    private Button boton(String texto, String estilo) {
        Button b = new Button(texto);
        b.getStyleClass().add(estilo);
        return b;
    }

    private GridPane formulario() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.setPadding(new Insets(12));
        return grid;
    }

    private void fila(GridPane grid, int index, String texto, Node node) {
        Label label = new Label(texto);
        label.getStyleClass().add("field-label");
        grid.add(label, 0, index);
        grid.add(node, 1, index);
        GridPane.setHgrow(node, Priority.ALWAYS);
    }

    private <T> void configurarCombo(ComboBox<T> combo, Function<T, String> texto) {
        combo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(T obj) { return obj == null ? "" : texto.apply(obj); }
            @Override public T fromString(String value) { return null; }
        });
        // Estilo del selector cerrado: azul oscuro y texto claro.
        combo.setStyle(
                "-fx-background-color: #10243a; " +
                        "-fx-border-color: #294765; " +
                        "-fx-border-radius: 7; " +
                        "-fx-background-radius: 7; " +
                        "-fx-mark-color: #f5b91b; " +
                        "-fx-font-size: 12px;"
        );

        // Estilo de las opciones desplegadas (evita el menú blanco de JavaFX).
        combo.setCellFactory(list -> {
            ListCell<T> cell = new ListCell<>() {
                @Override protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : texto.apply(item));
                    setTextFill(javafx.scene.paint.Color.web("#f5f7fb"));
                    setStyle("-fx-background-color: #0d2035; " +
                            "-fx-padding: 8 10 8 10; " +
                            "-fx-font-size: 12px;");
                }
            };
            cell.hoverProperty().addListener((obs, anterior, hover) -> {
                if (!cell.isEmpty()) {
                    cell.setStyle(hover
                            ? "-fx-background-color: #1b3b5a; -fx-text-fill: #ffffff; " +
                              "-fx-padding: 8 10 8 10; -fx-font-size: 12px;"
                            : "-fx-background-color: #0d2035; -fx-text-fill: #f5f7fb; " +
                              "-fx-padding: 8 10 8 10; -fx-font-size: 12px;");
                }
            });
            cell.selectedProperty().addListener((obs, anterior, seleccionado) -> {
                if (!cell.isEmpty() && seleccionado) {
                    cell.setStyle("-fx-background-color: #f5b91b; " +
                            "-fx-text-fill: #101827; -fx-padding: 8 10 8 10; " +
                            "-fx-font-size: 12px; -fx-font-weight: bold;");
                    cell.setTextFill(javafx.scene.paint.Color.web("#101827"));
                }
            });
            return cell;
        });

        // Texto visible cuando el ComboBox está cerrado.
        combo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : texto.apply(item));
                setTextFill(javafx.scene.paint.Color.web("#f5f7fb"));
                setStyle("-fx-background-color: transparent; " +
                        "-fx-text-fill: #f5f7fb; -fx-font-size: 12px;");
            }
        });
        combo.setMaxWidth(Double.MAX_VALUE);
    }

    /**
     * Diálogo personalizado de confirmación para recargas de la tarjeta virtual.
     * Mantiene la estética azul noche y dorada de CinemaUQ.
     */
    private void mostrarConfirmacionRecarga(BigDecimal valor, TarjetaVirtual tarjetaActualizada) {
        Dialog<Void> dialogo = new Dialog<>();
        configurarDialogoCinemaUQ(dialogo);
        if (owner != null) {
            dialogo.initOwner(owner);
        }

        dialogo.setTitle("CinemaUQ · Recarga exitosa");
        dialogo.setHeaderText(null);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.OK);

        DialogPane panel = dialogo.getDialogPane();
        panel.setStyle(
                "-fx-background-color: #071629;"
                        + "-fx-border-color: #34516e;"
                        + "-fx-border-width: 1;"
        );
        panel.setMinWidth(440);
        panel.setPrefWidth(460);

        Button cerrar = (Button) panel.lookupButton(ButtonType.OK);
        cerrar.setText("Continuar");
        cerrar.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffd34d, #eaa018);"
                        + "-fx-background-radius: 8;"
                        + "-fx-text-fill: #101827;"
                        + "-fx-font-size: 12px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-padding: 9 25;"
                        + "-fx-cursor: hand;"
        );

        VBox contenido = new VBox(13);
        contenido.setAlignment(javafx.geometry.Pos.CENTER);
        contenido.setPadding(new Insets(25, 28, 22, 28));
        contenido.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #10243a, #0a192b);"
                        + "-fx-background-radius: 12;"
        );

        StackPane iconoContenedor = new StackPane();
        iconoContenedor.setMinSize(62, 62);
        iconoContenedor.setPrefSize(62, 62);
        iconoContenedor.setMaxSize(62, 62);
        iconoContenedor.setStyle(
                "-fx-background-color: rgba(63, 190, 139, 0.15);"
                        + "-fx-background-radius: 40;"
                        + "-fx-border-color: #3a9b78;"
                        + "-fx-border-radius: 40;"
                        + "-fx-border-width: 1.2;"
        );

        Label icono = new Label("✓");
        icono.setStyle(
                "-fx-text-fill: #78e0b5;"
                        + "-fx-font-size: 33px;"
                        + "-fx-font-weight: bold;"
        );
        iconoContenedor.getChildren().add(icono);

        Label titulo = new Label("¡Recarga realizada!");
        titulo.setStyle(
                "-fx-text-fill: #f5f7fb;"
                        + "-fx-font-size: 21px;"
                        + "-fx-font-weight: bold;"
        );

        Label subtitulo = new Label("El dinero ya está disponible en tu tarjeta virtual.");
        subtitulo.setWrapText(true);
        subtitulo.setMaxWidth(350);
        subtitulo.setAlignment(javafx.geometry.Pos.CENTER);
        subtitulo.setStyle(
                "-fx-text-fill: #aebfd1;"
                        + "-fx-font-size: 11px;"
        );

        VBox detalleRecarga = new VBox(6);
        detalleRecarga.setAlignment(javafx.geometry.Pos.CENTER);
        detalleRecarga.setMaxWidth(Double.MAX_VALUE);
        detalleRecarga.setPadding(new Insets(15, 18, 15, 18));
        detalleRecarga.setStyle(
                "-fx-background-color: #142b44;"
                        + "-fx-background-radius: 11;"
                        + "-fx-border-color: #385675;"
                        + "-fx-border-radius: 11;"
        );

        Label etiquetaImporte = new Label("VALOR RECARGADO");
        etiquetaImporte.setStyle(
                "-fx-text-fill: #91a8c0;"
                        + "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
        );

        Label importe = new Label(moneda(valor));
        importe.setStyle(
                "-fx-text-fill: #ffd04a;"
                        + "-fx-font-size: 27px;"
                        + "-fx-font-weight: bold;"
        );

        Separator separador = new Separator();
        separador.setStyle("-fx-background-color: #2b4662;");

        HBox filaSaldo = new HBox(8);
        filaSaldo.setAlignment(javafx.geometry.Pos.CENTER);
        Label etiquetaSaldo = new Label("Nuevo saldo disponible");
        etiquetaSaldo.setStyle("-fx-text-fill: #c0cddd; -fx-font-size: 11px;");
        Label nuevoSaldo = new Label(moneda(tarjetaActualizada.getSaldo()));
        nuevoSaldo.setStyle(
                "-fx-text-fill: #78e0b5;"
                        + "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
        );
        filaSaldo.getChildren().addAll(etiquetaSaldo, nuevoSaldo);

        detalleRecarga.getChildren().addAll(
                etiquetaImporte, importe, separador, filaSaldo
        );

        Label pie = new Label("CinemaUQ · Tu experiencia, siempre contigo");
        pie.setStyle(
                "-fx-text-fill: #7189a2;"
                        + "-fx-font-size: 9px;"
        );

        contenido.getChildren().addAll(
                iconoContenedor, titulo, subtitulo, detalleRecarga, pie
        );
        panel.setContent(contenido);

        dialogo.showAndWait();
    }

    /**
     * Aplica el tema CinemaUQ a todas las superficies de un Dialog.
     * Incluye la escena independiente, el panel, el contenido y la barra inferior,
     * para evitar que la skin predeterminada de JavaFX deje áreas blancas.
     */
    private void configurarDialogoCinemaUQ(Dialog<?> dialogo) {
        DialogPane panel = dialogo.getDialogPane();
        panel.getStyleClass().add("cinemauq-client-dialog");
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
                        Insets.EMPTY
                )
        ));

        URL hoja = ClienteViewController.class.getResource(
                "/co/edu/uniquindio/proyectofinal/css/styles.css"
        );
        if (hoja != null && !panel.getStylesheets().contains(hoja.toExternalForm())) {
            panel.getStylesheets().add(hoja.toExternalForm());
        }

        panel.sceneProperty().addListener((observable, anterior, escena) -> {
            if (escena == null) return;
            javafx.application.Platform.runLater(() -> {
                javafx.scene.Parent raizEscena = escena.getRoot();
                if (raizEscena instanceof Region regionRaiz) {
                    regionRaiz.setBackground(new javafx.scene.layout.Background(
                            new javafx.scene.layout.BackgroundFill(
                                    javafx.scene.paint.Color.web("#071629"),
                                    javafx.scene.layout.CornerRadii.EMPTY,
                                    Insets.EMPTY
                            )
                    ));
                    regionRaiz.setStyle("-fx-background-color: #071629;");
                }

                panel.setStyle(
                        "-fx-background-color: #071629;"
                                + "-fx-control-inner-background: #0b1b2d;"
                                + "-fx-border-color: #344c67;"
                                + "-fx-padding: 0;"
                );
                panel.applyCss();

                for (String selector : List.of(
                        ".content", ".header-panel", ".button-bar",
                        ".container", ".graphic-container"
                )) {
                    Node nodo = panel.lookup(selector);
                    if (nodo instanceof Region region) {
                        region.setBackground(new javafx.scene.layout.Background(
                                new javafx.scene.layout.BackgroundFill(
                                        javafx.scene.paint.Color.web(
                                                selector.equals(".button-bar")
                                                        ? "#071629" : "#0b1b2d"
                                        ),
                                        javafx.scene.layout.CornerRadii.EMPTY,
                                        Insets.EMPTY
                                )
                        ));
                        region.setStyle(
                                "-fx-background-color: "
                                        + (selector.equals(".button-bar")
                                        ? "#071629" : "#0b1b2d")
                                        + ";"
                        );
                    }
                }
                panel.layout();
            });
        });
    }

    /**
     * Formulario de texto CinemaUQ para reemplazar TextInputDialog nativos.
     */
    private Optional<String> solicitarTextoCinemaUQ(
            String titulo,
            String encabezado,
            String etiqueta,
            String valorInicial,
            String prompt
    ) {
        Dialog<String> dialogo = new Dialog<>();
        configurarDialogoCinemaUQ(dialogo);
        if (owner != null) dialogo.initOwner(owner);

        dialogo.setTitle("CinemaUQ · " + titulo);
        dialogo.setHeaderText(null);

        ButtonType aceptar = new ButtonType("Continuar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialogo.getDialogPane().getButtonTypes().addAll(aceptar, cancelar);

        VBox contenido = new VBox(11);
        contenido.setPadding(new Insets(20, 23, 17, 23));
        contenido.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #10243a, #09192b);"
        );

        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle(
                "-fx-text-fill: #f5f7fb; -fx-font-size: 18px; -fx-font-weight: bold;"
        );

        Label detalle = new Label(encabezado == null ? "" : encabezado);
        detalle.setWrapText(true);
        detalle.setStyle("-fx-text-fill: #b8c9d9; -fx-font-size: 11px;");

        Label etiquetaLabel = new Label(etiqueta);
        etiquetaLabel.setStyle(
                "-fx-text-fill: #e9bb4e; -fx-font-size: 10px; -fx-font-weight: bold;"
        );

        TextField entrada = new TextField(valorInicial == null ? "" : valorInicial);
        entrada.setPromptText(prompt);
        entrada.setMaxWidth(Double.MAX_VALUE);
        entrada.setStyle(
                "-fx-background-color: #0b1b2d;"
                        + "-fx-control-inner-background: #0b1b2d;"
                        + "-fx-text-fill: #f5f7fb;"
                        + "-fx-prompt-text-fill: #8198af;"
                        + "-fx-highlight-fill: #d9a52b;"
                        + "-fx-background-radius: 8;"
                        + "-fx-border-color: #35516e;"
                        + "-fx-border-radius: 8;"
                        + "-fx-padding: 10 12;"
        );

        contenido.getChildren().addAll(
                tituloLabel, detalle, etiquetaLabel, entrada
        );
        dialogo.getDialogPane().setContent(contenido);

        Button botonAceptar = (Button) dialogo.getDialogPane().lookupButton(aceptar);
        botonAceptar.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffd34d, #eaa018);"
                        + "-fx-text-fill: #101827; -fx-font-weight: bold;"
                        + "-fx-background-radius: 8; -fx-padding: 9 20;"
        );
        Button botonCancelar = (Button) dialogo.getDialogPane().lookupButton(cancelar);
        botonCancelar.setStyle(
                "-fx-background-color: #172d45; -fx-text-fill: #e5edf6;"
                        + "-fx-border-color: #3b5876; -fx-border-radius: 8;"
                        + "-fx-background-radius: 8; -fx-padding: 9 20;"
        );

        dialogo.setResultConverter(tipo ->
                tipo == aceptar ? entrada.getText().trim() : null
        );
        dialogo.setOnShown(evento -> entrada.requestFocus());

        return dialogo.showAndWait();
    }

    /**
     * Diálogo informativo personalizado. Se utiliza en las validaciones y
     * confirmaciones de los módulos del cliente para mantener una apariencia
     * uniforme con CinemaUQ, en lugar de mostrar alertas nativas de JavaFX.
     */
    private void aviso(String mensaje) {
        String texto = mensaje == null || mensaje.isBlank()
                ? "La operación requiere tu atención."
                : mensaje.trim();
        String normalizado = texto.toLowerCase(Locale.ROOT);

        boolean exito = normalizado.contains("agregada")
                || normalizado.contains("guardada")
                || normalizado.contains("retirada")
                || normalizado.contains("actualizada correctamente")
                || normalizado.contains("realizada correctamente")
                || normalizado.contains("se ha actualizado")
                || normalizado.contains("cancelada correctamente")
                || normalizado.contains("confirmada");

        String titulo = exito ? "¡Todo listo!" : "Un momento";
        String colorIcono = exito ? "#78E0B5" : "#FFD04A";
        String fondoIcono = exito
                ? "rgba(63, 190, 139, 0.15)"
                : "rgba(238, 177, 45, 0.13)";
        String bordeIcono = exito ? "#3A9B78" : "#A77A24";
        String simbolo = exito ? "✓" : "i";

        Dialog<Void> dialogo = new Dialog<>();
        configurarDialogoCinemaUQ(dialogo);
        if (owner != null) {
            dialogo.initOwner(owner);
        }
        dialogo.setTitle("CinemaUQ · " + (exito ? "Confirmación" : "Aviso"));
        dialogo.setHeaderText(null);
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.OK);

        DialogPane panel = dialogo.getDialogPane();
        panel.setMinWidth(420);
        panel.setPrefWidth(445);
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
        contenido.setAlignment(javafx.geometry.Pos.CENTER);
        contenido.setPadding(new Insets(22, 25, 20, 25));
        contenido.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #10243a, #09192b);"
                        + "-fx-background-radius: 11;"
        );

        StackPane iconoContenedor = new StackPane();
        iconoContenedor.setMinSize(56, 56);
        iconoContenedor.setPrefSize(56, 56);
        iconoContenedor.setMaxSize(56, 56);
        iconoContenedor.setStyle(
                "-fx-background-color: " + fondoIcono + ";"
                        + "-fx-border-color: " + bordeIcono + ";"
                        + "-fx-background-radius: 40;"
                        + "-fx-border-radius: 40;"
                        + "-fx-border-width: 1;"
        );

        Label icono = new Label(simbolo);
        icono.setStyle(
                "-fx-text-fill: " + colorIcono + ";"
                        + "-fx-font-size: " + (exito ? "29px" : "25px") + ";"
                        + "-fx-font-weight: bold;"
        );
        iconoContenedor.getChildren().add(icono);

        Label encabezado = new Label(titulo);
        encabezado.setStyle(
                "-fx-text-fill: #F5F7FB;"
                        + "-fx-font-size: 19px;"
                        + "-fx-font-weight: bold;"
        );

        Label descripcion = new Label(texto);
        descripcion.setWrapText(true);
        descripcion.setMaxWidth(340);
        descripcion.setAlignment(javafx.geometry.Pos.CENTER);
        descripcion.setStyle(
                "-fx-text-fill: #C2CFDD;"
                        + "-fx-font-size: 12px;"
                        + "-fx-line-spacing: 2px;"
        );

        Label marca = new Label("CINEMAUQ  ·  EXPERIENCIA CINEMATOGRÁFICA");
        marca.setStyle(
                "-fx-text-fill: #7189A2;"
                        + "-fx-font-size: 8px;"
                        + "-fx-font-weight: bold;"
        );

        contenido.getChildren().addAll(iconoContenedor, encabezado, descripcion, marca);
        panel.setContent(contenido);
        dialogo.showAndWait();
    }

    /**
     * Muestra errores con el lenguaje visual de CinemaUQ y conserva el mensaje
     * real de la operación para que el usuario sepa qué debe corregir.
     */
    private void mostrarError(Throwable ex) {
        String detalle = obtenerMensajeError(ex);
        String detalleNormalizado = detalle.toLowerCase(Locale.ROOT);

        boolean saldoInsuficiente = detalleNormalizado.contains("saldo")
                && (detalleNormalizado.contains("suficiente")
                || detalleNormalizado.contains("insuficiente"));

        String titulo = saldoInsuficiente
                ? "Saldo insuficiente"
                : "No se pudo completar la operación";

        String explicacion = saldoInsuficiente
                ? "Tu tarjeta virtual no tiene saldo suficiente para pagar este pedido."
                : "Ocurrió un inconveniente al procesar tu solicitud.";

        String recomendacion = saldoInsuficiente
                ? "Puedes recargar tu tarjeta virtual o quitar algunos productos del carrito y volver a intentarlo."
                : "Revisa la información e inténtalo nuevamente. Si el problema continúa, contacta al administrador.";

        Dialog<Void> dialogo = new Dialog<>();
        configurarDialogoCinemaUQ(dialogo);
        if (owner != null) {
            dialogo.initOwner(owner);
        }
        dialogo.setTitle("CinemaUQ · Aviso");
        dialogo.getDialogPane().getButtonTypes().add(ButtonType.OK);

        DialogPane panel = dialogo.getDialogPane();
        panel.setMinWidth(450);
        panel.setPrefWidth(470);
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

        VBox contenido = new VBox(13);
        contenido.setAlignment(javafx.geometry.Pos.CENTER);
        contenido.setPadding(new Insets(23, 26, 20, 26));
        contenido.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #10243a, #09192b);"
                        + "-fx-background-radius: 11;"
        );

        StackPane iconoContenedor = new StackPane();
        iconoContenedor.setMinSize(58, 58);
        iconoContenedor.setPrefSize(58, 58);
        iconoContenedor.setMaxSize(58, 58);
        iconoContenedor.setStyle(
                saldoInsuficiente
                        ? "-fx-background-color: rgba(238, 177, 45, 0.13);"
                          + "-fx-border-color: #a77a24;"
                          + "-fx-background-radius: 40; -fx-border-radius: 40;"
                        : "-fx-background-color: rgba(220, 91, 91, 0.13);"
                          + "-fx-border-color: #a84f56;"
                          + "-fx-background-radius: 40; -fx-border-radius: 40;"
        );

        Label icono = new Label(saldoInsuficiente ? "$" : "!");
        icono.setStyle(
                (saldoInsuficiente ? "-fx-text-fill: #ffd04a;" : "-fx-text-fill: #ff9292;")
                        + "-fx-font-size: 29px; -fx-font-weight: bold;"
        );
        iconoContenedor.getChildren().add(icono);

        Label encabezado = new Label(titulo);
        encabezado.setStyle(
                "-fx-text-fill: #f5f7fb;"
                        + "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
        );

        Label mensaje = new Label(explicacion);
        mensaje.setWrapText(true);
        mensaje.setMaxWidth(365);
        mensaje.setAlignment(javafx.geometry.Pos.CENTER);
        mensaje.setStyle(
                "-fx-text-fill: #b9c9d9;"
                        + "-fx-font-size: 12px;"
        );

        VBox detalleCard = new VBox(7);
        detalleCard.setMaxWidth(Double.MAX_VALUE);
        detalleCard.setPadding(new Insets(13, 15, 13, 15));
        detalleCard.setStyle(
                "-fx-background-color: #142b44;"
                        + "-fx-background-radius: 10;"
                        + "-fx-border-color: #344f6d;"
                        + "-fx-border-radius: 10;"
        );

        Label etiquetaDetalle = new Label("DETALLE");
        etiquetaDetalle.setStyle(
                "-fx-text-fill: #e9bb4e;"
                        + "-fx-font-size: 9px;"
                        + "-fx-font-weight: bold;"
        );

        Label textoDetalle = new Label(detalle);
        textoDetalle.setWrapText(true);
        textoDetalle.setMaxWidth(Double.MAX_VALUE);
        textoDetalle.setStyle(
                "-fx-text-fill: #e1e9f2;"
                        + "-fx-font-size: 11px;"
        );
        detalleCard.getChildren().addAll(etiquetaDetalle, textoDetalle);

        Label sugerencia = new Label(recomendacion);
        sugerencia.setWrapText(true);
        sugerencia.setMaxWidth(365);
        sugerencia.setAlignment(javafx.geometry.Pos.CENTER);
        sugerencia.setStyle(
                "-fx-text-fill: #8fa6be;"
                        + "-fx-font-size: 10px;"
        );

        Label marca = new Label("CINEMAUQ  ·  EXPERIENCIA CINEMATOGRÁFICA");
        marca.setStyle(
                "-fx-text-fill: #647d97;"
                        + "-fx-font-size: 8px;"
                        + "-fx-font-weight: bold;"
        );

        contenido.getChildren().addAll(
                iconoContenedor,
                encabezado,
                mensaje,
                detalleCard,
                sugerencia,
                marca
        );

        panel.setContent(contenido);
        dialogo.showAndWait();
    }

    /**
     * Recupera el mensaje más útil de la excepción, incluyendo su causa
     * cuando la excepción principal no trae una descripción.
     */
    private String obtenerMensajeError(Throwable ex) {
        if (ex == null) {
            return "Se produjo un error inesperado.";
        }

        Throwable actual = ex;
        while (actual != null) {
            String mensaje = actual.getMessage();
            if (mensaje != null && !mensaje.isBlank()) {
                return mensaje.trim();
            }
            actual = actual.getCause();
        }

        return "Se produjo un error inesperado. Inténtalo nuevamente.";
    }

    private String moneda(BigDecimal valor) {
        return java.text.NumberFormat.getCurrencyInstance(new Locale("es", "CO"))
                .format(valor);
    }

    private String mascara(String numero) {
        return numero.length() <= 4 ? numero
                : "•••• •••• " + numero.substring(numero.length() - 4);
    }

    private VBox tarjetaResumenCompra(String titulo, Label valor, String icono) {
        VBox tarjeta = new VBox(8);
        tarjeta.setPadding(new Insets(13, 16, 13, 16));
        tarjeta.setMinHeight(88);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        tarjeta.setStyle("-fx-background-color: linear-gradient(to bottom right, #132c46, #0c1d31);"
                + "-fx-background-radius: 12; -fx-border-color: #294764; -fx-border-radius: 12;");
        HBox encabezado = new HBox(8);
        encabezado.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label simbolo = new Label(icono);
        simbolo.setStyle("-fx-text-fill: #f5c242; -fx-font-size: 16px; -fx-font-weight: bold;");
        Label etiqueta = new Label(titulo);
        etiqueta.setStyle("-fx-text-fill: #9fb3c9; -fx-font-size: 9px; -fx-font-weight: bold;");
        encabezado.getChildren().addAll(simbolo, etiqueta);
        valor.setStyle("-fx-text-fill: white; -fx-font-size: 19px; -fx-font-weight: bold;");
        tarjeta.getChildren().addAll(encabezado, valor);
        HBox.setHgrow(tarjeta, Priority.ALWAYS);
        tarjeta.setPrefWidth(220);
        return tarjeta;
    }


    private VBox crearIndicadorPuntos(String titulo, String valor, String icono, String color) {
        VBox tarjeta = new VBox(7);
        tarjeta.setPadding(new Insets(14));
        tarjeta.setMinHeight(83);
        tarjeta.setStyle("-fx-background-color: #10243a; -fx-background-radius: 12;"
                + "-fx-border-color: #294760; -fx-border-radius: 12;");
        Label encabezado = new Label(icono + "  " + titulo);
        encabezado.setStyle("-fx-text-fill: #a9bfd4; -fx-font-size: 9px; -fx-font-weight: bold;");
        Label dato = new Label(valor);
        dato.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 17px; -fx-font-weight: bold;");
        dato.setWrapText(true);
        tarjeta.getChildren().addAll(encabezado, dato);
        HBox.setHgrow(tarjeta, Priority.ALWAYS);
        tarjeta.setMaxWidth(Double.MAX_VALUE);
        return tarjeta;
    }

}

