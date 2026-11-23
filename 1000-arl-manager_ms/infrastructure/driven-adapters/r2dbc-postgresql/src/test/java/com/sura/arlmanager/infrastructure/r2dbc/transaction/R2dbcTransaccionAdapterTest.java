package com.sura.arlmanager.infrastructure.r2dbc.transaction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class R2dbcTransaccionAdapterTest {
    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private R2dbcTransaccionAdapter transaccionAdapter;

    @Test
    void ejecutaLaOperacionDentroDelOperadorTransaccional() {
        // Arrange
        Mono<String> operacion = Mono.just("resultado");
        when(transactionalOperator.transactional(org.mockito.ArgumentMatchers.<Mono<String>>any()))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        // Act
        Mono<String> resultado = transaccionAdapter.ejecutar(() -> operacion);

        // Assert
        StepVerifier.create(resultado)
                .assertNext(valor -> assertThat(valor).isEqualTo("resultado"))
                .verifyComplete();
        verify(transactionalOperator).transactional(org.mockito.ArgumentMatchers.<Mono<String>>any());
    }
}
