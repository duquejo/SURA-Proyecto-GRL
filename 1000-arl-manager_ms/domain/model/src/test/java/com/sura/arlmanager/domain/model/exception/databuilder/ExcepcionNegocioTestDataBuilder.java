package com.sura.arlmanager.domain.model.exception.databuilder;

import com.sura.arlmanager.domain.model.exception.AccesoDenegadoGrupoException;
import com.sura.arlmanager.domain.model.exception.EstadoInvalidoException;
import com.sura.arlmanager.domain.model.exception.NegocioException;
import com.sura.arlmanager.domain.model.exception.SaldoInsuficienteException;

public final class ExcepcionNegocioTestDataBuilder {
    private ExcepcionNegocioTestDataBuilder() {}

    public static NegocioException negocioException() {
        return new NegocioException("RN-02", "La orden de servicio es indivisible");
    }

    public static SaldoInsuficienteException saldoInsuficienteException() {
        return new SaldoInsuficienteException("El saldo disponible es insuficiente");
    }

    public static AccesoDenegadoGrupoException accesoDenegadoGrupoException() {
        return new AccesoDenegadoGrupoException("El usuario no pertenece al grupo de prevención solicitado");
    }

    public static EstadoInvalidoException estadoInvalidoException() {
        return new EstadoInvalidoException("RN-07", "El plan de trabajo no permite la transición solicitada");
    }
}
