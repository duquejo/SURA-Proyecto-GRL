package com.sura.arlmanager.infrastructure.web.error;

import static com.sura.arlmanager.infrastructure.web.error.databuilder.ErrorNegocioTestDataBuilder.excepcionDeReglaPresupuestal;
import static com.sura.arlmanager.infrastructure.web.error.databuilder.ErrorNegocioTestDataBuilder.excepcionDeRol;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.net.URI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

@ExtendWith(MockitoExtension.class)
class ErrorNegocioHandlerTest {
    @Mock
    private ServerWebExchange exchange;

    @Mock
    private ServerHttpRequest request;

    @InjectMocks
    private ErrorNegocioHandler errorNegocioHandler;

    @Test
    void respondeForbiddenParaRestriccionesDeAcceso() {
        // Arrange
        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(URI.create("/api/v1/empresas-afiliadas/empresa/presupuestos"));

        // Act
        org.springframework.http.ResponseEntity<ErrorNegocioHandler.ErrorNegocioRFC7807> respuesta =
                errorNegocioHandler.manejar(excepcionDeRol(), exchange);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(respuesta.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().status()).isEqualTo(403);
        assertThat(respuesta.getBody().codigoRegla()).isEqualTo("RN-10");
        assertThat(respuesta.getBody().instance()).isEqualTo("/api/v1/empresas-afiliadas/empresa/presupuestos");
    }

    @Test
    void respondeUnprocessableEntityParaReglasDeNegocio() {
        // Arrange
        when(exchange.getRequest()).thenReturn(request);
        when(request.getURI()).thenReturn(URI.create("/api/v1/empresas-afiliadas/empresa/presupuestos"));

        // Act
        org.springframework.http.ResponseEntity<ErrorNegocioHandler.ErrorNegocioRFC7807> respuesta =
                errorNegocioHandler.manejar(excepcionDeReglaPresupuestal(), exchange);

        // Assert
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().status()).isEqualTo(422);
        assertThat(respuesta.getBody().codigoRegla()).isEqualTo("RN-03");
    }
}
