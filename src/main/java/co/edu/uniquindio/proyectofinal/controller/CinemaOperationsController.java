package co.edu.uniquindio.proyectofinal.controller;

import co.edu.uniquindio.proyectofinal.model.enums.*;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.state.CompraPendiente;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.strategy.CancelacionEntre2y24Horas;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.strategy.CancelacionMas24Horas;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.strategy.CancelacionNoPermitida;
import co.edu.uniquindio.proyectofinal.model.patrones.comportamiento.strategy.CancelacionPorCine;
import co.edu.uniquindio.proyectofinal.model.patrones.creacionales.builder.CompraBuilder;
import co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator.ElementoVendibleDecorador;
import co.edu.uniquindio.proyectofinal.model.principal.*;
import co.edu.uniquindio.proyectofinal.model.service.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador de aplicación para las operaciones administrativas y del cliente.
 * No contiene referencias a componentes JavaFX.
 */
public class CinemaOperationsController {

    private final AdministracionCatalogoService catalogo;
    private final FuncionService funciones;
    private final CineService cine;
    private final GestionSalasService salas;
    private final BloqueoSalaService bloqueos;
    private final AutenticacionService autenticacion;
    private final InventarioService inventario;
    private final PromocionService promociones;
    private final ControlAccesoService acceso;
    private final ListaEsperaService listaEspera;
    private final ReporteService reportes;
    private final CompraService compras;
    private final TarjetaVirtualService tarjetas;
    private final PuntosService puntos;
    private final ListaPersonalService listasPersonales;
    private final ResenaService resenas;
    private final CancelacionService cancelaciones;
    private final NotificacionService notificaciones;
    private final ConfiteriaService confiteria;

    public CinemaOperationsController(
            AdministracionCatalogoService catalogo,
            FuncionService funciones,
            CineService cine,
            GestionSalasService salas,
            BloqueoSalaService bloqueos,
            AutenticacionService autenticacion,
            InventarioService inventario,
            PromocionService promociones,
            ControlAccesoService acceso,
            ListaEsperaService listaEspera,
            ReporteService reportes,
            CompraService compras,
            TarjetaVirtualService tarjetas,
            PuntosService puntos,
            ListaPersonalService listasPersonales,
            ResenaService resenas,
            CancelacionService cancelaciones,
            NotificacionService notificaciones,
            ConfiteriaService confiteria
    ) {
        this.catalogo = Objects.requireNonNull(catalogo);
        this.funciones = Objects.requireNonNull(funciones);
        this.cine = Objects.requireNonNull(cine);
        this.salas = Objects.requireNonNull(salas);
        this.bloqueos = Objects.requireNonNull(bloqueos);
        this.autenticacion = Objects.requireNonNull(autenticacion);
        this.inventario = Objects.requireNonNull(inventario);
        this.promociones = Objects.requireNonNull(promociones);
        this.acceso = Objects.requireNonNull(acceso);
        this.listaEspera = Objects.requireNonNull(listaEspera);
        this.reportes = Objects.requireNonNull(reportes);
        this.compras = Objects.requireNonNull(compras);
        this.tarjetas = Objects.requireNonNull(tarjetas);
        this.puntos = Objects.requireNonNull(puntos);
        this.listasPersonales = Objects.requireNonNull(listasPersonales);
        this.resenas = Objects.requireNonNull(resenas);
        this.cancelaciones = Objects.requireNonNull(cancelaciones);
        this.notificaciones = Objects.requireNonNull(notificaciones);
        this.confiteria = Objects.requireNonNull(confiteria);
    }

    // ---------------------------------------------------------------------
    // CATÁLOGO DE PELÍCULAS
    // ---------------------------------------------------------------------

    public List<Pelicula> consultarPeliculas() {
        return catalogo.consultarPeliculas();
    }

    public List<Pelicula> consultarPeliculasActivas() {
        return catalogo.consultarPeliculasActivas();
    }

    public Pelicula registrarPelicula(String codigo, String titulo,
                                      String sinopsis, int duracion,
                                      String clasificacion, String genero) {
        Pelicula pelicula = new Pelicula(
                codigo, titulo, sinopsis, duracion, clasificacion, genero);
        catalogo.registrarPelicula(pelicula);
        return pelicula;
    }

