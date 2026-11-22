package com.sura.arlmanager.domain.usecase.port;

import java.util.function.Supplier;
import reactor.core.publisher.Mono;

public interface TransaccionPort {
    <T> Mono<T> ejecutar(Supplier<Mono<T>> operacion);
}
