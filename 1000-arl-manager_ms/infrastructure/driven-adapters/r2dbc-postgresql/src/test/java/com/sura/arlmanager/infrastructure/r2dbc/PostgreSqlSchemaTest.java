package com.sura.arlmanager.infrastructure.r2dbc;

import static org.assertj.core.api.Assertions.assertThat;

import com.sura.arlmanager.infrastructure.r2dbc.databuilder.PostgreSqlSchemaTestDataBuilder;
import com.sura.arlmanager.infrastructure.r2dbc.databuilder.PostgreSqlSchemaTestDataBuilder.EmpresaFixture;
import com.sura.arlmanager.infrastructure.r2dbc.databuilder.PostgreSqlSchemaTestDataBuilder.PresupuestoFixture;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(classes = PostgreSqlSchemaTest.TestApplication.class)
@Testcontainers
class PostgreSqlSchemaTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void debeCrearLasTablasCoreYLaTablaDeAuditoria() {
        // Arrange
        List<String> tablasEsperadas = List.of(
                "grupo_prevencion",
                "empresa_afiliada",
                "usuario",
                "presupuesto_anual",
                "plan_trabajo",
                "orden_servicio",
                "evaluacion_riesgo",
                "auditoria_inmutable",
                "outbox_event");

        // Act
        Integer totalTablas = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_name IN (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                Integer.class,
                tablasEsperadas.toArray());

        // Assert
        assertThat(totalTablas).isEqualTo(tablasEsperadas.size());
    }

    @Test
    void debeCrearIndicesParaClavesForaneasYEventosPendientes() {
        // Arrange
        List<String> indicesEsperados = List.of(
                "idx_empresa_grupo",
                "idx_usuario_grupo",
                "idx_presupuesto_empresa",
                "idx_plan_empresa",
                "idx_orden_plan",
                "idx_orden_prestador",
                "idx_orden_profesional",
                "idx_auditoria_entidad",
                "idx_outbox_unprocessed");

        // Act
        Integer totalIndices = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM pg_indexes
                WHERE schemaname = 'public'
                  AND indexname IN (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """,
                Integer.class,
                indicesEsperados.toArray());

        // Assert
        assertThat(totalIndices).isEqualTo(indicesEsperados.size());
    }

    @Test
    void debeRechazarSaldoDisponibleNegativoYEcuacionInconsistente() {
        // Arrange
        EmpresaFixture empresa = insertarEmpresaAfiliada();
        PresupuestoFixture presupuesto = PostgreSqlSchemaTestDataBuilder.presupuestoValido(empresa.empresaAfiliadaId());

        // Act
        Throwable saldoNegativo = capturarErrorAlInsertarPresupuesto(presupuesto, new java.math.BigDecimal("-1.00"));
        Throwable ecuacionInconsistente =
                capturarErrorAlInsertarPresupuesto(presupuesto, new java.math.BigDecimal("699.00"));

        // Assert
        assertThat(saldoNegativo).isInstanceOf(DataAccessException.class);
        assertThat(ecuacionInconsistente).isInstanceOf(DataAccessException.class);
    }

    @Test
    void debeImpedirActualizarOBorrarLaAuditoria() {
        // Arrange
        EmpresaFixture empresa = insertarEmpresaAfiliada();
        String insertSql =
                """
                INSERT INTO auditoria_inmutable (
                    id, entidad, entidad_id, usuario_id, rol, operacion,
                    valores_anteriores, valores_nuevos
                )
                VALUES (?, 'PresupuestoAnual', ?, ?, 'LIDER_PREVENCION', 'CREAR', '{}'::jsonb, '{"monto":1000}'::jsonb)
                """;
        jdbcTemplate.update(insertSql, java.util.UUID.randomUUID(), empresa.empresaAfiliadaId(), empresa.usuarioId());

        // Act
        Throwable errorActualizar = capturarErrorAlActualizarAuditoria(empresa);
        Throwable errorBorrar = capturarErrorAlBorrarAuditoria(empresa);

        // Assert
        assertThat(errorActualizar).isInstanceOf(DataAccessException.class);
        assertThat(errorBorrar).isInstanceOf(DataAccessException.class);
    }

    private EmpresaFixture insertarEmpresaAfiliada() {
        EmpresaFixture empresa = PostgreSqlSchemaTestDataBuilder.empresaAfiliada();
        jdbcTemplate.update(
                "INSERT INTO grupo_prevencion (id, nombre) VALUES (?, ?)",
                empresa.grupoPrevencionId(),
                "Grupo de prueba");
        jdbcTemplate.update(
                "INSERT INTO empresa_afiliada (id, grupo_prevencion_id, razon_social) VALUES (?, ?, ?)",
                empresa.empresaAfiliadaId(),
                empresa.grupoPrevencionId(),
                "Empresa de prueba");
        jdbcTemplate.update(
                "INSERT INTO usuario (id, grupo_prevencion_id, rol) VALUES (?, ?, ?)",
                empresa.usuarioId(),
                empresa.grupoPrevencionId(),
                "LIDER_PREVENCION");
        return empresa;
    }

    private Throwable capturarErrorAlInsertarPresupuesto(
            PresupuestoFixture presupuesto, java.math.BigDecimal saldoDisponible) {
        return capturarError(() -> jdbcTemplate.update(
                """
                INSERT INTO presupuesto_anual (
                    id, empresa_afiliada_id, vigencia, monto_asignado,
                    saldo_comprometido, saldo_ejecutado, saldo_disponible
                )
                VALUES (?, ?, 2026, ?, ?, ?, ?)
                """,
                presupuesto.id(),
                presupuesto.empresaAfiliadaId(),
                presupuesto.montoAsignado(),
                presupuesto.saldoComprometido(),
                presupuesto.saldoEjecutado(),
                saldoDisponible));
    }

    private Throwable capturarErrorAlActualizarAuditoria(EmpresaFixture empresa) {
        return capturarError(() -> jdbcTemplate.update(
                "UPDATE auditoria_inmutable SET entidad = ? WHERE usuario_id = ?",
                "OrdenServicio",
                empresa.usuarioId()));
    }

    private Throwable capturarErrorAlBorrarAuditoria(EmpresaFixture empresa) {
        return capturarError(
                () -> jdbcTemplate.update("DELETE FROM auditoria_inmutable WHERE usuario_id = ?", empresa.usuarioId()));
    }

    private Throwable capturarError(Runnable operacion) {
        try {
            operacion.run();
            return new AssertionError("La base de datos debio rechazar la operacion");
        } catch (DataAccessException error) {
            return error;
        }
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = R2dbcAutoConfiguration.class)
    static class TestApplication {}
}
