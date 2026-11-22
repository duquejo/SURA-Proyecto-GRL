package com.sura.arlmanager.domain.model.exception;

public final class SaldoInsuficienteException extends NegocioException {
    public SaldoInsuficienteException(String mensaje) {
        super("RN-01", mensaje);
    }
}
