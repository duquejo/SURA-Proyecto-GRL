package com.sura.arlmanager.domain.model.seguridad;

import static com.sura.arlmanager.domain.model.databuilder.DomainModelTestDataBuilder.contextoUsuario;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ContextoUsuarioTest {
    @Test
    void exponeElUsuarioComoNombreDelPrincipal() {
        // Arrange
        ContextoUsuario contexto = contextoUsuario();

        // Act
        String nombre = contexto.getName();

        // Assert
        assertThat(nombre).isEqualTo(contexto.usuarioId().toString());
    }

    @Test
    void rechazaGrupoDePrevencionNulo() {
        // Arrange
        ContextoUsuario contexto = contextoUsuario();

        // Act
        NullPointerException excepcion = assertThrows(
                NullPointerException.class, () -> new ContextoUsuario(contexto.usuarioId(), contexto.rol(), null));

        // Assert
        assertThat(excepcion).hasMessageContaining("grupoPrevencionId");
    }
}
