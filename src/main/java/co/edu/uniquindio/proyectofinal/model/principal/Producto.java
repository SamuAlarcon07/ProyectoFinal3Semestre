package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoProducto;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

/** Representa un producto individual del catálogo de confitería. */
public class Producto implements ElementoConfiteria {

    private final String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private EstadoProducto estado;
    private final Inventario inventario;

    public Producto(String codigo, String nombre, String descripcion,
                    BigDecimal precio, int stockInicial,
                    int stockMinimo, String unidadMedida) {
        this.codigo = validarTexto(codigo, "El código");
        actualizarInformacion(nombre, descripcion, precio);
        this.estado = EstadoProducto.ACTIVO;
        this.inventario = new Inventario(stockInicial, stockMinimo, unidadMedida);
    }

    /** Actualiza la información comercial del producto. */
    public void actualizarInformacion(String nombre, String descripcion, BigDecimal precio) {
        this.nombre = validarTexto(nombre, "El nombre");
        this.descripcion = validarTexto(descripcion, "La descripción");
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        this.precio = precio;
    }

    public void activar() { estado = EstadoProducto.ACTIVO; }
    public void desactivar() { estado = EstadoProducto.INACTIVO; }
    public boolean estaActivo() { return estado == EstadoProducto.ACTIVO; }
    public boolean estaDisponible(int cantidad) { return estaActivo() && inventario.verificarDisponibilidad(cantidad); }

    @Override
    public BigDecimal consultarPrecio() { return precio; }

    @Override
    public String consultarDescripcion() { return nombre + " - " + descripcion; }

    @Override
    public Map<Producto, Integer> obtenerProductosBase() { return Map.of(this, 1); }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " no puede estar vacío.");
        return valor.trim();
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public EstadoProducto getEstado() { return estado; }
    public Inventario getInventario() { return inventario; }
}