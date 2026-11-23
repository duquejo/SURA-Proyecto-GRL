package com.sura.arlmanager.infrastructure.r2dbc.databuilder;

import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import com.sura.arlmanager.domain.model.seguridad.ContextoUsuario;
import com.sura.arlmanager.domain.model.seguridad.RolUsuario;
import java.math.BigDecimal;
import java.util.UUID;

public final class AuditoriaPresupuestoR2dbcTestDataBuilder {
    private AuditoriaPresupuestoR2dbcTestDataBuilder() {}

    public static EscenarioAuditoria escenario() {
        UUID empresaAfiliadaId = UUID.randomUUID();
        UUID grupoPrevencionId = UUID.randomUUID();
        ContextoUsuario contextoUsuario =
                new ContextoUsuario(UUID.randomUUID(), RolUsuario.LIDER_PREVENCION, grupoPrevencionId);
        PresupuestoAnual presupuestoAnterior = presupuesto(empresaAfiliadaId, new BigDecimal("1000.00"));
        PresupuestoAnual presupuestoNuevo = new PresupuestoAnual(
                presupuestoAnterior.id(),
                empresaAfiliadaId,
                2026,
                new BigDecimal("1500.00"),
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("1200.00"),
                0L);
        return new EscenarioAuditoria(
                empresaAfiliadaId, grupoPrevencionId, contextoUsuario, presupuestoAnterior, presupuestoNuevo);
    }

    public static PresupuestoAnual presupuesto(UUID empresaAfiliadaId, BigDecimal monto) {
        return new PresupuestoAnual(
                UUID.randomUUID(),
                empresaAfiliadaId,
                2026,
                monto,
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                monto.subtract(new BigDecimal("300.00")),
                0L);
    }

    public record EscenarioAuditoria(
            UUID empresaAfiliadaId,
            UUID grupoPrevencionId,
            ContextoUsuario contextoUsuario,
            PresupuestoAnual presupuestoAnterior,
            PresupuestoAnual presupuestoNuevo) {}
}
