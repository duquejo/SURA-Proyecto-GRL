package com.sura.arlmanager.domain.usecase.presupuesto;

import static com.sura.arlmanager.domain.usecase.presupuesto.databuilder.AsignarPresupuestoTestDataBuilder.empresaAfiliada;
import static com.sura.arlmanager.domain.usecase.presupuesto.databuilder.AsignarPresupuestoTestDataBuilder.empresaAfiliadaId;
import static com.sura.arlmanager.domain.usecase.presupuesto.databuilder.AsignarPresupuestoTestDataBuilder.grupoPrevencionId;
import static com.sura.arlmanager.domain.usecase.presupuesto.databuilder.AsignarPresupuestoTestDataBuilder.presupuestoExistente;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sura.arlmanager.domain.model.empresa.EmpresaAfiliada;
import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AsignacionPresupuestoResultadoTest {
    @Test
    void rechazaEmpresaAfiliadaNula() {
        // Arrange
        PresupuestoAnual presupuesto = presupuestoExistente(empresaAfiliadaId(), 2026);

        // Act
        NullPointerException excepcion =
                assertThrows(NullPointerException.class, () -> new AsignacionPresupuestoResultado(null, presupuesto));

        // Assert
        assertThat(excepcion).hasMessageContaining("empresaAfiliada");
    }

    @Test
    void rechazaPresupuestoNulo() {
        // Arrange
        UUID empresaId = empresaAfiliadaId();
        EmpresaAfiliada empresa = empresaAfiliada(empresaId, grupoPrevencionId());

        // Act
        NullPointerException excepcion =
                assertThrows(NullPointerException.class, () -> new AsignacionPresupuestoResultado(empresa, null));

        // Assert
        assertThat(excepcion).hasMessageContaining("presupuesto");
    }
}
