package com.sura.arlmanager.infrastructure.r2dbc.auditoria;

import static com.sura.arlmanager.infrastructure.r2dbc.databuilder.AuditoriaPresupuestoR2dbcTestDataBuilder.escenario;
import static org.assertj.core.api.Assertions.assertThat;

import com.sura.arlmanager.domain.model.port.AuditoriaPresupuestoGateway;
import com.sura.arlmanager.infrastructure.r2dbc.databuilder.AuditoriaPresupuestoR2dbcTestDataBuilder.EscenarioAuditoria;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(classes = AuditoriaPresupuestoR2dbcAdapterTest.TestApplication.class)
@Testcontainers
class AuditoriaPresupuestoR2dbcAdapterTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private AuditoriaPresupuestoGateway auditoriaPresupuestoGateway;

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
    void registraCreacionConUsuarioRolYValoresNuevos() {
        // Arrange
        EscenarioAuditoria fixture = escenario();

        // Act
        Mono<AuditoriaAlmacenada> resultado = insertarActor(fixture)
                .then(auditoriaPresupuestoGateway.registrar(
                        fixture.presupuestoNuevo().id(), fixture.contextoUsuario(), null, fixture.presupuestoNuevo()))
                .then(buscarAuditoria(fixture.presupuestoNuevo().id()));

        // Assert
        StepVerifier.create(resultado)
                .assertNext(auditoria -> {
                    assertThat(auditoria.entidad()).isEqualTo("PresupuestoAnual");
                    assertThat(auditoria.operacion()).isEqualTo("CREAR");
                    assertThat(auditoria.rol()).isEqualTo("LIDER_PREVENCION");
                    assertThat(auditoria.valoresAnteriores()).isEqualTo("{}");
                    assertThat(auditoria.valoresNuevos()).contains("\"montoAsignado\": 1500.00");
                })
                .verifyComplete();
    }

    @Test
    void registraValoresAnterioresAlAjustarPresupuesto() {
        // Arrange
        EscenarioAuditoria fixture = escenario();

        // Act
        Mono<AuditoriaAlmacenada> resultado = insertarActor(fixture)
                .then(auditoriaPresupuestoGateway.registrar(
                        fixture.presupuestoNuevo().id(),
                        fixture.contextoUsuario(),
                        fixture.presupuestoAnterior(),
                        fixture.presupuestoNuevo()))
                .then(buscarAuditoria(fixture.presupuestoNuevo().id()));

        // Assert
        StepVerifier.create(resultado)
                .assertNext(auditoria -> {
                    assertThat(auditoria.operacion()).isEqualTo("ACTUALIZAR");
                    assertThat(auditoria.valoresAnteriores()).contains("\"montoAsignado\": 1000.00");
                    assertThat(auditoria.valoresNuevos()).contains("\"montoAsignado\": 1500.00");
                })
                .verifyComplete();
    }

    private Mono<Void> insertarActor(EscenarioAuditoria fixture) {
        return databaseClient
                .sql("INSERT INTO grupo_prevencion (id, nombre) VALUES (:id, :nombre)")
                .bind("id", fixture.grupoPrevencionId())
                .bind("nombre", "Grupo de prueba")
                .then()
                .then(databaseClient
                        .sql("INSERT INTO empresa_afiliada (id, grupo_prevencion_id, razon_social) "
                                + "VALUES (:id, :grupoPrevencionId, :razonSocial)")
                        .bind("id", fixture.empresaAfiliadaId())
                        .bind("grupoPrevencionId", fixture.grupoPrevencionId())
                        .bind("razonSocial", "Empresa de prueba")
                        .then())
                .then(databaseClient
                        .sql("INSERT INTO usuario (id, grupo_prevencion_id, rol) "
                                + "VALUES (:id, :grupoPrevencionId, :rol)")
                        .bind("id", fixture.contextoUsuario().usuarioId())
                        .bind("grupoPrevencionId", fixture.grupoPrevencionId())
                        .bind("rol", fixture.contextoUsuario().rol().name())
                        .then());
    }

    private Mono<AuditoriaAlmacenada> buscarAuditoria(java.util.UUID presupuestoId) {
        return databaseClient
                .sql(
                        """
                    SELECT entidad, rol, operacion,
                           valores_anteriores::text AS valores_anteriores,
                           valores_nuevos::text AS valores_nuevos
                    FROM auditoria_inmutable
                    WHERE entidad_id = :presupuestoId
                    """)
                .bind("presupuestoId", presupuestoId)
                .map((fila, metadata) -> new AuditoriaAlmacenada(
                        fila.get("entidad", String.class),
                        fila.get("rol", String.class),
                        fila.get("operacion", String.class),
                        fila.get("valores_anteriores", String.class),
                        fila.get("valores_nuevos", String.class)))
                .one();
    }

    private record AuditoriaAlmacenada(
            String entidad, String rol, String operacion, String valoresAnteriores, String valoresNuevos) {}

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(AuditoriaPresupuestoR2dbcAdapter.class)
    static class TestApplication {}
}
