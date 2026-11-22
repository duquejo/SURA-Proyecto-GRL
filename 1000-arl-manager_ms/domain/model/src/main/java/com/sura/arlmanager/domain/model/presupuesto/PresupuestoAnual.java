package com.sura.arlmanager.domain.model.presupuesto;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public record PresupuestoAnual(
        UUID id,
        UUID empresaAfiliadaId,
        int vigencia,
        BigDecimal montoAsignado,
        BigDecimal saldoComprometido,
        BigDecimal saldoEjecutado,
        BigDecimal saldoDisponible,
        long version) {

    public PresupuestoAnual {
        Objects.requireNonNull(id, "id es obligatorio");
        Objects.requireNonNull(empresaAfiliadaId, "empresaAfiliadaId es obligatorio");
        Objects.requireNonNull(montoAsignado, "montoAsignado es obligatorio");
        Objects.requireNonNull(saldoComprometido, "saldoComprometido es obligatorio");
        Objects.requireNonNull(saldoEjecutado, "saldoEjecutado es obligatorio");
        Objects.requireNonNull(saldoDisponible, "saldoDisponible es obligatorio");
        if (vigencia < 1) {
            throw new IllegalArgumentException("vigencia debe ser positiva");
        }
        if (version < 0) {
            throw new IllegalArgumentException("version no puede ser negativa");
        }
        if (montoAsignado.signum() < 0
                || saldoComprometido.signum() < 0
                || saldoEjecutado.signum() < 0
                || saldoDisponible.signum() < 0) {
            throw new IllegalArgumentException("los saldos y el monto asignado no pueden ser negativos");
        }
        BigDecimal saldoCalculado = montoAsignado.subtract(saldoComprometido).subtract(saldoEjecutado);
        if (saldoDisponible.compareTo(saldoCalculado) != 0) {
            throw new IllegalArgumentException("saldoDisponible no cumple la ecuacion presupuestal");
        }
    }
}
