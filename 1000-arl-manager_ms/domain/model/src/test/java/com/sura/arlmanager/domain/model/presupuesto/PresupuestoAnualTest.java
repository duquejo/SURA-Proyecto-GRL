package com.sura.arlmanager.domain.model.presupuesto;

import static com.sura.arlmanager.domain.model.databuilder.DomainModelTestDataBuilder.presupuestoAnual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PresupuestoAnualTest {
    @Test
    void rechazaEcuacionPresupuestalInconsistente() {
        // Arrange
        PresupuestoAnual presupuesto = presupuestoAnual();

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new PresupuestoAnual(
                        presupuesto.id(),
                        presupuesto.empresaAfiliadaId(),
                        presupuesto.vigencia(),
                        presupuesto.montoAsignado(),
                        presupuesto.saldoComprometido(),
                        presupuesto.saldoEjecutado(),
                        new BigDecimal("699.99"),
                        presupuesto.version()));

        // Assert
        assertThat(excepcion).hasMessageContaining("ecuacion presupuestal");
    }

    @Test
    void rechazaSaldoComprometidoNegativo() {
        // Arrange
        PresupuestoAnual presupuesto = presupuestoAnual();

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new PresupuestoAnual(
                        presupuesto.id(),
                        presupuesto.empresaAfiliadaId(),
                        presupuesto.vigencia(),
                        presupuesto.montoAsignado(),
                        new BigDecimal("-1.00"),
                        presupuesto.saldoEjecutado(),
                        new BigDecimal("901.00"),
                        presupuesto.version()));

        // Assert
        assertThat(excepcion).hasMessageContaining("no pueden ser negativos");
    }
}
