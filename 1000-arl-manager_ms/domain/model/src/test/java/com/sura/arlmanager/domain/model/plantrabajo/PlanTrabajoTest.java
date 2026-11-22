package com.sura.arlmanager.domain.model.plantrabajo;

import static com.sura.arlmanager.domain.model.databuilder.DomainModelTestDataBuilder.planTrabajo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PlanTrabajoTest {
    @Test
    void rechazaVigenciaNoPositiva() {
        // Arrange
        PlanTrabajo plan = planTrabajo();

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new PlanTrabajo(plan.id(), plan.empresaAfiliadaId(), 0, plan.estado(), plan.creadoEn()));

        // Assert
        assertThat(excepcion).hasMessageContaining("vigencia");
    }
}
