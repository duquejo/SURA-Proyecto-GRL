package com.sura.arlmanager.domain.usecase.presupuesto;

import com.sura.arlmanager.domain.model.exception.AccesoDenegadoGrupoException;
import com.sura.arlmanager.domain.model.exception.NegocioException;
import com.sura.arlmanager.domain.model.exception.RolNoAutorizadoException;
import com.sura.arlmanager.domain.model.port.AuditoriaPresupuestoGateway;
import com.sura.arlmanager.domain.model.port.EmpresaAfiliadaGateway;
import com.sura.arlmanager.domain.model.port.PresupuestoAnualGateway;
import com.sura.arlmanager.domain.model.port.TransaccionPort;
import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import com.sura.arlmanager.domain.model.seguridad.ContextoUsuario;
import com.sura.arlmanager.domain.model.seguridad.RolUsuario;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import reactor.core.publisher.Mono;

public class AsignarPresupuestoUseCase {
    private static final int VIGENCIA_MINIMA = 2026;
    private static final BigDecimal CENTAVO = new BigDecimal("0.01");
    private static final BigDecimal MONTO_MAXIMO = new BigDecimal("9999999999999.99");

    private final PresupuestoAnualGateway presupuestoAnualGateway;
    private final EmpresaAfiliadaGateway empresaAfiliadaGateway;
    private final AuditoriaPresupuestoGateway auditoriaPresupuestoGateway;
    private final TransaccionPort transaccionPort;

    public AsignarPresupuestoUseCase(
            PresupuestoAnualGateway presupuestoAnualGateway,
            EmpresaAfiliadaGateway empresaAfiliadaGateway,
            AuditoriaPresupuestoGateway auditoriaPresupuestoGateway,
            TransaccionPort transaccionPort) {
        this.presupuestoAnualGateway = presupuestoAnualGateway;
        this.empresaAfiliadaGateway = empresaAfiliadaGateway;
        this.auditoriaPresupuestoGateway = auditoriaPresupuestoGateway;
        this.transaccionPort = transaccionPort;
    }

    public Mono<AsignacionPresupuestoResultado> ejecutar(
            UUID empresaAfiliadaId, int vigencia, BigDecimal montoAsignado, ContextoUsuario contextoUsuario) {
        if (contextoUsuario.rol() != RolUsuario.LIDER_PREVENCION) {
            return Mono.error(
                    new RolNoAutorizadoException("Solo el Lider en prevencion puede asignar el Presupuesto anual"));
        }
        if (vigencia < VIGENCIA_MINIMA) {
            return Mono.error(new NegocioException("RN-03", "La vigencia del Presupuesto anual no es valida"));
        }
        if (montoAsignado == null || montoAsignado.signum() <= 0) {
            return Mono.error(new NegocioException("RN-03", "El monto asignado debe ser mayor que cero"));
        }
        if (montoAsignado.remainder(CENTAVO).signum() != 0 || montoAsignado.compareTo(MONTO_MAXIMO) > 0) {
            return Mono.error(new NegocioException(
                    "RN-03", "El monto asignado debe estar en centavos y dentro del rango permitido"));
        }

        return empresaAfiliadaGateway
                .buscarPorIdYGrupoPrevencion(empresaAfiliadaId, contextoUsuario.grupoPrevencionId())
                .switchIfEmpty(Mono.error(
                        new AccesoDenegadoGrupoException("La Empresa afiliada no esta disponible para este grupo")))
                .flatMap(empresa -> transaccionPort.ejecutar(() -> presupuestoAnualGateway
                        .buscarPorEmpresaYVigencia(empresaAfiliadaId, vigencia)
                        .map(Optional::of)
                        .defaultIfEmpty(Optional.empty())
                        .flatMap(presupuesto -> presupuesto
                                .map(existente -> actualizar(empresa, existente, montoAsignado, contextoUsuario))
                                .orElseGet(() -> crear(empresa, vigencia, montoAsignado, contextoUsuario)))));
    }

    private Mono<AsignacionPresupuestoResultado> crear(
            com.sura.arlmanager.domain.model.empresa.EmpresaAfiliada empresa,
            int vigencia,
            BigDecimal montoAsignado,
            ContextoUsuario contextoUsuario) {
        PresupuestoAnual presupuesto = new PresupuestoAnual(
                UUID.randomUUID(),
                empresa.id(),
                vigencia,
                montoAsignado,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                montoAsignado,
                0L);
        return presupuestoAnualGateway.crear(presupuesto).flatMap(presupuestoCreado -> auditoriaPresupuestoGateway
                .registrar(presupuestoCreado.id(), contextoUsuario, null, presupuestoCreado)
                .thenReturn(new AsignacionPresupuestoResultado(empresa, presupuestoCreado)));
    }

    private Mono<AsignacionPresupuestoResultado> actualizar(
            com.sura.arlmanager.domain.model.empresa.EmpresaAfiliada empresa,
            PresupuestoAnual presupuestoExistente,
            BigDecimal montoAsignado,
            ContextoUsuario contextoUsuario) {
        BigDecimal saldosNoDisponibles =
                presupuestoExistente.saldoComprometido().add(presupuestoExistente.saldoEjecutado());
        if (montoAsignado.compareTo(saldosNoDisponibles) < 0) {
            return Mono.error(new NegocioException(
                    "RN-03", "El monto asignado no puede ser menor a los saldos comprometido y ejecutado"));
        }

        BigDecimal saldoDisponible = montoAsignado.subtract(saldosNoDisponibles);
        PresupuestoAnual presupuestoActualizado = new PresupuestoAnual(
                presupuestoExistente.id(),
                presupuestoExistente.empresaAfiliadaId(),
                presupuestoExistente.vigencia(),
                montoAsignado,
                presupuestoExistente.saldoComprometido(),
                presupuestoExistente.saldoEjecutado(),
                saldoDisponible,
                presupuestoExistente.version());
        return presupuestoAnualGateway
                .actualizar(presupuestoActualizado)
                .flatMap(presupuestoGuardado -> auditoriaPresupuestoGateway
                        .registrar(presupuestoGuardado.id(), contextoUsuario, presupuestoExistente, presupuestoGuardado)
                        .thenReturn(new AsignacionPresupuestoResultado(empresa, presupuestoGuardado)));
    }
}
