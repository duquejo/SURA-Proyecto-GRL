package com.sura.arlmanager.config;

import com.sura.arlmanager.domain.model.port.AuditoriaPresupuestoGateway;
import com.sura.arlmanager.domain.model.port.EmpresaAfiliadaGateway;
import com.sura.arlmanager.domain.model.port.PresupuestoAnualGateway;
import com.sura.arlmanager.domain.model.port.TransaccionPort;
import com.sura.arlmanager.domain.usecase.presupuesto.AsignarPresupuestoUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AsignarPresupuestoConfiguration {
    @Bean
    public AsignarPresupuestoUseCase asignarPresupuestoUseCase(
            PresupuestoAnualGateway presupuestoAnualGateway,
            EmpresaAfiliadaGateway empresaAfiliadaGateway,
            AuditoriaPresupuestoGateway auditoriaPresupuestoGateway,
            TransaccionPort transaccionPort) {
        return new AsignarPresupuestoUseCase(
                presupuestoAnualGateway, empresaAfiliadaGateway, auditoriaPresupuestoGateway, transaccionPort);
    }
}
