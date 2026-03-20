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
    public Queue payrollPdfQueue() {
        return new Queue("payroll.pdf.generate.queue", true);
    }

    @Bean
    public Binding payrollPdfBinding(DirectExchange payrollExchange, Queue payrollPdfQueue) {
        return BindingBuilder.bind(payrollPdfQueue)
                .to(payrollExchange)
                .with("payroll.pdf.generate");
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
