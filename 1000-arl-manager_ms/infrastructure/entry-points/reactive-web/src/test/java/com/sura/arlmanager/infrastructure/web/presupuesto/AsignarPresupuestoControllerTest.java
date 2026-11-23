package com.sura.arlmanager.infrastructure.web.presupuesto;

import static com.sura.arlmanager.infrastructure.web.presupuesto.databuilder.AsignarPresupuestoWebTestDataBuilder.asignacion;
import static com.sura.arlmanager.infrastructure.web.presupuesto.databuilder.AsignarPresupuestoWebTestDataBuilder.contextoLider;
import static com.sura.arlmanager.infrastructure.web.presupuesto.databuilder.AsignarPresupuestoWebTestDataBuilder.empresaAfiliadaId;
import static com.sura.arlmanager.infrastructure.web.presupuesto.databuilder.AsignarPresupuestoWebTestDataBuilder.grupoPrevencionId;
import static com.sura.arlmanager.infrastructure.web.presupuesto.databuilder.AsignarPresupuestoWebTestDataBuilder.idempotencyKey;
import static com.sura.arlmanager.infrastructure.web.presupuesto.databuilder.AsignarPresupuestoWebTestDataBuilder.solicitud;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sura.arlmanager.domain.model.seguridad.ContextoUsuario;
import com.sura.arlmanager.domain.usecase.presupuesto.AsignarPresupuestoUseCase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AsignarPresupuestoControllerTest {
    @Mock
    private AsignarPresupuestoUseCase asignarPresupuestoUseCase;

    @InjectMocks
    private AsignarPresupuestoController controller;

    @Test
    void respondeCreatedConElPresupuestoSegunElContrato() {
        // Arrange
        UUID empresaId = empresaAfiliadaId();
        UUID grupoId = grupoPrevencionId();
        UUID claveIdempotencia = idempotencyKey();
        ContextoUsuario contexto = contextoLider(grupoId);
        AsignarPresupuestoController.AsignarPresupuestoRequest request = solicitud();
        when(asignarPresupuestoUseCase.ejecutar(empresaId, 2026, request.montoAsignado(), contexto))
                .thenReturn(Mono.just(asignacion(empresaId, grupoId)));

        // Act
        Mono<org.springframework.http.ResponseEntity<AsignarPresupuestoController.PresupuestoEmpresaResponse>>
                respuesta = controller.asignarPresupuesto(empresaId, claveIdempotencia, request, contexto);

        // Assert
        StepVerifier.create(respuesta)
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(response.getBody()).isNotNull();
                    assertThat(response.getBody().empresaAfiliadaId()).isEqualTo(empresaId);
                    assertThat(response.getBody().razonSocial()).isEqualTo("Empresa de prueba");
                    assertThat(response.getBody().vigencia()).isEqualTo(2026);
                    assertThat(response.getBody().presupuestoAnual()).isEqualByComparingTo("2500.00");
                    assertThat(response.getBody().saldoDisponible()).isEqualByComparingTo("2500.00");
                })
                .verifyComplete();
        verify(asignarPresupuestoUseCase).ejecutar(empresaId, 2026, request.montoAsignado(), contexto);
    }

    @Test
    void rechazaLaSolicitudSinUnContextoDeUsuarioAutenticado() {
        // Arrange
        UUID empresaId = empresaAfiliadaId();
        UUID claveIdempotencia = idempotencyKey();

        // Act
        Mono<org.springframework.http.ResponseEntity<AsignarPresupuestoController.PresupuestoEmpresaResponse>>
                respuesta = controller.asignarPresupuesto(empresaId, claveIdempotencia, solicitud(), () -> "usuario");

        // Assert
        StepVerifier.create(respuesta)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ResponseStatusException.class);
                    assertThat(((ResponseStatusException) error).getStatusCode())
                            .isEqualTo(HttpStatus.UNAUTHORIZED);
                })
                .verify();
    }
}
