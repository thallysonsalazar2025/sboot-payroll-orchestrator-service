package br.com.payroll.orchestrator.adapter.out.messaging;

import br.com.payroll.orchestrator.domain.exception.IntegrationException;
import br.com.payroll.orchestrator.domain.model.PdfGenerationMessage;
import br.com.payroll.orchestrator.domain.port.PdfMessagePublisher;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMqPdfMessagePublisher implements PdfMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange:payroll.exchange}")
    private String exchange;

    @Value("${app.messaging.routing-key:payroll.pdf.generate}")
    private String routingKey;

    @Override
    @Retry(name = "pdfMessagePublisher", fallbackMethod = "fallback")
    @CircuitBreaker(name = "pdfMessagePublisher", fallbackMethod = "fallback")
    @Bulkhead(name = "pdfMessagePublisher")
    public void publish(PdfGenerationMessage message) {
        log.info("Publicando payload para geração de PDF. exchange={}, routingKey={}, messageId={}",
                exchange, routingKey, message.messageId());
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    @SuppressWarnings("unused")
    private void fallback(PdfGenerationMessage message, Throwable throwable) {
        throw new IntegrationException("Falha ao publicar payload no broker RabbitMQ: " + throwable.getMessage());
    }
}
