package com.sura.arlmanager.infrastructure.web.error;

import com.sura.arlmanager.domain.model.exception.NegocioException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class ErrorNegocioHandler {
    @ExceptionHandler(NegocioException.class)
    public ResponseEntity<ErrorNegocioRFC7807> manejar(NegocioException excepcion, ServerWebExchange intercambio) {
        HttpStatus estado =
                esRestriccionDeAcceso(excepcion.codigoRegla()) ? HttpStatus.FORBIDDEN : HttpStatus.UNPROCESSABLE_ENTITY;
        ErrorNegocioRFC7807 problema = new ErrorNegocioRFC7807(
                "about:blank",
                estado == HttpStatus.FORBIDDEN ? "Acceso denegado" : "Regla de negocio infringida",
                estado.value(),
                excepcion.getMessage(),
                intercambio.getRequest().getURI().getPath(),
                excepcion.codigoRegla(),
                excepcion.getMessage(),
                Instant.now());
        return ResponseEntity.status(estado)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problema);
    }

    private boolean esRestriccionDeAcceso(String codigoRegla) {
        return "RN-09".equals(codigoRegla) || "RN-10".equals(codigoRegla);
    }

    public record ErrorNegocioRFC7807(
            String type,
            String title,
            int status,
            String detail,
            String instance,
            String codigoRegla,
            String error,
            Instant timestamp) {}
}
