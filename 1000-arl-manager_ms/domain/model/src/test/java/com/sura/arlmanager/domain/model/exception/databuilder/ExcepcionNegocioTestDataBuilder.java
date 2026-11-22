package com.sura.arlmanager.domain.model.exception.databuilder;

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
}
