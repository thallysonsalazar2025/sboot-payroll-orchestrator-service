package br.com.payroll.orchestrator.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public DirectExchange payrollExchange() {
        return new DirectExchange("payroll.exchange", true, false);
    }

    @Bean
    public Queue payrollOrchestratedQueue() {
        return new Queue("payroll.orchestrated.queue", true);
    }

    @Bean
    public Binding payrollOrchestratedBinding(DirectExchange payrollExchange, Queue payrollOrchestratedQueue) {
        return BindingBuilder.bind(payrollOrchestratedQueue)
                .to(payrollExchange)
                .with("payroll.orchestrated");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
