package com.sura.arlmanager.domain.model.databuilder;

import com.sura.arlmanager.domain.model.evaluacion.EvaluacionRiesgo;
import com.sura.arlmanager.domain.model.ordenservicio.EstadoOrdenServicio;
import com.sura.arlmanager.domain.model.ordenservicio.OrdenServicio;
import com.sura.arlmanager.domain.model.ordenservicio.ServicioPrevencion;
import com.sura.arlmanager.domain.model.plantrabajo.EstadoPlanTrabajo;
import com.sura.arlmanager.domain.model.plantrabajo.PlanTrabajo;
import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import com.sura.arlmanager.domain.model.seguridad.ContextoUsuario;
import com.sura.arlmanager.domain.model.seguridad.RolUsuario;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public final class DomainModelTestDataBuilder {
    private DomainModelTestDataBuilder() {}

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

    public static PlanTrabajo planTrabajo() {
        return new PlanTrabajo(
                UUID.randomUUID(), UUID.randomUUID(), 2026, EstadoPlanTrabajo.EN_PLANEACION, LocalDate.of(2026, 1, 1));
    }

    public static OrdenServicio ordenServicio() {
        return new OrdenServicio(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("250.00"),
                EstadoOrdenServicio.PLANEADA,
                LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 1, 1));
    }

    public static EvaluacionRiesgo evaluacionRiesgo(UUID ordenServicioId) {
        return new EvaluacionRiesgo(UUID.randomUUID(), ordenServicioId, 8, 5, LocalDate.of(2026, 3, 1));
    }

    public static ServicioPrevencion servicioPrevencion() {
        return new ServicioPrevencion(
                UUID.randomUUID(), "Inspeccion de seguridad", "Identificacion de condiciones de riesgo");
    }

    public static ContextoUsuario contextoUsuario() {
        return new ContextoUsuario(UUID.randomUUID(), RolUsuario.PROFESIONAL_PREVENCION, UUID.randomUUID());
    }
}
