package com.sura.arlmanager.infrastructure.r2dbc.auditoria;

import com.sura.arlmanager.domain.model.port.AuditoriaPresupuestoGateway;
import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import com.sura.arlmanager.domain.model.seguridad.ContextoUsuario;
import java.util.Objects;
import java.util.UUID;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class AuditoriaPresupuestoR2dbcAdapter implements AuditoriaPresupuestoGateway {
    private final DatabaseClient databaseClient;

    public AuditoriaPresupuestoR2dbcAdapter(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Void> registrar(
            UUID presupuestoId,
            ContextoUsuario contextoUsuario,
            PresupuestoAnual presupuestoAnterior,
            PresupuestoAnual presupuestoNuevo) {
        Objects.requireNonNull(presupuestoId, "presupuestoId es obligatorio");
        Objects.requireNonNull(contextoUsuario, "contextoUsuario es obligatorio");
        Objects.requireNonNull(presupuestoNuevo, "presupuestoNuevo es obligatorio");

        String valoresAnteriores = presupuestoAnterior == null ? "{}" : serializar(presupuestoAnterior);
        String operacion = presupuestoAnterior == null ? "CREAR" : "ACTUALIZAR";

        return databaseClient
                .sql(
                        """
                    INSERT INTO auditoria_inmutable (
                        id, entidad, entidad_id, usuario_id, rol, operacion,
                        valores_anteriores, valores_nuevos
                    )
                    VALUES (
                        :id, 'PresupuestoAnual', :entidadId, :usuarioId, :rol, :operacion,
                        CAST(:valoresAnteriores AS jsonb), CAST(:valoresNuevos AS jsonb)
                    )
                    """)
                .bind("id", UUID.randomUUID())
                .bind("entidadId", presupuestoId)
                .bind("usuarioId", contextoUsuario.usuarioId())
                .bind("rol", contextoUsuario.rol().name())
                .bind("operacion", operacion)
                .bind("valoresAnteriores", valoresAnteriores)
                .bind("valoresNuevos", serializar(presupuestoNuevo))
                .fetch()
                .rowsUpdated()
                .then();
    }

    private String serializar(PresupuestoAnual presupuesto) {
        return """
                {"vigencia":%d,"montoAsignado":%s,"saldoComprometido":%s,"saldoEjecutado":%s,"saldoDisponible":%s}
                """
                .formatted(
                        presupuesto.vigencia(),
                        presupuesto.montoAsignado().toPlainString(),
                        presupuesto.saldoComprometido().toPlainString(),
                        presupuesto.saldoEjecutado().toPlainString(),
                        presupuesto.saldoDisponible().toPlainString())
                .trim();
    }
}
