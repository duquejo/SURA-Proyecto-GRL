package com.sura.arlmanager.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.sura.arlmanager.domain.model.port.AuditoriaPresupuestoGateway;
import com.sura.arlmanager.domain.model.port.EmpresaAfiliadaGateway;
import com.sura.arlmanager.domain.model.port.PresupuestoAnualGateway;
import com.sura.arlmanager.domain.model.port.TransaccionPort;
import com.sura.arlmanager.domain.usecase.presupuesto.AsignarPresupuestoUseCase;
import org.junit.jupiter.api.Test;

class AsignarPresupuestoConfigurationTest {
    @Test
    void creaCasoDeUsoConSusPuertos() {
        // Arrange
        AsignarPresupuestoConfiguration configuration = new AsignarPresupuestoConfiguration();
        PresupuestoAnualGateway presupuestoGateway = mock(PresupuestoAnualGateway.class);
        EmpresaAfiliadaGateway empresaGateway = mock(EmpresaAfiliadaGateway.class);
        AuditoriaPresupuestoGateway auditoriaGateway = mock(AuditoriaPresupuestoGateway.class);
        TransaccionPort transaccionPort = mock(TransaccionPort.class);

        // Act
        AsignarPresupuestoUseCase resultado = configuration.asignarPresupuestoUseCase(
                presupuestoGateway, empresaGateway, auditoriaGateway, transaccionPort);

        // Assert
        assertThat(resultado).isNotNull();
    }
}
