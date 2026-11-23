package com.sura.arlmanager.domain.model.port;

import com.sura.arlmanager.domain.model.empresa.EmpresaAfiliada;
import java.util.UUID;
import reactor.core.publisher.Mono;

public interface EmpresaAfiliadaGateway {
    Mono<EmpresaAfiliada> buscarPorIdYGrupoPrevencion(UUID empresaAfiliadaId, UUID grupoPrevencionId);
}
