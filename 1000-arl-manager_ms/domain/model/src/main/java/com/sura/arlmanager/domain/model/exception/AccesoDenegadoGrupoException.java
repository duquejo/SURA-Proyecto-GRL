package com.sura.arlmanager.domain.model.exception;

public final class AccesoDenegadoGrupoException extends NegocioException {
    public AccesoDenegadoGrupoException(String mensaje) {
        super("RN-09", mensaje);
    }
}
