package com.sura.arlmanager.infrastructure.r2dbc.presupuesto;

import static org.assertj.core.api.Assertions.assertThat;

import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import com.sura.arlmanager.infrastructure.r2dbc.databuilder.PresupuestoAnualR2dbcTestDataBuilder;
import org.junit.jupiter.api.Test;

class PresupuestoAnualEntityTest {
    @Test
    void debeMapearLaVersionPersistidaAlModeloDeDominio() {
        // Arrange
        PresupuestoAnual presupuesto = PresupuestoAnualR2dbcTestDataBuilder.presupuestoAnual();
        PresupuestoAnualEntity entidad = PresupuestoAnualEntity.desdeDominio(presupuesto, 3L);

        // Act
        PresupuestoAnual resultado = entidad.aDominio();

        // Assert
        assertThat(resultado)
                .isEqualTo(new PresupuestoAnual(
                        presupuesto.id(),
                        presupuesto.empresaAfiliadaId(),
                        presupuesto.vigencia(),
                        presupuesto.montoAsignado(),
                        presupuesto.saldoComprometido(),
                        presupuesto.saldoEjecutado(),
                        presupuesto.saldoDisponible(),
                        3L));
    }

    @Test
    void debePermitirVersionNulaAlInsertarUnPresupuestoNuevo() {
        // Arrange
        PresupuestoAnual presupuesto = PresupuestoAnualR2dbcTestDataBuilder.presupuestoAnual();

        // Act
        PresupuestoAnualEntity entidad = PresupuestoAnualEntity.desdeDominio(presupuesto, null);

        // Assert
        assertThat(entidad.version()).isNull();
    }
}
