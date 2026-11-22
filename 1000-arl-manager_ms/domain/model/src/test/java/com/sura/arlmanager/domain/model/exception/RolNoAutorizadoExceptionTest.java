package com.sura.arlmanager.domain.model.exception;

import static com.sura.arlmanager.domain.model.exception.databuilder.RolNoAutorizadoExceptionTestDataBuilder.rolNoAutorizado;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RolNoAutorizadoExceptionTest {
    @Test
    void identificaReglaDeAsignacionExclusivaDelLider() {
        // Arrange
        RolNoAutorizadoException excepcion = rolNoAutorizado();

        // Act
        String codigoRegla = excepcion.codigoRegla();

        // Assert
        assertThat(codigoRegla).isEqualTo("RN-10");
    }
}
