package com.sura.arlmanager.domain.model.exception.databuilder;

import com.sura.arlmanager.domain.model.exception.RolNoAutorizadoException;

public final class RolNoAutorizadoExceptionTestDataBuilder {
    private RolNoAutorizadoExceptionTestDataBuilder() {}

    public static RolNoAutorizadoException rolNoAutorizado() {
        return new RolNoAutorizadoException("Solo el Lider en prevencion puede asignar presupuesto");
    }
}
