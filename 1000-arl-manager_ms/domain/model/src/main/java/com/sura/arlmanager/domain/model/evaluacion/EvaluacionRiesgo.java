package com.sura.arlmanager.domain.model.evaluacion;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public record EvaluacionRiesgo(
        UUID id, UUID ordenServicioId, int nivelRiesgoAnterior, int nivelRiesgoActual, LocalDate evaluadaEn) {

    public EvaluacionRiesgo {
        Objects.requireNonNull(id, "id es obligatorio");
        Objects.requireNonNull(ordenServicioId, "ordenServicioId es obligatorio");
        Objects.requireNonNull(evaluadaEn, "evaluadaEn es obligatoria");
        if (nivelRiesgoAnterior < 0 || nivelRiesgoActual < 0) {
            throw new IllegalArgumentException("los niveles de riesgo no pueden ser negativos");
        }
    }
}
