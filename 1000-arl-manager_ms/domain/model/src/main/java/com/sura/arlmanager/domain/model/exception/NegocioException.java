package com.sura.arlmanager.domain.model.exception;

import java.util.Objects;

public class NegocioException extends RuntimeException {
    private final String codigoRegla;

    public NegocioException(String codigoRegla, String mensaje) {
        super(Objects.requireNonNull(mensaje, "mensaje es obligatorio"));
        this.codigoRegla = Objects.requireNonNull(codigoRegla, "codigoRegla es obligatorio");
        if (codigoRegla.isBlank()) {
            throw new IllegalArgumentException("codigoRegla no puede estar vacio");
        }
    }

    public String codigoRegla() {
        return codigoRegla;
    }
}
