package org.example.carestock.service;

import org.example.carestock.dto.AlertaInventario.Categoria;
import org.example.carestock.dto.AlertaInventario.Severidad;

import java.util.Objects;

/**
 * Necesidad de reposicion de un producto de una farmacia.
 * La identidad se compone de categoria e idProducto, nunca del codigo comercial.
 */
public record RegistroReposicion(
        Categoria categoria,
        int idProducto,
        String codigo,
        String nombre,
        int existenciasActuales,
        int stockMinimo,
        int cantidadFaltante,
        Severidad severidad
) {
    public RegistroReposicion {
        Objects.requireNonNull(categoria, "La categoria es obligatoria");
        Objects.requireNonNull(severidad, "La severidad es obligatoria");
        if (idProducto <= 0) {
            throw new IllegalArgumentException("El identificador de producto es invalido");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio");
        }
        if (existenciasActuales < 0 || stockMinimo < 0) {
            throw new IllegalArgumentException("Las existencias y el minimo no pueden ser negativos");
        }
        if (existenciasActuales > stockMinimo) {
            throw new IllegalArgumentException("Un producto con existencias suficientes no requiere reposicion");
        }
        if (cantidadFaltante != Math.max(0, stockMinimo - existenciasActuales)) {
            throw new IllegalArgumentException("La cantidad faltante no coincide con el stock minimo");
        }
        Severidad esperada = existenciasActuales == 0
                ? Severidad.CRITICA : Severidad.ADVERTENCIA;
        if (severidad != esperada) {
            throw new IllegalArgumentException("La severidad no corresponde a las existencias");
        }
        codigo = codigo == null || codigo.isBlank() ? "No registrado" : codigo;
    }
}
