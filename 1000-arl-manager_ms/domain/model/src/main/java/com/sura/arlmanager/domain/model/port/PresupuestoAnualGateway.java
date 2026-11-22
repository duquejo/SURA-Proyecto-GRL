package com.sura.arlmanager.domain.model.port;

import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface PresupuestoAnualGateway {
    Mono<PresupuestoAnual> crear(PresupuestoAnual presupuestoAnual);

    Mono<PresupuestoAnual> actualizar(PresupuestoAnual presupuestoAnual);

    Mono<PresupuestoAnual> buscarPorEmpresaYVigencia(UUID empresaAfiliadaId, int vigencia);
}
