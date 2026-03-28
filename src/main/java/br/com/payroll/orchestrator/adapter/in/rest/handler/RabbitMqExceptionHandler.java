package br.com.payroll.orchestrator.adapter.in.rest.handler;

import org.springframework.amqp.AmqpException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class RabbitMqExceptionHandler {

    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> handleAmqpException(AmqpException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "error", "Service Unavailable",
                        "details", List.of("Falha ao publicar payload consolidado no broker RabbitMQ: " + ex.getMessage())
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal Server Error", "details", List.of(ex.getMessage())));
    }
}
