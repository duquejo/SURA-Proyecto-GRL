package com.sura.arlmanager.domain.model.ordenservicio;

import static com.sura.arlmanager.domain.model.databuilder.DomainModelTestDataBuilder.ordenServicio;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class OrdenServicioTest {
    @Test
    void rechazaValorTotalNoPositivo() {
        // Arrange
        OrdenServicio orden = ordenServicio();

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new OrdenServicio(
                        orden.id(),
                        orden.planTrabajoId(),
                        orden.prestadorId(),
                        orden.profesionalPrevencionId(),
                        BigDecimal.ZERO,
                        orden.estado(),
                        orden.fechaProgramada(),
                        orden.creadoEn()));

        // Assert
        assertThat(excepcion).hasMessageContaining("valorTotal");
    }
}
