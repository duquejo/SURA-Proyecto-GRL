package com.sura.arlmanager.domain.model.exception;

import static com.sura.arlmanager.domain.model.exception.databuilder.ExcepcionNegocioTestDataBuilder.accesoDenegadoGrupoException;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccesoDenegadoGrupoExceptionTest {
    @Test
    void identificaLaReglaDeAccesoEntreGrupos() {
        // Arrange
        AccesoDenegadoGrupoException excepcion = accesoDenegadoGrupoException();

        // Act
        String codigoRegla = excepcion.codigoRegla();

        // Assert
        assertThat(codigoRegla).isEqualTo("RN-09");
        assertThat(excepcion).hasMessage("El usuario no pertenece al grupo de prevención solicitado");
    }
}
