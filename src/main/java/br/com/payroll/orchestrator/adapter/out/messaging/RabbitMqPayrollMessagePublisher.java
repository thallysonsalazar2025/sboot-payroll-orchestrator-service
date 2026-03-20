package br.com.payroll.orchestrator.adapter.out.messaging;

import br.com.payroll.orchestrator.domain.exception.IntegrationException;
import br.com.payroll.orchestrator.domain.model.PayrollPayloadMessage;
import br.com.payroll.orchestrator.domain.port.PayrollMessagePublisher;
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
public class RabbitMqPayrollMessagePublisher implements PayrollMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.exchange:payroll.exchange}")
    private String exchange;

    @Value("${app.messaging.routing-key:payroll.orchestrated}")
    private String routingKey;

    @Override
    @Retry(name = "payrollMessagePublisher", fallbackMethod = "fallback")
    @CircuitBreaker(name = "payrollMessagePublisher", fallbackMethod = "fallback")
    @Bulkhead(name = "payrollMessagePublisher")
    public void publish(PayrollPayloadMessage message) {
        log.info("Publicando payload consolidado da folha. exchange={}, routingKey={}, messageId={}",
                exchange, routingKey, message.messageId());
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

    @SuppressWarnings("unused")
    private void fallback(PayrollPayloadMessage message, Throwable throwable) {
        throw new IntegrationException("Falha ao publicar payload consolidado no broker RabbitMQ: " + throwable.getMessage());
    }
}
