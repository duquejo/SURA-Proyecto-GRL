package com.sura.arlmanager.domain.model.exception;

import static com.sura.arlmanager.domain.model.exception.databuilder.ExcepcionNegocioTestDataBuilder.estadoInvalidoException;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EstadoInvalidoExceptionTest {
    @Test
    void conservaCodigoDeReglaYMensajeDeEstadoInvalido() {
        // Arrange
        EstadoInvalidoException excepcion = estadoInvalidoException();

        // Act
        String codigoRegla = excepcion.codigoRegla();

        // Assert
        assertThat(codigoRegla).isEqualTo("RN-07");
        assertThat(excepcion).hasMessage("El plan de trabajo no permite la transición solicitada");
    }
}
