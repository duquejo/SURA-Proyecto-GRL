package com.sura.arlmanager.domain.model.exception;

import static com.sura.arlmanager.domain.model.exception.databuilder.ExcepcionNegocioTestDataBuilder.negocioException;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NegocioExceptionTest {
    @Test
    void conservaCodigoDeReglaYMensajeDeNegocio() {
        // Arrange
        NegocioException excepcion = negocioException();

        // Act
        String codigoRegla = excepcion.codigoRegla();

        // Assert
        assertThat(codigoRegla).isEqualTo("RN-02");
        assertThat(excepcion).hasMessage("La orden de servicio es indivisible");
    }
}
