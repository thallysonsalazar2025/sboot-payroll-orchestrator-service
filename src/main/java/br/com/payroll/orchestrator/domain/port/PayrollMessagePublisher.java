package br.com.payroll.orchestrator.domain.port;

import br.com.payroll.orchestrator.domain.model.PayrollPayloadMessage;

public interface PayrollMessagePublisher {
    void publish(PayrollPayloadMessage message);
}
