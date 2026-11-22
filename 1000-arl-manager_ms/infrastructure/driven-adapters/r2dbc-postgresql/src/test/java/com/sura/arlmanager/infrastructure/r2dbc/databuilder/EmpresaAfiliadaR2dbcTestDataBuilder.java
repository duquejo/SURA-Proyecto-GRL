package com.sura.arlmanager.infrastructure.r2dbc.databuilder;

import java.util.UUID;

public final class EmpresaAfiliadaR2dbcTestDataBuilder {
    private EmpresaAfiliadaR2dbcTestDataBuilder() {}

    public static EmpresaConGrupo empresaConGrupo() {
        return new EmpresaConGrupo(UUID.randomUUID(), UUID.randomUUID(), "Empresa de prueba");
    }

    public record EmpresaConGrupo(UUID empresaAfiliadaId, UUID grupoPrevencionId, String razonSocial) {}
}
