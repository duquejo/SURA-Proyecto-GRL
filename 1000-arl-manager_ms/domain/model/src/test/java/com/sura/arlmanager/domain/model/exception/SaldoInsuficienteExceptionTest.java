package com.sura.arlmanager.domain.model.exception;

import static com.sura.arlmanager.domain.model.exception.databuilder.ExcepcionNegocioTestDataBuilder.saldoInsuficienteException;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SaldoInsuficienteExceptionTest {
    @Test
    void identificaLaReglaDeTopePresupuestal() {
        // Arrange
        SaldoInsuficienteException excepcion = saldoInsuficienteException();

        // Act
        String codigoRegla = excepcion.codigoRegla();

        // Assert
        assertThat(codigoRegla).isEqualTo("RN-01");
        assertThat(excepcion).isInstanceOf(NegocioException.class);
    }
}
