package com.sura.arlmanager.infrastructure.r2dbc.empresa;

import com.sura.arlmanager.domain.model.empresa.EmpresaAfiliada;
import com.sura.arlmanager.domain.model.exception.AccesoDenegadoGrupoException;
import com.sura.arlmanager.domain.model.port.EmpresaAfiliadaGateway;
import java.util.Objects;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class EmpresaAfiliadaR2dbcAdapter implements EmpresaAfiliadaGateway {
    private final DatabaseClient databaseClient;

    public EmpresaAfiliadaR2dbcAdapter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<EmpresaAfiliada> buscarPorIdYGrupoPrevencion(UUID empresaAfiliadaId, UUID grupoPrevencionId) {
        Objects.requireNonNull(empresaAfiliadaId, "empresaAfiliadaId es obligatorio");
        Objects.requireNonNull(grupoPrevencionId, "grupoPrevencionId es obligatorio");

        return databaseClient
                .sql(
                        """
                    SELECT id, grupo_prevencion_id, razon_social
                    FROM empresa_afiliada
                    WHERE id = :empresaAfiliadaId
                      AND grupo_prevencion_id = :grupoPrevencionId
                    """)
                .bind("empresaAfiliadaId", empresaAfiliadaId)
                .bind("grupoPrevencionId", grupoPrevencionId)
                .map((fila, metadata) -> new EmpresaAfiliada(
                        fila.get("id", UUID.class),
                        fila.get("grupo_prevencion_id", UUID.class),
                        fila.get("razon_social", String.class)))
                .one()
                .switchIfEmpty(Mono.error(
                        new AccesoDenegadoGrupoException("La Empresa afiliada no esta disponible para este grupo")));
    }
}
