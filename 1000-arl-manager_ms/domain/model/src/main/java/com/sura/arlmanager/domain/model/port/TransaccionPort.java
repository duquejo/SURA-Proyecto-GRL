package com.sura.arlmanager.domain.model.port;

import java.util.function.Supplier;
import reactor.core.publisher.Mono;

public interface TransaccionPort {
    <T> Mono<T> ejecutar(Supplier<Mono<T>> operacion);
}
