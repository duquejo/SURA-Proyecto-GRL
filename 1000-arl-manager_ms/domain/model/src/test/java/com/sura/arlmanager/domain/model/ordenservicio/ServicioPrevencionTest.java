package com.sura.arlmanager.domain.model.ordenservicio;

import static com.sura.arlmanager.domain.model.databuilder.DomainModelTestDataBuilder.servicioPrevencion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ServicioPrevencionTest {
    @Test
    void rechazaNombreVacio() {
        // Arrange
        ServicioPrevencion servicio = servicioPrevencion();

        // Act
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> new ServicioPrevencion(servicio.id(), " ", servicio.descripcion()));

        // Assert
        assertThat(excepcion).hasMessageContaining("nombre");
    }
}
