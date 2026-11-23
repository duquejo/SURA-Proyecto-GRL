package com.sura.arlmanager.domain.model.port;

import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import com.sura.arlmanager.domain.model.seguridad.ContextoUsuario;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface AuditoriaPresupuestoGateway {
    Mono<Void> registrar(
            UUID presupuestoId,
            ContextoUsuario contextoUsuario,
            PresupuestoAnual presupuestoAnterior,
            PresupuestoAnual presupuestoNuevo);
}
