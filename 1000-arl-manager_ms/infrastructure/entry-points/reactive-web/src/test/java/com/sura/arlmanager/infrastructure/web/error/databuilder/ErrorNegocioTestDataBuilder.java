package com.sura.arlmanager.infrastructure.web.error.databuilder;

import com.sura.arlmanager.domain.model.exception.NegocioException;

public final class ErrorNegocioTestDataBuilder {
    private ErrorNegocioTestDataBuilder() {}

    public static NegocioException excepcionDeRol() {
        return new NegocioException("RN-10", "Solo LiderPrevencion puede asignar presupuesto");
    }

    public static NegocioException excepcionDeReglaPresupuestal() {
        return new NegocioException("RN-03", "El monto es inferior al saldo comprometido");
    }
}
