package com.sura.arlmanager.infrastructure.r2dbc.databuilder;

import java.math.BigDecimal;
import java.util.UUID;

public final class PostgreSqlSchemaTestDataBuilder {
    private PostgreSqlSchemaTestDataBuilder() {}

    public static EmpresaFixture empresaAfiliada() {
        return new EmpresaFixture(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    public static PresupuestoFixture presupuestoValido(UUID empresaAfiliadaId) {
        return new PresupuestoFixture(
                UUID.randomUUID(),
                empresaAfiliadaId,
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("700.00"));
    }

    public record EmpresaFixture(UUID grupoPrevencionId, UUID empresaAfiliadaId, UUID usuarioId) {}

    public record PresupuestoFixture(
            UUID id,
            UUID empresaAfiliadaId,
            BigDecimal montoAsignado,
            BigDecimal saldoComprometido,
            BigDecimal saldoEjecutado,
            BigDecimal saldoDisponible) {}
}
