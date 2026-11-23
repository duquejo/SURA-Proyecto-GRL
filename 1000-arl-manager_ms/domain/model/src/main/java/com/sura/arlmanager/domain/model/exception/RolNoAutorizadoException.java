package com.sura.arlmanager.domain.model.exception;

public final class RolNoAutorizadoException extends NegocioException {
    public RolNoAutorizadoException(String mensaje) {
        super("RN-10", mensaje);
    }
}
