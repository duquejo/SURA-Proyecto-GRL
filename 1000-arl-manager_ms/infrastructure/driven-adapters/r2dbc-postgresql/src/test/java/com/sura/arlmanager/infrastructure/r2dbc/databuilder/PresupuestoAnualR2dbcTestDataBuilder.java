package com.sura.arlmanager.infrastructure.r2dbc.databuilder;

import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import java.math.BigDecimal;
import java.util.UUID;

public final class PresupuestoAnualR2dbcTestDataBuilder {
    private PresupuestoAnualR2dbcTestDataBuilder() {}

    public static PresupuestoAnual presupuestoAnual() {
        return new PresupuestoAnual(
                UUID.randomUUID(),
                UUID.randomUUID(),
                2026,
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("700.00"),
                0L);
    }

    public static EmpresaConPresupuesto empresaConPresupuesto() {
        UUID grupoPrevencionId = UUID.randomUUID();
        PresupuestoAnual presupuesto = presupuestoAnual();
        return new EmpresaConPresupuesto(grupoPrevencionId, presupuesto);
    }

    public static PresupuestoAnual actualizarMonto(
            PresupuestoAnual presupuesto, BigDecimal montoAsignado, long version) {
        BigDecimal saldoDisponible =
                montoAsignado.subtract(presupuesto.saldoComprometido()).subtract(presupuesto.saldoEjecutado());
        return new PresupuestoAnual(
                presupuesto.id(),
                presupuesto.empresaAfiliadaId(),
                presupuesto.vigencia(),
                montoAsignado,
                presupuesto.saldoComprometido(),
                presupuesto.saldoEjecutado(),
                saldoDisponible,
                version);
    }

    public record EmpresaConPresupuesto(UUID grupoPrevencionId, PresupuestoAnual presupuesto) {}
}