    public void actualizarPelicula(String codigo, String titulo,
                                   String sinopsis, int duracion,
                                   String clasificacion, String genero) {
        Pelicula pelicula = catalogo.buscarPelicula(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Película no encontrada."));
        pelicula.actualizarInformacion(
                titulo, sinopsis, duracion, clasificacion, genero);
    }

    public void cambiarEstadoPelicula(String codigo, boolean activa) {
        Pelicula pelicula = catalogo.buscarPelicula(codigo)
                .orElseThrow(() -> new IllegalArgumentException("Película no encontrada."));
        if (activa) pelicula.activar();
        else pelicula.desactivar();
    }

    // ---------------------------------------------------------------------
    // SALAS Y FUNCIONES
    // ---------------------------------------------------------------------

    public List<TipoSala> consultarTiposSala() {
        return salas.consultarTiposSala();
    }

    public List<Sala> consultarSalas() {
        return cine.consultarSalas();
    }

    public TipoSala registrarTipoSala(String codigo, String nombre,
                                      String descripcion, Set<FormatoProyeccion> formatos,
                                      boolean gafas3D, boolean movimiento, boolean efectos) {
        TipoSala tipo = new TipoSala(
                codigo, nombre, descripcion, formatos, gafas3D, movimiento, efectos);
        salas.registrarTipoSala(tipo);
        return tipo;
    }

    public Sala registrarSala(String codigo, String nombre, TipoSala tipo,
                              int filas, int asientosPorFila,
                              TipoAsiento tipoAsiento) {
        if (filas < 1 || filas > 26 || asientosPorFila < 1 || asientosPorFila > 30) {
            throw new IllegalArgumentException(
                    "La sala debe tener entre 1 y 26 filas y entre 1 y 30 asientos por fila.");
        }
        Sala sala = new Sala(codigo, nombre, tipo);
        for (int fila = 0; fila < filas; fila++) {
            String letra = String.valueOf((char) ('A' + fila));
            for (int numero = 1; numero <= asientosPorFila; numero++) {
                sala.agregarAsiento(new Asiento(letra, numero, tipoAsiento));
            }
        }
        salas.registrarSala(sala);
        return sala;
    }

    public List<Funcion> consultarFunciones() {
        return funciones.consultarFunciones();
    }

    public Funcion programarFuncion(String codigo, Pelicula pelicula, Sala sala,
                                    LocalDateTime fechaHora, BigDecimal precio,
                                    FormatoProyeccion formato) {
        Funcion funcion = new Funcion(
                codigo, pelicula, sala, fechaHora, precio, formato);
        funciones.registrarFuncion(funcion);
        return funcion;
    }

    public void cancelarFuncion(String codigo) {
        funciones.cancelarFuncion(codigo);
    }

    public void finalizarFuncion(String codigo) {
        funciones.finalizarFuncion(codigo);
    }

    public BloqueoSala registrarBloqueo(String codigo, Sala sala,
                                        TipoBloqueoSala tipo,
                                        LocalDateTime inicio, LocalDateTime fin,
                                        String motivo, Administrador administrador) {
        BloqueoSala bloqueo = new BloqueoSala(
                codigo, sala, tipo, inicio, fin, motivo, administrador);
        // Valida antes de registrar en cualquiera de los servicios para evitar
        // que un fallo deje el bloqueo guardado solo en una de las colecciones.
        funciones.validarBloqueo(bloqueo);
        bloqueos.registrarBloqueo(bloqueo);
        funciones.registrarBloqueo(bloqueo);
        return bloqueo;
    }

    public List<BloqueoSala> consultarBloqueos() {
        return bloqueos.consultarTodos();
    }

    public void cancelarBloqueo(String codigo) {
        bloqueos.cancelarBloqueo(codigo);
    }

    // ---------------------------------------------------------------------
    // USUARIOS
    // ---------------------------------------------------------------------

    public List<Usuario> consultarUsuarios() {
        return List.copyOf(autenticacion.consultarUsuarios());
    }

    public void cambiarEstadoUsuario(String id, boolean activo) {
        Usuario usuario = autenticacion.buscarUsuario(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado."));
        if (activo) autenticacion.activarUsuario(id);
        else autenticacion.bloquearUsuario(id);
    }

    public void actualizarPerfil(Usuario usuario, String nombre, String correo) {
        Objects.requireNonNull(usuario);
        usuario.actualizarDatos(nombre, correo);
    }

    // ---------------------------------------------------------------------
    // INVENTARIO Y PRODUCTOS
    // ---------------------------------------------------------------------

    public List<Producto> consultarProductos() {
        return catalogo.consultarProductos();
    }

    public List<Combo> consultarCombos() {
        return confiteria.consultarCombos();
    }

    public List<Combo> consultarCombosActivos() {
        return confiteria.consultarCombosActivos();
    }

    public Combo registrarCombo(String codigo, String nombre, BigDecimal precio,
                                Map<String, Integer> componentes) {
        if (componentes == null || componentes.isEmpty()) {
            throw new IllegalArgumentException("Agrega al menos un componente al combo.");
        }
        Combo combo = new Combo(codigo, nombre, precio);
        for (Map.Entry<String, Integer> entrada : componentes.entrySet()) {
            String codigoElemento = entrada.getKey();
            int cantidad = entrada.getValue();
            ElementoConfiteria elemento = catalogo.buscarProducto(codigoElemento)
                    .<ElementoConfiteria>map(producto -> producto)
                    .orElseGet(() -> confiteria.buscarCombo(codigoElemento)
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "No existe producto o combo con código " + codigoElemento)));
            combo.agregarComponente(elemento, cantidad);
        }
        return confiteria.registrarCombo(combo);
    }

