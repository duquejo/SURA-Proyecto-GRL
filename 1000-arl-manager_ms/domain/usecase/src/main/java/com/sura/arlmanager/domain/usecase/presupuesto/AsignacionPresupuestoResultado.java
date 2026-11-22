package com.sura.arlmanager.domain.usecase.presupuesto;

import com.sura.arlmanager.domain.model.empresa.EmpresaAfiliada;
import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import java.util.Objects;

public record AsignacionPresupuestoResultado(EmpresaAfiliada empresaAfiliada, PresupuestoAnual presupuesto) {
    public AsignacionPresupuestoResultado {
        Objects.requireNonNull(empresaAfiliada, "empresaAfiliada es obligatoria");
        Objects.requireNonNull(presupuesto, "presupuesto es obligatorio");
    }
}
