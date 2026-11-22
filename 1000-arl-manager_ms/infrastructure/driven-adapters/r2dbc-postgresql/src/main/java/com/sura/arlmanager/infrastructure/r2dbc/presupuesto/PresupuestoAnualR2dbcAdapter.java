package com.sura.arlmanager.infrastructure.r2dbc.presupuesto;

import com.sura.arlmanager.domain.model.port.PresupuestoAnualGateway;
import com.sura.arlmanager.domain.model.presupuesto.PresupuestoAnual;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public class PresupuestoAnualR2dbcAdapter implements PresupuestoAnualGateway {
    private final R2dbcEntityTemplate template;

    public PresupuestoAnualR2dbcAdapter(R2dbcEntityTemplate template) {
        this.template = template;
    }

    @Override
    public Mono<PresupuestoAnual> crear(PresupuestoAnual presupuestoAnual) {
        PresupuestoAnualEntity entidad = PresupuestoAnualEntity.desdeDominio(presupuestoAnual, null);
        return template.insert(entidad).map(entidadInsertada -> entidadInsertada.aDominio());
    }

    @Override
    public Mono<PresupuestoAnual> actualizar(PresupuestoAnual presupuestoAnual) {
        PresupuestoAnualEntity entidad =
                PresupuestoAnualEntity.desdeDominio(presupuestoAnual, presupuestoAnual.version());
        return template.update(entidad).map(entidadActualizada -> entidadActualizada.aDominio());
    }

    @Override
    public Mono<PresupuestoAnual> buscarPorEmpresaYVigencia(UUID empresaAfiliadaId, int vigencia) {
        Query consulta = Query.query(Criteria.where("empresaAfiliadaId")
                .is(Objects.requireNonNull(empresaAfiliadaId))
                .and("vigencia")
                .is(vigencia));
        return template.select(PresupuestoAnualEntity.class)
                .matching(consulta)
                .one()
                .map(entidadEncontrada -> entidadEncontrada.aDominio());
    }
}
