package com.sura.arlmanager.infrastructure.r2dbc.presupuesto;

import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.NonNull;

@Table("presupuesto_anual")
public record PresupuestoAnualEntity(
        @Id UUID id,
        @Column("empresa_afiliada_id") UUID empresaAfiliadaId,
        int vigencia,
        @Column("monto_asignado") BigDecimal montoAsignado,
        @Column("saldo_comprometido") BigDecimal saldoComprometido,
        @Column("saldo_ejecutado") BigDecimal saldoEjecutado,
        @Column("saldo_disponible") BigDecimal saldoDisponible,
        @Version Long version) {

    @NonNull public static PresupuestoAnualEntity desdeDominio(PresupuestoAnual presupuestoAnual, Long version) {
        return new PresupuestoAnualEntity(
                presupuestoAnual.id(),
                presupuestoAnual.empresaAfiliadaId(),
                presupuestoAnual.vigencia(),
                presupuestoAnual.montoAsignado(),
                presupuestoAnual.saldoComprometido(),
                presupuestoAnual.saldoEjecutado(),
                presupuestoAnual.saldoDisponible(),
                version);
    }

    public PresupuestoAnual aDominio() {
        Long versionPersistida = Objects.requireNonNull(version, "version es obligatoria");
        return new PresupuestoAnual(
                id,
                empresaAfiliadaId,
                vigencia,
                montoAsignado,
                saldoComprometido,
                saldoEjecutado,
                saldoDisponible,
                versionPersistida.longValue());
    }
}
