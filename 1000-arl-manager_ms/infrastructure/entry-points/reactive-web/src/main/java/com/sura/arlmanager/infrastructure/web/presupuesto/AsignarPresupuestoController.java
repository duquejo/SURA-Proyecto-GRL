package com.sura.arlmanager.infrastructure.web.presupuesto;

import com.sura.arlmanager.domain.model.seguridad.ContextoUsuario;
import com.sura.arlmanager.domain.usecase.presupuesto.AsignacionPresupuestoResultado;
import com.sura.arlmanager.domain.usecase.presupuesto.AsignarPresupuestoUseCase;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Objects;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/empresas-afiliadas")
public class AsignarPresupuestoController {
    private final AsignarPresupuestoUseCase asignarPresupuestoUseCase;

    public AsignarPresupuestoController(AsignarPresupuestoUseCase asignarPresupuestoUseCase) {
        this.asignarPresupuestoUseCase = asignarPresupuestoUseCase;
    }

    @PostMapping("/{empresaAfiliadaId}/presupuestos")
    public Mono<ResponseEntity<PresupuestoEmpresaResponse>> asignarPresupuesto(
            @PathVariable UUID empresaAfiliadaId,
            @RequestHeader("X-Idempotency-Key") UUID idempotencyKey,
            @RequestBody AsignarPresupuestoRequest solicitud,
            Principal principal) {
        Objects.requireNonNull(idempotencyKey, "X-Idempotency-Key es obligatorio");
        Objects.requireNonNull(solicitud, "la solicitud es obligatoria");

        return Mono.justOrEmpty(principal)
                .filter(ContextoUsuario.class::isInstance)
                .cast(ContextoUsuario.class)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED)))
                .flatMap(contextoUsuario -> asignarPresupuestoUseCase.ejecutar(
                        empresaAfiliadaId, solicitud.vigencia(), solicitud.montoAsignado(), contextoUsuario))
                .map(AsignarPresupuestoController::respuesta)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    private static PresupuestoEmpresaResponse respuesta(AsignacionPresupuestoResultado asignacion) {
        return new PresupuestoEmpresaResponse(
                asignacion.empresaAfiliada().id(),
                asignacion.empresaAfiliada().razonSocial(),
                asignacion.presupuesto().vigencia(),
                asignacion.presupuesto().montoAsignado(),
                asignacion.presupuesto().saldoComprometido(),
                asignacion.presupuesto().saldoEjecutado(),
                asignacion.presupuesto().saldoDisponible());
    }

    public record AsignarPresupuestoRequest(int vigencia, BigDecimal montoAsignado) {}

    public record PresupuestoEmpresaResponse(
            UUID empresaAfiliadaId,
            String razonSocial,
            int vigencia,
            BigDecimal presupuestoAnual,
            BigDecimal saldoComprometido,
            BigDecimal saldoEjecutado,
            BigDecimal saldoDisponible) {}
}
