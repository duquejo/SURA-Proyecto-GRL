package com.sura.arlmanager.domain.usecase.presupuesto.databuilder;

import com.sura.arlmanager.domain.model.empresa.EmpresaAfiliada;
import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import com.sura.arlmanager.domain.model.seguridad.ContextoUsuario;
import com.sura.arlmanager.domain.model.seguridad.RolUsuario;
import java.math.BigDecimal;
import java.util.UUID;

public final class AsignarPresupuestoTestDataBuilder {
    private AsignarPresupuestoTestDataBuilder() {}

    public static UUID empresaAfiliadaId() {
        return UUID.randomUUID();
    }

    public static UUID grupoPrevencionId() {
        return UUID.randomUUID();
    }

    public static EmpresaAfiliada empresaAfiliada(UUID empresaAfiliadaId, UUID grupoPrevencionId) {
        return new EmpresaAfiliada(empresaAfiliadaId, grupoPrevencionId, "Empresa de prueba");
    }

    public static ContextoUsuario liderPrevencion(UUID grupoPrevencionId) {
        return new ContextoUsuario(UUID.randomUUID(), RolUsuario.LIDER_PREVENCION, grupoPrevencionId);
    }

    public static ContextoUsuario profesionalPrevencion(UUID grupoPrevencionId) {
        return new ContextoUsuario(UUID.randomUUID(), RolUsuario.PROFESIONAL_PREVENCION, grupoPrevencionId);
    }

    public static PresupuestoAnual presupuestoExistente(UUID empresaAfiliadaId, int vigencia) {
        return new PresupuestoAnual(
                UUID.randomUUID(),
                empresaAfiliadaId,
                vigencia,
                new BigDecimal("1000.00"),
                new BigDecimal("200.00"),
                new BigDecimal("100.00"),
                new BigDecimal("700.00"),
                2L);
    }
}
