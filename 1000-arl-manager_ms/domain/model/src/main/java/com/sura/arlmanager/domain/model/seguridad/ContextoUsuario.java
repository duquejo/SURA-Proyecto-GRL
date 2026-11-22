package com.sura.arlmanager.domain.model.seguridad;

import java.util.Objects;
import java.util.UUID;

public record ContextoUsuario(UUID usuarioId, RolUsuario rol, UUID grupoPrevencionId) {

    public ContextoUsuario {
        Objects.requireNonNull(usuarioId, "usuarioId es obligatorio");
        Objects.requireNonNull(rol, "rol es obligatorio");
        Objects.requireNonNull(grupoPrevencionId, "grupoPrevencionId es obligatorio");
    }
}
