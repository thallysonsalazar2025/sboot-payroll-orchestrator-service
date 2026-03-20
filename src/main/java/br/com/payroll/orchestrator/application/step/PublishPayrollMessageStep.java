package br.com.payroll.orchestrator.application.step;

import br.com.payroll.orchestrator.domain.model.OrchestrationResult;
import br.com.payroll.orchestrator.domain.model.ProcessingContext;
import br.com.payroll.orchestrator.domain.port.PayrollMessagePublisher;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(50)
@RequiredArgsConstructor
public class PublishPayrollMessageStep implements FlowStep {

    private final PayrollMessagePublisher payrollMessagePublisher;

    @Override
    public ProcessingContext execute(ProcessingContext context) {
        payrollMessagePublisher.publish(context.payrollPayloadMessage());

        OrchestrationResult result = OrchestrationResult.builder()
                .orchestrationId(UUID.randomUUID().toString())
                .correlationId(context.request().correlationId())
                .messageId(context.payrollPayloadMessage().messageId())
                .status("ORCHESTRATED")
                .processedAt(Instant.now())
                .reusedResult(false)
                .build();

        return context.toBuilder().orchestrationResult(result).build();
    }

    @Override
    public String name() {
        return "publish-payroll-message";
    }
}
