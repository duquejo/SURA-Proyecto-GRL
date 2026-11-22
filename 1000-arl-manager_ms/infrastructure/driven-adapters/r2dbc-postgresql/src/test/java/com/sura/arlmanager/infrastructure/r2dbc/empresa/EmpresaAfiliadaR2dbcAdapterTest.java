package com.sura.arlmanager.infrastructure.r2dbc.empresa;

import static com.sura.arlmanager.infrastructure.r2dbc.databuilder.EmpresaAfiliadaR2dbcTestDataBuilder.empresaConGrupo;

import com.sura.arlmanager.domain.model.empresa.EmpresaAfiliada;
import com.sura.arlmanager.domain.model.exception.AccesoDenegadoGrupoException;
import com.sura.arlmanager.domain.model.port.EmpresaAfiliadaGateway;
import com.sura.arlmanager.infrastructure.r2dbc.databuilder.EmpresaAfiliadaR2dbcTestDataBuilder.EmpresaConGrupo;
import java.util.UUID;
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
import reactor.test.StepVerifier;

@SpringBootTest(classes = EmpresaAfiliadaR2dbcAdapterTest.TestApplication.class)
@Testcontainers
class EmpresaAfiliadaR2dbcAdapterTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private EmpresaAfiliadaGateway empresaAfiliadaGateway;

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
    void encuentraEmpresaSoloDentroDelGrupoSolicitado() {
        // Arrange
        EmpresaConGrupo fixture = empresaConGrupo();

        // Act
        StepVerifier.create(insertarEmpresa(fixture)
                        .then(empresaAfiliadaGateway.buscarPorIdYGrupoPrevencion(
                                fixture.empresaAfiliadaId(), fixture.grupoPrevencionId())))
                // Assert
                .expectNext(new EmpresaAfiliada(
                        fixture.empresaAfiliadaId(), fixture.grupoPrevencionId(), fixture.razonSocial()))
                .verifyComplete();
    }

    @Test
    void ocultaEmpresaCuandoElGrupoNoCoincide() {
        // Arrange
        EmpresaConGrupo fixture = empresaConGrupo();
        UUID grupoAjeno = UUID.randomUUID();

        // Act
        StepVerifier.create(insertarEmpresa(fixture)
                        .then(empresaAfiliadaGateway.buscarPorIdYGrupoPrevencion(
                                fixture.empresaAfiliadaId(), grupoAjeno)))
                // Assert
                .expectError(AccesoDenegadoGrupoException.class)
                .verify();
    }

    private reactor.core.publisher.Mono<Void> insertarEmpresa(EmpresaConGrupo fixture) {
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
                        .bind("razonSocial", fixture.razonSocial())
                        .then());
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(EmpresaAfiliadaR2dbcAdapter.class)
    static class TestApplication {}
}
