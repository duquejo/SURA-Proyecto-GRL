package com.sura.arlmanager.domain.model.plantrabajo;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record PlanTrabajo(UUID id, UUID empresaAfiliadaId, int vigencia, EstadoPlanTrabajo estado, LocalDate creadoEn) {

    public PlanTrabajo {
        Objects.requireNonNull(id, "id es obligatorio");
        Objects.requireNonNull(empresaAfiliadaId, "empresaAfiliadaId es obligatorio");
        Objects.requireNonNull(estado, "estado es obligatorio");
        Objects.requireNonNull(creadoEn, "creadoEn es obligatorio");
        if (vigencia < 1) {
            throw new IllegalArgumentException("vigencia debe ser positiva");
        }
    }
}
