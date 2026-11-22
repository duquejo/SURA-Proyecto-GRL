package com.sura.arlmanager;

import static org.mockito.Mockito.mockStatic;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

class ArlManagerApplicationTest {
    @Test
    void delegaElInicioEnSpringApplication() {
        // Arrange
        String[] argumentos = new String[0];

        // Act
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            ArlManagerApplication.main(argumentos);

            // Assert
            springApplication.verify(() -> SpringApplication.run(ArlManagerApplication.class, argumentos));
        }
    }
}