    public void cambiarEstadoCombo(String codigo, boolean activo) {
        confiteria.cambiarEstado(codigo, activo);
    }

    /**
     * Compra productos y combos de confitería en una sola transacción de servicio.
     * Si falla el cobro después de redimir puntos, se reintegran mediante un nuevo lote.
     */
    public Compra comprarConfiteria(Cliente cliente,
                                    Map<? extends ElementoConfiteria, Integer> elementos,
                                    String codigoPromocion,
                                    int puntosARedimir) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        if (elementos == null || elementos.isEmpty()) {
            throw new IllegalArgumentException("El carrito está vacío.");
        }
        TarjetaVirtual tarjeta = tarjetas.buscarTarjetaPorCliente(cliente)
                .orElseThrow(() -> new IllegalStateException(
                        "No tienes una tarjeta virtual registrada."));

        String codigoCompra = "COMP-CONF-" + UUID.randomUUID()
                .toString().substring(0, 8).toUpperCase(Locale.ROOT);
        CompraBuilder builder = new CompraBuilder(
                codigoCompra, cliente, new CompraPendiente());

        int correlativo = 1;
        for (Map.Entry<? extends ElementoConfiteria, Integer> item : elementos.entrySet()) {
            ElementoConfiteria elemento = Objects.requireNonNull(item.getKey());
            Integer cantidad = item.getValue();
            if (cantidad == null || cantidad <= 0) {
                throw new IllegalArgumentException("Las cantidades deben ser mayores que cero.");
            }
            if (elemento instanceof Producto producto && !producto.estaActivo()) {
                throw new IllegalStateException("El producto " + producto.getNombre() + " está inactivo.");
            }
            if (elemento instanceof Combo combo && !combo.estaActivo()) {
                throw new IllegalStateException("El combo " + combo.getNombre() + " está inactivo.");
            }
            builder.agregarDetalle(new DetalleCompra(
                    "DET-" + correlativo++ + "-" + UUID.randomUUID()
                            .toString().substring(0, 6).toUpperCase(Locale.ROOT),
                    elemento, cantidad));
        }

        Compra compra = builder.build();
        if (codigoPromocion != null && !codigoPromocion.isBlank()) {
            promociones.aplicarPromocion(codigoPromocion.trim(), compra, LocalDate.now());
        }

        // Valida existencias antes de consumir puntos o intentar cobrar.
        if (!inventario.validarDisponibilidadCompra(compra)) {
            throw new IllegalStateException("No hay existencias suficientes para completar el pedido.");
        }

        BigDecimal valorPuntos = BigDecimal.ZERO;
        if (puntosARedimir > 0) {
            valorPuntos = puntos.redimirParaCompra(cliente, compra, puntosARedimir);
        }

