package com.sura.arlmanager.domain.model.evaluacion;

import static com.sura.arlmanager.domain.model.databuilder.DomainModelTestDataBuilder.evaluacionRiesgo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class EvaluacionRiesgoTest {
    @Test
    void rechazaNivelDeRiesgoNegativo() {
        // Arrange
        EvaluacionRiesgo evaluacion = evaluacionRiesgo(UUID.randomUUID());

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new EvaluacionRiesgo(
                        evaluacion.id(),
                        evaluacion.ordenServicioId(),
                        evaluacion.nivelRiesgoAnterior(),
                        -1,
                        evaluacion.evaluadaEn()));

        // Assert
        assertThat(excepcion).hasMessageContaining("no pueden ser negativos");
    }
}
