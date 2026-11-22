package com.sura.arlmanager.domain.model.ordenservicio;

import java.util.Objects;
import java.util.UUID;

public record ServicioPrevencion(UUID id, String nombre, String descripcion) {

    public ServicioPrevencion {
        Objects.requireNonNull(id, "id es obligatorio");
        Objects.requireNonNull(nombre, "nombre es obligatorio");
        Objects.requireNonNull(descripcion, "descripcion es obligatoria");
        if (nombre.isBlank()) {
            throw new IllegalArgumentException("nombre no puede estar vacio");
        }
        if (descripcion.isBlank()) {
            throw new IllegalArgumentException("descripcion no puede estar vacia");
        }
    }
}