        try {
            return compras.confirmarCompra(compra, tarjeta);
        } catch (RuntimeException ex) {
            if (puntosARedimir > 0 && valorPuntos.compareTo(BigDecimal.ZERO) > 0) {
                // Reintegro compensatorio: los puntos consumidos vuelven como un lote nuevo.
                puntos.acumularPuntos(cliente, puntosARedimir, LocalDate.now(),
                        "Reintegro por compra fallida " + codigoCompra);
            }
            throw ex;
        }
    }

    public Producto registrarProducto(String codigo, String nombre,
                                      String descripcion, BigDecimal precio,
                                      int stockInicial, int stockMinimo,
                                      String unidad) {
        Producto producto = new Producto(
                codigo, nombre, descripcion, precio, stockInicial, stockMinimo, unidad);
        catalogo.registrarProducto(producto);
        return producto;
    }

    public void actualizarProducto(Producto producto, String nombre,
                                   String descripcion, BigDecimal precio) {
        Objects.requireNonNull(producto);
        producto.actualizarInformacion(nombre, descripcion, precio);
    }

    public void cambiarEstadoProducto(Producto producto, boolean activo) {
        Objects.requireNonNull(producto);
        if (activo) producto.activar();
        else producto.desactivar();
    }

    public void registrarEntradaInventario(Producto producto, int cantidad,
                                           String observacion) {
        inventario.registrarEntrada(producto, cantidad, observacion);
    }

    public void registrarMermaInventario(Producto producto, int cantidad,
                                         String observacion) {
        inventario.registrarMerma(producto, cantidad, observacion);
    }

    public List<Producto> consultarProductosPorReponer() {
        return inventario.consultarProductosPorReponer(consultarProductos());
    }

    // ---------------------------------------------------------------------
    // PROMOCIONES
    // ---------------------------------------------------------------------

    public List<Promocion> consultarPromociones() {
        return promociones.consultarPromociones();
    }

    public Promocion registrarPromocion(String codigo, String nombre,
                                        String descripcion, BigDecimal porcentaje,
                                        LocalDate desde, LocalDate hasta) {
        Promocion promocion = new Promocion(
                codigo, nombre, descripcion, porcentaje, desde, hasta);
        promociones.registrarPromocion(promocion);
        return promocion;
    }

    public void cambiarEstadoPromocion(Promocion promocion, boolean activa) {
        Objects.requireNonNull(promocion);
        if (activa) promocion.activar();
        else promocion.desactivar();
    }

    // ---------------------------------------------------------------------
    // TARJETAS VIRTUALES Y RECARGAS
    // ---------------------------------------------------------------------

    public List<TarjetaVirtual> consultarTarjetas() {
        return tarjetas.consultarTarjetas();
    }

    public Optional<TarjetaVirtual> consultarTarjeta(Cliente cliente) {
        return tarjetas.buscarTarjetaPorCliente(cliente);
    }

    public void recargarTarjeta(TarjetaVirtual tarjeta, BigDecimal valor,
                                Administrador administrador) {
        tarjetas.recargar(tarjeta.getNumero(), valor, administrador);
    }

    /** Recarga simulada iniciada por el titular de la tarjeta virtual. */
    public void recargarTarjetaCliente(Cliente cliente, BigDecimal valor) {
        TarjetaVirtual tarjeta = tarjetas.buscarTarjetaPorCliente(cliente)
                .orElseThrow(() -> new IllegalStateException(
                        "No tienes una tarjeta virtual registrada."));
        tarjeta.recargarDesdeCliente(valor, cliente);
    }

    public void cambiarEstadoTarjeta(TarjetaVirtual tarjeta, boolean activa) {
        if (activa) tarjetas.activar(tarjeta.getNumero());
        else tarjetas.bloquear(tarjeta.getNumero());
    }

    // ---------------------------------------------------------------------
    // COMPRAS, ENTRADAS Y PUNTOS DEL CLIENTE
    // ---------------------------------------------------------------------

    public List<Compra> consultarCompras() {
        return compras.consultarCompras();
    }

    public List<Compra> consultarCompras(Cliente cliente) {
        return compras.consultarComprasCliente(cliente.getId());
    }

    public List<Entrada> consultarEntradas(Cliente cliente) {
        return extraerEntradas(consultarCompras(cliente));
    }

    public List<Entrada> consultarTodasLasEntradas() {
        return extraerEntradas(compras.consultarCompras());
    }

    private List<Entrada> extraerEntradas(Collection<Compra> origen) {
        List<Entrada> resultado = new ArrayList<>();
        for (Compra compra : origen) {
            for (DetalleCompra detalle : compra.getDetalles()) {
                ElementoVendible elemento = desenvolver(detalle.getElemento());
                if (elemento instanceof Entrada entrada) {
                    resultado.add(entrada);
                }
            }
        }
        return List.copyOf(resultado);
    }

    private ElementoVendible desenvolver(ElementoVendible elemento) {
        ElementoVendible actual = Objects.requireNonNull(elemento);
        Set<ElementoVendible> visitados =
                Collections.newSetFromMap(new IdentityHashMap<>());
        while (actual instanceof ElementoVendibleDecorador decorador) {
            if (!visitados.add(actual)) {
                throw new IllegalStateException("Ciclo en decoradores.");
            }
            actual = decorador.obtenerElementoOriginal();
        }
        return actual;
    }

    public int consultarPuntos(Cliente cliente) {
        return puntos.consultarSaldo(cliente, LocalDate.now());
    }

    public List<MovimientoPuntos> consultarMovimientosPuntos(Cliente cliente) {
        return puntos.buscarCuenta(cliente)
                .map(CuentaPuntos::getMovimientos)
                .orElse(List.of());
    }

    public BigDecimal consultarValorPunto() {
        return puntos.consultarValorPunto();
    }

    public BigDecimal redimirPuntos(Cliente cliente, Compra compra, int cantidad) {
        return puntos.redimirParaCompra(cliente, compra, cantidad);
    }

    // ---------------------------------------------------------------------
    // PELÍCULAS GUARDADAS Y RESEÑAS
    // ---------------------------------------------------------------------

    public List<PeliculaGuardada> consultarListaPersonal(
            Cliente cliente, TipoListaPelicula tipo) {
        return listasPersonales.consultarLista(cliente, tipo);
    }

    public PeliculaGuardada guardarPelicula(
            Cliente cliente, Pelicula pelicula, TipoListaPelicula tipo) {
        return listasPersonales.agregar(cliente, pelicula, tipo);
    }

    public void quitarPeliculaGuardada(
            Cliente cliente, Pelicula pelicula, TipoListaPelicula tipo) {
        listasPersonales.eliminar(cliente, pelicula, tipo);
    }

    public List<ResenaPelicula> consultarResenas(Pelicula pelicula) {
        return resenas.consultarResenas(pelicula);
    }

    public double promedioResenas(Pelicula pelicula) {
        return resenas.calcularPromedio(pelicula);
    }

    public ResenaPelicula publicarResena(
            Cliente cliente, Pelicula pelicula,
            int calificacion, String comentario) {
        return resenas.publicarResena(
                cliente, pelicula, calificacion, comentario,
                compras.consultarComprasCliente(cliente.getId()));
    }

    public void editarResena(
            Cliente cliente, Pelicula pelicula,
            int calificacion, String comentario) {
        resenas.editarResena(cliente, pelicula, calificacion, comentario);
    }

    public void ocultarResena(Cliente cliente, Pelicula pelicula) {
        resenas.ocultarResena(cliente, pelicula);
    }

    // ---------------------------------------------------------------------
    // CONTROL DE ACCESO Y NOTIFICACIONES
    // ---------------------------------------------------------------------

    public RegistroAcceso validarCodigoQR(String codigoQR) {
        return acceso.validarAcceso(codigoQR, LocalDateTime.now());
    }

    public List<RegistroAcceso> consultarRegistrosAcceso() {
        return acceso.consultarRegistros();
    }

    public List<String> consultarNotificaciones(Cliente cliente) {
        return notificaciones.consultarNotificaciones(cliente);
    }

    // ---------------------------------------------------------------------
    // LISTA DE ESPERA
    // ---------------------------------------------------------------------

    public SolicitudEspera solicitarEspera(
            Funcion funcion, Cliente cliente, int cantidad) {
        return listaEspera.registrarSolicitud(funcion, cliente, cantidad);
    }

    public List<SolicitudEspera> consultarSolicitudesEspera(Funcion funcion) {
        return listaEspera.consultarSolicitudes(funcion, LocalDateTime.now());
    }

    public List<SolicitudEspera> consultarSolicitudesEspera(Cliente cliente) {
        Objects.requireNonNull(cliente, "El cliente es obligatorio.");
        return funciones.consultarFunciones().stream()
                .flatMap(funcion -> listaEspera.consultarSolicitudes(funcion, LocalDateTime.now()).stream())
                .filter(solicitud -> solicitud.getCliente().getId().equals(cliente.getId()))
                .toList();
    }

    public void cancelarEsperaCliente(SolicitudEspera solicitud, Cliente cliente) {
        if (solicitud == null || cliente == null
                || !solicitud.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("La solicitud no pertenece al cliente.");
        }
        listaEspera.cancelarSolicitud(solicitud);
    }

    public int procesarOfertasVencidas() {
        return listaEspera.procesarOfertasVencidas(LocalDateTime.now());
    }

    public Optional<SolicitudEspera> notificarSiguiente(
            Funcion funcion, int asientosLiberados, int minutos) {
        return listaEspera.notificarSiguiente(
                funcion, asientosLiberados, minutos, LocalDateTime.now());
    }

    public void atenderSolicitud(SolicitudEspera solicitud) {
        listaEspera.atenderSolicitud(solicitud, LocalDateTime.now());
    }

    public void cancelarSolicitud(SolicitudEspera solicitud) {
        listaEspera.cancelarSolicitud(solicitud);
    }

    // ---------------------------------------------------------------------
    // CANCELACIONES Y REEMBOLSOS
    // ---------------------------------------------------------------------

    public Cancelacion solicitarCancelacion(Compra compra, String motivo) {
        List<Entrada> entradas = extraerEntradas(List.of(compra));
        if (entradas.isEmpty()) {
            throw new IllegalStateException(
                    "La compra no contiene entradas para una función.");
        }

        Funcion funcion = entradas.get(0).getFuncion();
        LocalDateTime ahora = LocalDateTime.now();

        co.edu.uniquindio.proyectofinal.model.interfaces.PoliticaCancelacion politica;
        if (funcion.getEstado() == EstadoFuncion.CANCELADA) {
            politica = new CancelacionPorCine();
        } else {
            java.time.Duration restante =
                    java.time.Duration.between(ahora, funcion.getFechaHora());
            if (restante.compareTo(java.time.Duration.ofHours(24)) > 0) {
                politica = new CancelacionMas24Horas();
            } else if (restante.compareTo(java.time.Duration.ofHours(2)) >= 0) {
                politica = new CancelacionEntre2y24Horas();
            } else {
                politica = new CancelacionNoPermitida();
            }
        }

        return cancelaciones.solicitarCancelacion(
                "CAN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                compra, funcion, motivo, politica, ahora);
    }

    public Optional<Reembolso> procesarReembolso(Cancelacion cancelacion, Cliente cliente) {
        TarjetaVirtual tarjeta = tarjetas.buscarTarjetaPorCliente(cliente)
                .orElseThrow(() -> new IllegalStateException(
                        "No existe tarjeta virtual para el cliente."));
        return cancelaciones.procesarReembolso(
                "REE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                cancelacion, tarjeta);
    }

    public List<Cancelacion> consultarCancelaciones() {
        return cancelaciones.consultarCancelaciones();
    }

    public List<Reembolso> consultarReembolsos() {
        return cancelaciones.consultarReembolsos();
    }

    // ---------------------------------------------------------------------
    // REPORTES
    // ---------------------------------------------------------------------

    public String generarReporte(String tipo, LocalDate desde, LocalDate hasta) {
        LocalDate inicio = Objects.requireNonNull(desde);
        LocalDate fin = Objects.requireNonNull(hasta);
        List<Compra> listaCompras = compras.consultarCompras();
        List<Funcion> listaFunciones = funciones.consultarFunciones();
        List<Entrada> listaEntradas = consultarTodasLasEntradas();

        switch (tipo) {
            case "Ventas":
                return reportes.generarReporteVentas(listaCompras, inicio, fin);
            case "Entradas por película":
                return reportes.generarReporteEntradasPorPelicula(listaCompras, inicio, fin);
            case "Entradas por función":
                return reportes.generarReporteEntradasPorFuncion(listaCompras, inicio, fin);
            case "Ocupación":
                return reportes.generarReporteOcupacion(listaFunciones, listaEntradas);
            case "Productos":
                return reportes.generarReporteProductos(listaCompras, inicio, fin);
            case "Recargas":
                return reportes.generarReporteRecargas(tarjetas.consultarTarjetas(), inicio, fin);
            case "Cancelaciones y reembolsos":
                return reportes.generarReporteCancelacionesYReembolsos(
                        cancelaciones.consultarCancelaciones(),
                        cancelaciones.consultarReembolsos(), inicio, fin);
            case "Puntos":
                List<CuentaPuntos> cuentas = autenticacion.consultarUsuarios()
                        .stream().filter(Cliente.class::isInstance)
                        .map(Cliente.class::cast)
                        .map(cliente -> puntos.buscarCuenta(cliente).orElse(null))
                        .filter(Objects::nonNull).toList();
                return reportes.generarReportePuntos(cuentas, inicio, fin);
            case "Reembolsos":
                return reportes.generarReporteReembolsos(
                        cancelaciones.consultarReembolsos());
            default:
                throw new IllegalArgumentException("Tipo de reporte no reconocido.");
        }
    }

    public List<Cancelacion> consultarCancelacionesCliente(Cliente cliente) {
        return cancelaciones.consultarCancelaciones().stream()
                .filter(c -> c.getCompra().getCliente().getId().equals(cliente.getId()))
                .toList();
    }
}
