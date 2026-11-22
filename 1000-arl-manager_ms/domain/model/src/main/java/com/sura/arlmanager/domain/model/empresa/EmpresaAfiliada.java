package com.sura.arlmanager.domain.model.empresa;

import java.util.Objects;
import java.util.UUID;

public record EmpresaAfiliada(UUID id, UUID grupoPrevencionId, String razonSocial) {
    public EmpresaAfiliada {
        Objects.requireNonNull(id, "id es obligatorio");
        Objects.requireNonNull(grupoPrevencionId, "grupoPrevencionId es obligatorio");
        Objects.requireNonNull(razonSocial, "razonSocial es obligatoria");
        if (razonSocial.isBlank()) {
            throw new IllegalArgumentException("razonSocial no puede estar vacia");
        }
    }
}
