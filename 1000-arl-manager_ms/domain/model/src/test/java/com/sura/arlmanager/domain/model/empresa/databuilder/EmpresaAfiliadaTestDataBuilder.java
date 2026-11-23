package com.sura.arlmanager.domain.model.empresa.databuilder;

import com.sura.arlmanager.domain.model.empresa.EmpresaAfiliada;
import java.util.UUID;

public final class EmpresaAfiliadaTestDataBuilder {
    private EmpresaAfiliadaTestDataBuilder() {}

    public static EmpresaAfiliada empresaAfiliada() {
        return new EmpresaAfiliada(UUID.randomUUID(), UUID.randomUUID(), "Empresa de prueba");
    }
}
