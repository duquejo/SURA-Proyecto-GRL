package com.sura.arlmanager.domain.model.ordenservicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record OrdenServicio(
        UUID id,
        UUID planTrabajoId,
        UUID prestadorId,
        UUID profesionalPrevencionId,
        BigDecimal valorTotal,
        EstadoOrdenServicio estado,
        LocalDate fechaProgramada,
        LocalDate creadoEn) {

    public OrdenServicio {
        Objects.requireNonNull(id, "id es obligatorio");
        Objects.requireNonNull(planTrabajoId, "planTrabajoId es obligatorio");
        Objects.requireNonNull(prestadorId, "prestadorId es obligatorio");
        Objects.requireNonNull(profesionalPrevencionId, "profesionalPrevencionId es obligatorio");
        Objects.requireNonNull(valorTotal, "valorTotal es obligatorio");
        Objects.requireNonNull(estado, "estado es obligatorio");
        Objects.requireNonNull(fechaProgramada, "fechaProgramada es obligatoria");
        Objects.requireNonNull(creadoEn, "creadoEn es obligatorio");
        if (valorTotal.signum() <= 0) {
            throw new IllegalArgumentException("valorTotal debe ser positivo");
        }
    }
}
