package com.sura.arlmanager.infrastructure.r2dbc.presupuesto;

import static org.assertj.core.api.Assertions.assertThat;

import com.sura.arlmanager.domain.model.port.PresupuestoAnualGateway;
import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import com.sura.arlmanager.infrastructure.r2dbc.databuilder.PresupuestoAnualR2dbcTestDataBuilder;
import com.sura.arlmanager.infrastructure.r2dbc.databuilder.PresupuestoAnualR2dbcTestDataBuilder.EmpresaConPresupuesto;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import reactor.test.StepVerifier;

@SpringBootTest(classes = PresupuestoAnualR2dbcAdapterTest.TestApplication.class)
@Testcontainers
class PresupuestoAnualR2dbcAdapterTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private PresupuestoAnualGateway presupuestoAnualGateway;

    @Autowired
    private DatabaseClient databaseClient;

    @DynamicPropertySource
    static void registrarConexionR2dbc(DynamicPropertyRegistry propiedades) {
        propiedades.add(
                "spring.r2dbc.url",
                () -> "r2dbc:postgresql://"
                        + POSTGRES.getHost()
                        + ":"
                        + POSTGRES.getFirstMappedPort()
                        + "/"
                        + POSTGRES.getDatabaseName());
        propiedades.add("spring.r2dbc.username", POSTGRES::getUsername);
        propiedades.add("spring.r2dbc.password", POSTGRES::getPassword);
    }

    @Test
    void debeCrearYConsultarElPresupuestoPorEmpresaYVigencia() {
        // Arrange
        EmpresaConPresupuesto fixture = PresupuestoAnualR2dbcTestDataBuilder.empresaConPresupuesto();

        // Act
        StepVerifier.create(insertarEmpresa(fixture)
                        .then(presupuestoAnualGateway.crear(fixture.presupuesto()))
                        .flatMap(presupuesto -> presupuestoAnualGateway.buscarPorEmpresaYVigencia(
                                presupuesto.empresaAfiliadaId(), presupuesto.vigencia())))
                // Assert
                .expectNext(fixture.presupuesto())
                .verifyComplete();
    }

    @Test
    void debeIncrementarLaVersionAlActualizarElPresupuesto() {
        // Arrange
        EmpresaConPresupuesto fixture = PresupuestoAnualR2dbcTestDataBuilder.empresaConPresupuesto();
        PresupuestoAnual actualizado = PresupuestoAnualR2dbcTestDataBuilder.actualizarMonto(
                fixture.presupuesto(), new BigDecimal("1200.00"), 0L);

        // Act
        StepVerifier.create(insertarEmpresa(fixture)
                        .then(presupuestoAnualGateway.crear(fixture.presupuesto()))
                        .flatMap(guardado -> presupuestoAnualGateway.actualizar(actualizado)))
                // Assert
                .assertNext(presupuesto -> {
                    assertThat(presupuesto.version()).isEqualTo(1L);
                    assertThat(presupuesto.montoAsignado()).isEqualByComparingTo("1200.00");
                    assertThat(presupuesto.saldoDisponible()).isEqualByComparingTo("900.00");
                })
                .verifyComplete();
    }

    @Test
    void debeRechazarLaActualizacionConUnaVersionObsoleta() {
        // Arrange
        EmpresaConPresupuesto fixture = PresupuestoAnualR2dbcTestDataBuilder.empresaConPresupuesto();
        PresupuestoAnual actualizado = PresupuestoAnualR2dbcTestDataBuilder.actualizarMonto(
                fixture.presupuesto(), new BigDecimal("1200.00"), 0L);

        // Act
        StepVerifier.create(insertarEmpresa(fixture)
                        .then(presupuestoAnualGateway.crear(fixture.presupuesto()))
                        .flatMap(guardado -> presupuestoAnualGateway.actualizar(actualizado))
                        .then(presupuestoAnualGateway.actualizar(fixture.presupuesto())))
                // Assert
                .expectError(OptimisticLockingFailureException.class)
                .verify();
    }

    private reactor.core.publisher.Mono<Void> insertarEmpresa(EmpresaConPresupuesto fixture) {
        return databaseClient
                .sql("INSERT INTO grupo_prevencion (id, nombre) VALUES (:id, :nombre)")
                .bind("id", fixture.grupoPrevencionId())
                .bind("nombre", "Grupo de prueba")
                .then()
                .then(databaseClient
                        .sql("INSERT INTO empresa_afiliada (id, grupo_prevencion_id, razon_social) "
                                + "VALUES (:id, :grupoPrevencionId, :razonSocial)")
                        .bind("id", fixture.presupuesto().empresaAfiliadaId())
                        .bind("grupoPrevencionId", fixture.grupoPrevencionId())
                        .bind("razonSocial", "Empresa de prueba")
                        .then());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(PresupuestoAnualR2dbcAdapter.class)
    static class TestApplication {}
}
