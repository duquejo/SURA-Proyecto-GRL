package com.sura.arlmanager.infrastructure.web.presupuesto.databuilder;

import com.sura.arlmanager.domain.model.empresa.EmpresaAfiliada;
import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import com.sura.arlmanager.domain.model.seguridad.ContextoUsuario;
import com.sura.arlmanager.domain.model.seguridad.RolUsuario;
import com.sura.arlmanager.domain.usecase.presupuesto.AsignacionPresupuestoResultado;
import com.sura.arlmanager.infrastructure.web.presupuesto.AsignarPresupuestoController.AsignarPresupuestoRequest;
import java.math.BigDecimal;
import java.util.UUID;

public final class AsignarPresupuestoWebTestDataBuilder {
    private AsignarPresupuestoWebTestDataBuilder() {}

    public static UUID empresaAfiliadaId() {
        return UUID.randomUUID();
    }

    public static UUID grupoPrevencionId() {
        return UUID.randomUUID();
    }

    public static UUID idempotencyKey() {
        return UUID.randomUUID();
    }

    public static ContextoUsuario contextoLider(UUID grupoPrevencionId) {
        return new ContextoUsuario(UUID.randomUUID(), RolUsuario.LIDER_PREVENCION, grupoPrevencionId);
    }

    public static AsignarPresupuestoRequest solicitud() {
        return new AsignarPresupuestoRequest(2026, new BigDecimal("2500.00"));
    }

    public static AsignacionPresupuestoResultado asignacion(UUID empresaAfiliadaId, UUID grupoPrevencionId) {
        EmpresaAfiliada empresaAfiliada =
                new EmpresaAfiliada(empresaAfiliadaId, grupoPrevencionId, "Empresa de prueba");
        PresupuestoAnual presupuesto = new PresupuestoAnual(
                UUID.randomUUID(),
                empresaAfiliadaId,
                2026,
                new BigDecimal("2500.00"),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                new BigDecimal("2500.00"),
                0L);
        return new AsignacionPresupuestoResultado(empresaAfiliada, presupuesto);
    }
}
