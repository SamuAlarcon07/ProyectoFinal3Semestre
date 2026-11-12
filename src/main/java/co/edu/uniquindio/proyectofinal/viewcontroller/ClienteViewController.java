package co.edu.uniquindio.proyectofinal.viewcontroller;

import co.edu.uniquindio.proyectofinal.controller.CinemaOperationsController;
import co.edu.uniquindio.proyectofinal.model.enums.EstadoCancelacion;
import co.edu.uniquindio.proyectofinal.model.enums.TipoListaPelicula;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;
import co.edu.uniquindio.proyectofinal.model.principal.*;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Window;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * Construye los módulos del cliente dentro del Dashboard compartido.
 */
public class ClienteViewController {

    private final CinemaOperationsController operaciones;
    private final Cliente cliente;
    private final Window owner;

    public ClienteViewController(
            CinemaOperationsController operaciones,
            Cliente cliente,
            Window owner) {
        this.operaciones = Objects.requireNonNull(operaciones);
        this.cliente = Objects.requireNonNull(cliente);
        this.owner = owner;
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
                "Consulta tus entradas emitidas y los códigos QR asociados.");

        List<Entrada> entradas = operaciones.consultarEntradas(cliente);
        TableView<Entrada> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", Entrada::getCodigo),
                columna("Película", e -> e.getFuncion().getPelicula().getTitulo()),
                columna("Función", e -> e.getFuncion().getFechaHora().toString().replace('T', ' ')),
                columna("Sala", e -> e.getFuncion().getSala().getNombre()),
                columna("Asiento", e -> e.getAsiento().obtenerIdentificador()),
                columna("Estado", e -> e.getEstado().name()),
                columna("Código QR", Entrada::getCodigoQR)
        );
        refrescar(tabla, entradas);
        Button actualizar = boton("Actualizar", "button-small-outline");
        actualizar.setOnAction(e -> refrescar(tabla, operaciones.consultarEntradas(cliente)));
        Button verQr = boton("Ver código QR", "button-gold");
        verQr.setOnAction(e -> {
            Entrada seleccion = tabla.getSelectionModel().getSelectedItem();
            if (seleccion == null) { aviso("Selecciona una entrada."); return; }
            aviso("Código QR de la entrada:\n\n" + seleccion.getCodigoQR()
                    + "\n\nPresenta este código en el control de acceso.");
        });
        root.getChildren().addAll(barra(actualizar, verQr), tabla);
        return root;
    }

    private Node vistaCompras() {
        VBox root = pagina("Mis compras",
                "Consulta tus transacciones, detalles de compra y solicita cancelaciones cuando la política lo permita.");

        TableView<Compra> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Código", Compra::getCodigo),
                columna("Fecha", c -> c.getFecha().toString().replace('T', ' ')),
                columna("Artículos", c -> String.valueOf(c.getDetalles().size())),
                columna("Subtotal", c -> moneda(c.calcularSubtotal())),
                columna("Descuentos", c -> moneda(c.getDescuentos())),
                columna("Total", c -> moneda(c.getTotal())),
                columna("Estado", Compra::consultarNombreEstado)
        );
        refrescar(tabla, operaciones.consultarCompras(cliente));

        Button detalle = boton("Ver detalle", "button-small-outline");
        detalle.setOnAction(e -> {
            Compra compra = tabla.getSelectionModel().getSelectedItem();
            if (compra == null) { aviso("Selecciona una compra."); return; }
            StringBuilder texto = new StringBuilder();
            for (DetalleCompra d : compra.getDetalles()) {
                texto.append(d.getElemento().consultarDescripcion())
                        .append(" x").append(d.getCantidad())
                        .append(" — ").append(moneda(d.getSubtotal())).append('\n');
            }
            texto.append("\nSubtotal: ").append(moneda(compra.calcularSubtotal()))
                    .append("\nDescuentos: ").append(moneda(compra.getDescuentos()))
                    .append("\nPuntos redimidos: ").append(moneda(compra.getValorPuntosRedimidos()))
                    .append("\nTotal: ").append(moneda(compra.getTotal()));
            aviso(texto.toString());
        });

        Button cancelar = boton("Solicitar cancelación", "button-gold");
        cancelar.setOnAction(e -> {
            Compra compra = tabla.getSelectionModel().getSelectedItem();
            if (compra == null) { aviso("Selecciona una compra."); return; }
            if (!"PAGADA".equals(compra.consultarNombreEstado())) {
                aviso("Solo se pueden cancelar compras pagadas.");
                return;
            }
            TextInputDialog motivo = new TextInputDialog("No podré asistir a la función");
            motivo.setTitle("Solicitud de cancelación");
            motivo.setHeaderText("Compra " + compra.getCodigo());
            motivo.setContentText("Motivo:");
            if (owner != null) motivo.initOwner(owner);
            motivo.showAndWait().ifPresent(texto -> {
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
                    refrescar(tabla, operaciones.consultarCompras(cliente));
                } catch (RuntimeException ex) {
                    mostrarError(ex);
                }
            });
        });

        Button actualizar = boton("Actualizar", "button-small-outline");
        actualizar.setOnAction(e -> refrescar(tabla, operaciones.consultarCompras(cliente)));
        root.getChildren().addAll(barra(detalle, cancelar, actualizar), tabla);
        return root;
    }

    private Node vistaTarjeta() {
        VBox root = pagina("Mi tarjeta virtual",
                "Consulta el saldo, el estado y los movimientos de tu tarjeta CinemaUQ.");

        Optional<TarjetaVirtual> opcional = operaciones.consultarTarjeta(cliente);
        if (opcional.isEmpty()) {
            root.getChildren().add(estadoVacio(
                    "No tienes tarjeta virtual",
                    "Contacta al administrador para verificar tu cuenta."));
            return root;
        }

        TarjetaVirtual tarjeta = opcional.get();
        VBox tarjetaVisual = new VBox(9);
        tarjetaVisual.getStyleClass().add("virtual-card-face");
        Label marca = new Label("🎬 CINEMA UQ");
        marca.getStyleClass().add("virtual-card-brand");
        Label numero = new Label(mascara(tarjeta.getNumero()));
        numero.getStyleClass().add("virtual-card-number");
        Label titular = new Label(cliente.getNombre());
        titular.getStyleClass().add("virtual-card-holder");
        Label saldo = new Label("Saldo disponible: " + moneda(tarjeta.getSaldo()));
        saldo.getStyleClass().add("virtual-card-balance");
        Label estado = new Label("Estado: " + tarjeta.getEstado().name());
        tarjetaVisual.getChildren().addAll(marca, numero, titular, saldo, estado);

        Label movimientosLabel = new Label("Movimientos");
        movimientosLabel.getStyleClass().add("section-title");

        TableView<MovimientoTarjeta> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Fecha", m -> m.getFecha().toString().replace('T', ' ')),
                columna("Tipo", m -> m.getTipo().name()),
                columna("Valor", m -> moneda(m.getValor())),
                columna("Saldo", m -> moneda(m.getSaldoResultante())),
                columna("Referencia", MovimientoTarjeta::getReferenciaOperacion)
        );
        refrescar(tabla, tarjeta.getMovimientos());

        // Recargas de demostración: simulan el flujo de una pasarela de pago.
        java.util.function.Consumer<BigDecimal> ejecutarRecarga = valor -> {
            try {
                operaciones.recargarTarjetaCliente(cliente, valor);
                saldo.setText("Saldo disponible: "
                        + moneda(operaciones.consultarTarjeta(cliente)
                        .orElseThrow().getSaldo()));
                refrescar(tabla, tarjeta.getMovimientos());
                aviso("Recarga realizada correctamente por " + moneda(valor)
                        + ". El nuevo saldo ya está disponible.");
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
            TextInputDialog entrada = new TextInputDialog("30000");
            entrada.setTitle("Recargar tarjeta virtual");
            entrada.setHeaderText("Ingresa el valor que deseas agregar");
            entrada.setContentText("Valor en pesos colombianos (COP):");
            if (owner != null) entrada.initOwner(owner);
            entrada.showAndWait().ifPresent(valorTexto -> {
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
                "Recargas de demostración: no se realiza un cobro bancario real.");
        notaRecarga.getStyleClass().add("muted-small");

        root.getChildren().addAll(tarjetaVisual,
                new Label("Agregar saldo"), accionesRecarga, notaRecarga,
                movimientosLabel, tabla);
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
                "Consulta tu saldo, acumulaciones, redenciones y vencimientos.");

        int saldo = operaciones.consultarPuntos(cliente);
        Label numero = new Label(String.format("%,d puntos disponibles", saldo));
        numero.getStyleClass().add("points-value-large");

        Label regla = new Label(
                "Regla actual: 1 punto por cada $10.000 efectivamente pagados. "
                        + "Cada punto equivale a $50 y la redención está limitada al 50 % del subtotal."
        );
        regla.setWrapText(true);
        regla.getStyleClass().add("muted-small");

        TableView<MovimientoPuntos> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Fecha", m -> m.getFecha().toString()),
                columna("Movimiento", m -> m.getTipo().name()),
                columna("Cantidad", m -> String.valueOf(m.getCantidad())),
                columna("Descripción", MovimientoPuntos::getDescripcion),
                columna("Referencia", MovimientoPuntos::getCodigoReferencia)
        );
        refrescar(tabla, operaciones.consultarMovimientosPuntos(cliente));

        root.getChildren().addAll(numero, regla,
                new Label("Historial de puntos"), tabla);
        return root;
    }

    private Node vistaPeliculasGuardadas() {
        VBox root = pagina("Películas guardadas",
                "Organiza las películas que te gustan y las que quieres ver más adelante.");

        ComboBox<TipoListaPelicula> tipo = new ComboBox<>(
                FXCollections.observableArrayList(TipoListaPelicula.values()));
        tipo.setValue(TipoListaPelicula.FAVORITA);

        TableView<PeliculaGuardada> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Película", p -> p.getPelicula().getTitulo()),
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
            if (pelicula.getValue() == null) { aviso("Selecciona una película."); return; }
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
            PeliculaGuardada p = tabla.getSelectionModel().getSelectedItem();
            if (p == null) { aviso("Selecciona una película guardada."); return; }
            try {
                operaciones.quitarPeliculaGuardada(
                        cliente, p.getPelicula(), p.getTipoLista());
                cargar.run();
            } catch (RuntimeException ex) { mostrarError(ex); }
        });

        cargar.run();
        root.getChildren().addAll(barra(tipo, pelicula, agregar, eliminar), tabla);
        return root;
    }

    private Node vistaResenas() {
        VBox root = pagina("Reseñas",
                "Publica tu opinión sobre películas cuya función ya haya finalizado y para las que tengas una entrada pagada.");

        ComboBox<Pelicula> pelicula = new ComboBox<>(
                FXCollections.observableArrayList(operaciones.consultarPeliculas()));
        configurarCombo(pelicula, Pelicula::getTitulo);

        ComboBox<Integer> calificacion = new ComboBox<>(
                FXCollections.observableArrayList(1, 2, 3, 4, 5));
        calificacion.setValue(5);
        TextArea comentario = new TextArea();
        comentario.setPromptText("Escribe tu reseña...");
        comentario.setPrefRowCount(3);

        Button publicar = boton("Publicar reseña", "button-gold");
        Label promedio = new Label("Selecciona una película para ver sus reseñas.");
        promedio.getStyleClass().add("muted-small");

        TableView<ResenaPelicula> tabla = tabla();
        tabla.getColumns().addAll(
                columna("Cliente", r -> r.getCliente().getNombre()),
                columna("Calificación", r -> r.getCalificacion() + "/5"),
                columna("Comentario", ResenaPelicula::getComentario),
                columna("Fecha", r -> r.getFechaPublicacion().toString())
        );

        Runnable cargar = () -> {
            if (pelicula.getValue() == null) return;
            refrescar(tabla, operaciones.consultarResenas(pelicula.getValue()));
            promedio.setText(String.format(Locale.ROOT,
                    "Promedio: %.1f / 5", operaciones.promedioResenas(pelicula.getValue())));
        };
        pelicula.setOnAction(e -> cargar.run());

        publicar.setOnAction(e -> {
            if (pelicula.getValue() == null) { aviso("Selecciona una película."); return; }
            try {
                operaciones.publicarResena(cliente, pelicula.getValue(),
                        calificacion.getValue(), comentario.getText());
                comentario.clear();
                cargar.run();
                aviso("Reseña publicada.");
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });

        cargar.run();
        root.getChildren().addAll(barra(pelicula, calificacion, publicar),
                comentario, promedio, tabla);
        return root;
    }

    private Node vistaPerfil() {
        VBox root = pagina("Mi perfil", "Consulta y actualiza tus datos personales.");

        TextField id = new TextField(cliente.getId());
        id.setEditable(false);
        TextField nombre = new TextField(cliente.getNombre());
        TextField correo = new TextField(cliente.getCorreo());

        GridPane grid = formulario();
        fila(grid, 0, "Identificación", id);
        fila(grid, 1, "Nombre", nombre);
        fila(grid, 2, "Correo electrónico", correo);

        Button guardar = boton("Guardar cambios", "button-gold");
        guardar.setOnAction(e -> {
            try {
                operaciones.actualizarPerfil(cliente, nombre.getText(), correo.getText());
                aviso("Perfil actualizado.");
            } catch (RuntimeException ex) {
                mostrarError(ex);
            }
        });

        root.getChildren().addAll(grid, guardar);
        return root;
    }

    private Node vistaNotificaciones() {
        VBox root = pagina("Notificaciones", "Mensajes y avisos asociados a tus compras.");
        ListView<String> lista = new ListView<>(
                FXCollections.observableArrayList(operaciones.consultarNotificaciones(cliente)));
        lista.setPrefHeight(520);
        if (lista.getItems().isEmpty()) {
            lista.getItems().add("Todavía no tienes notificaciones.");
        }
        root.getChildren().add(lista);
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
        combo.setCellFactory(list -> new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : texto.apply(item));
            }
        });
        combo.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : texto.apply(item));
            }
        });
        combo.setMaxWidth(Double.MAX_VALUE);
    }

    private void aviso(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (owner != null) alert.initOwner(owner);
        alert.setTitle("CinemaUQ");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarError(Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        if (owner != null) alert.initOwner(owner);
        alert.setTitle("CinemaUQ");
        alert.setHeaderText("No se pudo completar la operación");
        alert.setContentText(ex.getMessage() == null ? ex.toString() : ex.getMessage());
        alert.showAndWait();
    }

    private String moneda(BigDecimal valor) {
        return java.text.NumberFormat.getCurrencyInstance(new Locale("es", "CO"))
                .format(valor);
    }

    private String mascara(String numero) {
        return numero.length() <= 4 ? numero
                : "•••• •••• " + numero.substring(numero.length() - 4);
    }
}
