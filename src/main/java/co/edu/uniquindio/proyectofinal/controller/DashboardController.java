package co.edu.uniquindio.proyectofinal.controller;

import co.edu.uniquindio.proyectofinal.model.principal.*;
import co.edu.uniquindio.proyectofinal.model.service.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Controlador de aplicación que prepara los datos que necesita el dashboard.
 * Esta clase no conoce FXML ni controles JavaFX.
 */
public class DashboardController {

    private final AutenticacionService autenticacionService;
    private final AdministracionCatalogoService catalogoService;
    private final CarteleraService carteleraService;
    private final PuntosService puntosService;
    private final TarjetaVirtualService tarjetaVirtualService;

    public DashboardController(AutenticacionService autenticacionService,
                               AdministracionCatalogoService catalogoService,
                               CarteleraService carteleraService,
                               PuntosService puntosService,
                               TarjetaVirtualService tarjetaVirtualService) {
        this.autenticacionService = Objects.requireNonNull(autenticacionService);
        this.catalogoService = Objects.requireNonNull(catalogoService);
        this.carteleraService = Objects.requireNonNull(carteleraService);
        this.puntosService = Objects.requireNonNull(puntosService);
        this.tarjetaVirtualService = Objects.requireNonNull(tarjetaVirtualService);
    }

    public List<Pelicula> consultarPeliculasDestacadas() {
        return catalogoService.consultarPeliculasActivas()
                .stream()
                .limit(5)
                .toList();
    }

    public List<Pelicula> buscarPeliculas(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        return catalogoService.consultarPeliculasActivas()
                .stream()
                .filter(pelicula -> filtro.isBlank()
                        || pelicula.getTitulo().toLowerCase().contains(filtro)
                        || pelicula.getGenero().toLowerCase().contains(filtro)
                        || pelicula.getSinopsis().toLowerCase().contains(filtro))
                .toList();
    }

    public List<Funcion> consultarProximasFunciones() {
        LocalDateTime ahora = LocalDateTime.now();
        return carteleraService.consultarFuncionesEntre(
                ahora, ahora.plusDays(14)
        ).stream().limit(5).toList();
    }

    public int consultarPuntos(Usuario usuario) {
        if (!(usuario instanceof Cliente cliente)) {
            return 0;
        }
        return puntosService.consultarSaldo(cliente, LocalDate.now());
    }

    public Optional<TarjetaVirtual> consultarTarjeta(Usuario usuario) {
        if (!(usuario instanceof Cliente cliente)) {
            return Optional.empty();
        }
        return tarjetaVirtualService.buscarTarjetaPorCliente(cliente);
    }

    public int consultarTotalPeliculas() {
        return catalogoService.consultarPeliculasActivas().size();
    }

    public int consultarTotalUsuarios() {
        return autenticacionService.consultarUsuarios().size();
    }

    public int consultarTotalProductos() {
        return catalogoService.consultarProductos().size();
    }

    public int consultarTotalFuncionesProximas() {
        return consultarProximasFunciones().size();
    }
}
