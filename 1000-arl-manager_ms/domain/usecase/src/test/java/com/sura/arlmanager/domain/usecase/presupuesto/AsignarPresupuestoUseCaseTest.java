package com.sura.arlmanager.domain.usecase.presupuesto;

import static com.sura.arlmanager.domain.usecase.presupuesto.databuilder.AsignarPresupuestoTestDataBuilder.empresaAfiliada;
import static com.sura.arlmanager.domain.usecase.presupuesto.databuilder.AsignarPresupuestoTestDataBuilder.empresaAfiliadaId;
import static com.sura.arlmanager.domain.usecase.presupuesto.databuilder.AsignarPresupuestoTestDataBuilder.grupoPrevencionId;
import static com.sura.arlmanager.domain.usecase.presupuesto.databuilder.AsignarPresupuestoTestDataBuilder.liderPrevencion;
import static com.sura.arlmanager.domain.usecase.presupuesto.databuilder.AsignarPresupuestoTestDataBuilder.presupuestoExistente;
import static com.sura.arlmanager.domain.usecase.presupuesto.databuilder.AsignarPresupuestoTestDataBuilder.profesionalPrevencion;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.sura.arlmanager.domain.model.exception.AccesoDenegadoGrupoException;
import com.sura.arlmanager.domain.model.exception.NegocioException;
import com.sura.arlmanager.domain.model.port.AuditoriaPresupuestoGateway;
import com.sura.arlmanager.domain.model.port.EmpresaAfiliadaGateway;
import com.sura.arlmanager.domain.model.port.PresupuestoAnualGateway;
import com.sura.arlmanager.domain.model.port.TransaccionPort;
import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import com.sura.arlmanager.domain.model.seguridad.ContextoUsuario;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AsignarPresupuestoUseCaseTest {
    @Mock
    private PresupuestoAnualGateway presupuestoAnualGateway;

    @Mock
    private EmpresaAfiliadaGateway empresaAfiliadaGateway;

    @Mock
    private AuditoriaPresupuestoGateway auditoriaPresupuestoGateway;

    @Mock
    private TransaccionPort transaccionPort;

    @InjectMocks
    private AsignarPresupuestoUseCase asignarPresupuestoUseCase;

    @Test
    void creaPresupuestoSinHeredarSaldosDeVigenciasAnteriores() {
        // Arrange
        UUID empresaId = empresaAfiliadaId();
        UUID grupoId = grupoPrevencionId();
        ContextoUsuario lider = liderPrevencion(grupoId);
        BigDecimal montoAsignado = new BigDecimal("2500.00");
        when(empresaAfiliadaGateway.buscarPorIdYGrupoPrevencion(empresaId, grupoId))
                .thenReturn(Mono.just(empresaAfiliada(empresaId, grupoId)));
        when(presupuestoAnualGateway.buscarPorEmpresaYVigencia(empresaId, 2026)).thenReturn(Mono.empty());
        when(presupuestoAnualGateway.crear(org.mockito.ArgumentMatchers.any(PresupuestoAnual.class)))
                .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));
        ejecutarTransaccionesEnLinea();
        permitirRegistroAuditoria();

        // Act
        Mono<AsignacionPresupuestoResultado> resultado =
                asignarPresupuestoUseCase.ejecutar(empresaId, 2026, montoAsignado, lider);

        // Assert
        StepVerifier.create(resultado)
                .assertNext(asignacion -> {
                    PresupuestoAnual presupuesto = asignacion.presupuesto();
                    assertThat(presupuesto.montoAsignado()).isEqualByComparingTo(montoAsignado);
                    assertThat(presupuesto.saldoComprometido()).isZero();
                    assertThat(presupuesto.saldoEjecutado()).isZero();
                    assertThat(presupuesto.saldoDisponible()).isEqualByComparingTo(montoAsignado);
                    assertThat(asignacion.empresaAfiliada().id()).isEqualTo(empresaId);
                })
                .verifyComplete();
        verify(presupuestoAnualGateway).crear(org.mockito.ArgumentMatchers.any(PresupuestoAnual.class));
    }

    @Test
    void registraAuditoriaDeCreacionSinValoresAnteriores() {
        // Arrange
        UUID empresaId = empresaAfiliadaId();
        UUID grupoId = grupoPrevencionId();
        ContextoUsuario lider = liderPrevencion(grupoId);
        when(empresaAfiliadaGateway.buscarPorIdYGrupoPrevencion(empresaId, grupoId))
                .thenReturn(Mono.just(empresaAfiliada(empresaId, grupoId)));
        when(presupuestoAnualGateway.buscarPorEmpresaYVigencia(empresaId, 2026)).thenReturn(Mono.empty());
        when(presupuestoAnualGateway.crear(any(PresupuestoAnual.class)))
                .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));
        ejecutarTransaccionesEnLinea();
        permitirRegistroAuditoria();

        // Act
        Mono<AsignacionPresupuestoResultado> resultado =
                asignarPresupuestoUseCase.ejecutar(empresaId, 2026, new BigDecimal("2500.00"), lider);

        // Assert
        StepVerifier.create(resultado).expectNextCount(1).verifyComplete();
        verify(auditoriaPresupuestoGateway)
                .registrar(any(UUID.class), eq(lider), isNull(), any(PresupuestoAnual.class));
    }

    @Test
    void registraAuditoriaDeAjusteConValoresAnteriores() {
        // Arrange
        UUID empresaId = empresaAfiliadaId();
        ContextoUsuario lider = liderPrevencion(grupoPrevencionId());
        PresupuestoAnual existente = presupuestoExistente(empresaId, 2026);
        when(empresaAfiliadaGateway.buscarPorIdYGrupoPrevencion(empresaId, lider.grupoPrevencionId()))
                .thenReturn(Mono.just(empresaAfiliada(empresaId, lider.grupoPrevencionId())));
        when(presupuestoAnualGateway.buscarPorEmpresaYVigencia(empresaId, 2026)).thenReturn(Mono.just(existente));
        when(presupuestoAnualGateway.actualizar(any(PresupuestoAnual.class)))
                .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));
        ejecutarTransaccionesEnLinea();
        permitirRegistroAuditoria();

        // Act
        Mono<AsignacionPresupuestoResultado> resultado =
                asignarPresupuestoUseCase.ejecutar(empresaId, 2026, new BigDecimal("1500.00"), lider);

        // Assert
        StepVerifier.create(resultado).expectNextCount(1).verifyComplete();
        verify(auditoriaPresupuestoGateway)
                .registrar(any(UUID.class), eq(lider), eq(existente), any(PresupuestoAnual.class));
    }

    @Test
    void ajustaPresupuestoConservandoSaldosDeLaMismaVigencia() {
        // Arrange
        UUID empresaId = empresaAfiliadaId();
        ContextoUsuario lider = liderPrevencion(grupoPrevencionId());
        PresupuestoAnual existente = presupuestoExistente(empresaId, 2026);
        BigDecimal montoAsignado = new BigDecimal("1500.00");
        when(empresaAfiliadaGateway.buscarPorIdYGrupoPrevencion(empresaId, lider.grupoPrevencionId()))
                .thenReturn(Mono.just(empresaAfiliada(empresaId, lider.grupoPrevencionId())));
        when(presupuestoAnualGateway.buscarPorEmpresaYVigencia(empresaId, 2026)).thenReturn(Mono.just(existente));
        when(presupuestoAnualGateway.actualizar(org.mockito.ArgumentMatchers.any(PresupuestoAnual.class)))
                .thenAnswer(invocacion -> Mono.just(invocacion.getArgument(0)));
        ejecutarTransaccionesEnLinea();
        permitirRegistroAuditoria();

        // Act
        Mono<AsignacionPresupuestoResultado> resultado =
                asignarPresupuestoUseCase.ejecutar(empresaId, 2026, montoAsignado, lider);

        // Assert
        StepVerifier.create(resultado)
                .assertNext(asignacion -> {
                    PresupuestoAnual presupuesto = asignacion.presupuesto();
                    assertThat(presupuesto.montoAsignado()).isEqualByComparingTo(montoAsignado);
                    assertThat(presupuesto.saldoComprometido()).isEqualByComparingTo("200.00");
                    assertThat(presupuesto.saldoEjecutado()).isEqualByComparingTo("100.00");
                    assertThat(presupuesto.saldoDisponible()).isEqualByComparingTo("1200.00");
                    assertThat(presupuesto.version()).isEqualTo(existente.version());
                })
                .verifyComplete();
        verify(presupuestoAnualGateway).actualizar(org.mockito.ArgumentMatchers.any(PresupuestoAnual.class));
    }

    @Test
    void rechazaMontoInferiorALosSaldosComprometidosYEjecutados() {
        // Arrange
        UUID empresaId = empresaAfiliadaId();
        ContextoUsuario lider = liderPrevencion(grupoPrevencionId());
        PresupuestoAnual existente = presupuestoExistente(empresaId, 2026);
        when(empresaAfiliadaGateway.buscarPorIdYGrupoPrevencion(empresaId, lider.grupoPrevencionId()))
                .thenReturn(Mono.just(empresaAfiliada(empresaId, lider.grupoPrevencionId())));
        when(presupuestoAnualGateway.buscarPorEmpresaYVigencia(empresaId, 2026)).thenReturn(Mono.just(existente));
        ejecutarTransaccionesEnLinea();

        // Act
        Mono<AsignacionPresupuestoResultado> resultado =
                asignarPresupuestoUseCase.ejecutar(empresaId, 2026, new BigDecimal("299.99"), lider);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(NegocioException.class);
                    assertThat(((NegocioException) error).codigoRegla()).isEqualTo("RN-03");
                })
                .verify();
        verify(presupuestoAnualGateway).buscarPorEmpresaYVigencia(empresaId, 2026);
        verify(presupuestoAnualGateway, org.mockito.Mockito.never())
                .crear(org.mockito.ArgumentMatchers.any(PresupuestoAnual.class));
        verify(presupuestoAnualGateway, org.mockito.Mockito.never())
                .actualizar(org.mockito.ArgumentMatchers.any(PresupuestoAnual.class));
    }

    @Test
    void rechazaMontoQueNoEsMultiploDeUnCentavo() {
        // Arrange
        ContextoUsuario lider = liderPrevencion(grupoPrevencionId());

        // Act
        Mono<AsignacionPresupuestoResultado> resultado =
                asignarPresupuestoUseCase.ejecutar(empresaAfiliadaId(), 2026, new BigDecimal("0.001"), lider);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(NegocioException.class);
                    assertThat(((NegocioException) error).codigoRegla()).isEqualTo("RN-03");
                })
                .verify();
        verifyNoInteractions(empresaAfiliadaGateway, presupuestoAnualGateway);
    }

    @Test
    void rechazaMontoCero() {
        // Arrange
        ContextoUsuario lider = liderPrevencion(grupoPrevencionId());

        // Act
        Mono<AsignacionPresupuestoResultado> resultado =
                asignarPresupuestoUseCase.ejecutar(empresaAfiliadaId(), 2026, BigDecimal.ZERO, lider);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(NegocioException.class);
                    assertThat(((NegocioException) error).codigoRegla()).isEqualTo("RN-03");
                })
                .verify();
        verifyNoInteractions(empresaAfiliadaGateway, presupuestoAnualGateway);
    }

    @Test
    void rechazaMontoMayorAlRangoPersistible() {
        // Arrange
        ContextoUsuario lider = liderPrevencion(grupoPrevencionId());

        // Act
        Mono<AsignacionPresupuestoResultado> resultado = asignarPresupuestoUseCase.ejecutar(
                empresaAfiliadaId(), 2026, new BigDecimal("10000000000000.00"), lider);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(NegocioException.class);
                    assertThat(((NegocioException) error).codigoRegla()).isEqualTo("RN-03");
                })
                .verify();
        verifyNoInteractions(empresaAfiliadaGateway, presupuestoAnualGateway);
    }

    @Test
    void rechazaVigenciaAnteriorAlMinimoDelContrato() {
        // Arrange
        ContextoUsuario lider = liderPrevencion(grupoPrevencionId());

        // Act
        Mono<AsignacionPresupuestoResultado> resultado =
                asignarPresupuestoUseCase.ejecutar(empresaAfiliadaId(), 2025, new BigDecimal("2500.00"), lider);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(NegocioException.class);
                    assertThat(((NegocioException) error).codigoRegla()).isEqualTo("RN-03");
                })
                .verify();
        verifyNoInteractions(empresaAfiliadaGateway, presupuestoAnualGateway);
    }

    @Test
    void rechazaUsuarioQueNoSeaLiderDePrevencion() {
        // Arrange
        UUID grupoId = grupoPrevencionId();
        ContextoUsuario usuario = profesionalPrevencion(grupoId);

        // Act
        Mono<AsignacionPresupuestoResultado> resultado =
                asignarPresupuestoUseCase.ejecutar(empresaAfiliadaId(), 2026, new BigDecimal("2500.00"), usuario);

        // Assert
        StepVerifier.create(resultado)
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(NegocioException.class);
                    assertThat(((NegocioException) error).codigoRegla()).isEqualTo("RN-10");
                })
                .verify();
        verifyNoInteractions(empresaAfiliadaGateway, presupuestoAnualGateway);
    }

    @Test
    void rechazaEmpresaFueraDelGrupoSinConsultarSuPresupuesto() {
        // Arrange
        UUID empresaId = empresaAfiliadaId();
        ContextoUsuario lider = liderPrevencion(grupoPrevencionId());
        when(empresaAfiliadaGateway.buscarPorIdYGrupoPrevencion(empresaId, lider.grupoPrevencionId()))
                .thenReturn(Mono.error(new AccesoDenegadoGrupoException("Empresa no disponible para este grupo")));

        // Act
        Mono<AsignacionPresupuestoResultado> resultado =
                asignarPresupuestoUseCase.ejecutar(empresaId, 2026, new BigDecimal("2500.00"), lider);

        // Assert
        StepVerifier.create(resultado)
                .expectError(AccesoDenegadoGrupoException.class)
                .verify();
        verifyNoInteractions(presupuestoAnualGateway);
    }

    private void ejecutarTransaccionesEnLinea() {
        doAnswer(invocacion -> {
                    Supplier<?> operacion = invocacion.getArgument(0);
                    return operacion.get();
                })
                .when(transaccionPort)
                .ejecutar(any());
    }

    private void permitirRegistroAuditoria() {
        when(auditoriaPresupuestoGateway.registrar(
                        any(UUID.class),
                        any(ContextoUsuario.class),
                        nullable(PresupuestoAnual.class),
                        any(PresupuestoAnual.class)))
                .thenReturn(Mono.empty());
    }
}
