package com.sura.arlmanager.domain.model.exception;

public final class EstadoInvalidoException extends NegocioException {
    public EstadoInvalidoException(String codigoRegla, String mensaje) {
        super(codigoRegla, mensaje);
    }
}
