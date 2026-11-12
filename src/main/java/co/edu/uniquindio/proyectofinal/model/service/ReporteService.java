package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.enums.*;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoVendible;
import co.edu.uniquindio.proyectofinal.model.interfaces.ExportadorReporte;
import co.edu.uniquindio.proyectofinal.model.patrones.estructurales.decorator.ElementoVendibleDecorador;
import co.edu.uniquindio.proyectofinal.model.principal.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/** Construye reportes operativos y los delega al exportador seleccionado. */
public class ReporteService {

    /** Las ventas se calculan únicamente con compras efectivamente pagadas. */
    public String generarReporteVentas(
            Collection<Compra> compras, LocalDate desde, LocalDate hasta) {
        validarColeccionYPeriodo(compras, desde, hasta);

        List<Compra> filtradas = compras.stream()
                .filter(Objects::nonNull)
                .filter(this::compraPagada)
                .filter(compra -> dentroDelPeriodo(compra.getFecha(), desde, hasta))
                .toList();

        BigDecimal total = filtradas.stream()
                .map(Compra::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder texto = new StringBuilder(cabecera("REPORTE DE VENTAS", desde, hasta));
        texto.append("Compras pagadas: ").append(filtradas.size()).append('\n');
        for (Compra compra : filtradas) {
            texto.append(compra.getCodigo())
                    .append(" | Cliente: ").append(compra.getCliente().getId())
                    .append(" | Fecha: ").append(compra.getFecha())
                    .append(" | Total: $").append(compra.getTotal()).append('\n');
        }
        texto.append("Total de ventas: $").append(total);
        return texto.toString();
    }

    /** Reporta las entradas vendidas agrupadas por película. */
    public String generarReporteEntradasPorPelicula(
            Collection<Compra> compras, LocalDate desde, LocalDate hasta) {
        validarColeccionYPeriodo(compras, desde, hasta);
        Map<String, Integer> cantidades = new TreeMap<>();

        for (Compra compra : comprasPagadasEnPeriodo(compras, desde, hasta)) {
            for (DetalleCompra detalle : compra.getDetalles()) {
                ElementoVendible elemento = desenvolver(detalle.getElemento());
                if (elemento instanceof Entrada entrada) {
                    String clave = entrada.getFuncion().getPelicula().getCodigo()
                            + " - " + entrada.getFuncion().getPelicula().getTitulo();
                    cantidades.merge(clave, detalle.getCantidad(), Integer::sum);
                }
            }
        }

        StringBuilder texto = new StringBuilder(
                cabecera("REPORTE DE ENTRADAS VENDIDAS POR PELÍCULA", desde, hasta));
        cantidades.forEach((pelicula, cantidad) -> texto.append(pelicula)
                .append(": ").append(cantidad).append(" entradas\n"));
        texto.append("Total de entradas vendidas: ")
                .append(cantidades.values().stream().mapToInt(Integer::intValue).sum());
        return texto.toString();
    }

    /** Reporta las entradas vendidas agrupadas por función. */
    public String generarReporteEntradasPorFuncion(
            Collection<Compra> compras, LocalDate desde, LocalDate hasta) {
        validarColeccionYPeriodo(compras, desde, hasta);
        Map<String, Integer> cantidades = new TreeMap<>();

        for (Compra compra : comprasPagadasEnPeriodo(compras, desde, hasta)) {
            for (DetalleCompra detalle : compra.getDetalles()) {
                ElementoVendible elemento = desenvolver(detalle.getElemento());
                if (elemento instanceof Entrada entrada) {
                    Funcion funcion = entrada.getFuncion();
                    String clave = funcion.getCodigo() + " - "
                            + funcion.getPelicula().getTitulo() + " - "
                            + funcion.getFechaHora();
                    cantidades.merge(clave, detalle.getCantidad(), Integer::sum);
                }
            }
        }

        StringBuilder texto = new StringBuilder(
                cabecera("REPORTE DE ENTRADAS VENDIDAS POR FUNCIÓN", desde, hasta));
        cantidades.forEach((funcion, cantidad) -> texto.append(funcion)
                .append(": ").append(cantidad).append(" entradas\n"));
        texto.append("Total de entradas vendidas: ")
                .append(cantidades.values().stream().mapToInt(Integer::intValue).sum());
        return texto.toString();
    }

    /** Reporta la ocupación por función; no cuenta entradas anuladas. */
    public String generarReporteOcupacion(
            Collection<Funcion> funciones, Collection<Entrada> entradas) {
        Objects.requireNonNull(funciones, "La colección de funciones es obligatoria.");
        Objects.requireNonNull(entradas, "La colección de entradas es obligatoria.");

        StringBuilder texto = new StringBuilder(cabeceraSinPeriodo("REPORTE DE OCUPACIÓN"));
        int totalVendidas = 0;
        int totalCapacidad = 0;

        for (Funcion funcion : funciones) {
            if (funcion == null) {
                continue;
            }
            long vendidas = entradas.stream()
                    .filter(Objects::nonNull)
                    .filter(entrada -> entrada.getFuncion() != null
                            && Objects.equals(entrada.getFuncion().getCodigo(), funcion.getCodigo()))
                    .filter(entrada -> entrada.getEstado() != EstadoEntrada.ANULADA)
                    .count();

            int capacidad = funcion.getSala().calcularCapacidad();
            totalVendidas += (int) vendidas;
            totalCapacidad += capacidad;

            texto.append(funcion.getCodigo()).append(" - ")
                    .append(funcion.getPelicula().getTitulo()).append(": ")
                    .append(vendidas).append('/').append(capacidad)
                    .append(" entradas; ocupación: ")
                    .append(capacidad == 0 ? "0.00" : porcentaje(vendidas, capacidad))
                    .append(" %\n");
        }

        texto.append("Total: ").append(totalVendidas).append('/')
                .append(totalCapacidad).append(" entradas; ocupación general: ")
                .append(totalCapacidad == 0 ? "0.00" : porcentaje(totalVendidas, totalCapacidad))
                .append(" %");
        return texto.toString();
    }

    /** Reporta productos individuales y combos vendidos en compras pagadas. */
    public String generarReporteProductos(Collection<Compra> compras) {
        Objects.requireNonNull(compras, "La colección de compras es obligatoria.");
        return generarReporteProductosInterno(compras, null, null);
    }

    public String generarReporteProductos(
            Collection<Compra> compras, LocalDate desde, LocalDate hasta) {
        validarColeccionYPeriodo(compras, desde, hasta);
        return generarReporteProductosInterno(compras, desde, hasta);
    }

    private String generarReporteProductosInterno(
            Collection<Compra> compras, LocalDate desde, LocalDate hasta) {
        Map<String, Integer> cantidades = new TreeMap<>();

        for (Compra compra : comprasFiltradas(compras, desde, hasta)) {
            for (DetalleCompra detalle : compra.getDetalles()) {
                ElementoVendible elemento = desenvolver(detalle.getElemento());
                if (elemento instanceof ElementoConfiteria) {
                    cantidades.merge(elemento.consultarDescripcion(),
                            detalle.getCantidad(), Integer::sum);
                }
            }
        }

        String encabezado = desde == null
                ? cabeceraSinPeriodo("REPORTE DE PRODUCTOS Y COMBOS VENDIDOS")
                : cabecera("REPORTE DE PRODUCTOS Y COMBOS VENDIDOS", desde, hasta);
        StringBuilder texto = new StringBuilder(encabezado);
        cantidades.forEach((nombre, cantidad) -> texto.append(nombre)
                .append(": ").append(cantidad).append(" unidades vendidas\n"));
        texto.append("Total de unidades vendidas: ")
                .append(cantidades.values().stream().mapToInt(Integer::intValue).sum());
        return texto.toString();
    }

    /** Reporta las recargas encontradas en los movimientos de todas las tarjetas. */
    public String generarReporteRecargas(
            Collection<TarjetaVirtual> tarjetas, LocalDate desde, LocalDate hasta) {
        validarColeccionYPeriodo(tarjetas, desde, hasta);
        StringBuilder texto = new StringBuilder(
                cabecera("REPORTE DE RECARGAS", desde, hasta));
        int operaciones = 0;
        BigDecimal total = BigDecimal.ZERO;

        for (TarjetaVirtual tarjeta : tarjetas) {
            if (tarjeta == null) {
                continue;
            }
            for (MovimientoTarjeta movimiento : tarjeta.getMovimientos()) {
                if (movimiento.getTipo() == TipoMovimientoTarjeta.RECARGA
                        && dentroDelPeriodo(movimiento.getFecha(), desde, hasta)) {
                    operaciones++;
                    total = total.add(movimiento.getValor());
                    texto.append(movimiento.getFecha())
                            .append(" | Tarjeta: ").append(tarjeta.getNumero())
                            .append(" | Cliente: ").append(tarjeta.getTitular().getId())
                            .append(" | Responsable: ").append(movimiento.getResponsableId())
                            .append(" | Valor: $").append(movimiento.getValor()).append('\n');
                }
            }
        }

        texto.append("Recargas encontradas: ").append(operaciones)
                .append("\nTotal recargado: $").append(total);
        return texto.toString();
    }

    /** Reporta cancelaciones y reembolsos asociados en el periodo indicado. */
    public String generarReporteCancelacionesYReembolsos(
            Collection<Cancelacion> cancelaciones,
            Collection<Reembolso> reembolsos,
            LocalDate desde, LocalDate hasta) {
        validarColeccionYPeriodo(cancelaciones, desde, hasta);
        validarColeccionYPeriodo(reembolsos, desde, hasta);

        StringBuilder texto = new StringBuilder(
                cabecera("REPORTE DE CANCELACIONES Y REEMBOLSOS", desde, hasta));
        int solicitudes = 0;
        int aprobadas = 0;
        int rechazadas = 0;
        int procesadas = 0;

        for (Cancelacion cancelacion : cancelaciones) {
            if (cancelacion == null
                    || !dentroDelPeriodo(cancelacion.getFecha(), desde, hasta)) {
                continue;
            }
            solicitudes++;
            if (cancelacion.getEstado() == EstadoCancelacion.APROBADA) aprobadas++;
            if (cancelacion.getEstado() == EstadoCancelacion.RECHAZADA) rechazadas++;
            if (cancelacion.getEstado() == EstadoCancelacion.PROCESADA) procesadas++;

            texto.append("Cancelación ").append(cancelacion.getCodigo())
                    .append(" | Compra: ").append(cancelacion.getCompra().getCodigo())
                    .append(" | Estado: ").append(cancelacion.getEstado()).append('\n');
        }

        int reembolsosProcesados = 0;
        BigDecimal totalReembolsado = BigDecimal.ZERO;
        for (Reembolso reembolso : reembolsos) {
            if (reembolso == null
                    || !dentroDelPeriodo(reembolso.getFecha(), desde, hasta)) {
                continue;
            }
            if (reembolso.getEstado() == EstadoReembolso.PROCESADO) {
                reembolsosProcesados++;
                totalReembolsado = totalReembolsado.add(reembolso.getValor());
            }
            texto.append("Reembolso ").append(reembolso.getCodigo())
                    .append(" | Estado: ").append(reembolso.getEstado())
                    .append(" | Valor: $").append(reembolso.getValor()).append('\n');
        }

        texto.append("Solicitudes de cancelación: ").append(solicitudes)
                .append("\nAprobadas: ").append(aprobadas)
                .append("\nRechazadas: ").append(rechazadas)
                .append("\nCancelaciones procesadas: ").append(procesadas)
                .append("\nReembolsos procesados: ").append(reembolsosProcesados)
                .append("\nTotal reembolsado: $").append(totalReembolsado);
        return texto.toString();
    }

    /** Mantiene el reporte histórico de reembolsos para no romper llamadas existentes. */
    public String generarReporteReembolsos(Collection<Reembolso> reembolsos) {
        Objects.requireNonNull(reembolsos, "La colección de reembolsos es obligatoria.");
        StringBuilder texto = new StringBuilder(cabeceraSinPeriodo("REPORTE DE REEMBOLSOS"));
        int procesados = 0;
        BigDecimal total = BigDecimal.ZERO;
        for (Reembolso reembolso : reembolsos) {
            if (reembolso != null && reembolso.getEstado() == EstadoReembolso.PROCESADO) {
                procesados++;
                total = total.add(reembolso.getValor());
            }
        }
        texto.append("Reembolsos procesados: ").append(procesados)
                .append("\nValor total procesado: $").append(total);
        return texto.toString();
    }

    /** Reporta puntos acumulados y redimidos por cada cliente en el periodo. */
    public String generarReportePuntos(Collection<CuentaPuntos> cuentas) {
        Objects.requireNonNull(cuentas, "La colección de cuentas es obligatoria.");
        return generarReportePuntosInterno(cuentas, null, null);
    }

    public String generarReportePuntos(
            Collection<CuentaPuntos> cuentas, LocalDate desde, LocalDate hasta) {
        validarColeccionYPeriodo(cuentas, desde, hasta);
        return generarReportePuntosInterno(cuentas, desde, hasta);
    }

    private String generarReportePuntosInterno(
            Collection<CuentaPuntos> cuentas, LocalDate desde, LocalDate hasta) {
        String encabezado = desde == null
                ? cabeceraSinPeriodo("REPORTE DE PUNTOS ACUMULADOS Y REDIMIDOS")
                : cabecera("REPORTE DE PUNTOS ACUMULADOS Y REDIMIDOS", desde, hasta);
        StringBuilder texto = new StringBuilder(encabezado);
        int totalAcumulados = 0;
        int totalRedimidos = 0;

        for (CuentaPuntos cuenta : cuentas) {
            if (cuenta == null) {
                continue;
            }
            int acumuladosCliente = 0;
            int redimidosCliente = 0;
            for (MovimientoPuntos movimiento : cuenta.getMovimientos()) {
                if (desde != null && !dentroDelPeriodo(movimiento.getFecha(), desde, hasta)) {
                    continue;
                }
                if (movimiento.getTipo() == TipoMovimientoPuntos.ACUMULACION) {
                    acumuladosCliente += movimiento.getCantidad();
                } else if (movimiento.getTipo() == TipoMovimientoPuntos.REDENCION) {
                    redimidosCliente += movimiento.getCantidad();
                }
            }
            totalAcumulados += acumuladosCliente;
            totalRedimidos += redimidosCliente;
            texto.append("Cliente: ").append(cuenta.getTitular().getId())
                    .append(" - ").append(cuenta.getTitular().getNombre())
                    .append(" | Acumulados: ").append(acumuladosCliente)
                    .append(" | Redimidos: ").append(redimidosCliente)
                    .append(" | Saldo vigente: ")
                    .append(cuenta.consultarSaldo(LocalDate.now())).append('\n');
        }

        texto.append("Total acumulado: ").append(totalAcumulados)
                .append(" puntos\nTotal redimido: ").append(totalRedimidos)
                .append(" puntos");
        return texto.toString();
    }

    public void exportar(String contenido, Path destino,
                         ExportadorReporte exportador) throws IOException {
        Objects.requireNonNull(contenido, "El contenido del reporte es obligatorio.");
        Objects.requireNonNull(destino, "La ruta de destino es obligatoria.");
        Objects.requireNonNull(exportador, "El exportador es obligatorio.");
        exportador.exportar(contenido, destino);
    }

    private List<Compra> comprasPagadasEnPeriodo(
            Collection<Compra> compras, LocalDate desde, LocalDate hasta) {
        return comprasFiltradas(compras, desde, hasta);
    }

    private List<Compra> comprasFiltradas(
            Collection<Compra> compras, LocalDate desde, LocalDate hasta) {
        return compras.stream()
                .filter(Objects::nonNull)
                .filter(this::compraPagada)
                .filter(compra -> desde == null
                        || dentroDelPeriodo(compra.getFecha(), desde, hasta))
                .toList();
    }

    private boolean compraPagada(Compra compra) {
        return "PAGADA".equals(compra.consultarNombreEstado());
    }

    private boolean dentroDelPeriodo(LocalDateTime fecha, LocalDate desde, LocalDate hasta) {
        LocalDate dia = fecha.toLocalDate();
        return !dia.isBefore(desde) && !dia.isAfter(hasta);
    }

    private ElementoVendible desenvolver(ElementoVendible elemento) {
        ElementoVendible actual = Objects.requireNonNull(elemento);
        Set<ElementoVendible> visitados = Collections.newSetFromMap(new IdentityHashMap<>());
        while (actual instanceof ElementoVendibleDecorador decorador) {
            if (!visitados.add(actual)) {
                throw new IllegalStateException("Se detectó un ciclo en los decoradores.");
            }
            actual = decorador.obtenerElementoOriginal();
        }
        return actual;
    }

    private void validarColeccionYPeriodo(
            Collection<?> coleccion, LocalDate desde, LocalDate hasta) {
        Objects.requireNonNull(coleccion, "La colección de datos es obligatoria.");
        Objects.requireNonNull(desde, "La fecha inicial es obligatoria.");
        Objects.requireNonNull(hasta, "La fecha final es obligatoria.");
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException(
                    "La fecha final no puede ser anterior a la fecha inicial.");
        }
    }

    private String cabecera(String titulo, LocalDate desde, LocalDate hasta) {
        return titulo + "\nFecha de generación: " + LocalDateTime.now()
                + "\nPeriodo: " + desde + " a " + hasta + "\n";
    }

    private String cabeceraSinPeriodo(String titulo) {
        return titulo + "\nFecha de generación: " + LocalDateTime.now() + "\n";
    }

    private String porcentaje(long parte, long total) {
        return BigDecimal.valueOf(parte)
                .multiply(new BigDecimal("100"))
                .divide(BigDecimal.valueOf(total), 2, java.math.RoundingMode.HALF_UP)
                .toPlainString();
    }
}
