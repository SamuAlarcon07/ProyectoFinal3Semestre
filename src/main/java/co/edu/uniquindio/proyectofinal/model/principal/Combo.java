package co.edu.uniquindio.proyectofinal.model.principal;

import co.edu.uniquindio.proyectofinal.model.enums.EstadoProducto;
import co.edu.uniquindio.proyectofinal.model.interfaces.ElementoConfiteria;

import java.math.BigDecimal;
import java.util.*;

/** Representa un combo compuesto por uno o varios elementos de confitería. */
public class Combo implements ElementoConfiteria {

    private final String codigo;
    private String nombre;
    private BigDecimal precio;
    private EstadoProducto estado;
    private final List<ComponenteCombo> componentes;

    public Combo(String codigo, String nombre, BigDecimal precio) {
        this.codigo = validarTexto(codigo, "El código");
        this.nombre = validarTexto(nombre, "El nombre");
        actualizarPrecio(precio);
        this.estado = EstadoProducto.ACTIVO;
        this.componentes = new ArrayList<>();
    }

    public void agregarComponente(ElementoConfiteria elemento, int cantidad) {
        if (elemento == null) throw new IllegalArgumentException("El elemento no puede ser nulo.");
        if (cantidad <= 0) throw new IllegalArgumentException("La cantidad debe ser positiva.");
        if (elemento == this) throw new IllegalArgumentException("Un combo no puede contenerse a sí mismo.");
        if (elemento instanceof Combo otro && otro.contieneCombo(this, Collections.newSetFromMap(new IdentityHashMap<>()))) {
            throw new IllegalArgumentException("La composición generaría una referencia circular entre combos.");
        }
        componentes.add(new ComponenteCombo(elemento, cantidad));
    }

    private boolean contieneCombo(Combo buscado, Set<Combo> visitados) {
        if (this == buscado) return true;
        if (!visitados.add(this)) return false;
        for (ComponenteCombo componente : componentes) {
            if (componente.getElemento() instanceof Combo combo
                    && combo.contieneCombo(buscado, visitados)) return true;
        }
        return false;
    }

    public void actualizarNombre(String nombre) { this.nombre = validarTexto(nombre, "El nombre"); }
    public void actualizarPrecio(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) throw new IllegalArgumentException("El precio debe ser mayor que cero.");
        this.precio = precio;
    }

    public void activar() { estado = EstadoProducto.ACTIVO; }
    public void desactivar() { estado = EstadoProducto.INACTIVO; }
    public boolean estaActivo() { return estado == EstadoProducto.ACTIVO; }

    @Override
    public BigDecimal consultarPrecio() { return precio; }

    @Override
    public String consultarDescripcion() { return "Combo: " + nombre; }

    /** Descompone recursivamente el combo en productos base y cantidades requeridas. */
    @Override
    public Map<Producto, Integer> obtenerProductosBase() {
        Map<Producto, Integer> resultado = new LinkedHashMap<>();
        for (ComponenteCombo componente : componentes) {
            for (Map.Entry<Producto, Integer> entrada : componente.getElemento().obtenerProductosBase().entrySet()) {
                resultado.merge(entrada.getKey(), entrada.getValue() * componente.getCantidad(), Integer::sum);
            }
        }
        return Collections.unmodifiableMap(resultado);
    }

    private static String validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw new IllegalArgumentException(campo + " no puede estar vacío.");
        return valor.trim();
    }

    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public BigDecimal getPrecio() { return precio; }
    public EstadoProducto getEstado() { return estado; }
    public List<ComponenteCombo> getComponentes() { return List.copyOf(componentes); }
}