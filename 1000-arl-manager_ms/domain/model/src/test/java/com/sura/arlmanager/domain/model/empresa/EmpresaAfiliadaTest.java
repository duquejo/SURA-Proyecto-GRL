package com.sura.arlmanager.domain.model.empresa;

import static com.sura.arlmanager.domain.model.empresa.databuilder.EmpresaAfiliadaTestDataBuilder.empresaAfiliada;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EmpresaAfiliadaTest {
    @Test
    void rechazaNombreEnBlanco() {
        // Arrange
        EmpresaAfiliada empresa = empresaAfiliada();

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new EmpresaAfiliada(empresa.id(), empresa.grupoPrevencionId(), " "));

        // Assert
        assertThat(excepcion).hasMessageContaining("razonSocial");
    }
}
