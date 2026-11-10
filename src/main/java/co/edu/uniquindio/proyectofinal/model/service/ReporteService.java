package co.edu.uniquindio.proyectofinal.model.service;



import co.edu.uniquindio.proyectofinal.model.interfaces.ExportadorReporte;

import co.edu.uniquindio.proyectofinal.model.principal.Compra;

import co.edu.uniquindio.proyectofinal.model.principal.Entrada;

import co.edu.uniquindio.proyectofinal.model.principal.Funcion;

import co.edu.uniquindio.proyectofinal.model.principal.Producto;

import co.edu.uniquindio.proyectofinal.model.principal.Reembolso;



import java.io.IOException;

import java.math.BigDecimal;

import java.nio.file.Path;

import java.time.LocalDate;

import java.util.Collection;

import java.util.Map;

import java.util.stream.Collectors;



/** Construye reportes operativos y los delega al exportador seleccionado. */

public class ReporteService {



    public String generarReporteVentas(Collection<Compra> compras,

                                       LocalDate desde, LocalDate hasta) {

        var filtradas = compras.stream().filter(c ->

                !c.getFecha().toLocalDate().isBefore(desde)

                        && !c.getFecha().toLocalDate().isAfter(hasta)).toList();

        BigDecimal total = filtradas.stream().map(Compra::getTotal)

                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return "REPORTE DE VENTAS\nPeriodo: " + desde + " a " + hasta

                + "\nCompras registradas: " + filtradas.size()

                + "\nTotal registrado: $" + total;

    }



    public String generarReporteOcupacion(Collection<Funcion> funciones,

                                          Collection<Entrada> entradas) {

        StringBuilder texto = new StringBuilder("REPORTE DE OCUPACIÓN\n");

        for (Funcion funcion : funciones) {

            long vendidas = entradas.stream().filter(e ->

                    e.getFuncion().getCodigo().equals(funcion.getCodigo())

                            && e.getEstado() != co.edu.uniquindio.proyectofinal.model.enums.EstadoEntrada.ANULADA).count();

            texto.append(funcion.getCodigo()).append(" - ")

                    .append(funcion.getPelicula().getTitulo()).append(": ")

                    .append(vendidas).append("/")

                    .append(funcion.getSala().calcularCapacidad()).append(" entradas\n");

        }

        return texto.toString();

    }



    public String generarReporteProductos(Collection<Compra> compras) {

        Map<String, Integer> cantidades = compras.stream()

                .flatMap(c -> c.getDetalles().stream())

                .filter(d -> d.getElemento() instanceof co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria)

                .collect(Collectors.groupingBy(d -> d.getElemento().consultarDescripcion(),

                        Collectors.summingInt(d -> d.getCantidad())));

        StringBuilder texto = new StringBuilder("REPORTE DE PRODUCTOS\n");

        cantidades.forEach((nombre, cantidad) -> texto.append(nombre)

                .append(": ").append(cantidad).append(" unidades\n"));

        return texto.toString();

    }



    public String generarReporteReembolsos(Collection<Reembolso> reembolsos) {

        BigDecimal total = reembolsos.stream().filter(r ->

                        r.getEstado() == co.edu.uniquindio.proyectofinal.model.enums.EstadoReembolso.PROCESADO)

                .map(Reembolso::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

        return "REPORTE DE REEMBOLSOS\nOperaciones: " + reembolsos.size()

                + "\nValor procesado: $" + total;

    }



    public String generarReportePuntos(Collection<co.edu.uniquindio.proyectofinal.model.principal.CuentaPuntos> cuentas) {

        int saldoTotal = cuentas.stream().mapToInt(c -> c.consultarSaldo(LocalDate.now())).sum();

        return "REPORTE DE PUNTOS\nCuentas: " + cuentas.size()

                + "\nPuntos vigentes: " + saldoTotal;

    }



    public void exportar(String contenido, Path destino,

                         ExportadorReporte exportador) throws IOException {

        if (exportador == null) throw new IllegalArgumentException("El exportador es obligatorio.");

        exportador.exportar(contenido, destino);

    }

}