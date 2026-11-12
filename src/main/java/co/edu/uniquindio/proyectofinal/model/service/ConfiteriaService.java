package co.edu.uniquindio.proyectofinal.model.service;

import co.edu.uniquindio.proyectofinal.model.principal.Combo;

import java.util.*;

/**
 * Administra los combos de confitería de CinemaUQ.
 * Los productos individuales permanecen en AdministracionCatalogoService.
 */
public class ConfiteriaService {

    private final Map<String, Combo> combos = new LinkedHashMap<>();

    public synchronized Combo registrarCombo(Combo combo) {
        Objects.requireNonNull(combo, "El combo es obligatorio.");
        if (combo.getComponentes().isEmpty()) {
            throw new IllegalArgumentException(
                    "El combo debe contener al menos un producto o combo.");
        }
        if (combos.putIfAbsent(combo.getCodigo(), combo) != null) {
            throw new IllegalArgumentException(
                    "Ya existe un combo con ese código.");
        }
        return combo;
    }

    public synchronized Optional<Combo> buscarCombo(String codigo) {
        if (codigo == null || codigo.isBlank()) return Optional.empty();
        return Optional.ofNullable(combos.get(codigo.trim()));
    }

    public synchronized List<Combo> consultarCombos() {
        return List.copyOf(combos.values());
    }

    public synchronized List<Combo> consultarCombosActivos() {
        return combos.values().stream().filter(Combo::estaActivo).toList();
    }

    public synchronized void cambiarEstado(String codigo, boolean activo) {
        Combo combo = buscarCombo(codigo).orElseThrow(() ->
                new IllegalArgumentException("No existe el combo indicado."));
        if (activo) combo.activar();
        else combo.desactivar();
    }
}
