package com.sura.arlmanager.infrastructure.r2dbc.transaction;

import com.sura.arlmanager.domain.model.port.TransaccionPort;
import java.util.Objects;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

@Component
public class R2dbcTransaccionAdapter implements TransaccionPort {
    private final TransactionalOperator transactionalOperator;

    public R2dbcTransaccionAdapter(TransactionalOperator transactionalOperator) {
        this.transactionalOperator = transactionalOperator;
    }

    @Override
    public <T> Mono<T> ejecutar(Supplier<Mono<T>> operacion) {
        Objects.requireNonNull(operacion, "operacion es obligatoria");
        return transactionalOperator.transactional(Mono.defer(operacion));
    }
}
